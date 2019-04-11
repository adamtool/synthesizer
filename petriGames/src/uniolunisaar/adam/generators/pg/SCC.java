package uniolunisaar.adam.generators.pg;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * 
 * @author Niklas Metzger
 *  
 */

public class SCC {
	/**
	 * Creates a Petri net that contains i independent sccs
	 * 
	 * @param number of independt sccs
	 * @return The corresponding Petri Net
	 */
	public static PetriGame generatePetriNet(int size) throws FileNotFoundException{
		return scc(size);
	}
	
	private static PetriGame scc(int size) {
		if (size < 1)
			throw new RuntimeException("at least 1 scc!");
		PetriGame net = PGTools.createPetriGame("scc_" + size);
		PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
		Place env = net.createEnvPlace("env");
		env.setInitialToken(1);	
		Place bad = net.createPlace("bad");
		net.setBad(bad);
		//initial transitions
		for (int i = 1; i <= size; i++) {
			Transition t = net.createTransition("e_" + i);
			net.createFlow(env,t);
			Place p = net.createPlace("store_" + i);
			net.createFlow(t,p);
			for (int j = 1; j <= size; j++) {
				Transition single = net.createTransition("e_single_" + i + "_" + j);
				net.createFlow(env, single);
				net.createFlow(single, p);// storage place needed for single??
				}
			}
		for (int i = 1; i<=size; i++) {
			for (int j = 1; j <= size; j++) {
				Place p = net.createPlace("sys_" + i + "_" + j);
				net.createFlow(net.getTransition("e_" + j),p);
				net.createFlow(net.getTransition("e_single_" + i + "_" + j), p);
			}
		}
		for (int i = 1; i<=size; i++) {
			for (int j = 1; j <= size; j++) {
				createSCC(net,i,j);
			}
		}
		return net;
	}
	
	private static void createSCC (PetriGame net, int i, int j) {
		Place sys = net.getPlace("sys_" + i + "_" + j);
		createSCCrec(net,sys,i,j,1 , "s_"+i + "_" + j);
		
	}
	private static void createSCCrec(PetriGame net, Place p, int i, int j, int recdepth, String str) {
		if (recdepth > i)
			return;
		Transition t = net.createTransition();
		Transition t1 = net.createTransition();
		net.createFlow(p,t);
		net.createFlow(p,t1);
		str += "_" + recdepth;
		Place pnew1 = net.createPlace(str + "_left");//"sys_local_left _" + i + "_" + j + "_"+ recdepth + str);
		Place pnew2 = net.createPlace(str + "_right");//"sys_local_right _" + i + "_" + j + "_" + recdepth + str);
		net.createFlow(t,pnew1);
		net.createFlow(t1,pnew2);
		if (recdepth == i) {
			Place store_right = net.createEnvPlace(str + "end_" + i + "_right");
			Place store_left = net.createEnvPlace(str + "end_" + i + "_left");
			net.createFlow(t1,store_right);
			net.createFlow(t, store_left);
			Transition t_reload = net.createTransition();
			Transition t_reload11 = net.createTransition();
			net.createFlow(pnew1, t_reload);
			net.createFlow(pnew2, t_reload11);			
			net.createFlow(store_right, t_reload11);
			net.createFlow(store_left, t_reload);
			net.createFlow(t_reload, net.getPlace("sys_"+ i + "_" + j));
			net.createFlow(t_reload11, net.getPlace("sys_"+ i + "_" + j));
			
			//Bad local behavior
			Transition badtr = net.createTransition();
			net.createFlow(store_left, badtr);
			net.createFlow(net.getPlace("store_"+i), badtr);
			net.createFlow(badtr, net.getPlace("bad"));
		}
		createSCCrec(net, pnew1, i, j, recdepth + 1, str + "left");
		createSCCrec(net, pnew2, i, j, recdepth + 1, str + "right");
	}
	
}
