package uniolunisaar.adam.symbolic.bddapproach.petrigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSafetySolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDPetriGameSafetyStrategyBuilder extends BDDPetriGameStrategyBuilder {
    
    private static final String DELIM_TYPE_2 = "_2_";
    
    private static BDDPetriGameSafetyStrategyBuilder instance = null;
    
    public static BDDPetriGameSafetyStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDPetriGameSafetyStrategyBuilder();
        }
        return instance;
    }
    
    private BDDPetriGameSafetyStrategyBuilder() {
        BDDPetriGameStrategyBuilder.getInstance();
    }
    
    private boolean firstType2State = true;
    private Map<BDD, List<Place>> visitedType2Markings;
    private int type2Ids = 0;
    
    @Override
    void addSpecialStateBehaviour(BDDSolver<? extends WinningCondition> solver, Graph<BDDState, Flow> graph, PetriNet strategy, BDDState prevState, List<Place> prevMarking) {
        super.addSpecialStateBehaviour(solver, graph, strategy, prevState, prevMarking); //To change body of generated methods, choose Tools | Templates.

        // Adapt the name of the net
        strategy.setName("Safety winning strategy of the system players of the net '" + solver.getNet().getName() + "'.");
        // Must be a safety solver
        BDDSafetySolver sol = (BDDSafetySolver) solver;
        // Add type2-strategy. We only have to consider it once. Since type2-strategies are
        // only strategies where the system can infinitely on its own. So when a new type2 state is reached
        // the place must be occupied by a token (states are enriched markings), the token cannot be taken 
        // away, since the system won't do it (is not stupid) and the environment cannot, since only sys places
        // are able to be type2-places. So we can play as long as we need, but the token will stuck there. Maybe we
        // reach other type2-places, but its also a valid strategy to let them stuck there, because we can play
        // infinitely long on our own.
        if (firstType2State && sol.isType2(prevState.getState())) {
            visitedType2Markings = new HashMap<>();
            type2Step(sol, strategy, prevState.getState(), prevMarking);
            firstType2State = false;
        }
        
    }
    
    private void type2Step(BDDSafetySolver solver, PetriNet strategy, BDD state, List<Place> marking) {
//        System.out.println("Add type2 strategy");
        visitedType2Markings.put(state, new ArrayList<>(marking));
//        System.out.println("type2 stuff");
        // get all good successors of typ2 system transitions
        BDD succs = solver.getSystem2SuccTransitions(state);
        // is there a firable transition ?
        if (!succs.isZero()) {
            succs = solver.getGoodType2Succs(succs);
            if (!succs.isZero()) {
// todo: the same?               
                BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
// todo: if fullSatOne used, then chosed bad one, should take one till got a good
                // one ... (the same by all the others)
//                BDD succ = succs.fullSatOne();

                Transition t = solver.getTransition(state, succ);
                Transition strat_t = strategy.createTransition();
                strat_t.setLabel(t.getId());
                // add preset edges
                for (Place p : t.getPreset()) {
                    Place place = getSuitablePredecessor(p.getId(), marking);
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
                        Place place = getSuitablePredecessor(p.getId(), visitedType2Markings.get(succ));
                        strategy.createFlow(strat_t, place);
                    }
                } else {
                    for (Place p : t.getPostset()) {
                        Place strat_p = strategy.createPlace(p.getId() + DELIM_TYPE_2 + type2Ids++);
                        strat_p.putExtension("origID", p.getId());
                        strat_p.copyExtensions(p);
                        strategy.createFlow(strat_t, strat_p);
                        marking.add(strat_p);
                    }
                    type2Step(solver, strategy, succ, new ArrayList<>(marking));
                }
            } else {
                System.out.println("Nothing good firable in type2 place");
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
