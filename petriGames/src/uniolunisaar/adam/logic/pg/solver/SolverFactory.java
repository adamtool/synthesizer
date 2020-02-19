package uniolunisaar.adam.logic.pg.solver;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.petrigame.IPetriGame;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolverOptions;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <S>
 * @param <SOP>
 */
public abstract class SolverFactory<G extends IPetriGame, SOP extends SolverOptions, S extends Solver<G, ? extends SolvingObject<G, ? extends Condition<?>>, SOP>> {

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
    public abstract S getSolver(String path, SOP options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException;

    /**
     * Creates a solver for the given Petri game.The winning condition has to be
     * in the extension of the game.
     *
     * @param game
     * @param options
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws SolvingException
     */
    public abstract S getSolver(G game, SOP options) throws CouldNotFindSuitableConditionException, SolvingException;

    public S getSolver(G game, Condition.Objective winCon, SOP options) throws SolvingException {
        switch (winCon) {
            case E_SAFETY: {
                SolvingObject<G, Safety> obj = createSolvingObject(game, new Safety(true));
                return getESafetySolver(obj, options);
            }
            case A_SAFETY: {
                SolvingObject<G, Safety> obj = createSolvingObject(game, new Safety(false));
                return getASafetySolver(obj, options);
            }
            case E_REACHABILITY: {
                SolvingObject<G, Reachability> obj = createSolvingObject(game, new Reachability(true));
                return getEReachabilitySolver(obj, options);
            }
            case A_REACHABILITY: {
                SolvingObject<G, Reachability> obj = createSolvingObject(game, new Reachability(false));
                return getAReachabilitySolver(obj, options);
            }
            case E_BUCHI: {
                SolvingObject<G, Buchi> obj = createSolvingObject(game, new Buchi(true));
                return getEBuchiSolver(obj, options);
            }
            case A_BUCHI: {
                SolvingObject<G, Buchi> obj = createSolvingObject(game, new Buchi(false));
                return getABuchiSolver(obj, options);
            }
            case LTL:
                break;
        }
        return null;
    }

    protected abstract <W extends Condition<W>> SolvingObject<G, W> createSolvingObject(G game, W winCon) throws NotSupportedGameException;

    protected abstract S getESafetySolver(SolvingObject<G, Safety> solverObject, SOP options) throws SolvingException;

    protected abstract S getASafetySolver(SolvingObject<G, Safety> solverObject, SOP options) throws SolvingException;

    protected abstract S getEReachabilitySolver(SolvingObject<G, Reachability> solverObject, SOP options) throws SolvingException;

    protected abstract S getAReachabilitySolver(SolvingObject<G, Reachability> solverObject, SOP options) throws SolvingException;

    protected abstract S getEBuchiSolver(SolvingObject<G, Buchi> solverObject, SOP options) throws SolvingException;

    protected abstract S getABuchiSolver(SolvingObject<G, Buchi> solverObject, SOP options) throws SolvingException;

}
