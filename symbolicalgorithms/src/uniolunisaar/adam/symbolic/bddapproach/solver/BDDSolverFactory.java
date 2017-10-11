package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.logic.solver.SolverFactory;
import uniolunisaar.adam.ds.winningconditions.Buchi;
import uniolunisaar.adam.ds.winningconditions.Reachability;
import uniolunisaar.adam.ds.winningconditions.Safety;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverFactory extends SolverFactory<BDDSolverOptions, BDDSolver<? extends WinningCondition>> {

    private static BDDSolverFactory instance = null;

    public static BDDSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDSolverFactory();
        }
        return instance;
    }

    private BDDSolverFactory() {

    }

    public BDDSolver<? extends WinningCondition> getSolver(PetriNet net, boolean skipTests) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException {
        return super.getSolver(net, skipTests, new BDDSolverOptions());
    }

    public BDDSolver<? extends WinningCondition> getSolver(String file, boolean skipTests) throws ParseException, IOException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableWinningConditionException, ParameterMissingException {
        return super.getSolver(file, skipTests, new BDDSolverOptions());
    }

    public BDDSolver<? extends WinningCondition> getSolver(String file) throws ParseException, IOException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableWinningConditionException, ParameterMissingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

    @Override
    protected BDDASafetySolver getESafetySolver(PetriNet net, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDSolver<Safety> getASafetySolver(PetriNet pn, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDASafetySolver(pn, skipTests, new Safety(false), opts);
//        return new BDDASafetySolverNested(pn, skipTests, new Safety(false), opts);
    }

    @Override
    protected BDDSolver<Reachability> getEReachabilitySolver(PetriNet pn, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDEReachabilitySolver(pn, skipTests, new Reachability(true), opts);
    }

    @Override
    protected BDDSolver<? extends WinningCondition> getAReachabilitySolver(PetriNet net, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDAReachabilitySolver(net, skipTests, new Reachability(false), options);
    }

    @Override
    protected BDDSolver<Buchi> getEBuchiSolver(PetriNet pn, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDEBuechiSolver(pn, skipTests, new Buchi(true), opts);
    }

    @Override
    protected BDDSolver<? extends WinningCondition> getABuchiSolver(PetriNet net, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        return new BDDABuechiSolver(net, skipTests, new Buchi(false), options);
    }

}
