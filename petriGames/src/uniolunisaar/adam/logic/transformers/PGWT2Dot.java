package uniolunisaar.adam.logic.transformers;

import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class PGWT2Dot {

    public static String get(PetriGameWithTransits net, boolean withLabel, boolean withOrigPlaces) {
        return new PGWT2DotRenderer<>().render(net, withLabel, withOrigPlaces);
    }

    public static String get(PetriGameWithTransits net, boolean withLabel, boolean withOrigPlaces, Integer nb_partitions) {
        return new PGWT2DotRenderer<>().render(net, withLabel, withOrigPlaces, nb_partitions);
    }
}
