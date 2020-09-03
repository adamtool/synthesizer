package uniolunisaar.adam.logic.pg.builder.petrigame.symbolic.bddapproach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDType2Solver;

/**
 * @author Manuel Gieseking
 */
public class BDDPetriGameWithAllType2StrategyBuilder extends BDDPetriGameStrategyBuilder {

    private static final String DELIM_TYPE_2 = "_2_";

    private static BDDPetriGameWithAllType2StrategyBuilder instance = null;

    public static BDDPetriGameWithAllType2StrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDPetriGameWithAllType2StrategyBuilder();
        }
        return instance;
    }

    private BDDPetriGameWithAllType2StrategyBuilder() {
        BDDPetriGameStrategyBuilder.getInstance();
    }

    private boolean firstType2State = true; // no problem for thread safety as long the deprecated methods are not used
    private Map<BDD, List<Place>> visitedType2Markings;// no problem for thread safety as long the deprecated methods are not used
    private int type2Ids = 0;

    @Override
    void cleanup() {
        firstType2State = true;
        visitedType2Markings = null;
        type2Ids = 0;
    }

    @Override
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            void addSpecialStateBehaviour(BDDSolver<W, SO, SOP> solver, Graph<BDDState, Flow> graph, PetriGame strategy, BDDState prevState, List<Place> prevMarking) {
        super.addSpecialStateBehaviour(solver, graph, strategy, prevState, prevMarking);
        // Must be a solver with type2 ability
        DistrSysBDDType2Solver sol = (DistrSysBDDType2Solver) solver;
        // we only should do something if there are type2 places
        if (sol.isType2(prevState.getState())) {
            BDD succs = sol.getSystem2SuccTransitions(prevState.getState());
            // is there a firable system 2 transition ?
            if (!succs.isZero()) {
                succs = sol.getGoodType2Succs(succs); // get all good successors
                while (!succs.isZero()) {
                    BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
                    List<Transition> trans = sol.getAllSystem2Transition(prevState.getState(), succ);
                    if (trans.isEmpty()) {
                        succs = succs.andWith(succ.not());
                        continue;
                    }
                    boolean foundTransition = false;
                    for (Transition t : trans) {
                        if (t == null) {
                            continue;
                        }
                        foundTransition = true;
                        addType2Strat(sol, strategy, prevState.getState(), succ, t, new ArrayList<>(prevMarking), new HashMap<>());
                        succs = succs.andWith(succ.not());
                    }
                    if (!foundTransition) {
                        succs = succs.andWith(succ.not());
                    }
                }
            }
        }
    }

    private void addType2Strat(DistrSysBDDType2Solver solver, PetriGame strategy, BDD prev, BDD succ, Transition t, List<Place> prevMarking, Map<BDD, List<Place>> visitedStates) {
        // if already added this transition to this marking, skip
        if (alreadyAdded(strategy, t, prevMarking)) {
            return;
        }
//        System.out.println("$$$$$$$$$$$$$$$$ ADD NEW STRAT" + t);
        visitedStates.put(prev, new ArrayList<>(prevMarking));
//        System.out.println("--- add BDD : ");
//        BDDTools.printDecodedDecisionSets(prev, (BDDSolver) solver, true);

        // Add first successor
        boolean newOne = addNewSuccessor(strategy, visitedStates, succ, t, prevMarking);
        visitedStates.put(succ, new ArrayList<>(prevMarking));
//        System.out.println("FIRST");
//        BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
        if (newOne) { // as long as new successors had been added
            prev = succ;
            // find a suitable new one
            BDD succs = solver.getSystem2SuccTransitions(prev);
            // is there any firable transition ?
            if (!succs.isZero()) {
                // is there a good one?
                succs = solver.getGoodType2Succs(succs);
                while (!succs.isZero()) { // search as long as you found a good one (since not all BDD are uniquely solvable (not all flags are set to everytime for type2 transitions :$)
//                    System.out.println("solve new one");
                    succ = succs.satOne(solver.getFirstBDDVariables(), false);
                    List<Transition> trans = solver.getAllSystem2Transition(prev, succ);
//                    System.out.println("trnas " + trans.toString());
                    if (trans.isEmpty()) {
                        succs = succs.andWith(succ.not());
                        continue;
                    }
                    List<Transition> goodSys2Transitions = new ArrayList<>();
                    for (Transition tr : trans) {
                        if (tr == null || goodSys2Transitions.contains(tr)) {
                            continue;
                        }
                        // add only a new successor when not already added
                        if (!alreadyAdded(strategy, tr, prevMarking)) {
                            goodSys2Transitions.add(tr);
                        }
                    }
                    for (int i = 0; i < goodSys2Transitions.size(); i++) {
                        Transition tr = goodSys2Transitions.get(i);
//                        if (i == goodSys2Transitions.size() - 1) { // last one add the strategy
//                            newOne = addNewSuccessor(strategy, visitedStates, succ, tr, prevMarking);
//                            if (newOne) {
//                                visitedStates.put(succ, new ArrayList<>(prevMarking));
//                            }
//                            System.out.println("--- add BDD : ");
//                            BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
//                        } else { // there is more than one good successor
//                            System.out.println("more than one");
                        addType2Strat(solver, strategy, prev, succ, tr, new ArrayList<>(prevMarking), new HashMap<>(visitedStates));
//                        }
                    }
                    succs = succs.andWith(succ.not());
                }
            }
        }
    }

    private boolean alreadyAdded(PetriGame game, Transition t, List<Place> prevMarking) {
//        System.out.println("test transition" + t);
        for (Place p : t.getPreset()) {
            Place place = getSuitablePredecessor(game, p.getId(), prevMarking);
            boolean isSucc = false;
            for (Transition post : place.getPostset()) {
                if (post.getLabel().equals(t.getId())) {
                    isSucc = true;
                }
            }
            if (!isSucc && place.getPostset().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean addNewSuccessor(PetriGame strategy, Map<BDD, List<Place>> visitedStates, BDD succ, Transition t, List<Place> prevMarking) {
//        System.out.println("really add transition" + t);
//        for (BDD bdd : visitedStates.keySet()) {
//            BDDTools.printDecisionSets(bdd, true);
//        }
//        System.out.println("the successor");
//        BDDTools.pr intDecisionSets(succ, true);
        Transition strat_t = strategy.createTransition();
        strat_t.setLabel(t.getId());
        // add preset edges
        for (Place p : t.getPreset()) {
            Place place = getSuitablePredecessor(strategy, p.getId(), prevMarking);
            strategy.createFlow(place, strat_t);
            prevMarking.remove(place);
        }

        if (visitedStates.containsKey(succ)) {
            for (Place p : t.getPostset()) {
                Place place = getSuitablePredecessor(strategy, p.getId(), visitedStates.get(succ));
                strategy.createFlow(strat_t, place);
            }
            return false;
        } else {
            for (Place p : t.getPostset()) {
                Place strat_p = strategy.createPlace(p.getId() + DELIM_TYPE_2 + type2Ids++);
                strategy.setOrigID(strat_p, p.getId());
                strat_p.copyExtensions(p);
                strategy.createFlow(strat_t, strat_p);
                prevMarking.add(strat_p);
            }
            return true;
        }
    }

    /**
     * has a problem for forallBuchi/toyexamples/type2_2
     *
     * @param solver
     * @param graph
     * @param strategy
     * @param prevState
     * @param prevMarking
     * @deprecated
     */
    //@Override
    @Deprecated
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            void addSpecialStateBehaviourOld(BDDSolver<W, SO, SOP> solver, Graph<BDDState, Flow> graph, PetriGame strategy, BDDState prevState, List<Place> prevMarking) {
        super.addSpecialStateBehaviour(solver, graph, strategy, prevState, prevMarking);

//        // Adapt the name of the net
//        strategy.setName("Winning strategy of the system players of the net '" + solver.getNet().getName() + "' with type 2");
        // Must be a solver with type2 ability
        DistrSysBDDType2Solver sol = (DistrSysBDDType2Solver) solver;
        // Add type2-strategy. We only have to consider it once. Since type2-strategies are
        // only strategies where the system can infinitely play on its own. So when a new type2 state is reached
        // the place must be occupied by a token (states are enriched markings), the token cannot be taken 
        // away, since the system won't do it (it's not stupid) and the environment cannot, since only sys places
        // are able to be type2-places. So we can play as long as we need, but the token will stuck there. Maybe we
        // reach other type2-places, but its also a valid strategy to let them stuck there, because we can play
        // infinitely long on our own.
        if (firstType2State && sol.isType2(prevState.getState())) {
//            System.out.println("add special" + prevMarking.toString());
            BDD succs = sol.getSystem2SuccTransitions(prevState.getState());
            // is there a firable transition ?
            if (!succs.isZero()) {
                succs = sol.getGoodType2Succs(succs); // find a good one
                List<Transition> usedTransitions = new ArrayList<>();
                while (!succs.isZero()) {
//                    if (!succs.isZero()) {
                    BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
//                System.out.println("succ");
//                BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
//                        List<Transition> trans = solver.getAllTransitions(prevState.getState(), succ);
                    Transition t = sol.getSystem2Transition(prevState.getState(), succ);
//                        for (Transition t : trans) {
//                        if (usedTransitions.contains(t)) {
//                            continue;
//                        }
                    if (t == null || usedTransitions.contains(t)) {
                        succs = succs.andWith(succ.not());
                        continue;
                    }

//                    BDDTools.printDecodedDecisionSets(prevState.getState(), (BDDSolver) solver, true);
//                    System.out.println(" outer " + t);
//                    BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
//                        usedTransitions.add(t);
//                    visitedType2Markings = new HashMap<>();
//                    type2Step(sol, strategy, prevState.getState(), new ArrayList<>(prevMarking));
                    addType2StratOld(sol, strategy, prevState.getState(), succ, t, new ArrayList<>(prevMarking));
//                        }
                    usedTransitions.add(t);
                    succs = succs.andWith(succ.not());
//                    }
                }
            }
            firstType2State = false;
        }
    }

    /**
     * has a problem for forallBuchi/toyexamples/type2_2
     *
     */
    @Deprecated
    private boolean addNewSuccessorOld(PetriGame strategy, Map<BDD, List<Place>> visitedStates, BDD succ, Transition t, List<Place> prevMarking) {
        Transition strat_t = strategy.createTransition();
        strat_t.setLabel(t.getId());
        // add preset edges
        for (Place p : t.getPreset()) {
            Place place = getSuitablePredecessor(strategy, p.getId(), prevMarking);
            strategy.createFlow(place, strat_t);
            prevMarking.remove(place);
        }

        if (visitedStates.containsKey(succ)) {
            for (Place p : t.getPostset()) {
                Place place = getSuitablePredecessor(strategy, p.getId(), visitedStates.get(succ));
                strategy.createFlow(strat_t, place);
            }
            return false;
        } else {
            for (Place p : t.getPostset()) {
                Place strat_p = strategy.createPlace(p.getId() + DELIM_TYPE_2 + type2Ids++);
                strategy.setOrigID(strat_p, p.getId());
                strat_p.copyExtensions(p);
                strategy.createFlow(strat_t, strat_p);
                prevMarking.add(strat_p);
            }
            return true;
        }
    }

    /**
     * has a problem for forallBuchi/toyexamples/type2_2
     *
     */
    @Deprecated
    private void addType2StratOld(DistrSysBDDType2Solver solver, PetriGame strategy, BDD prev, BDD succ, Transition t, List<Place> prevMarking) {
        Map<BDD, List<Place>> visitedStates = new HashMap<>();
        visitedStates.put(prev, new ArrayList<>(prevMarking));

        // Add first successor
        boolean newOne = addNewSuccessorOld(strategy, visitedStates, succ, t, prevMarking);
        visitedStates.put(succ, new ArrayList<>(prevMarking));
        while (newOne) { // as long as new successors had been added
            prev = succ;
            // find a suitable new one
            BDD succs = solver.getSystem2SuccTransitions(prev);
            // is there any firable transition ?
            if (!succs.isZero()) {
                // is there a good one?
                succs = solver.getGoodType2Succs(succs);
                while (!succs.isZero()) { // search as long as you found a good one (since not all BDD are uniquely solvable (not all flags are set to everytime for type2 transitions :$)
                    succ = succs.satOne(solver.getFirstBDDVariables(), false);
                    t = solver.getSystem2Transition(prev, succ);
                    if (t == null) {
                        succs = succs.andWith(succ.not());
                        continue;
                    }
//                    BDDTools.printDecodedDecisionSets(prev, (BDDSolver) solver, true);
//                    System.out.println(" inner " + t);
//                    BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
                    newOne = addNewSuccessorOld(strategy, visitedStates, succ, t, prevMarking);
                    visitedStates.put(succ, new ArrayList<>(prevMarking));
                    break;// found one
                }
            }
        }
    }

    /**
     * old version for only one successor
     *
     */
    @Deprecated
    private void type2Step(DistrSysBDDType2Solver solver, PetriGame strategy, BDD state, List<Place> marking) {
//        System.out.println("Add type2 strategy");
        visitedType2Markings.put(state, new ArrayList<>(marking));
//        System.out.println("type2 stuff");
        // get all good successors of typ2 system transitions
        BDD succs = solver.getSystem2SuccTransitions(state);
        // is there a firable transition ?
        if (!succs.isZero()) {
            succs = solver.getGoodType2Succs(succs);

//            List<Transition> usedTransitions = new ArrayList<>();
            while (!succs.isZero()) {
                if (!succs.isZero()) {
// todo: the same?               
                    BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
//                System.out.println("succ");
//                BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
// todo: if fullSatOne used, then chosed bad one, should take one till got a good
                    // one ... (the same by all the others)
//                BDD succ = succs.fullSatOne();

//                List<Transition> trans = solver.getAllTransitions(state, succ);
//                if (t == null) {
                    Transition t = solver.getSystem2Transition(state, succ);
                    if (t == null) {
                        succs = succs.andWith(succ.not());
                        continue;
                    }
//                }
//                for (Transition t : trans) {
//                List<Place> m = new ArrayList<>(marking);
//
//                    BDDTools.printDecodedDecisionSets(state, (BDDSolver) solver, true);
//                    System.out.println(" inner " + t);
//                    BDDTools.printDecodedDecisionSets(succ, (BDDSolver) solver, true);
//                    if (usedTransitions.contains(t)) {
//                        succs = succs.andWith(succ.not());
//                        continue;
//                    }
//                    usedTransitions.add(t);
                    Transition strat_t = strategy.createTransition();
                    strat_t.setLabel(t.getId());
                    // add preset edges
                    for (Place p : t.getPreset()) {
                        Place place = getSuitablePredecessor(strategy, p.getId(), marking);
                        strategy.createFlow(place, strat_t);
                        marking.remove(place);
                    }
//                System.out.println("from");
//                Tools.printDecodedDecisionSets(state, game, true);
//
//                System.out.println("to");
//                Tools.printDecodedDecisionSets(succ, game, true);

                    if (visitedType2Markings.containsKey(succ)) {
                        for (Place p : t.getPostset()) {
                            Place place = getSuitablePredecessor(strategy, p.getId(), visitedType2Markings.get(succ));
                            strategy.createFlow(strat_t, place);
                        }
                        break;
                    } else {
                        for (Place p : t.getPostset()) {
                            Place strat_p = strategy.createPlace(p.getId() + DELIM_TYPE_2 + type2Ids++);
                            strategy.setOrigID(strat_p, p.getId());
                            strat_p.copyExtensions(p);
                            strategy.createFlow(strat_t, strat_p);
                            marking.add(strat_p);
                        }
                        type2Step(solver, strategy, succ, new ArrayList<>(marking));
                    }

//                succs = succs.andWith(succ.not());
                }
            }
        } else {
            System.out.println("Nothing fireable in type2 place");
        }
    }

//    private PetriNet strategy;
//    private final BDDPetriGame game;
//    private final BDDGraph graph;
//    private Map<BDD, List<Place>> visitedType2Markings;
//    private int type2Ids = 0;
//
//    public BDDPetriGameStrategyBuilder(BDDGraph graph, BDDPetriGame game) {
//        this.graph = graph;
//        this.game = game;
//    }
//
//    public PetriNet calculateStrategy() {
//        Logger.getInstance().addMessage("Calculate Petri game strategy.");
//        strategy = new PetriNet(graph.getName());
//        BDDState init = graph.getInitial();
//        // create the initial places
//        List<Place> initial = new ArrayList<>();
//        Marking initialMarking = game.getNet().getInitialMarking();
//        for (Place p : game.getNet().getPlaces()) {
//            if (initialMarking.getToken(p).getValue() > 0) {
//                Place place = strategy.createPlace(p.getId() + DELIM + init.getId());
//                place.putExtension("origID", p.getId());
//                place.copyExtensions(p);
//                place.setInitialToken(1);
//                initial.add(place);
//            }
//        }
//        // calculate Petri game
////        calculationStep(init, initial, false);
//        calculateStrategyByBFS(init, initial);
//        // Don't do that since they are also controllers and "interesting" behaviour
//        // delete not used initial places        
////        for (Place place : initial) {
////            if (place.getPostset().isEmpty() && place.getPreset().isEmpty()) {
////                strategy.removePlace(place);
////            }
////        }
//        Logger.getInstance().addMessage("Done calculating Petri game strategy.");
//        return strategy;
//    }
//        private void type2Step(BDD state, List<Place> marking) {
////        System.out.println("Add type2 strategy");
//        visitedType2Markings.put(state, new ArrayList<>(marking));
////        System.out.println("type2 stuff");
//        // get all good successors of typ2 system transitions
//        BDD succs = game.getSystem2SuccTransitions(state);
//        // is there a firable transition ?
//        if (!succs.isZero()) {
//            succs = game.getGoodType2Succs(succs);
//            if (!succs.isZero()) {
//// todo: the same?               
//                BDD succ = succs.satOne(game.getFirstBDDVariables(), false);
//// todo: if fullSatOne used, then chosed bad one, should take one till got a good
//                // one ... (the same by all the others)
////                BDD succ = succs.fullSatOne();
//
//                Transition t = getTransition(state, succ);
//                Transition strat_t = strategy.createTransition();
//                strat_t.setLabel(t.getId());
//                // add preset edges
//                for (Place p : t.getPreset()) {
//                    Place place = getSuitablePredecessor(p.getId(), marking);
//                    strategy.createFlow(place, strat_t);
//                    marking.remove(place);
//                }
////                System.out.println("from");
////                Tools.printDecodedDecisionSets(state, game, true);
////
////                System.out.println("to");
////                Tools.printDecodedDecisionSets(succ, game, true);
//
//                if (visitedType2Markings.containsKey(succ)) {
//                    for (Place p : t.getPostset()) {
//                        Place place = getSuitablePredecessor(p.getId(), visitedType2Markings.get(succ));
//                        strategy.createFlow(strat_t, place);
//                    }
//                } else {
//                    for (Place p : t.getPostset()) {
//                        Place strat_p = strategy.createPlace(p.getId() + DELIM_TYPE_2 + type2Ids++);
//                        strat_p.putExtension("origID", p.getId());
//                        strat_p.copyExtensions(p);
//                        strategy.createFlow(strat_t, strat_p);
//                        marking.add(strat_p);
//                    }
//                    type2Step(succ, new ArrayList<>(marking));
//                }
//            } else {
//                System.out.println("Nothing good firable in type2 place");
//            }
//        } else {
//            System.out.println("Nothing fireable in type2 place");
//        }
//    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% DEPRECATED STUFF %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5
//    /**
//     * Deprecated Depth-first search
//     *
//     * @param state
//     * @param marking
//     * @param lastType2
//     */
//    private void calculationStep(BDDState state, List<Place> marking, boolean lastType2) {
////        if (state.isMcut()) { todo save only mcuts, since they are the only ones which we already could have be visited.
//        visitedmCuts.put(state.getId(), marking);
////        }
//
//        // it's type2
//        if (game.isType2(state.getState())) {
//            if (!lastType2) { // todo: not enough, could have been an other type2 place,
//                // really have to consider if there has just been a new type2 place created
//                visitedType2Markings = new HashMap<>();
//                type2Step(state.getState(), new ArrayList<>(marking));
//                lastType2 = true;
//            }
//        } else {
//            lastType2 = false;
//        }
//
//        // type2 strategy already added
////        int count = 0;
//        for (BDDState succ : graph.getPostset(state.getId())) {
////            if(count==1) {
////                break;
////            }
////            count++;
//            // jump over tops
//            if (game.hasTop(succ.getState())) {
//                for (BDDState succsucc : graph.getPostset(succ.getId())) {
//                    succ = succsucc;
//                    createPlaceTransition(state, succ, new ArrayList<>(marking), lastType2);
//                }
//            } else {
//                createPlaceTransition(state, succ, new ArrayList<>(marking), lastType2);
//            }
//        }
//    }
//
//    /**
//     *
//     * Belongs to deprecated Depth-first search
//     *
//     * @param prev
//     * @param succ
//     * @param marking
//     * @param lastType2
//     */
//    private void createPlaceTransition(BDDState prev, BDDState succ, List<Place> marking, boolean lastType2) {
////        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% MARKING");
////        System.out.println(marking.toString());
//        Transition t = getTransition(prev.getState(), succ.getState());
//        Transition strat_t = strategy.createTransition();
//        strat_t.setLabel(t.getId());
////        System.out.println("Transition " + t);
////        System.out.println("prev ");
////        BDDTools.printDecodedDecisionSets(prev.getState(), game, true);
////        System.out.println("succ");
////        BDDTools.printDecodedDecisionSets(succ.getState(), game, true);
//
//        // add preset edges
//        for (Place p : t.getPreset()) { // all places in preset of the original Petri net
//            Place place = getSuitablePredecessor(p.getId(), marking);
//            strategy.createFlow(place, strat_t);
//            marking.remove(place);
//        }
//
//        // handle postset edges and places
//        if (!visitedmCuts.containsKey(succ.getId())) { // we have a new cut
//            // create all postset places as new copies
//            for (Place p : t.getPostset()) {
//                Place strat_p = strategy.createPlace(p.getId() + DELIM + succ.getId());
//                strat_p.putExtension("origID", p.getId());
//                strat_p.copyExtensions(p);
//                strategy.createFlow(strat_t, strat_p);
//                marking.add(strat_p);
//            }
//            calculationStep(succ, marking, lastType2);
//        } else { // we already had this cut.
//            // Don't create new places, only add the "suitable" flows and delete 
//            // the places which had been created before, which are now double.
//            List<Place> visitedMarking = visitedmCuts.get(succ.getId());
//            // Do not create new places. Take the belonging places and only add
//            // the needed flows            
//            for (Place p : t.getPostset()) {
//                Place place = getSuitablePredecessor(p.getId(), visitedMarking);
//                strategy.createFlow(strat_t, place);
//                // TODO: changes nothing since we have copies...
//                marking.add(place);
//            }
//            // those places which haven't change through the transition but are in the marking
//            // thus a new place had been created before and must be delete and mapped to an existing place
//            List<Place> oldMarking = new ArrayList<>(marking);
//            for (Place place : visitedMarking) {
//                if (!t.getPostset().contains(place)) {
//                    // get old place
//                    for (Place place1 : oldMarking) {
//                        if (!place.getExtension("origID").equals(place1.getExtension("origID"))) {
//                            continue;
//                        }
//                        // it should only be at most one transition, since we are in the unfolding and deterministic
//                        System.out.println(place1.toString());
//                        Iterator<Transition> it = place1.getPreset().iterator();
//                        if (it.hasNext()) {
//                            Transition tr = it.next();
//                            System.out.println(tr);
//                            strategy.removePlace(place1);
//                            strategy.createFlow(tr, place);
//                            // adapt marking
//                            // TODO: changes nothing since we have copies...
//                            marking.remove(place1);
//                            marking.add(place);
//                        }
//                    }
//                }
//            }
//
//            ////////////// OLDVERSION
////            List<Place> visitedmCut = visitedmCuts.get(succ.getId());
////            // Do not create new places. Take the belonging places and only add
////            // the needed flows            
////            for (Place p : t.getPostset()) {
////                System.out.println("getId" + p.getId());
////                //TODO: OLDVERSION naechste Zeile
////                //Place place = getSuitablePredecessor(p.getId(), visitedmCut);                
////                // NEWVERSION naechste Zeile                
////                Place place = strategy.getPlace(p.getId() + DELIM + succ.getId());
////                strategy.createFlow(strat_t, place);
////            }
////            // Delete the places which had already been created in a previous step
////            // but also consists in the old cut.
////            // TODO: WARUM DIES????
////            for (Place p : marking) {
////                // TODO: OLD
////                //String id = p.getId().substring(0, p.getId().indexOf(DELIM));
////                // END OLD
////                // NEW 
////                String id = p.getExtension("origID").toString();
////                // END NEW
////                Place place = getSuitablePredecessor(id, visitedmCut);
////                for (Transition trans : p.getPreset()) {
////                    strategy.createFlow(trans, place);
////                }
////                strategy.removePlace(p);
////            }
//            //////////////////// END OLDVERSION
//        }
//    }
}
