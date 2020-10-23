package uniolunisaar.adam.exceptions.synthesis.pgwt;

import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Place;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuitableDistributionFoundException extends SolvingException {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public NoSuitableDistributionFoundException() {
    }

    public NoSuitableDistributionFoundException(String message) {
        super(message);
    }

    /**
     *
     * @param tokenCount
     */
    public NoSuitableDistributionFoundException(long tokenCount) {
        super("Oh, that's embarrassing, but we couldn't find a suitable disjunct distribution of the places of the net!"
                + " Please annotate (token=<number> , number starting in 1. A suitable "
                + "maximum number could be " + (tokenCount - 1) + ".) the system places by yourself.");
    }

    /**
     *
     * @param tokenCount
     * @param invariants
     */
    public NoSuitableDistributionFoundException(long tokenCount, List<Set<Place>> invariants) {
        super("Oh, that's embarrassing, but we couldn't find a suitable disjunct distribution of the places of the net!"
                + " Please annotate (token=<number> , number starting in 1. A suitable "
                + "maximum number could be " + (tokenCount - 1) + ".) the system places by yourself."
                + " Maybe those place invariants can help you:\n"
                + invariants.toString());
    }

    /**
     *
     * @param place
     */
    public NoSuitableDistributionFoundException(Place place) {
        super("The places are not properly annotated with partition ids. Place " + place.getId() + " has no token related to.");
    }

    /**
     *
     * @param maxTokenCount
     * @param tokencount
     */
    public NoSuitableDistributionFoundException(long maxTokenCount, long tokencount) {
        super("The places are not properly annotated with partition ids. The maximum token id, which should be assigned is: " + (tokencount - 1)
                + ". You assigned: " + maxTokenCount);
    }
}
