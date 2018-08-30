package uniolunisaar.adam.symbolic.bddapproach.safety;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.exceptions.SolvingException;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFilesASafety {

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
            "abb62.apt",
            "lateSameDecision.apt",
            "tafel.apt",
            "tafel_3.apt",
            "vsp__adam_machines.apt",
            "infiniteSystemTrysToAvoidEnvUseBadPlace.apt",
            "nondet.apt",
            "nondet_withBad.apt", // has a strategy since nondet is overseen
            "nondet2WithSys.apt", // has a strategy since nondet is overseen
            "nondet_s3_noStrat.apt",
            "nondet_unnecessarily_noStrat.apt",
            "firstExamplePaper_extended.apt",
            "envSkipsSys.apt",
            "robots_false.apt",
            "myexample4.apt",
            "nondet_withBad.apt",
            "nondet2WithSys.apt",
            "nondet2.apt",
            "nondet_jhh1.apt",
            "nondet_jhh3.apt",
            "nondet2SelectionToken.apt",
            "robinhood.apt",
            "nondet2SysAtStart.apt", // should have a strategy for the original definition of ndet
            "nondet2WithStratByGameSolving.apt" // should have a strategy for the original definition of ndet
    ));
    private static final List<String> skip = new ArrayList<>(Arrays.asList(
            "container.apt", // takes to long ... 
            "container_withoutAnnotation.apt", // takes to long
            "myexample1.apt", // no token annotation given and not able to do it on its own
            "myexample2.apt", // no token annotation given and not able to do it on its own
            "myexample7.apt", // has two environment token
            "wf_2_3_pg_reversible.apt", // currently unbounded
            "sendingprotocolTwo.apt",// two environment token
            "toMakeCP.apt",// two environment token
            "madeCP.apt",// two environment token
            "nondet_motivationForSchedulingChange.apt",// two environment token
            "burglar-HL.apt",// high-level version
            "firstTest.apt" // not safe 
    ));
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList( //            "nondet2WithStratByGameSolving.apt", // should have a strategy
            //            "missDeadlock.apt", // should have a strategy
            //            "nondet_withBad.apt", // should have no strategy, builds one voilating S3
            //            "nondet2WithSys.apt", // should have no strategy, builds one violating S3
            //            "nondet2.apt", // should have no strategy, builds one violating S3
            //            "nondet.apt", // should have no strategy, builds one voilating S3
            //            "nondet_jhh1.apt", // should have no strategy, and also detects this
            //            "nondet_jhh2.apt", // should have no strategy, and also detects this
            //            "nondet_jhh3.apt" // should have no strategy, builds one violating S3
            ));

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    @DataProvider(name = "files")
    public static Object[][] allExamples() {
        Collection<File> files = FileUtils.listFiles(
                new File(inputDir),
                new RegexFileFilter(".*\\.apt"),
                DirectoryFileFilter.DIRECTORY);
        Object[][] out = new Object[files.size() - skip.size()][2];
        int i = 0;
        for (File file : files) {
            if (!skip.contains(file.getName())) {
                out[i][0] = file;
                out[i][1] = !(withoutStrategy.contains(file.getName()));
                i++;
            }
        }
        return out;
    }

    @Test(dataProvider = "files")
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, SolvingException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NotSupportedGameException, ParameterMissingException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(file.getAbsolutePath(), true);
//        if (notSupported.contains(file.getName())) {
//            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
//            Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
//        } else {
            String output = outputDir + file.getName().split(".apt")[0];
            BDDTestingTools.testExample(solv, output, hasStrategy);
//        }
    }
}
