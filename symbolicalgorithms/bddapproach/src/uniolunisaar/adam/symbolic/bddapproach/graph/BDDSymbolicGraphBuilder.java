package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.List;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDSymbolicGraphBuilder extends BDDGraphBuilder {

    private static BDDSymbolicGraphBuilder instance = null;

    public static BDDSymbolicGraphBuilder getInstance() {
        if (instance == null) {
            instance = new BDDSymbolicGraphBuilder();
        }
        return instance;
    }

    @Override
    Flow addFlow(BDDSolver<? extends Condition> solver, BDDGraph graph, BDDState pre, BDDState succ) {
        List<Transition> trans = solver.getAllTransitions(pre.getState(), succ.getState());
        Flow f = null;
        for (Transition tran : trans) {
            f = graph.addFlow(pre, succ, tran);
        }
        return f;
    }

}
