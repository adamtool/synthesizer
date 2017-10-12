package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
import uniolunisaar.adam.ds.winningconditions.Buchi;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.TokenFlow;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDBuchiGraphBuilder;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameWithType2StrategyBuilder;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * Todo: adapt all javadoc just copied of existiential buchi
 *
 * Problem what to do with the non-deterministic states? Already a fixed-point
 * combi of safety and reachability? It is not possible to totally omit them
 * because when a ndet state is a successor of an env state, then the env state
 * would errorously marked as good. Furthermore, if it's the only successor of a
 * sys state, then also this sys state would errorously marked as good. Solve it
 * the same way how it is done for reachability. It's also the same that no
 * deadlock and not type2 analysis necessary.
 *
 * Problem 2: Terminating of the game is not allowed. We have to add selfloops
 * at every state which isn't a buchi state and if a buchi state doesn't have
 * any successor than a succesor with a selfloop has to be added.
 *
 *
 * @author Manuel Gieseking
 */
public class BDDABuechiSolver extends BDDSolver<Buchi> implements BDDType2Solver {

    // Domains for predecessor and successor for each token
    private BDDDomain[][] NOCC;
    private BDDDomain[] LOOP;
    private BDDDomain[][] GOODCHAIN;
    private BDDDomain[] OBAD;
    // Domains for predecessor and successor for each token
    private BDDDomain[][] TYPE; // 1 means it is type 1, 0 means it is type2

    // Precalculated BDDs (todo:necessary?)
    private BDD system2 = null;
    private BDD type2Trap = null;

    /**
     * Creates a new Buchi solver for a given game.
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
    BDDABuechiSolver(PetriNet net, boolean skipTests, Buchi win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, win, opts);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Creates the variables for this solver. This have to be overriden since
     * the flag if the place has been newly occupied in this state has to be
     * coded additionally.
     *
     * Codierung: p_i_0 - Environment Token n - TokenCount occ=1 states newly
     * occupied in this state.
     *
     * |p_i_0|occ|p_i_1|occ|top|t_1|...|t_m| ... |p_i_n|occ|top|t_1|...|t_m|
     */
    @Override
    void createVariables() {
        int tokencount = getGame().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        NOCC = new BDDDomain[2][tokencount];
        GOODCHAIN = new BDDDomain[2][tokencount];
        TYPE = new BDDDomain[2][tokencount - 1];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        LOOP = new BDDDomain[2];
        OBAD = new BDDDomain[2];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (getGame().isConcurrencyPreserving()) ? 0 : 1;
            PLACES[i][0] = getFactory().extDomain(getGame().getPlaces()[0].size() + add);
            NOCC[i][0] = getFactory().extDomain(2);
            GOODCHAIN[i][0] = getFactory().extDomain(2);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getGame().getPlaces()[j + 1].size() + add);
                // newly occupied
                NOCC[i][j + 1] = getFactory().extDomain(2);
                // good chains
                GOODCHAIN[i][j + 1] = getFactory().extDomain(2);
                // type
                TYPE[i][j] = getFactory().extDomain(2);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
            LOOP[i] = getFactory().extDomain(2);
            OBAD[i] = getFactory().extDomain(2);
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% START Special loop stuff %%%%%%%%%%%%%%%%%%%%
    /**
     *
     * Problem for concurrency preserving nets, since there zero is an id of a
     * place and so it is posible to have this loopState as a sen
     *
     * Since they are only id's it's also not possible to code e.g. a sys place
     * at the env position.
     *
     * So added a new LOOP variable which should be 0 at any case a apart from
     * the loop state
     *
     * @param pos
     * @return
     */
    private BDD loopState(int pos) {
//        Place sys = this.getGame().getPlaces()[0].iterator().next();
//        BDD nearlyZero = this.getZero().exist(PLACES[pos][0].domain());
//        BDDTools.printDecisionSets(nearlyZero, true);
//        return nearlyZero.andWith(codePlace(sys, pos, 0));
        BDD loop = getOne();
        int start = (pos == 0) ? 0 : getDcs_length();
        for (int i = start; i < start + getDcs_length() - 2; i++) {
            loop.andWith(getFactory().nithVar(i));
        }
        loop.andWith(OBAD[pos].ithVar(0)); // bad flag
        loop.andWith(LOOP[pos].ithVar(1));
        return loop;
    }

    /**
     * @param pos
     * @return
     */
    private BDD endStates(int pos) {
        BDD term = getOne();
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition transition : trans) {
            term.andWith(firable(transition, pos).not());
        }
        term.andWith(getNotTop());
        term.andWith(type2().not());
        return term;
    }

    private BDD loops() {
        BDD buchi = winningStates();
        BDD term = endStates(0);
        // Terminating not buchi states add selfloop
        BDD termNBuchi = term.and(buchi.not().andWith(wellformed(0)));
        BDD loops = termNBuchi.andWith(preBimpSucc());
        // Terminating buchi states add transition to new looping state (all 
        loops.orWith(term.and(buchi).and(loopState(1)));
//        // add loop
        loops.orWith(loopState(0).andWith(loopState(1)));
//        System.out.println("end states");
//        BDDTools.printDecodedDecisionSets(term.and(buchi), this, true);
//        BDDTools.printDecisionSets(term.and(buchi), true);
//        System.out.println("END");
        return loops.andWith(wellformed(0));
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END Special loop stuff %%%%%%%%%%%%%%%%%%%%%%

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% START Special TYPE 2 Stuff %%%%%%%%%%%%%%%%%%
    /**
     * Calculates a BDD representing all situations where at least one place is
     * type2 typed.
     *
     * Should be expensive, since it compares variables over wide ranges. This
     * should be expensive for BDDs.
     *
     * @return BDD with at least one place is type2 typed.
     */
    private BDD type2() {
        BDD type2 = getFactory().zero();
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            BDD type = TYPE[0][i - 1].ithVar(0);
            // todo: really necessary? It is, but why? because I set all flags to zero when the token is not used in the bdd
            if (!getGame().isConcurrencyPreserving()) {
                type.andWith(codePlace(0, 0, i).not());
            }
            type2.orWith(type);
        }
//        return type2;
        return type2;//.andWith(getWellformed());
    }

    /**
     * Calculates a BDD representing all system2 transitions.
     *
     * @return BDD for all system2 transitions
     */
    private BDD sys2Transitions() {
//        return sys2TransitionsNotCP();
        if (getGame().isConcurrencyPreserving()) {
            return sys2TransitionsCP();
        } else {
            return sys2TransitionsNotCP();
        }
    }

    /**
     * Calculates a BDD representing all system2 transitions for a concurrency
     * preserving net.
     *
     * @return BDD for all system2 transitions for a concurrency preserving net.
     */
    private BDD sys2TransitionsCP() {
        BDD sys2 = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre = t.getPreset();
            BDD all = firable(t, false, 0);
            for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getGame().getPlaces()[i]) {
                    if (AdamExtensions.isEnvironment(place)) {
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
                        // nocc'=0
                        inner.andWith(NOCC[1][i].ithVar(0));
                        // gc'=gc
                        inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                    } else {
                        Place post = getSuitableSuccessor(place, t);
                        //pre_i=post_i'
                        inner.andWith(codePlace(post, 1, i));
                        // nocc'=1
                        inner.andWith(NOCC[1][i].ithVar(1));
                        // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                        inner.andWith(setGoodChainFlagForTransition(t, post, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
                // type = type'
                all.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
            }
            sys2.orWith(all.andWith(setOverallBad(t)));
        }
        // set the newly occupation flag of the env place to zero
        sys2 = sys2.andWith(NOCC[1][0].ithVar(0));

        // keep the good chain flag for the environment, since there nothing could have changed        
        sys2 = sys2.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));

        sys2.andWith(LOOP[0].ithVar(0));
        sys2.andWith(LOOP[1].ithVar(0));

        // p0=p0'        
        sys2 = sys2.andWith(placesEqual(0));

        // overall bad state don't have any successor
        sys2.andWith(OBAD[0].ithVar(0));

        sys2.orWith(loops());

//        System.out.println("for wellformed");
//        return sys2;//.andWith(wellformedTransition());
        return sys2.andWith(ndetStates(0).not());//.andWith(wellformedTransition());
    }

    /**
     * Calculates a BDD representing all system2 transitions for a net which is
     * not concurrency preserving.
     *
     * @return BDD for all system2 transitions for a concurrency preserving net.
     */
    private BDD sys2TransitionsNotCP() {
        BDD sys2 = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, false, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = AdamExtensions.getToken(post);
                if (token != 0) { // jump over environment
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=0
                    all.andWith(TOP[1][token - 1].ithVar(0));
                    // type = type'
                    all.andWith(TYPE[0][token - 1].buildEquals(TYPE[1][token - 1]));
                    // nocc'=1
                    all.andWith(NOCC[1][token].ithVar(1));
                    // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                    all.andWith(setGoodChainFlagForTransition(t, post, token));
                } else {
                    throw new RuntimeException("should not appear. No env place in sys2 transitions.");
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);
            sys2.orWith(all.andWith(setOverallBad(t)));
        }
        // set the newly occupation flag of the env place to zero
        sys2 = sys2.andWith(NOCC[1][0].ithVar(0));
//            Tools.printDecodedDecisionSets(sys2, game, true);
//        System.out.println("for ende");
        // keep the good chain flag for the environment, since there nothing could have changed        
        sys2 = sys2.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));

        sys2.andWith(LOOP[0].ithVar(0));
        sys2.andWith(LOOP[1].ithVar(0));

        // p0=p0'        
        sys2.andWith(placesEqual(0));

        // overall bad state don't have any successor
        sys2.andWith(OBAD[0].ithVar(0));

        sys2.orWith(loops());
        System.out.println("sys2 trans");
        BDDTools.printDecisionSets(sys2, true);
//        return sys2;//.andWith(wellformedTransition());
// wrong typed2 sets don't have succesor
        return sys2.andWith(ndetStates(0).not());//.andWith(wellformedTransition());
    }

    /**
     * Calculates a BDD representing all decisionsets from which the system
     * players can play infinitely long without any further interaction with the
     * environment.
     *
     * @return BDD for all decision set in the type2 trap, so from which the
     * system players can play on their own infinitely long.
     */
    BDD type2Trap() {
        // Fixpoint
        BDD Q = getZero();
        BDD Q_ = winningStates();
//        System.out.println("winning states");
//        BDDTools.printDecisionSets(Q_, true);
//        int counter = 0;
        while (!Q_.equals(Q)) {
//            System.out.println("first" +counter);
//            if(counter++==1) {
//                break;
//            }
            Q.free();
            // add all sys2 succcessors
            Q = Q_.andWith(wellformed());
            BDD succs = (getBufferedSystem2Transition().and(Q)).exist(getFirstBDDVariables());
            succs = shiftFirst2Second(succs);
            // delete all which don't have a sys2 successor
            Q_ = ((getBufferedSystem2Transition().and(succs)).exist(getSecondBDDVariables())).and(Q);
        }
        System.out.println("type 2 trap");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
        return Q_;
    }

    /**
     * Calculates a BDD representing the situations where a decision set is
     * typed as type2, but is not contained in the type2 trap.
     *
     * @return BDD of wrongly type2 typed decision sets.
     */
    BDD wrongTypedDCS() {
        // not(type2 => type2Trap)
        BDD wrongTyped2 = type2().andWith(getBufferedType2Trap().not());
        // it is typed as 1 but when its type is 2 it belongs to the trap
        BDD wrongType1 = wrongTypedType1DCS();
        return wrongType1.orWith(wrongTyped2);
    }

    private BDD wrongTypedType1DCS() {
        BDD type2 = getBufferedType2Trap();
        for (int i = 0; i < getGame().getMaxTokenCountInt() - 1; i++) {
            type2 = type2.exist(TYPE[0][i].set());
        }
        return type2.andWith(getBufferedType2Trap().not());
    }

    /**
     * Calculates a BDD representing the successors of the given 'trans' which
     * are contained in the type2 trap.
     *
     * @param trans - the transition containing the successor for which the
     * successor should be found in the type2 trap.
     * @return the transitions where the successors of 'trans' have a successor
     * int the type2 trap.
     */
    @Override
    public BDD getGoodType2Succs(BDD trans) {
        // shift to the successors
        trans = trans.exist(getFirstBDDVariables());
        trans = shiftSecond2First(trans);
        // get only the good ones
        trans = trans.and(getBufferedType2Trap());
        return trans;
    }

    /**
     * Calculates the transitions where 'state' is the predecessor and there
     * exists a system2 transition.
     *
     * @param state - the predecessor to find the system2 transitions to.
     * @return a BDD representing the type2 transitions starting with 'state'.
     */
    @Override
    public BDD getSystem2SuccTransitions(BDD state) {
        return state.and(getBufferedSystem2Transition());
    }

    /**
     * States if a type2 flag is set in the decision set represented by 'bdd'.
     *
     * @param bdd - the bdd to check for the type2 flag.
     * @return true if 'bdd' has a type2 flag set to true.
     */
    @Override
    public boolean isType2(BDD bdd) {
        return !bdd.and(type2()).isZero();
    }

    /**
     * Calculates a BDD with all good situations for the system.
     *
     * @return BDD representing all good decision sets for the system.
     */
    private BDD goodSysDCSForType2Trap() {
        return winningStates();//.andWith(getWellformed());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END Special TYPE 2 Stuff %%%%%%%%%%%%%%%%%%%%   
// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Good when all visible token belong to a good chain.
     */
    private BDD winningStates() {
        BDD buchi = getZero();
        for (Place place : getWinningCondition().getBuchiPlaces()) {
            int token = AdamExtensions.getToken(place);
            // is a buchi place and is newly occupied, than it's a buchi state
            buchi.orWith(codePlace(place, 0, token).andWith(NOCC[0][token].ithVar(1)));
        }
//        buchi.andWith(wellformed(0));
//        System.out.println("buchi");
        BDDTools.printDecisionSets(buchi, true);
        BDD ret = getOne();
        for (int i = 0; i < getGame().getMaxTokenCount(); i++) {
            if (AdamExtensions.getConcurrencyPreserving(getNet())) {
                ret.andWith(GOODCHAIN[0][i].ithVar(1));
            } else {
                ret.andWith(GOODCHAIN[0][i].ithVar(1).orWith(codePlace(0, 0, i)));
            }
        }
        ret.andWith(OBAD[0].ithVar(0));
        return ret.and(buchi);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

//%%%%%%%%%%%%%%%% ADAPTED to NOCC  / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    BDD wellformed(int pos) {
        BDD well = super.wellformed(pos);
        well.andWith(LOOP[pos].ithVar(0));
        well.orWith(loopState(pos));
        return well;
    }

//    @Override
//    BDD calcDCSs() {
//        BDD all = super.calcDCSs();
//        all.andWith(LOOP[0].ithVar(0));
//        all.andWith(LOOP[1].ithVar(0));
//        all.orWith(loopState(0));
//        return all;
//    }
    @Override
    public BDD initial() {
        BDD init = super.initial();
        // one version, then in the most cases there is one unfolding of the place
//        // all newly occupied flags to 0
//        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
//            init.andWith(NOCC[0][i].ithVar(0));
//        }
        // all newly ocupied flags for the initial token are 1 the others zero
        Marking m = getNet().getInitialMarking();
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            boolean occ = false;
            boolean good = false;
            for (Place p : getGame().getPlaces()[i]) {
                if (m.getToken(p).getValue() > 0) {
                    occ = true;
                    if (getWinningCondition().getBuchiPlaces().contains(p)) {
                        good = true;
                    }
                    break;
                }
            }
            init.andWith(NOCC[0][i].ithVar(occ ? 1 : 0));
            init.andWith(GOODCHAIN[0][i].ithVar(good ? 1 : 0));
        }
        init.andWith(LOOP[0].ithVar(0));
        init.andWith(OBAD[0].ithVar(0));
        init.andWith(ndetStates(0).not());
        return init;
    }

    private BDD setGoodChainFlagForTransition(Transition t, Place post, int token) {
//        System.out.println("Post:" + post.getId());
        if (getWinningCondition().getBuchiPlaces().contains(post)) { // it is a buchi -> 1
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
                    int preToken = AdamExtensions.getToken(p);
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

    private BDD setOverallBad(Transition t) {
        List<TokenFlow> fls = AdamExtensions.getTokenFlow(t);
        for (Place p : t.getPreset()) {
            boolean hasFlow = false;
            for (TokenFlow fl : fls) {
                if (fl.getPreset().contains(p) && !fl.getPostset().isEmpty()) {
                    hasFlow = true;
                }
            }
            if (!hasFlow) {
                int token = AdamExtensions.getToken(p);
                BDD preBad = codePlace(p, 0, token);
                preBad.andWith(GOODCHAIN[0][token].ithVar(0));
                BDD ret = preBad.impWith(OBAD[1].ithVar(1));
                return ret;
            }
        }
        return OBAD[1].ithVar(0);
    }

    @Override
    BDD envTransitionsCP() {
        BDD env = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
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
                            // type = type'
                            inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                            // nocc'=0
                            inner.andWith(NOCC[1][i].ithVar(0));
                            // gc'=gc
                            inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                        } else { // the place was in the preset of the transition, thus find a suitable sucessor and code it
                            Place post = getSuitableSuccessor(place, t);
                            //pre_i=post_i'
                            inner.andWith(codePlace(post, 1, i));
                            // top'=1
                            inner.andWith(TOP[1][i - 1].ithVar(1));
                            // type'=1 (only allow to choose a new type after resolving top
                            all.andWith(TYPE[1][i - 1].ithVar(1));
                            // all t_i'=0
                            inner.andWith(nothingChosen(1, i));
                            // occ' = 0 (newly occupied only in the case where we solve the top)
                            inner.andWith(NOCC[1][i].ithVar(0));
                            // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                            inner.andWith(setGoodChainFlagForTransition(t, post, i));
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
                    all.andWith(GOODCHAIN[0][0].ithVar(0));
                    all.andWith(NOCC[0][0].ithVar(0));
                }
                if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
                    Place postPlace = post.get(0);
                    all.andWith(codePlace(postPlace, 1, 0));
                    // occ' = 1 
                    all.andWith(NOCC[1][0].ithVar(1));
                    // it is good if it was good, or is a reach place
                    if (getWinningCondition().getBuchiPlaces().contains(postPlace)) { // it is a buchi place -> 1
                        all.andWith(GOODCHAIN[1][0].ithVar(1));
                    } else {
                        List<TokenFlow> tfls = AdamExtensions.getTokenFlow(t);
                        for (TokenFlow tfl : tfls) {
                            if (tfl.getPostset().contains(postPlace)) {
                                if (tfl.getPreset().isEmpty()) {
                                    all.andWith(GOODCHAIN[1][0].ithVar(0));
                                } else {
                                    all.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));
                                }
                            }
                        }
                    }
                } else {
                    all.andWith(codePlace(0, 1, 0));
                    all.andWith(NOCC[1][0].ithVar(0));
                    all.andWith(GOODCHAIN[1][0].ithVar(0));
                }
                dis.orWith(all.andWith(setOverallBad(t)));
            }
        }
        env.andWith(LOOP[0].ithVar(0));
        env.andWith(LOOP[1].ithVar(0));
        env.andWith(dis);
        // overall bad state don't have any successor
        env.andWith(OBAD[0].ithVar(0));

        // bad states don't have succesors
        env.andWith(wrongTypedDCS().not());
        env.orWith(loops());
        return env;
    }

    @Override
    BDD notUsedToken(int pos, int token) {
        BDD zero = super.notUsedToken(pos, token);
        zero.andWith(TYPE[pos][token - 1].ithVar(0));
        zero.andWith(NOCC[pos][token].ithVar(0));
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
                // type = type'
                inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                // nocc'=0
                inner.andWith(NOCC[1][i].ithVar(0));
                // gc'=gc
                inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    @Override
    BDD envTransitionsNotCP() {
        BDD mcut = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
            if (!getGame().getSysTransition().contains(t)) {
                Set<Place> pre_sys = t.getPreset();
                BDD all = firable(t, 0);

                List<Integer> visitedToken = new ArrayList<>();

                // set the dcs for the place of the postset 
                for (Place post : t.getPostset()) {
                    int token = AdamExtensions.getToken(post);
                    if (token != 0) { // jump over environment
                        visitedToken.add(token);
                        //pre_i=post_j'
                        all.andWith(codePlace(post, 1, token));
                        // top'=1
                        all.andWith(TOP[1][token - 1].ithVar(1));
                        // type'=1 (only allow to choose a new type after resolving top
                        all.andWith(TYPE[1][token - 1].ithVar(1));
                        // all t_i'=0
                        all.andWith(nothingChosen(1, token));
                        // occ' = 0 (newly occupied only in the case where we solve the top)
                        all.andWith(NOCC[1][token].ithVar(0));
                        // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                        all.andWith(setGoodChainFlagForTransition(t, post, token));
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
                    all.andWith(NOCC[0][0].ithVar(0));
                    all.andWith(GOODCHAIN[0][0].ithVar(0));
                }
                if (!post.isEmpty()) {
                    Place postPlace = post.get(0);
                    all.andWith(codePlace(postPlace, 1, 0));
                    all.andWith(NOCC[1][0].ithVar(1));
                    // it is good if it was good, or is a buchi place
                    if (getWinningCondition().getBuchiPlaces().contains(postPlace)) { // it is a buchi -> 1
                        all.andWith(GOODCHAIN[1][0].ithVar(1));
                    } else {
                        List<TokenFlow> tfls = AdamExtensions.getTokenFlow(t);
                        for (TokenFlow tfl : tfls) {
                            if (tfl.getPostset().contains(postPlace)) {
                                if (tfl.getPreset().isEmpty()) {
                                    all.andWith(GOODCHAIN[1][0].ithVar(0));
                                } else {
                                    all.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));
                                }
                            }
                        }
                    }
                } else {
                    all.andWith(codePlace(0, 1, 0));
                    all.andWith(NOCC[1][0].ithVar(0));
                    all.andWith(GOODCHAIN[1][0].ithVar(0));
                }
                dis.orWith(all.andWith(setOverallBad(t)));
            }
        }
        mcut.andWith(LOOP[0].ithVar(0));
        mcut.andWith(LOOP[1].ithVar(0));

        mcut.andWith(dis);
        // overall bad state don't have any successor
        mcut.andWith(OBAD[0].ithVar(0));

        // bad states don't have succesors
        mcut.andWith(wrongTypedDCS().not());

        mcut.orWith(loops());
        return mcut;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    @Override
    BDD sysTransitionsCP() {
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();

        // not all tops are zero
        BDD top = getTop();

        // normal part
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre = t.getPreset();
            BDD all = firable(t, 0);
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
                        // nocc'=0
                        inner.andWith(NOCC[1][i].ithVar(0));
                        // gc'=gc
                        inner.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
                    } else {
                        Place post = getSuitableSuccessor(place, t);
                        //pre_i=post_i'
                        inner.andWith(codePlace(post, 1, i));
                        // nocc'=1
                        inner.andWith(NOCC[1][i].ithVar(1));
                        // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                        inner.andWith(setGoodChainFlagForTransition(t, post, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
            }
            sysN.orWith(all.andWith(setOverallBad(t)));
        }
        // in not top case set the newly occupation flag of the env place to zero
        sysN = sysN.andWith(NOCC[1][0].ithVar(0));
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
            // \not topi=>(ti=ti'\wedge type'=type \wedge nocc=nocc' \wedge gc'=gc)
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(NOCC[1][i].ithVar(0))
                    .andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1])));
            // topi=> nocc'=1            
            BDD impl1 = TOP[0][i - 1].ithVar(1).impWith(NOCC[1][i].ithVar(1));
            sysT.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
            sysT.andWith(impl).andWith(impl1);
        }
        // in top case just copy the newly occupation flag of the env place
        sysT = sysT.andWith(NOCC[0][0].buildEquals(NOCC[1][0]));
        // in top part copy overallbad flag 
        sysT.andWith(OBAD[0].buildEquals(OBAD[1]));
        sysT = top.impWith(sysT);

        sys.andWith(sysN);
        sys.andWith(sysT);

        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));

        // keep the good chain flag for the environment, since there nothing could have changed        
        sys = sys.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));

        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        // overall bad state don't have any successor
        sys.andWith(OBAD[0].ithVar(0));

        sys.orWith(loops());

        // bad states don't have succesors
        sys.andWith((wrongTypedDCS().or(ndetStates(0))).not());
        return sys;
    }

    @Override
    BDD sysTransitionsNotCP() {
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();

        // not all tops are zero
        BDD top = getTop();

        // normal part        
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = AdamExtensions.getToken(post);
                if (token != 0) { // jump over environment, could not appear...
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=0
                    all.andWith(TOP[1][token - 1].ithVar(0));
                    // nocc'=1
                    all.andWith(NOCC[1][token].ithVar(1));
                    // gc'=1 iff forall p\in pre(t) p fl(t) post => p gc was 1
                    all.andWith(setGoodChainFlagForTransition(t, post, token));
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            sysN.orWith(all.andWith(setOverallBad(t)));
        }
        // in not top case set the newly occupation flag of the env place to zero
        sysN = sysN.andWith(NOCC[1][0].ithVar(0));

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
            // \not topi=>(ti=ti'\wedge nocc'=0)
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(NOCC[1][i].ithVar(0))
                    .andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1])));
            // topi=> nocc'=1            
            BDD impl1 = TOP[0][i - 1].ithVar(1).impWith(NOCC[1][i].ithVar(1));
            sysT.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
            sysT.andWith(impl).andWith(impl1);
        }
        // in top case just copy the newly occupation flag of the env place
        sysT = sysT.andWith(NOCC[0][0].buildEquals(NOCC[1][0]));
        // in top part copy overallbad flag 
        sysT.andWith(OBAD[0].buildEquals(OBAD[1]));
        sysT = top.impWith(sysT);

        sys.andWith(sysN);
        sys.andWith(sysT);
        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));

        // keep the good chain flag for the environment, since there nothing could have changed        
        sys = sys.andWith(GOODCHAIN[0][0].buildEquals(GOODCHAIN[1][0]));

        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        // overall bad state don't have any successor
        sys.andWith(OBAD[0].ithVar(0));

        // bad states don't have succesors
        sys.andWith((wrongTypedDCS().or(ndetStates(0))).not());

        sys.orWith(loops());
        return sys;
    }

    //%%%%%%%%%%%%%%%% ADAPTED to type2 / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Overriden since the standard case only knows type1 places.
     */
    @Override
    BDD enabled(Transition t, int pos) {
        return enabled(t, true, pos);
    }

    /**
     * Only adds the type2 behavior to the enabled function of the standard
     * case.
     *
     * @param t - the transition which is checked to be enabled.
     * @param type1 - the type to check
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing all decision sets where 't' is enabled in the
     * given position 'pos' and with the correct type 'type1'.
     */
    private BDD enabled(Transition t, boolean type1, int pos) {
        BDD en = super.enabled(t, pos);
        for (Place place : t.getPreset()) {
            if (!AdamExtensions.isEnvironment(place)) {
                // Sys places
                int token = AdamExtensions.getToken(place);
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
        return !(source.and(firable(t, true, 0)).isZero() && source.and(firable(t, false, 0)).isZero());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * with strategy building from zimmermanns lecture script
     *
     * @return
     */
    private BDD buchi(Map<Integer, BDD> distance) {
        BDD S = getBufferedDCSs().id();
        BDD W = getZero();
        BDD W_;
        BDD B = winningStates();
        do {
            B = B.and(S);
            if (distance != null) {
                distance.clear();
            }
            BDD R = attractor(B, false, S, distance);
//            System.out.println("R states");
//            BDDTools.printDecodedDecisionSets(R, this, true);
//            System.out.println("END R staes");
//            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%% attr reach ");
//            BDDTools.printDecodedDecisionSets(R, this, true);
            BDD Tr = S.and(R.not());
//            System.out.println("TR states");
//            BDDTools.printDecodedDecisionSets(Tr, this, true);
//            System.out.println("END TR states");
//            System.out.println("%%%%%%%%%%%%%%%% TR");
//            BDDTools.printDecodedDecisionSets(Tr, this, true);         
            W_ = attractor(Tr, true, S);

//            System.out.println("W_ states");
//            BDDTools.printDecodedDecisionSets(W_, this, true);
//            System.out.println("END W_ states");
//            System.out.println("%%%%%%%%%%%%%%%% atrrroktor TR");
//            BDDTools.printDecodedDecisionSets(W_, this, true);
            W = W.or(W_);
            S.andWith(W_.not());
        } while (!W_.isZero());
        //        System.out.println("%%%%%%%%%%%% W");
//        BDDTools.printDecodedDecisionSets(W, this, true);
        W = W.not().and(getBufferedDCSs());
        // Save attr0(recurm(F))\recurm(F) at position -1
        if (distance != null) {
//            attractor(B, false, getBufferedDCSs(), distance);
//            System.out.println("hier" + distance.toString());
            distance.put(-1, B);
        }
//        System.out.println("%%%%%%%%%%%% return");
//        BDDTools.printDecodedDecisionSets(endStates(0), this, true);
        return W;
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     */
    @Override
    BDD calcWinningDCSs(Map<Integer, BDD> distance) {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = buchi(distance);//.andWith(ndetStates(0).not()).andWith(wellformed(0)); // not really necesarry, since those don't have any successor.
        fixedPoint.andWith(wrongTypedDCS().not()); // todo: why does it not already worked with added not type to to endstates?
//        BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
//        try {
//            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//            BDDTools.saveStates2Pdf("./states", fixedPoint, this);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(BDDABuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(BDDABuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return fixedPoint;
    }

    @Override
    public BDDGraph getGraphGame() {
        BDDGraph graph = super.getGraphGame();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !winningStates().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
//        System.out.println("loops");
//        BDDTools.printDecisionSets(loops(), true);
//        BDDTools.printDecodedDecisionSets(loops(), this, true);
//        System.out.println("reach");
//        BDDTools.printDecisionSets(getBufferedEnvTransitions(), true);
//        BDDTools.printDecodedDecisionSets(getBufferedDCSs(), this, true);
        return graph;
    }

    @Override
    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException {
        HashMap<Integer, BDD> distance = new HashMap<>();
        BDD win = calcWinningDCSs(distance);
        super.setBufferedWinDCSs(win);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph strat = BDDBuchiGraphBuilder.getInstance().builtGraphStrategy(this, distance);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        for (BDDState state : strat.getStates()) { // mark all special states
            if (!winningStates().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return strat;
    }

    @Override
    protected PetriNet calculateStrategy() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pn = BDDPetriGameWithType2StrategyBuilder.getInstance().builtStrategy(this, gstrat);
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
        PetriNet pstrat = BDDPetriGameWithType2StrategyBuilder.getInstance().builtStrategy(this, gstrat);
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
            variables.andWith(NOCC[pos][i].set());
            variables.andWith(GOODCHAIN[pos][i].set());
            if (i < getGame().getMaxTokenCount() - 1) {
                variables.andWith(TYPE[pos][i].set());
            }
        }
        variables.andWith(LOOP[pos].set());
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
        variables.andWith(NOCC[pos][token].set());
        variables.andWith(GOODCHAIN[pos][token].set());
        if (token != 0) { // env has no type flag
            variables.andWith(TYPE[pos][token - 1].set());
        }
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
            preBimpSucc.andWith(NOCC[0][i].buildEquals(NOCC[1][i]));
            preBimpSucc.andWith(GOODCHAIN[0][i].buildEquals(GOODCHAIN[1][i]));
            if (i < getGame().getMaxTokenCount() - 1) {
                preBimpSucc.andWith(TYPE[0][i].buildEquals(TYPE[1][i]));
            }
        }
        preBimpSucc.andWith(LOOP[0].buildEquals(LOOP[1]));
        preBimpSucc.andWith(OBAD[0].buildEquals(OBAD[1]));
        return preBimpSucc;
    }

    /**
     * Only in not looping states and not overall bad state there could have a
     * transition fired.
     *
     * good and nocc flag and bad could have changed
     *
     * @param t
     * @param source
     * @param target
     * @return
     */
    @Override
    public boolean hasFired(Transition t, BDD source, BDD target) {
        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        if (!source.and(LOOP[0].ithVar(1)).isZero() || !source.and(OBAD[0].ithVar(1)).isZero()) {
            return false;
        }
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%

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
            int token = AdamExtensions.getToken(p);
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
            restSource = restSource.exist(getTokenVariables(0, AdamExtensions.getToken(p)));
        }

        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        // The flag indication that the place is newly occupied, may have changed
        for (int i = 0; i < getGame().getMaxTokenCountInt(); i++) {
            restSource = restSource.exist(NOCC[0][i].set());
            restTarget = restTarget.exist(NOCC[0][i].set());
            restSource = restSource.exist(GOODCHAIN[0][i].set());
            restTarget = restTarget.exist(GOODCHAIN[0][i].set());
        }
        restSource = restSource.exist(OBAD[0].set());
        restTarget = restTarget.exist(OBAD[0].set());
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%

        // now test if the places not in pre- or postset of t stayed equal between source and target
        return !(restTarget.and(restSource)).isZero();
    }

    BDD getBufferedType2Trap() {
        if (type2Trap == null) {
            type2Trap = type2Trap();
        }
        return type2Trap;
    }

    BDD getBufferedSystem2Transition() {
        if (system2 == null) {
            system2 = sys2Transitions();
        }
        return system2;
//        return sys2Transitions();
    }

}
