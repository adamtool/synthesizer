package uniolunisaar.adam.logic.pg.calculators;

import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.util.pg.ExtensionCalculator;
import uniolunisaar.adam.tools.Logger;

/**
 * Additionally to the equality of ingoing and outgoing arcs of each transition
 * this calculator demands that also the numbers for the partition to the
 * players agree.
 *
 * @author Manuel Gieseking
 */
public class ConcurrencyPreservingGamesCalculator extends ExtensionCalculator<Boolean> {

    public ConcurrencyPreservingGamesCalculator() {
        super(CalculatorIDs.CONCURRENCY_PRESERVING.name());
    }

    @Override
    public Boolean calculate(PetriGame game) {
        Logger.getInstance().addMessage("Check concurrency preserving ...");
        boolean concurrencyPreserving = true;
        for (Transition t : game.getTransitions()) {
            Set<Place> pre = t.getPreset();
            Set<Place> post = t.getPostset();
            if (post.size() != pre.size()) {
                concurrencyPreserving = false;
            } else {
                int nb_pre_env = 0;
                for (Place place : pre) {
                    if (game.isEnvironment(place)) {
                        ++nb_pre_env;
                    }
                }
                int nb_post_env = 0;
                for (Place place : post) {
                    if (game.isEnvironment(place)) {
                        ++nb_post_env;
                    }
                }
                if (nb_post_env != nb_pre_env) {
                    concurrencyPreserving = false;
                }
            }
        }
        Logger.getInstance().addMessage("Concurrency preserving: " + concurrencyPreserving);
        return concurrencyPreserving;
    }

}
