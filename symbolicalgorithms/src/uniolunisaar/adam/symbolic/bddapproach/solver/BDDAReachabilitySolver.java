package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Reachability;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.TokenTree;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.logic.tokenflow.TokenTreeCreator;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDReachabilityGraphBuilder;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameWithInitialEnvStrategyBuilder;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
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
public class BDDAReachabilitySolver extends BDDSolver<Reachability> {

    private BDDDomain[] TOKENTREES;

    /**
     * Creates a new universal reachability solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs. Creates
     * and annotates the token trees.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a reachability condition.
     */
    BDDAReachabilitySolver(PetriNet net, boolean skipTests, Reachability win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, win, opts);
        TokenTreeCreator.createAndAnnotateTokenTree(getNet());
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Creates the variables for this solver. This have to be overriden since
     * the flags for the tokentrees have to be added.
     *
     * Codierung: p_i_0 - Environment token n - TokenCount m - number of
     * transitions c_i - token trees
     *
     * |p_i_0|occ|p_i_1|top|t_1|...|t_m| ... |p_i_n|top|t_1|...|t_m|c_1|...|c_l]
     */
    @Override
    void createVariables() {
        int tokencount = getGame().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        TOKENTREES = new BDDDomain[2];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (getGame().isConcurrencyPreserving()) ? 0 : 1;
            PLACES[i][0] = getFactory().extDomain(getGame().getPlaces()[0].size() + add);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getGame().getPlaces()[j + 1].size() + add);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
            // one flag for each tokentree
            BigInteger nbTrees = BigInteger.valueOf(2).pow(AdamExtensions.getTokenTrees(getNet()).size());
            TOKENTREES[i] = getFactory().extDomain(nbTrees);
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    /**
     */
    private BDD winningStates() {
        // Set all tokentrees to 1
        BigInteger nbTrees = BigInteger.valueOf(2).pow(AdamExtensions.getTokenTrees(getNet()).size()).add(BigInteger.valueOf(-1));
        BDD reach = TOKENTREES[0].ithVar(nbTrees);
        return reach;
    }

    @Override
    BDD initial() {
        System.out.println(AdamExtensions.getTokenTrees(getNet()).toString());
        BDD init = super.initial();
        System.out.println("super");
                    BDDTools.printDecodedDecisionSets(init, this, true);
        init.andWith(ndetStates(0).not());
        System.out.println("nondet");
                    BDDTools.printDecodedDecisionSets(init, this, true);
        Marking initial = getNet().getInitialMarking();
//        for (Place place : getGame().getNet().getPlaces()) {
//            if (initial.getToken(place).getValue() > 0) {
//                List<Integer> treeIds = BDDTools.getTreeIDs(place);
//                for (Integer treeId : treeIds) {
//                    System.out.println("IDS"+treeId);
//                    init.andWith(getFactory().ithVar(TOKENTREES[0].vars()[treeId]));
//                    BDDTools.printDecodedDecisionSets(init, this, true);
//                }
//            }
//        }
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
    BDD calcWinningDCSs(Map<Integer, BDD> distance) {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD goodReach = winningStates().andWith(ndetStates(0).not()).andWith(wellformed(0));
//        BDDTools.printDecodedDecisionSets(goodReach, this, true);
        BDD fixedPoint = attractor(goodReach, false, distance);
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
    BDD sysTransitionsNotCP() {
        BDD sys = super.sysTransitionsNotCP();
        sys.andWith(ndetStates(0).not());
        return sys;
    }

    /**
     * Non-deterministic states don't have any successor. This allows to avoid
     * non-deterministic strategies.
     *
     * @return
     */
    @Override
    BDD sysTransitionsCP() {
        BDD sys = super.sysTransitionsCP();
        sys.andWith(ndetStates(0).not());
        return sys;
    }

    /**
     * Returns the graph game for the reachability objective.
     *
     * Is the standard graph game, but before returning we are just marking the
     * reachable states as special.
     *
     * @return - The graph game for the reachability objective.
     */
    @Override
    public BDDGraph getGraphGame() {
        BDDGraph graph = super.getGraphGame();
        BDD reach = winningStates();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !reach.and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return graph;
    }

    @Override
    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException {
        HashMap<Integer, BDD> distance = new HashMap<>();
        BDD win = calcWinningDCSs(distance);
        super.setBufferedWinDCSs(win);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph strat = BDDReachabilityGraphBuilder.getInstance().builtGraphStrategy(this, distance);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        for (BDDState state : strat.getStates()) { // mark all special states
            if (!winningStates().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return strat;
    }

    @Override
    protected PetriNet calculateStrategy() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pn = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    @Override
    public Pair<BDDGraph, PetriNet> getStrategies() throws NoStrategyExistentException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriNet pstrat = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
    }

}
