package uniolunisaar.adam.symbolic.bddapproach.safety;

import java.io.File;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
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
public class TestingSomeFilesESafety {

    private static final String inputDir = System.getProperty("examplesfolder") + "/existssafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/existssafety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    private void testToy(String name, boolean hasStrat) throws Exception {
        final String path = inputDir + "toyexamples" + File.separator;
//        Logger.getInstance().setVerbose(true);

        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
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
        testToy("infiniteBadWithEscape", true);
    }

    @Test(enabled = true)
    public void testInfFlowChains() throws Exception {
        final String path = inputDir + "infflowchains" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "infflowchains";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }

    @Test(enabled = true)
    public void testEscape11() throws Exception {
        final String path = inputDir + "escape" + File.separator;
//        Logger.getInstance().setVerbose(true);
        String name = "escape11";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, false);
    }
}
