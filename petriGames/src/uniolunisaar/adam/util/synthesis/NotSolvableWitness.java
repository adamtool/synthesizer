package uniolunisaar.adam.util.synthesis;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 */
public class NotSolvableWitness {

    private final Transition t1;
    private final Transition t2;
    private final Marking m1;
    private final Marking m2;

    public NotSolvableWitness(Transition t1, Transition t2, Marking m1, Marking m2) {
        this.t1 = t1;
        this.t2 = t2;
        this.m1 = m1;
        this.m2 = m2;
    }

    public Transition getT1() {
        return t1;
    }

    public Transition getT2() {
        return t2;
    }

    public Marking getM1() {
        return m1;
    }

    public Marking getM2() {
        return m2;
    }

    @Override
    public String toString() {
        return "Transition: " + t1.toString() + " and " + t2.toString() + " in marking " + m1.toString() + " and " + m2.toString();
    }
}
