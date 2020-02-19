package uniolunisaar.adam.symbolic.mtbdd.solver;

import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.pg.solver.LLSolverFactory;
import uniolunisaar.adam.symbolic.mtbdd.petrigame.MTBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class MTBDDSolverFactory extends LLSolverFactory<MTBDDSolverOptions, MTBDDSolver<? extends Condition<?>>> {

    private static MTBDDSolverFactory instance = null;

    public static MTBDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new MTBDDSolverFactory();
        }
        return instance;
    }

    private MTBDDSolverFactory() {

    }

    @Override
    protected <W extends Condition<W>> SolvingObject<PetriGame, W> createSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new MTBDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getESafetySolver(SolvingObject<PetriGame, Safety> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getASafetySolver(SolvingObject<PetriGame, Safety> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getEReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getAReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getEBuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<? extends Condition<?>> getABuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
