package uniolunisaar.adam.logic.pg.calculators;

import uniol.apt.analysis.conpres.ConcurrencyPreserving;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.util.pg.ExtensionCalculator;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class ConcurrencyPreservingCalculator extends ExtensionCalculator<Boolean> {

    public ConcurrencyPreservingCalculator() {
        super(CalculatorIDs.CONCURRENCY_PRESERVING.name());
    }

    @Override
    public Boolean calculate(PetriGame game) {
        ConcurrencyPreserving con = new ConcurrencyPreserving(game);
        Logger.getInstance().addMessage("Check concurrency preserving ...");
        boolean concurrencyPreserving = con.check();
        Logger.getInstance().addMessage("Concurrency preserving: " + concurrencyPreserving);
        return concurrencyPreserving;
    }

}
