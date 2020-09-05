package uniolunisaar.adam.logic.synthesis.solver;

import uniolunisaar.adam.ds.petrigame.IPetriGame;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <W>
 * @param <SO>
 * @param <SOP>
 */
public abstract class Solver<G extends IPetriGame, W extends Condition<W>, SO extends SolvingObject<G, W, SO>, SOP extends SolverOptions> {

    // the game and winning condition which should be solved
    private final SO solvingObject;
    private final SOP solverOpts;

    private Boolean existsWinStrat = null;
    private PetriGame strategy = null;

    /**
     * Creates a new solver for the given game and winning condition.
     *
     * @param solverObject
     * @param options
     */
    protected Solver(SO solverObject, SOP options) {// throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //clazz.getConstructor(PetriNet.class, boolean.class).newInstance(net, skipTests);
        this.solvingObject = solverObject;
        this.solverOpts = options;
    }

    protected abstract boolean exWinStrat() throws CalculationInterruptedException;

    protected abstract PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException;

    public boolean existsWinningStrategy() throws CalculationInterruptedException {
        if (existsWinStrat == null) {
            existsWinStrat = exWinStrat();
        }
        return existsWinStrat;
    }

    public PetriGame getStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        if (strategy == null) {
            strategy = calculateStrategy();
        }
        return strategy;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% GETTER / SETTER 
    public G getGame() {
        return solvingObject.getGame();
    }

    public Condition<W> getWinningCondition() {
        return solvingObject.getWinCon();
    }

    public SO getSolvingObject() {
        return solvingObject;
    }

    public SOP getSolverOpts() {
        return solverOpts;
    }
}
