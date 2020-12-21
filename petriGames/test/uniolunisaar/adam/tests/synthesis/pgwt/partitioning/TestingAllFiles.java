package uniolunisaar.adam.tests.synthesis.pgwt.partitioning;

import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.Partitioner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingAllFiles {

    private static final String inputDir = System.getProperty("examplesfolder");
    private static final String outputDir = System.getProperty("testoutputfolder");
    private static final List<String> withoutStrategy = new ArrayList<>(Arrays.asList());
    private static final List<String> skipFile = new ArrayList<>(Arrays.asList(
            //            //            "robots_true.apt", // infinite inv calculation (only wihtout the concurrency preserving stuff?)
            //            //            "robots_false.apt", // infinite inv calculation (only without the concurrency preserving stuff?)
            //            "wf_2_3_pg_reversible.apt", // infinite inv calculation with PIPE and FARKAS
            //            //            "container_withoutAnnotation.apt", // infinite inv calculation with FARKAS
            //            //            "container.apt", // infinite inv calculation with FARKAS
            //            "unfair2.apt", // has two env token  
            //            "myexample7.apt", // has two environment token
            //            "sendingprotocolTwo.apt", // two environment token
            //            "unfair7.apt", // two env token
            //            "unfair8.apt", // two env token
            //            "unfair9.apt", // two env token
            //            "unfair10.apt" // two env token
            /////////////////////////////////////////////
            ///// not bounded
            "wf_2_3_pg_reversible.apt",
            ///// take to long calculating the reachability graph
            "10_DW.apt",
            "11_DW.apt",
            "12_DW.apt",
            "13_DW.apt",
            "14_DW.apt",
            "15_DW.apt",
            "16_DW.apt",
            "17_DW.apt",
            "18_DW.apt",
            "19_DW.apt",
            "11_DWs.apt",
            "12_DWs.apt",
            "13_DWs.apt",
            "14_DWs.apt",
            "15_DWs.apt",
            "16_DWs.apt",
            "17_DWs.apt",
            "18_DWs.apt",
            "19_DWs.apt",
            "20_DWs.apt",
            "09_JP.apt",
            "10_JP.apt",
            "11_JP.apt",
            "r3_d3_SR.apt",
            "r3_d4_SR.apt",
            "m3_w5_CM.apt",
            "m3_w6_CM.apt",
            "m4_w4_CM.apt",
            "m4_w5_CM.apt",
            "m4_w6_CM.apt",
            "m5_w3_CM.apt",
            "m5_w4_CM.apt",
            "m5_w5_CM.apt",
            "m5_w6_CM.apt",
            "m6_w3_CM.apt",
            "m6_w4_CM.apt",
            "m6_w5_CM.apt",
            "m6_w6_CM.apt",
            ///// take still too much time for the reachability graph to have them all in the suite
            "07_DW.apt",
            "08_DW.apt",
            "09_DW.apt",
            ///// High-Level
            "burglar-HL.apt",
            ///// missing transit annotation
            "handbuiltSDN-TACAS21.apt",
            "SDN-Arpanet196912.apt",
            "SDN-Napnet.apt",
            "handbuiltSDN-ATVA19.apt",
            "SDN-Heanet.apt"
    )
    );
    private static final List<String> skipFolder = new ArrayList<>(Arrays.asList("sdn"));
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList());

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(true);
        (new File(outputDir)).mkdirs();
    }

    @DataProvider(name = "files")
    public static Object[][] allExamples() {
        Collection<File> files = FileUtils.listFiles(
                new File(inputDir),
                new RegexFileFilter(".*\\.apt"),
                DirectoryFileFilter.DIRECTORY);
        Object[][] out = new Object[files.size() - skipFile.size()][2];
        int i = 0;
        for (File file : files) {
            boolean skip = false;
            for (String folder : skipFolder) {
                if (file.getAbsolutePath().contains(folder)) {
                    skip = true;
                    break;
                }
            }
            if (!skip && !skipFile.contains(file.getName())) {
//            if (!file.getAbsolutePath().contains("sdn") && !skipFile.contains(file.getName())) {
                out[i][0] = file;
                out[i][1] = !(withoutStrategy.contains(file.getName()));
                i++;
            }
        }
        Object[][] decreasedOut = new Object[i][2];
        System.arraycopy(out, 0, decreasedOut, 0, i);
        return decreasedOut;
    }

    @Test
    public void testSomeFiles() throws Exception {
        File file;
//        File file = new File(inputDir+"/synthesis/forallsafety/burglar/burglar.apt");
//        testFile(file, true);
//        file = new File(inputDir+"/synthesis/forallsafety/scalable/documentWorkFlow/standard/15_DW.apt"); // takes too long calculating the reachability graph
//        testFile(file, true);
//        file = new File(inputDir + "/synthesis/forallsafety/scalable/documentWorkFlow/standard/DW-1.apt");
//        testFile(file, true);
        file = new File(inputDir + "/synthesis/forallsafety/jhh/myexample00.apt");
        testFile(file, true);
//        file = new File(inputDir+"/synthesis/forallsafety/reversible/wf_2_3_pg_reversible.apt"); // not bounded
//        testFile(file, true);
    }

    @Test(dataProvider = "files", enabled = true)
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NotSupportedGameException, ParameterMissingException, FileNotFoundException, ModuleException, CouldNotCalculateException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        PetriNet net = Tools.getPetriNet(file.getAbsolutePath());
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
//        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
//        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);

        for (Place place : game.getPlaces()) {
            place.removeExtension("token");
        }

//        PetriGameAnnotator.annotateMaxTokenCount(net);
//        long tokencount = AdamExtensions.getMaxTokenCount(net);
//        if (file.getName().equals("abb62.apt")) {
//        if (file.getName().equals("nondetNoStrat.apt")) {
        long start = System.currentTimeMillis();
        System.out.println("start partitioning: " + start);
        Partitioner.doIt(game, false);
        System.out.println("end partitioning. Needed: " + (System.currentTimeMillis() - start) / 100 + "s");

        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        PNWTTools.savePnwt2PDF(outputDir + File.separator + file.getName(), game, false, (int) tokencount);
        Assert.assertTrue(isValidDistributed(game));
//        }

    }

    private boolean isValidDistributed(PetriGameWithTransits net) {
//        System.out.println("Check distribution");
        for (Place place : net.getPlaces()) {
            if (!net.hasPartition(place)) {
                return false;
            }
        }
        CoverabilityGraph graph = CoverabilityGraph.getReachabilityGraph(net);
        for (Iterator<CoverabilityGraphNode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            Marking m = next.getMarking();
            for (Place place : net.getPlaces()) {
                if (m.getToken(place).getValue() > 0) {
                    int token = net.getPartition(place);
                    for (Place p1 : net.getPlaces()) {
                        if (p1 != place && m.getToken(p1).getValue() > 0 && token == net.getPartition(p1)) {
                            return false;
                        }
                    }

                }
            }
        }
        return true;
    }

    private boolean hasEnv(Set<Place> places, PetriGameWithTransits game) {
        for (Place place : places) {
            if (game.isEnvironment(place)) {
                return true;
            }
        }
        return false;
    }
}
