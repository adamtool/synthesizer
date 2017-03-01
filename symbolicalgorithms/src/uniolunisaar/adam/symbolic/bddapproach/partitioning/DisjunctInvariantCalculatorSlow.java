package uniolunisaar.adam.symbolic.bddapproach.partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class DisjunctInvariantCalculatorSlow {

    private final BDDPetriGame game;

    public DisjunctInvariantCalculatorSlow(BDDPetriGame game) {
        this.game = game;
    }

    /**
     * Choses maxTokencount many invariants, such that uniting the chosen 
     * invariants lead to all places. So the chosen invariants cover all
     * places.   
     */
    public void addToken2Places() throws NoSuitableDistributionFoundException {
        System.out.println("Start invariant calculation ...");
        Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game.getNet(), InvariantCalculator.InvariantAlgorithm.FARKAS);
        System.out.println("... done.");
        System.out.println("Chose desired invariants ...");
        List<Set<Place>> invs = convert(invariants);
        System.out.println(invs.toString());
        System.out.println("%%%%%%%%%%%%%%");
        List<Set<Place>> desired = new ArrayList<>();
        boolean succ = calcDesiredInvariants(invs, desired, 0);
        System.out.println("... done.");
        if (!succ) {
            throw new NoSuitableDistributionFoundException(game.getTOKENCOUNT(), invs);
        }
        System.out.println(desired.toString());
        addToken2Places(desired);
    }

    /**
     * Converts the list of integers to a set of places, where every 
     * place in the result is containt which had a 1 in the original list.
     * Attention it additionally deletes invariants with a weight greater than 
     * one.
     * 
     * @param invariants
     * @return 
     */
    private List<Set<Place>> convert(Set<List<Integer>> invariants) {
        List<Set<Place>> invs = new ArrayList<>();
        for (List<Integer> inv : invariants) {
            int count = 0;
            Set<Place> newInv = new HashSet<>();
            boolean goodInv = true;
            for (Iterator<Place> it = game.getNet().getPlaces().iterator(); it.hasNext();) {
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

    /**
     * If one place is listed in more than one invariant, only the last one
     * is taking for splitting the places of the net.
     * 
     * @param splitted 
     */
    private void addToken2Places(List<Set<Place>> splitted) {
        Set<Place> alreadyConsidered = new HashSet<>();
        int count = 1;
        for (Set<Place> inv : splitted) {
//            int counter = (game.isConcurrencyPreserving()) ? 0 : 1;

            boolean allConsidered = true;
            for (Place place : inv) {
                if (!alreadyConsidered.contains(place)) {
//                    place.putExtension("id", counter++);                  
                    place.putExtension("token", count);
                    alreadyConsidered.add(place);
                    allConsidered = false;
                } else {
                    System.out.println("blub");
                }
            }
            if (allConsidered) {
                System.out.println("%%%%%%%%%%%%%%%%%%%% TADAAAAAAAAAAAAAAA" + game.getTOKENCOUNT());
            }
            ++count;
        }

        if (game.getTOKENCOUNT() != count) {
            throw new RuntimeException("TOKENCOUNT " + game.getTOKENCOUNT() + " vs." + count);
        }
        if (alreadyConsidered.size() != game.getNet().getPlaces().size() - game.getEnvPlaces().size()) {
            System.out.println("TOKENCOUNT " + game.getTOKENCOUNT());
            throw new RuntimeException("Considered: " + (alreadyConsidered.size() + "+" + game.getEnvPlaces().size())
                    + " but " + game.getNet().getPlaces().size() + " places.");
        }

//        count = (game.isConcurrencyPreserving()) ? 0 : 1;
        for (Place place : game.getEnvPlaces()) {
//            place.putExtension("id", count++);
            place.putExtension("token", 0);
        }
    }

    /**
     * todo: not nice
     * @param desired
     * @param places
     * @return 
     */
    private boolean contains(List<Set<Place>> desired, Set<Place> places) {
        for (Set<Place> set : desired) {
            if (set.containsAll(places)) {
                return true;
            }
            if (places.containsAll(set)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List<Set<Place>> desired, Place p) {
        for (Set<Place> set : desired) {
            if (set.contains(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean calcDesiredInvariants(List<Set<Place>> invs, List<Set<Place>> desired, int start) {
        if (desired.size() == game.getTOKENCOUNT() - 1) {
            Set<Place> considered = new HashSet<>();
            for (Set<Place> set : desired) {
                considered.addAll(set);
            }
            return considered.size() == game.getNet().getPlaces().size() - game.getEnvPlaces().size();
        }
        for (int i = start; i < invs.size(); ++i) {
            Set<Place> inv = invs.get(i);
            Set<Place> desiredPlaces = new HashSet<>();
            for (Place place : inv) {
                if (!place.hasExtension("env") && !contains(desired, place)) {
                    desiredPlaces.add(place);
                }
            }
            if (contains(desired, desiredPlaces)) {
                continue;
            }
            desired.add(desiredPlaces);
            boolean succ = calcDesiredInvariants(invs, desired, i + 1);
            if (succ) {
                return true;
            } else {
                desired.remove(desiredPlaces);
            }
        }
        return false;
    }
}
