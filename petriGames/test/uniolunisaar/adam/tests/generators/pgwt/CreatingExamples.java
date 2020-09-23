package uniolunisaar.adam.tests.generators.pgwt;

import uniolunisaar.adam.generators.pgwt.Philosopher;
import uniolunisaar.adam.generators.pgwt.ManufactorySystem;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.generators.pgwt.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pgwt.RobotCell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CreatingExamples {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testPhilosophers() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        testPhilosophers(2);
        testPhilosophers(3);
        testPhilosophers(4);
//        testPhilosophers(4);
//        testPhilosophers(5);
//        testPhilosophers(6);
//        testPhilosophersGuided(2);
//        testPhilosophersGuided(3);
//        testPhilosophersGuided(4);
//        testPhilosophersGuided(5);
//        testPhilosophersGuided(6);
    }

    @Test
    public void testClerks() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
//        testClerksNonCP(1);
        testClerksNonCP(2);
        testClerksNonCP(3);
        testClerksNonCP(4);
//        testClerksNonCP(5);
//        testClerksNonCP(12);
//        testClerksNonCP(13);
//        testClerksNonCP(20);
//        testClerksNonCP(21);
//        testClerksNonCP(22);
//        testClerksCP(1);
//        testClerksCP(2);
//        testClerksCP(3);
//        testClerksCP(4);
//        testClerksCP(5);
//        testClerksCP(6);
//        testClerksCP(7);
//        testClerksCP(8);
//        testClerksCP(9);
    }

    @Test
    public void testRobotCell() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        testRobotCell(2, 1, true);
        testRobotCell(3, 2, true);
    }

    @Test
    public void testSelfOrganizingRobots() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        testSelfOrgaRobots(2, 1, true);
//        testSelfOrgaRobots(3, 2);
//        testSelfOrgaRobots(4, 1);
    }

    @Test
    public void testSelfOrganizingRobotsNew() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
//        testSelfOrgaRobotsNew(2, 2, 1, true);
        testSelfOrgaRobotsNew(2, 3, 2, true);
//        testSelfOrgaRobotsNew(2, 2, 2, true);
    }

    @Test
    public void testManufactorySystem() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        testManu(2);
        testManu(3);
//        testManu(12);
    }

    @Test
    public void testWorkflow() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
//        testWork(4, 1);
//        testWork(3, 3);
        testWork(3, 2, true);
//        testWork(4, 3);
//        testWork(4, 4);
    }

    private void testPhilosophersGuided(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "philosophers_guided" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Philosopher.generateGuided2(count, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testPhilosophers(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "philosophers" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Philosopher.generateIndividual(count, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testClerksCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "clerks" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Clerks.generateCP(count, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testClerksNonCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "clerks_nonCP" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate clerks ...");
        PetriNet pn = Clerks.generateNonCP(count, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testRobotCell(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "robotCell" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriNet pn = RobotCell.generate(robots, destroyable, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testSelfOrgaRobots(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "selfOrgaRobots" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriNet pn = SelfOrganizingRobots.generate(robots, destroyable, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testSelfOrgaRobotsNew(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "selfOrgaRobotsNewVersion" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriNet pn = SelfOrganizingRobots.generate(robots, tools, phases, true, true);
        Tools.savePN(path + name, pn);     
        //todo: do testing
    }

    private void testManu(int machines) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "manufactory" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate factory...");
        PetriNet pn = ManufactorySystem.generate(machines, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }

    private void testWork(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriNet pn = Workflow.generate(machines, pieces, true, true);
        Tools.savePN(path + name, pn);
        //todo: do testing
    }
}
