package uniolunisaar.adam.ds.graph.symbolic.bddapproach;

import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDGraph extends Graph<BDDState, Flow> {

    public BDDGraph(String name) {
        super(name);
    }

    public BDDGraph(Graph<BDDState, Flow> g) {
        super(g);
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            BDDState addState(BDD state, BDDSolver<W, SO, SOP> solver) {
        return addState(state, -1, solver);
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            BDDState addState(BDD state, int dist, BDDSolver<W, SO, SOP> solver) {
        String value = BDDTools.getDecodedDecisionSets(state, solver);
        value = value.substring(0, value.indexOf("->"));
        return super.addState(new BDDState(state, dist, value));
    }

    @Override
    public BDDState addState(BDDState state) {
        return super.addState(state);
    }

    public Flow addFlow(BDDState source, BDDState target, Transition t) {
        Flow flow = new Flow(source.getId(), target.getId(), t);
        return super.addFlow(flow);
    }

    public BDDState contains(BDD state) {
        for (BDDState graphState : this.getStates()) {
            if (graphState.isEqualTo(state)) {
                return graphState;
            }
        }
        return null;
    }

}
