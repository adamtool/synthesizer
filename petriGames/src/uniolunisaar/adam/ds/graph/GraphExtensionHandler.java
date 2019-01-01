package uniolunisaar.adam.ds.graph;

import java.util.List;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.util.AdamExtensions;

/**
 *
 * @author Manuel Gieseking
 */
class GraphExtensionHandler {

// %%%%%%%%%%%%%%%%%%%%%%%%% STATE EXTENSIONS
    static List<Transition> getTransition(State state) {
        return (List<Transition>) state.getExtension(AdamExtensions.t.name());
    }

    static boolean hasTransition(State state) {
        return state.hasExtension(AdamExtensions.t.name());
    }

    static void setTransition(State state, List<Transition> trans) {
        state.putExtension(AdamExtensions.t.name(), trans);
    }

    static List<Transition> getStrategyTransition(State state) {
        return (List<Transition>) state.getExtension(AdamExtensions.strat_t.name());
    }

    static boolean hasStrategyTransition(State state) {
        return state.hasExtension(AdamExtensions.strat_t.name());
    }

    static void setStrategyTransition(State state, List<Transition> strat_trans) {
        state.putExtension(AdamExtensions.strat_t.name(), strat_trans);
    }

}
