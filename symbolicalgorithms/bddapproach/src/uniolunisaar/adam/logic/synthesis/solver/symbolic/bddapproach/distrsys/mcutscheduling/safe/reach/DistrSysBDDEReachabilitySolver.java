package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.reach;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.util.HashMap;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.local.Reachability;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach.BDDReachabilityGraphAndGStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.builder.pgwt.symbolic.bddapproach.BDDPetriGameWithInitialEnvStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a reachability objective by simply using an attractor
 * function. Don't need any type2 analysis or deadlock-avoiding constraint.
 *
 * Problem what to do with the non-deterministic states? Already a fixed-point
 * combi of safety and reachability? It is not possible to totally omit them
 * because when a ndet state is a successor of an env state, then the env state
 * would errorously marked as good. Furthermore, if it's the only successor of a
 * sys state, then also this sys state would errorously marked as good.
 *
 * We solve it by marking every non-deterministic state as end-state and
 * deleting non-determinisic states from the set of good states to reach and
 * from the inital states.
 *
 * @author Manuel Gieseking
 */
public class DistrSysBDDEReachabilitySolver extends DistrSysBDDSolver<Reachability> {

    /**
     * Creates a new Reachability solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a reachability condition.
     */
    DistrSysBDDEReachabilitySolver(DistrSysBDDSolvingObject<Reachability> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, opts);
    }

    /**
     * Creates a BDD where a disjunction of all places which should be reached
     * is coded.
     *
     * @return - A BDD with a disjunction of all reachable places.
     */
    private BDD reach() {
        BDD reach = getZero();
        for (Place place : getSolvingObject().getWinCon().getPlaces2Reach()) {
            reach.orWith(codePlace(place, 0, getSolvingObject().getGame().getPartition(place)));
        }
        return reach;
    }

    @Override
    protected BDD initial() {
        BDD init = super.initial();
//                    BDDTools.printDecodedDecisionSets(init, this, true);
        init.andWith(getBufferedNDet().not());
        return init;
    }

    /**
     * Returns the winning decisionsets for the system players.
     *
     * In this case only an attractor to the reachable states.
     *
     * @return - A BDD containing all states from which a state with a reachable
     * place is able the be reached against all behavior of the environment.
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD goodReach = reach().andWith(getBufferedNDet().not()).andWith(wellformed(0));
//        BDDTools.printDecodedDecisionSets(goodReach, this, true);
        BDD fixedPoint = attractor(goodReach, false, distance, false, null);
        //BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    /**
     * Non-deterministic states don't have any successor. This allows to avoid
     * non-deterministic strategies.
     *
     * @return
     */
    @Override
    protected BDD calcSystemTransitions() {
        BDD sys = super.calcSystemTransitions();
        sys.andWith(getBufferedNDet().not());
        return sys;
    }

    @Override
    protected BDD calcBadDCSs() {
        return getBufferedNDet();
    }

    @Override
    protected BDD calcSpecialDCSs() {
        return reach();
    }

    /**
     * Returns the graph game for the reachability objective.Is the standard
     * graph game, but before returning we are just marking the reachable states
     * as special.
     *
     *
     * @return - The graph game for the reachability objective.
     * @throws uniolunisaar.adam.exceptions.CalculationInterruptedException
     */
    @Override
    public BDDGraph getGraphGame() throws CalculationInterruptedException {
        BDDGraph graph = super.getGraphGame();
        BDD reach = reach();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !reach.and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
            if (!getBufferedNDet().and(state.getState()).isZero()) {
                state.setBad(true);
            }
        }
        return graph;
    }

    @Override
    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        HashMap<Integer, BDD> distance = new HashMap<>();
        BDD win = calcWinningDCSs(distance);
        super.setBufferedWinDCSs(win);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph strat = BDDReachabilityGraphAndGStrategyBuilder.getInstance().builtGraphStrategy(this, distance);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        for (BDDState state : strat.getStates()) { // mark all special states
            if (!reach().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return strat;
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pn = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    @Override
    public Pair<BDDGraph, PetriGameWithTransits> getStrategies() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pstrat = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
    }

}
