package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
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

    /**
     * find a smarter solution. Problem is there is no distances added by the
     * super class for enviroment state and the intital state
     *
     * @param prev
     * @param distance
     * @return
     */
    private int getPrevDistance(BDDState prev, Map<Integer, BDD> distance) {
        for (Map.Entry<Integer, BDD> entry : distance.entrySet()) {
            Integer key = entry.getKey();
            BDD value = entry.getValue();
            if (!value.and(prev.getState()).isZero()) {
                return key;
            }
        }
        return -1;
    }

    @Override
    void addOneSuccessor(BDD succs, BDDSolver<? extends WinningCondition> solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
        BDD min = null;
        int dist = -1;
        int idx = prev.getDistance();
        if (idx == -1) { // should be the initial state and all env states (todo: find a smarter solution)
            idx = getPrevDistance(prev, distance);
        }
        BDD nearestSuccs = distance.get(idx - 1); // there should be a successor in the previous iteration
        while (!succ.isZero()) {
            if (idx == 0) { // if we are already there every successor is OK
                min = succ;
                dist = 1;
                break;
            } else { // otherwise choose one which is getting nearer to the reachable states        
                if (!succ.and(nearestSuccs).isZero()) {
                    min = succ;
                    dist = idx - 1;
                    break;
                }
            }
            succs.andWith(succ.not());
            succ = succs.satOne(solver.getFirstBDDVariables(), false);
        }
        BDDState oldSuccState = graph.contains(min);
        if (oldSuccState != null) { // jump to every already visited cut
            addFlow(solver, graph, prev, oldSuccState);
        } else {
            BDDState succState = graph.addState(min, dist);
            succState.setMcut(solver.isEnvState(min));
            super.addFlow(solver, graph, prev, succState);
            // take the next step
            todoStates.add(succState);
        }
    }

}
