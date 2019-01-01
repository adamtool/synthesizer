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
 *
 * @author Manuel Gieseking
 */
public class Clerks {

    public static PetriGame generateNonCP(int size, boolean withPartition, boolean withMaxToken) {
        if (size < 1) {
            throw new RuntimeException("There should be at least one Clerk to sign the document.");
        }

        PetriGame net = PGTools.createPetriGame("Clerks_" + size);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        if (withMaxToken) {
            int maxToken = size + 2;
            calc.setManuallyFixedTokencount(net, maxToken);
            // todo: hack to get it saved in file
//            env.putExtension("MAXTOKEN", maxToken, ExtensionProperty.WRITE_TO_FILE);
        }
        for (int i = 0; i < size; i++) {
            Transition t = net.createTransition();
            net.createFlow(env, t);
            createOneClerkNonCP(net, t, i, withPartition);
        }
        net.createFlow(net.getPlace("end" + (size - 1)), net.getTransition("glue" + 0));
        // Bad place     

        // Did all clerks have signed the note?
        Place ready = net.createPlace("ready");
        // are they all Y or all N?
        Place good = net.createPlace("good");
        Place goodReady = net.createPlace("goodReady");

        // they are not all Y or all N
        Place bad = net.createPlace("bad");
        net.setBad(bad);
        Transition tbad = net.createTransition("tbad");
        Transition tGoodReady = net.createTransition();
        net.createFlow(ready, tbad);
        net.createFlow(tbad, bad);
        net.createFlow(ready, tGoodReady);
        net.createFlow(good, tGoodReady);
        net.createFlow(tGoodReady, goodReady);

        Transition yes = net.createTransition("yes");
        net.createFlow(yes, good);
        Transition no = net.createTransition("no");
        net.createFlow(no, good);
        for (int i = 0; i < size; ++i) {
            net.createFlow(net.getPlace("Y" + i), yes);
            net.createFlow(net.getPlace("N" + i), no);
            //  Did all clerks have signed the note?
            Transition tReady = net.createTransition("t_ready" + i);
            net.createFlow(tReady, ready);
            Place first = net.getPlace("first" + i);
            Place end = net.getPlace("end" + (i == 0 ? size - 1 : i - 1));
            net.createFlow(first, tReady);
            net.createFlow(end, tReady);
//            net.createFlow(tReady, end);
        }
        if (withPartition) {
            net.setPartition(good, 2);
            net.setPartition(bad, 1);
            net.setPartition(goodReady, 1);
            net.setPartition(ready, 1);
        }
        return net;
    }

    private static void createOneClerkNonCP(PetriGame net, Transition t, int count, boolean withPartition) {
        Place start = net.createEnvPlace("start" + count);
        Place firstClerk = net.createEnvPlace("first" + count);

        Place cl = net.createPlace("cl" + count);
        cl.setInitialToken(1);
        Place vote = net.createPlace("vote" + count);
        Place yes = net.createPlace("Y" + count);
        Place no = net.createPlace("N" + count);
        Place end = net.createPlace("end" + count);

        if (withPartition) {
            net.setPartition(cl, count + 2);
            net.setPartition(vote, 1);
            net.setPartition(end, 1);
            net.setPartition(yes, count + 2);
            net.setPartition(no, count + 2);
        }

        Transition t1 = net.createTransition();
        Transition t2 = net.createTransition();
        Transition t3 = net.createTransition();
        Transition glue = net.createTransition("glue" + count);

        net.createFlow(t, start);
        //t1
        net.createFlow(t1, vote);
        net.createFlow(t1, firstClerk);
        net.createFlow(start, t1);
        net.createFlow(cl, t1);
        //t2
        net.createFlow(vote, t2);
        net.createFlow(t2, yes);
        net.createFlow(t2, end);
        //t3
        net.createFlow(vote, t3);
        net.createFlow(t3, no);
        net.createFlow(t3, end);
        // Glue
        if (count > 0) {
            net.createFlow(net.getPlace("end" + (count - 1)), glue);
        }
        net.createFlow(glue, vote);
        net.createFlow(cl, glue);
    }

    public static PetriGame generateCP(int size, boolean withPartition, boolean withMaxToken) {
        if (size < 1) {
            throw new RuntimeException("There should be at least one Clerk to sign the document.");
        }
        if (withMaxToken) {
// todo : !!!!!!!!!
        }
        PetriGame net = PGTools.createPetriGame("Clerks_" + size);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        for (int i = 0; i < size; i++) {
            Transition t = net.createTransition();
            net.createFlow(env, t);
            createOneClerkCP(net, t, i, size, withPartition);
        }
        net.createFlow(net.getPlace("end" + (size - 1)), net.getTransition("glue" + 0));
        return net;
    }

    private static void createOneClerkCP(PetriGame net, Transition t, int count, int max, boolean withPartition) {
        int startToken = count * 2;
        Place start = net.createEnvPlace("start" + count);
        Place env = net.createEnvPlace("env" + count);

        Place cl = net.createPlace("cl" + count);
        cl.setInitialToken(1);
        Place vote = net.createPlace("votel" + count);
        Place yes = net.createPlace("Y" + count);
        Place no = net.createPlace("N" + count);
        Place end = net.createPlace("end" + count);
        Place bad = net.createPlace("bad" + count);
        net.setBad(bad);
        Place buf0 = net.createPlace("buf" + count);
        Place buf1 = net.createPlace("buff" + count);
        buf1.setInitialToken(1);

        if (withPartition) {
            net.setPartition(cl, (startToken + 1));
            net.setPartition(vote, (startToken + 1));
            net.setPartition(end, (startToken + 1));
            net.setPartition(yes, (startToken + 2));
            net.setPartition(no, (startToken + 2));
            net.setPartition(bad, (startToken + 2));
            if (count == 0) {
                net.setPartition(buf0, 2 * (max - 1) + 1);
            } else {
                net.setPartition(buf0, startToken - 1);
            }
            net.setPartition(buf1, startToken + 2);
        }

        Transition t1 = net.createTransition();
        Transition t2 = net.createTransition();
        Transition t3 = net.createTransition();
        Transition t4 = net.createTransition("glue" + count);
        Transition badT = net.createTransition("tbad" + count);

        net.createFlow(t, start);
        //t1
        net.createFlow(t1, vote);
        net.createFlow(t1, env);
        net.createFlow(start, t1);
        net.createFlow(cl, t1);
        //t2
        net.createFlow(buf1, t2);
        net.createFlow(vote, t2);
        net.createFlow(t2, yes);
        net.createFlow(t2, end);
        //t3
        net.createFlow(buf1, t3);
        net.createFlow(vote, t3);
        net.createFlow(t3, no);
        net.createFlow(t3, end);
        //t5
        net.createFlow(t4, vote);
        net.createFlow(t4, buf0);
        net.createFlow(cl, t4);
        if (count > 0) {
            net.createFlow(net.getPlace("end" + (count - 1)), t4);
        }
        net.createFlow(no, badT);
        net.createFlow(badT, bad);
    }
}
