package uniolunisaar.adam.symbolic.bddapproach.petrigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.logic.util.Logger;

/**
 * @author Manuel Gieseking
 */
public class BDDPetriGameStrategyBuilder {

    private static final String DELIM = "_";
    private static BDDPetriGameStrategyBuilder instance = null;

    public static BDDPetriGameStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDPetriGameStrategyBuilder();
        }
        return instance;
    }

    BDDPetriGameStrategyBuilder() {
    }

    public PetriNet builtStrategy(BDDSolver solver, Graph<BDDState, Flow> graph) {
        Logger.getInstance().addMessage("Calculate Petri game strategy.");
        PetriNet strategy = new PetriNet("Winning strategy of the system players of the net '" + solver.getNet().getName() + "'.");
        BDDState init = graph.getInitial();
        // create the initial places
        List<Place> initial = new ArrayList<>();
        Marking initialMarking = solver.getNet().getInitialMarking();
        for (Place p : solver.getNet().getPlaces()) {
            if (initialMarking.getToken(p).getValue() > 0) {
                Place place = strategy.createPlace(p.getId() + DELIM + init.getId());
                place.putExtension("origID", p.getId());
                place.copyExtensions(p);
                place.setInitialToken(1);
                initial.add(place);
            }
        }
        // calculate Petri game
        calculateStrategyByBFS(solver, graph, strategy, init, initial);

        Logger.getInstance().addMessage("Done calculating Petri game strategy.");
        return strategy;
    }

    private void calculateStrategyByBFS(BDDSolver solver, Graph<BDDState, Flow> graph, PetriNet strategy, BDDState initialState, List<Place> initialMarking) {
        Map<Integer, List<Place>> visitedMCuts = new HashMap<>();
        LinkedList<Pair<BDDState, List<Place>>> todoStates = new LinkedList<>();
        todoStates.add(new Pair<>(initialState, initialMarking));
        // add to visited mcuts 
        if (initialState.isMcut()) {
            visitedMCuts.put(initialState.getId(), initialMarking);
        }
        while (!todoStates.isEmpty()) {
            Pair<BDDState, List<Place>> state = todoStates.poll();
            BDDState prevState = state.getFirst();
            List<Place> prevMarking = state.getSecond();
            for (Flow flow : graph.getPostsetFlows(prevState.getId())) {
                BDDState succState = graph.getState(flow.getTargetid());
                List<Place> succMarking = new ArrayList<>(prevMarking);

                // Jump over tops (tau transitions) todo: all this top stuff should be in the safety builder!
                Transition strat_t = null;
                Transition t = flow.getTransition();
                if (t == null) {
                    if (!visitedMCuts.containsKey(succState.getId())) { // if not already visited
                        // there can only be one successor of a top state in a strategy (deterministic)
                        todoStates.add(new Pair<>(succState, succMarking));
                        // add to visited mcuts 
                        if (succState.isMcut()) {
                            visitedMCuts.put(succState.getId(), succMarking);
                        }
                        continue; // break is also OK, since there is only one
                    } else {
                        // if we have an already visited mcut, we have to find 
                        // the transition which let to the previous state (since this was a
                        // tau transition) to put the right postset to this transition
                        t = (Transition) prevState.getExtension("t");
                        strat_t = (Transition) prevState.getExtension("strat_t");
                        for (Place place : strat_t.getPostset()) { // delete the former wrong created postset
                            strategy.removePlace(place);
                        }
                    }
                } else {
                    // Create the new Transition
                    strat_t = strategy.createTransition();
                    strat_t.setLabel(t.getId());

                    // Create the preset edges
                    for (Place p : t.getPreset()) { // all places in preset of the original Petri net
                        Place place = getSuitablePredecessor(p.getId(), prevMarking);
                        strategy.createFlow(place, strat_t);
                        succMarking.remove(place);
                    }
                }

                // Calculate the postset edges and possibly the new postset places
                if (succState.isMcut() && visitedMCuts.containsKey(succState.getId())) { // already visited mcut
                    // Don't create new places, only add the "suitable" flows and delete 
                    // the places which had been created before, which are now double.
                    List<Place> visitedMarking = visitedMCuts.get(succState.getId());
                    // Do not create new places. Take the belonging places and only add
                    // the needed flows            
                    for (Place p : t.getPostset()) {
                        Place place = getSuitablePredecessor(p.getId(), visitedMarking);
                        strategy.createFlow(strat_t, place);
                    }
                    // those places which haven't change through the transition but are in the marking
                    // thus a new place could had been created before and must be deleted and mapped to an existing place
                    for (Place place : visitedMarking) {
                        if (!containsID(t.getPostset(), place)) {
                            Place place1 = null;
                            // find the possibly unnecessarly created place
                            for (Place p : prevMarking) {
                                if (!(!place.getExtension("origID").equals(p.getExtension("origID")) || place.equals(p))) {
                                    // so there is a different incarnation of the same place in the already visited mcut and the current marking
                                    place1 = p;
                                    break;
                                }
                            }
                            if (place1 != null) {
                                // it should only be at most one transition, since we are in the unfolding and deterministic
                                Iterator<Transition> it = place1.getPreset().iterator();
                                Transition tr = it.next();
                                strategy.removePlace(place1);
                                strategy.createFlow(tr, place);
                                // update all markings
                                // first the one for the other successors:
                                prevMarking.remove(place1);
                                prevMarking.add(place);
                                // then all those which are still in the todo list. E.g. previous flows 
                                // with the same prevMarking calculated in the flow loop before
                                for (Pair<BDDState, List<Place>> pair : todoStates) {
                                    List<Place> mark = pair.getSecond();
                                    if (mark.contains(place1)) {
                                        mark.remove(place1);
                                        mark.add(place);
                                    }
                                    // TODO: Test if necessare or call by by reference
                                    //todoStates.push(new Pair<>(pair.getFirst(), mark));
                                }
                                // that's it since the visitedMarkings belong to mcuts and we would already had updated them
                            }
                        }
                    }
                } else {
                    // create all postset places as new copies
                    for (Place p : t.getPostset()) {
                        Place strat_p = strategy.createPlace(p.getId() + DELIM + succState.getId());
                        strat_p.putExtension("origID", p.getId());
                        strat_p.copyExtensions(p);
                        strategy.createFlow(strat_t, strat_p);
                        succMarking.add(strat_p);
                    }
                    // if the succ has a top, save the transition leeding to the state
                    // for creating the pg strategy
                    if (solver.hasTop(succState.getState())) { // TODO: attention problem with the top stuff. Should be separated class
                        succState.putExtension("strat_t", strat_t);
                        succState.putExtension("t", t);
                    }
                    todoStates.add(new Pair<>(succState, succMarking));
                    // add to visited mcuts 
                    if (succState.isMcut()) {
                        visitedMCuts.put(succState.getId(), succMarking);
                    }
                }
            }
            addSpecialStateBehaviour(solver, graph, strategy, prevState, prevMarking);
        }
    }

    /**
     * No special behaviour is needed here.
     *
     * @param solver
     * @param graph
     * @param strategy
     */
    void addSpecialStateBehaviour(Solver solver, Graph<BDDState, Flow> graph, PetriNet strategy, BDDState prevState, List<Place> prevMarking) {

    }

    private boolean containsID(Set<Place> places, Place place) {
        for (Place p : places) {
            if (p.getId().equals(place.getExtension("origID"))) {
                return true;
            }
        }
        return false;
        // TODO: change to 1.8
        //return places.stream().anyMatch((p) -> (p.getId().equals(place.getExtension("origID"))));
    }

    /**
     * Searches in marking for a place with the original ID equal to placeid. In
     * marking the id should be put as extension to the place with the hashtag
     * origID.
     *
     * Where the function is used their should be only one, since otherwise this
     * would mean that two incarnation of the same place of the original net are
     * occupied in the unfolding.
     *
     * TODO: Attention here is a problem if we don't have a safe net! Then this
     * comparison could find more than one place.
     *
     * @param placeid - the ID of the place in the original Petri net
     * @param marking - the current marking of the unfolding
     * @return the place of the marking with the given placeid
     */
    Place getSuitablePredecessor(String placeid, List<Place> marking) {
        for (Place p : marking) {
            String id = (String) p.getExtension("origID");
            if (id.equals(placeid)) {
                return p;
            }
        }
        //todo: error
        throw new RuntimeException("ERROR in PG-StratBuilder Predecessor!" + placeid + " " + marking.toString());
    }
}
