package uniolunisaar.adam.symbolic.bddapproach.graph;

import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.Graph;

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

    public BDDState addState(BDD state) {
        return super.addState(new BDDState(state));
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
