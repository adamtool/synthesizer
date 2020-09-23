package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.kbounded;

import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.kbounded.BDDkBoundedSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.kbounded.BDDkBoundedSolver;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NetNotConcurrencyPreservingException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class FirstTests {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/kbounded";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/kbounded/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(true);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    private void testExamples(String name, boolean hasStrat) throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        final String path = inputDir + File.separator;
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        BDDkBoundedSolver solv = (BDDkBoundedSolver) BDDkBoundedSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        System.out.println("ExStrat" + solv.existsWinningStrategy());
//        solv.getGraphStrategy();
//        BDDGraph g = BDDGraphBuilder.builtGraphStrategy(solv, 5);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat_d5", g, solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_graphgame", solv.getGraphGame(), solv);
//        BDDTools.saveGraph2PDF(outputDir + name + "_gg_strat", solv.getGraphStrategy(), solv);
        PNWTTools.savePnwt2PDF(outputDir + name, solv.getGame(), false);
//        Assert.expectThrows(UnsupportedOperationException.class, solv.exWinStrat());
        Assert.assertEquals(0, solv.getDcs_length(), name);
    }

    @Test
    public void testFirstExample() throws IOException, SolvingException, NetNotSafeException, NetNotConcurrencyPreservingException, InterruptedException, NoStrategyExistentException, NoSuitableDistributionFoundException, UnboundedException, ParseException, SolverDontFitPetriGameException, NotSupportedGameException, CouldNotFindSuitableConditionException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParameterMissingException {
        testExamples("firstTest", true);
    }
}
