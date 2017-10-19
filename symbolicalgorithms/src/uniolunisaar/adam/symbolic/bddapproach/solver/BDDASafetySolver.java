package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameWithType2StrategyBuilder;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a safety objective with BDDs.
 *
 *
 * @author Manuel Gieseking
 */
public class BDDASafetySolver extends BDDSolver<Safety> implements BDDType2Solver {

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
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    BDDASafetySolver(PetriNet net, boolean skipTests, Safety win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
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
     *
     * TODO: is this ordering more expensive, since the types are all together
     * at the end?
     *
     * But at least problem for my output functions in BDDTools, since I use
     * explicit counting.
     */
//    @Override
//    void createVariables() {
//        super.createVariables();
//        int tokencount = getGame().getMaxTokenCountInt();
//        TYPE = new BDDDomain[2][tokencount - 1];
//        for (int i = 0; i < 2; ++i) {
//            //for any token add the type
//            for (int j = 0; j < tokencount - 1; ++j) {
//                // type
//                TYPE[i][j] = getFactory().extDomain(2);
//            }
//        }
//        setDCSLength(getFactory().varNum() / 2);
//    }
    /**
     * Codierung: p_i_0 - Environment Token n - TokenCount
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
            int add = (!getGame().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0;
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
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        // Env place
        sb.append("(");
        sb.append(BDDTools.getPlaceIDByBin(dcs, PLACES[pos][0], getGame().getPlaces()[0], getGame().isConcurrencyPreserving()));
        sb.append(")").append("\n");
        for (int j = 0; j < getGame().getMaxTokenCount() - 1; j++) {
            sb.append("(");
            String sid = BDDTools.getPlaceIDByBin(dcs, PLACES[pos][j + 1], getGame().getPlaces()[j + 1], getGame().isConcurrencyPreserving());
            sb.append(sid);
            if (!sid.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getTypeFlagByBin(dcs, TYPE[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], getGame().getTransitions()[j]));
            }
            sb.append(")").append("\n");
        }
        return sb.toString();
    }

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
     * TODO: this is not infinitely: Calculates a BDD representing all
     * decisionsets from which the system players can play infinitely long
     * without any further interaction with the environment.
     *
     * @return BDD for all decision set in the type2 trap, so from which the
     * system players can play on their own infinitely long.
     */
    BDD type2Trap() {
        // Fixpoint
        BDD Q = getOne();
        BDD Q_ = goodSysDCSForType2Trap();
//        BDDTools.printDecisionSets(deadSysDCS(0).not(),  true);
//        int counter = 0;
        while (!Q_.equals(Q)) {
//            System.out.println("first" +counter);
//            if(counter++==1) {
//                break;
//            }
            Q.free();
            Q = Q_.andWith(super.wellformed(0));
            BDD Q_shifted = shiftFirst2Second(Q);
            // there is a predecessor (sys2) such that the transition is in the considered set of transitions
            Q_ = ((getBufferedSystem2Transition().and(Q_shifted)).exist(getSecondBDDVariables())).and(Q);
        }
//        System.out.println("type 2 trap");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
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
            bad.orWith(codePlace(place, pos, AdamExtensions.getToken(place)));
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
     * Calculates a BDD with all possible bad situations:
     *
     * 1) bad place reached 2) non determinism encountered 3) deadlock created
     *
     * @return BDD representing all bad situations
     */
    public BDD badSysDCS() {
//        System.out.println("bad");
//        BDDTools.printDecisionSets(baddcs(0).not(),  true);
//        System.out.println("end");
        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
    }

    public BDD badStates() {
        return badSysDCS().orWith(wrongTypedDCS());
    }

    /**
     * Calculates a BDD with all good situations for the system.
     *
     * @return BDD representing all good decision sets for the system.
     */
    private BDD goodSysDCSForType2Trap() {
        return badSysDCS().not().andWith(super.wellformed(0));
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 
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

//%%%%%%%%%%%%%%%% ADAPTED to type2 / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
//    @Override
//    public BDD wellformed(int pos) {
//        BDD well = super.wellformed(pos);
//        if (pos == 0) {
//            well.andWith(wrongTypedType2DCS().not());
//        } else {          
//            well.andWith(shiftFirst2Second(wrongTypedType2DCS().not()));
//        }
//        return well;
//    }
//    /**
//     * Overriden since for a safety objectiv is termination also OK. Only
//     * necessary, since we don't have for all states a successor?
//     *
//     * todo: currently not used
//     */
//    @Override
//    public BDD preSys(BDD succ) {
//        BDD succ_shifted = shiftFirst2Second(succ);
//        BDD forall = (getBufferedEnvTransitions().imp(succ_shifted)).forAll(getSecondBDDVariables()).and(getBufferedExEnvSucc());
//        BDD exists = (getBufferedSystemTransition().and(succ_shifted)).exist(getSecondBDDVariables()).or(term(0));
//        return forall.or(exists).and(wellformed());
//    }
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
                        if (AdamExtensions.isEnvironment(place)) {
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
                            Place postPlace = getSuitableSuccessor(place, t);
                            //pre_i=post_i'
                            inner.andWith(codePlace(postPlace, 1, i));
                            // top'=1
                            inner.andWith(TOP[1][i - 1].ithVar(1));
                            // type'=1 (only allow to choose a new type after resolving top
                            all.andWith(TYPE[1][i - 1].ithVar(1));
                            // all t_i'=0
                            inner.andWith(nothingChosen(1, i));
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
                };
//                if (t.getId().equals("t3")) {
//                    System.out.println("transition " + t);
//                    BDDTools.printDecisionSets(all, true);
//                    BDDTools.printDecodedDecisionSets(all, this, true);
//                }
                dis.orWith(all);
            }
        }
        env.andWith(dis);
//        env.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%");
//        BDDTools.printDecisionSets(env,true);
//        BDDTools.printDecodedDecisionSets(env, this, true);       
        // env.andWith(wellformed(1));//.andWith(wellformedTransition()));
//        
//        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@ type2");
//        BDDTools.printDecodedDecisionSets(getBufferedType2Trap(), this, true);
//        BDDTools.printDecisionSets(getBufferedType2Trap(), true);
        // bad states don't have succesors
        env.andWith(badStates().not());
        return env;
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
        // bad states don't have succesors
        mcut.andWith(badStates().not());
        return mcut;//.andWith(wellformed(1));//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
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
//                        inner.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1]));
                    } else {
                        //pre_i=post_i'
                        inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
                // type = type' I can't say anothing about if it is still type2
//                all.andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1])); // todo: changed
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
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1])));
            sysT.andWith(impl);
        }
        sysT = top.impWith(sysT);

        sys1.andWith(sysN);
        sys1.andWith(sysT);
        // p0=p0'        
        sys1 = sys1.andWith(placesEqual(0));

        // bad states don't have succesors
        sys1.andWith(badStates().not());

//        sys1.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
        return sys1;//.andWith(wellformed(1));//.andWith(wellformedTransition()));
    }

    @Override
    BDD sysTransitionsNotCP() {
        // Only useable if it's not an mcut
        BDD sys1 = getMcut().not();
        // no successors for already reached states
//        sys1.andWith(reach(0).not());

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
                    // predecessor was set -> type = type'
//                    BDD typeEQ = TYPE[0][token - 1].buildEquals(TYPE[1][token - 1]);
//                    all.andWith(codePlace(0, 0, token).not().impWith(typeEQ)); // todo: changed
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

//            if(t.getId().equals("t2")){
//                System.out.println("t2 succesors");
//                BDDTools.printDecisionSets(all, true);
//                BDDTools.printDecodedDecisionSets(all, this, true);
//                System.out.println("t2 finished");
//            }
            sysN.orWith(all);
        }
//        System.out.println("sysN");
//        BDDTools.printDecodedDecisionSets(sysN, this, true);
//        System.out.println("end sysN");

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
            // \not topi=>ti=ti'\wedge type=type'
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(TYPE[0][i - 1].buildEquals(TYPE[1][i - 1])));
            sysT.andWith(impl);
        }
        sysT = top.impWith(sysT);

        sys1.andWith(sysN);
        sys1.andWith(sysT);
        // p0=p0'        
        sys1 = sys1.andWith(placesEqual(0));

//        Place r = getNet().getPlace("r");
//        Place q = getNet().getPlace("q");
//        Place p = getNet().getPlace("p");
////        Place env = getNet().getPlace("ENV");
//        Place env = getNet().getPlace("env1");
//        BDD output = sys1.and(codePlace(r, 0, AdamExtensions.getToken(r)));
//        output.andWith(codePlace(q, 0, AdamExtensions.getToken(q)));
//        output.andWith(codePlace(p, 0, AdamExtensions.getToken(p)));
//        output.andWith(codePlace(env, 0, 0));
////        output.andWith(codePlace(env,0,0).not());
//        output.andWith(TYPE[0][AdamExtensions.getToken(r) - 1].ithVar(1));
//        output.andWith(TOP[0][AdamExtensions.getToken(r) - 1].ithVar(0));
//        output.andWith(TYPE[0][AdamExtensions.getToken(q) - 1].ithVar(1));
//        output.andWith(TOP[0][AdamExtensions.getToken(q) - 1].ithVar(0));
//        output.andWith(TYPE[0][AdamExtensions.getToken(p) - 1].ithVar(1));
////        output.andWith(TOP[0][AdamExtensions.getToken(p) - 1].ithVar(0));
//        BDDTools.printDecodedDecisionSets(output, this, true);
//TODO: mache den oldtype stuff
        // bad states don't have succesors
        sys1.andWith(badStates().not());
        return sys1;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
    }

//    @Override
//    BDD sysTransitionsNotCP() {
//        return super.sysTransitionsNotCP().andWith(wellformed(1));
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
//    @Override
//    BDD calcDCSs() {
//        return super.calcDCSs().andWith(wrongTypedDCS().not());
////        BDDTools.printDecodedDecisionSets(wellformed(), this, true);
//    }
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
        BDD fixedPoint = attractor(badStates(), true, distance).not().and(getBufferedDCSs());//fixpointOuter();
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()).andWith(getNotTop()), this, true);
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
            if (!graph.getInitial().equals(state) && !badStates().and(state.getState()).isZero()) {
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

// Don't want to do this, since here exists means enviroment.
//    /**
//     * Overriden since not every state has a successor mark that it is also 
//     * good for the system, when there is not succesor at all.
//
//     * @param succ
//     * @param allTrans
//     * @param existsTrans
//     * @return 
//     */
//    @Override
//    public BDD pre(BDD succ, BDD allTrans, BDD existsTrans) {
//        BDD succ_shifted = shiftFirst2Second(succ);
//        BDD forall = ((allTrans.imp(succ_shifted)).forAll(getSecondBDDVariables())).and(allTrans.id().exist(getSecondBDDVariables()));
//        BDD exists = (existsTrans.and(succ_shifted)).exist(getSecondBDDVariables()).or(term(0));
//        return forall.or(exists).and(wellformed());
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations %%%%%%%%%%%%%%%%
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment + type-flag
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
     * This means the variables for the coding of the place, the top-flag, the
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
