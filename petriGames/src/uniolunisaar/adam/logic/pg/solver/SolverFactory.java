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
 * @param <SOP>
 * @param <S>
 */
public abstract class SolverFactory<G extends IPetriGame, SOP extends SolverOptions, S extends Solver<G, ? extends Condition<?>, ? extends SolvingObject<G, ? extends Condition<?>, ? extends SolvingObject<G, ? extends Condition<?>, ?>>, SOP>> {

    /**
     * Creates a solver for the given game in the APT file.The winning condition
     * and type of the game has to be annotated.
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
                return getESafetySolver(game, new Safety(true), options);
            }
            case A_SAFETY: {
                return getASafetySolver(game, new Safety(false), options);
            }
            case E_REACHABILITY: {
//                SolvingObject<G, Reachability, ? extends SolvingObject<G, Reachability, ?>> obj = createSolvingObject(game, new Reachability(true));
//                return getEReachabilitySolver(obj, options);
                return getEReachabilitySolver(game, new Reachability(true), options);
            }
            case A_REACHABILITY: {
//                SolvingObject<G, Reachability, ? extends SolvingObject<G, Reachability, ?>> obj = createSolvingObject(game, new Reachability(false));
//                return getAReachabilitySolver(obj, options);
                return getAReachabilitySolver(game, new Reachability(false), options);
            }
            case E_BUCHI: {
//                SolvingObject<G, Buchi, ? extends SolvingObject<G, Buchi, ?>> obj = createSolvingObject(game, new Buchi(true));
//                return getEBuchiSolver(obj, options);
                return getEBuchiSolver(game, new Buchi(true), options);
            }
            case A_BUCHI: {
//                SolvingObject<G, Buchi, ? extends SolvingObject<G, Buchi, ?>> obj = createSolvingObject(game, new Buchi(false));
//                return getABuchiSolver(obj, options);
                return getABuchiSolver(game, new Buchi(false), options);
            }
            case LTL:
                break;
        }
        return null;
    }

    protected abstract <W extends Condition<W>> SolvingObject<G, W, ? extends SolvingObject<G, W, ?>> createSolvingObject(G game, W winCon) throws NotSupportedGameException;

//    protected abstract Solver<G, Safety, ? extends SolvingObject<G, Safety, ? extends SolvingObject<G, Safety, ?>>, SOP> getESafetySolver(G game, Safety con, SOP options) throws SolvingException;
    protected abstract S getESafetySolver(G game, Safety con, SOP options) throws SolvingException;

//    protected abstract Solver<G, Safety, ? extends SolvingObject<G, Safety, ? extends SolvingObject<G, Safety, ?>>, SOP> getASafetySolver(G game, Safety con, SOP options) throws SolvingException;
    protected abstract S getASafetySolver(G game, Safety con, SOP options) throws SolvingException;

//    protected abstract Solver<G, Reachability, ? extends SolvingObject<G, Reachability, ? extends SolvingObject<G, Reachability, ?>>, SOP> getEReachabilitySolver(G game, Reachability con, SOP options) throws SolvingException;
    protected abstract S getEReachabilitySolver(G game, Reachability con, SOP options) throws SolvingException;

//    protected abstract Solver<G, Reachability, ? extends SolvingObject<G, Reachability, ? extends SolvingObject<G, Reachability, ?>>, SOP> getAReachabilitySolver(G game, Reachability con, SOP options) throws SolvingException;
    protected abstract S getAReachabilitySolver(G game, Reachability con, SOP options) throws SolvingException;

//    protected abstract Solver<G, Buchi, ? extends SolvingObject<G, Buchi, ? extends SolvingObject<G, Buchi, ?>>, SOP> getEBuchiSolver(G game, Buchi con, SOP options) throws SolvingException;
    protected abstract S getEBuchiSolver(G game, Buchi con, SOP options) throws SolvingException;

//    protected abstract Solver<G, Buchi, ? extends SolvingObject<G, Buchi, ? extends SolvingObject<G, Buchi, ?>>, SOP> getABuchiSolver(G game, Buchi con, SOP options) throws SolvingException;
    protected abstract S getABuchiSolver(G game, Buchi con, SOP options) throws SolvingException;

}
