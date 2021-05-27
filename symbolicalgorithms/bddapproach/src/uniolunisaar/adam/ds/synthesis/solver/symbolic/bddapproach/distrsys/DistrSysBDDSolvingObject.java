package uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.Partitioner;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;

/**
 * This class serves for storing the input of the solving algorithm, i.e., the
 * model (the Petri game) and the specification (the winning condition).
 *
 * For the BDD approach for one 1 env and arbitrary system players we split the
 * system places (and the environment) up into disjunct partitions to have a
 * more efficient encoding and know exactly the indices of the place ids.
 *
 * @author Manuel Gieseking
 * @param <W> - the winning condition.
 */
public class DistrSysBDDSolvingObject<W extends Condition<W>> extends BDDSolvingObject<W> {

    private Set<Transition> sysTransition;
    private Map<Transition, Pair<List<Place>, List<Place>>> preset;
    private Map<Transition, Pair<List<Place>, List<Place>>> postset;
    // saves transitions belonging in the presets of the places to each partition
    private List<Transition>[] transitions;

    public DistrSysBDDSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        this(game, winCon, false);
    }

    public DistrSysBDDSolvingObject(PetriGameWithTransits game, W winCon, boolean skipChecks) throws NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException, NetNotSafeException {
        super(game, winCon, skipChecks);
        sysTransition = new HashSet<>();
        preset = new HashMap<>();
        postset = new HashMap<>();
        bufferData();
    }

    public DistrSysBDDSolvingObject(DistrSysBDDSolvingObject<W> obj) {
        super(obj);
        sysTransition = new HashSet<>();
        preset = new HashMap<>();
        postset = new HashMap<>();
        bufferData();
    }

    @Override
    protected void checkPrecondition(PetriGameWithTransits game) throws NetNotSafeException, NotSupportedGameException {
        // todo: merge this one with the PgwtPreconditionChecker
        if (!game.getBounded().isSafe()) {
            throw new NetNotSafeException(game.getBounded().unboundedPlace.toString(), game.getBounded().sequence.toString());
        }
        //            NotSolvableWitness witness = AdamTools.isSolvablePetriGame(pn, cover);
//            if (witness != null) {
////                throw new NotSupportedGameException("Petri game not solvable: " + witness.toString());
//            }
        PGTools.checkOnlyOneEnvToken(game);
    }

    @Override
    protected void annotatePlacesWithPartitions(boolean skipChecks) throws InvalidPartitionException, NoSuitableDistributionFoundException {
//        TokenTreeCreator.createAndAnnotateTokenTree(getNet());
        //        if (net.hasExtension("MAXTOKEN")) {
//            TOKENCOUNT = (Integer) net.getExtension("MAXTOKEN");
//            Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (read from net)");
//        } else
        // add suitable partition to the places (extension token)
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Partitioner.doIt(getGame(), true);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS       
        if (!skipChecks) {
            Logger.getInstance().addMessage("check partitioning ... ", true);
            PGTools.checkValidPartitioned(getGame());
            Logger.getInstance().addMessage("... partitionning check done.");
        }
    }

    // todo: proof that it's a suitable slicing, such that every environment place
    // belongs to one single invariant   
    private void bufferData() {
        int id = 1;
        for (Transition t : getGame().getTransitions()) {
            // Add a unique ID starting from 1
            getGame().setID(t, id++);
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

        // Calculate the possible transitions for the places of a special token
        transitions = new List[getMaxTokenCountInt() - 1]; // todo: get rid of the generic array?// todo: get rid of the generic array?
        for (int i = 1; i < getMaxTokenCountInt(); ++i) {
            transitions[i - 1] = new ArrayList<>();
            for (Place place : getDevidedPlaces()[i]) {
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

    public List<Transition>[] getDevidedTransitions() {
        return transitions;
    }

    @Override
    public DistrSysBDDSolvingObject<W> getCopy() {
        return new DistrSysBDDSolvingObject<>(this);
    }

}
