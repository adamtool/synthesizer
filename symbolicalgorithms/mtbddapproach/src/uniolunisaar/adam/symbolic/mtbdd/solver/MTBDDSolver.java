package uniolunisaar.adam.symbolic.mtbdd.solver;

import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.symbolic.mtbdd.petrigame.MTBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class MTBDDSolver<W extends Condition<W>> extends Solver<PetriGame, W, MTBDDSolvingObject<W>, MTBDDSolverOptions> {

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
