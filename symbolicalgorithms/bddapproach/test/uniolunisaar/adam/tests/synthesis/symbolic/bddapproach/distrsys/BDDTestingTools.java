package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys;

import java.io.IOException;
import org.testng.Assert;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDTestingTools {

    public static void testExample(DistrSysBDDSolver<? extends Condition> solv, String file, boolean hasStrategy) throws NetNotSafeException, NoStrategyExistentException, IOException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, CalculationInterruptedException {
//        CoverabilityGraph cover = solv.getGame().getReachabilityGraph();
        PNWTTools.savePnwt2PDF(file, solv.getGame(), false);
//        Assert.assertTrue(PNWTTools.isSolvablePetriGame(solv.getNet(), cover) == null, "Is solvable:");
//        BDDTools.saveGraph2PDF(file + "_graph", solv.getGraphGame(), solv);
        PNWTTools.savePnwt2PDF(file + "_debug", solv.getGame(), true, solv.getSolvingObject().getMaxTokenCountInt());
//            printWinningStrategies(solv, file);
        boolean exStrat = solv.existsWinningStrategy();
        if (hasStrategy) {
            Assert.assertTrue(exStrat, "Net: " + solv.getGame().getName() + " has winning strategy: ");
        } else {
            Assert.assertFalse(solv.existsWinningStrategy(), "Net: " + solv.getGame().getName() + " has winning strategy: ");
        }
        if (exStrat) {
            printWinningStrategies(solv, file);
        }
    }

    static void testExample(DistrSysBDDSolver<? extends Condition> solv, String file) throws NetNotSafeException, NoStrategyExistentException, IOException, InterruptedException, NoSuitableDistributionFoundException, UnboundedException, CalculationInterruptedException {
        testExample(solv, file, true);
    }

    private static void printWinningStrategies(DistrSysBDDSolver<? extends Condition> solv, String path) throws NoStrategyExistentException, IOException, InterruptedException, CalculationInterruptedException {
        Pair<BDDGraph, PetriGameWithTransits> strats = solv.getStrategies();
        //        System.out.println("Save graph to pdf.");
        BDDTools.saveGraph2PDF(path + "_gg", strats.getFirst(), solv);
//        System.out.println("Save petri game pdf.");
        PNWTTools.savePnwt2PDF(path + "_pg", strats.getSecond(), true);

        //   Tools.savePN2DotAndPDF(path + "_debug", pg.getNet(), true, pg);        
        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strats.getSecond());
        boolean det = PGTools.isDeterministic(strats.getSecond(), cover);
        Assert.assertTrue(det, strats.getSecond().getName() + " is deterministic");
        boolean res = PGTools.restrictsEnvTransition(solv.getGame(), strats.getSecond(), false);
        Assert.assertFalse(res, strats.getSecond().getName() + " restricts Environment Transitions");
        if (!(solv.getWinningCondition().getObjective().equals(Condition.Objective.A_REACHABILITY) || solv.getWinningCondition().getObjective().equals(Condition.Objective.E_REACHABILITY))) {
            if (!solv.getWinningCondition().getObjective().equals(Condition.Objective.E_SAFETY)) { // todo: we have to develop a notion for deadlock-avoiding but ok when goal reached
                boolean dead = PGTools.isDeadlockAvoiding(solv.getGame(), strats.getSecond(), cover);
                Assert.assertTrue(dead, strats.getSecond().getName() + " is Deadlock Avoiding");
            }
        }
    }

    private static void printWinningStratGraph(DistrSysBDDSolver<? extends Condition> solv, String path) throws NoStrategyExistentException, IOException, InterruptedException, CalculationInterruptedException {
        BDDGraph strat = solv.getGraphStrategy();

        BDDTools.saveGraph2DotAndPDF(path + "_gg", strat, solv);
    }

    private static void printWinningStratPG(DistrSysBDDSolver<? extends Condition> solv, String path) throws Exception {
        PetriGameWithTransits strategy = solv.getStrategy();
        PNWTTools.savePnwt2DotAndPDF(path + "_pg", strategy, true);
    }

}
