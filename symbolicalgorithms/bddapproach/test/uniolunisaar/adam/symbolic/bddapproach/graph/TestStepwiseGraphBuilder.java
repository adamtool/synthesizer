package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestStepwiseGraphBuilder {

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testStepwise() throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "burglar" + File.separator;
        final String name = "burglar";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        solv.initialize();
        BDDGraph graph = new BDDGraph("burglar_gg");
        BDDState init = BDDGraphGameBuilderStepwise.getInitialState(graph, solv);
        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.getSuccessors(init, graph, solv);

        for (BDDState bDDState : succs.getSecond()) {
            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.getSuccessors(bDDState, graph, solv);

        }
    }

    @Test
    public void testStepwiseFirstExample() throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        solv.initialize();
        BDDGraph graph = new BDDGraph("firstExamplePaper_gg");
        BDDState init = BDDGraphGameBuilderStepwise.getInitialState(graph, solv);
        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.getSuccessors(init, graph, solv);
        for (BDDState bDDState : succs.getSecond()) {
//            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.getSuccessors(bDDState, graph, solv);
//            System.out.println(bDDState.toString());
        }
    }

}
