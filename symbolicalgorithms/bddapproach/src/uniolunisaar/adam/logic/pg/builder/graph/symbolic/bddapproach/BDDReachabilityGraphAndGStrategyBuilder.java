package uniolunisaar.adam.logic.pg.builder.graph.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;

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
    <S extends BDDSolver<? extends Condition<?>, ? extends BDDSolvingObject<?>>> void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
    <S extends BDDSolver<? extends Condition<?>, ? extends BDDSolvingObject<?>>> void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
        addState(solver, graph, prev, todoStates, succ);
    }
}
