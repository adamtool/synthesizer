package uniolunisaar.adam.symbolic.bddapproach.petrigame;

import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.util.PetriNetAnnotator;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

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
     * we can do it on our own. But when we can win on our own the enviroment
     * could only stop us by stealing some token and this is something we could
     * prevent by not enabling the transition. Thus, the only problem is, that
     * our strategies is missing the single enviroment transitions when we are
     * looping. Thus with this method we are adding them. For the universal
     * version we have got the problem that for this token the reachable part
     * could be in the enviroment part and we can never get it, when we also
     * have a reachable place in the system part.
     *
     * @param solver
     * @param graph
     * @return
     */
    @Override
    public PetriNet builtStrategy(BDDSolver<? extends WinningCondition> solver, Graph<BDDState, Flow> graph) {
        PetriNet strategy = super.builtStrategy(solver, graph);
        PetriNet net = solver.getGame().getNet();
        Set<Place> todo = new HashSet<>();
        // add all enviroment places without successors of the strategy
        for (Place place : strategy.getPlaces()) {
            if (place.hasExtension("env")) {
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
            Place orig = net.getPlace((String) pre.getExtension("origID"));
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
                            if (pl.getExtension("origID").equals(p.getId())) {
                                strategy.createFlow(tstrat, pl);
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            //Create place
                            Place strat_p = strategy.createPlace(p.getId() + BDDPetriGameStrategyBuilder.DELIM + id++);
                            PetriNetAnnotator.setOrigID(strat_p, p.getId());
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
