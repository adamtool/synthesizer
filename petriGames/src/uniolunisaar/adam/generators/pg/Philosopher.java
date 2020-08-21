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
public class Philosopher {

    public static PetriGame generateGuided2(int n, boolean withPartition, boolean withMaxToken) {
        if (n < 2) {
            throw new RuntimeException("Error: need at least 2 philosophers");
        }
        // System
        PetriGame pn = PGTools.createPetriGame("Philosopher_guided_" + n);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        pn.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(pn, Condition.Objective.A_SAFETY);

        for (int i = 0; i < n; ++i) {
            generateOne(pn, false, i, n, withPartition, true);
        }
        // Environment        
        Place envS = pn.createEnvPlace("env" + 0);
        if (withMaxToken) {
            int maxToken = n * 2 + 1;
            calc.setManuallyFixedTokencount(pn, maxToken);
            // todo: hack to get it saved in file
//            envS.putExtension("MAXTOKEN", maxToken, ExtensionProperty.WRITE_TO_FILE);
        }
        int id1 = 1;
        Transition t1 = pn.getTransition("t" + id1);
        pn.createFlow(envS, t1);
        int id2 = 2;
        Transition t2 = pn.getTransition("t" + id2);
        pn.createFlow(envS, t2);
        for (int i = 1; i < n; ++i) {
            Place env = pn.createEnvPlace("env" + i);
            pn.createFlow(t1, env);
            pn.createFlow(t2, env);
            id1 += 5;
            id2 += 5;
            t1 = pn.getTransition("t" + id1);
            pn.createFlow(env, t1);
            t2 = pn.getTransition("t" + id2);
            pn.createFlow(env, t2);
        }
        pn.createFlow(t1, envS);
        pn.createFlow(t2, envS);
        envS.setInitialToken(1);

        // bad places
        for (int i = 0; i < n; ++i) {
            Place gi_eat = pn.getPlace("gi_eat" + i);
            Transition tbi = pn.createTransition("badT" + i);
            Place gni_eat = pn.getPlace("gin_eat" + i);
            Transition tbin = pn.createTransition("badTn" + i);
            Place bad = pn.createPlace("bad" + i);
            pn.setPartition(bad, pn.getPartition(gi_eat));
            pn.setBad(bad);
            Place badn = pn.createPlace("badn" + i);
            pn.setPartition(badn, pn.getPartition(gni_eat));
            pn.setBad(badn);
            pn.createFlow(gi_eat, tbi);
            pn.createFlow(gni_eat, tbin);
            pn.createFlow(tbi, bad);
            pn.createFlow(tbin, badn);
        }
        return pn;
    }

    public static PetriGame generateGuided(int n, boolean withPartition, boolean withMaxToken) {
        if (n < 2) {
            //todo: exception
            throw new RuntimeException("Error: need at least 2 philosophers");
        }
        // System
        PetriGame pn = PGTools.createPetriGame("Philosopher_guided_" + n);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        pn.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(pn, Condition.Objective.A_SAFETY);
        for (int i = 0; i < n; ++i) {
            generateOne(pn, false, i, n, withPartition, true);
        }
        // Environment        
        Place envS = pn.createEnvPlace("env" + 0);
        if (withMaxToken) {
            int maxToken = n * 2 + 1;
            calc.setManuallyFixedTokencount(pn, maxToken);
            // todo hack
//            envS.putExtension("MAXTOKEN", maxToken, ExtensionProperty.WRITE_TO_FILE);
        }
        int id1 = 1;
        Transition t1 = pn.getTransition("t" + id1);
        pn.createFlow(envS, t1);
        int id2 = 2;
        Transition t2 = pn.getTransition("t" + id2);
        pn.createFlow(envS, t2);
        for (int i = 1; i < n; ++i) {
            Place env = pn.createEnvPlace("env" + i);
            pn.createFlow(t1, env);
            pn.createFlow(t2, env);
            id1 += 5;
            id2 += 5;
            t1 = pn.getTransition("t" + id1);
            pn.createFlow(env, t1);
            t2 = pn.getTransition("t" + id2);
            pn.createFlow(env, t2);
        }
        pn.createFlow(t1, envS);
        pn.createFlow(t2, envS);
        envS.setInitialToken(1);

        // bad places
        for (int i = 0; i < n - 1; ++i) {
            Place gni_res = pn.getPlace("gin_res" + i);
            Place gni_res1 = pn.getPlace("gin_res" + (i + 1));
            Transition tb = pn.createTransition("badT" + i);
            Place bad = pn.createPlace("bad" + i);
            int token = i == 0 ? 1 : 4 + 2 * (i - 1);
            pn.setPartition(bad, token);
            pn.setBad(bad);
            pn.createFlow(gni_res, tb);
            pn.createFlow(gni_res1, tb);
            pn.createFlow(tb, bad);
        }
        return pn;
    }

    public static PetriGame generateIndividual(int n, boolean withPartition, boolean withMaxToken) {
        if (n < 2) {
            //todo: exception
            throw new RuntimeException("Error: need at least 2 philosophers");
        }
        if (withMaxToken) {
            // todo: !!!!!!!!!!!!
        }
        PetriGame pn = PGTools.createPetriGame("Philosopher_" + n);
        PNWTTools.setConditionAnnotation(pn, Condition.Objective.A_SAFETY);

        generateOne(pn, true, 0, n, withPartition, false);
        for (int i = 1; i < n; i++) {
            generateOne(pn, false, i, n, withPartition, false);
        }

        return pn;
    }

    private static void generateOne(PetriGame pn, boolean env, int count, int max, boolean withPartition, boolean guided) {
        int startToken = count == 0 ? 0 : 3 + 2 * (count - 1);
        if (guided) {
            startToken++;
        }

        Place gi;
        if (count == 0) {
            gi = pn.createPlace("gi" + count);
            gi.setInitialToken(1);
            if (withPartition) {
                pn.setPartition(gi, startToken + 1);
            }
        } else {
            gi = pn.getPlace("gin" + (count - 1));
        }

        Place gi_res = pn.createPlace("gi_res" + count);
        Place gi_eat = pn.createPlace("gi_eat" + count);
        Place sleep = pn.createPlace("sleep" + count);
        sleep.setInitialToken(1);
        Place eat = pn.createPlace("eat" + count);
        Place gin = (count < max - 1) ? pn.createPlace("gin" + count) : pn.getPlace("gi" + 0);
        gin.setInitialToken(1);
        Place gin_res = pn.createPlace("gin_res" + count);
        Place gin_eat = pn.createPlace("gin_eat" + count);
        if (env) {
            pn.setEnvironment(gi_res);
            pn.setEnvironment(sleep);
            pn.setEnvironment(eat);
            pn.setEnvironment(gin_res);
        } else if (withPartition) {
            pn.setPartition(gi_res, startToken);
            pn.setPartition(sleep, startToken);
            pn.setPartition(eat, startToken);
            pn.setPartition(gin_res, startToken);
        }
        if (withPartition) {
            pn.setPartition(gi_eat, (count == 0) ? startToken + 1 : startToken - 1);
            if (count < max - 1) {
                int add = (count == 0) ? 2 : +1;
                pn.setPartition(gin_eat, startToken + add);
                pn.setPartition(gin, startToken + add);
            } else {
                pn.setPartition(gin_eat, guided ? 2 : 1);
            }

        }
        Transition t1 = pn.createTransition();
        Transition t2 = pn.createTransition();
        Transition t3 = pn.createTransition();
        Transition t4 = pn.createTransition();
        Transition t5 = pn.createTransition();
        // t1
        pn.createFlow(gi, t1);
        pn.createFlow(sleep, t1);
        pn.createFlow(t1, gi_res);
        pn.createFlow(t1, gi_eat);
        // t2
        pn.createFlow(gi_res, t2);
        pn.createFlow(gin, t2);
        pn.createFlow(t2, gin_eat);
        pn.createFlow(t2, eat);
        // t3
        pn.createFlow(gin, t3);
        pn.createFlow(sleep, t3);
        pn.createFlow(t3, gin_res);
        pn.createFlow(t3, gin_eat);
        // t4
        pn.createFlow(gin_res, t4);
        pn.createFlow(gi, t4);
        pn.createFlow(t4, gi_eat);
        pn.createFlow(t4, eat);
        // t5
        pn.createFlow(gi_eat, t5);
        pn.createFlow(gin_eat, t5);
        pn.createFlow(eat, t5);
        pn.createFlow(t5, sleep);
        pn.createFlow(t5, gi);
        pn.createFlow(t5, gin);
    }
}
