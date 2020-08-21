package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.pg.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * Self-reconfiguring Robots example of the ADAM paper.
 *
 * @author Manuel Gieseking
 */
public class SelfOrganizingRobots {

    /**
     * Adapted the generate version below on 2018/11/30 to the benchmark family
     * of the Bengt Jonsson Festschrift submission. Now it is not possible
     * anymore to have a strategy which uses a wrong tool in any phase. In the
     * other version this is only guarented in the last phase.
     *
     * todo: add withPartition and withMaxToken abillity
     *
     * @param robots
     * @param tools
     * @param phases
     * @param withPartition
     * @param withMaxToken
     * @return
     */
    public static PetriGame generateImproved(int robots, int tools, int phases, boolean withPartition, boolean withMaxToken) {
        if (robots < 1) {
            throw new RuntimeException("less than 1 robots does not make any sense!");
        }
        if (tools < 1) {
            throw new RuntimeException("less than 1 tools does not make any sense!");
        }
        if (tools < robots) {
            throw new RuntimeException("less tools than robots does not make any sense!");
        }
        if (phases < 1) {
            throw new RuntimeException("you should at least destroy one tool.");
        }
        PetriGame net = PGTools.createPetriGame("SR_r" + robots + "_t" + tools + "_p" + phases);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        Place env = net.createEnvPlace("Env");
        env.setInitialToken(1);
        // phases
        for (int i = 0; i < phases; i++) {
            addPhaseImproved(net, robots, tools, i, withPartition, withMaxToken);
        }
        // robots
        Place work = net.createPlace("work");
        work.setInitialToken(1);
        Place c = net.createEnvPlace("C");
        // phases
        for (int i = 0; i < phases; i++) {
            Transition tIn = net.createTransition("tw" + i);
            net.createFlow(work, tIn);
            net.createFlow(net.getPlace("W" + i), tIn);
            net.createFlow(tIn, c);
        }
        Transition tOut = net.createTransition("nxt");
        net.createFlow(c, tOut);
        net.createFlow(tOut, env);
        net.createFlow(tOut, work);
        for (int i = 0; i < robots; i++) {
            Place rOut = net.createPlace("restart" + i);
            net.createFlow(rOut, tOut);
            for (int j = 0; j < tools; j++) {
                net.createPlace("r" + i + "t" + j);
            }
            for (int j = 0; j < phases; j++) {
                // get the robot working in this phase
                Place rIn = net.createPlace("R" + i + "P" + j);
                Transition tw = net.getTransition("tw" + j);
                net.createFlow(tw, rIn);
                // change the tool for each robot in each phase
                for (int k = 0; k < tools; k++) {
                    for (int l = 0; l < tools; l++) {
                        Transition t = net.createTransition();
                        net.createFlow(rIn, t);
                        net.createFlow(net.getPlace("r" + i + "t" + k), t);
                        net.createFlow(t, net.getPlace("r" + i + "t" + l));
                        net.createFlow(t, net.getPlace("R_" + i + "T_" + l + "P_" + j));
                        net.createFlow(t, rOut);
                    }
                }
            }
        }
        // add bad transitions
        for (int i = 0; i < robots; i++) {
            for (int j = 0; j < tools; j++) {
                for (int k = 0; k < phases; k++) {
                    Transition tb = net.getTransition("tb" + i + "_" + j + "_" + k);
                    net.createFlow(net.getPlace("R_" + i + "T_" + j + "P_" + k), tb);
                }
            }
        }
        // put initial token
        for (int i = 0; i < robots; i++) {
            net.getPlace("r" + i + "t" + i).setInitialToken(1);
        }
        return net;
    }

    private static PetriGame addPhaseImproved(PetriGame net, int robots, int tools, int id, boolean withPartition, boolean withMaxToken) {
        Place e0 = net.getPlace("Env");
        Place e1 = net.createEnvPlace("W" + id);
        Place p = net.createPlace("P" + id);
        p.setInitialToken(1);
        Place ep = net.createEnvPlace("S" + id);
        Transition t = net.createTransition();
        net.createFlow(p, t);
        net.createFlow(e0, t);
        net.createFlow(t, ep);
        Place bad = net.createPlace("bad" + id);
        net.setBad(bad);
        for (int i = 0; i < robots; i++) {
            for (int j = 0; j < tools; j++) {
                Place s = net.createPlace("R" + i + "T" + j + "P" + id);
                Place s2 = net.createPlace("R_" + i + "T_" + j + "P_" + id);
                Transition tr = net.createTransition();
                net.createFlow(ep, tr);
                net.createFlow(tr, s);
                net.createFlow(tr, e1);
                Transition tb = net.createTransition("tb" + i + "_" + j + "_" + id);
                net.createFlow(s, tb);
                net.createFlow(tb, bad);
            }
        }
        return net;
    }

    /**
     * more adaptive version created from the high-level idea 2018/06/13
     * 
     * This version only checks that no robots uses a destroyed tool, not 
     * that all tools are used
     *
     * todo: add withPartition and withMaxToken abillity
     *
     * @param robots
     * @param tools
     * @param phases
     * @param withPartition
     * @param withMaxToken
     * @return
     */
    public static PetriGame generate(int robots, int tools, int phases, boolean withPartition, boolean withMaxToken) {
        if (robots < 1) {
            throw new RuntimeException("less than 1 robots does not make any sense!");
        }
        if (tools < 1) {
            throw new RuntimeException("less than 1 tools does not make any sense!");
        }
        if (tools < robots) {
            throw new RuntimeException("less tools than robots does not make any sense!");
        }
        if (phases < 1) {
            throw new RuntimeException("you should at least destroy one tool.");
        }
        PetriGame net = PGTools.createPetriGame("SelfOrgaRobots_r" + robots + "_t" + tools + "_p" + phases);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        Place env = net.createEnvPlace("E0");
        env.setInitialToken(1);
        Place e1 = net.createEnvPlace("E1");
        // phases
        for (int i = 0; i < phases; i++) {
            addPhase(net, robots, tools, i, withPartition, withMaxToken);
        }
        // robots
        Place prog = net.createPlace("prog");
        prog.setInitialToken(1);
        Place e2 = net.createEnvPlace("E2");
        Transition tIn = net.createTransition();
        net.createFlow(prog, tIn);
        net.createFlow(e1, tIn);
        net.createFlow(tIn, e2);
        Transition tOut = net.createTransition();
        net.createFlow(e2, tOut);
        net.createFlow(tOut, env);
        net.createFlow(tOut, prog);
        for (int i = 0; i < robots; i++) {
            addRobot(net, i, tools, withPartition, withMaxToken);
            net.createFlow(tIn, net.getPlace("rI" + i));
            net.createFlow(net.getPlace("rO" + i), tOut);
        }
        // add bad transitions
        for (int i = 0; i < robots; i++) {
            for (int j = 0; j < tools; j++) {
                for (int k = 0; k < phases; k++) {
                    Transition tb = net.getTransition("tb" + i + "_" + j + "_" + k);
                    net.createFlow(net.getPlace("r" + i + "t" + j), tb);
                }
            }
        }
        // put initial token
        for (int i = 0; i < robots; i++) {
            net.getPlace("r" + i + "t" + i).setInitialToken(1);
        }
        return net;
    }

    private static PetriGame addPhase(PetriGame net, int robots, int tools, int id, boolean withPartition, boolean withMaxToken) {
        Place e0 = net.getPlace("E0");
        Place e1 = net.getPlace("E1");
        Place p = net.createPlace("P" + id);
        p.setInitialToken(1);
        Place ep = net.createEnvPlace("EP" + id);
        Transition t = net.createTransition();
        net.createFlow(p, t);
        net.createFlow(e0, t);
        net.createFlow(t, ep);
        Place bad = net.createPlace("bad" + id);
        net.setBad(bad);
        for (int i = 0; i < robots; i++) {
            for (int j = 0; j < tools; j++) {
                Place s = net.createPlace("R" + i + "T" + j + "P" + id);
                Transition tr = net.createTransition();
                net.createFlow(ep, tr);
                net.createFlow(tr, s);
                net.createFlow(tr, e1);
                Transition tb = net.createTransition("tb" + i + "_" + j + "_" + id);
                net.createFlow(s, tb);
                net.createFlow(tb, bad);
            }
        }
        return net;
    }

    private static PetriGame addRobot(PetriGame net, int id, int tools, boolean withPartition, boolean withMaxToken) {
        Place rIn = net.createPlace("rI" + id);
        Place rOut = net.createPlace("rO" + id);
        for (int i = 0; i < tools; i++) {
            net.createPlace("r" + id + "t" + i);
        }
        for (int i = 0; i < tools; i++) {
            Place ri = net.getPlace("r" + id + "t" + i);
            for (int j = 0; j < tools; j++) {
                Transition t = net.createTransition();
                net.createFlow(rIn, t);
                net.createFlow(ri, t);
                net.createFlow(t, net.getPlace("r" + id + "t" + j));
                net.createFlow(t, rOut);
            }
        }
        return net;
    }

    /**
     * Original version.
     *
     * @param size
     * @param destroy
     * @param withPartition
     * @param withMaxToken
     * @return
     */
    public static PetriGame generate(int size, int destroy, boolean withPartition, boolean withMaxToken) {
        if (size < 2) {
            throw new RuntimeException("less than 2 robots and tools does not make any sense!");
        }
        if (destroy < 1) {
            throw new RuntimeException("you should at least destroy one tool.");
        }
        PetriGame net = PGTools.createPetriGame("SelfOrgaRobots_" + size + "_" + destroy);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place env = net.createEnvPlace("env" + 0);
        env.setInitialToken(1);
        if (withMaxToken) {
            int maxtoken = size + 2 + destroy;
            calc.setManuallyFixedTokencount(net, maxtoken);
            // todo: hack to get it saved in file
//            env.putExtension("MAXTOKEN", maxtoken, ExtensionProperty.WRITE_TO_FILE);
        }
        Place prog = net.createPlace("prog");
        if (withPartition) {
            net.setPartition(prog, 1);
        }
        prog.setInitialToken(1);
        for (int d = 0; d < destroy; ++d) {
            // Robots
            for (int i = 0; i < size; ++i) {
                createRobot(net, i, size, d, withPartition);
            }

            Place env1 = net.createEnvPlace("envA" + (d + 1));
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    Transition t = net.createTransition();
                    net.createFlow(env, t);
                    net.createFlow(t, env1);
                    net.createFlow(t, net.getPlace("R" + i + "D" + j + "P" + d));
                }
            }
            Place env2 = net.createEnvPlace("envB" + (d + 1));
            Transition t = net.createTransition();
            net.createFlow(net.getPlace("prog"), t);
            net.createFlow(env1, t);
            net.createFlow(t, env2);
            net.createFlow(t, net.getPlace("change" + 0));
            env = env2;
        }
        // Bad is two robots would like to do the same job
        for (int i = 0; i < size; ++i) { // tools
            for (int j = 0; j < size; ++j) { // robots A
                Place bad = net.getPlace("R" + j + "B" + i);
                Place p = net.getPlace("R" + j + "T" + i);
                for (int k = j + 1; k < size; ++k) { // robots B
                    if (j != k) {
                        Transition t1 = net.createTransition();
                        net.createFlow(t1, bad);
                        net.createFlow(p, t1);
                        net.createFlow(net.getPlace("R" + k + "T" + i), t1);
                    }
                }
            }
        }
        return net;
    }

    private static void createRobot(PetriGame net, int rid, int size, int destroy, boolean withPartition) {
        for (int i = 0; i < size; ++i) {
            Place tool = null;
            if (destroy == 0) {
                tool = net.createPlace("R" + rid + "T" + i);
                if (rid == i) {
                    tool.setInitialToken(1);
                }
                if (withPartition) {
                    net.setPartition(tool, rid + 2);
                }
            } else {
                tool = net.getPlace("R" + rid + "T" + i);
            }
            Place destroyed = net.createPlace("R" + rid + "D" + i + "P" + destroy);
            // Bad if a destroyed tool is used
            Place bad = null;
            if (destroy == 0) {
                bad = net.createPlace("R" + rid + "B" + i);
                net.setBad(bad);
                if (withPartition) {
                    net.setPartition(bad, rid + 2);
                }
            } else {
                bad = net.getPlace("R" + rid + "B" + i);
            }
            Transition t = net.createTransition();
            net.createFlow(tool, t);
            net.createFlow(destroyed, t);
            net.createFlow(t, bad);
            if (withPartition) {
                net.setPartition(destroyed, size + 2 + destroy);
            }
        }

        // Changing the tools
        if (destroy == 0) {
            Place change = (rid == 0) ? net.createPlace("change" + rid) : net.getPlace("change" + rid);
            Place changeNext = (size - 1 == rid) ? net.getPlace("prog") : net.createPlace("change" + (rid + 1));
            if (withPartition) {
                net.setPartition(change, 1);
                net.setPartition(changeNext, 1);
            }
            // no changing
            Transition t = net.createTransition();
            net.createFlow(change, t);
            net.createFlow(t, changeNext);
            // really change
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    if (i != j) {
                        Transition t1 = net.createTransition();
                        net.createFlow(change, t1);
                        net.createFlow(t1, changeNext);
                        Place from = net.getPlace("R" + rid + "T" + i);
                        Place to = net.getPlace("R" + rid + "T" + j);
                        net.createFlow(from, t1);
                        net.createFlow(t1, to);
                    }
                }
            }
        }
    }
//   TODO: PRODUCES ERROR IN CREATING PETRI GAME STRATEGY!!!!!!!!!!!!!!!!!  2, 1
    // liegt wahrscheinlich daran, dass immer wieder neu angefangen werden kann zu spielen (wegen prog stelle)
//    public static PetriGame generate(int size, int destroy, boolean withPartition) {
//        if (size < 2) {
//            throw new RuntimeException("less than 2 robots and tools does not make any sense!");
//        }
//        PetriGame net = PGTools.createPetriGame("SelfOrgaRobots_" + size + "_" + destroy);
//
//        // Environment
//        Place env = net.createPlace("env" + 0);
//        env.putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
//        env.setInitialToken(1);
//        Place prog = net.createPlace("prog");
//        if (withPartition) {
//            prog.putExtension("token", 1, ExtensionProperty.WRITE_TO_FILE);
//        }
//        prog.setInitialToken(1);
//        for (int d = 0; d < destroy; ++d) {
//            // Robots
//            for (int i = 0; i < size; ++i) {
//                createRobot(net, i, size, d, withPartition);
//            }
//
//            Place env1 = net.createPlace("env" + (d + 1));
//            env1.putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
//            for (int i = 0; i < size; ++i) {
//                for (int j = 0; j < size; ++j) {
//                    Transition t = net.createTransition();
//                    net.createFlow(env, t);
//                    net.createFlow(t, env1);
//                    net.createFlow(t, net.getPlace("R" + i + "D" + j + "P" + d));
//                }
//            }
//            Transition t = net.createTransition();
//            net.createFlow(net.getPlace("prog"), t);
//            net.createFlow(env1, t);
//            net.createFlow(t, env1);
//            net.createFlow(t, net.getPlace("change" + 0));
//            env = env1;
//        }
//        // Bad is two robots would like to do the same job
//        for (int i = 0; i < size; ++i) { // tools
//            for (int j = 0; j < size; ++j) { // robots A
//                Place bad = net.getPlace("R" + j + "B" + i);
//                Place p = net.getPlace("R" + j + "T" + i);
//                for (int k = j + 1; k < size; ++k) { // robots B
//                    if (j != k) {
//                        Transition t1 = net.createTransition();
//                        net.createFlow(t1, bad);
//                        net.createFlow(p, t1);
//                        net.createFlow(net.getPlace("R" + k + "T" + i), t1);
//                    }
//                }
//            }
//        }
//        return net;
//    }
//
//    private static void createRobot(PetriGame net, int rid, int size, int destroy, boolean withPartition) {
//        for (int i = 0; i < size; ++i) {
//            Place tool = null;
//            if (destroy == 0) {
//                tool = net.createPlace("R" + rid + "T" + i);
//                if (rid == i) {
//                    tool.setInitialToken(1);
//                }
//                if (withPartition) {
//                    tool.putExtension("token", rid + 2, ExtensionProperty.WRITE_TO_FILE);
//                }
//            } else {
//                tool = net.getPlace("R" + rid + "T" + i);
//            }
//            Place destroyed = net.createPlace("R" + rid + "D" + i + "P" + destroy);
//            // Bad if a destroyed tool is used
//            Place bad = net.createPlace("R" + rid + "B" + i);
//            bad.putExtension("bad", true, ExtensionProperty.WRITE_TO_FILE);
//            Transition t = net.createTransition();
//            net.createFlow(tool, t);
//            net.createFlow(destroyed, t);
//            net.createFlow(t, bad);
//            if (withPartition) {
//                destroyed.putExtension("token", size + 2 + destroy, ExtensionProperty.WRITE_TO_FILE);
//                bad.putExtension("token", size + 2 + destroy, ExtensionProperty.WRITE_TO_FILE);
//            }
//        }
//
//        // Changing the tools
//        Place change = (rid == 0) ? net.createPlace("change" + rid) : net.getPlace("change" + rid);
//        Place changeNext = (size - 1 == rid) ? net.getPlace("prog") : net.createPlace("change" + (rid + 1));
//        if (withPartition) {
//            change.putExtension("token", 1, ExtensionProperty.WRITE_TO_FILE);
//            changeNext.putExtension("token", 1, ExtensionProperty.WRITE_TO_FILE);
//        }
//        // no changing
//        Transition t = net.createTransition();
//        net.createFlow(change, t);
//        net.createFlow(t, changeNext);
//        // really change
//        for (int i = 0; i < size; ++i) {
//            for (int j = 0; j < size; ++j) {
//                if (i != j) {
//                    Transition t1 = net.createTransition();
//                    net.createFlow(change, t1);
//                    net.createFlow(t1, changeNext);
//                    Place from = net.getPlace("R" + rid + "T" + i);
//                    Place to = net.getPlace("R" + rid + "T" + j);
//                    net.createFlow(from, t1);
//                    net.createFlow(t1, to);
//                }
//            }
//        }
//    }
}
