package uniolunisaar.adam.tests.pg.partitioning;

import uniolunisaar.adam.logic.pg.partitioning.PartitionerInvariants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.conpres.ConcurrencyPreserving;
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
public class TestingAllFilesInvariants {

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

        boolean cp = new ConcurrencyPreserving(net).check();
        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        Set<Place> bufferPre = new HashSet<>();
        Set<Place> bufferPost = new HashSet<>();
        if (!cp) {
            for (Transition t : game.getTransitions()) {
                int pre = t.getPreset().size();
                int post = t.getPostset().size();
                if (pre < post) {
                    Place p = null;
                    for (int i = pre; i < post; i++) {
                        p = game.createPlace();
                        game.createFlow(p, t);
                        bufferPre.add(p);
                    }
                    if (hasEnv(t.getPostset(), game) && !hasEnv(t.getPreset(), game)) {
                        game.setEnvironment(p);
                    }
                }
                if (post < pre) {
                    Place p = null;
                    for (int i = post; i < pre; i++) {
                        p = game.createPlace();
                        game.createFlow(t, p);
                        bufferPost.add(p);
                    }
                    if (hasEnv(t.getPreset(), game) && !hasEnv(t.getPostset(), game)) {
                        game.setEnvironment(p);
                    }
                }
            }
            Marking m = game.getInitialMarking();
            for (Place place : bufferPre) {
                m = m.addTokenCount(place, 1);
//                System.out.println(place.toString());
            }
//            System.out.println("%%%%%%%%%%%%%%%%%%%%%" + m.toString());
            game.setInitialMarking(m);
//            PetriGameExtensionHandler.setConcurrencyPreserving(game, true);
            PNWTTools.savePnwt2PDF(outputDir + File.separator + file.getName() + "_ext", game, false);
        }

//        if (file.getName().equals("abb62.apt")) {
        PartitionerInvariants.doIt(game);
//        }
        if (!cp) {
            for (Place place : bufferPre) {
                game.removePlace(place);
            }
            for (Place place : bufferPost) {
                game.removePlace(place);
            }
//            PetriGameExtensionHandler.setConcurrencyPreserving(game, false);
        }
        PNWTTools.savePnwt2PDF(outputDir + File.separator + file.getName(), game, false, (int) tokencount);
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
