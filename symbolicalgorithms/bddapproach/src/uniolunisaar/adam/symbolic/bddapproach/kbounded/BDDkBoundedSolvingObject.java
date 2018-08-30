package uniolunisaar.adam.symbolic.bddapproach.kbounded;

import java.util.Iterator;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.calculators.CalculatorIDs;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class BDDkBoundedSolvingObject<W extends WinningCondition> extends SolvingObject<PetriGame, W> {

    public BDDkBoundedSolvingObject(PetriGame game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        this(game, winCon, false);
    }

    public BDDkBoundedSolvingObject(PetriGame game, W winCon, boolean skipChecks) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(game, winCon);

        if (!skipChecks) {
            CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(game);
//            NotSolvableWitness witness = AdamTools.isSolvablePetriGame(pn, cover);
//            if (witness != null) {
////                throw new NotSupportedGameException("Petri game not solvable: " + witness.toString());
//            }
            // only one env token is allowed (todo: do it less expensive ?)
            for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
                CoverabilityGraphNode next = iterator.next();
                Marking m = next.getMarking();
                boolean first = false;
                for (Place place : game.getPlaces()) {
                    if (m.getToken(place).getValue() > 0 && game.isEnvironment(place)) {
                        if (first) {
                            throw new NotSupportedGameException("There are two enviroment token in marking " + m.toString() + ". The BDD approach only allows one external source of information.");
                        }
                        first = true;
                    }
                }
            }

        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net is safe or"
                    + " belongs to the class of solvable Petri games!", false);
        }

    }

    public BDDkBoundedSolvingObject(BDDkBoundedSolvingObject<W> obj) {
        super(new PetriGame(obj.getGame()), obj.getWinCon().getCopy());
    }

    /**
     * Problem if it's really a long
     *
     * @return
     */
    public int getMaxTokenCountInt() {
        return (int) getMaxTokenCount();
    }

    // Delegate methods
    public boolean isConcurrencyPreserving() {
        return getGame().getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
    }

    public long getMaxTokenCount() {
        return getGame().getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
    }

    @Override
    public BDDkBoundedSolvingObject<W> getCopy() {
        return new BDDkBoundedSolvingObject(this);
    }

}
