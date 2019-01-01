package uniolunisaar.adam.ds.graph;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 * @param <S>
 * @param <F>
 */
public class Graph<S extends State, F extends Flow> {

    private String name;
    private final Map<Integer, S> states;
    private final Set<F> flows;
    private SoftReference<Map<Integer, SoftReference<Set<S>>>> postSets = null;
    private S initial;

    public Graph(String name) {
        this.name = name;
        states = new HashMap<>();
        flows = new HashSet<>();
        // Initialises the SoftReferences for the postset
        Map<Integer, SoftReference<Set<S>>> post = new HashMap<>();
        postSets = new SoftReference<>(post);
    }

    public Graph(Graph<S, F> g) {
        this.name = g.name;
        this.states = new HashMap<>(g.states);
        this.flows = new HashSet<>(g.flows);
    }

    protected S addState(S state) {
        int id = states.size();
        state.setId(id);
        states.put(id, state);
        // update postsets
//            Map<Integer, SoftReference<Set<GraphS>>> posts = postSets.get();
//            if (posts == null) {
//                posts = new HashMap<>();
//                postSets = new SoftReference<>(posts);
//            }
//            Set<GraphS> post = new HashSet<>();
//            posts.put(s.getId(), new SoftReference<>(post));
        return state;
    }

    /**
     * Return the flow if successful otherwise null
     *
     * @param flow
     * @return
     */
    protected F addFlow(F flow) {
//        GraphS s = new GraphS(-1, source, false);
//        if (!states.contains(s)) {
//            System.out.println("source not existend");
//           // return false;
//        }
//        s = new GraphS(-1, target, false);
//        if (!states.contains(s)) {
//            System.out.println("target not existend");
//            //return false;
//        }
        boolean check = flows.add(flow);
        // todo: really not nice (it is just for PetriGame strategy, for boost up
        // skip for graphgame        
        //calculatePostset(getS(source.hashCode()).getId()).add(getS(target.hashCode()));
        return check ? flow : null;
    }

    public boolean removeFlows(List<F> flows) {
        return this.flows.removeAll(flows);
    }

    private Set<S> calculatePostset(Integer graphstateID) {
        Map<Integer, SoftReference<Set<S>>> posts = postSets.get();
        if (posts == null) {
            posts = new HashMap<>();
            postSets = new SoftReference<>(posts);
        }
        SoftReference<Set<S>> postSoft = posts.get(graphstateID);
        Set<S> post = (postSoft != null) ? postSoft.get() : null;
        if (post == null) {
            post = new HashSet<>();
            for (F f : flows) {
                if (f.getSourceid() == graphstateID) {
                    post.add(states.get(f.getTargetid()));
                }
            }
            posts.put(graphstateID, new SoftReference<>(post));
        }
        return post;
    }

    public Set<F> getPostsetFlows(Integer stateID) {
        Set<F> postFs = new HashSet<>();
        for (F f : flows) {
            if (f.getSourceid() == stateID) {
                postFs.add(f);
            }
        }
        return postFs;
    }

    /**
     * Returns the successor of a state.
     *
     * @param stateID
     * @return
     */
    public Set<S> getPostset(Integer stateID) {
//        if (!states.containsKey(id)) {
////            System.out.println("error...");
//        }
        return Collections.unmodifiableSet(calculatePostset(stateID));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public S getState(int id) {
        return states.get(id);
    }

    public S getInitial() {
        return initial;
    }

    public void setInitial(S initial) {
        this.initial = initial;
    }

    public Set<S> getStates() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(states.values()));
    }

    public Set<F> getFlows() {
        return Collections.unmodifiableSet(flows);
    }

    public List<Transition> getTransition(State prevState) {
        return GraphExtensionHandler.getTransition(prevState);
    }

    public List<Transition> getStrategyTransition(State prevState) {
        return GraphExtensionHandler.getStrategyTransition(prevState);
    }

    public boolean hasStrategyTransition(State succState) {
        return GraphExtensionHandler.hasStrategyTransition(succState);
    }

    public boolean hasTransition(State succState) {
        return GraphExtensionHandler.hasTransition(succState);
    }

    public void setStrategyTransition(State succState, List<Transition> strat_trans) {
        GraphExtensionHandler.setStrategyTransition(succState, strat_trans);
    }

    public void setTransition(State succState, List<Transition> trans) {
        GraphExtensionHandler.setTransition(succState, trans);
    }
}
