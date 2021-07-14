package uniolunisaar.adam.logic.transformers;

import java.util.ArrayList;
import java.util.List;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoCalculatorProvidedException;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;

/**
 * Turns a safe Petri game with transits with one environment player into a safe
 * Petri game with transits where there is no mixed communication, i.e., there
 * are no system places having a transition with the environment and a
 * transition only containing system places in its postset.
 *
 * @author Manuel Gieseking
 */
public class PGWT2PGWTN_NMC {

    private static final String PREFIX = "com-";
    private static final String ACT = "act-";

    public static List<Pair<Place, List<Transition>>> getMixedCommunicationPlaceWithEnvTransitions(PetriGameWithTransits pgwt) {
        List<Pair<Place, List<Transition>>> mcPlaces = new ArrayList<>();
        for (Place place : pgwt.getPlaces()) {
            if (pgwt.isSystem(place)) { // all system places
                boolean hasOnlySystem = false;
                List<Transition> envTransitions = new ArrayList<>();
                for (Transition transition : place.getPostset()) { // check postset for mixed transitions  
                    boolean allSystem = true;
                    for (Place p2 : transition.getPreset()) {
                        if (pgwt.isEnvironment(p2)) {
                            envTransitions.add(transition);
                            allSystem = false;
                            break;
                        }
                    }
                    if (allSystem) {
                        hasOnlySystem = true;
                    }
                }
                if (hasOnlySystem && !envTransitions.isEmpty()) { // found a mixed one
                    mcPlaces.add(new Pair<>(place, envTransitions));
                }
            }
        }
        return mcPlaces;
    }

    public static PetriGameWithTransits create(PetriGameWithTransits in, boolean withActPlace) {
        Integer maxToken = null;
        try {
            maxToken = ((Long) in.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name())).intValue();
        } catch (NoCalculatorProvidedException ncpe) {
        }
        PetriGameWithTransits out = new PetriGameWithTransits(in);
        List<Pair<Place, List<Transition>>> mcPlaces = getMixedCommunicationPlaceWithEnvTransitions(out);
        System.out.println(out.getName() + ": " + mcPlaces.toString());
        for (Pair<Place, List<Transition>> pair : mcPlaces) {
            Place mcPlace = pair.getFirst();
            // add communication transition for this system place
            Transition t = out.createTransition();
            String newId = PREFIX + t.getId();
            out.rename(t, newId);
            t = out.getTransition(newId);
            Place pCom = out.createPlace();
            newId = PREFIX + pCom.getId();
            out.rename(pCom, newId);
            pCom = out.getPlace(newId);
            if (maxToken != null) {
                out.setPartition(pCom, out.getPartition(mcPlace));
            }
            out.createFlow(mcPlace, t);
            out.createFlow(t, pCom);
            Place act = null;
            if (withActPlace) {
                // create the activation place for the system communication            
                act = out.createPlace();
                newId = ACT + act.getId();
                out.rename(act, newId);
                act = out.getPlace(newId);
                if (maxToken != null) {
                    out.setPartition(act, maxToken++);
                }
                out.createFlow(act, t);
            }
            // for all transition in the postset of the mixed place having an environment
            // place in the preset, add the notification of the environment for the communication
            // and move the real communication to the communication places
            for (Transition transition : pair.getSecond()) {
                // Since we collected only transitions having an env place in its
                // preset, we can here directly 
                // move the edge from the mixed communication place to the new com-place of the system
                out.removeFlow(mcPlace, transition);
                out.createFlow(pCom, transition);

                if (withActPlace) {
                    // now for all env places
                    for (Place place : transition.getPreset()) {
                        if (out.isEnvironment(place)) {       // for the environment places
                            // check whether there is already a communication transition for this env place
                            Transition envCom = null;
                            for (Transition preTransition : place.getPreset()) {
                                if (preTransition.getId().startsWith(PREFIX)) {
                                    envCom = preTransition;
                                    break;
                                }
                            }
                            if (envCom != null) {
                                // add only the edge to the communication place
                                out.createFlow(envCom, act);
                            } else {
                                // create the env communication
                                Place envComPlace = out.createEnvPlace();
                                newId = PREFIX + envComPlace.getId();
                                out.rename(envComPlace, newId);
                                envComPlace = out.getPlace(newId);
                                envCom = out.createTransition();
                                newId = PREFIX + envCom.getId();
                                out.rename(envCom, newId);
                                envCom = out.getTransition(newId);
                                // move all input arcs of the env place to the envCom place
                                for (Flow pre : place.getPresetEdges()) {
                                    out.removeFlow(pre);
                                    out.createFlow(pre.getTransition(), envComPlace);
                                }
                                // move the env com token
                                out.createFlow(envComPlace, envCom);
                                out.createFlow(envCom, place);
                                // activate the sys communication
                                out.createFlow(envCom, act);
                                // check whether the env place was initially marked, thus, mark the communication place
                                Marking init = out.getInitialMarking();
                                long val = init.getToken(place).getValue();
                                if (val > 0) {
                                    init = init.setTokenCount(envComPlace, (int) val);
                                    init = init.setTokenCount(place, 0);
                                }
                                out.setInitialMarking(init);
                            }
                        }
                    }
                }
            }
        }
        return out;
    }
}
