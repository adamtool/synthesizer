package uniolunisaar.adam.symbolic.mtbddapproach.libraries;

import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
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
    public static void testCall() throws ParseException, IOException, SolvingException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableConditionException, ParameterMissingException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        MTBDDSolver<? extends Condition> solv = MTBDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        solv.existsWinningStrategy();
    }

    @Test
    public static void testCall2() throws ParseException, IOException, SolvingException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableConditionException, ParameterMissingException, NoStrategyExistentException, uniolunisaar.adam.ds.exceptions.NoStrategyExistentException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";       
        String output = "asdf";
        try {
            MTBDDSolver<? extends Condition> solv = MTBDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
            solv.getStrategy();
        } catch (UnsupportedOperationException e) {
            output = e.getMessage();
        }
        Assert.assertEquals(output, "Not supported yet.");
    }
}
