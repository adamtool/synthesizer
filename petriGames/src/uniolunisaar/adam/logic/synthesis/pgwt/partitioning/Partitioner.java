package uniolunisaar.adam.logic.synthesis.pgwt.partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.tools.Logger;

/**
 *
 *
 * @author Manuel Gieseking
 */
public class Partitioner {

    /**
     * Checks whether each reachable marking contains no two places with the
     * same partition id.
     *
     * @param game
     * @return
     * @throws
     * uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException
     * @throws uniolunisaar.adam.exceptions.CalculationInterruptedException
     * @throws
     * uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException
     */
    public static boolean checkPartitioning(PetriGameWithTransits game) throws InvalidPartitionException, CalculationInterruptedException, NoSuitableDistributionFoundException {
        CoverabilityGraph cg = CoverabilityGraph.getReachabilityGraph(game);
        for (CoverabilityGraphNode node : cg.getNodes()) {
            Marking m = node.getMarking(); // todo: the markings are very expensive for this use case. 
            List<Integer> partitions = new ArrayList<>();
            for (Place place : game.getPlaces()) {
                if (Thread.interrupted()) {
                    CalculationInterruptedException e = new CalculationInterruptedException();
                    Logger.getInstance().addError(e.getMessage(), e);
                    throw e;
                }
                if (m.getToken(place).getValue() > 0) {
                    // check the partition
                    if (!game.hasPartition(place)) {
                        throw new NoSuitableDistributionFoundException(place);
                    }
                    int partition = game.getPartition(place);
                    if (partitions.contains(partition)) {
                        throw new InvalidPartitionException(game, m, place);
                    } else {
                        partitions.add(partition);
                    }
                }
            }
        }
        return true;
    }

    /**
     *
     * @param game
     * @param onlyOneEnv
     * @throws NoSuitableDistributionFoundException
     */
    public static void doIt(PetriGameWithTransits game, boolean onlyOneEnv) throws NoSuitableDistributionFoundException {
        // hasDistribution only checks that all system places have a partition id annotated
        // so in the case that we have more than one environment player we discard all annotations 
        // and do the annotation on our own
        if (!hasDistribution(game, onlyOneEnv)) {
            Logger.getInstance().addMessage("Calculating partition of places ...");

            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            List<Set<Place>> partitions = calcPartitions(game, onlyOneEnv);

            if (partitions.size() > tokencount) {
                Logger.getInstance().addWarning("We distributed the net into " + partitions.size() + " partitions."
                        + " Only " + tokencount + " token can be visible together at a certain point in time. This could be necessary, but"
                        + " we could also be too greedy in creating new partitions.");
                MaxTokenCountCalculator calc = (MaxTokenCountCalculator) game.getCalculators().get(CalculatorIDs.MAX_TOKEN_COUNT.name());
                calc.setManuallyFixedTokencount(game, partitions.size());
            }

            // add token ids
            int count = onlyOneEnv ? 0 : -1;
            for (Set<Place> set : partitions) {
                ++count;
                if (set.isEmpty()) { // especially for the no env token case
                    --count;
                }
                for (Place place : set) {
                    if (onlyOneEnv && game.isEnvironment(place)) {
                        --count;
                        break; // it's the env partition, do it at the end
                    } else {
                        game.setPartition(place, count);
                    }
                }
            }
            Logger.getInstance().addMessage("... done calculation of partition of places.");
        } else {
            Logger.getInstance().addMessage("Using the annotated partition of places.");
        }
        if (onlyOneEnv) {
            for (Place p : game.getPlaces()) { // to simplify the annotations for the user (env automatically gets 0)
                if (game.isEnvironment(p)) {
                    game.setPartition(p, 0);
                }
            }
        }
    }

    /**
     * A very easy but expensive method to calculate the partitions of the net.
     *
     *
     * @param game
     * @param onlyOneEnv - there is only one environment player in the game.
     * @return
     * @throws NoSuitableDistributionFoundException
     */
    private static List<Set<Place>> calcPartitions(PetriGameWithTransits game, boolean onlyOneEnv) throws NoSuitableDistributionFoundException {
        List<Set<Place>> partitions = new ArrayList<>();
        Set<Place> considered = new HashSet<>();

        // the env partition
        Set<Place> env = new HashSet<>();
        if (onlyOneEnv) {
            for (Place place : game.getPlaces()) { // env should be one partition since it is only one token
                if (game.isEnvironment(place)) {
                    env.add(place);
                    considered.add(place);
                }
            }
        }

        // calculate a list of all possible markings        
//        CoverabilityGraph graph = CoverabilityGraph.getReachabilityGraph(game);
        CoverabilityGraph graph = CoverabilityGraph.get(game);
        List<Set<Place>> markings = new ArrayList<>();
        Logger.getInstance().addMessage("Starting to calculate the reachability graph.", true);
        for (Iterator<CoverabilityGraphNode> iterator = graph.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            markings.add(getPlaces(next.getMarking()));
        }
        Logger.getInstance().addMessage("Finished calculating the reachability graph.", true);

        // all initially marked places create one separate partition
        CoverabilityGraphNode init = graph.getInitialNode();
        for (Place place : getPlaces(init.getMarking())) { // for all initially marked places their should be at least one partition
            if (!onlyOneEnv || !game.isEnvironment(place)) {
                Set<Place> set = new HashSet<>();
                set.add(place);
                considered.add(place);
                partitions.add(set);
            }
        }

        // recursively add new places and try to pack them into a suitable partition
        boolean ret = addNewPlace(game, considered, partitions, markings);
        if (!ret) {
            long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            throw new NoSuitableDistributionFoundException(tokencount);
        }

        // the environment partition
        if (onlyOneEnv) {
            // had already been calculated, just add at the end to do not do anything more with it
            // For the no env-token case this one is empty, but that is intented
            partitions.add(env);
        }

        return partitions;
    }

    /**
     * Tries to inductively add not already partitioned places to partitions or
     * create a new one if the test fails which checks that no reachable marking
     * contains more than one place of any partition.
     *
     * The method is very naive and expensive because it just takes a random not
     * considered place, adds it to the first partition, if the check fails, the
     * place is added to the next partition and so on. If no partition could be
     * found such that the test does not fail, a new partition containing the
     * place is created.
     *
     * @param net - the considered Petri net
     * @param considered - all places which are already sorted into a partition
     * @param partitions - the so fare partitioned places
     * @param markings - a list of all reachable markings
     * @return
     */
    private static boolean addNewPlace(PetriGameWithTransits net, Set<Place> considered, List<Set<Place>> partitions, List<Set<Place>> markings) {
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

        // try to add the place to the first partition and if this fits
        // recursively add next places.
        boolean good = false;
        List<Set<Place>> partitionsToIterate = new ArrayList<>(partitions);
        for (Set<Place> partition : partitionsToIterate) {
            if (!partition.isEmpty() && (net.isEnvironment(partition.iterator().next()) == net.isEnvironment(place))) { // only if the types of the partition and the place fits
                partition.add(place);
                if (isValidDistribution(partitions, markings)) {
                    considered.add(place);
                    good = addNewPlace(net, considered, partitions, markings);
                    if (!good) {
                        considered.remove(place);
                        partition.remove(place);
                    } else {
                        break;
                    }
                } else {
                    partition.remove(place);
                }
            }
        }
        // if there was no partition possible when adding the place to an already
        // existing partition, create a new one.
        if (!good) {
            Set<Place> partition = new HashSet<>();
            partition.add(place);
            considered.add(place);
            partitions.add(partition);
            good = addNewPlace(net, considered, partitions, markings);
            if (!good) {
                considered.remove(place);
                partitions.remove(partition);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks wether the given list or partitions are valid, i.e., no two places
     * of any reachable marking are contained in the same partition.
     *
     * @param partitions
     * @param markings
     * @return
     */
    static boolean isValidDistribution(List<Set<Place>> partitions, List<Set<Place>> markings) {
        for (Set<Place> marking : markings) {
            for (Set<Place> tree : partitions) {
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
    public static boolean hasDistribution(PetriGameWithTransits game, boolean system) throws NoSuitableDistributionFoundException {
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
