package uniolunisaar.adam.logic.synthesis.solver.symbolic.mtbdd;

import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Buchi;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.local.Reachability;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.synthesis.solver.LLSolverFactory;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.mtbdd.MTBDDSolvingObject;

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
    protected <W extends Condition<W>> MTBDDSolvingObject<W> createSolvingObject(PetriGameWithTransits game, W winCon, MTBDDSolverOptions options) throws NotSupportedGameException {
        try {
            return new MTBDDSolvingObject<>(game, winCon, options.isSkipTests());
        } catch (NetNotSafeException | NoSuitableDistributionFoundException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected MTBDDSolver<Safety> getESafetySolver(PetriGameWithTransits game, Safety con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<Safety> getASafetySolver(PetriGameWithTransits game, Safety con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<Reachability> getEReachabilitySolver(PetriGameWithTransits game, Reachability con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<Reachability> getAReachabilitySolver(PetriGameWithTransits game, Reachability con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<Buchi> getEBuchiSolver(PetriGameWithTransits game, Buchi con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MTBDDSolver<Buchi> getABuchiSolver(PetriGameWithTransits game, Buchi con, MTBDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
