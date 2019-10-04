package uniolunisaar.adam.symbolic.bddapproach;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamBehavior {

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXISTS WINSTRAT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Calculates with the BDDSolver if a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws
     * uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException
     * @throws uniolunisaar.adam.exceptions.pg.SolvingException
     */
    public static boolean existsWinningStrategy(String path) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws java.io.IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path, BDDSolverOptions so) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver
     * options are set to the standards and no tests are skipped.
     *
     * @param game
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame game) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(game, false);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
        return solver.existsWinningStrategy();
    }

//    /**
//     * Calculates with the BDDSolver if a winning strategy exists for the given
//     * Petri game. ATTENTION a new PetriGame object is created from the net in
//     * game.
//     *
//     * The solver is initiated with the given options and no tests are skipped.
//     *
//     * @param game
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static boolean existsWinningStrategy(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.existsWinningStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PG STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(String path) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(String path, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver
     * options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
        return solver.getStrategy();
    }

//    /**
//     * Calculates with the BDDSolver a winning strategy exists for the given
//     * Petri game. ATTENTION a new PetriGame object is created from the net in
//     * game.
//     *
//     * The solver is initiated with the given options and no tests are skipped.
//     *
//     * @param game
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static PetriGame getStrategy(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a winning graph strategy for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a graph strategy for the given Petri net.
     * Is has to be equipped with a suitable Condition. The solver options are
     * set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri net.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
        return solver.getGraphStrategy();
    }

//    /**
//     * Calculates with the BDDSolver a winning graph strategy for the given
//     * Petri game. ATTENTION a new PetriGame object is created from the net in
//     * game.
//     *
//     * The solver is initiated with the given options and no tests are skipped.
//     *
//     * @param game
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static BDDGraph getGraphStrategy(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getGraphStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a graph game for the a file given by the
     * path. The file in APT format has to be equipped with a suitable
     * Condition. The solver options are set to the standards and no tests are
     * skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the a file given by the
     * path. The file in APT format has to be equipped with a suitable
     * Condition. The solver instatiated with the given options and no tests are
     * skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net. Is
     * has to be equipped with a suitable Condition. The solver options are set
     * to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net. Is
     * has to be equipped with a suitable Condition. The solver is initiated
     * with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
        return solver.getGraphGame();
    }

//    /**
//     * Calculates with the BDDSolver a graph game for the given Petri game.
//     * ATTENTION a new PetriGame object is created from the net in game.
//     *
//     * The solver is initiated with the given options and no tests are skipped.
//     *
//     * @param game
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static BDDGraph getGraphGame(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getGraphGame();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH AND PG STRATEGIES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver winning strategies for the a file given by
     * the path.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable Condition. The
     * solver options are set to the standards and no tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(String path) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable Condition. The
     * solver instatiated with the given options and no tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(String path, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable Condition. The solver options are
     * set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable Condition. The solver is initiated
     * with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
        return solver.getStrategies();
    }

//    /**
//     * Calculates with the BDDSolver a winning strategy exists for the given
//     * Petri game. ATTENTION a new PetriGame object is created from the net in
//     * game.
//     *
//     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
//     *
//     * The solver is initiated with the given options and no tests are skipped.
//     *
//     * @param game
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws uniolunisaar.adam.ds.exceptions.NoStrategyExistentException
//     * @throws uniolunisaar.adam.ds.exceptions.ParameterMissingException
//     */
//    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends Condition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getStrategies();
//    }
}
