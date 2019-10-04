package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDBuchiGraphBuilder<S extends BDDSolver<? extends Condition>> extends BDDGraphBuilder<S> {

    private static BDDBuchiGraphBuilder instance = null;

    public static BDDBuchiGraphBuilder getInstance() {
        if (instance == null) {
            instance = new BDDBuchiGraphBuilder();
        }
        return instance;
    }

    private BDDBuchiGraphBuilder() {
        BDDGraphBuilder.getInstance();
    }

    @Override
    void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addNearestInitState(solver, graph, inits, todoStates, distance);
    }

    @Override
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
