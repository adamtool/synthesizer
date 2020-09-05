package uniolunisaar.adam.tests.pg.partitioning;

import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.PartitionerInvariants;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author thewn
 */
public class FirstTests {

    private static final String inputDir = System.getProperty("examplesfolder");
    private static final String outputDir = System.getProperty("testoutputfolder");

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    @Test(enabled = true)
    public void testContainer() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/safety/container/container_withoutAnnotation.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testMultiChains() throws Exception {
        PetriGameWithTransits net = PGTools.getPetriGameFromParsedPetriNet(Tools.getPetriNet(inputDir + "/forallreachability/toyexamples/oneTokenMultiChains3.apt"), true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(net);

    }

    @Test(enabled = true)
    public void testBuchiNondet() throws Exception {
        PetriGameWithTransits net = PGTools.getPetriGameFromParsedPetriNet(Tools.getPetriNet(inputDir + "/buechi/toyExamples/nondet.apt"), true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(net);

    }

    @Test(enabled = true)
    public void testGoodBadLoop0() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/buechi/toyExamples/goodBadLoop0.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testInfFlowChains() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/existssafety/infflowchains/infflowchains.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testTafel3() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/safety/tafel/tafel_3.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testNonDet() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/safety/ndet/nondet2WithStratByGameSolving.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testNcp1() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/safety/notConcurrencyPreservingTests/ncp1.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
//        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        deleteTokenAnnotation(net);
        PartitionerInvariants.doIt(game);

    }

    @Test(enabled = true)
    public void testAbb62() throws Exception {
        PetriNet net = Tools.getPetriNet(inputDir + "/safety/ma_vsp/abb62.apt");
        PetriGameWithTransits game = PGTools.getPetriGameFromParsedPetriNet(net, true, true);
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS);
//        System.out.println(invariants.toString());
//        Assert.assertTrue(InvariantCalculator.coveredBySInvariants(net, invariants) != null);
//        List<Set<Place>> invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());
//        
//        invariants = Partitioner.getInvariantOneEquations(net, invariants);
//        System.out.println("suitable");
//        System.out.println(invariants.toString());
//    
//        invs = Partitioner.convert(net, invariants);
//        System.out.println(invs.toString());

        deleteTokenAnnotation(net);
        PartitionerInvariants.doIt(game);

    }

    public void deleteTokenAnnotation(PetriNet net) {

        for (Place place : net.getPlaces()) {
            place.removeExtension("token");
        }
    }
}
