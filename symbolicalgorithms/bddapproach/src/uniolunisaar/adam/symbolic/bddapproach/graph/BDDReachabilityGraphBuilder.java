package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDReachabilityGraphBuilder extends BDDGraphBuilder {

    private static BDDReachabilityGraphBuilder instance = null;

    public static BDDReachabilityGraphBuilder getInstance() {
        if (instance == null) {
            instance = new BDDReachabilityGraphBuilder();
        }
        return instance;
    }

    private BDDReachabilityGraphBuilder() {
        BDDGraphBuilder.getInstance();
    }

    @Override
    void addOneInitState(BDDSolver<? extends Condition> solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver,graph,inits,todoStates,distance);
    }

    @Override
    void addOneSuccessor(BDD succs, BDDSolver<? extends Condition> solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
        addState(solver, graph, prev, todoStates, succ);
    }
}
