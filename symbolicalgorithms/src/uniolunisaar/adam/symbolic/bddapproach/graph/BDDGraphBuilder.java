package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.LinkedList;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 * @author Manuel Gieseking
 */
public class BDDGraphBuilder {

    public static BDDGraph builtGraph(BDDSolver<? extends WinningCondition> solver) {
        return builtGraph(solver, false);
    }

    public static BDDGraph builtGraphStrategy(BDDSolver<? extends WinningCondition> solver) throws NoStrategyExistentException {
        if (!solver.existsWinningStrategy()) {
            throw new NoStrategyExistentException();
        }
        return builtGraph(solver, true);
    }

    private static BDDGraph builtGraph(BDDSolver<? extends WinningCondition> solver, boolean strategy) {

        String text = (strategy) ? "strategy" : "game";
        BDDGraph graph = new BDDGraph("Finite graph " + text + " of the net "
                + solver.getNet().getName());
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
        BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
        if (strategy) { // is strategy only add one initial state            
            BDDState in = graph.addState(init);
            in.setMcut(solver.isEnvState(init));
            graph.setInitial(in);
            todoStates.add(in);
        } else { // is graph add all initial states
            // Create a bufferstate where all initial states are childs
            BDDState in = graph.addState(solver.getOne());
            graph.setInitial(in);
            while (!init.isZero()) {
                BDDState initSucc = graph.addState(init);
                initSucc.setMcut(solver.isEnvState(init));
                graph.addFlow(in, initSucc, null);
                todoStates.add(initSucc);
                inits = inits.and(init.not());
                init = inits.satOne(solver.getFirstBDDVariables(), false);
            }
        }

        while (!todoStates.isEmpty()) {
            BDDState prev = todoStates.poll();
            boolean envState = prev.isMcut();
            BDD succs = (envState) ? solver.getEnvSuccTransitions(prev.getState()) : solver.getSystemSuccTransitions(prev.getState());
            if (!succs.isZero()) {// is there a firable transition ?
                // shift successors to the first variables
                succs = solver.getSuccs(succs);
                // todo: should all be good, comment next line
                succs = succs.and(states);
                BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
                while (!succ.isZero()) {
                    // only loops to mcuts are allowed, thus this is the only point
                    // in the program where we possibly can add a loop    
                    boolean succEnvState = solver.isEnvState(succ);
                    BDDState oldSuccState = graph.contains(succ);
                    if (succEnvState && oldSuccState != null) { // we found an existing mcut and it's a succ of this current cut
                        addFlow(solver, graph, prev, oldSuccState);
                    } else {
                        BDDState succState = graph.addState(succ);
                        succState.setMcut(succEnvState);
                        addFlow(solver, graph, prev, succState);
                        // take the next step
                        todoStates.add(succState);
                    }
                    if (strategy && !envState) { // if we like to have the strategy and don't have an env place, we only like to have one successor
                        break;
                    }
                    succs.andWith(succ.not());
                    succ = succs.satOne(solver.getFirstBDDVariables(), false);
                }
            }
        }
        return graph;
    }

    private static Flow addFlow(BDDSolver<? extends WinningCondition> solver, BDDGraph graph, BDDState pre, BDDState succ) {
        return graph.addFlow(pre, succ, solver.getTransition(pre.getState(), succ.getState()));
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
