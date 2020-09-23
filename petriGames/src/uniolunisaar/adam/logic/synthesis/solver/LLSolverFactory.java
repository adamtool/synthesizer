package uniolunisaar.adam.logic.synthesis.solver;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.LLSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 * @param <SOP>
 * @param <S>
 */
public abstract class LLSolverFactory<SOP extends LLSolverOptions, S extends Solver<PetriGameWithTransits, ? extends Condition<?>, ? extends SolvingObject<PetriGameWithTransits, ? extends Condition<?>, ? extends SolvingObject<PetriGameWithTransits, ? extends Condition<?>, ?>>, SOP>> extends SolverFactory<PetriGameWithTransits, SOP, S> {

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
//    public Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, SOP> getSolver(String path, SOP options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
    public S getSolver(String path, SOP options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        PetriGameWithTransits game = PGTools.getPetriGame(path, options.isSkipTests(), options.isWithAutomaticTransitAnnotation());
//        Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, SOP> solver;
//        solver = getSolver(game, options);
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
//    public Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, SOP> getSolver(PetriGame game, SOP options) throws CouldNotFindSuitableConditionException, SolvingException {
    public S getSolver(PetriGameWithTransits game, SOP options) throws CouldNotFindSuitableConditionException, SolvingException {
//    public <W extends Condition<W>, SO extends SolvingObject<PetriGame, W, SO>> Solver<PetriGame, W, SO, SOP> getSolver(PetriGame game, SOP options) throws CouldNotFindSuitableConditionException, SolvingException {
        Condition.Objective winCon = PGTools.parseConditionFromNetExtensionText(game);
        return getSolver(game, winCon, options);
    }
}
