package uniolunisaar.adam.symbolic.bddapproach.graph;

import uniolunisaar.adam.logic.distrsynt.builder.graph.symbolic.bddapproach.BDDGraphGameBuilderStepwise;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestStepwiseGraphBuilder {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/";
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
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        solv.initialize();
        BDDGraph graph = new BDDGraph("burglar_gg");
        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);

        for (BDDState bDDState : succs.getSecond()) {
            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.addSuccessors(bDDState, graph, solv);

        }
    }

    @Test
    public void testStepwiseFirstExample() throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
        solv.initialize();
        BDDGraph graph = new BDDGraph("firstExamplePaper_gg");
        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);
        for (BDDState bDDState : succs.getSecond()) {
//            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.getSuccessors(bDDState, graph, solv);
//            System.out.println(bDDState.toString());
        }
    }

    @Test
    public void testSuccessors() throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException, SolvingException {
        final String apt = ".name \"Workflow_M3WP1\"\n"
                + ".type LPN\n"
                + ".options\n"
                + "condition=\"A_SAFETY\"\n"
                + "\n"
                + ".places\n"
                + "A0[token=2]\n"
                + "A1[token=4]\n"
                + "A2[token=6]\n"
                + "B00[bad=\"true\", token=1]\n"
                + "B10[bad=\"true\", token=1]\n"
                + "B20[bad=\"true\", token=1]\n"
                + "Env[env=\"true\"]\n"
                + "G0[token=2]\n"
                + "G1[token=4]\n"
                + "G2[token=6]\n"
                + "M00[token=1]\n"
                + "M10[token=1]\n"
                + "M20[token=1]\n"
                + "S0[token=1]\n"
                + "e[env=\"true\"]\n"
                + "testP0[token=3]\n"
                + "testP1[token=5]\n"
                + "testP2[token=7]\n"
                + "\n"
                + ".transitions\n"
                + "t0[label=\"t0\"]\n"
                + "t1[label=\"t1\"]\n"
                + "t10[label=\"t10\"]\n"
                + "t11[label=\"t11\"]\n"
                + "t2[label=\"t2\"]\n"
                + "t3[label=\"t3\"]\n"
                + "t4[label=\"t4\"]\n"
                + "t5[label=\"t5\"]\n"
                + "t6[label=\"t6\"]\n"
                + "t7[label=\"t7\"]\n"
                + "t8[label=\"t8\"]\n"
                + "t9[label=\"t9\"]\n"
                + "test0[label=\"test0\"]\n"
                + "test1[label=\"test1\"]\n"
                + "test2[label=\"test2\"]\n"
                + "\n"
                + ".flows\n"
                + "t0: {1*Env} -> {1*e, 1*testP1, 1*testP2, 1*A2, 1*A1}\n"
                + "t1: {1*Env} -> {1*e, 1*A0, 1*testP2, 1*A2, 1*testP0}\n"
                + "t10: {1*M20} -> {1*B20}\n"
                + "t11: {1*M20, 1*A2} -> {1*G2}\n"
                + "t2: {1*Env} -> {1*A0, 1*testP0, 1*e, 1*testP1, 1*A1}\n"
                + "t3: {1*S0} -> {1*M00}\n"
                + "t4: {1*M00} -> {1*B00}\n"
                + "t5: {1*A0, 1*M00} -> {1*G0}\n"
                + "t6: {1*S0} -> {1*M10}\n"
                + "t7: {1*M10} -> {1*B10}\n"
                + "t8: {1*M10, 1*A1} -> {1*G1}\n"
                + "t9: {1*S0} -> {1*M20}\n"
                + "test0: {1*S0, 1*testP0} -> {1*S0}\n"
                + "test1: {1*S0, 1*testP1} -> {1*S0}\n"
                + "test2: {1*S0, 1*testP2} -> {1*S0}\n"
                + "\n"
                + ".initial_marking {1*Env, 1*S0}";

        PetriGameWithTransits game = PGTools.getPetriGameFromAPTString(apt, true, true);
        BDDSolverOptions opts = new BDDSolverOptions(true, true);
        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(game, opts);
        solv.initialize();
        BDDGraph graph = new BDDGraph("firstExamplePaper_gg");
        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);
        for (BDDState bDDState : succs.getSecond()) {
            Pair<List<Flow>, List<BDDState>> succs2 = BDDGraphGameBuilderStepwise.addSuccessors(bDDState, graph, solv);
//            System.out.println(succs2.getSecond().size());
        }
    }

}
