package uniolunisaar.adam.ds.petrigame;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.util.AdamExtensions;

/**
 *
 * @author Manuel Gieseking
 */
public class PetriGameExtensionHandler {

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PLACE EXTENSIONS   
    static boolean isEnvironment(Place place) {
        return place.hasExtension(AdamExtensions.env.name());
    }

    static void setEnvironment(Place place) {
        place.putExtension(AdamExtensions.env.name(), true, ExtensionProperty.WRITE_TO_FILE);
    }

    static boolean isSystem(Place place) {
        return !place.hasExtension(AdamExtensions.env.name());
    }

    static void setSystem(Place place) {
        if (isEnvironment(place)) {
            place.removeExtension(AdamExtensions.env.name());
        }
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%% NET EXTENSIONS
//    static long getMaxTokenCount(PetriGame game) throws NoCalculatorProvidedException {
//        if (!hasMaxTokenCount(game)) {
//            ExtensionCalculator calc = game.getCalculators().get(AdamExtensions.MAXTOKENCOUNT);
//            if (calc == null) {
//                throw new NoCalculatorProvidedException(game, AdamExtensions.MAXTOKENCOUNT.name());
//            }
//            setMaxTokenCount(game, (long) calc.calculate(game));
//        }
//        return (long) game.getExtension(AdamExtensions.MAXTOKENCOUNT.name());
////        //todo: properly
////        Object tokencount = net.getExtension(AdamExtensions.MAXTOKENCOUNT.name());
////        if (tokencount instanceof Long) {
////            return (long) tokencount;
////        }
////        return (int) tokencount;
//    }
//    static void setMaxTokenCount(PetriNet net, long maxToken) {
//        net.putExtension(AdamExtensions.MAXTOKENCOUNT.name(), maxToken, ExtensionProperty.WRITE_TO_FILE);
//    }
//
//    static boolean hasMaxTokenCount(PetriNet net) {
//        return net.hasExtension(AdamExtensions.MAXTOKENCOUNT.name());
//    }
//    static void setConcurrencyPreserving(PetriNet net, boolean cp) {
//        net.putExtension(AdamExtensions.CONCURRENCYPRESERVING.name(), cp);
//    }
//
//    static boolean hasConcurrencyPreserving(PetriNet net) {
//        return net.hasExtension(AdamExtensions.CONCURRENCYPRESERVING.name());
//    }
//
//    static boolean isConcurrencyPreserving(PetriGame game) throws NoCalculatorProvidedException {
//        if (!hasConcurrencyPreserving(game)) {
//            ExtensionCalculator calc = game.getCalculators().get(AdamExtensions.CONCURRENCYPRESERVING);
//            if (calc == null) {
//                throw new NoCalculatorProvidedException(game, AdamExtensions.CONCURRENCYPRESERVING.name());
//            }
//            setConcurrencyPreserving(game, (boolean) calc.calculate(game));
//        }
//        return (boolean) game.getExtension(AdamExtensions.CONCURRENCYPRESERVING.name());
//    } 
//    public static void setPartialObservation(PetriGame game, boolean po) {
//        game.putExtension(AdamExtensions.partialObservation.name(), po, ExtensionProperty.WRITE_TO_FILE);
//    }
}
