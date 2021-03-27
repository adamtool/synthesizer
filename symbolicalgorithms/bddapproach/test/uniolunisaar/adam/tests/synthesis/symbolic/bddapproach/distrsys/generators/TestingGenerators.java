package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.LoopUnrolling;
import uniolunisaar.adam.generators.pgwt.ManufactorySystem;
import uniolunisaar.adam.generators.pgwt.Philosopher;
import uniolunisaar.adam.generators.pgwt.RobotCell;
import uniolunisaar.adam.generators.pgwt.SecuritySystem;
import uniolunisaar.adam.generators.pgwt.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pgwt.Watchdog;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.BDDTestingTools;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingGenerators {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/generators/";
//    private static final int countPhilsGuided = 4;
//    private static final int countPhils = 3;
//    private static final int countClerksCP = 10;
//    private static final int countClerksNonCP = 8;
//    private static final int countManu = 5;
//    private static final int countRobotCell_rb = 2;
//    private static final int countRobotCell_destr = 3;
//    private static final int countSelfOrga_rb = 1;
//    private static final int countSelfOrga_destr = 2;
//    private static final int countCM_machines = 3;
//    private static final int countCM_pieces = 3;
////    private static final int countWD_machines = 9;
//    private static final int countWD_machines = 3;
//    private static final int countSecuritySystems = 8;

    // fast
    private static final int countPhilsGuided = 1;
    private static final int countPhils = 1;
    private static final int countClerksCP = 2;
    private static final int countClerksNonCP = 2;
    private static final int countManu = 2;
    private static final int countRobotCell_rb = 2;
    private static final int countRobotCell_destr = 2;
    private static final int countSelfOrga_rb = 1;
    private static final int countSelfOrga_destr = 2;
    private static final int countSelfOrgaNew_rb = 1;
    private static final int countSelfOrgaNew_tool = 1;
    private static final int countSelfOrgaNew_ph = 2;
    private static final int countCM_machines = 1;
    private static final int countCM_pieces = 2;
//    private static final int countWD_machines = 9;
    private static final int countWD_machines = 2;
    private static final int countSecuritySystems = 1;
    private static final int countContainerPlaces = 1;
    private static final int countRoutingEReachRoutes = 2;
    private static final int countRoutingEReachCars = 5;
    private static final int countLoopUnrollings = 3;

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GUIDED PHILOSOPHERS
    @DataProvider(name = "philsGuided")
    public static Object[][] philsGuided() {
        Object[][] out = new Object[countPhilsGuided][2];
        for (int i = 0; i < countPhilsGuided; i++) {
            out[i][0] = i + 2;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "philsGuided")
    public void testPhilosophersGuided(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "philosophers_guided" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Philosopher.generateGuided2(count, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PHILOSOPHERS
    @DataProvider(name = "phils")
    public static Object[][] phils() {
        Object[][] out = new Object[countPhils][2];
        for (int i = 0; i < countPhils; i++) {
            out[i][0] = i + 2;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "phils")
    public void testPhilosophers(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, UnboundedException, CalculationInterruptedException {
        final String path = outputDir + "philosophers" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Philosopher.generateIndividual(count, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Clerks CP
    @DataProvider(name = "clerksCP")
    public static Object[][] clerksCP() {
        Object[][] out = new Object[countClerksCP][2];
        for (int i = 0; i < countClerksCP; i++) {
            out[i][0] = i + 1;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "clerksCP")
    public void testClerksCP(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "clerks" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        PetriGameWithTransits pn = Clerks.generateCP(count, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Clerks nonCP
    @DataProvider(name = "clerksNonCP")
    public static Object[][] clerksNonCP() {
        Object[][] out = new Object[countClerksNonCP][2];
        for (int i = 0; i < countClerksNonCP; i++) {
            out[i][0] = i + 1;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "clerksNonCP")
    public void testClerksNonCP(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "clerks_nonCP" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate clerks ...");
        PetriGameWithTransits pn = Clerks.generateNonCP(count, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Manu
    @DataProvider(name = "manu")
    public static Object[][] manu() {
        Object[][] out = new Object[countManu][2];
        for (int i = 0; i < countManu; i++) {
            out[i][0] = i + 2;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "manu")
    public void testManu(int machines, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "manufactory" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate factory...");
        PetriGameWithTransits pn = ManufactorySystem.generate(machines, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% robotCell
    @DataProvider(name = "robotCell")
    public static Object[][] robotCell() {
        Object[][] out = new Object[countRobotCell_rb * countRobotCell_destr][3];
        for (int i = 0; i < countRobotCell_rb; i++) {
            for (int j = 0; j < countRobotCell_destr; j++) {
                out[i * countRobotCell_destr + j][0] = i + 2;
                out[i * countRobotCell_destr + j][1] = j;
                out[i * countRobotCell_destr + j][2] = true;
            }
        }
        return out;
    }

    @Test(dataProvider = "robotCell", timeOut = (60 * 1000) / 2) // 30 sec
    public void testRobotCell(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "robotCell" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGameWithTransits pn = RobotCell.generate(robots, destroyable, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% self organizing robots
    @DataProvider(name = "selfOrgaRobots")
    public static Object[][] selfOrgaRobots() {
        Object[][] out = new Object[countSelfOrga_rb * countSelfOrga_destr][3];
        for (int i = 0; i < countSelfOrga_rb; i++) {
            for (int j = 0; j < countSelfOrga_destr; j++) {
                out[i * countSelfOrga_destr + j][0] = i + 2;
                out[i * countSelfOrga_destr + j][1] = j + 1;
                out[i * countSelfOrga_destr + j][2] = (i + 2) > (j + 1);
            }
        }
        return out;
    }

    @Test(dataProvider = "selfOrgaRobots")
    public void testSelfOrgaRobots(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
//        if (robots == 2 && destroyable == 2) {
//            return; // is not solvable (new determinism condition)
//        }
        final String path = outputDir + "selfOrgaRobots" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriGameWithTransits pn = SelfOrganizingRobots.generate(robots, destroyable, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% self organizing robots new version
    @DataProvider(name = "selfOrgaRobotsNew")
    public static Object[][] selfOrgaRobotsNew() {
        Object[][] out = new Object[countSelfOrgaNew_rb * countSelfOrgaNew_tool * countSelfOrgaNew_ph][4];
        for (int i = 0; i < countSelfOrgaNew_rb; i++) {
            for (int j = 0; j < countSelfOrgaNew_tool; j++) {
                for (int k = 0; k < countSelfOrgaNew_ph; k++) {
                    out[i * countSelfOrgaNew_tool * countSelfOrgaNew_ph + j * countSelfOrgaNew_ph + k][0] = i + 1;
                    out[i * countSelfOrgaNew_tool * countSelfOrgaNew_ph + j * countSelfOrgaNew_ph + k][1] = i + 1 + j;
                    out[i * countSelfOrgaNew_tool * countSelfOrgaNew_ph + j * countSelfOrgaNew_ph + k][2] = k + 1;
                    out[i * countSelfOrgaNew_tool * countSelfOrgaNew_ph + j * countSelfOrgaNew_ph + k][3] = i + j > k;
                }
            }
        }
        return out;
    }

    @Test(dataProvider = "selfOrgaRobotsNew")
    public void testSelfOrgaRobotsNew(int robots, int tools, int phases, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "selfOrgaRobotsNew" + File.separator;
        String name = "R" + robots + "T" + tools + "P" + phases;
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots R" + robots + "T" + tools + "P" + phases + " ...");
        PetriGameWithTransits pn = SelfOrganizingRobots.generate(robots, tools, phases, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% concurrent machines
    @DataProvider(name = "concurrentMachines")
    public static Object[][] work() {
        Object[][] out = new Object[countCM_machines * countCM_pieces][3];
        for (int i = 0; i < countCM_machines; i++) {
            for (int j = 0; j < countCM_pieces; j++) {
                out[i * countCM_pieces + j][0] = i + 2;
                out[i * countCM_pieces + j][1] = j + 1;
                out[i * countCM_pieces + j][2] = (i + 2) > (j + 1);
            }
        }
        return out;
    }

    @Test(dataProvider = "concurrentMachines")
    public void testWork(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriGameWithTransits pn = Workflow.generateNewAnnotationPoster(machines, pieces, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    @Test(dataProvider = "concurrentMachines")
    public void testCMImproved(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "workflowImproved" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriGameWithTransits pn = Workflow.generateImprovedVersion(machines, pieces, true, true);
        PNWTTools.savePnwt2PDF(path + name, pn, false);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Watchdog
    @DataProvider(name = "watchdog")
    public static Object[][] watchdog() {
        Object[][] out = new Object[countWD_machines][2];
        for (int i = 0; i < countWD_machines; i++) {
            out[i][0] = i + 1;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "watchdog")
    public void testWatchdog(int machines, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "watchdog" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate watchdog...");
        PetriGameWithTransits pn = Watchdog.generate(machines, false, false, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Security System
    @DataProvider(name = "secSystem")
    public static Object[][] securitySystem() {
        Object[][] out = new Object[countSecuritySystems][2];
        for (int i = 0; i < countSecuritySystems; i++) {
            out[i][0] = i + 2;
            out[i][1] = true;
        }
        return out;
    }

    @Test(dataProvider = "secSystem")
    public void testSecuritySystem(int intrudingPoints, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "securitySystem" + File.separator;
        String name = intrudingPoints + "_secSystems";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate security System...");
        PetriGameWithTransits pn = SecuritySystem.createSafetyVersion(intrudingPoints, true);
//        Tools.savePN(path+name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Loop Unrolling
    @DataProvider(name = "loopUnrolling")
    public static Object[][] loopUnrolling() {
        Object[][] out = new Object[countLoopUnrollings][2];
        for (int i = 0; i < countLoopUnrollings; i++) {
            out[i][0] = i + 1;
            out[i][1] = false;
        }
        return out;
    }

    @Test(dataProvider = "loopUnrolling", timeOut = (60 * 1000) / 2) // 30 sec
    public void testLoopUnrollingWithNewChain(int nb_unrollings, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "loopUnrollingWithNewChain" + File.separator;
        String name = nb_unrollings + "_unrollings";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate loop unrolling example...");
        PetriGameWithTransits pn = LoopUnrolling.createESafetyVersion(nb_unrollings, true, true);
//        Tools.savePN(path+name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrategy);
    }

    @Test(dataProvider = "loopUnrolling", timeOut = (60 * 1000) / 2) // 30 sec
    public void testLoopUnrolling(int nb_unrollings, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, SolvingException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException, CalculationInterruptedException {
        final String path = outputDir + "loopUnrolling" + File.separator;
        String name = nb_unrollings + "_unrollings";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate loop unrolling example...");
        PetriGameWithTransits pn = LoopUnrolling.createESafetyVersion(nb_unrollings, false, true);
//        Tools.savePN(path+name, pn);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(pn, opts);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
        BDDTestingTools.testExample(solv, path + name, true);
    }

//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Security System
//    @DataProvider(name = "ERouting")
//    public static Object[][] existsRouting() {
//        Object[][] out = new Object[countE][2];
//        for (int i = 0; i < countSecuritySystems; i++) {
//            out[i][0] = i + 2;
//            out[i][1] = true;
//        }
//        return out;
//    }
//
//    @Test(dataProvider = "secSystem")
//    public void testSecuritySystem(int intrudingPoints, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, ParameterMissingException, ParseException {
//        final String path = outputDir + "securitySystem" + File.separator;
//        String name = intrudingPoints + "_secSystems";
//        File f = new File(path);
//        f.mkdir();
//        System.out.println("Generate security System...");
//        PetriGame pn = SecuritySystem.createSafetyVersion(intrudingPoints, true);
////        Tools.savePN(path+name, pn);
//        DistrSysBDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
////        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, path + name, hasStrategy);
//    }
//    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Container terminal (more than one env place)
//    @DataProvider(name = "conTerminal")
//    public static Object[][] containerTerminal() {
//        Object[][] out = new Object[countContainerPlaces][2];
//        for (int i = 0; i < countContainerPlaces; i++) {
//            out[i][0] = i + 2;
//            out[i][1] = true;
//        }
//        return out;
//    }
//
//    @Test(dataProvider = "conTerminal")
//    public void testContainerTerminal(int containerPlaces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableConditionException {
//        final String path = outputDir + "containerTerminal" + File.separator;
//        String name = containerPlaces + "_conTerminal";
//        File f = new File(path);
//        f.mkdir();
//        System.out.println("Generate container terminal...");
//        PetriGame pn = ContainerTerminal.createSafetyVersion(containerPlaces, true);
////        Tools.savePN(path+name, pn);
//        DistrSysBDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(pn, true);
////        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTestingTools.testExample(solv, path + name, hasStrategy);
//    }
}
