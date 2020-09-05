package uniolunisaar.adam.logic.synthesis.solver.symbolic.mtbdd;

import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.solver.Solver;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.mtbdd.MTBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class MTBDDSolver<W extends Condition<W>> extends Solver<PetriGameWithTransits, W, MTBDDSolvingObject<W>, MTBDDSolverOptions> {

    /**
     * Creates a new solver for the given game.
     *
     * @throws SolverDontFitPetriGameException - thrown if the created solver
     * don't fit the given winning objective specified in the given game.
     */
    MTBDDSolver(MTBDDSolvingObject<W> solvingObject, MTBDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(solvingObject, opts);
    }

}
