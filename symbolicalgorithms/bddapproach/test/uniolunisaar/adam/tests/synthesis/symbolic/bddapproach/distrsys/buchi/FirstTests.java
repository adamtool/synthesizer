package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.buchi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.BDDTestingTools;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class FirstTests {

    private static final String inputDir = System.getProperty("examplesfolder") + "/existsbuechi/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/buechi/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    private void testToyExamples(String name, boolean hasStrat) throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + "toyExamples" + File.separator;
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        System.out.println("ExStrat" + solv.existsWinningStrategy());
//        solv.getGraphStrategy();
//        BDDGraph g = BDDGraphBuilder.builtGraphStrategy(solv, 5);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat_d5", g, solv);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    private void testExamples(String name, boolean hasStrat) throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        final String path = inputDir + File.separator;
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        System.out.println("ExStrat" + solv.existsWinningStrategy());
//        solv.getGraphStrategy();
//        BDDGraph g = BDDGraphBuilder.builtGraphStrategy(solv, 5);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat_d5", g, solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    @Test(enabled = true)
    public void testToyExampleType2A() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("type2A", true);
    }

    @Test(enabled = true)
    public void testToyExampleType2B() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("type2B", true);
    }

    @Test
    public void testToyExampleFiniteA() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("finiteA", false); // should be false
    }

    @Test
    public void testToyExampleInfiniteA() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("infiniteA", true); // should be true
    }

    @Test
    public void testToyExampleInfiniteB() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("infiniteB", false);
    }

    @Test
    public void testToyExampleDecInLoop() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("decInLoop", true); // should be true
    }

    @Test
    public void testToyExampleOneGoodInfEnv() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("oneGoodInfEnv", false);
    }

    @Test
    public void testToyExampleFirstExamplePaperBuchi() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("firstExamplePaperBuchi", true);
    }

    @Test
    public void testToyExampleGoodBadLoop0() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("goodBadLoop0", true);
    }

    @Test
    public void testToyExampleGoodBadLoop1() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("goodBadLoop1", true);
    }

    @Test
    public void testToyExampleGoodBadLoop2() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("goodBadLoop2", true);
    }

    @Test
    public void testToyExampleIndependentLoops() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testToyExamples("independentloops", true);
    }

    @Test
    public void testMutex() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, CalculationInterruptedException {
        testExamples("mutex", true);
    }
}
