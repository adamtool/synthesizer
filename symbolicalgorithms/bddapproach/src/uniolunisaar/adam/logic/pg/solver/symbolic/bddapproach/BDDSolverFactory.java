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

    public BDDSolver<? extends Condition<?>> getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

//    public Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, BDDSolverOptions> getSolver(PetriGame game) throws CouldNotFindSuitableConditionException, SolvingException {
    public BDDSolver<? extends Condition<?>> getSolver(PetriGame game) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, new BDDSolverOptions());
    }

    @Override
    protected <W extends Condition<W>> BDDSolvingObject<W> createSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new BDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException | InvalidPartitionException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected BDDSolver<Safety> getESafetySolver(PetriGame game, Safety con, BDDSolverOptions options) throws SolvingException, NoSuitableDistributionFoundException, NotSupportedGameException, InvalidPartitionException {
        try {
// if it creates a new token chain, use the co-Buchi solver
            BDDSolvingObject<Safety> so = createSolvingObject(game, con);
            for (Transition t : so.getGame().getTransitions()) {
                for (Transit tfl : so.getGame().getTransits(t)) {
                    if (tfl.isInitial()) {
                        return new BDDESafetyWithNewChainsSolver(so, options);
                    }
                }
            }
            return new BDDESafetySolver(so, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDSolver<Safety> getASafetySolver(PetriGame game, Safety con, BDDSolverOptions opts) throws SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            BDDSolvingObject<Safety> so = createSolvingObject(game, con);
            if (opts.isNoType2()) {
                return new BDDASafetyWithoutType2Solver(so, opts);
            } else {
                return new BDDASafetySolver(so, opts);
            }
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected BDDEReachabilitySolver getEReachabilitySolver(PetriGame game, Reachability con, BDDSolverOptions opts) throws SolvingException {
        try {
            BDDSolvingObject<Reachability> so = createSolvingObject(game, con);
            return new BDDEReachabilitySolver(so, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDAReachabilitySolver getAReachabilitySolver(PetriGame game, Reachability con, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDAReachabilitySolver(createSolvingObject(game, con), options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDEBuechiSolver getEBuchiSolver(PetriGame game, Buchi con, BDDSolverOptions opts) throws SolvingException {
        try {
            return new BDDEBuechiSolver(createSolvingObject(game, con), opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDABuechiSolver getABuchiSolver(PetriGame game, Buchi con, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDABuechiSolver(createSolvingObject(game, con), options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }
}
