package uniolunisaar.adam.symbolic.bddapproach.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.generators.pg.CarRouting;
import uniolunisaar.adam.generators.pg.Clerks;
import uniolunisaar.adam.generators.pg.ContainerTerminal;
import uniolunisaar.adam.generators.pg.ManufactorySystem;
import uniolunisaar.adam.generators.pg.Philosopher;
import uniolunisaar.adam.generators.pg.RobotCell;
import uniolunisaar.adam.generators.pg.SecuritySystem;
import uniolunisaar.adam.generators.pg.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pg.Workflow;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CreatingGeneratorExamples {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/generators/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testPhilosophers() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException {
        testPhilosophers(2);
        testPhilosophers(3);
//        testPhilosophers(4);
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
    public void testClerks() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException {
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

    @Test(timeOut = (60 * 1000) / 2) // 30 sec
    public void testRobotCell() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testRobotCell(2, 1, true);
        testRobotCell(3, 2, true);
    }

    @Test(timeOut = (60 * 1000) / 2) // 30 sec
    public void testSelfOrganizingRobots() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testSelfOrgaRobots(2, 1, true);
//        testSelfOrgaRobots(3, 1, true);
//        testSelfOrgaRobots(2, 2, false); // not solvable ndet?
//        testSelfOrgaRobots(3, 2, true); // not solvable ndet?
//        testSelfOrgaRobots(4, 1);
    }

    @Test
    public void testSelfOrganizingRobotsNew() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testSelfOrgaRobotsNew(1, 1, 1, false);
        testSelfOrgaRobotsNew(1, 1, 2, false);
        testSelfOrgaRobotsNew(1, 1, 3, false);
        testSelfOrgaRobotsNew(1, 2, 1, true);
        testSelfOrgaRobotsNew(1, 2, 2, false);
        testSelfOrgaRobotsNew(1, 2, 3, false);
        testSelfOrgaRobotsNew(2, 2, 1, true);
        testSelfOrgaRobotsNew(2, 2, 2, false);
//        testSelfOrgaRobotsNew(2, 2, 3, false);
    }

    @Test
    public void testSelfOrganizingRobotsBengt() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testSelfOrgaRobotsBengt(1, 1, 1, false);
//        testSelfOrgaRobotsBengt(1, 1, 2, false);
//        testSelfOrgaRobotsBengt(1, 1, 3, false);
//        testSelfOrgaRobotsBengt(1, 2, 1, true);        
//        testSelfOrgaRobotsBengt(1, 2, 2, false);
//        testSelfOrgaRobotsBengt(1, 2, 3, false);
//        testSelfOrgaRobotsBengt(2, 2, 1, true);        
        testSelfOrgaRobotsBengt(2, 2, 1, true);
        testSelfOrgaRobotsBengt(2, 2, 2, false);
//        testSelfOrgaRobotsBengt(2, 2, 3, false);
    }

    @Test
    public void testManufactorySystem() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testManu(2);
        testManu(3);
//        testManu(12);
    }

    @Test
    public void testWorkflow() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
//        testWork(4, 1);
//        testWork(3, 3);
        testWork(3, 2, true);
//        testWork(4, 3);
//        testWork(4, 4);
    }

    @Test
    public void testCMImproved() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
//        testWork(4, 1);
//        testWork(3, 3);
        testWorkImprovedVersion(2, 1, true);
        testWorkImprovedVersion(3, 2, true);
//        testWork(4, 3);
//        testWork(4, 4);
    }

    @Test
    public void testSecuritySystem() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testSecuritySystem(4, true);
    }

    @Test
    public void testERouting() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testERouting(2, 2, true);
    }

    @Test
    public void testARouting() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        testARouting(2, 2, true);
        testAReRouting(2, 2, false); // wanted to build it with true, but it correctly has no strategy, because of the scheduling
    }

    @Test
    public void testContainerTerminal() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException {
//        testContainerTerminal(2); //net not safe, more than one env token!
    }

    private void testContainerTerminal(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "containerTerminal" + File.separator;
        String name = count + "_container";
        File f = new File(path);
        f.mkdir();
        PetriGame pn = ContainerTerminal.createSafetyVersion(count, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, false);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testPhilosophersGuided(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "philosophers_guided" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGame pn = Philosopher.generateGuided2(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testPhilosophers(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "philosophers" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGame pn = Philosopher.generateIndividual(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testClerksCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException {
        final String path = outputDir + "clerks" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        PetriGame pn = Clerks.generateCP(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testClerksNonCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "clerks_nonCP" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate clerks ...");
        PetriGame pn = Clerks.generateNonCP(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testRobotCell(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "robotCell" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGame pn = RobotCell.generate(robots, destroyable, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobots(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "selfOrgaRobots" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGame pn = SelfOrganizingRobots.generate(robots, destroyable, true, false);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, false);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobotsNew(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "selfOrgaRobotsNewVersion" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGame pn = SelfOrganizingRobots.generate(robots, tools, phases, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, false);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobotsBengt(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "selfOrgaRobotsBengtVersion" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGame pn = SelfOrganizingRobots.generateImproved(robots, tools, phases, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, false);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testManu(int machines) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "manufactory" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate factory...");
        PetriGame pn = ManufactorySystem.generate(machines, true, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testWork(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriGame pn = Workflow.generate(machines, pieces, true, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testWorkImprovedVersion(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriGame pn = Workflow.generateImprovedVersion(machines, pieces, true, true);
        Tools.savePN(path + name, pn);
        PNWTTools.saveAPT(path + name, pn, true);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSecuritySystem(int intrudingPoints, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "securitySystem" + File.separator;
        String name = intrudingPoints + "_secSystems";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate security System...");
        PetriGame pn = SecuritySystem.createReachabilityVersion(intrudingPoints, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testERouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "routing" + File.separator;
        String name = "Erouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate routing...");
        PetriGame pn = CarRouting.createEReachabilityVersion(nb_routings, nb_cars, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testARouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "routing" + File.separator;
        String name = "Arouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate routing...");
        PetriGame pn = CarRouting.createAReachabilityVersion(nb_routings, nb_cars, true);
        Tools.savePN(path + name, pn);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
        BDDGraph gg = solv.getGraphGame();
        BDDTools.saveGraph2PDF(path + name + "_graphgame", gg, solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
        Pair<BDDGraph, PetriGame> strats = solv.getStrategies();
        String gtikz = BDDTools.graph2Tikz(strats.getFirst(), solv);
        String ggtikz = BDDTools.graph2Tikz(gg, solv);
        String pgtikz = PGTools.pg2Tikz(strats.getSecond());
        Tools.saveFile(path + name + "_g.tex", gtikz);
        Tools.saveFile(path + name + "_gg.tex", ggtikz);
        Tools.saveFile(path + name + "_pg.tex", pgtikz);
    }

    private void testAReRouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException {
        final String path = outputDir + "routing" + File.separator;
        String name = "ARErouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate routing...");
        PetriGame pn = CarRouting.createAReachabilityVersionWithRerouting(nb_routings, nb_cars, true);
        PNWTTools.saveAPT(path + name, pn, true);
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
//        BDDGraph gg = solv.getGraphGame();
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", gg, solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
//        Pair<BDDGraph, PetriGame> strats = solv.getStrategies();
//        String gtikz = BDDTools.graph2Tikz(strats.getFirst(), solv);
//        String ggtikz = BDDTools.graph2Tikz(gg, solv);
//        String pgtikz = PNWTTools.pg2Tikz(strats.getSecond());
//        Tools.saveFile(path + name + "_g.tex", gtikz);
//        Tools.saveFile(path + name + "_gg.tex", ggtikz);
//        Tools.saveFile(path + name + "_pg.tex", pgtikz);
    }
}
