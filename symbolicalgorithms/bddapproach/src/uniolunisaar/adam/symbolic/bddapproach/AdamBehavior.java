package uniolunisaar.adam.symbolic.bddapproach;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGameExtensionHandler;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
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
     * suitable WinningCondition. The solver options are set to the standards
     * and no tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable WinningCondition. The solver instatiated with the given options
     * and no tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver options are set to the standards and no tests are skipped.
     *
     * @param game
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame game) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(game, false);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
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
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
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
//     * @throws CouldNotFindSuitableWinningConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static boolean existsWinningStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.existsWinningStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PG STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable WinningCondition. The solver options are set to the standards
     * and no tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable WinningCondition. The solver instatiated with the given options
     * and no tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
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
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGame getStrategy(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
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
//     * @throws CouldNotFindSuitableWinningConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static PetriGame getStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a winning graph strategy for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable WinningCondition. The solver options are set to the standards
     * and no tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable WinningCondition. The solver instatiated with the given options
     * and no tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a graph strategy for the given Petri net.
     * Is has to be equipped with a suitable WinningCondition. The solver
     * options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
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
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
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
//     * @throws CouldNotFindSuitableWinningConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static BDDGraph getGraphStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getGraphStrategy();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH STRATEGY %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver a graph game for the a file given by the
     * path. The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the a file given by the
     * path. The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net. Is
     * has to be equipped with a suitable WinningCondition. The solver options
     * are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net. Is
     * has to be equipped with a suitable WinningCondition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
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
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
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
//     * @throws CouldNotFindSuitableWinningConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     */
//    public static BDDGraph getGraphGame(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getGraphGame();
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH AND PG STRATEGIES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver winning strategies for the a file given by
     * the path.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable WinningCondition. The solver
     * options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable WinningCondition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
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
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, win, false, so);
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
//     * @throws CouldNotFindSuitableWinningConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws uniolunisaar.adam.ds.exceptions.NoStrategyExistentException
//     * @throws uniolunisaar.adam.ds.exceptions.ParameterMissingException
//     */
//    public static Pair<BDDGraph, PetriGame> getStrategies(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        PetriNet net = game.getNet();
//        AdamExtensions.setWinningCondition(net, win.getObjective());
//        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
//        return solver.getStrategies();
//    }
}
