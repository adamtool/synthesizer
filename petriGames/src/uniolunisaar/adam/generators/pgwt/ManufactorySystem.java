package uniolunisaar.adam.generators.pgwt;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * generates the Job Processing example of the ADAM paper
 *
 * @author Manuel Gieseking
 */
public class ManufactorySystem {

    /**
     *
     * generates the Job Processing example of the ADAM paper
     *
     * @param size
     * @param withPartition
     * @param withMaxToken
     * @return
     */
    public static PetriGameWithTransits generate(int size, boolean withPartition, boolean withMaxToken) {
        if (size < 2) {
            throw new RuntimeException("less than 2 machines does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("ManufactorySystem" + size);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // job desk
        Place start = net.createEnvPlace("S");
        start.setInitialToken(1);
        if (withMaxToken) {
            int maxtoken = size + 1;
            calc.setManuallyFixedTokencount(net, maxtoken);
            // todo: hack to get it saved in file
//            start.putExtension("MAXTOKEN", maxtoken, ExtensionProperty.WRITE_TO_FILE);
        }
        Place work = net.createPlace("sw");
        if (withPartition) {
            net.setPartition(work, 1);
        }
        for (int i = 0; i < size; ++i) {
            String name = "JD_M" + i;
            for (int j = i; j < size; ++j) {
                if (j != i) {
                    name += "M" + j;
                }
                Place m = net.createEnvPlace(name);
                Transition t = net.createTransition();
                net.createFlow(start, t);
                net.createFlow(t, work);
                net.createFlow(t, m);
            }
            // workstation
            Transition t = net.createTransition();
            Place m = net.createPlace("M" + i);
            net.createFlow(work, t);
            net.createFlow(t, m);
            Transition ready = net.createTransition();
            Place mReady = net.createPlace("OUT_M" + i);
            net.createFlow(m, ready);
            net.createFlow(ready, mReady);
            // to next machine
            if (i != 0) {
                Transition next = net.createTransition();
                Place mo = net.getPlace("M" + (i - 1));
                Place outo = net.getPlace("OUT_M" + (i - 1));
                net.createFlow(mo, next);
                net.createFlow(next, outo);
                net.createFlow(next, m);
            }
            // bad stuff
            Place bad = net.createPlace("bad" + i);
            net.setBad(bad);
            if (withPartition) {
                net.setPartition(m, (i + 1));
                net.setPartition(mReady, (i + 1));
                net.setPartition(bad, (i + 1));
            }
            Transition tbad = net.createTransition();
            net.createFlow(mReady, tbad);
            net.createFlow(tbad, bad);
        }

        // return to beginning 
        // new loop since we need all the constructed OUT_M places      
        // todo: just a new place because otherwise for the moment not bounded
        start = net.createEnvPlace("end");
        for (int i = 0; i < size; ++i) {
            String name = "JD_M" + i;
            for (int j = i; j < size; ++j) {
                if (j != i) {
                    name += "M" + j;
                }
                Transition home = net.createTransition();
                net.createFlow(home, start);
                net.createFlow(net.getPlace(name), home);
                for (int k = i; k <= j; ++k) {
                    net.createFlow(net.getPlace("OUT_M" + k), home);
                }
            }
        }
        return net;
    }
}
