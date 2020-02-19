package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach;

import java.io.IOException;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.pg.solver.LLSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverFactory extends LLSolverFactory<BDDSolverOptions, BDDSolver<? extends Condition<?>>> {

    private static BDDSolverFactory instance = null;

    public static BDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDSolverFactory();
        }
        return instance;
    }

    private BDDSolverFactory() {

    }

    public BDDSolver<? extends Condition> getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

    public BDDSolver<? extends Condition> getSolver(PetriGame game) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, new BDDSolverOptions());
    }

    @Override
    protected <W extends Condition<W>> SolvingObject<PetriGame, W> createSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new BDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException | InvalidPartitionException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected BDDSolver<Safety> getESafetySolver(SolvingObject<PetriGame, Safety> solverObject, BDDSolverOptions options) throws SolvingException, NoSuitableDistributionFoundException, NotSupportedGameException, InvalidPartitionException {
        try {
// if it creates a new token chain, use the co-Buchi solver
            for (Transition t : solverObject.getGame().getTransitions()) {
                for (Transit tfl : solverObject.getGame().getTransits(t)) {
                    if (tfl.isInitial()) {
                        return new BDDESafetyWithNewChainsSolver((BDDSolvingObject<Safety>) solverObject, options);
                    }
                }
            }
            return new BDDESafetySolver((BDDSolvingObject<Safety>) solverObject, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDSolver<Safety> getASafetySolver(SolvingObject<PetriGame, Safety> solverObject, BDDSolverOptions opts) throws SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            if (opts.isNoType2()) {
                return new BDDASafetyWithoutType2Solver((BDDSolvingObject<Safety>) solverObject, opts);
            } else {
                return new BDDASafetySolver((BDDSolvingObject<Safety>) solverObject, opts);
            }
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected BDDEReachabilitySolver getEReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, BDDSolverOptions opts) throws SolvingException {
        try {
            return new BDDEReachabilitySolver((BDDSolvingObject<Reachability>) solverObject, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDAReachabilitySolver getAReachabilitySolver(SolvingObject<PetriGame, Reachability> solverObject, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDAReachabilitySolver((BDDSolvingObject<Reachability>) solverObject, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDEBuechiSolver getEBuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, BDDSolverOptions opts) throws SolvingException {
        try {
            return new BDDEBuechiSolver((BDDSolvingObject<Buchi>) solverObject, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDABuechiSolver getABuchiSolver(SolvingObject<PetriGame, Buchi> solverObject, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDABuechiSolver((BDDSolvingObject<Buchi>) solverObject, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }
}
