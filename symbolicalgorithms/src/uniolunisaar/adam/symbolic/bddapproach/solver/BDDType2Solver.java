package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.util.List;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 */
public interface BDDType2Solver {

    public boolean isType2(BDD states);

    public BDD getSystem2SuccTransitions(BDD state);

    public BDD getGoodType2Succs(BDD trans);

    public BDD getFirstBDDVariables();

    public Transition getSystem2Transition(BDD source, BDD target);
}
