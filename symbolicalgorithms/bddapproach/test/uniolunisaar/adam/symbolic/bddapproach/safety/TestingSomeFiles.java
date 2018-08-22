package uniolunisaar.adam.symbolic.bddapproach.safety;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.util.AdamTools;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingSomeFiles {

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testFirstExamplePaper() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException, ParameterMissingException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testBurglar() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testAbb62() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ma_vsp" + File.separator;
        final String name = "abb62";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testVSPWithBad() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ma_vsp" + File.separator;
        final String name = "vsp_1_withBadPlaces";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTools.saveGraph2PDF(outputDir + name + "graph_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testAdamMachines() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "vsp" + File.separator;
        final String name = "vsp__adam_machines";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testInfiniteSys() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "testingNets" + File.separator;
        final String name = "infiniteSystemTrysToAvoidEnvUseBadPlace";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false); // todo must be false
    }

    @Test
    public void testConstructedExample() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "constructedExample" + File.separator;
        final String name = "constructedExample";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testType1Type2Mutex() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "olderog" + File.separator + "type1Type2Mutex" + File.separator;
        final String name = "net";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testWatchdog5() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "tests" + File.separator;
        final String name = "watchdog5";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

//    @Test
//    public void testNdet() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
//        final String path = inputDir + "ndet" + File.separator;
//        final String name = "nondet2";
//        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
//        Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
////        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
////        BDDTestingTools.testExample(solv, outputDir + name, false);
//    }

    @Test
    public void testNdet2WithSys() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2WithSys";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testNdetS3() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet_s3_noStrat";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false); // todo: should be false
    }

    @Test
    public void testNdetProbS3() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet_withBad";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
//        PetriNet strat = solv.getStrategy();
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strat);
//        Assert.assertTrue(AdamTools.isDeterministic(strat, cover));
//        Assert.assertTrue(AdamTools.isDeadlockAvoiding(solv.getNet(), strat, cover));
//        BDDTestingTools.testExample(solv, outputDir + name, false); // todo: should be false
    }

    @Test
    public void testNondet2WithStratByGameSolving() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2WithStratByGameSolving";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
//        PetriNet strat = solv.getStrategy();
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphenGame", solv.getGraphGame(), solv);
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strat);
//        Assert.assertTrue(AdamTools.isDeterministic(strat, cover));
//        Assert.assertTrue(AdamTools.isDeadlockAvoiding(solv.getNet(), strat, cover));
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testDeadlock() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "deadlock" + File.separator;
        final String name = "missDeadlock";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = false) // deleted?
    public void testJHHRobots() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "jhh" + File.separator;
        final String name = "robots_annotated";
//        Logger.getInstance().setVerbose(true);
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testJournal() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "tests" + File.separator;
        final String name = "journalReview2";
//        Logger.getInstance().setVerbose(true);

        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }
    
    
    @Test(enabled = true)
    public void testNotCP() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "notConcurrencyPreservingTests" + File.separator;
        final String name = "ncp0";
//        Logger.getInstance().setVerbose(true);

        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }
    
    @Test(enabled = true)
    public void testForbiddingTransitionOnce() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "toyexamples" + File.separator;
        final String name = "forbiddingTransitionOnce";
//        Logger.getInstance().setVerbose(true);

        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }
}
