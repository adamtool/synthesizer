package uniolunisaar.adam.logic.distrsynt.builder.graph.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDReachabilityGraphAndGStrategyBuilder extends BDDGraphAndGStrategyBuilder {

    private static BDDReachabilityGraphAndGStrategyBuilder instance = null;

    public static BDDReachabilityGraphAndGStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDReachabilityGraphAndGStrategyBuilder();
        }
        return instance;
    }

    private BDDReachabilityGraphAndGStrategyBuilder() {
        BDDGraphAndGStrategyBuilder.getInstance();
    }

    @Override
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
        addState(solver, graph, prev, todoStates, succ);
    }
}
