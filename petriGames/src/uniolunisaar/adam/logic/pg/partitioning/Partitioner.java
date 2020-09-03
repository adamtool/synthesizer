package uniolunisaar.adam.logic.pg.partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.pg.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.tools.Logger;

/**
 *
 *
 * @author Manuel Gieseking
 */
public class Partitioner {

    /**
     *
     * @param game
     * @throws NoSuitableDistributionFoundException
     */
    public static void doIt(PetriGame game) throws NoSuitableDistributionFoundException {
        if (!hasDistribution(game, true)) {
            Logger.getInstance().addMessage("Calculating partition of places ...");

            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            List<Set<Place>> tokenFlowTrees = calcTokenFlowTrees(game);

            if (tokenFlowTrees.size() > tokencount) {
                Logger.getInstance().addWarning("We distributed the net into " + tokenFlowTrees.size() + " partitions."
                        + " Only " + tokencount + " token can be visible together at a certain point in time. This could be necessary, but"
                        + " we could also be to greedy by creating new partitions.");
//                game.setMaxTokenCount(tokenFlowTrees.size());
                MaxTokenCountCalculator calc = (MaxTokenCountCalculator) game.getCalculators().get(CalculatorIDs.MAX_TOKEN_COUNT.name());
                calc.setManuallyFixedTokencount(game, tokenFlowTrees.size());
            }

            // add token ids
            int count = 0;
            for (Set<Place> set : tokenFlowTrees) {
                ++count;
                if (set.isEmpty()) { // for the no env token case
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

    private static List<Set<Place>> calcTokenFlowTrees(PetriGame game) throws NoSuitableDistributionFoundException {
        List<Set<Place>> tokenFlowTrees = new ArrayList<>();

        Set<Place> considered = new HashSet<>();

        Set<Place> env = new HashSet<>();
        for (Place place : game.getPlaces()) { // env should be one tokenflowtree since it is only one token
            if (game.isEnvironment(place)) {
                env.add(place);
                considered.add(place);
            }
        }

        CoverabilityGraph graph = CoverabilityGraph.getReachabilityGraph(game);
        CoverabilityGraphNode init = graph.getInitialNode();
        for (Place place : getPlaces(init.getMarking())) { // for all initially marked places their should be at least one tokenflow tree
            if (!game.isEnvironment(place)) {
                Set<Place> set = new HashSet<>();
                set.add(place);
                considered.add(place);
                tokenFlowTrees.add(set);
            }
        }

        // calculate a list of all possible markings        
        List<Set<Place>> markings = new ArrayList<>();
        for (Iterator<CoverabilityGraphNode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            markings.add(getPlaces(next.getMarking()));
        }

        boolean ret = addNewPlace(game, considered, tokenFlowTrees, markings);
        if (!ret) {
            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            throw new NoSuitableDistributionFoundException(tokencount);
        }

        // had already been calculated, just add at the end to do not do anything more with it
        // For the no env-token case this one is empty, but that is intented
        tokenFlowTrees.add(env);

        return tokenFlowTrees;
    }

    private static boolean addNewPlace(PetriNet net, Set<Place> considered, List<Set<Place>> tokenFlowTrees, List<Set<Place>> markings) {
        // find one not considered
        Place place = null;
        for (Place p : net.getPlaces()) {
            if (!considered.contains(p)) {
                place = p;
                break;
            }
        }
        if (place == null) { // nothing to consider anymore
            return true;
        }
        boolean good = false;
        List<Set<Place>> treesToIterate = new ArrayList<>(tokenFlowTrees);
        for (Set<Place> tree : treesToIterate) {
            tree.add(place);
            if (isValidDistribution(tokenFlowTrees, markings)) {
                considered.add(place);
                good = addNewPlace(net, considered, tokenFlowTrees, markings);
                if (!good) {
                    considered.remove(place);
                    tree.remove(place);
                } else {
                    break;
                }
            } else {
                tree.remove(place);
            }
        }
        if (!good) {
            Set<Place> tree = new HashSet<>();
            tree.add(place);
            considered.add(place);
            tokenFlowTrees.add(tree);
            good = addNewPlace(net, considered, tokenFlowTrees, markings);
            if (!good) {
                considered.remove(place);
                tokenFlowTrees.remove(tree);
                return false;
            }
        }
        return true;
    }

    static boolean isValidDistribution(List<Set<Place>> tokenFlowTrees, List<Set<Place>> markings) {
        for (Set<Place> marking : markings) {
            for (Set<Place> tree : tokenFlowTrees) {
                boolean alreadyContained = false;
                for (Place place : marking) {
                    if (tree.contains(place)) {
                        if (alreadyContained) {
                            return false;
                        }
                        alreadyContained = true;
                    }
                }
            }
        }
        return true;
    }

    private static Set<Place> getPlaces(Marking m) {
        Set<Place> ret = new HashSet<>();
        for (Place place : m.getNet().getPlaces()) {
            if (m.getToken(place).getValue() > 0) {
                ret.add(place);
            }
        }
        return ret;
    }

    /**
     * Checks whether all system places have a partition annotated when
     * system=true, when system=false it checks whether all environment places
     * have a partition annotated.
     *
     * @param game
     * @param system
     * @return
     * @throws NoSuitableDistributionFoundException
     */
    public static boolean hasDistribution(PetriGame game, boolean system) throws NoSuitableDistributionFoundException {
        // checks there is at least some annotation
        boolean isAnnotated = false;
        boolean hasPlaceToAnnotate = false;
        for (Place p : game.getPlaces()) {
            if ((system && game.isSystem(p)) || (!system && game.isEnvironment(p))) {
                hasPlaceToAnnotate = true;
            }
            if (game.hasPartition(p)) {
                isAnnotated = true;
                break;
            }
        }
        // when nothing is annotated return
        if (!isAnnotated && hasPlaceToAnnotate) {
            return false;
        }

        //todo: all comments are old version, before cavarti
//        int maxToken = 0;
        for (Place place : game.getPlaces()) {
            if (!game.hasPartition(place) && ((!game.isEnvironment(place) && system) || (!game.isSystem(place) && !system))) {
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
