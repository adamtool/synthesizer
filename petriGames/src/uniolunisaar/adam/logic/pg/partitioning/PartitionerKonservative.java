package uniolunisaar.adam.logic.pg.partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotCoveredBySInvariants;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.benchmarks.Benchmarks;

/**
 *
 * @author Manuel Gieseking
 */
public class PartitionerKonservative {

    public static void doIt(PetriGame game) throws NoSuitableDistributionFoundException {
        if (!hasDistribution(game)) {
            Logger.getInstance().addMessage("Calculating partition of places ...");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().start(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Logger.getInstance().addMessage("Calculating place invariants ...");
            Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game, InvariantCalculator.InvariantAlgorithm.FARKAS);
            Logger.getInstance().addMessage("... calculation of place invariants done.");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().stop(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 

            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            if (InvariantCalculator.coveredBySInvariants(game, invariants) == null) {
                throw new NotCoveredBySInvariants(tokencount, convert(game, invariants), true);
            }
            invariants = getSuitableInvariants(game, invariants);
            if (InvariantCalculator.coveredBySInvariants(game, invariants) == null) {
                throw new NotCoveredBySInvariants(tokencount, convert(game, invariants), false);
            }
            List<Set<Place>> invs = convert(game, invariants);
            invs = deleteEnvInvariants(invs, game);

            List<Set<Place>> desired = new ArrayList<>();
            Logger.getInstance().addMessage("Choose the desired invariants ...");
            int nbEnvPlaces = 0;
            for (Place p : game.getPlaces()) {
                if (game.isEnvironment(p)) {
                    ++nbEnvPlaces;
                }
            }
            boolean succ = calcDesiredInvariants(game, invs, desired, 0, tokencount, nbEnvPlaces);
            Logger.getInstance().addMessage("... choosing the desired invariants done.");
            if (!succ) {
                throw new NoSuitableDistributionFoundException(tokencount, convert(game, invariants));
            }

            // add token ids
            int count = 1;
            for (Set<Place> set : desired) {
                boolean env = false;
                for (Place place : set) {
                    if (game.isEnvironment(place)) {
                        env = true;
                        break;
                        //place.putExtension("token", 0); // todo: could be to greedy when not the right invariant for the env places had been found
                    }//else {
                    game.setPartition(place, count);
                    //  }
                }
                if (!env) {
                    ++count;
                }
                if (count == tokencount) {
                    //todo: attention could be to greedy
                    break;
                }
            }
            Logger.getInstance().addMessage("... done calculation of partition of places.");
        } else {
            Logger.getInstance().addMessage("Using the annotated partition of places.");
        }
        for (Place p : game.getPlaces()) {
            if (game.isEnvironment(p)) {
                game.setPartition(p, 0);
            }
        }
    }

    private static List<Set<Place>> deleteEnvInvariants(List<Set<Place>> invariants, PetriGame game) {
        List<Set<Place>> invs = new ArrayList<>();
        for (Set<Place> set : invariants) {
            boolean goodInv = true;
            for (Place place : set) {
                if (game.isEnvironment(place)) {
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
     * Converts the list of integers to a set of places, where every place in
     * the result is containt which had a 1 in the original list. Attention it
     * additionally deletes invariants with a weight greater than one.
     *
     * @param net
     * @param invariants
     * @return
     */
    static List<Set<Place>> convert(PetriNet net, Set<List<Integer>> invariants) {
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

    private static boolean calcDesiredInvariants(PetriNet net, List<Set<Place>> invs, List<Set<Place>> desired, int start, long tokencount, int nbEnvPlaces) {
        if (desired.size() == tokencount - 1) {
            // is the net covered?
            Set<Place> considered = new HashSet<>();
            for (Set<Place> set : desired) {
                considered.addAll(set);
            }
            return considered.size() == net.getPlaces().size() - nbEnvPlaces;
        }
        for (int i = start; i < invs.size(); ++i) {
            Set<Place> inv = invs.get(i);
            desired.add(inv);
            if (calcDesiredInvariants(net, invs, desired, i + 1, tokencount, nbEnvPlaces)) {
                return true;
            } else {
                desired.remove(inv);
            }
        }
        return false;
    }

    private static boolean hasDistribution(PetriGame game) throws NoSuitableDistributionFoundException {
        boolean isAnnotated = false;
        for (Place p : game.getPlaces()) {
            if (game.hasPartition(p)) {
                isAnnotated = true;
                break;
            }
        }
        if (!isAnnotated) {
            return false;
        }

        //todo: all comments are old version, before cavarti
//        int maxToken = 0;
        for (Place place : game.getPlaces()) {
            if (!game.hasPartition(place) && !game.isEnvironment(place)) {
                throw new NoSuitableDistributionFoundException(place);
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
