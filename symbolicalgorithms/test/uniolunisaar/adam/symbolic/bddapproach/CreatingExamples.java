package uniolunisaar.adam.symbolic.bddapproach;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CreatingExamples {

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
    }

    @Test
    public void testFirstExamplePaper() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testBurglar() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, UnboundedPGException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testConstructedExample() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String path = inputDir + "constructedExample" + File.separator;
        final String name = "constructedExample";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testType1Type2Mutex() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final String path = inputDir + "olderog" + File.separator + "type1Type2Mutex" + File.separator;
        final String name = "net";
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }
}
