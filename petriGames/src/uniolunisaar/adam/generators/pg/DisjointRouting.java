package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.util.PNWTTools;

/**
 * 
 * @author Jesko Hecking-Harbusch
 *
 * Give transition system (without loops) and number of players, forces disjoint routing on TS (-copies per player) 
 */

public class DisjointRouting {
	
	public static PetriGame disjointRouting (int nb_players) {
		TransitionSystem ts = getTransitionsSystem(nb_players);
		PetriGame pg = new PetriGame("DisjointRouting" + nb_players);
		PNWTTools.setConditionAnnotation(pg, Condition.Objective.A_SAFETY);
		// create net
		for (int player = 0; player < nb_players; ++player) {
			for (State s : ts.getNodes()) {
				pg.createEnvPlace(s.getId() + "_" + player);
			}
			Place firstStep = pg.createPlace("first" + player);
			firstStep.setInitialToken(1);
			pg.getPlace(ts.getInitialState().getId() + "_" + player).setInitialToken(1);
			for (Arc a : ts.getEdges()) {
				String source = a.getSourceId() + "_" + player;
				String target = a.getTargetId() + "_" + player;
				Transition t = pg.createTransition(source + "_" + target);
				pg.createFlow(source, t.getId());
				pg.createFlow(t.getId(), target);
				Place active = pg.createEnvPlace(t.getId() + "_active");
				pg.createFlow(active, t);
				pg.createFlow(t, active);
				if (a.getSourceId().equals("s0")) {
					Transition choice = pg.createTransition();
					pg.createFlow(firstStep, choice);
					pg.createFlow(choice, active);
				} else {
					active.setInitialToken(1);
				}
			}
		}
		// create bad places
		for (int player1 = 0; player1 < nb_players - 1; ++player1) {
			Place bad = pg.createPlace("bad_s" + player1);
			pg.setBad(bad);
			for (int player2 = player1 + 1; player2 < nb_players; ++player2) {
				for (int i = 1; i < nb_players + 1; ++i) {
					Transition toBad = pg.createTransition();
					pg.createFlow("s" + i + "_" + player1, toBad.getId());
					pg.createFlow("s" + i + "_" + player2, toBad.getId());
					pg.createFlow(toBad, bad);
				}
			}
		}
		return pg;
	}
	
	private static TransitionSystem getTransitionsSystem(int nb_players) {
		TransitionSystem ts = new TransitionSystem();
		int nb_nodes = 2 + nb_players; // one path for each player between start and finish
		for (int i = 0; i < nb_nodes; ++i) {
			ts.createState("s" + i);
		}
		for (int i = 1; i <= nb_players; ++i) {
			ts.createArc("s0", "s" + i, "s0_s" + i);
			ts.createArc("s" + i, "s" + (nb_players + 1), "s" + i + "_s" + (nb_players + 1));
		}
		ts.setInitialState("s0");
		return ts;
	}
}
