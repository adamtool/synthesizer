package uniolunisaar.adam.generators.pgwt;

import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;


/**
 *
 * @author Manuel Gieseking
 */
public class AdamBehavior {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGameWithTransits genConcurrentMaschines(int nb_machines, int nb_workpieces) {
        PetriGameWithTransits game = Workflow.generateNewAnnotationPoster(nb_machines, nb_workpieces, true, false);
        return game;
    }

    public static PetriGameWithTransits genContainerTerminal(int nb_systems) {
        PetriGameWithTransits game = ContainerTerminal.createSafetyVersion(nb_systems, true);
        return game;
    }

    public static PetriGameWithTransits genDocumentWorkflow(int nb_clerks, boolean allyes) {
        PetriGameWithTransits game = allyes ? Clerks.generateCP(nb_clerks, true, false)
                : Clerks.generateNonCP(nb_clerks, true, false);
        return game;
    }

    public static PetriGameWithTransits genEmergencyBreakdown(int nb_crit, int nb_norm) {
        PetriGameWithTransits game = EmergencyBreakdown.createSafetyVersion(nb_crit, nb_norm, true);
        return game;
    }

    public static PetriGameWithTransits genJobProcessing(int nb_machines) {
        PetriGameWithTransits game = ManufactorySystem.generate(nb_machines, true, false);
        return game;
    }

    public static PetriGameWithTransits genSecuritySystem(int nb_systems) {
        PetriGameWithTransits game = SecuritySystem.createSafetyVersion(nb_systems, true);
        return game;
    }

    public static PetriGameWithTransits genSelfReconfiguringRobots(int nb_robots, int nb_destroy) {
        PetriGameWithTransits game = SelfOrganizingRobots.generate(nb_robots, nb_destroy, true, false);
        return game;
    }

    public static PetriGameWithTransits genWatchdog(int nb_machines, boolean search, boolean partial_observation) {
        PetriGameWithTransits game = Watchdog.generate(nb_machines, search, partial_observation, true);
        return game;
    }
}
