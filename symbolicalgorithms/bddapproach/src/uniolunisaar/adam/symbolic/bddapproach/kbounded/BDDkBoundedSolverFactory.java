package uniolunisaar.adam.symbolic.bddapproach.kbounded;

import uniolunisaar.adam.symbolic.bddapproach.solver.*;
import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.solver.SolverFactory;
import uniolunisaar.adam.ds.petrinet.objectives.Buchi;
import uniolunisaar.adam.ds.petrinet.objectives.Reachability;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDkBoundedSolverFactory extends SolverFactory<BDDSolverOptions, BDDkBoundedSolver> {

    private static BDDkBoundedSolverFactory instance = null;

    public static BDDkBoundedSolverFactory getInstance() {
        if (instance == null) {
            instance = new BDDkBoundedSolverFactory();
        }
        return instance;
    }

    private BDDkBoundedSolverFactory() {

    }

    public BDDkBoundedSolver getSolver(String file) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, new BDDSolverOptions());
    }

    public BDDkBoundedSolver getSolver(String file, boolean skipTests) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(file, skipTests, new BDDSolverOptions());
    }

    public BDDkBoundedSolver getSolver(PetriGame game, boolean skipTests) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, skipTests, new BDDSolverOptions());
    }

    @Override
    protected BDDkBoundedSolver getESafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getASafetySolver(PetriGame game, Safety winCon, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        return new BDDkBoundedSolver(game, skipTests, winCon, opts);
//        return new BDDASafetySolverNested(pn, skipTests, winCon, opts);
    }

    @Override
    protected BDDkBoundedSolver getEReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getAReachabilitySolver(PetriGame game, Reachability winCon, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getEBuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, BDDSolverOptions opts) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected BDDkBoundedSolver getABuchiSolver(PetriGame game, Buchi winCon, boolean skipTests, BDDSolverOptions options) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
