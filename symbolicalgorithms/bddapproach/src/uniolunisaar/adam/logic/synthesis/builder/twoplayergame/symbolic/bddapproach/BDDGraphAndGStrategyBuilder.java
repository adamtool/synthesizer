package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach;

import java.util.LinkedList;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * @author Manuel Gieseking
 */
public class BDDGraphAndGStrategyBuilder {

    private static BDDGraphAndGStrategyBuilder instance = null;

    public static BDDGraphAndGStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new BDDGraphAndGStrategyBuilder();
        }
        return instance;
    }

    protected BDDGraphAndGStrategyBuilder() {
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDGraph builtGraph(S solver) throws CalculationInterruptedException {
        return builtGraph(solver, -1);
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDGraph builtGraphStrategy(S solver, Map<Integer, BDD> distance) throws NoStrategyExistentException, CalculationInterruptedException {
        return builtGraphStrategy(solver, -1, distance);
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDGraph builtGraph(S solver, int depth) throws CalculationInterruptedException {
        return builtGraph(solver, false, depth, null);
    }

    public <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDGraph builtGraphStrategy(S solver, int depth, Map<Integer, BDD> distance) throws NoStrategyExistentException, CalculationInterruptedException {
        if (!solver.existsWinningStrategy()) {
            throw new NoStrategyExistentException();
        }
        return builtGraph(solver, true, depth, distance);
    }

    /**
     *
     * @param solver
     * @param strategy
     * @param depth -1 means do the whole graph
     * @return
     */
    private <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>> BDDGraph builtGraph(S solver, boolean strategy, int depth, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        String text = (strategy) ? "strategy" : "game";
        BDDGraph graph = new BDDGraph("Finite graph " + text + " of the net "
                + solver.getGame().getName());
        BDD states = (strategy) ? solver.getBufferedWinDCSs() : solver.getBufferedDCSs();
//        BDD states = solver.getWinDCSs();

//        try {
//            BDDTools.saveStates2Pdf("./states", states, solver);
////        states = states.not();
//        } catch (IOException ex) {
//            Logger.getLogger(BDDGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(BDDGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
//        }
        LinkedList<BDDState> todoStates = new LinkedList<>();

        BDD inits = solver.getInitialDCSs();
        inits = inits.and(states);
        if (strategy) { // is strategy only add one initial state            
            addOneInitState(solver, graph, inits, todoStates, distance);
        } else { // is graph add all initial states
            // Create a bufferstate where all initial states are childs
            BDDState in = graph.addState(solver.getOne(), solver);
            graph.setInitial(in);
            BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
            while (!init.isZero()) {
                BDDState initSucc = graph.addState(init, solver);
                // mark mcut
                initSucc.setEnvState(solver.isEnvState(init));
                initSucc.setBad(solver.isBadState(init));
                initSucc.setSpecial(solver.isSpecialState(init));
                graph.addFlow(in, initSucc, null);
                todoStates.add(initSucc);
                inits = inits.and(init.not());
                init = inits.satOne(solver.getFirstBDDVariables(), false);
//                System.out.println("init state");
            }
        }

        int count = 0;
        while (!todoStates.isEmpty() && depth != count) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            ++count;
            BDDState prev = todoStates.poll();
            boolean envState = prev.isEnvState();
//            System.out.println("state" );
//                BDDTools.printDecodedDecisionSets(prev.getState(), solver, true);
//            System.out.println("mcut "+ envState);
//            System.out.println(!prev.getState().and(solver.getMcut()).isZero());
            BDD succs = (envState) ? solver.getEnvSuccTransitions(prev.getState()) : solver.getSystemSuccTransitions(prev.getState());

//            if (prev.getId() == 1) {
//                BDDTools.printDecisionSets(prev.getState(), true);
//                System.out.println("is env" + envState);
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
//            }
            if (!succs.isZero()) {// is there a firable transition ?
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
//System.out.println("TRANS");
//                BDDTools.printDecodedDecisionSets(solver.getBufferedSystemTransitions(), solver, true);
                // shift successors to the first variables
                succs = getSuccessorBDD(solver, succs, states);
//                System.out.println("succcs");
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
                if (!strategy || envState) {
                    addAllSuccessors(succs, solver, graph, prev, todoStates, false);
                } else {
                    addOneSuccessor(succs, solver, graph, prev, todoStates, distance);
                }
            }
        }
        return graph;
    }

    protected <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDD getSuccessorBDD(S solver, BDD succs, BDD validStates) {
        return solver.getSuccs(succs).and(validStates);
    }

    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
        BDDState in = graph.addState(init, solver);
        in.setEnvState(solver.isEnvState(init));
        in.setBad(solver.isBadState(init));
        in.setSpecial(solver.isSpecialState(init));
        graph.setInitial(in);
        todoStates.add(in);
    }

    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addAllSuccessors(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, boolean oneRandom) {
        BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
        while (!succ.isZero()) {
            String value = BDDTools.getDecodedDecisionSets(succ, solver);
            value = value.substring(0, value.indexOf("->"));
            addState(solver, graph, prev, todoStates, new BDDState(succ, -1, value));
            if (oneRandom) {
                return;
            }
            succs.andWith(succ.not());
            succ = succs.satOne(solver.getFirstBDDVariables(), false);
        }
    }

    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        addAllSuccessors(succs, solver, graph, prev, todoStates, true);
    }

    protected <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            Flow addFlow(S solver, BDDGraph graph, BDDState pre, BDDState succ) {
        return graph.addFlow(pre, succ, solver.getTransition(pre.getState(), succ.getState()));
    }

    /**
     * find a smarter solution. Problem is there is no distances added by the
     * super class for environment state and the initial state. (only needed for
     * the addOneSuccessor for reachability and buchi graph builder)
     *
     * @param state
     * @param distance
     * @return
     */
    int getDistance(BDD state, Map<Integer, BDD> distance) {
        int max = distance.containsKey(-1) ? distance.size() - 1 : distance.size();
        for (int i = 0; i < max; i++) {
            BDD value = distance.get(i);
            if (!value.and(state).isZero()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the nearest state according to the distance set. (for buchi and
     * reachability)
     *
     * @param solver
     * @param states
     * @param distance
     */
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDState getNearestState(S solver, BDD states, Map<Integer, BDD> distance) {
        BDD state = states.satOne(solver.getFirstBDDVariables(), false);
        BDD min = state;
        int min_dist = Integer.MAX_VALUE;
        while (!state.isZero()) {
            int dist = getDistance(state, distance);
            if (dist < min_dist) {
                min = state;
                min_dist = dist;
            }
            states = states.and(state.not());
            state = states.satOne(solver.getFirstBDDVariables(), false);
        }
        String value = BDDTools.getDecodedDecisionSets(min, solver);
        value = value.substring(0, value.indexOf("->"));
        return new BDDState(min, min_dist, value);
    }

    /**
     * Adds the init state which is the nearest according to the distance set.
     * (for buchi and reachability)
     *
     * @param solver
     * @param graph
     * @param inits
     * @param todoStates
     * @param distance
     */
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addNearestInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates, Map<Integer, BDD> distance) {
        BDDState min = getNearestState(solver, inits, distance);
        // add the minimal init
        BDDState in = graph.addState(min);
        in.setEnvState(solver.isEnvState(min.getState()));
        in.setBad(solver.isBadState(min.getState()));
        in.setSpecial(solver.isSpecialState(min.getState()));
        graph.setInitial(in);
        todoStates.add(in);
    }

    /**
     * Tests if a state already exists in the graph, then it only adds the flow
     * otherwise it constructs the succcessor and the flow and adds it to the
     * todo states.
     *
     * @param solver
     * @param graph
     * @param prev
     * @param todoStates
     * @param succ
     */
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            void addState(S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, BDDState succ) {
        BDDState oldSuccState = graph.contains(succ.getState());
        if (oldSuccState != null) { // jump to every already visited cut
            addFlow(solver, graph, prev, oldSuccState);
        } else {
            BDDState succState = graph.addState(succ);
            succState.setEnvState(solver.isEnvState(succ.getState()));
            succState.setBad(solver.isBadState(succ.getState()));
            succState.setSpecial(solver.isSpecialState(succ.getState()));
            addFlow(solver, graph, prev, succState);
            // take the next step
            todoStates.add(succState);
        }
    }

    /**
     * Gets the reachability strategy for the given attractor in distance.
     *
     * (only needed for the addOneSuccessor for reachability and buchi graph
     * builder)
     *
     * @param succs
     * @param solver
     * @param prev
     * @param distance
     * @return
     */
    <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions, S extends BDDSolver<W, SO, SOP>>
            BDDState getNearestSuccessor(BDD succs, S solver, BDDState prev, Map<Integer, BDD> distance) {
        BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
        int idx = prev.getDistance();
        if (idx == -1) { // should be the initial state and all env states (todo: find a smarter solution)
            idx = getDistance(prev.getState(), distance);
        }
        BDD nearestSuccs = distance.get(idx - 1); // there should be a successor in the previous iteration
        while (!succ.isZero()) {
            if (idx == 0) { // if we are already there every successor is OK
                String value = BDDTools.getDecodedDecisionSets(succ, solver);
                value = value.substring(0, value.indexOf("->"));
                return new BDDState(succ, -1, value);
            }
            // otherwise choose one which is getting nearer to the reachable states        
            if (!succ.and(nearestSuccs).isZero()) {
                String value = BDDTools.getDecodedDecisionSets(succ, solver);
                value = value.substring(0, value.indexOf("->"));
                return new BDDState(succ, idx - 1, value);
            }
            succs.andWith(succ.not());
            succ = succs.satOne(solver.getFirstBDDVariables(), false);
        }
        return null;// should not happen
    }

//    private final BDDPetriGame game;
//    // good in the view of player 0 (system)
//    private final BDD goodDCS;
//    private final Set<GraphStrategyData> strategies;
//
//    public BDDGraphBuilder(BDDPetriGame game, BDD goodSysDCS) {
//        this.game = game;
//        this.goodDCS = goodSysDCS;
//        this.strategies = new HashSet<>();
//        initialize();
//    }
//
//    public BDDGraph buildOneStrategy() throws NoStrategyExistentException {
//        if (!game.existsWinningStrategy()) {
//            throw new NoStrategyExistentException();
//        }
//
//        GraphStrategyData data = new GraphStrategyData(strategies.size(), game);
//        strategies.add(data);
//        // chose one good initial decision set
//        BDD init = game.getInitialDCS();
////        System.out.println("inits");
////        Tools.printDecodedDecisionSets(init, game, true);
////        System.out.println("init and bad");
////        Tools.printDecodedDecisionSets(init.and(badDCS), game, true);
////        System.out.println("init and not bad");
////        Tools.printDecodedDecisionSets(init.and(badDCS.not()), game, true);
////        Tools.printDecodedDecisionSets(goodDCS, game, true);
//        init = init.and(goodDCS);
////        System.out.println("asdf"
////                + "");
////        Tools.printDecodedDecisionSets(init, game, true);
//        init = init.satOne(game.getFirstBDDVariables(), false);
////        init = init.fullSatOne();
//        BDDState initState = data.addState(init);
//        data.getStrategy().setInitial(initState);
//        nextStep(initState, data, false);
//        return data.getStrategy();
//    }
//
//    public Set<BDDGraph> buildAllStrategies() throws NoStrategyExistentException {
//        if (!game.existsWinningStrategy()) {
//            throw new NoStrategyExistentException();
//        }
//        // all good initial decision sets
//        BDD inits = game.getInitialDCS();
//        inits = inits.and(goodDCS);
////        System.out.println("tadaaa");
////        Tools.printDecodedDecisionSets(inits, game, true);
//        while (!inits.isZero()) {
//            GraphStrategyData data = new GraphStrategyData(strategies.size(), game);
//            strategies.add(data);
//            BDD init = inits.satOne(game.getFirstBDDVariables(), false);
////            BDD init = inits.fullSatOne();
//            BDDState initState = data.addState(init);
//            data.getStrategy().setInitial(initState);
//            nextStep(initState, data, true);
//            inits.andWith(init.not());
//        }
//        Set<BDDGraph> ret = new HashSet<>();
//        for (GraphStrategyData strategy : strategies) {
//            ret.add(strategy.getStrategy());
//        }
//        return ret;
//    }
//
//    private void nextStep(BDDState prev, GraphStrategyData data, boolean all) {
////        System.out.println("Size of data " + data.getStrategy().getStates().size());
////        for (GraphState state : data.getStrategy().getStates()) {
////            BDDTools.printDecodedDecisionSets(state.getState(), game, true);
////        }
////        System.out.println("next step");
////        Tools.printDecodedDecisionSets(dcs, game, true);
//        // is it a good one?
//        BDD dcs = prev.getState();
//        BDD goodMarking = dcs.and(goodDCS);
//        if (goodMarking.isZero()) {
////            System.out.println("should not appear");
////            Tools.printDecodedDecisionSets(dcs, game, true);
//            return;
//        }
//        if (!game.isMcut(dcs)) {
//            prev.setMcut(false);
////            System.out.println("no mcut");
////            data.addStatesTillLastMcut(dcs);
//            // get all good successors of typ1 system transitions
//            BDD succs = game.getSystem1SuccTransitions(dcs);
////            System.out.println("SUCSS: ");
////            Tools.printDecodedDecisionSets(succs, game, true);
//            // is there a firable transition ?
//            if (!succs.isZero()) {
//                boolean first = true;
//                GraphStrategyData oldStrat = (all) ? new GraphStrategyData(strategies.size() - 1, data) : null;
//                // shift to the successors
//                succs = game.getSuccs(succs);
////                System.out.println("DCS:");
////                Tools.printDecodedDecisionSets(dcs, game, first);
////                System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
////                Tools.printDecodedDecisionSets(succs, game, first);
//                // get only the good ones
//                succs = succs.and(goodDCS);
////                System.out.println("%%%%%%%%%%%%%%%%%%%%%%");
////                Tools.printDecodedDecisionSets(succs, game, first);
//                BDD succ = succs.satOne(game.getFirstBDDVariables(), false);
////                BDD succ = succs.fullSatOne();
//
//                while (!succ.isZero()) {
//                    if (first) {
//                        // add
//                        //data.addState(succ, !succ.and(game.getMcut()).isZero()); 
//                        BDDState succState = data.addState(succ);
//                        data.addFlow(prev, succState);
//                        // and the next one
//                        nextStep(succState, data, all);
//                    } else {
//                        GraphStrategyData newData = new GraphStrategyData(strategies.size(), oldStrat);
//                        BDDState succState = newData.addState(succ);
//                        //  newData.addState(succ, !succ.and(game.getMcut()).isZero());
//                        newData.addFlow(prev, succState);
//                        strategies.add(newData);
//                        // and the next one
//                        nextStep(succState, newData, all);
//                        succs.andWith(succ.not());
//                        succ = succs.satOne(game.getFirstBDDVariables(), false);
////                        succ = succs.fullSatOne();
//                    }
//                    if (!all) {
//                        break;
//                    } else {
//                        first = false;
//                    }
//                }
//            } else {
//                System.out.println("no successor at all for:");
//                BDDTools.printDecodedDecisionSets(dcs, game, true);
//            }
//        } else {
//            prev.setMcut(true);
////            System.out.println("is mcut");
////            data.clearStatesTillLastMcut();
//            // get all good successors of env transitions
//            BDD succs = game.getEnvSuccTransitions(dcs);
//
////            System.out.println("dcs:");
////            Tools.printDecodedDecisionSets(succs, game, true);
////            Tools.printDecisionSets(succs, game, true);
////            System.out.println("succs: ");
////            Tools.printDecodedDecisionSets(succs, game, true);
//            if (!succs.isZero()) {
//                // shift successors to the first variables
//                succs = game.getSuccs(succs);
////                System.out.println("succ");
////                Tools.printDecodedDecisionSets(succs, game, true);
//                // todo: should all be good, comment next line
//                succs = succs.and(goodDCS);
////                System.out.println("succ");
////                Tools.printDecodedDecisionSets(succs, game, true);
//                BDD succ = succs.satOne(game.getFirstBDDVariables(), false);
////                BDD succ = succs.fullSatOne();
//                while (!succ.isZero()) {
//                    // add
//                    //data.addState(succ, !succ.and(game.getMcut()).isZero());  System.out.println("dcs:");
////            Tools.printDecodedDecisionSets(succ, game, true);
////            Tools.printDecisionSets(succ, game, true);
//                    // only loops between mcuts are allowed, thus this is the only point
//                    // in program where we possibly can add a loop    
//                    if (game.isMcut(succ)) {
//                        BDDState oldSuccState = data.contains(succ);
//                        if (oldSuccState != null) {
//                            data.addFlow(prev, oldSuccState);
//                        } else {
//                            BDDState succState = data.addState(succ);
//                            data.addFlow(prev, succState);
//                            // take the next step
//                            nextStep(succState, data, all);
//                        }
//                    } else {
//                        // take the next step
//                        BDDState oldSuccState = data.contains(succ);
//                        if (oldSuccState == null) {
//                            BDDState succState = data.addState(succ);
//                            data.addFlow(prev, succState);
//                            nextStep(succState, data, all);
//                        }
//                    }
//                    succs.andWith(succ.not());
//                    succ = succs.satOne(game.getFirstBDDVariables(), false);
////                    succ = succs.fullSatOne();
//                }
//            } else {
//                //System.out.println("nothing fireable (env transition) in mcut");                
//            }
//        }
//    }
//
//    public BDDPetriGame getGame() {
//        return game;
//    }
}
