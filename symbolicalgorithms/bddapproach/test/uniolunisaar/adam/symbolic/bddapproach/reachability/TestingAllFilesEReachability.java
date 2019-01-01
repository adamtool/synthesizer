package uniolunisaar.adam.symbolic.bddapproach.reachability;

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
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFilesEReachability {

    private static final String inputDir = System.getProperty("examplesfolder") + "/reachability/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/reachability/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
            "infiniteB.apt",
            "infiniteC.apt",
            "simple.apt",
            "nondetNoStrat.apt",
            "nondet_s3_noStrat.apt",
            "notReachable.apt",
            "unfair.apt",
            "nondetNoStrat",// should have no strategy, builds one voilating S3
            "nondet2.apt",// should have no strategy, builds one voilating S3,
            "twoDecisions1.apt",
            "unfairEnv.apt",
            "runaway.apt"
    ));
    private static final List<String> skip = new ArrayList<>(Arrays.asList(
            "unfair2.apt" // has two env token
    )
    );
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList(
            "nondet.apt", // should have a strategy
            "nondetWithBack.apt", // should have a strategy
            "nondetNoStrat.apt",// should have no strategy, builds one voilating S3
            "nondet2.apt"// should have no strategy, builds one voilating S3
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
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, SolvingException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NotSupportedGameException, ParameterMissingException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(file.getAbsolutePath(), true);
        if (notSupported.contains(file.getName())) {
            CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
            Assert.assertTrue(PNWTTools.isSolvablePetriGame(solv.getGame(), cover) != null, "Petri game not solvable: ");
        } else {
            String output = outputDir + file.getName().split(".apt")[0];
            BDDTestingTools.testExample(solv, output, hasStrategy);
        }
    }
}
