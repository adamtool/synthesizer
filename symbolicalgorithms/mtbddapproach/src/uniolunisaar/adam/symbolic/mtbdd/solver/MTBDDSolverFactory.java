package uniolunisaar.adam.symbolic.mtbdd.solver;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.logic.pg.solver.SolverFactory;
import uniolunisaar.adam.ds.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 */
public class MTBDDSolverFactory extends SolverFactory<MTBDDSolverOptions, MTBDDSolver<? extends Condition>> {

    private static MTBDDSolverFactory instance = null;

    public static MTBDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new MTBDDSolverFactory();
        }
        return instance;
    }

    private MTBDDSolverFactory() {

    }

    public MTBDDSolver<? extends Condition> getSolver(PetriGame game, boolean skipTests) throws SolvingException, CouldNotFindSuitableConditionException {
        return super.getSolver(game, skipTests, new MTBDDSolverOptions());
    }

    public MTBDDSolver<? extends Condition> getSolver(String file, boolean skipTests) throws SolvingException, IOException, ParseException, CouldNotFindSuitableConditionException {
        return super.getSolver(file, skipTests, new MTBDDSolverOptions());
    }

    public MTBDDSolver<? extends Condition> getSolver(String file) throws ParseException, IOException, NotSupportedGameException, NoSuitableDistributionFoundException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        return super.getSolver(file, new MTBDDSolverOptions());
    }

    @Override
    protected MTBDDSolver<? extends Condition> getESafetySolver(PetriGame game, Safety winCon, boolean skipTests, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition> getASafetySolver(PetriGame game, Safety winCon, boolean skipTests, MTBDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException, ParameterMissingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition> getEReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, MTBDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException, ParameterMissingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition> getAReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, MTBDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException, ParameterMissingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition> getEBuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, MTBDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException, ParameterMissingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition> getABuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, MTBDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException, ParameterMissingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
