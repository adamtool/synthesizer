package uniolunisaar.adam.logic.transformers;

import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class PGWT2Tikz {

    public static String get(PetriGameWithTransits net) {
        return new PGWT2TikzRenderer().renderFromCoordinates(net);
    }
}
