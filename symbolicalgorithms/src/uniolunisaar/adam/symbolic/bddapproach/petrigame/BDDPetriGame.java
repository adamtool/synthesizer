package uniolunisaar.adam.symbolic.bddapproach.petrigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.logic.partitioning.Partitioner;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.logic.util.AdamTools;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDPetriGame extends PetriGame {

    private final Set<Transition> sysTransition;
    private final Map<Transition, Pair<List<Place>, List<Place>>> preset;
    private final Map<Transition, Pair<List<Place>, List<Place>>> postset;
    // saves places devided into groups for each token
    private Set<Place>[] places;
    // saves transitions belonging in the presets of the places to each token
    private List<Transition>[] transitions;

    public BDDPetriGame(PetriNet pn) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        this(pn, false);
    }

    public BDDPetriGame(PetriNet pn, boolean skipChecks) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(pn, skipChecks);
        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(pn);
        Pair<Transition, Transition> witness = AdamTools.isSolvablePetriGame(pn, cover);
        if (witness != null) {
            throw new RuntimeException("Petri game not solvable: " + witness.toString());
        }
        if (!skipChecks) {
            if (!super.getBounded().isSafe()) {
                throw new NetNotSafeException(super.getBounded().unboundedPlace.toString(), super.getBounded().sequence.toString());
            }
//            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(pn);
//            if (AdamTools.isSolvablePetriGame(pn, cover)) {
//                throw new NoSuitableDistributionFoundException();
//            }
        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net is safe!", false);
        }

        sysTransition = new HashSet<>();
        preset = new HashMap<>();
        postset = new HashMap<>();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        bufferData();
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    // todo: proof that it's a suitable slicing, such that every environment place
    // belongs to one single invariant
    private void bufferData() throws NoSuitableDistributionFoundException {
        for (Transition t : getNet().getTransitions()) {
            // Pre- and Postset
            List<Place> pre = new ArrayList<>();
            pre.addAll(t.getPreset());
            List<Place> pre_env = new ArrayList<>();
            List<Place> post = new ArrayList<>();
            post.addAll(t.getPostset());
            List<Place> post_env = new ArrayList<>();

            // fill sysTransition
            boolean add = true;
            for (Place place : pre) {
                if (AdamExtensions.isEnviroment(place)) {
                    add = false;
                    pre_env.add(place);
                }
            }
            if (add) {
                sysTransition.add(t);
            }

            // split the environmental places from pre- and postset 
            for (Place place : post) {
                if (AdamExtensions.isEnviroment(place)) {
                    post_env.add(place);
                }
            }
            pre.removeAll(pre_env);
            post.removeAll(post_env);
            preset.put(t, new Pair<>(pre_env, pre));
            postset.put(t, new Pair<>(post_env, post));
        }

        //        if (net.hasExtension("MAXTOKEN")) {
//            TOKENCOUNT = (Integer) net.getExtension("MAXTOKEN");
//            Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (read from net)");
//        } else
        // add suitable partition to the places (extension token)
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Partitioner.doIt(getNet());
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS

        //todo:  all comments are old version, before cavarti
        // split places and add an id
        int additional = (isConcurrencyPreserving()) ? 0 : 1;
        places = (Set<Place>[]) new Set<?>[getMaxTokenCountInt()];
        for (Place place : getNet().getPlaces()) {
            int token = AdamExtensions.getToken(place);
            if (places[token] == null) {
                places[token] = new HashSet<>();
            }
            int add = additional;
            AdamExtensions.setID(place, places[token].size() + add);
            places[token].add(place);
        }

        // todo: test no two places in one group are marked at the same time        
//            if (!skip) {
//                CoverabilityGraph cv = CoverabilityGraph.get(net);
//                for (Iterator<CoverabilityGraphNode> it = cv.getNodes().iterator(); it.hasNext();) {
//                    CoverabilityGraphNode node = it.next();
//                    Marking m = node.getMarking();
//                    
//                }
//            }
        // Calculate the possible transitions for the places of a special token
        transitions = new List[getMaxTokenCountInt() - 1];
        for (int i = 1; i < getMaxTokenCountInt(); ++i) {
            transitions[i - 1] = new ArrayList<>();
            for (Place place : places[i]) {
                transitions[i - 1].addAll(place.getPostset());
            }
        }
    }

    public Set<Transition> getSysTransition() {
        return sysTransition;
    }

    /**
     * Returns a 2-tupel, where the first entry is the environment places of t's
     * postset and the second the system places of the postset.
     *
     * @param t - The transition to get the postset from
     * @return The postset of t splitted in (env, sys)
     */
    public Pair<List<Place>, List<Place>> getSplittedPostset(Transition t) {
        return postset.get(t);
    }

    /**
     * Returns a 2-tupel, where the first entry is the environment places of t's
     * preset and the second the system places of the preset.
     *
     * @param t - The transition to get the preset from
     * @return The preset of t splitted in (env, sys)
     */
    public Pair<List<Place>, List<Place>> getSplittedPreset(Transition t) {
        return preset.get(t);
    }

    public Set<Place>[] getPlaces() {
        return places;
    }

    public List<Transition>[] getTransitions() {
        return transitions;
    }

    /**
     * Problem if it's really a long
     *
     * @return
     */
    public int getMaxTokenCountInt() {
        return (int) getMaxTokenCount();
    }
}
