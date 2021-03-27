package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.generators.pgwt.CarRouting;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.ContainerTerminal;
import uniolunisaar.adam.generators.pgwt.ManufactorySystem;
import uniolunisaar.adam.generators.pgwt.Philosopher;
import uniolunisaar.adam.generators.pgwt.RobotCell;
import uniolunisaar.adam.generators.pgwt.SecuritySystem;
import uniolunisaar.adam.generators.pgwt.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.BDDTestingTools;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
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

    @BeforeMethod
    public void silence() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
    }

    @BeforeClass
    public void createFolder() {
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testPhilosophers() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, CalculationInterruptedException {
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
    public void testClerks() throws IOException, SolvingException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, CalculationInterruptedException {
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
    public void testRobotCell() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testRobotCell(2, 1, true);
        testRobotCell(3, 2, true);
    }

    @Test(timeOut = (60 * 1000) / 2) // 30 sec
    public void testSelfOrganizingRobots() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testSelfOrgaRobots(2, 1, true);
//        testSelfOrgaRobots(3, 1, true);
//        testSelfOrgaRobots(2, 2, false); // not solvable ndet?
//        testSelfOrgaRobots(3, 2, true); // not solvable ndet?
//        testSelfOrgaRobots(4, 1);
    }

    @Test(timeOut = (60 * 1000) / 2) // 30 sec
    public void testSelfOrganizingRobotsNew() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
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

    @Test(timeOut = (60 * 1000) / 2) // 30 sec
    public void testSelfOrganizingRobotsBengt() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
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
    public void testManufactorySystem() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, UnboundedException, CalculationInterruptedException {
        testManu(2);
        testManu(3);
//        testManu(12);
    }

    @Test
    public void testWorkflow() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        testWork(4, 1);
//        testWork(3, 3);
        testWork(3, 2, true);
//        testWork(4, 3);
//        testWork(4, 4);
    }

    @Test
    public void testCMImproved() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        testWork(4, 1);
//        testWork(3, 3);
        testWorkImprovedVersion(2, 1, true);
        testWorkImprovedVersion(3, 2, true);
//        testWork(4, 3);
//        testWork(4, 4);
    }

    @Test
    public void testSecuritySystem() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testSecuritySystem(4, true);
    }

    @Test
    public void testERouting() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testERouting(2, 2, true);
    }

    @Test
    public void testARouting() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        testARouting(2, 2, true);
        testAReRouting(2, 2, false); // wanted to build it with true, but it correctly has no strategy, because of the scheduling
    }

    @Test
    public void testContainerTerminal() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException {
//        testContainerTerminal(2); //net not safe, more than one env token!
    }

    private void testContainerTerminal(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "containerTerminal" + File.separator;
        String name = count + "_container";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = ContainerTerminal.createSafetyVersion(count, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testPhilosophersGuided(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "philosophers_guided" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Philosopher.generateGuided2(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testPhilosophers(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "philosophers" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Philosopher.generateIndividual(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testClerksCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "clerks" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Clerks.generateCP(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testClerksNonCP(int count) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "clerks_nonCP" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate clerks ...");
        PetriGameWithTransits pn = Clerks.generateNonCP(count, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testRobotCell(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "robotCell" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate robots (robotCell) ...");
        PetriGameWithTransits pn = RobotCell.generate(robots, destroyable, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobots(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "selfOrgaRobots" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate robots (std) ...");
        PetriGameWithTransits pn = SelfOrganizingRobots.generate(robots, destroyable, true, false);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobotsNew(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "selfOrgaRobotsNewVersion" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate robots (new) ...");
        PetriGameWithTransits pn = SelfOrganizingRobots.generate(robots, tools, phases, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSelfOrgaRobotsBengt(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, NotSupportedGameException, SolverDontFitPetriGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "selfOrgaRobotsBengtVersion" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate robots (bengt)...");
        PetriGameWithTransits pn = SelfOrganizingRobots.generateImproved(robots, tools, phases, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(false, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testManu(int machines) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "manufactory" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate factory...");
        PetriGameWithTransits pn = ManufactorySystem.generate(machines, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, true);
    }

    private void testWork(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate Workflow...");
        PetriGameWithTransits pn = Workflow.generate(machines, pieces, true, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testWorkImprovedVersion(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate Workflow...");
        PetriGameWithTransits pn = Workflow.generateImprovedVersion(machines, pieces, true, true);
        Tools.savePN(path + name, pn);
        PNWTTools.saveAPT(path + name, pn, true, false);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testSecuritySystem(int intrudingPoints, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "securitySystem" + File.separator;
        String name = intrudingPoints + "_secSystems";
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate security System...");
        PetriGameWithTransits pn = SecuritySystem.createReachabilityVersion(intrudingPoints, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testERouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "routing" + File.separator;
        String name = "Erouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate routing...");
        PetriGameWithTransits pn = CarRouting.createEReachabilityVersion(nb_routings, nb_cars, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    private void testARouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "routing" + File.separator;
        String name = "Arouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate routing...");
        PetriGameWithTransits pn = CarRouting.createAReachabilityVersion(nb_routings, nb_cars, true);
        Tools.savePN(path + name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDGraph gg = solv.getGraphGame();
        BDDTools.saveGraph2PDF(path + name + "_graphgame", gg, solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
        Pair<BDDGraph, PetriGameWithTransits> strats = solv.getStrategies();
        String gtikz = BDDTools.graph2Tikz(strats.getFirst(), solv);
        String ggtikz = BDDTools.graph2Tikz(gg, solv);
        String pgtikz = PGTools.pg2Tikz(strats.getSecond());
        Tools.saveFile(path + name + "_g.tex", gtikz);
        Tools.saveFile(path + name + "_gg.tex", ggtikz);
        Tools.saveFile(path + name + "_pg.tex", pgtikz);
    }

    private void testAReRouting(int nb_routings, int nb_cars, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        final String path = outputDir + "routing" + File.separator;
        String name = "ARErouting_" + nb_routings + "_cars_" + nb_cars;
        File f = new File(path);
        f.mkdir();
        Logger.getInstance().addMessage("Generate routing...");
        PetriGameWithTransits pn = CarRouting.createAReachabilityVersionWithRerouting(nb_routings, nb_cars, true);
        PNWTTools.saveAPT(path + name, pn, true, false);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
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
