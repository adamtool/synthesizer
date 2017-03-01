package uniolunisaar.adam.symbolic.bddapproach.partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotCoveredBySInvariants;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGame;
import uniolunisaar.adam.util.Logger;
import uniolunisaar.adam.util.benchmark.Benchmarks;

/**
 *
 * @author Manuel Gieseking
 */
public class Partitioner {

    public static void doIt(BDDPetriGame game) throws NoSuitableDistributionFoundException {
        if (!hasDistribution(game)) {
            Logger.getInstance().addMessage("Calculating partition of places ...");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().start(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Logger.getInstance().addMessage("Calculating place innvariants ...");
            Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game.getNet(), InvariantCalculator.InvariantAlgorithm.FARKAS);
            Logger.getInstance().addMessage("... calculation of place invariants done.");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().stop(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS            
            if (InvariantCalculator.coveredBySInvariants(game.getNet(), invariants) == null) {
                throw new NotCoveredBySInvariants(game.getTOKENCOUNT(), convert(game.getNet(), invariants), true);
            }
            invariants = getSuitableInvariants(game.getNet(), invariants);
            if (InvariantCalculator.coveredBySInvariants(game.getNet(), invariants) == null) {
                throw new NotCoveredBySInvariants(game.getTOKENCOUNT(), convert(game.getNet(), invariants), false);
            }
            List<Set<Place>> invs = convert(game.getNet(), invariants);
            invs = deleteEnvInvariants(invs);

            List<Set<Place>> desired = new ArrayList<>();
            Logger.getInstance().addMessage("Choose the desired invariants ...");
            boolean succ = calcDesiredInvariants(game, invs, desired, 0);
            Logger.getInstance().addMessage("... choosing the desired invariants done.");
            if (!succ) {
                throw new NoSuitableDistributionFoundException(game.getTOKENCOUNT(), convert(game.getNet(), invariants));
            }

            // add token ids
            for (Place place : game.getEnvPlaces()) {
                place.putExtension("token", 0);
            }
            int count = 1;
            for (Set<Place> set : desired) {
                boolean env = false;
                for (Place place : set) {
                    if (place.hasExtension("env")) {
                        env = true;
                        break;
                    }
                    place.putExtension("token", count);
                }
                if (!env) {
                    ++count;
                }
                if (count == game.getTOKENCOUNT()) {
                    //todo: attention could be to greedy
                    break;
                }
            }
            Logger.getInstance().addMessage("... done calculation of partition of places.");
        } else {
            Logger.getInstance().addMessage("Using the annotated partition of places.");
        }
    }

    private static List<Set<Place>> deleteEnvInvariants(List<Set<Place>> invariants) {
        List<Set<Place>> invs = new ArrayList<>();
        for (Set<Place> set : invariants) {
            boolean goodInv = true;
            for (Place place : set) {
                if (place.hasExtension("env")) {
                    goodInv = false;
                    break;
                }
            }
            if (goodInv) {
                invs.add(set);
            }
        }
        return invs;
    }

    /**
     * Converts the list of integers to a set of places, where every 
     * place in the result is containt which had a 1 in the original list.
     * Attention it additionally deletes invariants with a weight greater than 
     * one.
     * 
     * @param net
     * @param invariants
     * @return 
     */
    private static List<Set<Place>> convert(PetriNet net, Set<List<Integer>> invariants) {
        List<Set<Place>> invs = new ArrayList<>();
        for (List<Integer> inv : invariants) {
            int count = 0;
            Set<Place> newInv = new HashSet<>();
            boolean goodInv = true;
            for (Iterator<Place> it = net.getPlaces().iterator(); it.hasNext();) {
                Place place = it.next();
                int val = inv.get(count++);
                if (val == 1) {
                    newInv.add(place);
                } else if (val > 1) {
                    goodInv = false;
                    break;
                }
            }
            if (goodInv) {
                invs.add(newInv);
            }
        }
        return invs;
    }

    private static Set<List<Integer>> getSuitableInvariants(PetriNet net, Set<List<Integer>> invariants) {
        Set<List<Integer>> ret = new HashSet<>();
        Marking init = net.getInitialMarking();
        for (List<Integer> inv : invariants) {
            int val = 0;
            int count = 0;
            for (Iterator<Place> it = net.getPlaces().iterator(); it.hasNext();) {
                Place place = it.next();
                val += inv.get(count) * init.getToken(place).getValue();
                ++count;
            }
            if (val == 1) {
                ret.add(inv);
            }
        }
        return ret;
    }

    private static boolean calcDesiredInvariants(BDDPetriGame game, List<Set<Place>> invs, List<Set<Place>> desired, int start) {
        if (desired.size() == game.getTOKENCOUNT() - 1) {
            // is the net covered?
            Set<Place> considered = new HashSet<>();
            for (Set<Place> set : desired) {
                considered.addAll(set);
            }
            return considered.size() == game.getNet().getPlaces().size() - game.getEnvPlaces().size();
        }
        for (int i = start; i < invs.size(); ++i) {
            Set<Place> inv = invs.get(i);
            desired.add(inv);
            if (calcDesiredInvariants(game, invs, desired, i + 1)) {
                return true;
            } else {
                desired.remove(inv);
            }
        }
        return false;
    }

    private static boolean hasDistribution(BDDPetriGame game) throws NoSuitableDistributionFoundException {
        PetriNet net = game.getNet();
        boolean isAnnotated = false;
        for (Place p : game.getNet().getPlaces()) {
            if (p.hasExtension("token")) {
                isAnnotated = true;
                break;
            }
        }
        if (!isAnnotated) {
            return false;
        }
        
        //todo: all comments are old version, before cavarti
//        int maxToken = 0;
        for (Place place : net.getPlaces()) {
            if (!place.hasExtension("token") && !place.hasExtension("env")) {
                throw new NoSuitableDistributionFoundException(place);
            }
            if (place.hasExtension("env")) {
                place.putExtension("token", 0);
            }
//            int token = (Integer) place.getExtension("token");
//            if (maxToken < token) {
//                maxToken = token;
//            }
        }
//        if (maxToken != game.getTOKENCOUNT() - 1) {
//            throw new NoSuitableDistributionFoundException(maxToken, game.getTOKENCOUNT());
//        }

        return true;
    }
}
