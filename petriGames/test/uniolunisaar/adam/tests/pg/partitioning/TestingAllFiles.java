package uniolunisaar.adam.tests.pg.partitioning;

import uniolunisaar.adam.logic.pg.partitioning.Partitioner;
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
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
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
    private static final List<String> skip = new ArrayList<>(Arrays.asList(
            //            "robots_true.apt", // infinite inv calculation (only wihtout the concurrency preserving stuff?)
            //            "robots_false.apt", // infinite inv calculation (only without the concurrency preserving stuff?)
            "wf_2_3_pg_reversible.apt", // infinite inv calculation with PIPE and FARKAS
            //            "container_withoutAnnotation.apt", // infinite inv calculation with FARKAS
            //            "container.apt", // infinite inv calculation with FARKAS
            "unfair2.apt", // has two env token  
            "myexample7.apt", // has two environment token
            "sendingprotocolTwo.apt", // two environment token
            "unfair7.apt", // two env token
            "unfair8.apt", // two env token
            "unfair9.apt", // two env token
            "unfair10.apt" // two env token
    )
    );
    private static final List<String> notSupported = new ArrayList<>(Arrays.asList());

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
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
    public void testFile(File file, boolean hasStrategy) throws ParseException, IOException, NetNotSafeException, NoStrategyExistentException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, NotSupportedGameException, ParameterMissingException, FileNotFoundException, ModuleException, CouldNotCalculateException {
        Logger.getInstance().addMessage("Testing file: " + file.getAbsolutePath(), false);
        PetriNet net = Tools.getPetriNet(file.getAbsolutePath());
        PetriGame game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
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
        Partitioner.doIt(game);

        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        PNWTTools.savePnwt2PDF(outputDir + File.separator + file.getName(), game, false, (int) tokencount);
        Assert.assertTrue(isValidDistributed(game));
//        }

    }

    private boolean isValidDistributed(PetriGame net) {
        for (Place place : net.getPlaces()) {
            if (! net.hasPartition(place)) {
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

    private boolean hasEnv(Set<Place> places, PetriGame game) {
        for (Place place : places) {
            if (game.isEnvironment(place)) {
                return true;
            }
        }
        return false;
    }
}
