package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * Generates the watchdog example (with AND without partial observation).
 *
 * Without partial observation: ============================ The enviroment set
 * one machine of the n machines on fire. A smoke detector sounds the alarm. The
 * watchdog can asked the smoke detector where the fire started and have to
 * extinguish the right machine.
 *
 * With partial observation: ========================= Same scenario as above,
 * but in this case the smoke detector do not reveal where the fire started.
 * Thus the watchdog has to go to each machine and take a look if it was set on
 * fire. Still the goal is to extinguish the right machine.
 *
 * @author Manuel Gieseking
 */
public class Watchdog {

    public static PetriGame generate(int nb_machines, boolean search, boolean partialObservation, boolean withPartition) {
        PetriGame net = (!search) ? generateSmartSmokeDetector(nb_machines, partialObservation, withPartition)
                : generateSearching(nb_machines, partialObservation, withPartition);
        return net;
    }

    private static PetriGame generateSmartSmokeDetector(int nb_machines, boolean partialObservation, boolean withPartition) {
        if (nb_machines < 1) {
            throw new RuntimeException("less than 1 machine does not make any sense!");
        }
        PetriGame net = PGTools.createPetriGame("The Watchdog (clever smoke detector) - " + nb_machines + " machines.");
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
//        if (partialObservation) {
//            PetriGameExtensionHandler.setPartialObservation(net, true);
//        }

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        //// smoke detector
        Place test = net.createEnvPlace("test");
        Place[] machines = new Place[nb_machines];
        //// set them on fire
        for (int i = 0; i < nb_machines; ++i) {
            String name = "M" + i;
            machines[i] = net.createPlace(name);
            if (withPartition) {
                net.setPartition(machines[i], 1);
            }
            Transition t = net.createTransition("b" + i);
            net.createFlow(env, t);
            net.createFlow(t, machines[i]);
            net.createFlow(t, test);
            if (partialObservation) {
                t.setLabel("b");
            }
        }
        // The watchdog
        Place dog = net.createPlace("dog");
        dog.setInitialToken(1);
        Place check = net.createPlace("checked");
        if (withPartition) {
            net.setPartition(dog, 2);
            net.setPartition(check, 2);
        }
        Transition t = net.createTransition("ttest");
        net.createFlow(dog, t);
        net.createFlow(test, t);
        net.createFlow(t, test);
        net.createFlow(t, check);
        t = net.createTransition();
        net.createFlow(dog, t);
        net.createFlow(t, check);
        //// bad place
        Place bad = net.createPlace("bad");
        net.setBad(bad);
        if (withPartition) {
            net.setPartition(bad, 2);
        }
        //// machines
        for (int i = 0; i < nb_machines; ++i) {
            Place m = net.createPlace("m" + i);
            if (withPartition) {
                net.setPartition(m, 2);
            }
            t = net.createTransition();
            net.createFlow(check, t);
            net.createFlow(t, m);
            // bad situations
            for (int j = 0; j < nb_machines; ++j) {
                if (i != j) {
                    Transition tbad = net.createTransition();
                    net.createFlow(m, tbad);
                    net.createFlow(machines[j], tbad);
                    net.createFlow(tbad, bad);
                }
            }
        }
        return net;
    }

    /**
     * Version of the watchdog, which is going from machine to machine to detect
     * the burning one. This should also have a solution in the partial order
     * version where we don't see which of the machines had been set on fire.
     *
     * @param nb_machines - number of machines in the factory.
     * @param partialObservation - should the partial observation version be
     * created.
     * @param withPartition - should a partioning of the places be created.
     * @return a Petri net where the watchdog have to search the burning
     * machine.
     */
    private static PetriGame generateSearching(int nb_machines, boolean partialObservation, boolean withPartition) {
        if (nb_machines < 1) {
            throw new RuntimeException("less than 1 machine does not make any sense!");
        }
        PetriGame net = PGTools.createPetriGame("The Watchdog (dump smoke detector) - " + nb_machines + " machines.");
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
//        if (partialObservation) {
//            PetriGameExtensionHandler.setPartialObservation(net, true);
//        }

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        //// smoke detector
        Place test = net.createEnvPlace("test");

        Place[] machines = new Place[nb_machines]; // place for visiting machine
        Place[] bmachines = new Place[nb_machines];// token indicates burning
        //// set them on fire
        for (int i = 0; i < nb_machines; ++i) {
            String name = "M" + i;
            machines[i] = net.createPlace(name);
            name = "Bc" + i;
            Place notBurning = net.createPlace(name);
            notBurning.setInitialToken(1);
            name = "B" + i;
            bmachines[i] = net.createPlace(name);
            if (withPartition) {
                net.setPartition(notBurning, 3 + i);
                net.setPartition(machines[i], 3 + i);
                net.setPartition(bmachines[i], 1);
            }
            // set on fire
            Transition t = net.createTransition("b" + i);
            net.createFlow(env, t);
            net.createFlow(t, bmachines[i]);
            net.createFlow(t, test);
            if (partialObservation) {
                t.setLabel("b");
            }
            // prepare for visiting
            t = net.createTransition("nburn" + i);
            net.createFlow(notBurning, t);
            net.createFlow(t, machines[i]);
//            if (partialObservation) { // with those equally labelled there shouldn't be a solution with partial observation
//                t.setLabel("c" + i);
//            }
            t = net.createTransition("burn" + i);
            net.createFlow(notBurning, t);
            net.createFlow(bmachines[i], t);
            net.createFlow(t, machines[i]);
            net.createFlow(t, bmachines[i]);
//            if (partialObservation) {
//                t.setLabel("c" + i);
//            }
        }
        // The watchdog
        Place[] checked = new Place[nb_machines + 1];
        checked[0] = net.createPlace("dog");
        checked[0].setInitialToken(1);
        for (int i = 0; i < nb_machines; i++) {
            checked[i + 1] = net.createPlace("checked" + i);
            if (withPartition) {
                net.setPartition(checked[i], 2);
                net.setPartition(checked[i + 1], 2);
            }
            Transition t = net.createTransition("ttest" + i);
            net.createFlow(checked[i], t);
            net.createFlow(test, t);
            net.createFlow(machines[i], t);
            net.createFlow(t, test);
            net.createFlow(t, checked[i + 1]);
        }
        //// bad place
        Place bad = net.createPlace("bad");
        net.setBad(bad);
        if (withPartition) {
            net.setPartition(bad, 2);
        }
        //// machines
        for (int i = 0; i < nb_machines; ++i) {
            // jump over testing machines
            Transition t = net.createTransition();
            net.createFlow(checked[i], t);
            net.createFlow(t, checked[nb_machines]);
            Place m = net.createPlace("m" + i);
            if (withPartition) {
                net.setPartition(m, 2);
            }
            t = net.createTransition();
            net.createFlow(checked[nb_machines - 1], t);
            net.createFlow(t, m);
            // bad situations
            for (int j = 0; j < nb_machines; ++j) {
                if (i != j) {
                    Transition tbad = net.createTransition();
                    net.createFlow(m, tbad);
                    net.createFlow(bmachines[j], tbad);
                    net.createFlow(tbad, bad);
                }
            }
        }
        return net;
    }

    /**
     * Not working version of the watchdog, which is going from machine to
     * machine to detect the burning one. Problem is that the system can allow
     * the enviroment everytime just to set the first machine on fire. Then
     * there is no need for searching.
     *
     * @param nb_machines - number of machines in the factory.
     * @param partialObservation - should the partial observation version be
     * created.
     * @param withPartition - should a partioning of the places be created.
     * @return a Petri net where the watchdog have to search the burning
     * machine.
     */
    private static PetriGame generateWrongSearching(int nb_machines, boolean partialObservation, boolean withPartition) {
        if (nb_machines < 1) {
            throw new RuntimeException("less than 1 machine does not make any sense!");
        }
        PetriGame net = PGTools.createPetriGame("The Watchdog (with partial observation) - " + nb_machines);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
//        if (partialObservation) {
//            PetriGameExtensionHandler.setPartialObservation(net, true);
//        }

        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        //// smoke detector
        Place test = net.createEnvPlace("test");

        Place[] machines = new Place[nb_machines];
        Place[] cmachines = new Place[nb_machines];
        //// set them on fire
        for (int i = 0; i < nb_machines; ++i) {
            String name = "M" + i;
            machines[i] = net.createPlace(name);
            if (withPartition) {
                net.setPartition(machines[i], 1);
            }
            name = "Mc" + i;
            cmachines[i] = net.createPlace(name);
            cmachines[i].setInitialToken(1);
            if (withPartition) {
                net.setPartition(cmachines[i], 3 + i);
            }
            Transition t = net.createTransition("b" + i);
            net.createFlow(env, t);
            net.createFlow(cmachines[i], t);
            net.createFlow(t, machines[i]);
            net.createFlow(t, test);
            if (partialObservation) {
                t.setLabel("b");
            }
        }
        // The watchdog
        Place[] checked = new Place[nb_machines + 1];
        checked[0] = net.createPlace("dog");
        checked[0].setInitialToken(1);
        for (int i = 0; i < nb_machines; i++) {
            checked[i + 1] = net.createPlace("checked" + i);
            if (withPartition) {
                net.setPartition(checked[i], 2);
                net.setPartition(checked[i + 1], 2);
            }
            Transition t = net.createTransition("ttesta" + i);
            net.createFlow(checked[i], t);
            net.createFlow(test, t);
            net.createFlow(cmachines[i], t);
            net.createFlow(t, test);
            net.createFlow(t, checked[i + 1]);
            net.createFlow(t, cmachines[i]);
            t = net.createTransition("ttestb" + i);
            net.createFlow(checked[i], t);
            net.createFlow(test, t);
            net.createFlow(machines[i], t);
            net.createFlow(t, test);
            net.createFlow(t, checked[i + 1]);
            net.createFlow(t, machines[i]);
        }
        //// bad place
        Place bad = net.createPlace("bad");
        net.setBad(bad);
        if (withPartition) {
            net.setPartition(bad, 2);
        }
        //// machines
        for (int i = 0; i < nb_machines; ++i) {
            // jump over testing machines
            Transition t = net.createTransition();
            net.createFlow(checked[i], t);
            net.createFlow(t, checked[nb_machines]);
            Place m = net.createPlace("m" + i);
            if (withPartition) {
                net.setPartition(m, 2);
            }
            t = net.createTransition();
            net.createFlow(checked[nb_machines - 1], t);
            net.createFlow(t, m);
            // bad situations
            for (int j = 0; j < nb_machines; ++j) {
                if (i != j) {
                    Transition tbad = net.createTransition();
                    net.createFlow(m, tbad);
                    net.createFlow(machines[j], tbad);
                    net.createFlow(tbad, bad);
                }
            }
        }
        return net;
    }
}
