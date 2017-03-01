package uniolunisaar.adam.symbolic.bddapproach;

import java.io.File;
import java.io.IOException;
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
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.SolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CreatingExamples {

    private static final String inputDir = "../examples/safety/";

    @Test
    public void testFirstExamplePaper() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testBurglar() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, UnboundedPGException, SolverDontFitPetriGameException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testConstructedExample() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "constructedExample" + File.separator;
        final String name = "constructedExample";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }

    @Test
    public void testType1Type2Mutex() throws IOException, ParseException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException {
        final String path = inputDir + "olderog" + File.separator + "type1Type2Mutex" + File.separator;
        final String name = "net";
        Solver solv = SolverFactory.getSolver(path + name + ".apt", true);
        TestingTools.testExample(solv, path + name, true);
    }
}
