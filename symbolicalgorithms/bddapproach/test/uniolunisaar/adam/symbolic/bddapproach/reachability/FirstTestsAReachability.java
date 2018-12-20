package uniolunisaar.adam.symbolic.bddapproach.reachability;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.logic.exceptions.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.exceptions.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
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
        (new File(outputDir)).mkdirs();
    }

    private void testToyExamples(String name, boolean hasStrat) throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "toyexamples" + File.separator;
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        Transition t = solv.getNet().getTransition("t1");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    @Test
    public void testWinInit() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        testToyExamples("winInit", true);
    }

    @Test
    public void testChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        testToyExamples("chains", true);
        testToyExamples("chains0", false);
        testToyExamples("chains1", false);
    }
    
      @Test
    public void testOneTokenMultiChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
//        testToyExamples("oneTokenMultiChains3", true);
        testToyExamples("oneTokenMultiChains5", false);
//        testToyExamples("oneTokenMultiChains6", false);
//        testToyExamples("oneTokenMultiChains8", true);
    }

    @Test
    public void testType2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        testToyExamples("type2", true);
    }
    
    @Test
    public void testOverallBad() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        testToyExamples("overallBad0", false);
    }
        
    @Test
    public void testInfiniteFlowChains() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        testToyExamples("infiniteFlowChains", true);
    }
    
    @Test
    public void testUnfair() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "unfair";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test
    public void testMyExample2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexample2WithEnv";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
        
        Transition t = solv.getGame().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }


    @Test
    public void testNoEnv() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexample2";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
        
//        Transition t = solv.getNet().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }    
    
    @Test
    public void testMyExampleNoStrat() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "jhh" + File.separator;
        String name = "myexampleWithSysNoStrat";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
        
//        Transition t = solv.getNet().getTransition("t2");
//        System.out.println(AdamExtensions.getTokenFlow(t).toString());
//        System.out.println(AdamExtensions.getTokenTrees(solv.getNet()));
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }
    
    
    @Test
    public void testBurglar() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "burglar" + File.separator;
//        final String path = inputDir + "toyexamples" + File.separator;
//        String name = "chains";
        String name = "burglar";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        
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
    public void testBurglar2() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException, SolvingException {
        final String path = inputDir + "burglar" + File.separator;
//        final String path = inputDir + "toyexamples" + File.separator;
//        String name = "chains";
        String name = "burglar2";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        
//        System.out.println(AdamExtensions.getTokenChains(solv.getNet()));
//        System.out.println(solv.getNet().getName());
        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

}
