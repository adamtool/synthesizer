package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.kbounded;

import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.pg.solver.LLSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDkBoundedSolverFactory extends LLSolverFactory<BDDSolverOptions, BDDkBoundedSolver> {

    private static BDDkBoundedSolverFactory instance = null;

    public static BDDkBoundedSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDkBoundedSolverFactory();
        }
        return instance;
    }

    private BDDkBoundedSolverFactory() {

    }

    public BDDkBoundedSolver getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

    public BDDkBoundedSolver getSolver(PetriGame game) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, new BDDSolverOptions());
    }

    @Override
    protected <W extends Condition<W>> SolvingObject<PetriGame, W> createSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new BDDkBoundedSolvingObject<>(game, winCon);
        } catch (NoSuitableDistributionFoundException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected BDDkBoundedSolver getESafetySolver(SolvingObject<PetriGame, Safety> solverObject, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getASafetySolver(SolvingObject<PetriGame, Safety> solverObject, BDDSolverOptions options) throws SolvingException {
        return new BDDkBoundedSolver((BDDkBoundedSolvingObject<Safety>) solverObject, options);
    }

    @Override
    protected BDDkBoundedSolver getEReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getAReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getEBuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getABuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
