package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.safety;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a safety objective with BDDs. This solver can be used
 * when there is no possibillity for system player to play infinitely long
 * without any further interaction with the environment.
 *
 * @author Manuel Gieseking
 */
public class DistrSysBDDASafetyWithoutType2Solver extends DistrSysBDDSolver<Safety> {

    /**
     * Creates a new Safety solver for a given game.
     *
     * @param game - the Petri game to solve.
     * @param skipTests - should the tests for safe and bounded and other
     * preconditions be skipped?
     * @param opts - the options for the solver.
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    DistrSysBDDASafetyWithoutType2Solver(DistrSysBDDSolvingObject<Safety> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, opts);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Calculates a BDD with all possible situations containing a bad place.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing situations with bad places
     */
    private BDD baddcs(int pos) {
        BDD bad = getZero();
        for (Place place : getSolvingObject().getWinCon().getBadPlaces()) {
            bad.orWith(codePlace(place, pos, getSolvingObject().getGame().getPartition(place)));
        }
        return bad;
    }

    /**
     * Calculates a BDD representing all decision sets where the system decided
     * not to choose any enabled transition, but there exists at least one.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing the deadlocks of the Petri game.
     */
    private BDD deadSysDCS(int pos) {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, pos).not());
            buf.orWith(enabled(t, pos));
        }
        dead.andWith(buf);
        return dead.andWith(getTop().not());//.andWith(wellformed());
    }

    /**
     * Calculates a BDD with all possible bad situations:
     *
     * 1) bad place reached 2) non determinism encountered 3) deadlock created
     *
     * @return BDD representing all bad situations
     */
    public BDD badSysDCS() {
//        System.out.println("bad");
//        BDDTools.printDecisionSets(baddcs(0).not(),  true);
//        System.out.println("end");
//        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
    }

    public BDD badStates() {
        return badSysDCS();
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

    @Override
    protected BDD envTransitionCP(Transition t) {
        return super.envTransitionCP(t).andWith(badStates().not()); // bad states don't have any successor
    }

    @Override
    protected BDD envTransitionNotCP(Transition t) {
        return super.envTransitionNotCP(t).andWith(badStates().not()); // bad states don't have any successors
    }

    @Override
    protected BDD sysTransitionCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut        
        BDD sys1 = super.sysTransitionCP(t);
        // bad states don't have succesors
        sys1.andWith(badStates().not());
//        sys1.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
        return sys1;//.andWith(wellformed(1));//.andWith(wellformedTransition()));
    }

    @Override
    protected BDD sysTransitionNotCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut        
        BDD sys1 = super.sysTransitionNotCP(t);
        // bad states don't have succesors
        sys1.andWith(badStates().not());
//        sys1.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
        return sys1;//.andWith(wellformed(1));//.andWith(wellformedTransition()));
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = attractor(badStates(), true, distance).not().and(getBufferedDCSs());//fixpointOuter();
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()).andWith(getNotTop()), this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%
    @Override
    protected BDD calcBadDCSs() {
        return badStates();
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
