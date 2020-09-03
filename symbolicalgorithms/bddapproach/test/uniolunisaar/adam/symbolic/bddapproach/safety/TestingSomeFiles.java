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
import uniolunisaar.adam.exceptions.pg.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.logic.pg.calculators.ConcurrencyPreservingGamesCalculator;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.pg.ExtensionCalculator;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingSomeFiles {

    private static final String inputDir = System.getProperty("examplesfolder") + "/synthesis/forallsafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testFirstExamplePaper() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testBurglar() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testAbb62() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "ma_vsp" + File.separator;
        final String name = "abb62";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testVSPWithBad() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "ma_vsp" + File.separator;
        final String name = "vsp_1_withBadPlaces";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTools.saveGraph2PDF(outputDir + name + "graph_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testAdamMachines() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "vsp" + File.separator;
        final String name = "vsp__adam_machines";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir+name+"graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testInfiniteSys() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "testingNets" + File.separator;
        final String name = "infiniteSystemTrysToAvoidEnvUseBadPlace";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false); // todo must be false
    }

    @Test
    public void testConstructedExample() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "constructedExample" + File.separator;
        final String name = "constructedExample";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testType1Type2Mutex() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "olderog" + File.separator + "type1Type2Mutex" + File.separator;
        final String name = "net";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testWatchdog5() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "tests" + File.separator;
        final String name = "watchdog5";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

//    @Test
//    public void testNdet() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
//        final String path = inputDir + "ndet" + File.separator;
//        final String name = "nondet2";
//        DistrSysBDDSolver<? extends Condition<?>> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
//        Assert.assertTrue(PNWTTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
////        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
////        BDDTestingTools.testExample(solv, outputDir + name, false);
//    }
    @Test
    public void testNdet2WithSys() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2WithSys";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(PGTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testNdetS3() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet_s3_noStrat";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false); // todo: should be false
    }

    @Test
    public void testNdetProbS3() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet_withBad";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(PGTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
//        PetriNet strat = solv.getStrategy();
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strat);
//        Assert.assertTrue(PNWTTools.isDeterministic(strat, cover));
//        Assert.assertTrue(PNWTTools.isDeadlockAvoiding(solv.getNet(), strat, cover));
//        BDDTestingTools.testExample(solv, outputDir + name, false); // todo: should be false
    }

    @Test
    public void testNondet2WithStratByGameSolving() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2WithStratByGameSolving";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(PGTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
//        PetriNet strat = solv.getStrategy();
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphenGame", solv.getGraphGame(), solv);
//        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strat);
//        Assert.assertTrue(PNWTTools.isDeterministic(strat, cover));
//        Assert.assertTrue(PNWTTools.isDeadlockAvoiding(solv.getNet(), strat, cover));
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testDeadlock() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + "deadlock" + File.separator;
        final String name = "missDeadlock";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        Assert.assertTrue(PGTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
//        BDDTools.saveGraph2PDF(outputDir + name + "garaphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = false) // deleted?
    public void testJHHRobots() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
        final String name = "robots_annotated";
//        Logger.getInstance().setVerbose(true);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir+name+"garaphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testJournal() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "tests" + File.separator;
        final String name = "journalReview2";
//        Logger.getInstance().setVerbose(true);
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true); // should only have a strategy for the journal version of the ndet
        BDDTestingTools.testExample(solv, outputDir + name, false);
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    }

    @Test(enabled = true)
    public void testNondet2SysPlace() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondet2SysPlace";
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true); // should only have a strategy for the journal version of the ndet
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testNondetNetvsUnfolding() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "ndet" + File.separator;
        final String name = "nondetNetvsUnfolding";
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true); // should only have a strategy for the journal version of the ndet
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testNotCP() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "notConcurrencyPreservingTests" + File.separator;
        final String name = "ncp0";
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testForbiddingTransitionOnce() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "toyexamples" + File.separator;
        final String name = "forbiddingTransitionOnce";
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testNoSysPlaces() throws Exception {
        final String path = inputDir + "jhh" + File.separator;
        final String name = "myexample0";
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testStratNoInitSysPlace() throws Exception {
        final String path = inputDir + "nm" + File.separator;
        final String name = "minimal";
//        final String name = "minimalOnlySys";
//        final String name = "minimalNotFinishingEnv";
//        final String name = "minimalNonCP";
//        Logger.getInstance().setVerbose(true);

        // this doesn't work since the partitioning is done during the creation 
        // of the solver and this is already dependent on cp
//        DistrSysBDDSolver<? extends Condition<?>> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        PetriGame game = PGTools.getPetriGame(path + name + ".apt", false, true);
        ExtensionCalculator<Boolean> calc = new ConcurrencyPreservingGamesCalculator();
        ExtensionCalculator<?> c = game.addExtensionCalculator(calc.getKey(), calc);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition<?>> solv = DistrSysBDDSolverFactory.getInstance().getSolver(game, opts);

        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testUnreachableEnv() throws Exception {
        final String path = inputDir + "cornercases" + File.separator;
        final String name = "unreachableEnvTransition";
        // this has a problem with the partitioning
        // because there is a transition with two env places in the preset (but this transition is not reachable)
        // could either delete unreachable transitions (expensive) or more expensivly 
        // do the partitioning check and the coding of the partitions
//        Logger.getInstance().setVerbose(true);
//
//        DistrSysBDDSolver<? extends Condition<?>> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        System.out.println(outputDir + name);
//        PGTools.savePG2PDF(outputDir + name, solv.getGame(), false);
////        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, false);
    }
}
