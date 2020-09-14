package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.buchi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
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
public class FirstTestsABuchi {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallbuechi/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/forallbuechi/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    private void testToyExamples(String name, boolean hasStrat) throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, FileNotFoundException, ModuleException, CalculationInterruptedException {
        final String path = inputDir + "toyexamples" + File.separator;
//        AdamTools.savePG2PDF(outputDir + name, Tools.getPetriNet(path + name + ".apt"), false);
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
//    
//     private void testExamples(String name, boolean hasStrat) throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        final String path = inputDir + File.separator;
//        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
////        System.out.println("ExStrat" + solv.existsWinningStrategy());
////        solv.getGraphStrategy();
////        BDDGraph g = BDDGraphBuilder.builtGraphStrategy(solv, 5);
////        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat_d5", g, solv);
////        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
////        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat", solv.getGraphStrategy(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
//    }
//

    @Test(enabled = true)
    public void testOneTokenMultiChains() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, FileNotFoundException, ModuleException, CalculationInterruptedException {
//        testToyExamples("oneTokenMultiChains0", false);
        try {
            testToyExamples("oneTokenMultiChains1", true); // need type2 strategy
        } catch (ParseException e) {
            System.out.println("eeeee" + e);
        }
//        testToyExamples("oneTokenMultiChains2", true);
//        testToyExamples("oneTokenMultiChains3", false);
//        testToyExamples("oneTokenMultiChains4", false);
//        testToyExamples("oneTokenMultiChains5", true); // infinite token chains
//        testToyExamples("oneTokenMultiChains6", false);
    }
//
//    @Test(enabled = true)
//    public void testToyExampleType2B() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("type2B", true);
//    }
//
//    @Test
//    public void testToyExampleFiniteA() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("finiteA", false); // should be false
//    }
//

    @Test
    public void testToyExampleInfiniteFlowChains() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, FileNotFoundException, ModuleException, CalculationInterruptedException {
//        testToyExamples("infiniteChains", true); // should be true
        testToyExamples("infiniteChains1", false);
    }

    @Test
    public void testType2() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, FileNotFoundException, ModuleException, CalculationInterruptedException {
        testToyExamples("type2_0", false); // should be false
        testToyExamples("type2_1", true); // should be true
        testToyExamples("type2_2", true); // should be true
        testToyExamples("type2_4", true); // should be true
    }

    @Test
    public void testMyExamples() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, FileNotFoundException, ModuleException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
//        AdamTools.savePG2PDF(outputDir + name, Tools.getPetriNet(path + name + ".apt"), false);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + "myexample12" + ".apt", opts);
//        System.out.println("ExStrat" + solv.existsWinningStrategy());
//        solv.getGraphStrategy();
//        BDDGraph g = BDDGraphBuilder.builtGraphStrategy(solv, 5);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat_d5", g, solv);
        BDDTools.saveGraph2PDF(outputDir + "myexample12" + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + "myexample12", false);
    }
//
//    @Test
//    public void testToyExampleDecInLoop() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("decInLoop", true); // should be true
//    }
//
//    @Test
//    public void testToyExampleOneGoodInfEnv() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("oneGoodInfEnv", false);
//    }
//
//    @Test
//    public void testToyExampleFirstExamplePaperBuchi() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("firstExamplePaperBuchi", true);
//    }
//
//    @Test
//    public void testToyExampleGoodBadLoop0() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("goodBadLoop0", true);
//    }
//
//    @Test
//    public void testToyExampleGoodBadLoop1() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("goodBadLoop1", true);
//    }
//
//    @Test
//    public void testToyExampleGoodBadLoop2() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("goodBadLoop2", true);
//    }
//
//    @Test
//    public void testToyExampleIndependentLoops() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testToyExamples("independentloops", true);
//    }
//    
//    @Test
//    public void testMutex() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
//        testExamples("mutex", true);
//    }
}
