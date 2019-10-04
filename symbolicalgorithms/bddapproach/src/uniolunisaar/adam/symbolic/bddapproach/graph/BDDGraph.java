package uniolunisaar.adam.symbolic.bddapproach.graph;

import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;

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

    public BDDState addState(BDD state, BDDSolver<? extends Condition> solver) {
        return addState(state, -1, solver);
    }

    public BDDState addState(BDD state, int dist, BDDSolver<? extends Condition> solver) {
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
