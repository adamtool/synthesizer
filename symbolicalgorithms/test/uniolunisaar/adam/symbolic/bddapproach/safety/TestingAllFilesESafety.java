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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.util.AdamTools;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFilesESafety {

    private static final String inputDir = System.getProperty("examplesfolder") + "/existssafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/existssafety/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
           ));
    private static final List<String> skip = new ArrayList<>(//Arrays.asList(
//    )
            );
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList(
//            "nondet2WithStratByGameSolving.apt", // should have a strategy
//            "missDeadlock.apt", // should have a strategy
//            "nondet_withBad.apt", // should have no strategy, builds one voilating S3
//            "nondet2WithSys.apt", // should have no strategy, builds one violating S3
//            "nondet2.apt", // should have no strategy, builds one violating S3
//            "nondet.apt" // should have no strategy, builds one voilating S3
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
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NotSupportedGameException, ParameterMissingException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(file.getAbsolutePath(), true);
        if (notSupported.contains(file.getName())) {
            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
            Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
        } else {
            String output = outputDir + file.getName().split(".apt")[0];
            BDDTestingTools.testExample(solv, output, hasStrategy);
        }
    }
}
