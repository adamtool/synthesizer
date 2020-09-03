package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.reach;

import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
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
public class DistrSysBDDReachabilitySolverFactory {

    private static DistrSysBDDReachabilitySolverFactory instance = null;

    public static DistrSysBDDReachabilitySolverFactory getInstance() {
        if (instance == null) {
            instance = new DistrSysBDDReachabilitySolverFactory();
        }
        return instance;
    }

    private DistrSysBDDReachabilitySolverFactory() {

    }

    public DistrSysBDDEReachabilitySolver createDistrSysBDDEReachabilitySolver(DistrSysBDDSolvingObject<Reachability> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        return new DistrSysBDDEReachabilitySolver(obj, opts);
    }

    public DistrSysBDDAReachabilitySolver createDistrSysBDDAReachabilitySolver(DistrSysBDDSolvingObject<Reachability> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        return new DistrSysBDDAReachabilitySolver(obj, opts);
    }

}
