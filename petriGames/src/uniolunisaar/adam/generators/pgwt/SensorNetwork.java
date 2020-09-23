package uniolunisaar.adam.generators.pgwt;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.pgwt.TransitCalculator;

/**
 * Generates a sensor network example with nb_sensor_ nodes sensors sensing some
 * values (range 1, ..., nb_values) and reporting the value to nb_sink_nodes
 * sink nodes. This examples shows how the token flows can be used to model
 * redundency.
 *
 * THE SAME ES LOOPUNROLLING? Was it just a try and on the recordnicing that it
 * didn't work out?
 *
 * @author Manuel Gieseking
 */
public class SensorNetwork {

    /**
     * Should have trategy since all chains will eventually reach the bad place
     * (when withNewChain = true, otherwise has strategy)
     *
     * @param nb_unrolling
     * @param withPartitioning
     * @return
     */
    public static PetriGameWithTransits createESafetyVersion(int nb_unrolling, boolean withNewChain, boolean withPartitioning) {
        if (nb_unrolling < 1) {
            throw new RuntimeException("less than 1 unrolling does not show the interesting situation.");
        }

        PetriGameWithTransits net = PGTools.createPetriGame("ESafety version of loop unrolling example for creating new chains with " + nb_unrolling + " unrollings.");
        PGTools.setConditionAnnotation(net, Condition.Objective.E_SAFETY);
        // merge transition
        Transition tmerge = net.createTransition();
//        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb_unrolling; i++) {
//            if (i == 0) {
//                if (withNewChain) {
//                    sb.append(">->{sysE").append(i).append("}");
//                } else {
//                    sb.append("sysI0->{sysE").append(i).append("}");
//                }
//            } else {
//                sb.append(", sysI").append(i - 1).append("->{sysE").append(i).append("}");
//            }
            Place sysE = net.createPlace("sysE" + i);
            net.createFlow(tmerge, sysE);
            Transition t = net.createTransition();
            net.createFlow(sysE, t);
            Place sysI = net.createPlace("sysI" + i);
            sysI.setInitialToken(1);
            net.createFlow(t, sysI);
            net.createFlow(sysI, tmerge);
            if (i == 0) {
                if (withNewChain) {
                    net.createInitialTransit(tmerge, sysE);
                } else {
                    net.createTransit(sysI, tmerge, sysE);
                }
            } else {
                net.createTransit(net.getPlace("sysI" + (i - 1)), tmerge, sysE);
            }
        }

        // Bad chain
        Place sysBE = net.createPlace("sysBE");
        net.setBad(sysBE);
        net.createFlow(tmerge, sysBE);
        Transition t = net.createTransition();
        net.createFlow(sysBE, t);
        Place sysI = net.createPlace("sysBI");
        sysI.setInitialToken(1);
        net.createFlow(t, sysI);
        net.createFlow(sysI, tmerge);

        net.createTransit(net.getPlace("sysI" + (nb_unrolling - 1)), tmerge, sysBE);
        net.createTransit(sysI, tmerge, sysBE);
//        sb.append(", sysI").append(nb_unrolling - 1).append("->{sysBE}");
//        sb.append(", sysBI->{sysBE}");
//        net.setTokenFlow(tmerge, sb.toString());

        try {
            TransitCalculator.automaticallyCreateTransitsForTransitlessTransitions(net);
        } catch (CouldNotCalculateException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return net;
    }
}
