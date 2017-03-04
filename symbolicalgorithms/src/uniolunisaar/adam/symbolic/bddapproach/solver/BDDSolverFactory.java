package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uniol.apt.adt.pn.PetriNet;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.ds.solver.SolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverFactory extends SolverFactory<BDDSolver> {

    private static BDDSolverFactory instance = null;

    public static BDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDSolverFactory();
        }
        return instance;
    }

    private BDDSolverFactory() {

    }

    @Override
    protected BDDSolver getSafetySolver(PetriNet pn, boolean skipTests) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDSafetySolver(pn, skipTests);
    }

    @Override
    protected BDDSolver getReachabilitySolver(PetriNet pn, boolean skipTests) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDReachabilitySolver(pn, skipTests);
    }

    @Override
    protected BDDSolver getBuchiSolver(PetriNet pn, boolean skipTests) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDBuechiSolver(pn, skipTests);
    }

}
