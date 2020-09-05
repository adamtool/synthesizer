package uniolunisaar.adam.logic.pg.partitioning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotCoveredBySInvariants;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;

/**
 *
 * @author Manuel Gieseking
 */
public class PartitionerInvariants {

    public static void doIt(PetriGame game) throws NoSuitableDistributionFoundException {
        if (!hasDistribution(game)) {
            Logger.getInstance().addMessage("Calculating partition of places ...");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().start(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Logger.getInstance().addMessage("Calculating place invariants ...");
            Set<List<Integer>> invariants = InvariantCalculator.calcSInvariants(game, InvariantCalculator.InvariantAlgorithm.PIPE);
//            System.out.println(invariants.toString());
            Logger.getInstance().addMessage("... calculation of place invariants done.");
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
            Benchmarks.getInstance().stop(Benchmarks.Parts.INVARIANTS);
            // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 

            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            if (InvariantCalculator.coveredBySInvariants(game, invariants) == null) {
                throw new NotCoveredBySInvariants(tokencount, convert(game, invariants), true);
            }

//            System.out.println("All invariants");
//            System.out.println(convert(net, invariants).toString());
//            System.out.println(invariants.toString());
            invariants = getInvariantOneEquations(game, invariants);
//            System.out.println("equal to 0ine");
//            System.out.println(convert(net, invariants).toString());
            if (InvariantCalculator.coveredBySInvariants(game, invariants) == null) {
                throw new NotCoveredBySInvariants(tokencount, convert(game, invariants), false);
            }
            List<Set<Place>> invs = convert(game, invariants);
//            System.out.println("equal to 0ine");
//            System.out.println(invs.toString());

//            List<Set<Place>> tokenFlowTrees = getMergedTokenFlowTrees(net, invs, tokencount);
            List<Set<Place>> tokenFlowTrees = getFilledTokenFlowTrees(game, invs, tokencount);
            // add token ids
            int count = 0;
            for (Set<Place> set : tokenFlowTrees) {
                ++count;
                if (set.isEmpty()) { // no env case
                    --count;
                }
                for (Place place : set) {
                    if (game.isEnvironment(place)) {
                        --count;
                        break; // it's the env tokentree, do it at the end
                    } else {
                        game.setPartition(place, count);
                    }
                }
            }
            Logger.getInstance().addMessage("... done calculation of partition of places.");
        } else {
            Logger.getInstance().addMessage("Using the annotated partition of places.");
        }
        for (Place p : game.getPlaces()) { // to simplify the annotations for the user (env automatically gets 0)
            if (game.isEnvironment(p)) {
                game.setPartition(p, 0);
            }
        }
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
//            boolean goodInv = true;
            for (Iterator<Place> it = net.getPlaces().iterator(); it.hasNext();) {
                Place place = it.next();
                int val = inv.get(count++);
//                if (val == 1) {
                if (val > 0) {
                    newInv.add(place);
//                } else if (val > 1) {
//                    goodInv = false;
//                    break;
                }
            }
//            if (goodInv) {
            invs.add(newInv);
//            }
        }
        return invs;
    }

    static Set<List<Integer>> getInvariantOneEquations(PetriNet net, Set<List<Integer>> invariants) {
        Set<List<Integer>> ret = new HashSet<>();
        // filter only those with coefficents 0 and 1
        for (List<Integer> inv : invariants) {
            boolean add = true;
            for (Integer val : inv) {
                if (val != 1 && val != 0) {
                    add = false;
                    break;
                }
            }
            if (add) {
                ret.add(inv);
            }
        }
        // get only those which equations is equal to 1 (only one token is on the way through the net)
        invariants = new HashSet<>(ret);
        ret.clear();
        for (List<Integer> inv : invariants) {
            Marking m = net.getInitialMarking();
            int n0 = 0;
            int counter = 0;
            for (Iterator<Place> it = net.getPlaces().iterator(); it.hasNext();) {
                Place place = it.next();
                n0 += m.getToken(place).getValue() * inv.get(counter++);
            }
            if (n0 == 1) {
                ret.add(inv);
            }
        }
        return ret;
    }

    private static Pair<Set<Place>, Set<Place>> getEnviromentInvariant(PetriGame game, List<Set<Place>> invs) throws NoSuitableDistributionFoundException {
        // find enviroment invariant to give a hint to the modelling person in the case
        // the env and sys places had not been annotated appropriately
        int nb_envPlaces = 0;
        for (Place place : game.getPlaces()) {
            if (game.isEnvironment(place)) {
                ++nb_envPlaces;
            }
        }
        if (nb_envPlaces == 0) {
            return null;
        }
        for (Set<Place> inv : invs) {
            Set<Place> envInv = new HashSet<>();
            for (Place place : inv) {
                if (game.isEnvironment(place)) {
                    envInv.add(place);
                }
            }
            if (envInv.size() == nb_envPlaces) {
                return new Pair<>(inv, envInv);
            }
        }
        throw new NoSuitableDistributionFoundException("You didn't partitioned the places correctly into environment and system places. There is no invariant for all"
                + " environment places. Thus, maybe you let a token change from the environment to a system player or vice versa. These are the invariants with an"
                + " equation equal to one: " + invs.toString());
    }

    /**
     * starts with the invariant with the most places and than adds as trees for
     * each other invariant only the places which not have already been
     * considered. At the end merges those, which can be merged. Problem:
     * compare safety/ndet/nondet2withGamestrat one could have chosen the wrong
     * invs first, such that we don't end up by number of invariants we want
     * (tokencount).
     *
     */
    private static List<Set<Place>> getMergedTokenFlowTrees(PetriGame game, List<Set<Place>> invs, long tokencount) throws NoSuitableDistributionFoundException {
        List<Set<Place>> tokenFlowTrees = getTokenFlowTrees(game, invs);
        System.out.println(tokenFlowTrees.toString());
        // merge possibly fragmented tokenflowtrees
        while (tokenFlowTrees.size() > tokencount) {
            tokenFlowTrees.sort(Comparator.comparing(Set::size));
            Set<Place> merge = findMerge(tokenFlowTrees, invs);
            if (merge == null) {
                throw new NoSuitableDistributionFoundException(tokencount, invs);
            }
            tokenFlowTrees.add(merge);
        }
        System.out.println("merged");
        System.out.println(tokenFlowTrees.toString());
        return tokenFlowTrees;
    }

    /**
     * starts with the invariant with the most places and than adds as trees for
     * each other invariant only the places which not have already been
     * considered. Problem: compare safety/ndet/nondet2withGamestrat one could
     * have chosen the wrong invs first, such that we don't end up by number of
     * invariants we want (tokencount)
     *
     * @param game
     * @param invs
     * @return
     * @throws NoSuitableDistributionFoundException
     */
    private static List<Set<Place>> getTokenFlowTrees(PetriGame game, List<Set<Place>> invs) throws NoSuitableDistributionFoundException {
        List<Set<Place>> ret = new ArrayList<>();
        Pair<Set<Place>, Set<Place>> pairEnv = getEnviromentInvariant(game, invs);
        if (pairEnv == null) { // no env places exists
            ret.add(new HashSet<>());
        } else {
//            invs.remove(pairEnv.getFirst());
            ret.add(pairEnv.getSecond());
        }
        Set<Place> considered = pairEnv == null ? new HashSet<>() : new HashSet<>(pairEnv.getSecond());
        invs.sort(Comparator.comparing(Set::size));
        for (int i = invs.size() - 1; i >= 0; --i) {
            Set<Place> inv = invs.get(i);
            Set<Place> tokenTree = new HashSet<>();
            for (Place place : inv) {
                if (!considered.contains(place)) {
                    tokenTree.add(place);
                    considered.add(place);
                }
            }
            if (!tokenTree.isEmpty()) {
                ret.add(tokenTree);
            }
        }
        return ret;
    }

    private static List<Set<Place>> getFilledTokenFlowTrees(PetriGame game, List<Set<Place>> invs, long tokencount) throws NoSuitableDistributionFoundException {
        List<Set<Place>> tokenFlowTrees = getTokenFlowTrees2(game, invs);
//        System.out.println(tokenFlowTrees.toString());

        Set<Place> considered = new HashSet<>();
        for (Set<Place> tokenFlowTree : tokenFlowTrees) {
            considered.addAll(tokenFlowTree);
        }

        // fill up possibly omitted tokenflowtrees
        while (tokenFlowTrees.size() < tokencount) {
            for (Set<Place> inv : invs) {
                Set<Place> tokenTree = new HashSet<>();
                for (Place place : inv) {
                    if (!considered.contains(place)) {
                        tokenTree.add(place);
                    }
                }
                if (!tokenTree.isEmpty()) {
                    tokenFlowTrees.add(tokenTree);
                    considered.addAll(tokenTree);
                }
            }
        }

        // possibly fill further up, if they share the same place to get it disjunct
        if (considered.size() != game.getPlaces().size()) {
            for (Place place : game.getPlaces()) {
                if (!considered.contains(place)) {
                    for (Set<Place> inv : invs) {
                        if (!inv.contains(place)) {
                            continue;
                        }
                        Set<Place> tokenTree = new HashSet<>();
                        for (Place p : inv) {
                            if (!considered.contains(p)) {
                                tokenTree.add(p);
                            }
                        }
                        if (!tokenTree.isEmpty()) {
                            tokenFlowTrees.add(tokenTree);
                            considered.addAll(tokenTree);
                            break;
                        }
                    }
                }
            }
            Logger.getInstance().addWarning(
                    "Used more partitions than possible visible token. "
                    + "This could due to two token using the same place exclusive to different times."
                    + " Tokencount= " + tokencount
                    + " Partitions= " + tokenFlowTrees.size());
        }
//        System.out.println("filled up");
//        System.out.println(tokenFlowTrees.toString());
        return tokenFlowTrees;
    }

    /**
     * Choses invariants from those with the most places to those with the least
     * one and only takes those which have not place which is has already been
     * considered. Problem: cp. forallreachability/burglar/burglar.apt, when two
     * invariants have a place in common, it is totally OK to put it to the one
     * or the other tree.
     *
     * @param game
     * @param invs
     * @return
     * @throws NoSuitableDistributionFoundException
     */
    private static List<Set<Place>> getTokenFlowTrees2(PetriGame game, List<Set<Place>> invs) throws NoSuitableDistributionFoundException {
        List<Set<Place>> ret = new ArrayList<>();
        Pair<Set<Place>, Set<Place>> pairEnv = getEnviromentInvariant(game, invs);
        if (pairEnv == null) { // no env places exists
            ret.add(new HashSet<>());
        } else {
            invs.remove(pairEnv.getFirst());
            ret.add(pairEnv.getSecond());
        }
        Set<Place> considered = pairEnv == null ? new HashSet<>() : new HashSet<>(pairEnv.getSecond());
        invs.sort(Comparator.comparing(Set::size));
        for (int i = invs.size() - 1; i >= 0; --i) {
            Set<Place> inv = invs.get(i);
            Set<Place> tokenTree = new HashSet<>();
            for (Place place : inv) {
                if (!considered.contains(place)) {
                    tokenTree.add(place);
                } else {
                    tokenTree.clear();
                    break;
                }
            }
            if (!tokenTree.isEmpty()) {
                considered.addAll(tokenTree);
                ret.add(tokenTree);
            }
        }
        return ret;
    }

    private static Set<Place> findMerge(List<Set<Place>> tokenFlowTrees, List<Set<Place>> invs) {
        for (Set<Place> tree : tokenFlowTrees) {
            for (Set<Place> tree2 : tokenFlowTrees) {
                if (tree != tree2) {
                    Set<Place> merge = new HashSet<>(tree);
                    merge.addAll(tree2);
                    if (subsetOfAnInv(merge, invs)) {
                        tokenFlowTrees.remove(tree);
                        tokenFlowTrees.remove(tree2);
                        return merge;
                    }
                }
            }
        }
        return null;
    }

    private static boolean subsetOfAnInv(Set<Place> merge, List<Set<Place>> invs) {
        for (Set<Place> inv : invs) {
            if (inv.containsAll(merge)) {
                return true;
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
