package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Problem ist hier habe ich nur die Gewinnregion um mit der Umgebung die aus
 * Systemsicht schlechten Situationen zu erreichen. Daraus
 *
 * @author Manuel Gieseking
 */
public class BDDSafetySolverEnv extends BDDSolver<Safety> {

    /**
     * Creates a new Reachability solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a reachability condition.
     */
    BDDSafetySolverEnv(PetriNet net, boolean skipTests, Safety win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, win, opts);
    }
//
//    /**
//     * Nothing to do here.
//     */
//    @Override
//    void precalculateSpecificBDDs() {
//    }

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
        BDD Q_ = bad();
//        System.out.println("toooo reach");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
//        System.out.println("fertisch");
//        int step = 0;
        while (!Q_.equals(Q)) {
            Q = Q_;
            Q_ = preEnv(Q).or(Q);
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
     * Creates a BDD where a disjunction of all bad places and bad situations
     * (ndet and deadlock) is coded.
     *
     * @return - A BDD with a disjunction of all reachable places.
     */
    private BDD bad() {
        return badPlaces().orWith(ndet()).orWith(deadSysDCS());
    }

    private BDD badPlaces() {
        BDD reach = getZero();
        for (Place place : getWinningCondition().getBadPlaces()) {
            reach.orWith(codePlace(place, 0, AdamExtensions.getToken(place)));
        }
        return reach;
    }

    private BDD ndet() {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition t1 : trans) {
            for (Transition t2 : trans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<Place> pre1 = t1.getPreset();
                    Set<Place> pre2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(pre1);
                    intersect.retainAll(pre2);
                    boolean shared = false;
                    for (Place place : intersect) {
                        if (!AdamExtensions.isEnviroment(place)) {
                            shared = true;
                        }
                    }
                    if (shared) {
                        BDD first = firable(t1, 0).andWith(firable(t2, 0));
                        nondet = nondet.orWith(first);
                    }
                }
            }
        }
        return nondet.andWith(wellformed());
    }

    private BDD deadSysDCS() {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, 0).not());
            buf.orWith(enabled(t, 0));
        }
        dead.andWith(buf);
        return dead.andWith(getTop().not()).andWith(wellformed());
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
        BDD fixedPoint = attractor();
        fixedPoint = fixedPoint.not().andWith(wellformed());
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
        BDD bad = bad();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !bad.and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return graph;
    }
}
