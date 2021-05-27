package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety;

import uniolunisaar.adam.ds.objectives.local.Safety;
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
 * @author Manuel Gieseking
 */
public class DistrSysBDDSafetyLocNDetSolverFactory {

    private static DistrSysBDDSafetyLocNDetSolverFactory instance = null;

    public static DistrSysBDDSafetyLocNDetSolverFactory getInstance() {
        if (instance == null) {
            instance = new DistrSysBDDSafetyLocNDetSolverFactory();
        }
        return instance;
    }

    private DistrSysBDDSafetyLocNDetSolverFactory() {

    }

//    public DistrSysBDDESafetySolver createDistrSysBDDESafetySolver(DistrSysBDDSolvingObject<Safety> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
//        return new DistrSysBDDESafetySolver(obj, opts);
//    }
//
//    public DistrSysBDDESafetyWithNewChainsSolver createDistrSysBDDESafetyWithNewChainsSolver(DistrSysBDDSolvingObject<Safety> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
//        return new DistrSysBDDESafetyWithNewChainsSolver(obj, opts);
//    }
    public DistrSysBDDASafetyLocNDetSolver createDistrSysBDDASafetySolver(DistrSysBDDSolvingObject<Safety> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        return new DistrSysBDDASafetyLocNDetSolver(obj, opts);
    }

}
