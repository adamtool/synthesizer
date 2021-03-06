package uniolunisaar.adam.ds.synthesis.solver.symbolic.mtbdd;

import java.util.Iterator;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class MTBDDSolvingObject<W extends Condition<W>> extends SolvingObject<PetriGameWithTransits, W, MTBDDSolvingObject<W>> {

    public MTBDDSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        this(game, winCon, false);
    }

    public MTBDDSolvingObject(PetriGameWithTransits game, W winCon, boolean skipChecks) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(game, winCon);

        if (!skipChecks) {
            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(game);
            // only one env token is allowed (todo: do it less expensive ?)
            for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
                CoverabilityGraphNode next = iterator.next();
                Marking m = next.getMarking();
                boolean first = false;
                for (Place place : game.getPlaces()) {
                    if (m.getToken(place).getValue() > 0 && getGame().isEnvironment(place)) {
                        if (first) {
                            throw new NotSupportedGameException("There are two environment token in marking " + m.toString() + ". The BDD approach only allows one external source of information.");
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

    public MTBDDSolvingObject(MTBDDSolvingObject<W> obj) {
        super(new PetriGameWithTransits(obj.getGame()), obj.getWinCon().getCopy());
    }

    @Override
    public MTBDDSolvingObject<W> getCopy() {
        return new MTBDDSolvingObject<>(this);
    }
}
