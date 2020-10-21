package uniolunisaar.adam.util;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.util.interrupt.UncheckedInterruptedException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.MoreThanOneEnvironmentPlayerException;
import uniolunisaar.adam.logic.synthesis.pgwt.partitioning.Partitioner;
import uniolunisaar.adam.tools.Logger;

/**
 * This class is used to check and store all preconditions of a Petri game with
 * transits to be used in the distributed system approach.
 *
 * It checks: - whether the net is 1-bounded - it is correctly partitioned
 *
 * @author Manuel Gieseking
 */
public class PgwtPreconditionChecker extends PreconditionChecker {

    private BoundedResult bounded = null;
    private Boolean partitioned = null;
    private InvalidPartitionException ipe = null;
    private MoreThanOneEnvironmentPlayerException mtoepe = null;

    public PgwtPreconditionChecker(PetriGameWithTransits pgwt) {
        super(pgwt);
    }

    @Override
    public boolean check() throws NetNotSafeException, MoreThanOneEnvironmentPlayerException, InvalidPartitionException, CalculationInterruptedException {
        if (!isSafe()) {
            throw new NetNotSafeException(bounded.unboundedPlace.getId(), bounded.sequence.toString());
        }
        Boolean part = isPartitioned();
        if (part == null) {
            if (ipe != null) {
                throw ipe;
            } else if (mtoepe != null) {
                throw mtoepe;
            }
        }
        return part;
    }

    public boolean isSafe() throws CalculationInterruptedException {
        if (bounded == null) {
            try {
                bounded = Bounded.checkBounded(getNet());
            } catch (UncheckedInterruptedException ex) {
                CalculationInterruptedException e = new CalculationInterruptedException(ex.getMessage());
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
        }
        return bounded.isSafe();
    }

    public Boolean isPartitioned() throws CalculationInterruptedException {
        if (partitioned == null && mtoepe == null && ipe == null) {
            try {
                partitioned = Partitioner.checkPartitioning(getGame(), true);
            } catch (InvalidPartitionException e) {
                ipe = e;
            } catch (MoreThanOneEnvironmentPlayerException e) {
                mtoepe = e;
            }
        }
        return partitioned;
    }

    @Override
    public boolean changeOccurred(IGraph<PetriNet, Flow, Node> graph) {
        super.changeOccurred(graph);
        bounded = null;
        partitioned = null;
        ipe = null;
        mtoepe = null;
        return true;
    }

    public PetriGameWithTransits getGame() {
        return (PetriGameWithTransits) getNet();
    }

}
