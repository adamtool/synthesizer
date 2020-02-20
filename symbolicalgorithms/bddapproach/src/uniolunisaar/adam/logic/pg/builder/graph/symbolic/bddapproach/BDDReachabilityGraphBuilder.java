package uniolunisaar.adam.logic.pg.builder.graph.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;

/**
 * @author Manuel Gieseking
 * @param <S>
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
    <S extends BDDSolver<? extends Condition<?>>> void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
    <S extends BDDSolver<? extends Condition<?>>> void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
        addState(solver, graph, prev, todoStates, succ);
    }
}