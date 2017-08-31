package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a safety objective with BDDs.
 *
 * The first version of a BDD safety solver for Petri games. Uses two nested
 * fixpoints, termination and do not extend the game such that not every state
 * has to have a successor.
 *
 * @author Manuel Gieseking
 */
public class BDDSafetySolverNested extends BDDSolver<Safety> {

    // Domains for predecessor and successor for each token
    private BDDDomain[][] TYPE;

    // Precalculated BDDs (todo:necessary?)
    private BDD system2 = null;
    private BDD type2Trap = null;

    /**
     * Creates a new Safety solver for a given game.
     *
     * @param net - the Petri game to solve.
     * @param skipTests - should the tests for safe and bounded and other
     * preconditions be skipped?
     * @param opts - the options for the solver.
     * @throws UnboundedPGException - Thrown if the given net is not bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    BDDSafetySolverNested(PetriNet net, boolean skipTests, Safety win, BDDSolverOptions opts) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, win, opts);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Creates the variables for this solver. This have to be overriden since
     * the type flag has to be coded additionally.
     *
     * Codierung: p_i_0 - Environment Token n - TokenCount type 1 = 1 type 2 = 0
     * |p_i_0|p_i_1|top|t_1|...|t_m| ...
     * |p_i_n|top|t_1|...|t_m|type_1|...|type_n|
     */
    @Override
    void createVariables() {
        super.createVariables();
        int tokencount = getGame().getMaxTokenCountInt();
        TYPE = new BDDDomain[2][tokencount - 1];
        for (int i = 0; i < 2; ++i) {
            //for any token add the type
            for (int j = 0; j < tokencount - 1; ++j) {
                // type
                TYPE[i][j] = getFactory().extDomain(2);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
            // todo: really necessary? It is, but why?
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
        BDD Q = getOne();
        BDD Q_ = goodSysDCSForType2Trap();
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
            Q_ = ((getBufferedSystem2Transition().and(Q_shifted)).exist(getSecondBDDVariables())).and(Q);
        }
        return Q_;
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END Special TYPE 2 Stuff %%%%%%%%%%%%%%%%%%%%    

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Calculates a BDD with all possible situations containing a bad place.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing situations with bad places
     */
    private BDD baddcs(int pos) {
        BDD bad = getZero();
        for (Place place : getWinningCondition().getBadPlaces()) {
            bad.orWith(codePlace(place, pos, (Integer) place.getExtension("token")));
        }
        return bad;
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
            dead.andWith(firable(t, true, pos).not());
            buf.orWith(enabled(t, true, pos));
        }
        dead.andWith(buf);
        // set types to 1
        dead.andWith(type2().not());
        return dead.andWith(getTop().not());//.andWith(wellformed());
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

    /**
     * Calculates a BDD with all possible bad situations:
     *
     * 1) bad place reached 2) non determinism encountered 3) deadlock created
     *
     * @return BDD representing all bad situations
     */
    public BDD badSysDCS() {
        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
    }

    /**
     * Calculates a BDD with all good situations for the system.
     *
     * @return BDD representing all good decision sets for the system.
     */
    private BDD goodSysDCSForType2Trap() {
        return badSysDCS().not();//.andWith(getWellformed());
    }

    /**
     * Calculates a BDD with all good situations for the system also considering
     * the type2 situations (wrongly typed type2).
     *
     * @return BDD representing all good decision sets for the system (including
     * wrongly typed type2).
     */
    private BDD goodSysStates() {
        return (goodSysDCSForType2Trap().andWith(wrongTypedType2DCS().not())).andWith(wellformed());
    }

    /**
     * Calculates a BDD with all good situations for the system also considering
     * the type2 situations (wrongly typed type2) and wellformed.
     *
     * @return BDD representing all good decision sets for the system (including
     * wrongly typed type2 and wellformed).
     */
    private BDD getGoodSysStates() {
        return goodSysStates().and(wellformed());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 
    /**
     * Responsible for jumping from mcut to mcut
     *
     * so this is responsible for getting a strategy as well as creating the
     * game. this means it could be a reachabilty solving strategy and the
     * nested fixedpoints are for the complete information of the game.
     * Shurely??
     *
     * @return
     */
    BDD fixpointOuter() {
        BDD Q = getOne();
        BDD Q_ = getGoodSysStates().orWith(term(0));
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

    /**
     * Calculates a BDD representing the situations where a decision set is
     * typed as type2, but is not contained in the type2 trap.
     *
     * @return BDD of wrongly type2 typed decision sets.
     */
    BDD wrongTypedType2DCS() {
        // not(type2 => type2Trap)
        return type2().andWith(getBufferedType2Trap().not());
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
    public BDD getGoodType2Succs(BDD trans) {
        // shift to the successors
        trans = trans.exist(getFirstBDDVariables());
        trans = shiftSecond2First(trans);
        // get only the good ones
        trans = trans.and(getBufferedType2Trap());
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
    /**
     * TODO: javadoc.
     *
     * @return
     */
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
     * Calculates the transitions where 'state' is the predecessor and there
     * exists a system2 transition.
     *
     * @param state - the predecessor to find the system2 transitions to.
     * @return a BDD representing the type2 transitions starting with 'state'.
     */
    public BDD getSystem2SuccTransitions(BDD state) {
        return state.and(getBufferedSystem2Transition());
    }

    /**
     * States if a type2 flag is set in the decision set represented by 'bdd'.
     *
     * @param bdd - the bdd to check for the type2 flag.
     * @return true if 'bdd' has a type2 flag set to true.
     */
    public boolean isType2(BDD bdd) {
        return !bdd.and(type2()).isZero();
    }

//%%%%%%%%%%%%%%%% ADAPTED to type2 / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Overriden since for a safety objectiv is termination also OK. Only
     * necessary, since we don't have for all states a successor?
     */
    @Override
    public BDD preSys(BDD succ) {
        BDD succ_shifted = shiftFirst2Second(succ);
        BDD forall = (getBufferedEnvTransitions().imp(succ_shifted)).forAll(getSecondBDDVariables()).and(getBufferedExEnvSucc());
        BDD exists = (getBufferedSystemTransition().and(succ_shifted)).exist(getSecondBDDVariables()).or(term(0));
        return forall.or(exists).and(wellformed());
    }

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
        return !(source.and(firable(t, true, 0)).isZero() && source.and(firable(t, false, 0)).isZero());
    }

    @Override
    BDD envTransitionsCP() {
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
    BDD sysTransitionsCP() {
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

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    @Override
    BDD calcDCSs() {
        return wellformed().andWith(wrongTypedType2DCS().not());
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

    /**
     * Overriden for marking the bad states.
     *
     * @return
     */
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

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations %%%%%%%%%%%%%%%%
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

// %%%%%%%%%%%%%%%%%%%%%% Precalculated results / BDDs %%%%%%%%%%%%%%%%%%%%%%%%%%
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
