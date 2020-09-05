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
 * The concurrent machines benchmark
 * @author Manuel Gieseking
 */
public class Workflow {

    public static PetriGameWithTransits generate2(int machines, int work_pieces, boolean withPartition) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Workflow_" + "M" + machines + "WP" + work_pieces);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
        // todo: since no other partioning possible use two token to much...
//        int maxtoken = (machines - 1) * 2 + work_pieces + 1 + 2;
//        net.putExtension("MAXTOKEN", maxtoken, ExtensionProperty.WRITE_TO_FILE);

        // Environment
        Place start = net.createEnvPlace("S");
        start.setInitialToken(1);
        Place go = net.createPlace("GO");
        if (withPartition) {
            net.setPartition(go, work_pieces + 1);
        }
        // todo: hack to get it saved in file
//        start.putExtension("MAXTOKEN", Integer.toString(maxtoken), ExtensionProperty.WRITE_TO_FILE);

        Place[] macs = new Place[machines];
        Place[] bad = new Place[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createEnvPlace("MAC" + i);
            bad[i] = net.createEnvPlace("bad" + i);
            net.setBad(bad[i]);
            Transition t = net.createTransition();
            net.createFlow(start, t);
            net.createFlow(t, macs[i]);
            net.createFlow(t, go);
        }
        Transition gotT = net.createTransition();
        net.createFlow(go, gotT);
        for (int i = 0; i < work_pieces; ++i) {
            Place bereit = net.createPlace("B" + i);
            bereit.setInitialToken(1);
            Place sstart = net.createPlace("S" + i);
            net.createFlow(bereit, gotT);
            net.createFlow(gotT, sstart);
            if (withPartition) {
                net.setPartition(bereit, (i + 1));
                net.setPartition(sstart, (i + 1));
            }
            for (int j = 0; j < machines; ++j) {
                // working
                Place m = net.createPlace("MW" + i + "M" + j);
                Transition t = net.createTransition();
                net.createFlow(sstart, t);
                net.createFlow(t, m);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                net.createFlow(t, bad[j]);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                }
            }
        }

        return net;
    }

    public static PetriGameWithTransits generate(int machines, int work_pieces, boolean withPartition, boolean withMaxtoken) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Workflow_" + "M" + machines + "WP" + work_pieces);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("S");
        start.setInitialToken(1);
        // todo: hack to get it saved in file
        if (withMaxtoken) {
            // todo: since no other partioning possible use two token to much...
            int maxtoken = (machines - 1) * 2 + work_pieces + 1 + 2;
            calc.setManuallyFixedTokencount(net, maxtoken);
//            start.putExtension("MAXTOKEN", Integer.toString(maxtoken), ExtensionProperty.WRITE_TO_FILE);
        }
        Place stop = net.createEnvPlace("E");

        Place[] macs = new Place[machines];
        Place[] tests = new Place[machines];
        Place[] mready = new Place[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("MAC" + i);
            tests[i] = net.createPlace("testP" + i);
//            macs[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            mready[i] = net.createPlace("MOK" + i);
//            mready[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            if (withPartition) {
                // todo: not correct in the general case
                net.setPartition(macs[i], work_pieces + 1 + i * 2);
                net.setPartition(mready[i], work_pieces + 1 + i * 2);
                net.setPartition(tests[i], work_pieces + 1 + i * 2 + 1);
            }
            Transition test = net.createTransition("test" + i);
            // testing
            net.createFlow(tests[i], test);
        }

        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
            net.createFlow(trans[i], stop);
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                    net.createFlow(trans[i], tests[j]);
                }
            }
        }
//        Transition t1 = net.createTransition();
//        net.createFlow(start, t1);
//        net.createFlow(t1, macs[0]);
//        net.createFlow(t1, macs[1]);
//        net.createFlow(t1, tests[0]);
//        net.createFlow(t1, tests[1]);
//        net.createFlow(t1, stop);
//        Transition t2 = net.createTransition();
//        net.createFlow(start, t2);
//        net.createFlow(t2, macs[0]);
//        net.createFlow(t2, macs[2]);
//        net.createFlow(t2, tests[0]);
//        net.createFlow(t2, tests[2]);
//        net.createFlow(t2, stop);
//        Transition t3 = net.createTransition();
//        net.createFlow(start, t3);
//        net.createFlow(t3, macs[1]);
//        net.createFlow(t3, macs[2]);
//        net.createFlow(t3, tests[1]);
//        net.createFlow(t3, tests[2]);
//        net.createFlow(t3, stop);

        for (int i = 0; i < work_pieces; ++i) {
            Place s = net.createPlace("S" + i);
            s.setInitialToken(1);
            if (withPartition) {
                net.setPartition(s, (i + 1));
            }
            for (int j = 0; j < machines; ++j) {
                // testing
                Transition test = net.getTransition("test" + j);
                net.createFlow(test, s);
                net.createFlow(s, test);
                // working
                Place m = net.createPlace("MW" + i + "M" + j);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + i + "M" + j);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                net.createFlow(t, mready[j]);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                    net.setPartition(bad, (i + 1));
                }
            }
        }

        return net;
    }

    /**
     * New annotation of places for CAV2015
     *
     * @param machines
     * @param work_pieces
     * @param withPartition
     * @param withMaxtoken
     * @return
     */
    public static PetriGameWithTransits generateNewAnnotationPoster(int machines, int work_pieces, boolean withPartition, boolean withMaxtoken) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Workflow_" + "M" + machines + "WP" + work_pieces);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("Env");
        start.setInitialToken(1);
        // todo: hack to get it saved in file
        if (withMaxtoken) {
            // todo: since no other partioning possible use two token to much...
            int maxtoken = (machines - 1) * 2 + work_pieces + 1 + 2;
            calc.setManuallyFixedTokencount(net, maxtoken);
//            start.putExtension("MAXTOKEN", Integer.toString(maxtoken), ExtensionProperty.WRITE_TO_FILE);
        }
        Place stop = net.createEnvPlace("e");

        Place[] macs = new Place[machines];
        Place[] tests = new Place[machines];
        Place[] mready = new Place[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("A" + i);
            tests[i] = net.createPlace("testP" + i);
//            macs[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            mready[i] = net.createPlace("G" + i);
//            mready[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            if (withPartition) {
                // todo: not correct in the general case
                net.setPartition(macs[i], work_pieces + 1 + i * 2);
                net.setPartition(mready[i], work_pieces + 1 + i * 2);
                net.setPartition(tests[i], work_pieces + 1 + i * 2 + 1);
            }
            Transition test = net.createTransition("test" + i);
            // testing
            net.createFlow(tests[i], test);
        }

        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
            net.createFlow(trans[i], stop);
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                    net.createFlow(trans[i], tests[j]);
                }
            }
        }
//        Transition t1 = net.createTransition();
//        net.createFlow(start, t1);
//        net.createFlow(t1, macs[0]);
//        net.createFlow(t1, macs[1]);
//        net.createFlow(t1, tests[0]);
//        net.createFlow(t1, tests[1]);
//        net.createFlow(t1, stop);
//        Transition t2 = net.createTransition();
//        net.createFlow(start, t2);
//        net.createFlow(t2, macs[0]);
//        net.createFlow(t2, macs[2]);
//        net.createFlow(t2, tests[0]);
//        net.createFlow(t2, tests[2]);
//        net.createFlow(t2, stop);
//        Transition t3 = net.createTransition();
//        net.createFlow(start, t3);
//        net.createFlow(t3, macs[1]);
//        net.createFlow(t3, macs[2]);
//        net.createFlow(t3, tests[1]);
//        net.createFlow(t3, tests[2]);
//        net.createFlow(t3, stop);

        for (int i = 0; i < work_pieces; ++i) {
            Place s = net.createPlace("S" + i);
            s.setInitialToken(1);
            if (withPartition) {
                net.setPartition(s, (i + 1));
            }
            for (int j = 0; j < machines; ++j) {
                // testing
                Transition test = net.getTransition("test" + j);
                net.createFlow(test, s);
                net.createFlow(s, test);
                // working
                Place m = net.createPlace("M" + j + "" + i);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + j + "" + i);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                net.createFlow(t, mready[j]);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                    net.setPartition(bad, (i + 1));
                }
            }
        }

        return net;
    }

    /**
     * The new version which only has one place for testing and not as many as
     * machines are available.
     *
     *
     * @param machines
     * @param work_pieces
     * @param withPartition
     * @param withMaxtoken
     * @return
     */
    public static PetriGameWithTransits generateImprovedVersion(int machines, int work_pieces, boolean withPartition, boolean withMaxtoken) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("CM_" + "M" + machines + "WP" + work_pieces);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("Env");
        start.setInitialToken(1);
        if (withMaxtoken) {
            // todo: since no other partioning possible use one token to much...
            int maxtoken = work_pieces + 1 + machines + 1;
            calc.setManuallyFixedTokencount(net, maxtoken);
        }
//        Place stop = net.createEnvPlace("e"); should not be necessary anymore to always have an env place

        Place[] macs = new Place[machines];
        // testing
        Place test = net.createPlace("testP");
        net.setPartition(test, work_pieces + 1);
        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("A" + i);
            if (withPartition) {
                // todo: not correct in the general case
                net.setPartition(macs[i], work_pieces + 1 + i + 1);
            }
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
//            net.createFlow(trans[i], stop);should not be necessary anymore to always have an env place
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                }
            }
            net.createFlow(trans[i], test);
        }

        // test transition
        Transition testT = net.createTransition("test");
        net.createFlow(test, testT);
        for (int i = 0; i < work_pieces; ++i) {
            Place s = net.createPlace("S" + i);
            s.setInitialToken(1);
            if (withPartition) {
                net.setPartition(s, (i + 1));
            }
            //testing: version all orders together
            net.createFlow(testT, s);
            net.createFlow(s, testT);

            // testing: version when each order does it separatly (still must adds things that not the type2 strategy (infinitely testing) is winning)
//            Transition testT = net.createTransition("test" + i);
//            net.createFlow(test, testT);
//            net.createFlow(testT, test);
//            net.createFlow(testT, s);
//            net.createFlow(s, testT);
            for (int j = 0; j < machines; ++j) {
                // working
                Place m = net.createPlace("M" + j + "" + i);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + j + "" + i);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                Place mready = net.createPlace("G" + j + "" + i);
                net.createFlow(t, mready);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                    net.setPartition(bad, (i + 1));
                    net.setPartition(mready, (i + 1));
                }
            }
        }

        return net;
    }

    /**
     * The new version has also as many places as machines for testing, but only
     * puts there the good machine.
     *
     * Version for Bengt Jonsson Festschrift
     *
     * @param machines
     * @param work_pieces
     * @param withPartition
     * @param withMaxtoken
     * @return
     */
    public static PetriGameWithTransits generateBJVersion(int machines, int work_pieces, boolean withPartition, boolean withMaxtoken) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("CM_" + "M" + machines + "WP" + work_pieces);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("Env");
        start.setInitialToken(1);
        if (withMaxtoken) {
            // todo: since no other partioning possible use one token to much...
            int maxtoken = work_pieces + 1 + machines + 1;
            calc.setManuallyFixedTokencount(net, maxtoken);
        }
//        Place stop = net.createEnvPlace("e"); should not be necessary anymore to always have an env place

        Place[] macs = new Place[machines];
        // testing
        Place[] tests = new Place[machines];
        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("OK" + i);
            tests[i] = net.createPlace("ERR" + i);
            if (withPartition) {
                net.setPartition(macs[i], work_pieces + 1 + i);
                net.setPartition(tests[i], work_pieces + 1 + i);
            }
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
//            net.createFlow(trans[i], stop);should not be necessary anymore to always have an env place
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                }
            }
            net.createFlow(trans[i], tests[i]);
            // test transition
            Transition testT = net.createTransition("test" + i);
            net.createFlow(tests[i], testT);
        }

        for (int i = 0; i < work_pieces; ++i) {
            Place s = net.createPlace("Sys" + i);
            s.setInitialToken(1);
            if (withPartition) {
                net.setPartition(s, (i + 1));
            }
            //testing: version all orders together
            for (int j = 0; j < machines; j++) {
                Transition ttest = net.getTransition("test" + j);
                net.createFlow(ttest, s);
                net.createFlow(s, ttest);
            }

            // testing: version when each order does it separatly (still must adds things that not the type2 strategy (infinitely testing) is winning)
//            Transition testT = net.createTransition("test" + i);
//            net.createFlow(test, testT);
//            net.createFlow(testT, test);
//            net.createFlow(testT, s);
//            net.createFlow(s, testT);
            for (int j = 0; j < machines; ++j) {
                // working
                Place m = net.createPlace("M" + j + "" + i);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + j + "" + i);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                Place mready = net.createPlace("G" + j + "" + i);
                net.createFlow(t, mready);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                    net.setPartition(bad, (i + 1));
                    net.setPartition(mready, (i + 1));
                }
            }
        }

        return net;
    }

    /**
     * Eventually concurrency preserving CAV2015 doch nicht gemacht nur in datei
     * geaendert
     *
     * @param machines
     * @param work_pieces
     * @param withPartition
     * @param withMaxtoken
     * @return
     */
    public static PetriGameWithTransits generateCP(int machines, int work_pieces, boolean withPartition, boolean withMaxtoken) {
        if (machines < 2 || work_pieces < 1) {
            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
        }
        PetriGameWithTransits net = PGTools.createPetriGame("Workflow_" + "M" + machines + "WP" + work_pieces);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("Env");
        start.setInitialToken(1);
        // todo: hack to get it saved in file
        if (withMaxtoken) {
            // todo: since no other partioning possible use two token to much...
            int maxtoken = (machines - 1) * 2 + work_pieces + 1 + 2;
            calc.setManuallyFixedTokencount(net, maxtoken);
//            start.putExtension("MAXTOKEN", Integer.toString(maxtoken), ExtensionProperty.WRITE_TO_FILE);
        }
        Place stop = net.createEnvPlace("e");

        Place[] macs = new Place[machines];
        Place[] tests = new Place[machines];
        Place[] mready = new Place[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("A" + i);
            tests[i] = net.createPlace("testP" + i);
//            macs[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            mready[i] = net.createPlace("G" + i);
//            mready[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
            if (withPartition) {
                // todo: not correct in the general case
                net.setPartition(macs[i], work_pieces + 1 + i * 2);
                net.setPartition(mready[i], work_pieces + 1 + i * 2);
                net.setPartition(tests[i], work_pieces + 1 + i * 2 + 1);
            }
            Transition test = net.createTransition("test" + i);
            // testing
            net.createFlow(tests[i], test);
        }

        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
            net.createFlow(trans[i], stop);
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                    net.createFlow(trans[i], tests[j]);
                }
            }
        }
//        Transition t1 = net.createTransition();
//        net.createFlow(start, t1);
//        net.createFlow(t1, macs[0]);
//        net.createFlow(t1, macs[1]);
//        net.createFlow(t1, tests[0]);
//        net.createFlow(t1, tests[1]);
//        net.createFlow(t1, stop);
//        Transition t2 = net.createTransition();
//        net.createFlow(start, t2);
//        net.createFlow(t2, macs[0]);
//        net.createFlow(t2, macs[2]);
//        net.createFlow(t2, tests[0]);
//        net.createFlow(t2, tests[2]);
//        net.createFlow(t2, stop);
//        Transition t3 = net.createTransition();
//        net.createFlow(start, t3);
//        net.createFlow(t3, macs[1]);
//        net.createFlow(t3, macs[2]);
//        net.createFlow(t3, tests[1]);
//        net.createFlow(t3, tests[2]);
//        net.createFlow(t3, stop);

        for (int i = 0; i < work_pieces; ++i) {
            Place s = net.createPlace("S" + i);
            s.setInitialToken(1);
            if (withPartition) {
                net.setPartition(s, (i + 1));
            }
            for (int j = 0; j < machines; ++j) {
                // testing
                Transition test = net.getTransition("test" + j);
                net.createFlow(test, s);
                net.createFlow(s, test);
                // working
                Place m = net.createPlace("M" + j + "" + i);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + j + "" + i);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                net.createFlow(t, mready[j]);
                if (withPartition) {
                    net.setPartition(m, (i + 1));
                    net.setPartition(bad, (i + 1));
                }
            }
        }

        return net;
    }

// Error in creating the petri game strategy
//    public static PetriGame generate(int machines, int work_pieces, int activated, boolean withPartition) {
//        if (machines < 2 || work_pieces < 1) {
//            throw new RuntimeException("less than 2 machines or 1 work piece does not make any sense!");
//        }
//        PetriGame net = PGTools.createPetriGame("Workflow_" + "M" + machines + "WP" + work_pieces);
//        int maxtoken = work_pieces + ((activated > 0) ? activated : 1) + 1;
//        net.putExtension("MAXTOKEN", maxtoken, ExtensionProperty.WRITE_TO_FILE);
//
//        // Environment
//        Place start = net.createPlace("S");
//        start.putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
//        start.setInitialToken(1);
//        // todo: hack to get it saved in file
////        start.putExtension("MAXTOKEN", maxtoken, ExtensionProperty.WRITE_TO_FILE);        
//        Place stop = net.createPlace("E");
//        stop.putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
//
//        Place[] macs = new Place[machines];
//        Place[] mready = new Place[machines];
//        for (int i = 0; i < machines; ++i) {
//            macs[i] = net.createPlace("MAC" + i);
////            macs[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE);
//            mready[i] = net.createPlace("MOK" + i);
////            mready[i].putExtension("env", true, ExtensionProperty.WRITE_TO_FILE
//            if (withPartition) {
//                // todo: not correct in the general case
//                macs[i].putExtension("token", work_pieces + 1 + i, ExtensionProperty.WRITE_TO_FILE);
//                mready[i].putExtension("token", work_pieces + 1 + i, ExtensionProperty.WRITE_TO_FILE);
//            }
//        }
//
//        //todo: use bin coefficent to do it right, just for getting the example for bernd
//        Transition t1 = net.createTransition();
//        net.createFlow(start, t1);
//        net.createFlow(t1, macs[0]);
//        net.createFlow(t1, macs[1]);
//        net.createFlow(t1, stop);
//        Transition t2 = net.createTransition();
//        net.createFlow(start, t2);
//        net.createFlow(t2, macs[0]);
//        net.createFlow(t2, macs[2]);
//        net.createFlow(t2, stop);
//        Transition t3 = net.createTransition();
//        net.createFlow(start, t3);
//        net.createFlow(t3, macs[1]);
//        net.createFlow(t3, macs[2]);
//        net.createFlow(t3, stop);
//
//        for (int i = 0; i < work_pieces; ++i) {
//            Place s = net.createPlace("S" + i);
//            s.setInitialToken(1);
//            if (withPartition) {
//                s.putExtension("token", (i + 1), ExtensionProperty.WRITE_TO_FILE);
//            }
//            for (int j = 0; j < machines; ++j) {
//                // testing
//                Transition test = net.createTransition("test" + i + "M" + j);
//                net.createFlow(macs[j], test);
//                net.createFlow(test, macs[j]);
//                net.createFlow(test, s);
//                net.createFlow(s, test);
//                net.createFlow(test, stop);
//                net.createFlow(stop, test);
//                // working
//                Place m = net.createPlace("MW" + i + "M" + j);
//                Transition t = net.createTransition();
//                net.createFlow(s, t);
//                net.createFlow(t, m);
//                Place bad = net.createPlace("B" + i + "M" + j);
//                bad.putExtension("bad", true, ExtensionProperty.WRITE_TO_FILE);
//                t = net.createTransition();
//                net.createFlow(m, t);
//                net.createFlow(t, bad);
//                t = net.createTransition();
//                net.createFlow(m, t);
//                net.createFlow(macs[j], t);
//                net.createFlow(t, mready[j]);
//                if (withPartition) {
//                    m.putExtension("token", (i + 1), ExtensionProperty.WRITE_TO_FILE);
//                    bad.putExtension("token", (i + 1), ExtensionProperty.WRITE_TO_FILE);
//                }
//            }
//        }
//
//        return net;
//    }
}
