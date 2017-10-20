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

    private void testJHH(String name, boolean hasStrat) throws Exception {
        final String path = inputDir + "toyexamples" + File.separator;
//        Logger.getInstance().setVerbose(true);

        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", false);
        BDDTools.saveGraph2PDF(outputDir + name + "_graphengame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, outputDir + name, hasStrat);
    }

    @Test(enabled = true)
    public void testOneTransitionBoth1() throws Exception {
        testJHH("oneTransitionBoth1", true);
    }  
    
    @Test(enabled = true)
    public void testOneTransitionSys3() throws Exception {
        testJHH("oneTransitionSys3", true);
    }
    
    @Test(enabled = true)
    public void testOneTransitionEnv1() throws Exception {
        testJHH("oneTransitionEnv1", false);
    }
}
