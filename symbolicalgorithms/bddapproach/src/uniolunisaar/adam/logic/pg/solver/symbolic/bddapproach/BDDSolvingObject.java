package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.pg.partitioning.Partitioner;
import uniolunisaar.adam.util.benchmarks.Benchmarks;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class BDDSolvingObject<W extends Condition<W>> extends SolvingObject<PetriGame, W> {

    private final Set<Transition> sysTransition;
    private final Map<Transition, Pair<List<Place>, List<Place>>> preset;
    private final Map<Transition, Pair<List<Place>, List<Place>>> postset;
    // saves places divided into groups for each token
    private Set<Place>[] places;
    // saves transitions belonging in the presets of the places to each token
    private List<Transition>[] transitions;

    public BDDSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        this(game, winCon, false);
    }

    public BDDSolvingObject(PetriGame game, W winCon, boolean skipChecks) throws NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException, NetNotSafeException {
        super(game, winCon);
//        super(game, skipChecks);
        if (!skipChecks) {
            if (!game.getBounded().isSafe()) {
                throw new NetNotSafeException(game.getBounded().unboundedPlace.toString(), game.getBounded().sequence.toString());
            }
            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(game);
//            NotSolvableWitness witness = AdamTools.isSolvablePetriGame(pn, cover);
//            if (witness != null) {
////                throw new NotSupportedGameException("Petri game not solvable: " + witness.toString());
//            }
            // only one env token is allowed (todo: do it less expensive ?)
            for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
                CoverabilityGraphNode next = iterator.next();
                Marking m = next.getMarking();
                boolean first = false;
                for (Place place : game.getPlaces()) {
                    if (m.getToken(place).getValue() > 0 && game.isEnvironment(place)) {
                        if (first) {
                            throw new NotSupportedGameException("There are two enviroment token in marking " + m.toString() + ". The BDD approach only allows one external source of information.");
                        }
                        first = true;
                    }
                }
            }

        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net is safe or"
                    + " belongs to the class of solvable Petri games!", false);
        }

        sysTransition = new HashSet<>();
        preset = new HashMap<>();
        postset = new HashMap<>();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        bufferData(skipChecks);
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    public BDDSolvingObject(BDDSolvingObject<W> obj) {
        super(new PetriGame(obj.getGame()), obj.getWinCon().getCopy());
        // Cannot copy these sets and have the recalculate them since
        // the Petri net constructor creates new objects for each transition, etc.
        sysTransition = new HashSet<>();
        preset = new HashMap<>();
        postset = new HashMap<>();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        try {
            bufferData(true);
        } catch (NoSuitableDistributionFoundException ex) {
            // should not be possible since it should be a copy of a distributable game
            Logger.getInstance().addError("The original game which you gave me to copy wasn't correct!", ex);
        } catch (InvalidPartitionException ex) {
            // cannot happen (just thrown by skipChecks == false
        }
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    // todo: proof that it's a suitable slicing, such that every environment place
    // belongs to one single invariant
    private void bufferData(boolean skipChecks) throws NoSuitableDistributionFoundException, InvalidPartitionException {
        for (Transition t : getGame().getTransitions()) {
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
                if (getGame().isEnvironment(place)) {
                    add = false;
                    pre_env.add(place);
                }
            }
            if (add) {
                sysTransition.add(t);
            }

            // split the environmental places from pre- and postset 
            for (Place place : post) {
                if (getGame().isEnvironment(place)) {
                    post_env.add(place);
                }
            }
            pre.removeAll(pre_env);
            post.removeAll(post_env);
            preset.put(t, new Pair<>(pre_env, pre));
            postset.put(t, new Pair<>(post_env, post));
//            System.out.println(t+"  pree  "+ preset.get(t).toString());
//            System.out.println(t+"   post  "+postset.get(t).toString());
        }

//        TokenTreeCreator.createAndAnnotateTokenTree(getNet());
        //        if (net.hasExtension("MAXTOKEN")) {
//            TOKENCOUNT = (Integer) net.getExtension("MAXTOKEN");
//            Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (read from net)");
//        } else
        // add suitable partition to the places (extension token)
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Partitioner.doIt(getGame());
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS       
        if (!skipChecks) {
            Logger.getInstance().addMessage("check partitioning ... ", true);
            PGTools.checkValidPartitioned(getGame());
            Logger.getInstance().addMessage("... partitionning check done.");
        }

        try {
            //todo:  all comments are old version, before cavarti
            // split places and add an id
//        int add = getEnvPlaces().isEmpty() ? 1 : 0;
            places = (Set<Place>[]) new Set<?>[getMaxTokenCountInt()];
            // just do it for all, since user could annotate them badly (skipping ids)
//            if (getGame().getEnvPlaces().isEmpty()) { // add empty set when no env place existend (todo: is it to hacky for no env case?)
//                places[0] = new HashSet<>();
//            }
            for (int i = 0; i < getMaxTokenCount(); i++) {
                places[i] = new HashSet<>();
            }
            boolean cp = getGame().getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
            int additional = cp ? 0 : 1;
            for (Place place : getGame().getPlaces()) {
                int token = getGame().getPartition(place);
                if (places[token] == null) {
                    places[token] = new HashSet<>();
                }
                getGame().setID(place, places[token].size() + additional);
                places[token].add(place);
            }
        } catch (Exception e) {
            Logger.getInstance().addError("Sorry, most likely you did s.th. wrong with the annotation of the token.", e);
            throw e;
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

    public Set<Place>[] getDevidedPlaces() {
        return places;
    }

    public List<Transition>[] getDevidedTransitions() {
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

    // Delegate methods
    public boolean isConcurrencyPreserving() {
        return getGame().getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
    }

    public long getMaxTokenCount() {
        return getGame().getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
    }

    @Override
    public BDDSolvingObject<W> getCopy() {
        return new BDDSolvingObject<>(this);
    }

}
