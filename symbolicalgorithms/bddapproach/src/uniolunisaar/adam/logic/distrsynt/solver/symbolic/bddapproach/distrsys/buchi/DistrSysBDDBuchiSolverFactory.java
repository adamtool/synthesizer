package uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.buchi;

import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;

/**
 * This is just a helper class since JAVA does not allow restricting the
 * visibility to subpackages but the constructor of the single solver should not
 * be visible to the outside but have to be called in 'DistrSysBDDSolverFactor'.
 *
 * Do not use the factory to create the solver. Use 'DistrSysBDDSolverFactor'.
 *
 * @author thewn
 */
public class DistrSysBDDBuchiSolverFactory {

    private static DistrSysBDDBuchiSolverFactory instance = null;

    public static DistrSysBDDBuchiSolverFactory getInstance() {
        if (instance == null) {
            instance = new DistrSysBDDBuchiSolverFactory();
        }
        return instance;
    }

    private DistrSysBDDBuchiSolverFactory() {

    }

    public DistrSysBDDEBuechiSolver createDistrSysBDDEBuechiSolver(DistrSysBDDSolvingObject<Buchi> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        return new DistrSysBDDEBuechiSolver(obj, opts);
    }

    public DistrSysBDDABuechiSolver createDistrSysBDDABuechiSolver(DistrSysBDDSolvingObject<Buchi> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        return new DistrSysBDDABuechiSolver(obj, opts);
    }

}
