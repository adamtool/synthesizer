package uniolunisaar.adam.generators.pg;

import uniolunisaar.adam.ds.petrigame.PetriGame;


/**
 *
 * @author Manuel Gieseking
 */
public class AdamBehavior {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame genConcurrentMaschines(int nb_machines, int nb_workpieces) {
        PetriGame game = Workflow.generateNewAnnotationPoster(nb_machines, nb_workpieces, true, false);
        return game;
    }

    public static PetriGame genContainerTerminal(int nb_systems) {
        PetriGame game = ContainerTerminal.createSafetyVersion(nb_systems, true);
        return game;
    }

    public static PetriGame genDocumentWorkflow(int nb_clerks, boolean allyes) {
        PetriGame game = allyes ? Clerks.generateCP(nb_clerks, true, false)
                : Clerks.generateNonCP(nb_clerks, true, false);
        return game;
    }

    public static PetriGame genEmergencyBreakdown(int nb_crit, int nb_norm) {
        PetriGame game = EmergencyBreakdown.createSafetyVersion(nb_crit, nb_norm, true);
        return game;
    }

    public static PetriGame genJobProcessing(int nb_machines) {
        PetriGame game = ManufactorySystem.generate(nb_machines, true, true, false);
        return game;
    }

    public static PetriGame genSecuritySystem(int nb_systems) {
        PetriGame game = SecuritySystem.createSafetyVersion(nb_systems, true);
        return game;
    }

    public static PetriGame genSelfReconfiguringRobots(int nb_robots, int nb_destroy) {
        PetriGame game = SelfOrganizingRobots.generate(nb_robots, nb_destroy, true, false);
        return game;
    }

    public static PetriGame genWatchdog(int nb_machines, boolean search, boolean partial_observation) {
        PetriGame game = Watchdog.generate(nb_machines, search, partial_observation, true);
        return game;
    }
}
