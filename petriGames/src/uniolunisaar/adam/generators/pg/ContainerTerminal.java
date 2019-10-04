package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * Creates the Container-Terminal examples of the synt2017 paper
 *
 * @author Manuel Gieseking
 */
public class ContainerTerminal {

    /**
     * Currently the net is not 1-bounded and has the ndet pattern.
     *
     * @param nb_containerPlaces
     * @param withPartitioning
     * @return
     */
    public static PetriGame createSafetyVersion(int nb_containerPlaces, boolean withPartitioning) {
        if (nb_containerPlaces < 2) {
            throw new RuntimeException("less than 2 container places are not really "
                    + "interesting for a terminal.");
        }
        PetriGame net = PGTools.createPetriGame("Container terminal with " + nb_containerPlaces + " container places. (Safety)");
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        createContainerTerminal(net, nb_containerPlaces, withPartitioning);
        createContainer(net, withPartitioning);
        createContainerPlaces(net, nb_containerPlaces, withPartitioning);
        // Robot
        for (int i = 0; i < 2; i++) {
            createBattery(net, i, withPartitioning);
            createCommunicationUnit(net, i, withPartitioning);
            createTransportationOrChargingDecisionUnit(net, i, withPartitioning);
            createTransportationUnit(net, i, nb_containerPlaces, withPartitioning);
            createChargingUnit(net, i, withPartitioning);
        }
        createReset(net, nb_containerPlaces, withPartitioning);
        return net;
    }

    private static void createContainerTerminal(PetriGame net, int nb_containerPlaces, boolean withPartitioning) {
        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        Place voucher = net.createPlace("voucher");
        if (withPartitioning) {
            net.setPartition(voucher, (2));
        }
        for (int i = 0; i < nb_containerPlaces; i++) {
            Place cp = net.createEnvPlace("eCP" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, cp);
            net.createFlow(t, voucher);
        }
    }

    private static void createContainer(PetriGame net, boolean withPartitioning) {
        Place cont = net.createPlace("container");
        cont.setInitialToken(1);
        if (withPartitioning) {
            net.setPartition(cont, 1);
        }
        Place error = net.createPlace("error");
        net.setBad(error);
        if (withPartitioning) {
            net.setPartition(error, 1);
        }
        Transition t = net.createTransition();
        net.createFlow(cont, t);
        net.createFlow(t, error);
    }

    private static void createContainerPlaces(PetriGame net, int nb_containerPlaces, boolean withPartitioning) {
        Place delivered = net.createPlace("delivered");
        if (withPartitioning) {
            net.setPartition(delivered, 1);
        }
        for (int i = 0; i < nb_containerPlaces; i++) {
            Place cp = net.createPlace("sCP" + i);
            Place bad = net.createPlace("bad" + i);
            net.setBad(bad);
            if (withPartitioning) {
                net.setPartition(cp, (8 + i));
                net.setPartition(bad, (8 + i));
            }
            // bad
            Transition t = net.createTransition();
            net.createFlow(cp, t);
            net.createFlow(t, bad);
        }
    }

    private static void createBattery(PetriGame net, int robot_id, boolean withPartitioning) {
        // Environment
        Place on = net.createEnvPlace("on" + robot_id);
        on.setInitialToken(1);
        Place off = net.createEnvPlace("off" + robot_id);
        Place full = net.createEnvPlace("full" + robot_id);
        full.setInitialToken(1);
        Place med = net.createEnvPlace("medium" + robot_id);
        Place low = net.createEnvPlace("low" + robot_id);
        Place status = net.createPlace("status" + robot_id);
        if (withPartitioning) {
            net.setPartition(status, 3);
        }
        status.setInitialToken(1);
        Transition t = net.createTransition();
        net.createFlow(on, t);
        net.createFlow(low, t);
        net.createFlow(t, off);
    }

    private static void createCommunicationUnit(PetriGame net, int robot_id, boolean withPartitioning) {
        Place uninformed = net.createPlace("uninf" + robot_id);
        uninformed.setInitialToken(1);
        Place otherBatStatus = net.createPlace("otherBat" + robot_id);
        Place unOrinformed = net.createPlace("infUninf" + robot_id);
        unOrinformed.setInitialToken(1);
        if (withPartitioning) {
            net.setPartition(uninformed, 4);
            net.setPartition(unOrinformed, 5);
            net.setPartition(otherBatStatus, 4);
        }
        Transition t = net.createTransition();
        net.createFlow(uninformed, t);
        net.createFlow(unOrinformed, t);
        net.createFlow(t, unOrinformed);
        net.createFlow(t, otherBatStatus);
        Place status = net.getPlace("status" + robot_id);
        Place cont = net.getPlace("container");
        net.createFlow(status, t);
        net.createFlow(cont, t);
        net.createFlow(t, status);
        net.createFlow(t, cont);
    }

    private static void createTransportationOrChargingDecisionUnit(PetriGame net, int robot_id, boolean withPartitioning) {
        Place decide = net.createPlace("decide" + robot_id);
        decide.setInitialToken(1);
        Place transport = net.createPlace("transport" + robot_id);
        Place charge = net.createPlace("charge" + robot_id);
        if (withPartitioning) {
            net.setPartition(decide, 6);
            net.setPartition(transport, 6);
            net.setPartition(charge, 6);
        }
        Place p = net.getPlace("infUninf" + robot_id);
        Transition t = net.createTransition();
        net.createFlow(p, t);
        net.createFlow(decide, t);
        net.createFlow(t, p);
        net.createFlow(t, transport);
        t = net.createTransition();
        net.createFlow(p, t);
        net.createFlow(decide, t);
        net.createFlow(t, p);
        net.createFlow(t, charge);
    }

    private static void createTransportationUnit(PetriGame net, int robot_id, int nb_containerPlaces, boolean withPartitioning) {
        Place status = net.getPlace("status" + robot_id);
        Place cont = net.getPlace("container");
        Place voucher = net.getPlace("voucher");
        Place on = net.getPlace("on" + robot_id);
        Place transport = net.getPlace("transport" + robot_id);
        Place full = net.getPlace("full" + robot_id);
        Place medium = net.getPlace("medium" + robot_id);
        Place low = net.getPlace("low" + robot_id);
        Place delivered = net.getPlace("delivered");
        Place onRobot = net.createPlace("onRobot" + robot_id);
        Place transported = net.createPlace("transported" + robot_id);
        if (withPartitioning) {
            net.setPartition(onRobot, 1);
            net.setPartition(transported, 1);
        }

        Transition t = net.createTransition();
        net.createFlow(cont, t);
        net.createFlow(on, t);
        net.createFlow(transport, t);
        net.createFlow(t, onRobot);
        net.createFlow(t, on);

        t = net.createTransition();
        net.createFlow(cont, t);
        net.createFlow(voucher, t);
        net.createFlow(on, t);
        net.createFlow(transport, t);
        net.createFlow(t, onRobot);
        net.createFlow(t, on);
        net.createFlow(t, voucher);

        t = net.createTransition();
        net.createFlow(full, t);
        net.createFlow(status, t);
        net.createFlow(onRobot, t);
        net.createFlow(t, status);
        net.createFlow(t, medium);
        net.createFlow(t, transported);

        t = net.createTransition();
        net.createFlow(medium, t);
        net.createFlow(status, t);
        net.createFlow(onRobot, t);
        net.createFlow(t, status);
        net.createFlow(t, low);
        net.createFlow(t, transported);

        for (int i = 0; i < nb_containerPlaces; i++) {
            Place cp = net.getPlace("sCP" + i);
            t = net.createTransition();
            net.createFlow(transported, t);
            net.createFlow(t, cp);
            net.createFlow(t, delivered);
        }
    }

    private static void createChargingUnit(PetriGame net, int robot_id, boolean withPartitioning) {
        Place full = net.getPlace("full" + robot_id);
        Place medium = net.getPlace("medium" + robot_id);
        Place low = net.getPlace("low" + robot_id);
        Place status = net.getPlace("status" + robot_id);
        Place charge = net.getPlace("charge" + robot_id);
        Place on = net.getPlace("on" + robot_id);
        Place off = net.getPlace("off" + robot_id);
        Place discharging = net.createPlace("discharging" + robot_id);
        discharging.setInitialToken(1);
        Place charging = net.createPlace("charging" + robot_id);
        Place charged = net.createPlace("charged" + robot_id);
        if (withPartitioning) {
            net.setPartition(discharging, 7);
            net.setPartition(charging, 7);
            net.setPartition(charged, 7);
        }

        Transition t = net.createTransition();
        net.createFlow(on, t);
        net.createFlow(discharging, t);
        net.createFlow(charge, t);
        net.createFlow(t, off);
        net.createFlow(t, charging);

        t = net.createTransition();
        net.createFlow(full, t);
        net.createFlow(status, t);
        net.createFlow(charging, t);
        net.createFlow(t, status);
        net.createFlow(t, full);
        net.createFlow(t, charged);

        t = net.createTransition();
        net.createFlow(medium, t);
        net.createFlow(status, t);
        net.createFlow(charging, t);
        net.createFlow(t, status);
        net.createFlow(t, full);
        net.createFlow(t, charged);

        t = net.createTransition();
        net.createFlow(low, t);
        net.createFlow(status, t);
        net.createFlow(charging, t);
        net.createFlow(t, status);
        net.createFlow(t, medium);
        net.createFlow(t, charged);

        t = net.createTransition();
        net.createFlow(charged, t);
        net.createFlow(off, t);
        net.createFlow(t, on);
        net.createFlow(t, discharging);
    }

    private static void createReset(PetriGame net, int nb_containerPlaces, boolean withPartitioning) {
        Place good = net.createPlace("good");
//        Place r1 = net.createPlace("r1");
//        Place r2 = net.createPlace("r2");
        if (withPartitioning) {
            net.setPartition(good, 1);
//            r1.putExtension("token", 1, ExtensionProperty.WRITE_TO_FILE);
//            r2.putExtension("token", 1, ExtensionProperty.WRITE_TO_FILE);
        }
        for (int i = 0; i < nb_containerPlaces; i++) {
            Place ecp = net.getPlace("eCP" + i);
            Place scp = net.getPlace("sCP" + i);
            Transition t = net.createTransition();
            net.createFlow(ecp, t);
            net.createFlow(scp, t);
            net.createFlow(t, good);
        }

        Place cont = net.getPlace("container");
        Place env = net.getPlace("env");
        Place voucher = net.getPlace("voucher");
        Place delivered = net.getPlace("delivered");
        Place decide0 = net.getPlace("decide" + 0);
        Place decide1 = net.getPlace("decide" + 1);
        Place other0 = net.getPlace("otherBat" + 0);
        Place other1 = net.getPlace("otherBat" + 1);
        Place uninformed0 = net.getPlace("uninf" + 0);
        Place uninformed1 = net.getPlace("uninf" + 1);

        Transition t = net.createTransition();
        net.createFlow(delivered, t);
        net.createFlow(good, t);
        net.createFlow(voucher, t);
        net.createFlow(t, decide0);
        net.createFlow(t, decide1);
        net.createFlow(t, cont);
        net.createFlow(t, env);

        t = net.createTransition();
        net.createFlow(delivered, t);
        net.createFlow(good, t);
        net.createFlow(voucher, t);
        net.createFlow(other0, t);
        net.createFlow(t, uninformed0);
        net.createFlow(t, decide0);
        net.createFlow(t, decide1);
        net.createFlow(t, cont);
        net.createFlow(t, env);

        t = net.createTransition();
        net.createFlow(delivered, t);
        net.createFlow(good, t);
        net.createFlow(voucher, t);
        net.createFlow(other1, t);
        net.createFlow(t, uninformed1);
        net.createFlow(t, decide0);
        net.createFlow(t, decide1);
        net.createFlow(t, cont);
        net.createFlow(t, env);

        t = net.createTransition();
        net.createFlow(delivered, t);
        net.createFlow(good, t);
        net.createFlow(voucher, t);
        net.createFlow(other0, t);
        net.createFlow(other1, t);
        net.createFlow(t, uninformed0);
        net.createFlow(t, uninformed1);
        net.createFlow(t, decide0);
        net.createFlow(t, decide1);
        net.createFlow(t, cont);
        net.createFlow(t, env);
    }

}
