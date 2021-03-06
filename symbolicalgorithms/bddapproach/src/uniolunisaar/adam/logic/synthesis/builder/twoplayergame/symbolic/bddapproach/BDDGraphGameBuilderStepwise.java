package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach;

import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDGraphGameBuilderStepwise {

    public static <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            BDDState addInitialState(BDDGraph graph, BDDSolver<W, SO, SOP> solver) {
        BDDState init = graph.addState(solver.getOne(), solver);
        graph.setInitial(init);
        return init;
    }

    public static <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions>
            Pair<List<Flow>, List<BDDState>> addSuccessors(BDDState state, BDDGraph graph, BDDSolver<W, SO, SOP> solver) {
        // initial states
        if (graph.getInitial().equals(state)) {
            List<Flow> flows = new ArrayList<>();
            List<BDDState> states = new ArrayList<>();
            BDD inits = solver.getInitialDCSs();
//             inits = inits.and(solver.getBufferedDCSs()); //TODO:  can I get a cheaper solution then use the all buffered reachable states ?
            inits = inits.and(solver.getWellformed(0));
            BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
            while (!init.isZero()) {
//                BDDTools.printDecisionSets(init, true);
//                BDDTools.printDecodedDecisionSets(init, solver, true);
                BDDState initSucc = graph.addState(init, solver);
                initSucc.setEnvState(solver.isEnvState(init));
                initSucc.setBad(solver.isBadState(init));
                initSucc.setSpecial(solver.isSpecialState(init));
                states.add(initSucc);
                Flow f = graph.addFlow(state, initSucc, null);
                flows.add(f);
                inits = inits.and(init.not());
                init = inits.satOne(solver.getFirstBDDVariables(), false);
            }
            return new Pair<>(flows, states);
        }
        // others
        List<Flow> flows = new ArrayList<>();
        List<BDDState> states = new ArrayList<>();

        boolean envState = state.isEnvState();
        BDD succs = (envState) ? solver.getEnvSuccTransitions(state.getState()) : solver.getSystemSuccTransitions(state.getState());
        if (!succs.isZero()) {// is there a firable transition ?
//            succs = solver.getSuccs(succs); // TODO: can I get a cheaper solution then use the all buffered reachable states ? .and(states);
            succs = solver.getSuccs(succs).and(solver.getWellformed(0));

            BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
            while (!succ.isZero()) {
                String value = BDDTools.getDecodedDecisionSets(succ, solver);
                value = value.substring(0, value.indexOf("->"));
                BDDState succState = new BDDState(succ, -1, value);
                BDDState oldSuccState = graph.contains(succState.getState());
                if (oldSuccState != null) { // jump to every already visited cut
                    states.add(oldSuccState);
                    Flow f = graph.addFlow(state, oldSuccState, solver.getTransition(state.getState(), oldSuccState.getState()));
                    flows.add(f);
                } else {
                    succState = graph.addState(succState);
                    succState.setEnvState(solver.isEnvState(succState.getState()));
                    succState.setBad(solver.isBadState(succState.getState()));
                    succState.setSpecial(solver.isSpecialState(succState.getState()));
                    states.add(succState);
                    Flow f = graph.addFlow(state, succState, solver.getTransition(state.getState(), succState.getState()));
                    flows.add(f);
                }

                succs.andWith(succ.not());
                succ = succs.satOne(solver.getFirstBDDVariables(), false);
            }

        }
        return new Pair<>(flows, states);
    }
}
