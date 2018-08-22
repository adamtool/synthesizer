package uniolunisaar.adam.symbolic.bddapproach.buchi;

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
public class TestingAllFilesABuchi {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallbuechi/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/forallbuechi/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
            "oneTokenMultiChains0.apt",
            "oneTokenMultiChains3.apt",
            "oneTokenMultiChains4.apt",
//            "oneTokenMultiChains5.apt", infinitely many token chains
//            "infiniteChains.apt", infinitely many token chains
            "oneTokenMultiChains6.apt",
            "infiniteChains1.apt",
            "type2_0.apt",
            "myexample1.apt",
            "myexample2.apt",
            "myexample11.apt",
            "myexample12.apt",
            "myexample21.apt"
//            "myexample22.apt" infinitely many token chains
    ));
    private static final List<String> skip = new ArrayList<>(Arrays.asList(
    ));
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList());

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
            CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
            Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
        } else {
            String output = outputDir + file.getName().split(".apt")[0];
            BDDTestingTools.testExample(solv, output, hasStrategy);
        }
    }
}
