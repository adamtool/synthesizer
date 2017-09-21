package uniolunisaar.adam.symbolic.bddapproach;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable WinningCondition. The
     * solver options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriNet net) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri game. ATTENTION a new PetriGame object is created from the net in
     * game.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param game
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        PetriNet net = game.getNet();
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.existsWinningStrategy();
    }

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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(String path) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(PetriNet net) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri game. ATTENTION a new PetriGame object is created from the net in
     * game.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param game
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriNet getStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        PetriNet net = game.getNet();
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategy();
    }

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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriNet net) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri game. ATTENTION a new PetriGame object is created from the net in
     * game.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param game
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        PetriNet net = game.getNet();
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphStrategy();
    }

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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriNet net) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
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
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGraph(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri game.
     * ATTENTION a new PetriGame object is created from the net in game.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param game
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        PetriNet net = game.getNet();
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getGraphGame();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GRAPH AND PG STRATEGIES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    /**
     * Calculates with the BDDSolver winning strategies for the a file given by
     * the path.
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(String path) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path.
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * The file in APT format has to be equipped with a suitable
     * WinningCondition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(path, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable WinningCondition. The solver
     * options are set to the standards and no tests are skipped.
     *
     * @param net
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(PetriNet net) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable WinningCondition. The solver is
     * initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategies();
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri game. ATTENTION a new PetriGame object is created from the net in
     * game.
     *
     * THIS IS MUCH CHEAPER THEN CREATING THEM INDIVIDUALLY.
     *
     * The solver is initiated with the given options and no tests are skipped.
     *
     * @param game
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableWinningConditionException
     * @throws UnboundedPGException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriNet> getStrategies(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException {
        PetriNet net = game.getNet();
        net.putExtension("winningCondition", win);
        BDDSolver<? extends WinningCondition> solver = BDDSolverFactory.getInstance().getSolver(net, false, so);
        return solver.getStrategies();
    }

}
