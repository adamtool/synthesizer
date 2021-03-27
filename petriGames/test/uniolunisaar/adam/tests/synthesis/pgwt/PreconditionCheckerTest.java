package uniolunisaar.adam.tests.synthesis.pgwt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.PartitionerInvariants;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PgwtPreconditionChecker;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class PreconditionCheckerTest {

    private static final String inputDir = System.getProperty("examplesfolder") + "";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/pnwt/";
    

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }
    
    
    @Test(enabled = true)
    public void testGoodBadLoop0() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/existsbuechi/toyExamples/goodBadLoop0.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        
        new PgwtPreconditionChecker(game).check();

    }
}


