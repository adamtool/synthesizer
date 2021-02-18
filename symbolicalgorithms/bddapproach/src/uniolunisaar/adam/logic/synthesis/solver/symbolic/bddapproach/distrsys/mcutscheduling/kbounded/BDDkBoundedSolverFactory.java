package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.kbounded;

import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Buchi;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.local.Reachability;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.logic.synthesis.solver.Solver;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.synthesis.solver.LLSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDkBoundedSolverFactory extends LLSolverFactory<BDDSolverOptions, Solver<PetriGameWithTransits, ? extends Condition<?>, ? extends SolvingObject<PetriGameWithTransits, ? extends Condition<?>, ? extends SolvingObject<PetriGameWithTransits, ? extends Condition<?>, ?>>, BDDSolverOptions>> {

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
    protected <W extends Condition<W>> BDDkBoundedSolvingObject<W> createSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException {
        try {
            return new BDDkBoundedSolvingObject<>(game, winCon);
        } catch (NoSuitableDistributionFoundException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected BDDkBoundedSolver getESafetySolver(PetriGameWithTransits game, Safety con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getASafetySolver(PetriGameWithTransits game, Safety con, BDDSolverOptions options) throws SolvingException {
        return new BDDkBoundedSolver(createSolvingObject(game, con), options);
    }

    @Override
    protected Solver<PetriGameWithTransits, Reachability, BDDkBoundedSolvingObject<Reachability>, BDDSolverOptions> getEReachabilitySolver(PetriGameWithTransits game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGameWithTransits, Reachability, BDDkBoundedSolvingObject<Reachability>, BDDSolverOptions> getAReachabilitySolver(PetriGameWithTransits game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGameWithTransits, Buchi, BDDkBoundedSolvingObject<Buchi>, BDDSolverOptions> getEBuchiSolver(PetriGameWithTransits game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Solver<PetriGameWithTransits, Buchi, BDDkBoundedSolvingObject<Buchi>, BDDSolverOptions> getABuchiSolver(PetriGameWithTransits game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
