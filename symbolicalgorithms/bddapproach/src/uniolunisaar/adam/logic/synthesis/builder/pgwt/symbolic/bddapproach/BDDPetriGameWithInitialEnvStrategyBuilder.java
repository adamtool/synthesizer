package uniolunisaar.adam.logic.synthesis.builder.pgwt.symbolic.bddapproach;

import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDPetriGameWithInitialEnvStrategyBuilder extends BDDPetriGameStrategyBuilder {

    private static BDDPetriGameWithInitialEnvStrategyBuilder instance = null;

    public static BDDPetriGameWithInitialEnvStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDPetriGameWithInitialEnvStrategyBuilder();
        }
        return instance;
    }

    private BDDPetriGameWithInitialEnvStrategyBuilder() {
        BDDPetriGameStrategyBuilder.getInstance();
    }

    /**
     * For the existential Buechi, Reachability (and Parity?) we don't need any
     * type2 analysis, since either we have to cooporate to reach the place or
     * we can do it on our own.But when we can win on our own the enviroment
     * could only stop us by stealing some token and this is something we could
     * prevent by not enabling the transition.Thus, the only problem is, that
     * our strategies is missing the single enviroment transitions when we are
     * looping.Thus with this method we are adding them. For the universal
     * version we have got the problem that for this token the reachable part
     * could be in the enviroment part and we can never get it, when we also
     * have a reachable place in the system part.
     *
     * @param <W>
     * @param <SO>
     * @param <SOP>
     * @param solver
     * @param graph
     * @return
     */
    @Override
    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            PetriGameWithTransits builtStrategy(BDDSolver<W, SO, SOP> solver, Graph<BDDState, Flow> graph) {
        PetriGameWithTransits strategy = super.builtStrategy(solver, graph);
        Set<Place> todo = new HashSet<>();
        // add all enviroment places without successors of the strategy
        for (Place place : strategy.getPlaces()) {
            if (solver.getGame().isEnvironment(place)) {
                if (place.getPostset().isEmpty()) {
                    todo.add(place);
                }
            }
        }
        // Get all transitions only having environment places in their preset
        Set<Transition> trans = solver.getGame().getEnvTransitions();
        int id = 0;
        // add the enviroment strategy
        while (!todo.isEmpty()) {
            Place pre = todo.iterator().next();
            Place orig = solver.getGame().getPlace(solver.getGame().getOrigID(pre));
            for (Transition t : orig.getPostset()) {
                if (trans.contains(t)) { // it's a single enviroment transition -> add
                    // Create the new Transition
                    Transition tstrat = strategy.createTransition();
                    tstrat.setLabel(t.getId());
                    strategy.createFlow(pre, tstrat);
                    for (Place p : t.getPostset()) { // should only be one, since single env transitions (and restricted to one env token)
                        // test if successor is already in net
                        boolean added = false;
                        for (Place pl : strategy.getPlaces()) {
                            if (solver.getGame().getOrigID(pl).equals(p.getId())) {
                                strategy.createFlow(tstrat, pl);
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            //Create place
                            Place strat_p = strategy.createPlace(p.getId() + BDDPetriGameStrategyBuilder.DELIM + id++);
                            strategy.setOrigID(strat_p, p.getId());
                            PetriNetExtensionHandler.setLabel(strat_p, p.getId());
                            strat_p.copyExtensions(p);
                            strategy.createFlow(tstrat, strat_p);
                            todo.add(strat_p);
                        }
                    }
                }
            }
            todo.remove(pre);
        }
        return strategy;
    }

}
