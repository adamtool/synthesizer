package uniolunisaar.adam.generators.pg;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.pg.TransitCalculator;

/**
 * The environment decides which route is free and the first car should detect
 * the free route and communicate it the next car, and so on.
 *
 * @author Manuel Gieseking
 */
public class CarRouting {

    /**
     * should have strategy for nb_routes >=2 and nb_cars >=1
     *
     * workshop SCARE bad zwischenahn version
     *
     * @param nb_routes
     * @param nb_cars
     * @param withPartitioning
     * @return
     */
    public static PetriGame createEReachabilityVersion(int nb_routes, int nb_cars, boolean withPartitioning) {
        if (nb_routes < 2 || nb_cars < 1) {
            throw new RuntimeException("less than 2 routes or 1 car is not an interesting example");
        }
        PetriGame net = createGeneralNet(nb_routes, nb_cars, withPartitioning);
        addEReachabilityWinCondition(net, nb_routes, nb_cars, withPartitioning);
        return net;
    }

    /**
     * should have strategy for nb_routes >=2 and nb_cars >=1
     *
     * workshop SCARE bad zwischenahn version
     *
     * @param nb_routes
     * @param nb_cars
     * @param withPartitioning
     * @return
     */
    public static PetriGame createAReachabilityVersion(int nb_routes, int nb_cars, boolean withPartitioning) {
        if (nb_routes < 2 || nb_cars < 1) {
            throw new RuntimeException("less than 2 routes or 1 car is not an interesting example");
        }
        PetriGame net = createGeneralNet(nb_routes, nb_cars, withPartitioning);
        addAReachabilityWinCondition(net, nb_routes, nb_cars, withPartitioning);
        try {            
            TransitCalculator.automaticallyCreateTransitsForTransitlessTransitions(net);
        } catch (CouldNotCalculateException ex) {
            ex.printStackTrace();
        }
        return net;
    }

    /**
     * should have strategy for nb_routes >=2 and nb_cars >=1
     *
     * @param nb_routes
     * @param nb_cars
     * @param withPartitioning
     * @return
     */
    public static PetriGame createAReachabilityVersionWithRerouting(int nb_routes, int nb_cars, boolean withPartitioning) {
        if (nb_routes < 2 || nb_cars < 1) {
            throw new RuntimeException("less than 2 routes or 1 car is not an interesting example");
        }
        PetriGame net = createGeneralNet(nb_routes, nb_cars, withPartitioning);
        addAReachabilityWinCondition(net, nb_routes, nb_cars, withPartitioning);
        // delete the communication possiblity for the first car with the environment
        for (int i = 0; i < nb_routes; i++) {
            net.removeTransition("tenv" + i);
        }
        // add transition to try again to take a good route
        for (int i = 0; i < nb_cars; i++) {
            Transition t = net.createTransition();
            net.createFlow(t, net.getPlace("car" + i));
            net.createFlow(net.getPlace("car" + i + "E"), t);
        }
        try {
            TransitCalculator.automaticallyCreateTransitsForTransitlessTransitions(net);
        } catch (CouldNotCalculateException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return net;
    }

    private static PetriGame createGeneralNet(int nb_routes, int nb_cars, boolean withPartitioning) {
        PetriGame net = PGTools.createPetriGame("Car routing with " + nb_routes + " intruding " + nb_cars + " points");
        // Environment
        Place env = net.createEnvPlace("env");
        env.setInitialToken(1);
        // Sys
        Place sys = net.createPlace("car0");
        sys.setInitialToken(1);
        Place sysEnd = net.createPlace("car0E");
        for (int i = 0; i < nb_routes; i++) {
            Place envi = net.createEnvPlace("e" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, envi);
            // car 0 
            t = net.createTransition("tenv" + i);
            net.createFlow(sys, t);
            net.createFlow(t, sys);
            net.createFlow(envi, t);
            net.createFlow(t, envi);
            Place c0ri = net.createPlace("c0r" + i);
            t = net.createTransition("tc0r" + i);
            net.createFlow(sys, t);
            net.createFlow(t, c0ri);
            t = net.createTransition();
            net.createFlow(c0ri, t);
            net.createFlow(t, sysEnd);
        }

        for (int i = 1; i < nb_cars; i++) {
            Place sysi = net.createPlace("car" + i);
            sysi.setInitialToken(1);
            Transition t = net.createTransition();
            net.createFlow(sysi, t);
            net.createFlow(t, sysi);
            net.createFlow(sysEnd, t);
            net.createFlow(t, sysEnd);
            net.createTransit(sysi, t, sysi);
            net.createTransit(sysEnd, t, sysEnd);
//            net.setTokenFlow(t,  sysi.getId()+ "->{" + sysi.getId() + "},"
//                     + sysEnd.getId() + "->{" + sysEnd.getId() + "}");
            sysEnd = net.createPlace("car" + i + "E");
            for (int j = 0; j < nb_routes; j++) {
                Place cirj = net.createPlace("c" + i + "r" + j);
                t = net.createTransition("tc" + i + "r" + j);
                net.createFlow(sysi, t);
                net.createFlow(t, cirj);
                t = net.createTransition();
                net.createFlow(cirj, t);
                net.createFlow(t, sysEnd);
            }
        }
        return net;
    }

    private static void addEReachabilityWinCondition(PetriGame net, int nb_routes, int nb_cars, boolean withPartitioning) {
        PNWTTools.setConditionAnnotation(net, Condition.Objective.E_REACHABILITY);
        for (int i = 0; i < nb_cars; i++) {
            for (int j = 0; j < nb_routes; j++) {
                Place buf = net.createPlace("c" + i + "r" + j + "buf");
                Transition t = net.getTransition("tc" + i + "r" + j);
                net.createFlow(t, buf);
            }
        }
        Place reach = net.createPlace("reach");
        net.setReach(reach);
        for (int i = 0; i < nb_routes; i++) {
            Place env = net.getPlace("e" + i);
            Transition t = net.createTransition();
            net.createFlow(env, t);
            net.createFlow(t, env);
            net.createFlow(t, reach);
            for (int j = 0; j < nb_cars; j++) {
                Place buf = net.getPlace("c" + j + "r" + i + "buf");
                net.createFlow(buf, t);
            }
        }
    }

    private static void addAReachabilityWinCondition(PetriGame net, int nb_routes, int nb_cars, boolean withPartitioning) {
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_REACHABILITY);
        net.setReach(net.getPlace("env"));
        for (int i = 0; i < nb_cars; i++) {
            Place reach = net.createPlace("reach" + i);
            net.setReach(reach);
            Transition t = net.createTransition();
            net.createFlow(reach, t);
            net.createFlow(t, net.getPlace("car" + i + "E"));
            for (int j = 0; j < nb_routes; j++) {
                t = net.createTransition();
                net.createFlow(t, reach);
                net.createFlow(net.getPlace("c" + i + "r" + j), t);
                Place env = net.getPlace("e" + j);
                net.createFlow(t, env);
                net.createFlow(env, t);
            }
        }
    }
}
