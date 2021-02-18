package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.mcutscheduling.safe.reachability;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.BDDTestingTools;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class FirstTestsAReachability {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallreachability/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/forallreachability/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    private void testToyExamples(String name, boolean hasStrat) throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "toyexamples" + File.separator;
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        Transition t = solv.getNet().getTransition("t1");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    @Test
    public void testWinInit() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testToyExamples("winInit", true);
    }

    @Test
    public void testChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testToyExamples("chains", true);
        testToyExamples("chains0", false);
        testToyExamples("chains1", false);
    }

    @Test
    public void testOneTokenMultiChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        testToyExamples("oneTokenMultiChains3", true);
        testToyExamples("oneTokenMultiChains5", false);
//        testToyExamples("oneTokenMultiChains6", false);
//        testToyExamples("oneTokenMultiChains8", true);
    }

    @Test
    public void testType2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testToyExamples("type2", true);
    }

    @Test
    public void testOverallBad() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testToyExamples("overallBad0", false);
    }

    @Test
    public void testInfiniteFlowChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testToyExamples("infiniteFlowChains", true);
    }

    @Test
    public void testUnfair() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "unfair";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testMyExample2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexample2WithEnv";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);

        Transition t = solv.getGame().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testNoEnv() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexample2";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);

//        Transition t = solv.getNet().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testMyExampleNoStrat() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexampleWithSysNoStrat";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);

//        Transition t = solv.getNet().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testBurglar() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "burglar" + File.separator;
//        final String path = inputDir + "toyexamples" + File.separator;
//        String name = "chains";
        String name = "burglar";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);

//        System.out.println(AdamExtensions.getTokenChains(solv.getNet()));
//        System.out.println(solv.getNet().getName());
        Transition t = solv.getGame().getTransition("t4");
//        System.out.println(solv.getGame().getTokenFlow(t).toString());
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test
    public void testBurglar2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "burglar" + File.separator;
//        final String path = inputDir + "toyexamples" + File.separator;
//        String name = "chains";
        String name = "burglar2";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);

//        System.out.println(AdamExtensions.getTokenChains(solv.getNet()));
//        System.out.println(solv.getNet().getName());
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

}
