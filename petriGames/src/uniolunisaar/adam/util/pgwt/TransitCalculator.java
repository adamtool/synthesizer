package uniolunisaar.adam.util.pgwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;

/**
 *
 * @author Manuel Gieseking
 */
public class TransitCalculator {

    public static void automaticallyCreateTransitsForTransitlessTransitions(PetriGameWithTransits game) throws CouldNotCalculateException {
        for (Transition t : game.getTransitions()) {
            automaticallyCreateTransitsForTransitlessTransition(game, t);
        }
    }

    public static void automaticallyCreateTransitsForTransitlessTransition(PetriGameWithTransits game, Transition t) throws CouldNotCalculateException {
        if (!game.hasTransit(t)) {
            Place pre;
            Set<Place> postset = new HashSet<>();
            if (t.getPreset().size() == 1) {
                pre = t.getPreset().iterator().next();
                if (game.isEnvironment(pre)) {
                    if (t.getPostset().size() == 1) {
                        Place p2 = t.getPostset().iterator().next();
                        if (game.isEnvironment(p2)) {
                            postset.add(p2);
                        } else {
                            throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
                        }
                    } else {
                        throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
                    }
                } else {
                    for (Place p : t.getPostset()) {
                        if (game.isEnvironment(p)) {
                            throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
                        }
                        postset.add(p);
                    }
                }
                game.createTransit(pre, t, postset.toArray(new Place[postset.size()]));
            } else if (t.getPreset().size() == 2) {
                Iterator<Place> it = t.getPreset().iterator();
                Place p1 = it.next();
                Place p2 = it.next();
                Place env = null;
                Place sys = null;
                if (game.isEnvironment(p1) && !game.isEnvironment(p2)) {
                    env = p1;
                    sys = p2;
                } else if (game.isEnvironment(p2) && !game.isEnvironment(p1)) {
                    env = p2;
                    sys = p1;
                } else {
                    throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
                }

                pre = env;
                for (Place p : t.getPostset()) {
                    if (game.isEnvironment(p)) {
                        postset.add(p);
                    }
                }
                if (postset.isEmpty() && !t.getPostset().isEmpty()) {
                    throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
                }
                game.createTransit(pre, t, postset.toArray(new Place[postset.size()]));
                pre = sys;
                for (Place p : t.getPostset()) {
                    if (!game.isEnvironment(p)) {
                        postset.add(p);
                    }
                }

                game.createTransit(pre, t, postset.toArray(new Place[postset.size()]));
            } else {
                throw new CouldNotCalculateException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
            }
        }
//// Adds for all places im pre as well as im postset which are not mapped to tokenflows new tokenflows (I don't think that I want to have it anymore)
//            for (Place p : t.getPreset()) {
//                boolean found = false;
//                for (Transit tokenFlow : tfl) {
//                    if (tokenFlow.getPresetPlace().equals(p)) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    Transit tf = game.createInitialTransit(game, p, t);
//                    tfl.add(tf);
//                }
//            }
//            for (Place p : t.getPostset()) {
//                boolean found = false;
//                for (Transit tokenFlow : tfl) {
//                    if (tokenFlow.getPostset().contains(p)) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    Transit tf = new Transit(game, t);
//                    tf.addPostsetPlace(p);
//                    tfl.add(tf);
//                }
//            }
//            game.setTokenFlow(t, tfl);
    }

    public static void copyTokenflowsFromGameToStrategy(PetriGameWithTransits game, PetriGameWithTransits strategy) {
//        PetriGame game = solver.getGame();
        for (Transition t : strategy.getTransitions()) {
            Transition tOrig = game.getTransition(t.getLabel());
            if (game.hasTransit(tOrig)) {
                Collection<Transit> tflOrig = game.getTransits(tOrig);
                for (Transit tokenFlow : tflOrig) {
                    Place[] postset = new Place[tokenFlow.getPostset().size()];
                    int i = 0;
                    for (Place place : tokenFlow.getPostset()) {
                        postset[i] = getPlaceByOrigID(game, t.getPostset(), place.getId());
                        ++i;
                    }
                    if (tokenFlow.isInitial()) {
                        strategy.createInitialTransit(t, postset);
                    } else {
                        strategy.createTransit(getPlaceByOrigID(game, t.getPreset(), tokenFlow.getPresetPlace().getId()), t, postset);
                    }
                }
            }
        }
    }

    private static Place getPlaceByOrigID(PetriGameWithTransits game, Set<Place> postset, String id) {
        for (Place place : postset) {
            if (game.getOrigID(place).equals(id)) {
                return place;
            }
        }
        //todo: error
        throw new RuntimeException("ERROR in PG-StratBuilder no suitable origID (should not happen)! (" + id + ")");
    }

}
