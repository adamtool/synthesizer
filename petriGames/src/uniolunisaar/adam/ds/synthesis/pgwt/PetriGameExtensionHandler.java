package uniolunisaar.adam.ds.synthesis.pgwt;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.bounded.BoundedResult;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.MoreThanOneEnvironmentPlayerException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.MoreThanOneSystemPlayerException;
import uniolunisaar.adam.util.AdamPGWTExtensions;
import uniolunisaar.adam.util.ExtensionManagement;

/**
 *
 * @author Manuel Gieseking
 */
public class PetriGameExtensionHandler {

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PLACE EXTENSIONS   
    static boolean isEnvironment(Place place) {
        return ExtensionManagement.getInstance().hasExtension(place, AdamPGWTExtensions.env);
    }

    static void setEnvironment(Place place) {
        ExtensionManagement.getInstance().putExtension(place, AdamPGWTExtensions.env, true, ExtensionProperty.WRITE_TO_FILE);
    }

    static boolean isSystem(Place place) {
        return !ExtensionManagement.getInstance().hasExtension(place, AdamPGWTExtensions.env);
    }

    static void setSystem(Place place) {
        if (isEnvironment(place)) {
            ExtensionManagement.getInstance().removeExtension(place, AdamPGWTExtensions.env);
        }
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% FLOW EXTENSIONS   
    static boolean isSpecial(Flow f) {
        return ExtensionManagement.getInstance().hasExtension(f, AdamPGWTExtensions.special);
    }

    static void setSpecial(Flow f) {
        ExtensionManagement.getInstance().putExtension(f, AdamPGWTExtensions.special, true, ExtensionProperty.WRITE_TO_FILE);
    }

    static void removeSpecial(Flow f) {
        ExtensionManagement.getInstance().removeExtension(f, AdamPGWTExtensions.special);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%% NET EXTENSIONS    
    // $$ bounded
    public static BoundedResult getBoundedResult(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.bounded, BoundedResult.class);
    }

    public static void setBoundedResult(PetriGameWithTransits game, BoundedResult res) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.bounded, res);
    }

    public static boolean hasBoundedResult(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.bounded);
    }

    public static void removeBoundedResult(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().removeExtension(game, AdamPGWTExtensions.bounded);
    }

    // $$ partitioning
    public static void setValidPartioned(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.partitioned, null);
    }

    public static void removePartitioned(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().removeExtension(game, AdamPGWTExtensions.partitioned);
    }

    public static boolean isValidPartitioned(PetriGameWithTransits game) {
        if (ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.partitioned)) {
            return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.partitioned, InvalidPartitionException.class) == null;
        }
        return false;
    }

    public static void setInValidPartioned(PetriGameWithTransits game, InvalidPartitionException ivpe) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.partitioned, ivpe);
    }

    public static boolean isInValidPartitioned(PetriGameWithTransits game) {
        if (ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.partitioned)) {
            return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.partitioned, InvalidPartitionException.class) != null;
        }
        return false;
    }

    public static InvalidPartitionException getInValidPartitioned(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.partitioned, InvalidPartitionException.class);
    }

    // $$ one environment player
    public static void removeOneEnvPlayer(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().removeExtension(game, AdamPGWTExtensions.oneEnv);
    }

    public static boolean checkedOneEnvPlayer(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.oneEnv);
    }

    public static boolean thereIsOneEnvPlayer(PetriGameWithTransits game) {
        if (ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.oneEnv)) {
            return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.oneEnv, MoreThanOneEnvironmentPlayerException.class) == null;
        }
        return false;
    }

    public static MoreThanOneEnvironmentPlayerException getOneEnvPlayer(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.oneEnv, MoreThanOneEnvironmentPlayerException.class);
    }

    public static void setOneEnvPlayer(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.oneEnv, null);
    }

    public static void setOneEnvPlayer(PetriGameWithTransits game, MoreThanOneEnvironmentPlayerException mtoepe) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.oneEnv, mtoepe);
    }

    // $$ one system player
    public static void removeOneSysPlayer(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().removeExtension(game, AdamPGWTExtensions.oneSystem);
    }

    public static boolean checkedOneSysPlayer(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.oneSystem);
    }

    public static boolean thereIsOneSysPlayer(PetriGameWithTransits game) {
        if (ExtensionManagement.getInstance().hasExtension(game, AdamPGWTExtensions.oneSystem)) {
            return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.oneSystem, MoreThanOneSystemPlayerException.class) == null;
        }
        return false;
    }

    public static MoreThanOneSystemPlayerException getOneSysPlayer(PetriGameWithTransits game) {
        return ExtensionManagement.getInstance().getExtension(game, AdamPGWTExtensions.oneSystem, MoreThanOneSystemPlayerException.class);
    }

    public static void setOneSysPlayer(PetriGameWithTransits game) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.oneSystem, null);
    }

    public static void setOneSysPlayer(PetriGameWithTransits game, MoreThanOneSystemPlayerException mtoepe) {
        ExtensionManagement.getInstance().putExtension(game, AdamPGWTExtensions.oneSystem, mtoepe);
    }

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
