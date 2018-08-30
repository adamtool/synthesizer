package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.io.IOException;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrigame.TokenFlow;
import uniolunisaar.adam.logic.solver.SolverFactory;
import uniolunisaar.adam.ds.winningconditions.Buchi;
import uniolunisaar.adam.ds.winningconditions.Reachability;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverFactory extends SolverFactory<BDDSolverOptions, BDDSolver<? extends WinningCondition>> {

    private static BDDSolverFactory instance = null;

    public static BDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDSolverFactory();
        }
        return instance;
    }

    private BDDSolverFactory() {

    }

    public BDDSolver<? extends WinningCondition> getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableWinningConditionException, SolvingException {;
        return super.getSolver(file, new BDDSolverOptions());
    }

    public BDDSolver<? extends WinningCondition> getSolver(String file, boolean skipTests) throws IOException, ParseException, CouldNotFindSuitableWinningConditionException, SolvingException {
        return super.getSolver(file, skipTests, new BDDSolverOptions());
    }

    public BDDSolver<? extends WinningCondition> getSolver(PetriGame game, boolean skipTests) throws CouldNotFindSuitableWinningConditionException, SolvingException {
        return super.getSolver(game, skipTests, new BDDSolverOptions());
    }

    @Override
    protected BDDSolver<Safety> getESafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions options) throws SolvingException {
        // if it creates a new token chain, use the co-Buchi solver
        for (Transition t : game.getTransitions()) {
            for (TokenFlow tfl : game.getTokenFlow(t)) {
                if (tfl.isInitial()) {
                    return new BDDESafetyWithNewChainsSolver(game, skipTests, winCon, options);
                }
            }
        }
        return new BDDESafetySolver(game, skipTests, winCon, options);
    }

    @Override
    protected BDDSolver<Safety> getASafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions opts) throws SolvingException {
        return new BDDASafetySolver(game, skipTests, winCon, opts);
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected BDDEReachabilitySolver getEReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions opts) throws SolvingException {
        return new BDDEReachabilitySolver(game, skipTests, winCon, opts);
    }

    @Override
    protected BDDAReachabilitySolver getAReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions options) throws SolvingException {
        return new BDDAReachabilitySolver(game, skipTests, winCon, options);
    }

    @Override
    protected BDDEBuechiSolver getEBuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, BDDSolverOptions opts) throws SolvingException {
        return new BDDEBuechiSolver(game, skipTests, winCon, opts);
    }

    @Override
    protected BDDABuechiSolver getABuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, BDDSolverOptions options) throws SolvingException {
        return new BDDABuechiSolver(game, skipTests, winCon, options);
    }

}
