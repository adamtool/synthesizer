package uniolunisaar.adam.symbolic.bddapproach.buchi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.symbolic.bddapproach.BDDTestingTools;
import uniolunisaar.adam.ds.exceptions.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
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
public class FirstTests {

    private static final String inputDir = System.getProperty("examplesfolder") + "/buechi/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
    }

    private void testToyExamples(String name, boolean hasStrat) throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String path = inputDir + "toyExamples" + File.separator;
        BDDSolver<? extends WinningCondition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
//        BDDTools.saveGraph2PDF(path + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(path + name + "_gg_strat", solv.getGraphStrategy(), solv);
        BDDTestingTools.testExample(solv, path + name, hasStrat);
    }

    @Test(enabled = false) //-- currently bdd error
    public void testToyExampleType2A() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        testToyExamples("type2A", true);
    }

    @Test(enabled = false) //-- currently bdd error
    public void testToyExampleType2B() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        testToyExamples("type2B", true);
    }

    @Test
    public void testToyExampleFiniteA() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        testToyExamples("finiteA", true);
    }

    @Test
    public void testToyExampleInfiniteA() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        testToyExamples("infiniteA", true);
    }

    @Test
    public void testToyExampleInfiniteB() throws IOException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, UnboundedPGException, CouldNotFindSuitableWinningConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        testToyExamples("infiniteB", false);
    }

}
