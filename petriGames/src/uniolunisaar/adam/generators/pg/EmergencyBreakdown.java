package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * todo adapt all the textes and stuff an dso
 *
 * @author Manuel Gieseking
 */
public class EmergencyBreakdown {

    /**
     * Benchmark for two enviroment token.
     *
     * @param nb_critical_machines
     * @param nb_noncritical_machines
     * @param withPartitioning
     * @return
     */
    public static PetriGame createSafetyVersion(int nb_critical_machines, int nb_noncritical_machines, boolean withPartitioning) {
        if (nb_critical_machines + nb_noncritical_machines < 2) {
            throw new RuntimeException("less than 2 two machines are not meaningful");
        }
        PetriGame net = PGTools.createPetriGame("Emergency breackdown with " + nb_critical_machines + " intruding " + nb_noncritical_machines + " points");
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place env1 = net.createEnvPlace("e1");
        env1.setInitialToken(1);
        Place eclear1 = net.createEnvPlace("ec1");
        Place eemerg1 = net.createEnvPlace("ee1");
        Place ecom1 = net.createEnvPlace("ecom1");
        Transition t = net.createTransition();
        net.createFlow(env1, t);
        net.createFlow(t, eclear1);
        net.createFlow(t, ecom1);
        t = net.createTransition();
        net.createFlow(env1, t);
        net.createFlow(t, eemerg1);
        net.createFlow(t, ecom1);
        Place env2 = net.createEnvPlace("e2");
        env2.setInitialToken(1);
        Place eclear2 = net.createEnvPlace("ec2");
        Place eemerg2 = net.createEnvPlace("ee2");
        Place ecom2 = net.createEnvPlace("ecom2");
        t = net.createTransition();
        net.createFlow(env2, t);
        net.createFlow(t, eclear2);
        net.createFlow(t, ecom2);
        t = net.createTransition();
        net.createFlow(env2, t);
        net.createFlow(t, eemerg2);
        net.createFlow(t, ecom2);

//        // critical machines
//        for (int i = 0; i < nb_critical_machines; i++) {
//            createMachine(net, ecom1, ecom2, i);
//            // bad places for critial
//            Place bad = net.createPlace("bad1_cr_" + i);
//            bad.putExtension("bad", true, ExtensionProperty.WRITE_TO_FILE);
//            t = net.createTransition();
//            net.createFlow(eemerg1, t);
//            net.createFlow(eemerg2, t);
//            net.createFlow(net.getPlace("on_" + i), t);
//            net.createFlow(t, bad);
//            bad = net.createPlace("bad2_cr_" + i);
//            bad.putExtension("bad", true, ExtensionProperty.WRITE_TO_FILE);
//            t = net.createTransition();
//            net.createFlow(eclear1, t);
//            net.createFlow(net.getPlace("off_" + i), t);
//            net.createFlow(t, bad);
//            bad = net.createPlace("bad3_cr_" + i);
//            bad.putExtension("bad", true, ExtensionProperty.WRITE_TO_FILE);
//            t = net.createTransition();
//            net.createFlow(eclear2, t);
//            net.createFlow(net.getPlace("off_" + i), t);
//            net.createFlow(t, bad);
//        }
        // critical machines
        for (int i = 0; i < nb_critical_machines; i++) {
            createMachine(net, ecom1, ecom2, i);
            // bad places for critial
            Place bad = net.createPlace("bad_cr_" + i);
            net.setBad(bad);
            t = net.createTransition();
            net.createFlow(eemerg1, t);
            net.createFlow(eemerg2, t);
            net.createFlow(net.getPlace("on_" + i), t);
            net.createFlow(t, bad);
            t = net.createTransition();
            net.createFlow(eclear1, t);
            net.createFlow(net.getPlace("off_" + i), t);
            net.createFlow(t, bad);
            t = net.createTransition();
            net.createFlow(eclear2, t);
            net.createFlow(net.getPlace("off_" + i), t);
            net.createFlow(t, bad);
        }

        // non-critical machines
        for (int i = nb_critical_machines; i < nb_critical_machines + nb_noncritical_machines; i++) {
            createMachine(net, ecom1, ecom2, i);
            // bad places for normal 
            Place bad = net.createPlace("bad_ncr_" + i);
            net.setBad(bad);
            t = net.createTransition();
            net.createFlow(eclear1, t);
            net.createFlow(eclear2, t);
            net.createFlow(net.getPlace("off_" + i), t);
            net.createFlow(t, bad);
            t = net.createTransition();
            net.createFlow(eemerg1, t);
            net.createFlow(net.getPlace("on_" + i), t);
            net.createFlow(t, bad);
            t = net.createTransition();
            net.createFlow(eemerg2, t);
            net.createFlow(net.getPlace("on_" + i), t);
            net.createFlow(t, bad);
        }

        // the merge transition
        t = net.createTransition();
        for (int i = 0; i < nb_critical_machines; i++) {
            net.createFlow(net.getPlace("merge1_" + i), t);
            net.createFlow(t, net.getPlace("merge2_" + i));
        }
        for (int i = nb_critical_machines; i < nb_critical_machines + nb_noncritical_machines; i++) {
            net.createFlow(net.getPlace("merge1_" + i), t);
            net.createFlow(t, net.getPlace("merge2_" + i));
        }

        return net;
    }

    private static void createMachine(PetriGame net, Place ecom1, Place ecom2, int id) {
        Place m1l = net.createPlace("m1l_" + id);
        m1l.setInitialToken(1);
        Place m1r = net.createPlace("m1r_" + id);
        m1r.setInitialToken(1);
        Place m2l = net.createPlace("m2l_" + id);
        Place m2r = net.createPlace("m2r_" + id);
        Transition t = net.createTransition();
        net.createFlow(m1l, t);
        net.createFlow(ecom1, t);
        net.createFlow(t, m2l);
        t = net.createTransition();
        net.createFlow(m1r, t);
        net.createFlow(ecom2, t);
        net.createFlow(t, m2r);
        t = net.createTransition();
        net.createFlow(m1l, t);
        net.createFlow(t, m2l);
        t = net.createTransition();
        net.createFlow(m1r, t);
        net.createFlow(t, m2r);
        Place merge1 = net.createPlace("merge1_" + id);
        Place merge2 = net.createPlace("merge2_" + id);
        t = net.createTransition();
        net.createFlow(m2l, t);
        net.createFlow(m2r, t);
        net.createFlow(t, merge1);
        t = net.createTransition();
        net.createFlow(merge1, t);
        net.createFlow(t, merge2);
        Place on = net.createPlace("on_" + id);
        Place off = net.createPlace("off_" + id);
        t = net.createTransition();
        net.createFlow(merge2, t);
        net.createFlow(t, on);
        t = net.createTransition();
        net.createFlow(merge2, t);
        net.createFlow(t, off);
    }
}
