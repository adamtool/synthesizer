package uniolunisaar.adam.logic.pg.solver;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolverOptions;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 * @param <S>
 * @param <SOP>
 */
public abstract class SolverFactory<SOP extends SolverOptions, S extends Solver<? extends SolvingObject<? extends PetriGame, ? extends Condition>, SOP>> {

    /**
     *
     * @param file
     * @param options
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws CouldNotFindSuitableConditionException
     */
    public S getSolver(String file, SOP options) throws SolvingException, ParseException, IOException, CouldNotFindSuitableConditionException {
        return getSolver(file, false, options);
    }

    public S getSolver(String file, boolean skipTests, SOP options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        PetriGame game = PGTools.getPetriGame(file, skipTests, true); // todo: should I put the withAutomatic into the SOlverOptions?
        return getSolver(game, skipTests, options);
    }

    public S getSolver(PetriGame game, boolean skipTests, SOP options) throws CouldNotFindSuitableConditionException, SolvingException {
        Condition.Objective winCon = PNWTTools.parseConditionFromNetExtensionText(game);
        return getSolver(game, winCon, skipTests, options);
    }

    public S getSolver(PetriGame game, Condition.Objective winCon, boolean skipTests, SOP options) throws SolvingException {
        switch (winCon) {
            case E_SAFETY:
                return getESafetySolver(game, new Safety(true), skipTests, options);
            case A_SAFETY:
                return getASafetySolver(game, new Safety(false), skipTests, options);
            case E_REACHABILITY:
                return getEReachabilitySolver(game, new Reachability(true), skipTests, options);
            case A_REACHABILITY:
                return getAReachabilitySolver(game, new Reachability(false), skipTests, options);
            case E_BUCHI:
                return getEBuchiSolver(game, new Buchi(true), skipTests, options);
            case A_BUCHI:
                return getABuchiSolver(game, new Buchi(false), skipTests, options);
            case LTL:
                break;

        }
        return null;
    }

    protected abstract S getESafetySolver(PetriGame game, Safety winCon, boolean skipTests, SOP options) throws SolvingException;

    protected abstract S getASafetySolver(PetriGame game, Safety winCon, boolean skipTests, SOP options) throws SolvingException;

    protected abstract S getEReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, SOP options) throws SolvingException;

    protected abstract S getAReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, SOP options) throws SolvingException;

    protected abstract S getEBuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, SOP options) throws SolvingException;

    protected abstract S getABuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, SOP options) throws SolvingException;

}
