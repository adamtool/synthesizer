package uniolunisaar.adam.exceptions.pg;

import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;

/**
 *
 * @author Manuel Gieseking
 */
public class NotCoveredBySInvariants extends NoSuitableDistributionFoundException {

    private final String msg;
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param tokenCount
     * @param invariants
     * @param cov
     */
    public NotCoveredBySInvariants(long tokenCount, List<Set<Place>> invariants, boolean cov) {
        if (cov) {
            msg = "We are sorry, but the net is not covered by place invariants! Thus, we wasn't able"
                    + " to find a suitable disjunct distribution of the system places.\n"
                    + " Please annotate (token=<number> , number starting in 1. A suitable "
                    + "maximum number could be " + (tokenCount - 1) + ".) the system places by yourself."
                    + " Maybe those place invariants can help you:\n"
                    + invariants.toString();
        } else {
            msg = "We are sorry, but the net is not covered by place invariants which have a equation equal to one! Thus, we wasn't able"
                    + " to find a suitable disjunct distribution of the system places.\n"
                    + " Please annotate (token=<number> , number starting in 1. A suitable "
                    + "maximum number could be " + (tokenCount - 1) + ".) the system places by yourself."
                    + " Maybe those place invariants can help you:\n"
                    + invariants.toString();
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public String getMessage() {
        return msg;
    }
}
