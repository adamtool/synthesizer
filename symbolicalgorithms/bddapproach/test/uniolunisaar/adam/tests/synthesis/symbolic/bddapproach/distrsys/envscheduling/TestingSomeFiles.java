package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.envscheduling;

import java.io.File;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.ConcurrencyPreservingGamesCalculator;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.envscheduling.DistrSysBDDEnvSchedulingSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.envscheduling.DistrSysBDDEnvSchedulingSolverFactory;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingSomeFiles {

    private static final String inputDir = System.getProperty("examplesfolder") + "/globalsafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/globalsafety/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void unsyncDecision() throws Exception {
        String name = "UnSyncDecision";
        PetriGameWithTransits game = new PetriGameWithTransits(name,
                new ConcurrencyPreservingGamesCalculator(),
                new MaxTokenCountCalculator());
        // env
        Place E = game.createEnvPlace("E");
        E.setInitialToken(1);
        Place EL = game.createEnvPlace("EL");
        Place ER = game.createEnvPlace("ER");
        Transition el = game.createTransition("el");
        Transition er = game.createTransition("er");
        game.createFlow(E, el);
        game.createFlow(el, EL);
        game.createFlow(E, er);
        game.createFlow(er, ER);

        // sys
        Place S = game.createPlace("S");
        S.setInitialToken(1);
        Place S_ = game.createPlace("S'");
        Transition t = game.createTransition();
        game.createFlow(S, t);
        game.createFlow(t, S_);
        Place SL = game.createPlace("SL");
        Place SR = game.createPlace("SR");
        Transition sl = game.createTransition("sl");
        Transition sr = game.createTransition("sr");
        game.createFlow(S_, sl);
        game.createFlow(sl, SL);
        game.createFlow(S_, sr);
        game.createFlow(sr, SR);

        // Winning condition
        Marking m1 = new Marking(game);
        m1 = m1.setTokenCount(EL, 1);
        m1 = m1.setTokenCount(SL, 1);
        game.addFinalMarking(m1);

        Marking m2 = new Marking(game);
        m2 = m2.setTokenCount(ER, 1);
        m2 = m2.setTokenCount(SR, 1);
        game.addFinalMarking(m2);
     
        PGTools.setConditionAnnotation(game, Condition.Objective.GLOBAL_SAFETY);

        DistrSysBDDEnvSchedulingSolver solv = DistrSysBDDEnvSchedulingSolverFactory.getInstance().getSolver(game, new BDDSolverOptions(true));
        BDDTools.saveGraph2PDF(outputDir + name + "_GG", solv.getGraphGame(), solv);

        for (List<Place> m : solv.getSolvingObject().getWinCon().getMarkings()) {
            System.out.println(m.toString());
        }

        String file = outputDir + name;
        PNWTTools.savePnwt2PDF(file, solv.getGame(), false);
//        Assert.assertTrue(PNWTTools.isSolvablePetriGame(solv.getNet(), cover) == null, "Is solvable:");
//        BDDTools.saveGraph2PDF(file + "_graph", solv.getGraphGame(), solv);
        PNWTTools.savePnwt2PDF(file + "_debug", solv.getGame(), true, solv.getSolvingObject().getMaxTokenCountInt());
//            printWinningStrategies(solv, file);
        boolean exStrat = solv.existsWinningStrategy();
//            Assert.assertTrue(exStrat, "Net: " + solv.getGame().getName() + " has winning strategy: ");
        Assert.assertFalse(exStrat, "Net: " + solv.getGame().getName() + " has winning strategy: ");
    }

}
