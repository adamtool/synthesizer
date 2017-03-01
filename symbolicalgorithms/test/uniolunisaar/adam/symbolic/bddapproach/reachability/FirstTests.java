package uniolunisaar.adam.symbolic.bddapproach.reachability;

import java.io.File;
import java.io.IOException;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.symbolic.bddapproach.TestingTools;
import uniolunisaar.adam.ds.exceptions.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class FirstTests {

    private static final String inputDir = "../examples/safety/";

    @Test
    public void testFirstExamplePaper() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        //BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testBurglar() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);
    }
    
    
    @Test
    public void testNdet() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testToyExamples() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "toyExamples" + File.separator;
        String name = "type2A";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);

        name = "type2B";
        solv = SolverFactory.getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);

        name = "infiniteA";
        solv = SolverFactory.getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, true);

        name = "infiniteB";
        solv = SolverFactory.getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, false);

        name = "notReachable";
        solv = SolverFactory.getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        TestingTools.testExample(solv, path + name, false);
    }

}
