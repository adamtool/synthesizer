package uniolunisaar.adam.generators.pgwt;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class RobotCell {

    public static PetriGameWithTransits generate(int size, int destroyable, boolean withPartition) {
        if (size < 2) {
            throw new RuntimeException("less than 2 robots and tools do not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("RobotCell_" + size);
        PGTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Robots
        for (int i = 0; i < size; ++i) {
            createRobot(net, i, size, destroyable, withPartition);
        }
        // Environment
        Place env = net.createEnvPlace("env" + 0);
        env.setInitialToken(1);
        for (int i = 0; i < destroyable; ++i) {
            Place env2 = net.createEnvPlace("env" + (i + 1));
            for (int j = 0; j < size; ++j) {
                for (int k = 0; k < size; ++k) {
                    Transition t = net.createTransition();
                    net.createFlow(env, t);
                    Place p = net.getPlace("R" + j + "_T" + k + "_D" + i);
                    net.createFlow(t, p);
                    net.createFlow(t, env2);
                    Transition t1 = p.getPostset().iterator().next();
                    net.createFlow(env2, t1);
                }
            }
            env = env2;
        }
        return net;
    }

    private static void createRobot(PetriGameWithTransits net, int id, int size, int destroyable, boolean withPartition) {
        int startTokenId = id + 1;
        Place p1 = null;
        for (int i = 0; i < size; ++i) {
            // changing tool
            Place p2 = p1;
            p1 = net.createPlace("R" + id + "_T" + i);
            if (id == i) {
                p1.setInitialToken(1);
            }
            if (i != 0) {
                Transition t = net.createTransition();
                net.createFlow(p1, t);
                net.createFlow(t, p2);
                t = net.createTransition();
                net.createFlow(p2, t);
                net.createFlow(t, p1);
            }
            // get destroyed
            for (int j = 0; j < destroyable; ++j) {
                Place des = net.createPlace("R" + id + "_T" + i + "_D" + j);
                Place bad = net.createPlace("R" + id + "_T" + i + "_B" + j);
                net.setBad(bad);
                if (withPartition) {
                    net.setPartition(bad, startTokenId);
                    net.setPartition(des, size + j + 1);
                }
                Transition t = net.createTransition();
                net.createFlow(des, t);
                net.createFlow(p1, t);
                net.createFlow(t, bad);
            }
            if (withPartition) {
                net.setPartition(p1, startTokenId);
            }
        }
    }
}
