package uniolunisaar.adam.logic.pg.partitioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;

/**
 *
 * @author Manuel Gieseking
 */
public class DisjunctInvariantCalculator {

    private final PetriGame game;

    public DisjunctInvariantCalculator(PetriGame game) {
        this.game = game;
    }

    /**
     * Collects all invariants without any env place within and with weight less
     * equal 1 on every place. Furthermore, all invariants with more than one
     * initial token within are deleted.
     *
     * @param invariants
     */
    private List<Set<Place>> getSuitableInvariants2(Set<List<Integer>> invariants) {
        Marking init = game.getInitialMarking();
        List<Set<Place>> ret = new ArrayList<>();
        for (List<Integer> inv : invariants) {
            int count = 0;
            boolean isInv = true;
            Set<Place> sinv = new HashSet<>();
            int initialTokenCounter = 0;
            for (Iterator<Place> it = game.getPlaces().iterator(); it.hasNext();) {
                Place place = it.next();
                int val = inv.get(count++);
                if (val > 1) {
                    isInv = false;
                    break;
                } else if (val == 1) {
                    if (init.getToken(place).getValue() > 0) {
                        initialTokenCounter++;
                    }
                    if (game.isEnvironment(place) || initialTokenCounter > 1) {
                        isInv = false;
                        break;
                    } else {
                        sinv.add(place);
                    }
                }
            }
            if (isInv) {
                ret.add(sinv);
            }
        }
        System.out.println(ret.toString());
        return ret;
    }

    /**
     * Choses maxTokencount many invariants, such that uniting the chosen
     * invariants lead to all places. So the chosen invariants cover all places.
     */
    public void addToken2Places() {
        System.out.println("Start invariant calculation ...");
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game, InvariantCalculator.InvariantAlgorithm.FARKAS);
        System.out.println("... done.");
        System.out.println("Filtering suitable invariants ...");
        List<Set<Place>> invs = getSuitableInvariants2(invariants);
        System.out.println("... done.");
        System.out.println("Chose desired invariants ...");
        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        int nbEnvPlaces = 0;
        for (Place p : game.getPlaces()) {
            if (game.isEnvironment(p)) {
                ++nbEnvPlaces;
            }
        }
        List<Set<Place>> desired = new ArrayList<>();
        boolean succ = calcDesiredInvariants(invs, desired, 0, tokencount, nbEnvPlaces);
        System.out.println("... done.");

        if (!succ) {
            throw new RuntimeException("Should not appear. Not the desired invariants found.\n"
                    + "Invariants: " + invariants.toString() + "\n"
                    + "Desired: " + desired.toString());
        }
        addToken2Places(desired, tokencount, nbEnvPlaces);
    }

    /**
     * If one place is listed in more than one invariant, only the last one is
     * taking for splitting the places of the net.
     *
     * @param splitted
     */
    private void addToken2Places(List<Set<Place>> splitted, long tokencount, int nbEnvPlaces) {
        Set<Place> alreadyConsidered = new HashSet<>();
        int count = 1;
        for (Set<Place> inv : splitted) {
//            int counter = (game.isConcurrencyPreserving()) ? 0 : 1;
            for (Place place : inv) {
                if (!alreadyConsidered.contains(place)) {
//                    place.putExtension("id", counter++);
                    if (game.hasPartition(place)) {
                        System.out.println("%%%%%%%%%%%%%%%%%%%%%  Token for place " + place + " already existent.");
                    }
                    game.setPartition(place, count);
                    alreadyConsidered.add(place);
                }
            }
            ++count;
        }

        if (tokencount != count) {
            throw new RuntimeException("TOKENCOUNT " + tokencount + " vs." + count);
        }
        if (alreadyConsidered.size() != game.getPlaces().size() - nbEnvPlaces) {
            System.out.println("TOKENCOUNT " + tokencount);
            throw new RuntimeException("Considered: " + (alreadyConsidered.size() + "+" + nbEnvPlaces)
                    + " but " + game.getPlaces().size() + " places.");
        }

//        count = (game.isConcurrencyPreserving()) ? 0 : 1;
        for (Place place : game.getPlaces()) {
//            place.putExtension("id", count++);
            if (game.isEnvironment(place)) {
                game.setPartition(place, 0);
            }
        }
    }

    private boolean calcDesiredInvariants(List<Set<Place>> invs, List<Set<Place>> desired, int start, long tokencount, int nbEnvPlaces) {
        if (desired.size() == tokencount - 1) {
            Set<Place> considered = new HashSet<>();
            for (Set<Place> set : desired) {
                considered.addAll(set);
            }
            return considered.size() == game.getPlaces().size() - nbEnvPlaces;
        }
        for (int i = start; i < invs.size(); ++i) {
            Set<Place> set = invs.get(i);
            if (desired.containsAll(set)) {
                continue;
            }
            desired.add(set);
            boolean succ = calcDesiredInvariants(invs, desired, i + 1, tokencount, nbEnvPlaces);
            if (succ) {
                return true;
            } else {
                desired.remove(set);
            }
        }
        return false;
    }

    /**
     * old
     *
     * @return
     */
    public List<Set<Place>> calculate() {
        System.out.println("Start invariant calculation ...");
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game, InvariantCalculator.InvariantAlgorithm.FARKAS);
        System.out.println("... done.");
//        System.out.println(game.getNet().getName());
//        System.out.println(invariants.toString());
//        Set<Map<Place, Integer>> invsMap = InvariantCalculator.getMapping(net.getPlaces(), invariants);
        System.out.println("Start invariant disjunction calculation... ");

        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        List<Set<Place>> invs = getDisjunctInvariants2(invariants, tokencount);
        System.out.println("... done.");

//        List<Set<Place>> invs = getDisjunctInvariants(invsMap);
//        List<Set<Place>> invs = getSuitableInvariants(invsMap);
//        System.out.println(invs.size());
//        System.out.println(invs.toString());
        return invs;
    }

    /**
     * old
     */
    public void addTokenIDToPlaces() {
        System.out.println("Start invariant calculation ...");
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game, InvariantCalculator.InvariantAlgorithm.FARKAS);
        System.out.println("... done.");
        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
        int nbEnvPlaces = 0;
        for (Place p : game.getPlaces()) {
            if (game.isEnvironment(p)) {
                ++nbEnvPlaces;
            }
        }
        addDisjunctPlace2TokenMapping(invariants, tokencount, nbEnvPlaces);
    }

    /**
     * old
     *
     * @param invariants
     */
    private void addDisjunctPlace2TokenMapping(Set<List<Integer>> invariants, long tokencount, int nbEnvPlaces) {
        List<Set<Place>> invs = getSuitableInvariants2(invariants);
        addToken2Places(invs, tokencount, nbEnvPlaces);
    }

    /**
     * old
     *
     * Precondition: All env places set and no sys place set is an invariant ...
     *
     * @param invs
     * @return
     */
    private List<Set<Place>> getDisjunctInvariants2(Set<List<Integer>> invs, long tokencount) {
        Set<Place> envPlaces = new HashSet<>();
        for (Place p : game.getPlaces()) {
            if (game.isEnvironment(p)) {
                envPlaces.add(p);
            }
        }
        List<Set<Place>> ret = new ArrayList<>();
        // Place to position mapping
        Map<Place, Integer> map = new HashMap<>();
        int count = 0;
        for (Iterator<Place> it = game.getPlaces().iterator(); it.hasNext();) {
            Place p = it.next();
            map.put(p, count++);
        }

        // initialMarking place set
        Marking initMarking = game.getInitialMarking();
        Set<Place> marking = new HashSet<>();
        for (Place place : game.getPlaces()) {
            if (initMarking.getToken(place).getValue() > 0) {
                marking.add(place);
            }
        }

        for (List<Integer> inv : invs) {
            // count nb of env places in this invariant
            int nb_envPlaces = 0;
            for (Place p : envPlaces) {
                nb_envPlaces += inv.get(map.get(p));
            }
            // count nb of initial token in this invariant
            int nb_initialToken = 0;
            for (Place p : marking) {
                nb_initialToken += inv.get(map.get(p));
            }
            // if no env place and exactly one initial place is in the invariant
            // or if not concurrency preserving, then there don't need to be an
            // initial token within            
            boolean concurrencyPreserving = game.getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
            if (nb_envPlaces == 0 && nb_initialToken == 1 || (!concurrencyPreserving && nb_initialToken == 0)) {
                Set<Place> invSet = new HashSet<>();
                boolean notInv = false;
                for (Place place : game.getPlaces()) {
                    int val = inv.get(map.get(place));
                    if (val > 1) {
                        notInv = true;
                        break;
                    } else if (val == 1) {
                        invSet.add(place);
                    }
                }
                // add only those who has no values greater than one within
                if (!notInv) {
                    ret.add(invSet);
                }
            }
        }

        System.out.println(ret.toString());
        // if preprocessing step already delivers the right number of invariants
        // they already should be disjunct and the correct ones
        if (ret.size() == tokencount - 1) {
            //todo: delete if shure
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%% ATTENTION: possibly not disjunct!");
            // add the env invariant
            ret.add(envPlaces);
            return ret;
        }

        // todo: attention should not be enough to chose everytime the first one
        // or prove that it is enough ...
        List<Set<Place>> considered = new ArrayList<>();
//        while (true) {
        List<Set<Place>> dis = ret;
        for (int i = 1; i < tokencount; ++i) {
            Set<Place> inv = dis.get(0);
            considered.add(inv);
            dis = getAllDisjunctInvariants(inv, dis);
        }
//        }

        // add the env invariant
        considered.add(envPlaces);
        System.out.println("the considered ones");
        System.out.println(considered.toString());
        return considered;
    }

    /**
     * old
     *
     * @param inv
     * @param invs
     * @return
     */
    private List<Set<Place>> getAllDisjunctInvariants(Set<Place> inv, List<Set<Place>> invs) {
        List<Set<Place>> dis = new ArrayList<>();
        for (Set<Place> invariant : invs) {
            boolean disjunct = true;
            for (Place place : invariant) {
                if (inv.contains(place)) {
                    disjunct = false;
                    break;
                }
            }
            if (disjunct) {
                dis.add(invariant);
            }
        }
        return dis;
    }

    /**
     * chose for every token one invariant with the initial place for this token
     * within and the environmental s-invariant.
     *
     * todo: use predicates (Java 8) or at least make it nicer
     */
    private List<Set<Place>> getSuitableInvariants(Set<Map<Place, Integer>> invs) {
        Set<Map<Place, Integer>> suitInvs = new HashSet<>();
        // filter enviroment inv and delete all others with an environment place
        // within
        Set<Map<Place, Integer>> del = new HashSet<>();
        for (Map<Place, Integer> inv : invs) {
            boolean all = true;
            boolean first = true;
            for (Place place : inv.keySet()) {
                Integer val = inv.get(place);
                // todo: delete after proven (clerks2 often produce a 2) so 
                // either the algorithm is wrong or it is not provable.
                // why should it not be ok, to visit a place more than once?
//                if (val > 1) {
//                    del.add(inv);
//                    break;
//                    throw new RuntimeException("Should not appear. But proof that all"
//                            + " invariants are at most 1");
//                } else
                if (val > 0) {
                    if (game.isEnvironment(place)) {
                        if (!all) {
                            del.add(inv);
                            break;
                        }
                    } else {
                        if (all && !first) {
                            del.add(inv);
                            break;
                        }
                        all = false;
                    }
                }
            }
            if (all) {
                suitInvs.add(inv);
            }
        }
        invs.removeAll(del);

        // chose for every token one invariant with the initial place for this
        // token within
        Marking initMarking = game.getInitialMarking();
        for (Place place : game.getPlaces()) {
            if (initMarking.getToken(place).getValue() > 0) {
                System.out.println("found marking");
                for (Map<Place, Integer> inv : invs) {
                    if (inv.get(place) > 0) {
                        suitInvs.add(inv);
                        break;
                    }
                }
            }
        }

        // Return list of places
        List<Set<Place>> ret = new ArrayList<>();
        for (Map<Place, Integer> map : suitInvs) {
            Set<Place> inv = new HashSet<>();
            for (Place place : map.keySet()) {
                Integer val = map.get(place);
                if (val > 0) {
                    inv.add(place);
                }
            }
            ret.add(inv);
        }
        return ret;
    }

    /**
     * takes the environmental s-invariant and the disjunct invariants
     *
     * todo: use predicates (Java 8) or at least make it nicer
     */
    private List<Set<Place>> getDisjunctInvariants(Set<Map<Place, Integer>> invs, long tokencount) {
        Set<Map<Place, Integer>> disInvs = new HashSet<>();
        // filter enviroment inv and delete all others with an environment place
        // within
        Set<Map<Place, Integer>> del = new HashSet<>();
        for (Map<Place, Integer> inv : invs) {
            boolean maybeEnv = true;
            boolean maybeOther = true;
            for (Place place : inv.keySet()) {
                Integer val = inv.get(place);
                // todo: delete after proven (clerks2 often produce a 2) so 
                // either the algorithm is wrong or it is not provable.
                // why should it not be ok, to visit a place more than once?
//                if (val > 1) {
//                    del.add(inv);
//                    break;
//                    throw new RuntimeException("Should not appear. But proof that all"
//                            + " invariants are at most 1");
//                } else
                if (val > 0) {
                    if (game.isEnvironment(place)) {
                        maybeOther = false;
                        if (!maybeEnv) {
                            del.add(inv);
                            break;
                        }
                    } else {
                        if (!maybeOther) {
                            del.add(inv);
                            break;
                        }
                        maybeEnv = false;
                    }
                }
            }
            if (maybeEnv) {
                disInvs.add(inv);
            }
        }
        invs.removeAll(del);

        // collected only the disjunct ones
        for (Map<Place, Integer> inv : invs) {
            Set<Map<Place, Integer>> buf = new HashSet<>();
            for (Map<Place, Integer> inv2 : invs) {
                boolean disjunct = true;
                for (Place place : inv2.keySet()) {
                    if (inv2.get(place) > 0 && inv.get(place) > 0) {
                        disjunct = false;
                        break;
                    }
                }
                if (disjunct) {
                    buf.add(inv2);
                }
            }
            if (buf.size() == tokencount - 1) {
//                Set<Place> pls = new HashSet<>();
//                // todo:  mach ordentlich
//                for (Map<Place, Integer> map : buf) {
//                    for (Place place : map.keySet()) {
//                        if (map.get(place) > 0) {
//                            pls.add(place);
//                        }
//                    }
//                }
//                for (Place place : inv.keySet()) {
//                    if (inv.get(place) > 0) {
//                        pls.add(place);
//                    }
//                }
//                for (Map<Place, Integer> map : disInvs) {
//                    for (Place place : map.keySet()) {
//                        if (map.get(place) > 0) {
//                            pls.add(place);
//                        }
//                    }
//                }
//                // todo: hier ende
//                if (pls.size() == net.getPlaces().size()) {
                disInvs.add(inv);
                disInvs.addAll(buf);
                break;
//                }
            }
        }

        // Return list of places
        List<Set<Place>> ret = new ArrayList<>();
        for (Map<Place, Integer> map : disInvs) {
            Set<Place> inv = new HashSet<>();
            for (Place place : map.keySet()) {
                Integer val = map.get(place);
                if (val > 0) {
                    inv.add(place);
                }
            }
            ret.add(inv);
        }
        return ret;
    }

    /**
     * todo: use predicates (Java 8) or at least make it nicer
     *
     * @param invs
     * @param inv
     * @return
     */
    private boolean disjunct(Set<Map<Place, Integer>> invs, Map<Place, Integer> inv) {
        for (Map<Place, Integer> map : invs) {
            for (Place place : map.keySet()) {
//                System.out.println("bla");
//                System.out.println(map.get(place));
//                System.out.println(inv.get(place));
                if (map.get(place) > 0 && inv.get(place) > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
