package uniolunisaar.adam.generators.pgwt;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PGTools;

/**
 * Creates the burglar examples for n alarm systems
 *
 * @author Manuel Gieseking
 */
public class SecuritySystem {

    public static PetriGameWithTransits createReachabilityVersion(int nb_alarmSystems, boolean withPartitioning) {
        if (nb_alarmSystems < 2) {
            throw new RuntimeException("less than 2 intruding points are not "
                    + "interesting for a security system");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Security system with " + nb_alarmSystems + " intruding points. (Reachability)");
        PGTools.setConditionAnnotation(net, Condition.Objective.E_REACHABILITY);

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);

        Place[] alarmSystem = new Place[nb_alarmSystems];
        Place[] initAlarm = new Place[nb_alarmSystems];
        Transition[] infs = new Transition[nb_alarmSystems];
        //// set them on fire
        for (int i = 0; i < nb_alarmSystems; ++i) {
            String id = Tools.calcStringID(i);
            // environment
            Place env1 = net.createEnvPlace("env" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, env1);
            Place e = net.createEnvPlace("env" + id);
            // system
            alarmSystem[i] = net.createPlace(id);
            alarmSystem[i].setInitialToken(1);
            Place in = net.createPlace("in" + id);
            initAlarm[i] = net.createPlace("init" + id);
            if (withPartitioning) {
                net.setPartition(alarmSystem[i], (i + 1));
                net.setPartition(initAlarm[i], (i + 1));
                net.setPartition(in, (i + 1));
            }
            t = net.createTransition("t" + id);
            net.createFlow(env1, t);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, e);
            net.createFlow(t, in);
            t = net.createTransition("sol" + id);
            net.createFlow(in, t);
            net.createFlow(t, initAlarm[i]);
            t = net.createTransition("false" + id);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, initAlarm[i]);
            infs[i] = net.createTransition("inf" + id);
            net.createFlow(in, infs[i]);
            net.createFlow(infs[i], initAlarm[i]);
            // decide for alarm
            for (int j = 0; j < nb_alarmSystems; j++) {
                Place p = net.createPlace(id + Tools.calcStringID(j));
                if (withPartitioning) {
                    net.setPartition(p, (i + 1));
                }
                t = net.createTransition();
                net.createFlow(initAlarm[i], t);
                net.createFlow(t, p);
            }
        }
        // flows for informing the other systems
        for (int i = 0; i < nb_alarmSystems; i++) {
            for (int j = 0; j < nb_alarmSystems; j++) {
                if (i != j) {
                    net.createFlow(alarmSystem[j], infs[i]);
                    net.createFlow(infs[i], initAlarm[j]);
                }
            }
        }
        // how winning
        Place alarm = net.createPlace("alarm");
        net.setReach(alarm);
        if (withPartitioning) {
            net.setPartition(alarm, 1);
        }
        for (int i = 0; i < nb_alarmSystems; i++) {
            Transition t = net.createTransition("alarm" + i);
            String id = Tools.calcStringID(i);
            Place e = net.getPlace("env" + id);
            net.createFlow(e, t);
            net.createFlow(t, e);
            net.createFlow(t, alarm);
            for (int j = 0; j < nb_alarmSystems; j++) {
                Place p = net.getPlace(Tools.calcStringID(j) + id);
                net.createFlow(p, t);
            }
        }
        return net;
    }

    public static PetriGameWithTransits createSafetyVersion(int nb_alarmSystems, boolean withPartitioning) {
        if (nb_alarmSystems < 2) {
            throw new RuntimeException("less than 2 intruding points are not "
                    + "interesting for a security system");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Security system with " + nb_alarmSystems + " intruding points. (Safety)");
        PGTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);

        Place[] alarmSystem = new Place[nb_alarmSystems];
        Place[] initAlarm = new Place[nb_alarmSystems];
        Transition[] infs = new Transition[nb_alarmSystems];
        //// set them on fire
        for (int i = 0; i < nb_alarmSystems; ++i) {
            String id = Tools.calcStringID(i);
            // environment
            Place env1 = net.createEnvPlace("env" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, env1);
            Place e = net.createEnvPlace("env" + id);
            // system
            alarmSystem[i] = net.createPlace(id);
            alarmSystem[i].setInitialToken(1);
            Place in = net.createPlace("in" + id);
            initAlarm[i] = net.createPlace("init" + id);
            if (withPartitioning) {
                net.setPartition(alarmSystem[i], (i + 1));
                net.setPartition(initAlarm[i], (i + 1));
                net.setPartition(in, (i + 1));
            }
            t = net.createTransition("t" + id);
            net.createFlow(env1, t);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, e);
            net.createFlow(t, in);
            t = net.createTransition("sol" + id);
            net.createFlow(in, t);
            net.createFlow(t, initAlarm[i]);
            t = net.createTransition("false" + id);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, initAlarm[i]);
            infs[i] = net.createTransition("inf" + id);
            net.createFlow(in, infs[i]);
            net.createFlow(infs[i], initAlarm[i]);
            // decide for alarm
            for (int j = 0; j < nb_alarmSystems; j++) {
                Place p = net.createPlace(id + Tools.calcStringID(j));
                if (withPartitioning) {
                    net.setPartition(p, (i + 1));
                }
                t = net.createTransition();
                net.createFlow(initAlarm[i], t);
                net.createFlow(t, p);
            }
        }
        // flows for informing the other systems
        for (int i = 0; i < nb_alarmSystems; i++) {
            for (int j = 0; j < nb_alarmSystems; j++) {
                if (i != j) {
                    net.createFlow(alarmSystem[j], infs[i]);
                    net.createFlow(infs[i], initAlarm[j]);
                }
            }
        }
        // how winning
        // create bad places
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Place error = net.createPlace("error" + id);
            net.setBad(error);
            if (withPartitioning) {
                net.setPartition(error, (i + 1));
            }
        }
        // create transitions
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Place eSolely = net.getPlace("env" + i);
            Place e = net.getPlace("env" + id);
            for (int j = 0; j < nb_alarmSystems; j++) {// each alarm system
                id = Tools.calcStringID(j);
                for (int l = 0; l < nb_alarmSystems; l++) { // each false report
                    // for the env places before EA, EB, etc
                    Transition t = net.createTransition("alarma" + i + "_" + j + "_" + l);
                    net.createFlow(eSolely, t);
                    net.createFlow(t, eSolely);
                    net.createFlow(t, net.getPlace("error" + id));
                    Place p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                    if (i == l) {
                        continue;
                    }
                    // for the EA, EB, etc places
                    t = net.createTransition("alarm" + i + "_" + j + "_" + l);
                    net.createFlow(e, t);
                    net.createFlow(t, e);
                    net.createFlow(t, net.getPlace("error" + id));
                    p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                }
            }
        }
        return net;
    }

    /**
     * Creates the example for the alarm system presented in the high-level
     * representation paper for the festschrift of Bengt Jonsson.
     *
     * The names differ to the createSafetyVersion, and instead of several bad
     * places there is only one and therefor also one good place. The bad
     * transitions consume the environment token and not putting it back.
     *
     * Problem, for the disjunct partitioning there is no other possibility then
     * adding an additional partition for the bad place. Here it would by (at
     * least for small nets) cheaper to just add as many bad places as security
     * systems.
     *
     * @param nb_alarmSystems
     * @param withPartitioning
     * @return
     */
    public static PetriGameWithTransits createSafetyVersionForHLRep(int nb_alarmSystems, boolean withPartitioning) {
        if (nb_alarmSystems < 2) {
            throw new RuntimeException("less than 2 intruding points are not "
                    + "interesting for a security system");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Security system with " + nb_alarmSystems + " intruding points. (Safety)");
        PGTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);

        Place[] alarmSystem = new Place[nb_alarmSystems];
        Place[] initAlarm = new Place[nb_alarmSystems];
        Transition[] infs = new Transition[nb_alarmSystems];
        //// set them on fire
        for (int i = 0; i < nb_alarmSystems; ++i) {
            String id = Tools.calcStringID(i);
            // environment
            Place env1 = net.createEnvPlace("C_" + id);
            Transition t = net.createTransition("i_" + id);
            net.createFlow(env, t);
            net.createFlow(t, env1);
            Place e = net.createEnvPlace("I_" + id);
            // system
            alarmSystem[i] = net.createPlace("S_" + id);
            alarmSystem[i].setInitialToken(1);
            Place in = net.createPlace("D_" + id);
            initAlarm[i] = net.createPlace("p_" + id);
            if (withPartitioning) {
                net.setPartition(alarmSystem[i], (i + 1));
                net.setPartition(initAlarm[i], (i + 1));
                net.setPartition(in, (i + 1));
            }
            t = net.createTransition("t_" + id);
            net.createFlow(env1, t);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, e);
            net.createFlow(t, in);
            t = net.createTransition("fr_" + id);
            net.createFlow(in, t);
            net.createFlow(t, initAlarm[i]);
            t = net.createTransition("fa_" + id);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, initAlarm[i]);
            infs[i] = net.createTransition("info_" + id);
            net.createFlow(in, infs[i]);
            net.createFlow(infs[i], initAlarm[i]);
            // decide for alarm
            for (int j = 0; j < nb_alarmSystems; j++) {
                Place p = net.createPlace(id + Tools.calcStringID(j));
                if (withPartitioning) {
                    net.setPartition(p, (i + 1));
                }
                t = net.createTransition();
                net.createFlow(initAlarm[i], t);
                net.createFlow(t, p);
            }
        }
        // flows for informing the other systems
        for (int i = 0; i < nb_alarmSystems; i++) {
            for (int j = 0; j < nb_alarmSystems; j++) {
                if (i != j) {
                    net.createFlow(alarmSystem[j], infs[i]);
                    net.createFlow(infs[i], initAlarm[j]);
                }
            }
        }
        // how winning
        // create bad place
        Place error = net.createPlace("Bad");
        net.setBad(error);
        if (withPartitioning) {
            net.setPartition(error, nb_alarmSystems + 1);
        }
        // create transitions
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Place eSolely = net.getPlace("C_" + id);
            Place e = net.getPlace("I_" + id);
            for (int j = 0; j < nb_alarmSystems; j++) {// each alarm system
                id = Tools.calcStringID(j);
                for (int l = 0; l < nb_alarmSystems; l++) { // each false report
                    // for the env places before EA, EB, etc
                    Transition t = net.createTransition("alarma" + i + "_" + j + "_" + l);
                    net.createFlow(eSolely, t);
                    net.createFlow(t, error);
                    Place p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                    if (i == l) {
                        continue;
                    }
                    // for the EA, EB, etc places
                    t = net.createTransition("alarm" + i + "_" + j + "_" + l);
                    net.createFlow(e, t);
                    net.createFlow(t, error);
                    p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                }
            }
        }
        // create good place
        Place good = net.createPlace("Good");
        if (withPartitioning) {
            net.setPartition(good, 1);
        }
        // create transitions
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Transition t = net.createTransition("g" + id);
            Place e = net.getPlace("I_" + id);
            net.createFlow(e, t);
            net.createFlow(t, good);
            for (int j = 0; j < nb_alarmSystems; j++) {// each alarm system
                net.createFlow(net.getPlace(Tools.calcStringID(j) + id), t);
            }
        }
        return net;
    }

    public static PetriGameWithTransits createSafetyVersionForBounded(int nb_alarmSystems, boolean withPartitioning) {
        if (nb_alarmSystems < 2) {
            throw new RuntimeException("less than 2 intruding points are not "
                    + "interesting for a security system");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Security system with " + nb_alarmSystems + " intruding points. (Safety)");
        PGTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);

        Place[] alarmSystem = new Place[nb_alarmSystems];
        Place[] initAlarm = new Place[nb_alarmSystems];
        Transition[] infs = new Transition[nb_alarmSystems];
        //// set them on fire
        for (int i = 0; i < nb_alarmSystems; ++i) {
            String id = Tools.calcStringID(i);
            // environment
            Place env1 = net.createEnvPlace("env" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, env1);
            Place e = net.createEnvPlace("env" + id);
            // system
            alarmSystem[i] = net.createPlace(id);
            alarmSystem[i].setInitialToken(1);
            Place in = net.createPlace("in" + id);
            initAlarm[i] = net.createPlace("init" + id);
            if (withPartitioning) {
                net.setPartition(alarmSystem[i], (i + 1));
                net.setPartition(initAlarm[i], (i + 1));
                net.setPartition(in, (i + 1));
            }
            t = net.createTransition("t" + id);
            net.createFlow(env1, t);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, e);
            net.createFlow(t, in);
            t = net.createTransition("sol" + id);
            net.createFlow(in, t);
            net.createFlow(t, initAlarm[i]);
            t = net.createTransition("false" + id);
            net.createFlow(alarmSystem[i], t);
            net.createFlow(t, initAlarm[i]);
            infs[i] = net.createTransition("inf" + id);
            net.createFlow(in, infs[i]);
            net.createFlow(infs[i], initAlarm[i]);
            // decide for alarm
            for (int j = 0; j < nb_alarmSystems; j++) {
                Place p = net.createPlace(id + Tools.calcStringID(j));
                if (withPartitioning) {
                    net.setPartition(p, (i + 1));
                }
                t = net.createTransition();
                net.createFlow(initAlarm[i], t);
                net.createFlow(t, p);
            }
        }
        // flows for informing the other systems
        for (int i = 0; i < nb_alarmSystems; i++) {
            for (int j = 0; j < nb_alarmSystems; j++) {
                if (i != j) {
                    net.createFlow(alarmSystem[j], infs[i]);
                    net.createFlow(infs[i], initAlarm[j]);
                }
            }
        }
        // how winning
        // create bad places
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Place error = net.createPlace("error" + id);
            net.setBad(error);
            if (withPartitioning) {
                net.setPartition(error, (i + 1));
            }
        }
        // create transitions
        for (int i = 0; i < nb_alarmSystems; i++) { // each env place
            String id = Tools.calcStringID(i);
            Place eSolely = net.getPlace("env" + i);
            Place e = net.getPlace("env" + id);
            for (int j = 0; j < nb_alarmSystems; j++) {// each alarm system
                id = Tools.calcStringID(j);
                for (int l = 0; l < nb_alarmSystems; l++) { // each false report
                    // for the env places before EA, EB, etc
                    Transition t = net.createTransition("alarma" + i + "_" + j + "_" + l);
                    net.createFlow(eSolely, t);
//                    net.createFlow(t, eSolely);
                    net.createFlow(t, net.getPlace("error" + id));
                    Place p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                    if (i == l) {
                        continue;
                    }
                    // for the EA, EB, etc places
                    t = net.createTransition("alarm" + i + "_" + j + "_" + l);
                    net.createFlow(e, t);
//                    net.createFlow(t, e);
                    net.createFlow(t, net.getPlace("error" + id));
                    p = net.getPlace(id + Tools.calcStringID(l));
                    net.createFlow(p, t);
                }
            }
        }
        return net;
    }

}
