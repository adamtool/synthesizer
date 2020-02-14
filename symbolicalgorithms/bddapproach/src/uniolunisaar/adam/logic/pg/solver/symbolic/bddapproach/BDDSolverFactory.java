package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.logic.pg.solver.SolverFactory;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverFactory extends SolverFactory<BDDSolverOptions, BDDSolver<? extends Condition>> {

    private static BDDSolverFactory instance = null;

    public static BDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDSolverFactory();
        }
        return instance;
    }

    private BDDSolverFactory() {

    }

    public BDDSolver<? extends Condition> getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {;
        return super.getSolver(file, new BDDSolverOptions());
    }

    public BDDSolver<? extends Condition> getSolver(String file, boolean skipTests) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, skipTests, new BDDSolverOptions());
    }

    public BDDSolver<? extends Condition> getSolver(PetriGame game, boolean skipTests) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, skipTests, new BDDSolverOptions());
    }

    @Override
    protected BDDSolver<Safety> getESafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions options) throws SolvingException, NoSuitableDistributionFoundException, NotSupportedGameException, InvalidPartitionException {
        try {
// if it creates a new token chain, use the co-Buchi solver
            for (Transition t : game.getTransitions()) {
                for (Transit tfl : game.getTransits(t)) {
                    if (tfl.isInitial()) {
                        return new BDDESafetyWithNewChainsSolver(game, skipTests, winCon, options);
                    }
                }
            }
            return new BDDESafetySolver(game, skipTests, winCon, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDSolver<Safety> getASafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions opts) throws SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            if (opts.isNoType2()) {
                return new BDDASafetyWithoutType2Solver(game, skipTests, winCon, opts);
            } else {
                return new BDDASafetySolver(game, skipTests, winCon, opts);
            }
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected BDDEReachabilitySolver getEReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions opts) throws SolvingException {
        try {
            return new BDDEReachabilitySolver(game, skipTests, winCon, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDAReachabilitySolver getAReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDAReachabilitySolver(game, skipTests, winCon, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDEBuechiSolver getEBuchiSolver(PetriGame game, Buchi winCon,
            boolean skipTests, BDDSolverOptions opts) throws SolvingException {
        try {
            return new BDDEBuechiSolver(game, skipTests, winCon, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected BDDABuechiSolver getABuchiSolver(PetriGame game, Buchi winCon,
            boolean skipTests, BDDSolverOptions options) throws SolvingException {
        try {
            return new BDDABuechiSolver(game, skipTests, winCon, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

}
