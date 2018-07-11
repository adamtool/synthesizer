package uniolunisaar.adam.symbolic.mtbdd.petrigame;

import java.util.Iterator;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.util.AdamExtensions;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class MTBDDPetriGame extends PetriGame {

    public MTBDDPetriGame(PetriNet pn) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        this(pn, false);
    }

    public MTBDDPetriGame(PetriNet pn, boolean skipChecks) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(pn, skipChecks);

        if (!skipChecks) {
            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(pn);
            // only one env token is allowed (todo: do it less expensive ?)
            for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
                CoverabilityGraphNode next = iterator.next();
                Marking m = next.getMarking();
                boolean first = false;
                for (Place place : pn.getPlaces()) {
                    if (m.getToken(place).getValue() > 0 && AdamExtensions.isEnvironment(place)) {
                        if (first) {
                            throw new NotSupportedGameException("There are two enviroment token in marking " + m.toString() + ". The BDD approach only allows one external source of information.");
                        }
                        first = true;
                    }
                }
            }

        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net"
                    + " belongs to the class of solvable Petri games!", false);
        }
    }

}
