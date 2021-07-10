package uniolunisaar.adam.logic.transformers;

import uniol.apt.adt.pn.PetriNet;

/**
 *
 * @author Manuel Gieseking
 */
public class PGWT2Tikz {

    public static String get(PetriNet net) {
        return new PGWT2TikzRenderer().renderFromCoordinates(net);
    }
}
