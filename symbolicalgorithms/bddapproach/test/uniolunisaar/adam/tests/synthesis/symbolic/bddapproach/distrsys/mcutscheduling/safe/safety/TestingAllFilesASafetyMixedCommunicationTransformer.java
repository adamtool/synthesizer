package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety;

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
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.logic.transformers.PGWT2PGWTN_NMC;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFilesASafetyMixedCommunicationTransformer {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/mixedCommunication/";
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList(
            "abb62.apt",
            "lateSameDecision.apt",
            "tafel.apt",
            "tafel_3.apt",
            "vsp__adam_machines.apt",
            "infiniteSystemTrysToAvoidEnvUseBadPlace.apt",
            "firstExamplePaper_extended.apt",
            "envSkipsSys.apt",
            "robots_false.apt",
            "myexample4.apt",
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
            // %%%% Examples which tests the nondeterminism
            "nondet.apt",
            "nondet_s3_noStrat.apt",
            "nondet_unnecessarily_noStrat.apt",
            "nondet_withBad.apt",
            "nondet2WithSys.apt",
            "nondet2.apt",
            "nondet2LateDecision.apt",
            "nondet2LateDecision2.apt",
            "nondet_jhh1.apt",
            "nondet_jhh3.apt",
            //            "journalReview2.apt", // should only have a strategy for the journal version of the ndet
            //            "nondet_s3.apt", // should only have a strategy for the journal version of the ndet
            "nondet_withBad.apt",
            "nondet2WithSys.apt"
    // %%% Examples which we now cannot solve due to only checking the local ndet
    //            "nondet2SysAtStart.apt", // should have a strategy for the original definition of ndet
    //            "nondet2WithStratByGameSolving.apt", // should have a strategy for the original definition of ndet
    //            "nondet2SysPlace.apt" // should have a strategy for the original definition of ndet
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
            // unbounded when using the approach by adding an activation token but no inhibitor arcs
            "mutex.apt",
            "vsp_1_withBadPlaces.apt",
            "vsp_1.apt",
            // %%%%%% MISC
            "unreachableEnvTransition.apt" // this has a problem with the partitioning            
    // because there is a transition with two env places in the preset (but this transition is not reachable)
    // could either automatically delete unreachable transitions (expensive) or 
    // do a more expensive partitioning check and the coding of the partitions more expensive by checking reachabillity
    ));

    private static final List<String> skipFolder = new ArrayList<>(Arrays.asList(
            "envScheduling",
            "lukas-panneke"
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
        Collection<File> skipFiles = new ArrayList<>();
        for (String folderPath : skipFolder) {
            skipFiles.addAll(FileUtils.listFiles(
                    new File(inputDir + File.separator + folderPath),
                    new RegexFileFilter(".*\\.apt"),
                    DirectoryFileFilter.DIRECTORY));
        }
        Collection<File> files = FileUtils.listFiles(
                new File(inputDir),
                new RegexFileFilter(".*\\.apt"),
                DirectoryFileFilter.DIRECTORY);
        files.removeAll(skipFiles);
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
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, SolvingException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NotSupportedGameException, ParameterMissingException, CalculationInterruptedException, RenderException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        if (file.getAbsolutePath().contains("scalable")) { // just a quick hack to skip the large examples
            Logger.getInstance().addMessage("skip this", false);
            return;
        }
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv_std = DistrSysBDDSolverFactory.getInstance().getSolver(file.getAbsolutePath(), opts);
//        if (notSupported.contains(file.getName())) {
//            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(solv.getNet());
//            Assert.assertTrue(AdamTools.isSolvablePetriGame(solv.getNet(), cover) != null, "Petri game not solvable: ");
//        } else {
        System.out.println("File: " + file.getName());
        PetriGameWithTransits nmc_game = PGWT2PGWTN_NMC.create(solv_std.getGame(), true);
        System.out.println("Finished transformation.");

        String output = outputDir + file.getName().split(".apt")[0];
        PGTools.saveAPT(output + "_orig", solv_std.getGame(), true, true);
        PGTools.savePG2PDF(output + "_orig", solv_std.getGame(), false);
        PGTools.saveAPT(output + "_nmc", nmc_game, true, true);
        PGTools.savePG2PDF(output + "_nmc", nmc_game, false);

//        BoundedResult checkBounded = Bounded.checkBounded(nmc_game);
//        System.out.println(checkBounded.unboundedPlace.toString());
//        System.out.println(checkBounded.sequence.toString());
        DistrSysBDDSolver<? extends Condition> solv_nmc = DistrSysBDDSolverFactory.getInstance().getSolver(nmc_game, opts);
        Assert.assertEquals(solv_nmc.existsWinningStrategy(), solv_std.existsWinningStrategy());
//        BDDTestingTools.testExample(solv, output, hasStrategy);
//        }
    }

    @Test
    public void testSpecialFile() throws Exception {
//        String filename = "mixedCommunication" + File.separator + "twoMixedAtSameTransition.apt";
//        boolean hasStrat = true;
//        String filename = "mixedCommunication" + File.separator + "systemRefuge.apt";
//        boolean hasStrat = false;
        String filename = "mixedCommunication" + File.separator + "noCommitmentChoosableInComPlace.apt";
        boolean hasStrat = false;
//        String filename = "mixedCommunication" + File.separator + "twoMixedAtSameTransitionMoreSystem.apt";
//        boolean hasStrat = true;
//        String filename = "ma_vsp" + File.separator + "vsp_1_withBadPlaces.apt";
//        boolean hasStrat = true;
//        String filename = "mutex.apt";
//        boolean hasStrat = true;
        String path = inputDir + File.separator + filename;
        File file = new File(path);
        testFile(file, hasStrat);
    }
}
