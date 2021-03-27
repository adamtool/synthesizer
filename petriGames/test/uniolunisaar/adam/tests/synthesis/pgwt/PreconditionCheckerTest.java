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
import uniolunisaar.adam.AdamSynthesizer;
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

    @Test
    public void testPartitionCheck() throws Exception {
        PetriGameWithTransits pgame = PGTools.getPetriGameFromAPTString(".name \"Assassin\"\n"
                + ".description \"This game shows that sometimes a purely environmental transition has to be fired despite a system transition being enabled, to make the system loose\"\n"
                + ".type LPN\n"
                + ".options\n"
                + "condition=\"A_SAFETY\"\n"
                + "\n"
                + ".places\n"
                + "assassin_positioned[yCoord=-270.0, xCoord=-210.0, env=\"true\", token=2]\n"
                + "dead[yCoord=30.0, xCoord=-270.0, bad=\"true\", token=0]\n"
                + "env[yCoord=-390.0, xCoord=-90.0, env=\"true\", token=1]\n"
                + "guilty_env[yCoord=-390.0, xCoord=-330.0, env=\"true\", token=1]\n"
                + "safe[yCoord=30.0, xCoord=-30.0, token=0]\n"
                + "sys[yCoord=30.0, xCoord=-150.0, token=0]\n"
                + "\n"
                + ".transitions\n"
                + "hire_assassin[label=\"hire_assassin\", yCoord=-390.0, xCoord=-210.0]\n"
                + "kill[label=\"kill\", yCoord=-150.0, xCoord=-210.0]\n"
                + "mercy[label=\"mercy\", yCoord=-150.0, xCoord=-90.0]\n"
                + "\n"
                + ".flows\n"
                + "hire_assassin: {1*env} -> {1*assassin_positioned, 1*guilty_env}\n"
                + "kill: {1*sys, 1*assassin_positioned} -> {1*dead}\n"
                + "mercy: {1*sys, 1*env} -> {1*safe}\n"
                + "\n"
                + ".initial_marking {1*env, 1*sys}", false, false);
        PgwtPreconditionChecker checker = AdamSynthesizer.createPreconditionChecker(pgame);
        
        checker.check();
        
        boolean exStrat = AdamSynthesizer.existsWinningStrategyBDD(pgame);
        Assert.assertFalse(exStrat);
    }
}
