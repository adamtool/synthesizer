package uniolunisaar.adam.generators.pgwt;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.util.PGTools;


/**
 *
 * @author Niklas Metzger
 */
public class ProductionLine {
	/**
	 * Creates a Petri net simulating a production line.
	 * @param number of robots in the line
	 * @return
	 * @throws FileNotFoundException
	 */
	public static PetriGameWithTransits generatePetriNet(int size) throws FileNotFoundException{
		return productionLine(size);
	}


	
	private static PetriGameWithTransits productionLine(int size) throws FileNotFoundException{
		PetriGameWithTransits net = PGTools.createPetriGame("ProductionLine" + size);
		MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PGTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
		Place env = net.createEnvPlace("env");
		env.setInitialToken(1);	
		List<Place> postPlaces = new ArrayList<>();
				for (int j = 1; j<=size; j++) {
					Place p = net.createEnvPlace("e" + j);
					Place pcheck = net.createPlace("e" + j + "check");
					Place sysPlace = net.createPlace("s" + j);
					postPlaces.add(sysPlace);
					postPlaces.add(p);
				}
				for (int j = 1; j<=size; j++) {
						Transition t = net.createTransition();
						net.createFlow(env, t);
						for (Place p: postPlaces)
							net.createFlow(t, p);
							Place p_check = net.getPlace("e" +j + "check");
							net.createFlow(t, p_check);
							Place ppcheck = net.createPlace("e" + j + "check_new");
							net.createFlow(t,ppcheck);
				}
		for (int j = 1; j <= size; j++) {
				Transition t = net.createTransition();
				net.createFlow(env, t);
				for (Place p: postPlaces)
					net.createFlow(t, p);
		}		for (int i = 1; i <= size; i++){
			Place sysPlace = net.getPlace("s"+i);
			Place envPlace = net.getPlace("e"+i);
			Transition tdec1 = net.createTransition("tdec"+ i + "1");
			Transition tdec2 = net.createTransition("tdec" + i + "2");
			net.createFlow(sysPlace, tdec1);
			net.createFlow(sysPlace, tdec2);
			net.createFlow(envPlace, tdec2);
			Place postPlaceBad = net.createPlace("done" + i + "1");
			Place postPlaceGood = net.createPlace("done" + i + "2");
			net.createFlow(tdec1, postPlaceBad);
			net.createFlow(tdec2, postPlaceGood);
		}
		for (int i = 1; i <= size; i++){
			Place badPlace = net.createPlace("bad"+ i);
			net.setBad(badPlace);
			Transition t = net.createTransition("tbad" + i);
			Place p = net.getPlace("done" + i + 1);
			Place p_prime = net.getPlace("e" + i + "check");
			net.createFlow(p, t);
			net.createFlow(p_prime, t);
			net.createFlow(t,badPlace);
			if (i < size) {
				Place badPlace2 = net.createPlace("bad"+ i + "inner");
				net.setBad(badPlace2);
				Transition t_t = net.createTransition("tbad" + i + i);
				int j = i+1;
				Place pp = net.getPlace("done" + j + 2);
				Place pp_prime = net.getPlace("e" + i + "check_new"); 
				net.createFlow(pp, t_t);
				net.createFlow(pp_prime, t_t);
				net.createFlow(t_t,badPlace2);
			}
		}
		return net;
	}


    
}

