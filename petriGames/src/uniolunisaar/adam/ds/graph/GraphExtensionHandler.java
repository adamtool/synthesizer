package uniolunisaar.adam.ds.graph;

import java.util.List;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.util.AdamPGWTExtensions;
import uniolunisaar.adam.util.ExtensionManagement;

/**
 *
 * @author Manuel Gieseking
 */
class GraphExtensionHandler {

// %%%%%%%%%%%%%%%%%%%%%%%%% STATE EXTENSIONS
    static List<Transition> getTransitions(State state) {
        return ExtensionManagement.getInstance().getExtension(state, AdamPGWTExtensions.t, List.class);
    }

    static boolean hasTransitions(State state) {
        return ExtensionManagement.getInstance().hasExtension(state, AdamPGWTExtensions.t);
    }

    static void setTransitions(State state, List<Transition> trans) {
        ExtensionManagement.getInstance().putExtension(state, AdamPGWTExtensions.t, trans);
    }

    static List<Transition> getStrategyTransition(State state) {
        return ExtensionManagement.getInstance().getExtension(state, AdamPGWTExtensions.strat_t, List.class);
    }

    static boolean hasStrategyTransition(State state) {
        return ExtensionManagement.getInstance().hasExtension(state, AdamPGWTExtensions.strat_t);
    }

    static void setStrategyTransition(State state, List<Transition> strat_trans) {
        ExtensionManagement.getInstance().putExtension(state, AdamPGWTExtensions.strat_t, strat_trans);
    }

}
