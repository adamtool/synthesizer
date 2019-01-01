package uniolunisaar.adam.ds.graph;

import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 */
public class Flow {

    private final int sourceid;
    private final int targetid;
    private final Transition transition;

    public Flow(int sourceid, int targetid, Transition transition) {
        this.sourceid = sourceid;
        this.targetid = targetid;
        this.transition = transition;
    }

    public int getSourceid() {
        return sourceid;
    }

    public int getTargetid() {
        return targetid;
    }

    public Transition getTransition() {
        return transition;
    }

    @Override
    public String toString() {
        return sourceid + "->" + targetid;
    }

}
