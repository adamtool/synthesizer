package uniolunisaar.adam.generators.pg;

import java.io.FileNotFoundException;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 * The system tries to escape from the environment. The system wins if at least
 * one of its tokens escapes. Otherwise the environment wins this benchmark for
 * E_SAFETY.
 *
 * @author Jesko Hecking-Harbusch
 *
 */
public class Escape {

    /**
     * should have strategy for nb_sys > nb_env > 0
     *
     * @param nb_sys
     * @param nb_env
     * @param withPartitioning
     * @return
     * @throws ModuleException
     * @throws FileNotFoundException
     */
    public static PetriGame createESafetyVersion(int nb_sys, int nb_env, boolean withPartitioning) throws FileNotFoundException, ModuleException {
        if (nb_sys < 0 || nb_env < 0) {
            throw new RuntimeException("No system and no environment is not an interesting example.");
        }
        PetriGame net = createGeneralNet(nb_sys, nb_env, withPartitioning);
        addESafetyWinCondition(net, nb_sys, nb_env, withPartitioning);
        return net;
    }

    private static PetriGame createGeneralNet(int nb_sys, int nb_env, boolean withPartitioning) {
        PetriGame net = PGTools.createPetriGame("Escape with " + nb_sys + " escapees against " + nb_env + " captors");
        boolean initial = true;
        Place envCont = null;
        while (nb_sys > 0 && nb_env > 0) {
            int i = Math.max(nb_sys, nb_env);
            // Environment
            if (initial) {
                envCont = net.createEnvPlace("env" + i);
                net.setBad(envCont);
                envCont.setInitialToken(1);
            }
            createEnvChoice(net, i, envCont);

            // System
            Place sysIn = createSysChoice(net, i);
            sysIn.setInitialToken(1);

            envCont = addBadAndGood(net, i);

            initial = false;
            nb_sys--;
            nb_env--;
        }
        // only one of those happens
        for (int i = 0; i < nb_sys; ++i) {
            Place sysIn = createSysChoice(net, i);
            sysIn.setInitialToken(1);
        }
        for (int i = 0; i < nb_env; ++i) {
            createEnvChoice(net, i, envCont);
        }
        return net;
    }

    private static void createEnvChoice(PetriGame net, int i, Place start) {
        Place envL = net.createEnvPlace("envL" + i);
        Transition el = net.createTransition();
        net.createFlow(start, el);
        net.createFlow(el, envL);
        Place envR = net.createEnvPlace("envR" + i);
        Transition er = net.createTransition();
        net.createFlow(start, er);
        net.createFlow(er, envR);
    }

    private static Place createSysChoice(PetriGame net, int i) {
        Place sys = net.createPlace("esc" + i);
        sys.setInitialToken(1);
        Place sysL = net.createPlace("escL" + i);
        Transition l = net.createTransition();
        net.createFlow(sys, l);
        net.createFlow(l, sysL);
        Place sysR = net.createPlace("escR" + i);
        Transition r = net.createTransition();
        net.createFlow(sys, r);
        net.createFlow(r, sysR);
        return sys;
    }

    private static Place addBadAndGood(PetriGame net, int i) {
        Place sysL = net.getPlace("escL" + i);
        Place sysR = net.getPlace("escR" + i);
        Place envL = net.getPlace("envL" + i);
        Place envR = net.getPlace("envR" + i);
        Place bad = net.createPlace("badSys" + i);
        net.setBad(bad);
        Place badEnv = net.createEnvPlace("badEnv" + i);
        Transition bl = net.createTransition();
        net.createFlow(sysL, bl);
        net.createFlow(envL, bl);
        net.createFlow(bl, bad);
        net.createFlow(bl, badEnv);
        net.createTransit(sysL, bl, bad);
        net.createTransit(envL, bl, badEnv);
//        net.setTokenFlow(bl,  sysL.getId() + "->{" + bad.getId() + "}," + envL.getId() + "->{" + badEnv.getId() + "}");
        Transition br = net.createTransition();
        net.createFlow(sysR, br);
        net.createFlow(envR, br);
        net.createFlow(br, bad);
        net.createFlow(br, badEnv);
        net.createTransit(sysR, br, bad);
        net.createTransit(envR, br, badEnv);
//        net.setTokenFlow(br, sysR.getId() + "->{" + bad.getId() + "}," + envR.getId() + "->{" + badEnv.getId() + "}");
        Place good = net.createEnvPlace("goodEnv" + i);
        Place goodSys = net.createPlace("goodSys" + i);
        Transition gl = net.createTransition();
        net.createFlow(sysL, gl);
        net.createFlow(envR, gl);
        net.createFlow(gl, good);
        net.createFlow(gl, goodSys);
        net.createTransit(sysL, gl, goodSys);
        net.createTransit(envR, gl, good);
//        net.setTokenFlow(gl, sysL.getId() + "->{" + goodSys.getId() + "}," + envR.getId() + "->{" + good.getId() + "}");
        Transition gr = net.createTransition();
        net.createFlow(sysR, gr);
        net.createFlow(envL, gr);
        net.createFlow(gr, good);
        net.createFlow(gr, goodSys);
        net.createTransit(sysR, gr, goodSys);
        net.createTransit(envL, gr, good);
//        net.setTokenFlow(gr,  sysR.getId() + "->{" + goodSys.getId() + "}," + envL.getId() + "->{" + good.getId() + "}");
        return badEnv;
    }

    private static void addESafetyWinCondition(PetriGame net, int nb_sys, int nb_env, boolean withPartitioning) {
        PNWTTools.setConditionAnnotation(net, Condition.Objective.E_SAFETY);
    }
}
