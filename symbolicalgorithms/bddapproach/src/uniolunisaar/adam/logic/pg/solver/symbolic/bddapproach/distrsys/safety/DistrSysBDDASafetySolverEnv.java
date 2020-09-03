package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.safety;

import uniolunisaar.adam.ds.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.util.benchmarks.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Never really finished
 *
 * @author Manuel Gieseking
 */
@Deprecated
public class DistrSysBDDASafetySolverEnv extends DistrSysBDDSolver<Safety> {

    /**
     * Creates a new Reachability solver for a given game.
     *
     * Already creates the needed variables and precalculates some BDDs.
     *
     * @param game - the game to solve.
     * @throws SolverDontFitPetriGameException - Is thrown if the winning
     * condition of the game is not a reachability condition.
     */
    DistrSysBDDASafetySolverEnv(DistrSysBDDSolvingObject<Safety> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, opts);
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
        for (Place place : getSolvingObject().getWinCon().getBadPlaces()) {
            reach.orWith(codePlace(place, 0, getSolvingObject().getGame().getPartition(place)));
        }
        return reach;
    }

    private BDD ndet() {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getTransitions();
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
                        if (!getSolvingObject().getGame().isEnvironment(place)) {
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
        for (Transition t : getGame().getTransitions()) {
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
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) {
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

    @Override
    protected BDD calcBadDCSs() {
        return bad();
    }

    @Override
    protected BDD calcSpecialDCSs() {
        return getFactory().zero();
    }

    /**
     * Safety game graphs don't have a special state
     *
     * @param state
     * @return
     */
    @Override
    public boolean isSpecialState(BDD state) {
        return false;
    }
}
