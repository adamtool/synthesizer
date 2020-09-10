package uniolunisaar.adam.tests.util.pg;

import java.io.File;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNTools;

/**
 *
 * @author thewn
 */
@Test
public class TestStrategyCoordinateGeneration {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/coords/";
    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/";

    @BeforeClass
    public void createFolder() {
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void burglar() throws Exception {
        PetriGameWithTransits game = PGTools.getPetriGame(inputDir + "burglar/burglar.apt", true, false);
        PGTools.saveAPT(outputDir + game.getName(), game, true, true);
        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(game);
        PetriGameWithTransits strategy = solver.getStrategy();
        PGTools.addCoordinates(game, strategy);
        PetriNet unique = PNTools.createPetriNetWithIDsInLabel(strategy);
        PGTools.savePG2PDF(outputDir + strategy.getName(), strategy, true);
        Tools.savePN(outputDir + strategy.getName(), unique);
    }
}
