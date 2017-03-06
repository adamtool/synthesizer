package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameSafetyStrategyBuilder;
import uniolunisaar.adam.util.benchmark.Benchmarks;
import uniolunisaar.adam.util.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSafetySolver extends BDDSolver<Safety> {

    // Domains for predecessor and successor for each token
    private BDDDomain[][] TYPE;
    // Precalculated BDDs (todo:necessary?)
    private BDD system2 = null;
    private BDD type2Trap = null;
    //    private BDD wellformed;
//    private BDD baddcs;
//    private BDD ndet;
//    private BDD deadSysDCS;
//    private BDD mcut;
//    private BDD p0eqp0_;
//    private BDD term;
//    private BDD secondBDDVariables;
//    private BDD firstBDDVariables;
//    private BDD initialDCS;
//    private BDD nTop;
//    private BDD type2;
//    private BDD wrongTypedType2DCS;
//    private BDD goodSysStates;
//    private BDD preBimpSucc;
//    private BDD goodSysDCSForType2Trap;
//    private BDD wellformedTransition;
//    private Map<Integer, BDD> commitmentsEquals;

    /**
     * Creates a new Safety solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a safety condition.
     */
    BDDSafetySolver(PetriNet net, boolean skipTests, BDDSolverOptions opts) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, new Safety(), opts);
        super.initialize();
    }

    /**
     * Creates the variables for this solver. This have to be overriden since
     * the type flag has to be coded additionally.
     *
     * Codierung: p_i_0 - Environment Token n - TokenCount type 1 = 1 type 2 = 0
     * |p_i_0|p_i_1|type|top|t_1|...|t_m| ... |p_i_n|type|top|t_1|...|t_m|
     */
    @Override
    void createVariables() {
        int tokencount = getGame().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        TYPE = new BDDDomain[2][tokencount - 1];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (getGame().isConcurrencyPreserving()) ? 0 : 1;
            PLACES[i][0] = getFactory().extDomain(getGame().getPlaces()[0].size() + add);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getGame().getPlaces()[j + 1].size() + add);
                // type
                TYPE[i][j] = getFactory().extDomain(2);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size()); // at the end extDomain creates log2(size) variables
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }

    /**
     * Precalculates and saved the BDDs for the system2 transitions and the
     * type2-trap.
     */
    @Override
    void precalculateSpecificBDDs() {
        //        wellformed = wellformed();
//        type2 = type2();
//        nTop = nTop();
//        initialDCS = initial();
//        baddcs = baddcs();
//        ndet = ndet();
//        deadSysDCS = deadSysDCS();
//        mcut = mcut();
//        term = term();
//        goodSysDCSForType2Trap = goodSysDCSForType2Trap;
        // Existential variables
//        secondBDDVariables = secondBDDVariables();
//        firstBDDVariables = firstBDDVariables();
//        p0eqp0_ = placesEqual(0);
//        preBimpSucc = preBimpSucc();
//        System.out.println("Start calculation wellformed transitions");
////        wellformedTransition = wellformedTransition();
//        System.out.println("... done (calculation of wellformed transitions)");
//
////        commitmentsEquals = new HashMap<>();
////        int offset = PL_CODE_LEN;
////        for (int i = 1; i < game.getTOKENCOUNT(); ++i) {
////            commitmentsEquals.put(offset, commitmentsEqual(offset));
////            offset += OFFSET;
////        }

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.SYSTEM2_TRANS);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating system2 transitions ...");
        system2 = sys2Transitions();
        Logger.getInstance().addMessage("... calculation of type2 transitions done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.SYSTEM2_TRANS);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.TYPE2_TRAP);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS            
        Logger.getInstance().addMessage("Calculating type2 trap ...");
        type2Trap = type2Trap();
        Logger.getInstance().addMessage("... calculation of type2 trap done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.TYPE2_TRAP);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS   

//        // not(type2 => type2Trap)
//        wrongTypedType2DCS = type2.and(type2Trap.not()).and(wellformed);
//        goodSysStates = ((baddcs.or(ndet).or(deadSysDCS).or(wrongTypedType2DCS)).not()).and(wellformed);
    }

    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + type-flag + commitment
     * sets.
     *
     * So only adds the variables for the type.
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
        for (int i = 0; i < getGame().getMaxTokenCount() - 1; ++i) {
            variables.andWith(TYPE[pos][i].set());
        }
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * sucessor as BDD.
     *
     * This means the varibles for the coding of the place, the top-flag, the
     * type-flag and the belonging commitment set for a system token.
     *
     * So only add the variables for the type-flag.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    @Override
    BDD getTokenVariables(int pos, int token) {
        BDD variables = super.getTokenVariables(pos, token);
        if (token != 0) {
            variables.andWith(TYPE[pos][token - 1].set());
        }
        return variables;
    }

    /**
     * Create a BDD which is true, when the predesseccor und the successor of a
     * transition are equal.
     *
     * Only adds the conditions for the type-flag.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    BDD preBimpSucc() {
        BDD preBimpSucc = super.preBimpSucc();
        for (int i = 0; i < getGame().getMaxTokenCount() - 1; ++i) {
            preBimpSucc.andWith(TYPE[0][i].buildEquals(TYPE[1][i]));
        }
        return preBimpSucc;
    }

    /**
     * overriden since by safty also termination is ok for the system
     *
     * Brauche ich nicht, solange ich rueckwaerts gehe.
     *
     * @param succ
     * @return
     */
    @Override
    public BDD preSys(BDD succ) {
        BDD succ_shifted = shiftFirst2Second(succ);
        BDD forall = (getBufferedEnvTransitions().imp(succ_shifted)).forAll(getSecondBDDVariables()).and(getBufferedExEnvSucc());
        BDD exists = (getBufferedSystemTransition().and(succ_shifted)).exist(getSecondBDDVariables()).or(getTerm());
        return forall.or(exists).and(wellformed());
    }

    /**
     * Overriden since the type has to be considered in the transition
     *
     * @return
     */
    @Override
    BDD getEnvironmentTransitions() {
//        return envTransitionsNotCP();
        if (getGame().isConcurrencyPreserving()) {
            return envTransitionsCP();
        } else {
            return envTransitionsNotCP();
        }
    }

    BDD wrongTypedType2DCS() {
        // not(type2 => type2Trap)
        return type2().andWith(getType2Trap().not());
    }

    private BDD goodSysDCSForType2Trap() {
        return badSysDCS().not();//.andWith(getWellformed());
    }

    private BDD goodSysStates() {
        return (goodSysDCSForType2Trap().andWith(wrongTypedType2DCS().not())).andWith(wellformed());
    }

    public BDD badSysDCS() {
        return getBadDecisionsets().orWith(getBufferedNDet().or(getDeadSysDCS()));
    }

    /**
     * It exists a type 2 place
     *
     * eher teuer weil vergleich ueber die token und damit ueber recht weite
     * entfernungen
     */
    private BDD type2() {
        BDD type2 = getFactory().zero();
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            BDD type = TYPE[0][i - 1].ithVar(0);
            // todo: really necessary?
            if (!getGame().isConcurrencyPreserving()) {
                type.andWith(codePlace(0, 0, i).not());
            }
            type2.orWith(type);
        }
//        return type2;
        return type2;//.andWith(getWellformed());
    }

    /**
     * Calculates all decisionsets from which the systemplayers can play
     * infinitely long
     *
     * @return
     */
    BDD type2Trap() {
        // Fixpoint
        BDD Q = getOne();
        BDD Q_ = getGoodSysDCSForType2Trap();
//        int counter = 0;
        while (!Q_.equals(Q)) {
//            System.out.println("first" +counter);
//            if(counter++==1) {
//                break;
//            }
            Q.free();
            Q = Q_.andWith(wellformed());
            BDD Q_shifted = shiftFirst2Second(Q);
            // there is a predecessor (sys2) such that the transition is in the considered set of transitions
            Q_ = ((getSystem2Transition().and(Q_shifted)).exist(getSecondBDDVariables())).and(Q);
        }
        return Q_;
    }

    /**
     * Responsible for jumping from mcut to mcut
     *
     * so this is responsible for getting a strategy as well as creating the
     * game. this means it could be a reachabilty solving strategy and the
     * nested fixedpoints are for the complete information of the game.
     *
     * @return
     */
    BDD fixpointOuter() {
        BDD Q = getOne();
        BDD Q_ = getGoodSysStates().orWith(getTerm());
        while (!Q_.equals(Q)) {
            Q = Q_;
            Q_ = preSys(Q.and(getMcut()));
            Q_ = fixpointInner(Q_).and(Q);
        }
        return fixpointInner(Q_);
    }
// Idea when we would have the exact game created how to solve it in a second step
//    BDD fixpointOuter() {
////        System.out.println("type 2" + data.getType2Trap().isZero());
////        BDDTools.printDecodedDecisionSets(data.getType2Trap(), this, true);
////        System.out.println("asdf ");
////        BDDTools.printDecodedDecisionSets(data.wrongTypedType2DCS(), this, true);
//        BDD goodDCS = badSysDCS().not().and(wellformed().and(wrongTypedType2DCS().not()));
//        BDD Q = getZero();
////        BDD Q_ = goodDCS;
//        BDD Q_ = badSysDCS().and(wellformed().and(wrongTypedType2DCS().not()));
//        while (!Q_.equals(Q)) {
////            BDDTools.printDecodedDecisionSets(Q_, getGame(), true);
////            System.out.println("lauf");
//            Q = Q_;
//            Q_ = (preEnv(Q).and(wellformed().and(wrongTypedType2DCS().not()))).or(Q);
////            Q_ = preEnv(Q).or(Q);
////            Q_ = (preSys(Q).and(goodDCS)).or(Q);
//        }
//        return Q_;//.not().and(wellformed().and(wrongTypedType2DCS().not()));
//    }

    private BDD fixpointInner(BDD X) {
        BDD Q = getZero();
        BDD Q_ = X.and(getGoodSysStates());
        while (!Q_.equals(Q)) {
            Q = Q_;
            Q_ = (preSys(Q).or(Q)).and(getGoodSysStates());
        }
        return Q_;
    }

    public BDD getGoodType2Succs(BDD trans) {
        // shift to the successors
        trans = trans.exist(getFirstBDDVariables());
        trans = shiftSecond2First(trans);
        // get only the good ones
        trans = trans.and(getType2Trap());
        return trans;
    }

//    private BDD mixedTypes(int pos) {
//        BDD ret = getFactory().one();
//        for (Transition t : getGame().getNet().getTransitions()) {
//            BDD type1 = getOne();
//            BDD typ2 = getOne();
//            for (int i = 1; i < getGame().getTOKENCOUNT(); ++i) {
//                for (Place p : t.getPreset()) {
//                    if (!p.hasExtension("env")) {
//                        type1.andWith(codePlace(p, pos, i).impWith(TYPE[pos][i - 1].ithVar(1)));
//                        typ2.andWith(codePlace(p, pos, i).impWith(TYPE[pos][i - 1].ithVar(0)));
//                    }
//                }
//            }
//            ret.andWith(type1.orWith(typ2));
//        }
//        return ret;
//    }
    private BDD baddcs(int pos) {
        BDD bad = getZero();
        for (Place place : getWinningCondition().getBadPlaces()) {
            bad.orWith(codePlace(place, pos, (Integer) place.getExtension("token")));
        }
        return bad;
    }

    @Override
    BDD ndetStates(int pos) {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition t1 : trans) {
            for (Transition t2 : trans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<Place> pre1 = t1.getPreset();
                    Set<Place> pre2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(pre1);
                    intersect.retainAll(pre2);
                    boolean shared = false;
                    for (Place place : intersect) {
                        if (!place.hasExtension("env")) {
                            shared = true;
                        }
                    }
                    if (shared) {
                        BDD first = firable(t1, pos).andWith(firable(t2, pos));
                        nondet = nondet.orWith(first);
                    }
                }
            }
        }
        return nondet.andWith(wellformed());
    }

    @Override
    BDD ndetEncountered() {
        BDD Q = getOne();
        BDD Q_ = ndetStates(0);
        Set<Transition> envTrans = getGame().getEnvTransitions();
        int[] pres = new int[envTrans.size()];
        int[] posts = new int[envTrans.size()];
        int i = 0;
        for (Transition t : envTrans) {
            Iterator<Place> it = t.getPreset().iterator();
            pres[i] = (it.hasNext()) ? (Integer) it.next().getExtension("id") : 0;
            it = t.getPostset().iterator();
            posts[i] = (it.hasNext()) ? (Integer) it.next().getExtension("id") : 0;
            ++i;
        }
        while (!Q_.equals(Q)) {
            Q = Q_;
            BDD pre = getZero(); // get all predecessors of the current ndetEncountered-States with only env transitions
            for (int j = 0; j < pres.length; j++) {
                //replace the env place at the front and check if the postset is set at the end
                pre.orWith(Q.exist(PLACES[0][0].set()).and(codePlace(pres[j], 0, 0)) // preset and postset should be singleton
                        .and(shiftFirst2Second(Q).and(codePlace(posts[j], 1, 0))));
            }
            pre = pre.exist(getVariables(1)); // only predecessors are interesting
            Q_ = pre.or(Q);
            Q_.andWith(wellformed(0));
        }
//        System.out.println("ndet %%%%%%%");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
        return Q_;
    }

    @Override
    BDD enabled(Transition t, int pos) {
        return enabled(t, true, pos).orWith(enabled(t, false, pos));
    }

    /**
     * Enabled iff all preset places of t are set and the type fits.
     *
     * Only adds the type2 behavior.
     *
     * @param t
     * @param type1
     * @param pos
     * @return
     */
    private BDD enabled(Transition t, boolean type1, int pos) {
        BDD en = super.enabled(t, pos);
        for (Place place : t.getPreset()) {
            if (!place.hasExtension("env")) {
                // Sys places
                int token = (Integer) place.getExtension("token");
                BDD type = TYPE[pos][token - 1].ithVar(type1 ? 1 : 0);
                en.andWith(type);
            }
        }
        return en;//.andWith(getWellformed());
    }

    private BDD firable(Transition t, boolean type1, int pos) {
        return enabled(t, type1, pos).andWith(chosen(t, pos));
    }

    @Override
    boolean isFirable(Transition t, BDD source) {
        return source.and(firable(t, true, 0)).isZero() && source.and(firable(t, false, 0)).isZero();
    }

//    public BDD deadSysDCS(boolean type1) {
//        BDD dead = bddfac.one();
//        BDD buf = bddfac.zero();
//        for (Transition t : net.getTransitions()) {
//            dead = dead.and(firable(t, type1).not());
//            buf = buf.or(enabled(t, type1));
//        }
//        dead = dead.and(buf);
//        // set types to 1
////        int offset = PL_CODE_LEN;
////        for (int i = 1; i < game.getTOKENCOUNT(); i++) {
////            dead = dead.and(bddfac.ithVar(offset + PL_CODE_LEN));
////            offset += OFFSET;
////        }
//        return dead.and(nTop).and(wellformed);
//    }
    private BDD deadSysDCS(int pos) {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, true, pos).not());
            buf.orWith(enabled(t, true, pos));
        }
        dead.andWith(buf);
        // set types to 1
        dead.andWith(getType2().not());
        return dead.andWith(getTop().not()).andWith(wellformed());
    }

    private BDD mcut(int pos) {
        BDD mc = getOne();
        // all tops should be zero
        mc.andWith(getNotTop());
        for (Transition t : getGame().getSysTransition()) {
            mc.andWith(firable(t, true, pos).not());
        }
        return mc.andWith(wellformed());
    }

    private BDD term(int pos) {
        BDD notEn = getOne();
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition transition : trans) {
            notEn.andWith(enabled(transition, true, pos).not());
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

    private BDD envTransitionsCP() {
        BDD env = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
            if (!getGame().getSysTransition().contains(t)) {
                Set<Place> pre_sys = t.getPreset();
                BDD all = firable(t, true, 0);
                // Systempart
                for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                    BDD pl = getZero();
                    for (Place place : getGame().getPlaces()[i]) {
                        if (place.hasExtension("env")) {
                            throw new RuntimeException("Should not appear!"
                                    + "An enviromental place could not appear here!");
                            //                            continue;
                        }
                        BDD inner = getOne();
                        inner.andWith(codePlace(place, 0, i));
                        if (!pre_sys.contains(place)) {
                            // pi=pi'
                            inner.andWith(codePlace(place, 1, i));
                            // ti=ti'
                            inner.andWith(commitmentsEqual(i));
                            // top'=0
                            inner.andWith(TOP[1][i - 1].ithVar(0));
                            // type = type'
                            inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                        } else {
                            //pre_i=post_i'
                            inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                            // top'=1
                            inner.andWith(TOP[1][i - 1].ithVar(1));
                            // all t_i'=0
                            inner.andWith(nothingChosen(1, i));
                            // type' = 1
                            inner.andWith(TYPE[1][i - 1].ithVar(1));
                        }
                        pl.orWith(inner);
                    }
                    all.andWith(pl);
                }
                // Environmentpart                
                // todo: one environment token case
                List<Place> pre = getGame().getSplittedPreset(t).getFirst();
                List<Place> post = getGame().getSplittedPostset(t).getFirst();
                if (!pre.isEmpty()) { // not really necessary since CP, but for no envtoken at all
                    all.andWith(codePlace(pre.get(0), 0, 0));
                } else {
                    all.andWith(codePlace(0, 0, 0));
                }
                if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
                    all.andWith(codePlace(post.get(0), 1, 0));
                } else {
                    all.andWith(codePlace(0, 1, 0));
                }
                dis.orWith(all);
            }
        }
        env.andWith(dis);
        return env.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    @Override
    BDD notUsedToken(int pos, int token) {
        BDD zero = super.notUsedToken(pos, token);
        zero.andWith(TYPE[pos][token - 1].ithVar(0));
        return zero;
    }

    private void setPresetAndNeededZeros(Set<Place> pre_sys, List<Integer> visitedToken, BDD all) {
        List<Integer> postTokens = new ArrayList<>(visitedToken);
        // set the dcs for the places in the preset
        for (Place pre : pre_sys) {
            if (!pre.hasExtension("env")) { // jump over environment
                int token = (Integer) pre.getExtension("token");
                visitedToken.add(token);
                all.andWith(codePlace(pre, 0, token));
                // if this position in the dcs is free after this position
                if (!postTokens.contains(token)) {
                    all.andWith(notUsedToken(1, token));
                } else {
                    postTokens.remove((Integer) token);
                }
            }
        }

        // those where there is a post but no preset for this token
        for (Integer token : postTokens) {
            all.andWith(notUsedToken(0, token));
        }
    }

    private void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
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
                // type = type'
                inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    private BDD envTransitionsNotCP() {
        BDD mcut = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
            if (!getGame().getSysTransition().contains(t)) {
                Set<Place> pre_sys = t.getPreset();
                BDD all = firable(t, true, 0);

                List<Integer> visitedToken = new ArrayList<>();

                // set the dcs for the place of the postset 
                for (Place post : t.getPostset()) {
                    int token = (Integer) post.getExtension("token");
                    if (token != 0) { // jump over environment
                        visitedToken.add(token);
                        //pre_i=post_j'
                        all.andWith(codePlace(post, 1, token));
                        // top'=1
                        all.andWith(TOP[1][token - 1].ithVar(1));
                        // all t_i'=0
                        all.andWith(nothingChosen(1, token));
                        // type' = 1
                        all.andWith(TYPE[1][token - 1].ithVar(1));
                    }
                }

                // set the dcs for the places in the preset
                setPresetAndNeededZeros(pre_sys, visitedToken, all);

                // --------------------------
                // Positions in dcs not set with places of pre- or postset
                setNotAffectedPositions(all, visitedToken);

                // --------------------------
                // Environmentpart
                // todo: one environment token case
                List<Place> pre = getGame().getSplittedPreset(t).getFirst();
                List<Place> post = getGame().getSplittedPostset(t).getFirst();
                if (!pre.isEmpty()) {
                    all.andWith(codePlace(pre.get(0), 0, 0));
                } else {
                    all.andWith(codePlace(0, 0, 0));
                }
                if (!post.isEmpty()) {
                    all.andWith(codePlace(post.get(0), 1, 0));
                } else {
                    all.andWith(codePlace(0, 1, 0));
                }
                dis.orWith(all);
            }
        }

        mcut.andWith(dis);
        return mcut;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    @Override
    BDD getSystemTransitions() {
//        return sys1TransitionsNotCP();
        if (getGame().isConcurrencyPreserving()) {
            return sys1TransitionsCP();
        } else {
            return sys1TransitionsNotCP();
        }
    }

    private BDD sys1TransitionsCP() {
        // Only useable if it's not an mcut
        BDD sys1 = getMcut().not();
        // no successors for bad states
        sys1.andWith(badSysDCS().not());

        // not all tops are zero
        BDD top = getTop();

        // normal part
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre = t.getPreset();
            BDD all = firable(t, true, 0);
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
                        // type = type'
                        inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                    } else {
                        //pre_i=post_i'
                        inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
            }
            sysN.orWith(all);
        }
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getGame().getMaxTokenCount(); i++) {
//            // \not topi=>topi'=0
//            BDD topPart = bddfac.nithVar(offset + PL_CODE_LEN + 1);
//            topPart.impWith(bddfac.nithVar(DCS_LENGTH + offset + PL_CODE_LEN + 1));
//            sysT.andWith(topPart);
            // topi'=0
            sysT.andWith(TOP[1][i - 1].ithVar(0));
            // type = type' todo: document anpassen
            //sysT.andWith(bddfac.ithVar(offset + PL_CODE_LEN).biimp(bddfac.ithVar(DCS_LENGTH + offset + PL_CODE_LEN)));
            // pi=pi'
            sysT.andWith(placesEqual(i));
            // \not topi=>ti=ti'
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i));
            sysT.andWith(impl);
        }
        sysT = top.impWith(sysT);

        sys1.andWith(sysN);
        sys1.andWith(sysT);
        // p0=p0'        
        sys1 = sys1.andWith(placesEqual(0));

        return sys1.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    private BDD sys1TransitionsNotCP() {
        // Only useable if it's not an mcut
        BDD sys1 = getMcut().not();
        // no successors for bad states
        sys1.andWith(badSysDCS().not());

        // not all tops are zero
        BDD top = getTop();

        // normal part        
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, true, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = (Integer) post.getExtension("token");
                if (token != 0) { // jump over environment, could not appear...
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=0
                    all.andWith(TOP[1][token - 1].ithVar(0));
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            sysN.orWith(all);
        }

        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
//            // \not topi=>topi'=0
//            BDD topPart = bddfac.nithVar(offset + PL_CODE_LEN + 1);
//            topPart.impWith(bddfac.nithVar(DCS_LENGTH + offset + PL_CODE_LEN + 1));
//            sysT.andWith(topPart);
            // topi'=0
            sysT.andWith(TOP[1][i - 1].ithVar(0));
            // type = type' todo: document anpassen
            //sysT.andWith(bddfac.ithVar(offset + PL_CODE_LEN).biimp(bddfac.ithVar(DCS_LENGTH + offset + PL_CODE_LEN)));
            // pi=pi'
            sysT.andWith(placesEqual(i));
            // \not topi=>ti=ti'
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i));
            sysT.andWith(impl);
        }
        sysT = top.impWith(sysT);

        sys1.andWith(sysN);
        sys1.andWith(sysT);
        // p0=p0'        
        sys1 = sys1.andWith(placesEqual(0));
//TODO: mache den oldtype stuff
        return sys1;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    private BDD sys2Transitions() {
//        return sys2TransitionsNotCP();
        if (getGame().isConcurrencyPreserving()) {
            return sys2TransitionsCP();
        } else {
            return sys2TransitionsNotCP();
        }
    }

    private BDD sys2TransitionsNotCP() {
        BDD sys2 = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, false, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = (Integer) post.getExtension("token");
                if (token != 0) { // jump over environment
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=0
                    all.andWith(TOP[1][token - 1].ithVar(0));
                    // type = type'
                    all.andWith(TYPE[0][token - 1].buildEquals(TYPE[1][token - 1]));
                } else {
                    throw new RuntimeException("should not appear. No env place in sys2 transitions.");
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);
            sys2.orWith(all);
        }
//            Tools.printDecodedDecisionSets(sys2, game, true);
//        System.out.println("for ende");
        // p0=p0'        
        sys2.andWith(placesEqual(0));
//        System.out.println("for wellformed");
//        return sys2;//.andWith(wellformedTransition());
        return sys2;//.andWith(wellformedTransition());
    }

    private BDD sys2TransitionsCP() {
        BDD sys2 = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre = t.getPreset();
            BDD all = firable(t, false, 0);
            for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getGame().getPlaces()[i]) {
                    if (place.hasExtension("env")) {
                        throw new RuntimeException("Should not appear!"
                                + "An enviromental place could not appear here!");
//                        continue;
                    }
                    BDD inner = getOne();
                    inner.andWith(codePlace(place, 0, i));
                    if (!pre.contains(place)) {
                        // pi=pi'
                        inner.andWith(codePlace(place, 1, i));
                        // ti=ti'
                        inner.andWith(commitmentsEqual(i));
                    } else {
                        //pre_i=post_i'
                        inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
                // type = type'
                all.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
            }
            sys2.orWith(all);
        }
//        System.out.println("for ende");
        // p0=p0'        
        sys2 = sys2.andWith(placesEqual(0));
//        System.out.println("for wellformed");
//        return sys2;//.andWith(wellformedTransition());
        return sys2;//.andWith(wellformedTransition());
    }

    private BDD oldType2() {
        BDD prev = wrongTypedType2DCS().not().and(wellformed());
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            prev = TYPE[0][i - 1].ithVar(0).ite(
                    prev.restrict(TYPE[1][i - 1].ithVar(1)).id(),
                    prev.id()).id();
        }
        return prev;
    }

    /**
     * deprecated
     *
     * @param state
     * @param place
     * @return
     */
    boolean isType2(BDD state, Place place) {
        if (place.hasExtension("env")) {
            return false;
        }
        int token = (Integer) place.getExtension("token");
        BDD comp = TYPE[0][token - 1].ithVar(0); // set to type 2
        return !state.and(comp).isZero();
    }

    @Override
    public boolean isEnvState(BDD state) {
        // it's an env state when it's an mcut
        return !state.and(getMcut()).isZero();
    }

    BDD getGoodSysStates() {
        return goodSysStates().and(wellformed());
    }

    @Override
    BDD getMcut() {
        return mcut(0);
    }

    BDD getTerm() {
        return term(0);
    }

    public BDD getSystem2SuccTransitions(BDD state) {
        return state.and(getSystem2Transition());
    }

    BDD getSystem2Transition() {
        if (system2 == null) {
            system2 = sys2Transitions();
        }
        return system2;
//        return sys2Transitions();
    }

    BDD getGoodSysDCSForType2Trap() {
        return goodSysDCSForType2Trap();
    }

    BDD getFirable(Transition t, boolean b) {
        return firable(t, b, 0);
    }

    BDD getType2() {
        return type2();
    }

    BDD getType2Trap() {
        return type2Trap;
    }

    public boolean isType2(BDD bdd) {
        return !bdd.and(getType2()).isZero();
    }

    BDD getBadDecisionsets() {
        return baddcs(0);
    }

    BDD getEnabled(Transition t, boolean type1) {
        return enabled(t, type1, 0);
    }

    BDD getDeadSysDCS() {
        return deadSysDCS(0);
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     */
    @Override
    BDD calcWinningDCSs() {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = fixpointOuter();
//        BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    @Override
    BDD calcDCSs() {
        return wellformed().andWith(wrongTypedType2DCS().not());
    }

    @Override
    public BDDGraph getGraphGame() {
        BDDGraph graph = super.getGraphGame();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !badSysDCS().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return graph;
    }

    @Override
    public PetriNet calculateStrategy() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pn = BDDPetriGameSafetyStrategyBuilder.getInstance().builtStrategy(this, gstrat);
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
        PetriNet pstrat = BDDPetriGameSafetyStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
    }
}
