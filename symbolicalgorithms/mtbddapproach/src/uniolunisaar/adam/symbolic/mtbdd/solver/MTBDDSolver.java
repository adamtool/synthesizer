package uniolunisaar.adam.symbolic.mtbdd.solver;

import uniol.apt.adt.pn.PetriNet;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.mtbdd.petrigame.MTBDDPetriGame;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class MTBDDSolver<W extends WinningCondition> extends Solver<MTBDDPetriGame, W, MTBDDSolverOptions> {

     /**
     * Creates a new solver for the given game.
     *
     * @param game - the games which should be solved.
     * @throws SolverDontFitPetriGameException - thrown if the created solver
     * don't fit the given winning objective specified in the given game.
     */
    MTBDDSolver(PetriNet net, boolean skipTests, W winCon, MTBDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(new MTBDDPetriGame(net, skipTests), winCon, opts);
    }

}