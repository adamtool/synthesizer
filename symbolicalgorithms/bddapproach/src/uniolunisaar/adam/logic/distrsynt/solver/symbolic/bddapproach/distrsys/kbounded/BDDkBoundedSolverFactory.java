package uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.kbounded;

import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.pg.solver.LLSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDkBoundedSolverFactory extends LLSolverFactory<BDDSolverOptions, Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, BDDSolverOptions>> {

    private static BDDkBoundedSolverFactory instance = null;

    public static BDDkBoundedSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDkBoundedSolverFactory();
        }
        return instance;
    }

    private BDDkBoundedSolverFactory() {

    }

    @Override
    protected <W extends Condition<W>> BDDkBoundedSolvingObject<W> createSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new BDDkBoundedSolvingObject<>(game, winCon);
        } catch (NoSuitableDistributionFoundException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected BDDkBoundedSolver getESafetySolver(PetriGame game, Safety con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getASafetySolver(PetriGame game, Safety con, BDDSolverOptions options) throws SolvingException {
        return new BDDkBoundedSolver(createSolvingObject(game, con), options);
    }

    @Override
    protected Solver<PetriGame, Reachability, BDDkBoundedSolvingObject<Reachability>, BDDSolverOptions> getEReachabilitySolver(PetriGame game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGame, Reachability, BDDkBoundedSolvingObject<Reachability>, BDDSolverOptions> getAReachabilitySolver(PetriGame game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGame, Buchi, BDDkBoundedSolvingObject<Buchi>, BDDSolverOptions> getEBuchiSolver(PetriGame game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGame, Buchi, BDDkBoundedSolvingObject<Buchi>, BDDSolverOptions> getABuchiSolver(PetriGame game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
