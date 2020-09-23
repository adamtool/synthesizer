package uniolunisaar.adam.behavior;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamSynthesisBDDBehavior {

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
     * uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException
     */
    public static boolean existsWinningStrategy(String path) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, opts);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the a file
     * given by the path.The file in APT format has to be equipped with a
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
     * @throws
     * uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(String path, BDDSolverOptions so) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, so);
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
    public static boolean existsWinningStrategy(PetriGameWithTransits game) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(game, opts);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean existsWinningStrategy(PetriGameWithTransits net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, so);
        return solver.existsWinningStrategy();
    }

    /**
     * Calculates with the BDDSolver if a winning strategy exists for the given
     * Petri net.
     *
     * The solver is initiated with the given options.
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
    public static boolean existsWinningStrategy(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, win, so);
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
     * given by the path.The file in APT format has to be equipped with a
     * suitable Condition. The solver options are set to the standards and no
     * tests are skipped.
     *
     * @param path
     * @param withCoordinates
     * @return the pair (game, strategy)
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGameWithTransits getStrategy(String path, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, opts);
        PetriGameWithTransits strategy = solver.getStrategy();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategy);
        }
        return strategy;
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the a file
     * given by the path.The file in APT format has to be equipped with a
     * suitable Condition. The solver instatiated with the given options and no
     * tests are skipped.
     *
     * @param path
     * @param so
     * @param withCoordinates
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGameWithTransits getStrategy(String path, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, so);
        PetriGameWithTransits strategy = solver.getStrategy();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategy);
        }
        return strategy;
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
    public static PetriGameWithTransits getStrategy(PetriGameWithTransits net, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, opts);
        PetriGameWithTransits strategy = solver.getStrategy();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategy);
        }
        return strategy;
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static PetriGameWithTransits getStrategy(PetriGameWithTransits net, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, so);
        PetriGameWithTransits strategy = solver.getStrategy();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategy);
        }
        return strategy;
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * The solver is initiated with the given options.
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
    public static PetriGameWithTransits getStrategy(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, win, so);
        PetriGameWithTransits strategy = solver.getStrategy();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategy);
        }
        return strategy;
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
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, opts);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the a file
     * given by the path. The file in APT format has to be equipped with a
     * suitable Condition. The solver instantiated with the given options and no
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
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, so);
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
    public static BDDGraph getGraphStrategy(PetriGameWithTransits net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, opts);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri net. Is has to be equipped with a suitable Condition. The solver is
     * initiated with the given options.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphStrategy(PetriGameWithTransits net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, so);
        return solver.getGraphStrategy();
    }

    /**
     * Calculates with the BDDSolver a winning graph strategy for the given
     * Petri net.
     *
     * The solver is initiated with the given options.
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
    public static BDDGraph getGraphStrategy(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, win, so);
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
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, opts);
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
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, so);
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
    public static BDDGraph getGraphGame(PetriGameWithTransits net) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, opts);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net. Is
     * has to be equipped with a suitable Condition. The solver is initiated
     * with the given options.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static BDDGraph getGraphGame(PetriGameWithTransits net, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, so);
        return solver.getGraphGame();
    }

    /**
     * Calculates with the BDDSolver a graph game for the given Petri net.
     *
     * The solver is initiated with the given options.
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
    public static BDDGraph getGraphGame(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, win, so);
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
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategies(String path, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, opts);
        Pair<BDDGraph, PetriGameWithTransits> strategies = solver.getStrategies();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategies.getSecond());
        }
        return strategies;
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
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategies(String path, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(path, so);
        Pair<BDDGraph, PetriGameWithTransits> strategies = solver.getStrategies();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategies.getSecond());
        }
        return strategies;
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
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategies(PetriGameWithTransits net, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, opts);
        Pair<BDDGraph, PetriGameWithTransits> strategies = solver.getStrategies();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategies.getSecond());
        }
        return strategies;
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * Is has to be equipped with a suitable Condition. The solver is initiated
     * with the given options.
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     */
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategies(PetriGameWithTransits net, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, so);
        Pair<BDDGraph, PetriGameWithTransits> strategies = solver.getStrategies();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategies.getSecond());
        }
        return strategies;
    }

    /**
     * Calculates with the BDDSolver a winning strategy exists for the given
     * Petri net.
     *
     *
     * THIS IS MUCH CHEAPER THAN CREATING THEM INDIVIDUALLY.
     *
     * The solver is initiated with the given options.
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
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategies(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so, boolean withCoordinates) throws SolvingException, CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, CalculationInterruptedException {
        var solver = DistrSysBDDSolverFactory.getInstance().getSolver(net, win, so);
        Pair<BDDGraph, PetriGameWithTransits> strategies = solver.getStrategies();
        if (withCoordinates) {
            PGTools.addCoordinates(solver.getGame(), strategies.getSecond());
        }
        return strategies;
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
