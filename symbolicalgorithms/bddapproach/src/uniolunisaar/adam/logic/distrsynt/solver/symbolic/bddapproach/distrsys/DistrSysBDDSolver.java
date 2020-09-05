package uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class DistrSysBDDSolver<W extends Condition<W>> extends BDDSolver<W, DistrSysBDDSolvingObject<W>, BDDSolverOptions> {

    // Domains for predecessor and successor for each token   
    protected BDDDomain[][] TRANSITIONS;
    protected BDDDomain[][] TOP;

    //Buffered BDDs (todo:necessary?)  
    private BDD ndet = null;

    protected DistrSysBDDSolver(DistrSysBDDSolvingObject<W> solverObject, BDDSolverOptions options) {
        super(solverObject, options);
    }

//    /**
//     * Creates a new solver for the given game.
//     *
//     * @param game - the games which should be solved.
//     * @param skipTests
//     * @param winCon
//     * @param opts
//     * @throws uniolunisaar.adam.exceptions.pg.NotSupportedGameException
//     * @throws
//     * uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException
//     * @throws uniolunisaar.adam.exceptions.pg.InvalidPartitionException
//     */
//    protected BDDSolver(PetriGame game, boolean skipTests, W winCon, BDDSolverOptions opts) throws NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException, NetNotSafeException {
//        super(new BDDSolvingObject<>(game, winCon, skipTests), opts);
    //todo: make it dependable of the given winning conditions but since I'm in a hurry, be  more conservative             
//        // Need at least one env place
//        if (getGame().getEnvPlaces().isEmpty()) {
//            throw new NotSupportedGameException("BDD solving need at least one environment place.");
//        }
//        // Need at least one sys place
//        boolean hasSystem = false;
//        for (Place p : getGame().getNet().getPlaces()) {
//            if (!AdamExtensions.isEnvironment(p)) {
//                hasSystem = true;
//                break;
//            }
//        }
//        if (!hasSystem) {
//            throw new NotSupportedGameException("BDD solving need at least one system place.");
//        }
    // hopefully not neccessary anymore
    // every set of system places annotated with the same token need at least one place in it, which has a transition in it postset
//        for (int i = 1; i < getGame().getPlaces().length; i++) {
//            Set<Place> placeSet = getGame().getPlaces()[i];
//            boolean hasSuccesor = false;
//            for (Place place : placeSet) {
//                if (!place.getPostset().isEmpty()) {
//                    hasSuccesor = true;
//                    break;
//                }
//            }
//            if (!hasSuccesor) {
//                throw new NotSupportedGameException("BDD solving need at least one successor in the set of system places annotated with the same token. Set '"
//                        + placeSet.toString() + "' is missing a succesor.");
//            }
//        }
//    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//
//    /**
//     * Here should those BDDs be calculated which should be precalculated at
//     * creation time of this object.
//     */
//    abstract void precalculateSpecificBDDs();
    /**
     * Codierung: p_i_0 - Environment Token n - TokenCount
     * |p_i_0|p_i_1|top|t_1|...|t_m| ... |p_i_n|top|t_1|...|t_m|
     */
    @Override
    protected void createVariables() {
        int tokencount = getSolvingObject().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (!getSolvingObject().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0; // add for no env place at all a dummy space
            PLACES[i][0] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[0].size() + add);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[j + 1].size() + add);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getSolvingObject().getDevidedTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    protected String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        // Env place
        sb.append("(");
        String id = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][0], getSolvingObject().getDevidedPlaces()[0], getSolvingObject().isConcurrencyPreserving());
        sb.append(id);
        sb.append(")").append("\n");
        for (int j = 0; j < getSolvingObject().getMaxTokenCount() - 1; j++) {
            sb.append("(");
            String sid = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][j + 1], getSolvingObject().getDevidedPlaces()[j + 1], getSolvingObject().isConcurrencyPreserving());
            sb.append(sid);
            if (!sid.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], getSolvingObject().getDevidedTransitions()[j]));
            }
            sb.append(")").append("\n");
        }
        return sb.toString();
    }

    /**
     * Calculates the wellformed BDDs for the predecessor or successor.
     *
     * Only successors of the place are allowed in its commitment sets.
     * Additional reduction technique of Valentin Spreckel's MA.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return a wellformed BDD
     */
    @Override
    protected BDD wellformed(int pos) {
        // only the places belonging to the token (or zero) are allowed in the positions
        BDD well = super.wellformed(pos);

        // only transitions in the postset of pi are allowed in the commitments
        BDD com = getOne();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            for (Transition t : getSolvingObject().getDevidedTransitions()[i - 1]) {
                BDD place = getZero();
                for (Place p : t.getPreset()) {
                    if (!getSolvingObject().getGame().isEnvironment(p) && i == getSolvingObject().getGame().getPartition(p)) {
                        place.orWith(codePlace(p, pos, i));
                    }
                }
                int id = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t);
                com.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(place));
                // further reduction of the commitment sets by idea of valentin spreckels:
                // when transition only has one place in preset no other transitions are 
                // allowed in the commitment set
                if (t.getPreset().size() == 1) {
                    // all others have to be zero
                    BDD trans = getOne();
                    for (Transition t1 : getSolvingObject().getDevidedTransitions()[i - 1]) {
                        if (!t1.equals(t)) {
                            int id1 = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t1);
                            trans.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id1]));
                        }
                    }
                    com.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(trans));
                }
            }
        }
        well.andWith(com);

        //well.andWith(mixedTypes(pos)); //nicht so oder so not?
        return well;
    }

    @Override
    protected BDD notUsedToken(int pos, int token) {
        BDD zero = codePlace(0, pos, token);
        if (token == 0) { //env case
            return zero;
        }
        zero.andWith(TOP[pos][token - 1].ithVar(0));
        zero.andWith(nothingChosen(pos, token));
        return zero;
    }

    /**
     * Calculates the BDD belonging to the initial marking with all tops set to
     * false.
     *
     * @return BDD for the initial marking.
     */
    @Override
    protected BDD initial() {
        BDD init = super.initial();
        init.andWith(getNotTop());
        return init;//.and(getWellformed());
    }

    @Override
    protected BDD calcSystemTransitions() {
        BDD sys = getZero();
        for (Transition t : getSolvingObject().getSysTransition()) {
            sys.orWith(calcSystemTransition(t));
        }
        return sys;
    }

    /**
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * This is for the original definition with checking ndet within the
     * strategy. Since our scheduling does not consider every marking, some non
     * determinism could be overseen. Thus, we changed the definition of non
     * determinism for the Petri game itself and check now non determinism of
     * strategy transitions within the original Net (or less restrictive in the
     * unfolding).
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return BDD with all nondeterministic situations.
     */
    @Deprecated
    BDD ndetStates(int pos) {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getTransitions();
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
                        if (!getSolvingObject().getGame().isEnvironment(place)) {
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
        return nondet;//.andWith(wellformed());
    }

    /**
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * Since our scheduling does not consider every marking, some non
     * determinism with the original version could be overseen.
     *
     * Thus, here we changed the definition of non determinism for the Petri
     * game itself and check now non determinism of strategy transitions within
     * the original Net.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return BDD with all nondeterministic situations.
     */
    BDD ndetStatesReachNet(int pos) {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getTransitions();
        for (Transition t1 : trans) {
            for (Transition t2 : trans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<Place> pre1 = t1.getPreset();
                    Set<Place> pre2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(pre1);
                    intersect.retainAll(pre2);
                    boolean shared = false;
                    BDD sharedPlaces = getZero();
                    for (Place place : intersect) {
                        if (!getSolvingObject().getGame().isEnvironment(place)) {
                            shared = true;
                            sharedPlaces.orWith(codePlace(place, pos, getSolvingObject().getGame().getPartition(place)));
                        }
                    }
                    if (shared && this.getGame().eventuallyEnabled(t1, t2)) { // here check added for firing in the original game
                        BDD first = sharedPlaces.andWith(chosen(t1, pos).andWith(chosen(t2, pos)));
                        nondet = nondet.orWith(first);
                    }
                }
            }
        }
        return nondet;//.andWith(wellformed());
    }

    /**
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * Since our scheduling does not consider every marking, some non
     * determinism with the original version could be overseen.
     *
     * Thus, here we changed the definition of non determinism for the Petri
     * game itself and check now non determinism of strategy transitions within
     * the UNFOLDING of the original Net.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return BDD with all nondeterministic situations.
     */
    BDD ndetStatesReachUnfolding(int pos) {
        // buffer reachability states
        CoverabilityGraph reach = getGame().getReachabilityGraph();
        Set<CoverabilityGraphNode> states = new HashSet<>();
        for (CoverabilityGraphNode state : reach.getNodes()) {
            states.add(state);
        }

        // check all combinations of transitions
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getTransitions();
        for (Transition t1 : trans) {
            for (Transition t2 : trans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<Place> pre1 = t1.getPreset();
                    Set<Place> pre2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(pre1);
                    intersect.retainAll(pre2);
                    for (Place place : intersect) {
                        if (!getSolvingObject().getGame().isEnvironment(place)) { // for every shared system place
                            // check all markings which contain this place if they possibly encounter ndet in the future
                            List<CoverabilityGraphNode> toCheck = new ArrayList<>(states);
                            Set<CoverabilityGraphNode> ndetStates = new HashSet<>();
                            while (!toCheck.isEmpty()) {
                                CoverabilityGraphNode state = toCheck.get(0);
                                if (state.getMarking().getToken(place).getValue() > 0) { // this marking contains the place
                                    List<CoverabilityGraphNode> predecessors = new ArrayList<>();
                                    predecessors.add(state);
                                    checkSuccessors(t1, t2, place, state, toCheck, predecessors, nondet, pos, ndetStates);
                                } else { // does not contain the place
                                    toCheck.remove(state);
                                }
                            }
                        }
                    }
                }
            }
        }
        return nondet;//.andWith(wellformed());
    }

    private boolean checkSuccessors(Transition t1, Transition t2, Place place, CoverabilityGraphNode state,
            List<CoverabilityGraphNode> toCheck, List<CoverabilityGraphNode> predecessors, BDD nondet, int pos, Set<CoverabilityGraphNode> ndetStates) {
////        System.out.println("check " + state.getMarking().toString() + "for " + t1.toString() + " and " + t2.toString());
//        System.out.println("check " + state.getMarking().toString());
////        System.out.println("for " + t1.toString() + " and " + t2.toString());
////        System.out.println("aaaa");
        boolean removed = toCheck.remove(state);
        if (!removed && !ndetStates.contains(state)) { // we already did this state, and it was no ndet
            return false;
        }
        Marking m = state.getMarking();
        if (t1.isFireable(m) && t2.isFireable(m) || ndetStates.contains(state)) { // ndet encountered (in this state, or we reached a state which was already ndet detected)
            // all markings so far are ndet
            for (CoverabilityGraphNode marking : predecessors) {
                ndetStates.add(marking);
                // encode the marking
                BDD mark = getOne();
                for (Place place1 : getGame().getPlaces()) {
                    if (marking.getMarking().getToken(place1).getValue() > 0) {
                        mark.andWith(codePlace(place1, pos, getSolvingObject().getGame().getPartition(place1)));
                    }
                }
                // add the choosing
                BDD ndetDCS = mark.andWith(chosen(t1, pos).andWith(chosen(t2, pos)));
                nondet.orWith(ndetDCS);
            }
            return true;
        } else { // check all reachable markings from this state which not move place p 
            Set<CoverabilityGraphEdge> edges = state.getPostsetEdges();
            for (CoverabilityGraphEdge edge : edges) {
                Transition t = edge.getTransition();
                if (!t.getPreset().contains(place)) { // it's a successor not moving the common place
                    List<CoverabilityGraphNode> preds = new ArrayList<>(predecessors);
                    preds.add(edge.getTarget());
                    boolean found = checkSuccessors(t1, t2, place, edge.getTarget(), toCheck, preds, nondet, pos, ndetStates);
                    if (found) {
//                        System.out.println("is ndet, break succs");
                        return true;
                    }
                }
            }
        }
//        System.out.println("is not ndet");
        return false;
    }

    /**
     * There could ndet states be missed because of the scheduling. A conjecture
     * is that it is enough to take the states where ndet encountered and add
     * all of those states with which we could reach such states by firing
     * transitions which are only dependent of an environment token in the
     * preset. Todo: check if this es really enough! That's not enough and there
     * is most likely no fix in this reduction for this problem.
     *
     * ATTENTION: fixes only a very special case!
     *
     * @return BDD with all nondeterministic situations.
     */
    @Deprecated
    BDD ndetEncountered() {
        BDD Q = getOne();
        BDD Q_ = getBufferedNDet();
        Set<Transition> envTrans = getGame().getEnvTransitions();
        int[] pres = new int[envTrans.size()];
        int[] posts = new int[envTrans.size()];
        int i = 0;
        for (Transition t : envTrans) {
            for (Place pre : t.getPreset()) {
                if (getSolvingObject().getGame().isEnvironment(pre)) {
                    pres[i] = getSolvingObject().getGame().getID(pre);
                }
            }
            for (Place post : t.getPostset()) {
                if (getSolvingObject().getGame().isEnvironment(post)) {
                    posts[i] = getSolvingObject().getGame().getID(post);
                }
            }
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
        }
//        System.out.println("ndet %%%%%%%");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
//        return getZero();
        return Q_;
    }

    /**
     * Calculates the BDD where all tops are zero.
     *
     * Should be expensive, since it compares variables over wide ranges. This
     * should be expensive for BDDs.
     *
     * @return a BDD with all tops set to zero.
     */
    private BDD nTop() {
        BDD nTop = getFactory().one();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD top = TOP[0][i - 1].ithVar(0);
            // todo: really necessary?
            if (!getSolvingObject().isConcurrencyPreserving()) {
                BDD place = codePlace(0, 0, i);
                nTop.andWith(place.not().impWith(top));
            } else {
                nTop.andWith(top);
            }
        }
//        return nTop;
        return nTop;//.andWith(getWellformed());
    }

    protected BDD chosen(Transition t, int pos) {
        BDD c = getOne();
        for (Place p : t.getPreset()) {
            if (!getSolvingObject().getGame().isEnvironment(p)) {
                // Sys places
//                    //if pi=p and it's not top, then t has to be set to one (old version mit not top?)
//                    BDD pl = codePlace(binID, offset).and(bddfac.nithVar(offset + PL_CODE_LEN + 1));
                int token = getSolvingObject().getGame().getPartition(p);
                int id = getSolvingObject().getDevidedTransitions()[token - 1].indexOf(t);
//                c.andWith(getFactory().ithVar(TRANSITIONS[pos][token - 1].vars()[id]));                
                //todo: change on 2019/03/20  the previous line
                //      check if this previously was an optimization which I'm currently not getting?!     
                //      this is needed for the new nondeterminsm
                BDD pl = codePlace(p, pos, token);
                pl.impWith(getFactory().ithVar(TRANSITIONS[pos][token - 1].vars()[id]));
                c.andWith(pl);
            }
        }
        return c;//.andWith(getWellformed());
    }

    protected BDD firable(Transition t, int pos) {
        return enabled(t, pos).andWith(chosen(t, pos));
    }

    @Override
    protected boolean isFirable(Transition t, BDD source) {
        return !source.and(firable(t, 0)).isZero();
    }

    @Override
    public boolean hasFired(Transition t, BDD source, BDD target) {
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }
//        return super.hasFired(t, source, target);

        boolean cp = getSolvingObject().isConcurrencyPreserving();
        BDD trans = source.and(shiftFirst2Second(target));
        BDD out;

        // if purely system transition
        if (cp) {
            out = sysTransitionCP(t).and(trans);
        } else {
            out = sysTransitionNotCP(t).and(trans);
        }
        if (!out.isZero()) {
            return true;
        }

        // is env transition
        if (cp) {
            out = envTransitionCP(t).andWith(trans);
        } else {
            out = envTransitionNotCP(t).andWith(trans);
        }
        return !out.isZero();
    }

    private BDD mcut(int pos) {
        BDD mc = getOne();
        // all tops should be zero
        mc.andWith(getNotTop());
        for (Transition t : getSolvingObject().getSysTransition()) {
            mc.andWith(firable(t, pos).not());
        }
        return mc.andWith(wellformed());
    }

    protected void setPresetAndNeededZeros(Set<Place> pre_sys, List<Integer> visitedToken, BDD all) {
        List<Integer> postTokens = new ArrayList<>(visitedToken);
        // set the dcs for the places in the preset
        for (Place pre : pre_sys) {
            if (!getSolvingObject().getGame().isEnvironment(pre)) { // jump over environment
                int token = getSolvingObject().getGame().getPartition(pre);
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

    /**
     * Sets all successors-bits to the predecessor-bits for the token which had
     * not been affected by this transition.
     *
     * @param all
     * @param visitedToken
     */
    protected void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
        // Positions in dcs not set with places of pre- or postset
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            if (visitedToken.contains(i)) { // jump over already visited token
                continue;
            }
            BDD pl = getZero();
            for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
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
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    protected BDD envPart(Transition t) {
        BDD all = getOne();
        // todo: one environment token case
        List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
        List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
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
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        all.andWith(getMcut());
        return all;
    }

    protected BDD envTransitionCP(Transition t) {
        if (!getSolvingObject().getSysTransition().contains(t)) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);
            // Systempart
            for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
                    if (getSolvingObject().getGame().isEnvironment(place)) {
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
                    } else {
                        //pre_i=post_i'
                        inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                        // top'=1
                        inner.andWith(TOP[1][i - 1].ithVar(1));
                        // all t_i'=0
                        inner.andWith(nothingChosen(1, i));
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

    protected BDD envTransitionNotCP(Transition t) {
        if (!getSolvingObject().getSysTransition().contains(t)) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = getSolvingObject().getGame().getPartition(post);
                if (token != 0) { // jump over environment
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=1
                    all.andWith(TOP[1][token - 1].ithVar(1));
                    // all t_i'=0
                    all.andWith(nothingChosen(1, token));
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

    protected BDD sysTopPart() {
        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); i++) {
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
        return sysT;
    }

    protected BDD sysTransitionCP(Transition t) {
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
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD pl = getZero();
            for (Place place : getSolvingObject().getDevidedPlaces()[i]) {// these are all system places                    
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
            sysN.andWith(pl);
            // top'=0
            sysN.andWith(TOP[1][i - 1].ithVar(0));
        }
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());

        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        return sys;
    }

    protected BDD sysTransitionNotCP(Transition t) {
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
            int token = getSolvingObject().getGame().getPartition(post);
            if (token != 0) { // jump over environment, could not appear...
                visitedToken.add(token);
                //pre_i=post_j'
                sysN.andWith(codePlace(post, 1, token));
                // top'=0
                sysN.andWith(TOP[1][token - 1].ithVar(0));
            }
        }
        // set the dcs for the places in the preset
        setPresetAndNeededZeros(pre_sys, visitedToken, sysN);
        // Positions in dcs not set with places of pre- or postset
        setNotAffectedPositions(sysN, visitedToken);
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        return sys;
    }

    /**
     *
     * @param t
     * @param source
     * @param target
     * @return
     */
    @Deprecated
    public boolean hasFiredManually(Transition t, BDD source, BDD target) {
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }
        // here the preset of t is fitting the source (and the commitment set)! Do not need to test it extra

        // Create bdd mantarget with the postset of t and the rest -1
        // So with "and" we can test if the postset of t also fit to the target
        // additionally create a copy of the target BDD with the places of the postset set to -1
        Pair<List<Place>, List<Place>> post = getSolvingObject().getSplittedPostset(t);
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
            int token = getSolvingObject().getGame().getPartition(p);
            sysPlacesTarget.andWith(codePlace(p, 0, token));
            restTarget = restTarget.exist(getTokenVariables(0, token));
        }
        manTarget.andWith(sysPlacesTarget);

        if ((manTarget.and(target)).isZero()) {
            return false;
        }

        // Create now a copy of the source with all positions set to -1 where preset is set
        Pair<List<Place>, List<Place>> pre = getSolvingObject().getSplittedPreset(t);
        // todo: one environment token case
        BDD restSource = source.id();
        if (!pre.getFirst().isEmpty()) {
            restSource = restSource.exist(getTokenVariables(0, 0));
        }

        List<Place> preSys = pre.getSecond();
        for (Place p : preSys) {
            restSource = restSource.exist(getTokenVariables(0, getSolvingObject().getGame().getPartition(p)));
        }

        // now test if the places not in pre- or postset of t stayed equal between source and target
        return !(restTarget.and(restSource)).isZero();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment sets
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor
     * variables.
     * @return - the variables of the predecessor or the sucessor of a
     * transition.
     */
    @Override
    protected BDD getVariables(int pos) {
        // Existential variables
        BDD variables = PLACES[pos][0].set();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
            variables.andWith(PLACES[pos][i + 1].set());
            variables.andWith(TOP[pos][i].set());
            variables.andWith(TRANSITIONS[pos][i].set());
            // does not seem to make a big different if this or the other version is used
//            variables = variables.and(PLACES[pos][i + 1].set());
//            variables = variables.and(TOP[pos][i].set());
//            variables = variables.and(TRANSITIONS[pos][i].set());
        }
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * sucessor as BDD.
     *
     * This means the varibles for the coding of the place, the top-flag, and
     * the belonging commitment set for a system token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    @Override
    protected BDD getTokenVariables(int pos, int token) {
        if (token == 0) {
            return PLACES[pos][token].set();
        } else {
            BDD variables = (PLACES[pos][token].set());
            variables.andWith(TOP[pos][token - 1].set());
            variables.andWith(TRANSITIONS[pos][token - 1].set());
            return variables;
        }
    }

    /**
     * Creates a BDD which is true, when the predesseccor und the successor of a
     * transition are equivalent.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    protected BDD preBimpSucc() {
        BDD preBimpSucc = PLACES[0][0].buildEquals(PLACES[1][0]);
        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
            // does not seem to make a big different if this or the other version is used
//            preBimpSucc = preBimpSucc.and(PLACES[0][i + 1].buildEquals(PLACES[1][i + 1]));
//            preBimpSucc = preBimpSucc.and(TOP[0][i].buildEquals(TOP[1][i]));
//            preBimpSucc = preBimpSucc.and(TRANSITIONS[0][i].buildEquals(TRANSITIONS[1][i]));
            preBimpSucc.andWith(PLACES[0][i + 1].buildEquals(PLACES[1][i + 1]));
            preBimpSucc.andWith(TOP[0][i].buildEquals(TOP[1][i]));
            preBimpSucc.andWith(TRANSITIONS[0][i].buildEquals(TRANSITIONS[1][i]));
        }
        return preBimpSucc;
    }

    /**
     * Returns a BDD where the commitment sets of the predecessor and the
     * successor of the given token are equal.
     *
     * @param token - the token where the commitment sets should be equal.
     * @return - A BDD where the commitment sets are equal at the given token.
     */
    protected BDD commitmentsEqual(int token) {
        return TRANSITIONS[0][token - 1].buildEquals(TRANSITIONS[1][token - 1]);
    }

    /**
     * Returns a BDD where the commitment set of the predecessor or the
     * successor is the emptyset for a given token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - the token where the commitment set should be empty.
     * @return - A BDD with an empty commitment set at the given position for
     * the given token.
     */
    protected BDD nothingChosen(int pos, int token) {
        return TRANSITIONS[pos][token - 1].ithVar(0);
    }

    @Override
    protected BDD calcEnvironmentTransition(Transition t) {
        boolean cp = getSolvingObject().isConcurrencyPreserving();
        return cp ? envTransitionCP(t) : envTransitionNotCP(t);
    }

    @Override
    protected BDD calcSystemTransition(Transition t) {
        boolean cp = getSolvingObject().isConcurrencyPreserving();
        return cp ? sysTransitionCP(t) : sysTransitionNotCP(t);
    }

    public boolean hasTop(BDD bdd) {
        return !bdd.and(getTop()).isZero();
    }

    protected BDD getTop() {
        return nTop().not();
    }

    protected BDD getNotTop() {
        return nTop();
    }

    @Override
    public boolean isEnvState(BDD state) {
        // it's an env state when it's an mcut
        return !state.and(getMcut()).isZero();
    }

    @Override
    public boolean isBadState(BDD state) {
        return !getBadDCSs().and(state).isZero();
    }

    @Override
    public boolean isSpecialState(BDD state) {
        return !getSpecialDCSs().and(state).isZero();
    }

    @Override
    public boolean isBufferState(BDD state) {
        return hasTop(state);
    }

    public BDD getMcut() {
        return mcut(0);
    }

    BDD getChosen(Transition t) {
        return chosen(t, 0);
    }

    protected BDD getBadDCSs() {
        return calcBadDCSs();
    }

    BDD getSpecialDCSs() {
        return calcSpecialDCSs();
    }

// %%%%%%%%%%%%%%%%%%%%%% Precalculated results / BDDs %%%%%%%%%%%%%%%%%%%%%%%%%
    protected BDD getBufferedNDet() {
        if (ndet == null) {
//            ndet = ndetStates(0);
//            ndet = ndetStatesReachNet(0);
            ndet = ndetStatesReachUnfolding(0);
            //fixes one special case of the ndet problem but takes longer
//            ndet = ndetEncountered();
        }
        return ndet;
    }

    protected BDDDomain getTransitionDomain(int pos, int partition) {
        return TRANSITIONS[pos][partition];
    }

    protected BDDDomain getTopDomain(int pos, int partition) {
        return TOP[pos][partition];
    }

}
