package uniolunisaar.adam.symbolic.bddapproach.solver;

import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Reachability;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.logic.util.Logger;

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
 * @author Manuel Gieseking
 */
public class BDDReachabilitySolver extends BDDSolver<Reachability> {

    /**
     * Creates a new Reachability solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a reachability condition.
     */
    BDDReachabilitySolver(PetriNet net, boolean skipTests, BDDSolverOptions opts) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, new Reachability(), opts);
    }

    /**
     * Nothing to do here.
     */
    @Override
    void precalculateSpecificBDDs() {
    }

    /**
     * Creates a BDD with all states from which a state with a reachable place
     * can be reached against all behavior of the environment.
     *
     * This is only a standard attractor algorithm.
     *
     * @return BDD with the attractor for the reachable places.
     */
    private BDD attractor() {
        BDD Q = getOne();
        BDD Q_ = reach();
//        System.out.println("toooo reach");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
//        System.out.println("fertisch");
//        int step = 0;
        while (!Q_.equals(Q)) {
            Q = Q_;
            Q_ = preSys(Q).or(Q);
//            ++step;
//            if (step == 1) {
//                System.out.println("Step " + step);
//                BDDTools.printDecodedDecisionSets(preSys(Q), this, true);
//            }
            Q_.andWith(wellformed(0));
        }
        return Q_;
    }

    /**
     * Creates a BDD where a disjunction of all places which should be reached
     * is coded.
     *
     * @return - A BDD with a disjunction of all reachable places.
     */
    private BDD reach() {
        BDD reach = getZero();
        for (Place place : getWinningCondition().getPlaces2Reach()) {
            reach.orWith(codePlace(place, 0, (Integer) place.getExtension("token")));
        }
        return reach;
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
    BDD calcWinningDCSs() {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = attractor();
        //BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
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
        BDD reach = reach();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !reach.and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return graph;
    }
}
