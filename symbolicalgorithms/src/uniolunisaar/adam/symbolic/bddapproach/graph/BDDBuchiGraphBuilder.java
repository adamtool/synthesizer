package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDBuchiGraphBuilder extends BDDGraphBuilder {

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
    void addOneSuccessor(BDD succs, BDDSolver<? extends WinningCondition> solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        // Get the set attr0(recurm(F))\recurm(F)
        BDD choosed = distance.get(-1);
        if (choosed.and(succs).isZero()) { // first branch, thus choose strategy for the attractor
            BDDState succ = getNearestSuccessor(succs, solver, prev, distance);
            addState(solver, graph, prev, todoStates, succ);
        } else { // choose an arbitray winning successor
            addAllSuccessors(succs, solver, graph, prev, todoStates, true);
        }
    }
}
