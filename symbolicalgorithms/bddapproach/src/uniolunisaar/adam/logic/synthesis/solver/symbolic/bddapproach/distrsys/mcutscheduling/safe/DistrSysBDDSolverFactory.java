package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe;

import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.reach.DistrSysBDDEReachabilitySolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.buchi.DistrSysBDDEBuechiSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.reach.DistrSysBDDAReachabilitySolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.buchi.DistrSysBDDABuechiSolver;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.io.IOException;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.ds.objectives.local.Buchi;
import uniolunisaar.adam.ds.objectives.local.Reachability;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.synthesis.solver.LLSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.buchi.DistrSysBDDBuchiSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.reach.DistrSysBDDReachabilitySolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety.DistrSysBDDSafetyLocNDetSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety.separatetype2.DistrSysBDDSafetySolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class DistrSysBDDSolverFactory extends LLSolverFactory<BDDSolverOptions, DistrSysBDDSolver<? extends Condition<?>>> {

    private static DistrSysBDDSolverFactory instance = null;

    public static DistrSysBDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new DistrSysBDDSolverFactory();
        }
        return instance;
    }

    private DistrSysBDDSolverFactory() {

    }

    public DistrSysBDDSolver<? extends Condition<?>> getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

//    public Solver<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ? extends SolvingObject<PetriGame, ? extends Condition<?>, ?>>, BDDSolverOptions> getSolver(PetriGame game) throws CouldNotFindSuitableConditionException, SolvingException {
    public DistrSysBDDSolver<? extends Condition<?>> getSolver(PetriGameWithTransits game) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, new BDDSolverOptions());
    }

    @Override
    protected <W extends Condition<W>> DistrSysBDDSolvingObject<W> createSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException {
        try {
            return new DistrSysBDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException | InvalidPartitionException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected DistrSysBDDSolver<Safety> getESafetySolver(PetriGameWithTransits game, Safety con, BDDSolverOptions options) throws SolvingException, NoSuitableDistributionFoundException, NotSupportedGameException, InvalidPartitionException {
        try {
// if it creates a new token chain, use the co-Buchi solver
            DistrSysBDDSolvingObject<Safety> so = createSolvingObject(game, con);
            for (Transition t : so.getGame().getTransitions()) {
                for (Transit tfl : so.getGame().getTransits(t)) {
                    if (tfl.isInitial()) {
                        return DistrSysBDDSafetySolverFactory.getInstance().createDistrSysBDDESafetyWithNewChainsSolver(so, options);
                    }
                }
            }
            return DistrSysBDDSafetySolverFactory.getInstance().createDistrSysBDDESafetySolver(so, options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected DistrSysBDDSolver<Safety> getASafetySolver(PetriGameWithTransits game, Safety con, BDDSolverOptions opts) throws SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            DistrSysBDDSolvingObject<Safety> so = createSolvingObject(game, con);
            if (opts.isNoType2()) {
                return DistrSysBDDSafetySolverFactory.getInstance().createDistrSysBDDASafetyWithoutType2Solver(so, opts);
            } else if (opts.isWithLocNDet()) {
                return DistrSysBDDSafetyLocNDetSolverFactory.getInstance().createDistrSysBDDASafetySolver(so, opts);
            } else {
                return DistrSysBDDSafetySolverFactory.getInstance().createDistrSysBDDASafetySolver(so, opts);
            }
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected DistrSysBDDEReachabilitySolver getEReachabilitySolver(PetriGameWithTransits game, Reachability con, BDDSolverOptions opts) throws SolvingException {
        try {
            DistrSysBDDSolvingObject<Reachability> so = createSolvingObject(game, con);
            return DistrSysBDDReachabilitySolverFactory.getInstance().createDistrSysBDDEReachabilitySolver(so, opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected DistrSysBDDAReachabilitySolver getAReachabilitySolver(PetriGameWithTransits game, Reachability con, BDDSolverOptions options) throws SolvingException {
        try {
            return DistrSysBDDReachabilitySolverFactory.getInstance().createDistrSysBDDAReachabilitySolver(createSolvingObject(game, con), options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected DistrSysBDDEBuechiSolver getEBuchiSolver(PetriGameWithTransits game, Buchi con, BDDSolverOptions opts) throws SolvingException {
        try {
            return DistrSysBDDBuchiSolverFactory.getInstance().createDistrSysBDDEBuechiSolver(createSolvingObject(game, con), opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }

    @Override
    protected DistrSysBDDABuechiSolver getABuchiSolver(PetriGameWithTransits game, Buchi con, BDDSolverOptions options) throws SolvingException {
        try {
            return DistrSysBDDBuchiSolverFactory.getInstance().createDistrSysBDDABuechiSolver(createSolvingObject(game, con), options);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException(ex);
        }
    }
}
