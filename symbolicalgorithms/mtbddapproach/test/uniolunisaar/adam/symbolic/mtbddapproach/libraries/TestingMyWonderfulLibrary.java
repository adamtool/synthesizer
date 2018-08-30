package uniolunisaar.adam.symbolic.mtbddapproach.libraries;

import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolvingException;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.mtbdd.solver.MTBDDSolver;
import uniolunisaar.adam.symbolic.mtbdd.solver.MTBDDSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingMyWonderfulLibrary {

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public static void testCall() throws ParseException, IOException, SolvingException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableWinningConditionException, ParameterMissingException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        MTBDDSolver<? extends WinningCondition> solv = MTBDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        solv.existsWinningStrategy();
    }

    @Test
    public static void testCall2() throws ParseException, IOException, SolvingException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableWinningConditionException, ParameterMissingException, NoStrategyExistentException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";       
        String output = "asdf";
        try {
            MTBDDSolver<? extends WinningCondition> solv = MTBDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
            solv.getStrategy();
        } catch (UnsupportedOperationException e) {
            output = e.getMessage();
        }
        Assert.assertEquals(output, "Not supported yet.");
    }
}
