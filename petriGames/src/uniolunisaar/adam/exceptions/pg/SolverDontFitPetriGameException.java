package uniolunisaar.adam.exceptions.pg;

import uniolunisaar.adam.ds.petrigame.IPetriGame;
import uniolunisaar.adam.ds.synthesis.solver.Solver;
import uniolunisaar.adam.ds.synthesis.solver.SolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.ds.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 */
public class SolverDontFitPetriGameException extends Exception {

    private static final long serialVersionUID = 1L;

    public SolverDontFitPetriGameException(Solver<IPetriGame, ? extends Condition<?>, ? extends SolvingObject<? extends IPetriGame, ? extends Condition<?>, ?>, ? extends SolverOptions> sol, Exception cause) {
        super("The solver: " + sol.getClass().getSimpleName() + " can't be used to solve Petri games with the winning Condition: " + sol.getWinningCondition().getClass().getName(), cause);
    }
}
