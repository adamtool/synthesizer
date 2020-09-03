package uniolunisaar.adam.symbolic.bddapproach.safety;

import java.io.File;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingSomeFilesESafety {

    private static final String inputDir = System.getProperty("examplesfolder") + "/existssafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/existssafety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    private void testToy(String name, boolean hasStrat) throws Exception {
        final String path = inputDir + "toyexamples" + File.separator;
//        Logger.getInstance().setVerbose(true);

        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDGraph graph = solv.getGraphStrategy();
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg", graph, solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    @Test(enabled = true)
    public void testOneTransitionBoth1() throws Exception {
        testToy("oneTransitionBoth1", true);
    }

    @Test(enabled = true)
    public void testOneTransitionSys3() throws Exception {
        testToy("oneTransitionSys3", true);
    }

    @Test(enabled = true)
    public void testOneTransitionEnv1() throws Exception {
        testToy("oneTransitionEnv1", false);
    }

    @Test(enabled = true)
    public void testDecision1() throws Exception {
        testToy("decision1", false);
    }

    @Test(enabled = true)
    public void testInfiniteBad() throws Exception {
        testToy("infiniteBad", false);
    }

    @Test(enabled = true)
    public void testInfiniteBadWithEscape() throws Exception {
//        testToy("infiniteBadWithEscape", true);
        testToy("infiniteBadWithEscape2", true);
    }

    @Test(enabled = true)
    public void testInfOneGoodOneBad() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchainsOneGoodOneBad";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testInfFlowChains4() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains4";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testInfFlowChains2() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains2";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testInfFlowChains5() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains5";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, true);
    }

    @Test(enabled = true)
    public void testInfFlowChains6() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains6";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testInfFlowChainsEnv() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains_env_0";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testInfFlowChains() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testNewChains() throws Exception {
        final String path = inputDir + "newchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
//        String name = "newchainForget"; // true
        String name = "newchainForget_1"; // false
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, outputDir + name, true);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testEscape11() throws Exception {
        final String path = inputDir + "escape" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "escape11";
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }
}
