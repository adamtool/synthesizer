package uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach;

import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoCalculatorProvidedException;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.tools.Logger;

/**
 * This class serves for storing the input of the solving algorithm, i.e., the
 * model (the Petri game) and the specification (the winning condition).
 *
 * Inheriting classes need to split the system places and the environment places
 * up into disjunct partitions to have a more efficient encoding and know
 * exactly the indices of the place ids.
 *
 * @author Manuel Gieseking
 * @param <W> - the winning condition.
 */
public abstract class BDDSolvingObject<W extends Condition<W>> extends SolvingObject<PetriGameWithTransits, W, BDDSolvingObject<W>> {

    // saves places divided into groups for each token
    private Set<Place>[] places;

    protected abstract void checkPrecondition(PetriGameWithTransits game) throws NetNotSafeException, NotSupportedGameException;

    protected abstract void annotatePlacesWithPartitions(boolean skipChecks) throws InvalidPartitionException, NoSuitableDistributionFoundException;

    public BDDSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        this(game, winCon, false);
    }

    public BDDSolvingObject(PetriGameWithTransits game, W winCon, boolean skipChecks) throws NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException, NetNotSafeException {
        super(game, winCon);
        if (!skipChecks) {
            System.out.println("testing");
            checkPrecondition(game);
        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net is safe or"
                    + " belongs to the class of solvable Petri games!", false);
        }

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        bufferData(skipChecks);
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    public BDDSolvingObject(BDDSolvingObject<W> obj) {
        super(new PetriGameWithTransits(obj.getGame()), obj.getWinCon().getCopy());
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        try {
            bufferData(true);
        } catch (NoSuitableDistributionFoundException ex) {
            // should not be possible since it should be a copy of a distributable game
            Logger.getInstance().addError("The original game which you gave me to copy wasn't correct!", ex);
        } catch (InvalidPartitionException ex) {
            // cannot happen (just thrown by skipChecks == false
        }
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    // todo: proof that it's a suitable slicing, such that types of the places are preserved.
    private void bufferData(boolean skipChecks) throws NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            Logger.getInstance().addMessage("Annotate places with partitions ...");            
            annotatePlacesWithPartitions(skipChecks);
            Logger.getInstance().addMessage("... annotation done");        
            //todo:  all comments are old version, before cavarti
            // split places and add an id
//        int add = getEnvPlaces().isEmpty() ? 1 : 0;
            places = (Set<Place>[]) new Set<?>[getMaxTokenCountInt()]; // todo: get rid of the generic array?
            // just do it for all, since user could annotate them badly (skipping ids)
//            if (getGame().getEnvPlaces().isEmpty()) { // add empty set when no env place existend (todo: is it to hacky for no env case?)
//                places[0] = new HashSet<>();
//            }
            for (int i = 0; i < getMaxTokenCount(); i++) {
                places[i] = new HashSet<>();
            }
            boolean cp = getGame().getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
            int additional = cp ? 0 : 1;
            for (Place place : getGame().getPlaces()) {
                int token = getGame().getPartition(place);
                if (places[token] == null) {
                    places[token] = new HashSet<>();
                }
                getGame().setID(place, places[token].size() + additional);
                places[token].add(place);
            }
        } catch (InvalidPartitionException | NoCalculatorProvidedException | NoSuitableDistributionFoundException e) {
            Logger.getInstance().addError("Sorry, most likely you did s.th. while partitioning the places into disjunct subsets.", e);
            throw e;
        }

    }

    public Set<Place>[] getDevidedPlaces() {
        return places;
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

}
