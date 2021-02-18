package uniolunisaar.adam.tests.synthesis.symbolic.bddapproach.distrsys.mcutscheduling.safe.safety;

import org.testng.Assert;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.ConcurrencyPreservingCalculator;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CornerCasesDistrSysBDDASafety {

    @Test
    public void cornerCaseSysPlaceWithoutToken() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        pg.createPlace();

        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(0, strategy.getPlaces().size());
    }

    @Test
    public void cornerCaseSysPlaceWithToken() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        Place p = pg.createPlace();
        p.setInitialToken(1);

        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(1, strategy.getPlaces().size());
    }

    @Test
    public void cornerCaseEnvPlaceWithoutToken() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        pg.createEnvPlace();

        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(0, strategy.getPlaces().size());
    }

    @Test
    public void cornerCaseEnvPlaceWithToken() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        Place p = pg.createEnvPlace();
        p.setInitialToken(1);

        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(1, strategy.getPlaces().size());
    }

    @Test
    public void cornerCaseTransitionWithoutFlow() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        pg.createTransition();
        
        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(0, strategy.getPlaces().size());
    }
    @Test
    public void cornerCaseSystemPlaceAndTransitionWithoutFlow() throws CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NoStrategyExistentException {
        PetriGameWithTransits pg = new PetriGameWithTransits("asdf", new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator());
        pg.putExtension("condition", "A_SAFETY");
        pg.createTransition();
        Place sys = pg.createPlace();
        sys.setInitialToken(1);
        
        DistrSysBDDSolver<? extends Condition<?>> solver = DistrSysBDDSolverFactory.getInstance().getSolver(pg);
        Assert.assertTrue(solver.existsWinningStrategy());

        PetriGameWithTransits strategy = solver.getStrategy();
        Assert.assertEquals(1, strategy.getPlaces().size());
        Assert.assertEquals(1, strategy.getTransitions().size());
    }
}
