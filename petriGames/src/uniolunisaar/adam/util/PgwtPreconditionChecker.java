package uniolunisaar.adam.util;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.util.interrupt.UncheckedInterruptedException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameExtensionHandler;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.MoreThanOneEnvironmentPlayerException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.MoreThanOneSystemPlayerException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.Partitioner;
import uniolunisaar.adam.tools.Logger;

/**
 * This class is used to check and store all preconditions of a Petri game with
 * transits to be used in the distributed system or distributed environment
 * approach.
 *
 * It checks: - whether the net is 1-bounded - it is correctly partitioned -
 * there is only one environment player or only one system players
 *
 * The results are saved in the extensions of the game.
 *
 * @author Manuel Gieseking
 */
public class PgwtPreconditionChecker extends PreconditionChecker {

    public PgwtPreconditionChecker(PetriGameWithTransits pgwt) {
        super(pgwt);
    }

    @Override
    public boolean check() throws NetNotSafeException, InvalidPartitionException, CalculationInterruptedException, NoSuitableDistributionFoundException, SolvingException {
        if (!isSafe()) {
            BoundedResult bounded = PetriGameExtensionHandler.getBoundedResult(getGame());
            throw new NetNotSafeException(bounded.unboundedPlace.getId(), bounded.sequence.toString());
        }
        if (!playerNumbers()) {
            throw new SolvingException(
                    PetriGameExtensionHandler.getOneEnvPlayer(getGame()).getMessage() + " and "
                    + PetriGameExtensionHandler.getOneSysPlayer(getGame()).getMessage()
            );
        }
        if (!isPartitioned()) {
            throw PetriGameExtensionHandler.getInValidPartitioned(getGame());
        }

        return true;
    }

    public boolean isSafe() throws CalculationInterruptedException {
        BoundedResult bounded;
        if (!PetriGameExtensionHandler.hasBoundedResult(getGame())) {
            try {
                bounded = Bounded.checkBounded(getGame());
                PetriGameExtensionHandler.setBoundedResult(getGame(), bounded);
            } catch (UncheckedInterruptedException ex) {
                CalculationInterruptedException e = new CalculationInterruptedException(ex.getMessage());
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
        } else {
            bounded = PetriGameExtensionHandler.getBoundedResult(getGame());
        }
        return bounded.isSafe();
    }

    // todo: merge this with the PGTools methods and anyhow collect all the reachability checks together
    public boolean playerNumbers() throws CalculationInterruptedException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(getGame()) || PetriGameExtensionHandler.thereIsOneSysPlayer(getGame())) {
            return true;
        } else if (PetriGameExtensionHandler.checkedOneEnvPlayer(getGame()) && PetriGameExtensionHandler.checkedOneSysPlayer(getGame())) {
            return false;
        }
        CoverabilityGraph cg = CoverabilityGraph.getReachabilityGraph(getGame());
        for (CoverabilityGraphNode node : cg.getNodes()) {
            Marking m = node.getMarking(); // todo: the markings are very expensive for this use case. 
            boolean firstEnv = false;
            boolean firstSys = false;
            for (Place place : getGame().getPlaces()) {
                if (Thread.interrupted()) {
                    CalculationInterruptedException e = new CalculationInterruptedException();
                    Logger.getInstance().addError(e.getMessage(), e);
                    throw e;
                }
                if (m.getToken(place).getValue() > 0) {
                    if (getGame().isEnvironment(place)) {
                        if (firstEnv) {
                            PetriGameExtensionHandler.setOneEnvPlayer(getGame(), new MoreThanOneEnvironmentPlayerException(getGame(), m));
                            if (PetriGameExtensionHandler.checkedOneSysPlayer(getGame())) { // there is also more than one system player
                                return false;
                            }
                        } else {
                            firstEnv = true;
                        }
                    } else {
                        if (firstSys) {
                            PetriGameExtensionHandler.setOneSysPlayer(getGame(), new MoreThanOneSystemPlayerException(getGame(), m));
                            if (PetriGameExtensionHandler.checkedOneEnvPlayer(getGame())) { // there is also more than one env player)
                                return false;
                            }
                        } else {
                            firstSys = true;
                        }
                    }
                }
            }
        }
        if (!PetriGameExtensionHandler.checkedOneEnvPlayer(getGame())) {
            PetriGameExtensionHandler.setOneEnvPlayer(getGame());
        }
        if (!PetriGameExtensionHandler.checkedOneSysPlayer(getGame())) {
            PetriGameExtensionHandler.setOneSysPlayer(getGame());
        }
        return true;
    }

    public boolean isPartitioned() throws CalculationInterruptedException, NoSuitableDistributionFoundException, InvalidPartitionException {
        if (!PetriGameExtensionHandler.isValidPartitioned(getGame())
                && !PetriGameExtensionHandler.isInValidPartitioned(getGame())) {
            // first try to automatically annotate it
            Partitioner.doIt(getGame(), PetriGameExtensionHandler.thereIsOneEnvPlayer(getGame()));

            // then check it
            try {
                Partitioner.checkPartitioning(getGame());
                PetriGameExtensionHandler.setValidPartioned(getGame());
                return true;
            } catch (InvalidPartitionException e) {
                PetriGameExtensionHandler.setInValidPartioned(getGame(), e);
                throw e;
            }
        }
        return PetriGameExtensionHandler.isValidPartitioned(getGame());
    }

    @Override
    public boolean changeOccurred(IGraph<PetriNet, Flow, Node> graph) {
        super.changeOccurred(graph);
        PetriGameExtensionHandler.removeBoundedResult(getGame());
        PetriGameExtensionHandler.removeOneEnvPlayer(getGame());
        PetriGameExtensionHandler.removeOneSysPlayer(getGame());
        PetriGameExtensionHandler.removePartitioned(getGame());
        return true;
    }

    public PetriGameWithTransits getGame() {
        return (PetriGameWithTransits) getNet();
    }

}
