package uniolunisaar.adam.tests.generators.pgwt;

import uniolunisaar.adam.generators.pgwt.Philosopher;
import uniolunisaar.adam.generators.pgwt.ManufactorySystem;
import uniolunisaar.adam.generators.pgwt.Watchdog;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.generators.pgwt.SecuritySystem;
import uniolunisaar.adam.generators.pgwt.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pgwt.RobotCell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingGenerators {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/";
    private static final int countPhilsGuided = 4;
    private static final int countPhils = 3;
    private static final int countClerksCP = 10;
    private static final int countClerksNonCP = 8;
    private static final int countManu = 5;
    private static final int countRobotCell_rb = 2;
    private static final int countRobotCell_destr = 3;
    private static final int countSelfOrga_rb = 1;
    private static final int countSelfOrga_destr = 3;
    private static final int countCM_machines = 3;
    private static final int countCM_pieces = 3;
//    private static final int countWD_machines = 9;
    private static final int countWD_machines = 3;
    private static final int countSecuritySystems = 8;

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
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
    public void testPhilosophersGuided(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "philosophers_guided" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Philosopher.generateGuided2(count, true, true);
        //todo: do testing
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
    public void testPhilosophers(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "philosophers" + File.separator;
        String name = count + "_phils";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Philosopher.generateIndividual(count, true, true);
        //todo: do testing
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
    public void testClerksCP(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "clerks" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        PetriNet pn = Clerks.generateCP(count, true, true);
        //todo: do testing
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
    public void testClerksNonCP(int count, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "clerks_nonCP" + File.separator;
        String name = count + "_clerks";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate clerks ...");
        PetriNet pn = Clerks.generateNonCP(count, true, true);
        //todo: do testing
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
    public void testManu(int machines, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "manufactory" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate factory...");
        PetriNet pn = ManufactorySystem.generate(machines,  true, true);
        //todo: do testing
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

    @Test(dataProvider = "robotCell")
    public void testRobotCell(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "robotCell" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriNet pn = RobotCell.generate(robots, destroyable, true);
        //todo: do testing
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
    public void testSelfOrgaRobots(int robots, int destroyable, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "selfOrgaRobots" + File.separator;
        String name = robots + "_robots_" + destroyable + "destr";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate robots ...");
        PetriNet pn = SelfOrganizingRobots.generate(robots, destroyable, true, true);
        //todo: do testing
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
    public void testWork(int machines, int pieces, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "workflow" + File.separator;
        String name = machines + "_machines_" + pieces + "_pieces";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate Workflow...");
        PetriNet pn = Workflow.generateNewAnnotationPoster(machines, pieces, true, true);
        //todo: do testing
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Manu
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
    public void testWatchdog(int machines, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "watchdog" + File.separator;
        String name = machines + "_machines";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate watchdog...");
        PetriNet pn = Watchdog.generate(machines, false, false, true);
        //todo: do testing
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Sexurity System
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
    public void testSecuritySystem(int intrudingPoints, boolean hasStrategy) throws NetNotSafeException, NetNotConcurrencyPreservingException, NoStrategyExistentException, IOException, InterruptedException, FileNotFoundException, ModuleException, NoSuitableDistributionFoundException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException {
        final String path = outputDir + "securitySystem" + File.separator;
        String name = intrudingPoints + "_secSystems";
        File f = new File(path);
        f.mkdir();
        System.out.println("Generate security System...");
        PetriNet pn = SecuritySystem.createReachabilityVersion(intrudingPoints, true);
        //todo: do testing
    }
}
