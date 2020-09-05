package uniolunisaar.adam.logic.synthesis.builder.graph.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;

/**
 * @author Manuel Gieseking
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
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
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
