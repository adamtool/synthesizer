package uniolunisaar.adam.logic.pg.builder.graph.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;

/**
 * @author Manuel Gieseking
 * @param <S>
 */
public class BDDBuchiGraphAndGStrategyBuilder extends BDDGraphAndGStrategyBuilder {

    private static BDDBuchiGraphAndGStrategyBuilder instance = null;

    public static BDDBuchiGraphAndGStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDBuchiGraphAndGStrategyBuilder();
        }
        return instance;
    }

    private BDDBuchiGraphAndGStrategyBuilder() {
        BDDGraphAndGStrategyBuilder.getInstance();
    }

    @Override
    <S extends BDDSolver<? extends Condition<?>>> void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
    <S extends BDDSolver<? extends Condition<?>>> void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        // Get the set F^m
        BDD F = distance.get(-1);
        if (F.and(prev.getState()).isZero()) { // if it's not a buchi state
            BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
            addState(solver, graph, prev, todoStates, succ);
        } else { // choose an arbitray winning successor (adapted from to algorithm in zimmermann to a nearest successor)            
            BDDState succ = getNearestState(solver, succs, distance);
            addState(solver, graph, prev, todoStates, succ);
        }
    }
}
