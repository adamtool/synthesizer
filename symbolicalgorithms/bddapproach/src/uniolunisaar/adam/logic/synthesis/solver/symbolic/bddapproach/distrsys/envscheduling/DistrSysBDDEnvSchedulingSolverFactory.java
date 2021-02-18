package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.envscheduling;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.global.GlobalSafety;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class DistrSysBDDEnvSchedulingSolverFactory {

    private static DistrSysBDDEnvSchedulingSolverFactory instance = null;

    public static DistrSysBDDEnvSchedulingSolverFactory getInstance() {
        if (instance == null) {
            instance = new DistrSysBDDEnvSchedulingSolverFactory();
        }
        return instance;
    }

    private DistrSysBDDEnvSchedulingSolverFactory() {

    }

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
    public DistrSysBDDEnvSchedulingSolver getSolver(String path, BDDSolverOptions options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        PetriGameWithTransits game = PGTools.getPetriGame(path, options.isSkipTests(), options.isWithAutomaticTransitAnnotation());
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
    public DistrSysBDDEnvSchedulingSolver getSolver(PetriGameWithTransits game, BDDSolverOptions options) throws CouldNotFindSuitableConditionException, SolvingException {
        Condition.Objective winCon = PGTools.parseConditionFromNetExtensionText(game);
        if (winCon != Condition.Objective.GLOBAL_SAFETY) {
            throw new CouldNotFindSuitableConditionException(game);
        }
        GlobalSafety win = new GlobalSafety();
        DistrSysBDDSolvingObject<GlobalSafety> so = createSolvingObject(game, win);
        return new DistrSysBDDEnvSchedulingSolver(so, options);
    }

    public DistrSysBDDEnvSchedulingSolver getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return getSolver(file, new BDDSolverOptions());
    }

    public DistrSysBDDEnvSchedulingSolver getSolver(PetriGameWithTransits game) throws CouldNotFindSuitableConditionException, SolvingException {
        return getSolver(game, new BDDSolverOptions());
    }

    protected <W extends Condition<W>> DistrSysBDDSolvingObject<W> createSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException {
        try {
            return new DistrSysBDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException | InvalidPartitionException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

}
