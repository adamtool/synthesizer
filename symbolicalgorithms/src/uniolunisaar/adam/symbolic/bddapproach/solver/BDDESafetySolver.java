package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.TokenFlow;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameWithInitialEnvStrategyBuilder;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * @author Manuel Gieseking
 */
public class BDDESafetySolver extends BDDSolver<Safety> {

    // Domains for predecessor and successor for each token
    private BDDDomain[][] GOODCHAIN; // in the view of the enviroment
    private BDDDomain[] OBAD; // from the side of the environment

    /**
     * Creates a new existential safety solver for a given game.
     *
     * @param net - the Petri game to solve.
     * @param skipTests - should the tests for safe and bounded and other
     * preconditions be skipped?
     * @param opts - the options for the solver.
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    BDDESafetySolver(PetriNet net, boolean skipTests, Safety win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, win, opts);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Creates the variables for this solver. This have to be overriden since
     * the flag if the place has been newly occupied in this state has to be
     * coded additionally.
     *
     * Codierung: p_i_0 - Environment Token n - TokenCount gc=1 states newly
     * token belongs to a good chain.
     *
     * |p_i_0|gc|p_i_1|gc|top|t_1|...|t_m| ... |p_i_n|gc|top|t_1|...|t_m|
     */
    @Override
    void createVariables() {
        int tokencount = getGame().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        GOODCHAIN = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        OBAD = new BDDDomain[2];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (!getGame().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0;
            PLACES[i][0] = getFactory().extDomain(getGame().getPlaces()[0].size() + add);
            GOODCHAIN[i][0] = getFactory().extDomain(2);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getGame().getPlaces()[j + 1].size() + add);
                // good chains
                GOODCHAIN[i][j + 1] = getFactory().extDomain(2);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
            OBAD[i] = getFactory().extDomain(2);
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        // Env place
        sb.append("(");
        String id = BDDTools.getPlaceIDByBin(dcs, PLACES[pos][0], getGame().getPlaces()[0], getGame().isConcurrencyPreserving());
        sb.append(id);
        if (!id.equals("-")) {
            sb.append(", ");
            sb.append(BDDTools.getGoodChainFlagByBin(dcs, GOODCHAIN[pos][0]));
        }
        sb.append(")").append("\n");
        for (int j = 0; j < getGame().getMaxTokenCount() - 1; j++) {
            sb.append("(");
            String sid = BDDTools.getPlaceIDByBin(dcs, PLACES[pos][j + 1], getGame().getPlaces()[j + 1], getGame().isConcurrencyPreserving());
            sb.append(sid);
            if (!sid.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getGoodChainFlagByBin(dcs, GOODCHAIN[pos][j + 1]));
                sb.append(", ");
                sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], getGame().getTransitions()[j]));
            }
            sb.append(")").append("\n");
        }
        sb.append(BDDTools.getOverallBadByBin(dcs, OBAD[pos]));
        sb.append("\n");
        return sb.toString();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Good when all visible token belong to a good chain (for the evironment).
     */
    private BDD winningStates() {
        BDD ret = getOne();
        for (int i = 0; i < getGame().getMaxTokenCount(); i++) {
            if (i == 0 && getGame().getEnvPlaces().isEmpty()) { // no env token at all (skip the first block)
                continue;
            }
            if (AdamExtensions.isConcurrencyPreserving(getNet())) {
                ret.andWith(GOODCHAIN[0][i].ithVar(1));
            } else {
                ret.andWith(GOODCHAIN[0][i].ithVar(1).orWith(codePlace(0, 0, i)));
            }
        }
        ret.andWith(OBAD[0].ithVar(0));
        ret.andWith(term(0));
        ret = ret.or(getBufferedNDet());
        ret.orWith(deadSysDCS(0));
        return ret;
    }

    /**
     * Calculates a BDD representing all decision sets where no transition is
     * enabled.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing the terminating situations.
     */
    private BDD term(int pos) {
        BDD notEn = getOne();
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition transition : trans) {
            notEn.andWith(enabled(transition, pos).not());
        }
//        BDD notCh = getOne();
//        for (Transition transition : trans) {
//            if (!getGame().getSysTransition().contains(transition)) {
//                notCh.andWith(chosen(transition, pos).not());
//            }
//        }
//        BDD termType1 = notEn.orWith(type2().andWith(notCh));
//        return termType1;//.and(getWellformed());
        return notEn;
    }

    /**
     * Calculates a BDD representing all decision sets where the system decided
     * not to choose any enabled transition, but there exists at least one.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing the deadlocks of the Petri game.
     */
    private BDD deadSysDCS(int pos) {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, pos).not());
            buf.orWith(enabled(t, pos));
        }
        dead.andWith(buf);
        return dead.andWith(getTop().not());//.andWith(wellformed());
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

//%%%%%%%%%%%%%%%% ADAPTED to NOCC  / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    public BDD initial() {
        BDD init = super.initial();
        // all initial places which are marked as bad are on a good chain (for the environment)
        Marking m = getNet().getInitialMarking();
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            boolean good = false;
            for (Place p : getGame().getPlaces()[i]) {
                if (m.getToken(p).getValue() > 0) {
                    if (getWinningCondition().getBadPlaces().contains(p)) {
                        good = true;
                    }
                    break;
                }
            }
            init.andWith(GOODCHAIN[0][i].ithVar(good ? 1 : 0));
        }
        init.andWith(OBAD[0].ithVar(0));
        return init;
    }

    private BDD setGoodChainFlagForTransition(Transition t, Place post, int token) {
//        System.out.println("Post:" + post.getId());
        if (getWinningCondition().getBadPlaces().contains(post)) { // it is a bad place -> 1 (for the env)
            return GOODCHAIN[1][token].ithVar(1);
        }
        // 1 iff all predecessor which had been reached by a flow had gc=1
        BDD allPres = getOne();
        List<TokenFlow> fl = AdamExtensions.getTokenFlow(t);
        boolean hasEmptyPreset = false;
        for (TokenFlow tokenFlow : fl) {
            if (tokenFlow.getPostset().contains(post)) {
//                System.out.println(tokenFlow);
                for (Place p : tokenFlow.getPreset()) {
//                    System.out.println("Pre: " + p.getId());
                    int preToken = AdamExtensions.getPartition(p);
                    allPres.andWith(codePlace(p, 0, preToken));
                    allPres.andWith(GOODCHAIN[0][preToken].ithVar(1));
                }
                if (tokenFlow.getPreset().isEmpty()) {
                    hasEmptyPreset = true;
                    break;
                }
            }
        }
        allPres.biimpWith(GOODCHAIN[1][token].ithVar(1));
        return (hasEmptyPreset) ? GOODCHAIN[1][token].ithVar(0) : allPres;
    }

    // less restrictive version, let's it to often to 0. does it harm? don't now...
//    private BDD setGoodChainFlagForTransition(Transition t, Place post, int token) {
////        System.out.println("Post:" + post.getId());
//        if (getWinningCondition().getPlaces2Reach().contains(post)) { // it is a place2reach -> 1
//            return GOODCHAIN[1][token].ithVar(1);
//        }
//        BDD ret = GOODCHAIN[1][token].ithVar(0); // it is 0 or all predecessor which had been reached by a flow had gc=1
//        BDD allPres = getOne();
//        List<TokenFlow> fl = AdamExtensions.getTokenFlow(t);
//        for (TokenFlow tokenFlow : fl) {
//            if (tokenFlow.getPostset().contains(post)) {
//                for (Place p : tokenFlow.getPreset()) {
////                    System.out.println("Pre: " + p.getId());
//                    int preToken = AdamExtensions.getToken(p);
//                    allPres.andWith(codePlace(p, 0, preToken));
//                    allPres.andWith(GOODCHAIN[0][preToken].ithVar(1));
//                }
//                if (tokenFlow.getPreset().isEmpty()) {
//                    allPres.andWith(GOODCHAIN[1][token].ithVar(0));
//                } else {
//                    allPres.andWith(GOODCHAIN[1][token].ithVar(1));
//                }
//            }
//        }
//        ret.orWith(allPres);
//        return ret;
//    }
    private BDD setOverallBad(Transition t) { // for the enviroment means that a chain died before reaching a bad place, thus a bad chain died
        BDD exPreBad = getZero();
        List<TokenFlow> fls = AdamExtensions.getTokenFlow(t);
        for (Place p : t.getPreset()) {
            boolean hasFlow = false;
            for (TokenFlow fl : fls) {
                if (fl.getPreset().contains(p) && !fl.getPostset().isEmpty()) {
                    hasFlow = true;
                }
            }
            if (!hasFlow) {
                int token = AdamExtensions.getPartition(p);
                BDD preBad = codePlace(p, 0, token);
                preBad.andWith(GOODCHAIN[0][token].ithVar(0));
                exPreBad.orWith(preBad);
            }
        }
        return exPreBad.ite(OBAD[1].ithVar(1), OBAD[1].ithVar(0));
    }

    @Override
    BDD notUsedToken(int pos, int token) {
        BDD zero = super.notUsedToken(pos, token);
        zero.andWith(GOODCHAIN[pos][token].ithVar(0));
        return zero;
    }

    @Override
    void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
        // Positions in dcs not set with places of pre- or postset
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            if (visitedToken.contains(i)) { // jump over already visited token
                continue;
            }
            BDD pl = getZero();
            for (Place place : getGame().getPlaces()[i]) {
                // only sys places which are not within the preset of t
                // are possible to occure here
                BDD inner = getOne();
                inner.andWith(codePlace(place, 0, i));
                // pi=pi'
                inner.andWith(codePlace(place, 1, i));
                // ti=ti'
                inner.andWith(commitmentsEqual(i));
                // top'=0
                inner.andWith(TOP[1][i - 1].ithVar(0));
                // gc'=gc
                inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    /**
     * the same as for forall reachability apart fomrmn getBadPlaces
     *
     * @param t
     * @return
     */
    @Override
    BDD envPart(Transition t) {
        BDD env = super.envPart(t);
        // todo: one environment token case
        List<Place> pre = getGame().getSplittedPreset(t).getFirst();
        List<Place> post = getGame().getSplittedPostset(t).getFirst();
        if (pre.isEmpty()) {
            env.andWith(GOODCHAIN[0][0].ithVar(0));
        }
        if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
            Place postPlace = post.get(0);
            // it is good if it was good, or is a reach place
            if (getWinningCondition().getBadPlaces().contains(postPlace)) { // it is a place2reach -> 1
                env.andWith(GOODCHAIN[1][0].ithVar(1));
            } else {
                List<TokenFlow> tfls = AdamExtensions.getTokenFlow(t);
                for (TokenFlow tfl : tfls) {
                    if (tfl.getPostset().contains(postPlace)) {
                        if (tfl.getPreset().isEmpty()) {
                            env.andWith(GOODCHAIN[1][0].ithVar(0));
                        } else {
                            env.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));
                        }
                    }
                }
            }
        } else {
            env.andWith(GOODCHAIN[1][0].ithVar(0));
        }
        env.andWith(setOverallBad(t));
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // overall bad state don't have any successor
        env.andWith(OBAD[0].ithVar(0));
        return env;
    }

    /**
     * The same as for for all reachability
     *
     * @param t
     * @return
     */
    @Override
    BDD envTransitionCP(Transition t) {
        if (!getGame().getSysTransition().contains(t)) { // take only those transitions which have an env-place in preset
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0); // the transition should be enabled and choosen!
            // Systempart
            for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getGame().getPlaces()[i]) {
                    if (AdamExtensions.isEnvironment(place)) {
                        throw new RuntimeException("Should not appear!"
                                + "An enviromental place could not appear here!");
                        //                            continue;
                    }
                    BDD inner = getOne();
                    inner.andWith(codePlace(place, 0, i));
                    if (!pre_sys.contains(place)) { // the place wasn't in the preset of the transition, thus nothing can be changed here
                        // pi=pi'
                        inner.andWith(codePlace(place, 1, i));
                        // ti=ti'
                        inner.andWith(commitmentsEqual(i));
                        // top'=0
                        inner.andWith(TOP[1][i - 1].ithVar(0));
                        // gc'=gc
                        inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                    } else { // the place was in the preset of the transition, thus find a suitable sucessor and code it
                        Place post = getSuitableSuccessor(place, t);
                        //pre_i=post_i'
                        inner.andWith(codePlace(post, 1, i));
                        // top'=1
                        inner.andWith(TOP[1][i - 1].ithVar(1));
                        // all t_i'=0
                        inner.andWith(nothingChosen(1, i));
                        // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                        inner.andWith(setGoodChainFlagForTransition(t, post, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
            }
            // Environmentpart                
            all.andWith(envPart(t));
            return all;
        }
        return getZero();
    }

    /**
     * the same as universal reachability
     *
     * @return
     */
    @Override
    BDD envTransitionNotCP(Transition t) {
        if (!getGame().getSysTransition().contains(t)) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = AdamExtensions.getPartition(post);
                if (token != 0) { // jump over environment
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=1
                    all.andWith(TOP[1][token - 1].ithVar(1));
                    // all t_i'=0
                    all.andWith(nothingChosen(1, token));
                    // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                    all.andWith(setGoodChainFlagForTransition(t, post, token));
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // --------------------------
            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            // Environmentpart                
            all.andWith(envPart(t));
            return all;
        }
        return getZero();
    }

    /**
     * same as for forall reachablity
     *
     * @return
     */
    @Override
    BDD sysTopPart() {
        BDD sysT = super.sysTopPart();
        for (int i = 1; i < getGame().getMaxTokenCount(); i++) {
            sysT.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));// todo: 12.10.2017 not really check but should be better than (the here tag from 6.11.2017)
        }
        // in top part copy overallbad flag 
        sysT.andWith(OBAD[0].buildEquals(OBAD[1]));
        return sysT;
    }

    /**
     * same as for universal reachability (apart from the last ndet andwith in
     * forall reachability)
     *
     * @return
     */
    @Override
    BDD sysTransitionCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();
        // not all tops are zero
        BDD top = getTop();
        // normal part
        Set<Place> pre = t.getPreset();
        BDD sysN = firable(t, 0);
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            BDD pl = getZero();
            for (Place place : getGame().getPlaces()[i]) {// these are all system places                    
                BDD inner = getOne();
                inner.andWith(codePlace(place, 0, i));
                if (!pre.contains(place)) {
                    // pi=pi'
                    inner.andWith(codePlace(place, 1, i));
                    // ti=ti'
                    inner.andWith(commitmentsEqual(i));
                    // gc'=gc
                    inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                } else {
                    Place post = getSuitableSuccessor(place, t);
                    //pre_i=post_i'
                    inner.andWith(codePlace(post, 1, i));
                    // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                    inner.andWith(setGoodChainFlagForTransition(t, post, i));
                }
                pl.orWith(inner);
            }
            sysN.andWith(pl);
            // top'=0
            sysN.andWith(TOP[1][i - 1].ithVar(0));
        }
        sysN.andWith(setOverallBad(t));
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());

        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // keep the good chain flag for the environment, since there nothing could have changed        
        sys.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));
        // p0=p0'        
        sys.andWith(placesEqual(0));
        // overall bad state don't have any successor
        sys.andWith(OBAD[0].ithVar(0));

        return sys;
    }

    /**
     * same as for universal reachability (apart from the last ndet andwith in
     * forall reachability)
     *
     * @return
     */
    @Override
    BDD sysTransitionNotCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();
        // not all tops are zero
        BDD top = getTop();

        // normal part        
        Set<Place> pre_sys = t.getPreset();
        BDD sysN = firable(t, 0);
        List<Integer> visitedToken = new ArrayList<>();
        // set the dcs for the place of the postset 
        for (Place post : t.getPostset()) {
            int token = AdamExtensions.getPartition(post);
            if (token != 0) { // jump over environment, could not appear...
                visitedToken.add(token);
                //pre_i=post_j'
                sysN.andWith(codePlace(post, 1, token));
                // top'=0
                sysN.andWith(TOP[1][token - 1].ithVar(0));
                // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                sysN.andWith(setGoodChainFlagForTransition(t, post, token));
            }
        }
        // set the dcs for the places in the preset
        setPresetAndNeededZeros(pre_sys, visitedToken, sysN);
        // Positions in dcs not set with places of pre- or postset
        setNotAffectedPositions(sysN, visitedToken);
        // sets the overall bad flag
        sysN.andWith(setOverallBad(t));
        sysN = (top.not()).impWith(sysN);

        // top part      
        BDD sysT = top.impWith(sysTopPart());

        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // keep the good chain flag for the environment, since there nothing could have changed        
        sys = sys.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));
        // p0=p0'        
        sys = sys.andWith(placesEqual(0));
        // overall bad state don't have any successor
        sys.andWith(OBAD[0].ithVar(0));
        return sys;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    /**
     * Returns the winning decisionsets for the system players.
     *
     * In this case only an attractor to the reachable states.
     *
     * @return - A BDD containing all states from which a state with a reachable
     * place is able the be reached against all behavior of the environment.
     */
    @Override
    BDD calcWinningDCSs(Map<Integer, BDD> distance) {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
//        BDDTools.printDecodedDecisionSets(goodReach, this, true);
        BDD fixedPoint = attractor(winningStates(), true, distance).not().and(getBufferedDCSs());
        //BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    /**
     * Returns the graph game for the reachability objective.
     *
     * Is the standard graph game, but before returning we are just marking the
     * reachable states as special.
     *
     * @return - The graph game for the reachability objective.
     */
    @Override
    public BDDGraph getGraphGame() {
        BDDGraph graph = super.getGraphGame();
        BDD bad = winningStates();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !bad.and(state.getState()).isZero()) {
                state.setBad(true);
            }
        }
        return graph;
    }

    @Override
    protected PetriNet calculateStrategy() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pn = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    @Override
    public Pair<BDDGraph, PetriNet> getStrategies() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pstrat = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations %%%%%%%%%%%%%%%%
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment + nocc-flag
     * sets.
     *
     * So only adds the variables for the newly occupied places and the LOOP.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor
     * variables.
     * @return - the variables of the predecessor or the sucessor of a
     * transition.
     */
    @Override
    BDD getVariables(int pos) {
        // Existential variables
        BDD variables = super.getVariables(pos);
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            variables.andWith(GOODCHAIN[pos][i].set());
        }
        variables.andWith(OBAD[pos].set());
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * successor as BDD.
     *
     * This means the variables for the coding of the place, the top-flag, the
     * newly occupied-flag and the belonging commitment set for a system token.
     *
     * So only add the variables for the nocc-flag.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    @Override
    BDD getTokenVariables(int pos, int token) {
        BDD variables = super.getTokenVariables(pos, token);
        variables.andWith(GOODCHAIN[pos][token].set());
        return variables;
    }

    /**
     * Create a BDD which is true, when the predesseccor und the successor of a
     * transition are equal.
     *
     * Only adds the conditions for the nocc-flag and the loop flag.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    BDD preBimpSucc() {
        BDD preBimpSucc = super.preBimpSucc();
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            preBimpSucc.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
        }
        preBimpSucc.andWith(OBAD[0].buildEquals(OBAD[1]));
        return preBimpSucc;
    }

    /**
     * Only in not overall bad state there could have a transition fired. and
     * the good chain flag and overall bad could had changed.
     *
     * @param t
     * @param source
     * @param target
     * @return
     */
    @Override
    @Deprecated
    public boolean hasFiredManually(Transition t, BDD source, BDD target) {
        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        if (!source.and(OBAD[0].ithVar(1)).isZero()) {
            return false;
        }
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }

        if (!isFirable(t, source)) {
            return false;
        }
        // here the preset of t is fitting the source (and the commitment set)! Do not need to test it extra

        // Create bdd mantarget with the postset of t and the rest -1
        // So with "and" we can test if the postset of t also fit to the target
        // additionally create a copy of the target BDD with the places of the postset set to -1
        Pair<List<Place>, List<Place>> post = getGame().getSplittedPostset(t);
        // Environment place
        // todo: one environment token case
        BDD manTarget = getOne();
        BDD restTarget = target.id();
        if (!post.getFirst().isEmpty()) {
            manTarget.andWith(codePlace(post.getFirst().get(0), 0, 0));
            restTarget = restTarget.exist(getTokenVariables(0, 0));
        }

        // System places        
        List<Place> postSys = post.getSecond();
        BDD sysPlacesTarget = getOne();
        for (Place p : postSys) {
            int token = AdamExtensions.getPartition(p);
            sysPlacesTarget.andWith(codePlace(p, 0, token));
            restTarget = restTarget.exist(getTokenVariables(0, token));
        }
        manTarget.andWith(sysPlacesTarget);

        if ((manTarget.and(target)).isZero()) {
            return false;
        }

        // Create now a copy of the source with all positions set to -1 where preset is set
        Pair<List<Place>, List<Place>> pre = getGame().getSplittedPreset(t);
        // todo: one environment token case
        BDD restSource = source.id();
        if (!pre.getFirst().isEmpty()) {
            restSource = restSource.exist(getTokenVariables(0, 0));
        }

        List<Place> preSys = pre.getSecond();
        for (Place p : preSys) {
            restSource = restSource.exist(getTokenVariables(0, AdamExtensions.getPartition(p)));
        }

        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        // The flag indication that the place is newly occupied, may have changed
        for (int i = 0; i < getGame().getMaxTokenCountInt(); i++) {
            restSource = restSource.exist(GOODCHAIN[0][i].set());
            restTarget = restTarget.exist(GOODCHAIN[0][i].set());
        }
        restSource = restSource.exist(OBAD[0].set());
        restTarget = restTarget.exist(OBAD[0].set());
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%

        // now test if the places not in pre- or postset of t stayed equal between source and target
        return !(restTarget.and(restSource)).isZero();
    }

}
