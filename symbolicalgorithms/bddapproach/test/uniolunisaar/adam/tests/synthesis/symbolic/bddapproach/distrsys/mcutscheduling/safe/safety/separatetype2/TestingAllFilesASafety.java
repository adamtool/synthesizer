package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety.separatetype2;

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
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.BDDTestingTools;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFilesASafety {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
            "abb62.apt",
            "lateSameDecision.apt",
            "tafel.apt",
            "tafel_3.apt",
            "vsp__adam_machines.apt",
            "infiniteSystemTrysToAvoidEnvUseBadPlace.apt",
            "nondet.apt",
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
            "myexample0.apt",
            "myexample00.apt",
            "myexample000.apt",
            "minimalOnlySys.apt",
            "minimalNotFinishingEnv.apt",
            "minimalNonCP.apt",
            "ndetConcurrent2.apt",
            "deadlockAvoidance3.apt",
            "deadlockAvoidance5.apt",
            // %%%% Examples which should have a strategy for the journal version of the nondeterminism
            "journalReview2.apt", // should only have a strategy for the journal version of the ndet
            "nondet_s3.apt", // should only have a strategy for the journal version of the ndet
            "nondet_withBad.apt", // has a strategy for the journal version of ndet because nondet is overseen
            "nondet2WithSys.apt", // has a strategy for the journal version of ndet because nondet is overseen
            "nondet2SysAtStart.apt", // should have a strategy for the original definition of ndet
            "nondet2WithStratByGameSolving.apt", // should have a strategy for the original definition of ndet
            "nondet2SysPlace.apt" // should have a strategy for the original definition of ndet
    ));
    private static final List<String> skip = new ArrayList<>(Arrays.asList(
            // %%%% Examples skipped for saving time
            "container.apt", // takes to long ... 
            "container_withoutAnnotation.apt", // takes to long
            // %%%% Missing partitioning of the places regarding their occupation by tokesn
            "myexample1.apt", // no token annotation given and not able to do it on its own
            "myexample2.apt", // no token annotation given and not able to do it on its own
            // %%%% Not safe nets
            "wf_2_3_pg_reversible.apt", // currently unbounded
            "firstTest.apt", // not safe            
            // %%%% High-level versions            
            "burglar-HL.apt",// high-level version
            // %%%% Not parsable by APT            
            //            "DR2-2.apt", // error in the naming of the transitions (not existent anymore?)
            // %%%% More than one environment player
            "myexample7.apt", // has two environment token
            "sendingprotocolTwo.apt",// two environment token
            "toMakeCP.apt",// two environment token
            "madeCP.apt",// two environment token
            "paul.apt",// two environment token
            "nondet_motivationForSchedulingChange.apt",// two environment token
            "nounfolding.apt", // two environment token
            "oneunfolding.apt", // two environment token
            "trueconcurrent.apt", // two environment token
            "unfolding1.apt", // two environment token
            "independentNets.apt", //  more than one env token
            "unreachableEnvTransition2.apt", //  two env token      
            "thesis2sys2env.apt", //  two env token                  
            //            "secondTry.apt", // net not safe p0
            //            "finiteWithBad.apt", // net not safe p19
            //            "firstTry.apt" // net not safe
            // now safe but:
            "firstTry.apt", //  two env token
            "secondTry.apt", //  two env token
            "finiteWithBad.apt", //  two env token
            "finite3.apt", //  two env token
            "thirdTry.apt", // two env token
            "txt.apt", // two env token
            "txt2.apt", // two env token
            // %%%% changes player's membership
            "causalmemory.apt", // p0 (sys) - t0 -> p2 (env)
            "minimal.apt", // A (env) -tA-> B (sys)
            // %%%%%% MISC
            "unreachableEnvTransition.apt" // this has a problem with the partitioning
    // because there is a transition with two env places in the preset (but this transition is not reachable)
    // could either automatically delete unreachable transitions (expensive) or 
    // do a more expensive partitioning check and the coding of the partitions more expensive by checking reachabillity
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
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
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
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, SolvingException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NotSupportedGameException, ParameterMissingException, CalculationInterruptedException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        if (file.getAbsolutePath().contains("scalable")) { // just a quick hack to skip the large examples
            Logger.getInstance().addMessage("skip this", false);
            return;
        }
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(file.getAbsolutePath(), opts);
//        if (notSupported.contains(file.getName())) {
//            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
//            Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
//        } else {
        String output = outputDir + file.getName().split(".apt")[0];
        BDDTestingTools.testExample(solv, output, hasStrategy);
//        }
    }
}
