package uniolunisaar.adam.logic.pg.solver;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.solver.LLSolverOptions;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 * @param <SOP>
 * @param <S>
 */
public abstract class LLSolverFactory<SOP extends LLSolverOptions, S extends Solver<PetriGame, ? extends SolvingObject<PetriGame, ? extends Condition<?>>, SOP>> extends SolverFactory<PetriGame, SOP, S> {

    /**
     * Creates a solver for the given game in the APT file. The winning
     * condition and type of the game has to be annotated.
     *
     * @param path - to the file in APT format
     * @param options
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws CouldNotFindSuitableConditionException
     * @throws SolvingException
     */
    @Override
    public S getSolver(String path, SOP options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        PetriGame game = PGTools.getPetriGame(path, options.isSkipTests(), options.isWithAutomaticTransitAnnotation());
        return getSolver(game, options);
    }

    /**
     * Creates a solver for the given Petri game. The winning condition has to
     * be in the extension of the game.
     *
     * @param game
     * @param options
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws SolvingException
     */
    @Override
    public S getSolver(PetriGame game, SOP options) throws CouldNotFindSuitableConditionException, SolvingException {
        Condition.Objective winCon = PNWTTools.parseConditionFromNetExtensionText(game);
        return getSolver(game, winCon, options);
    }
}
