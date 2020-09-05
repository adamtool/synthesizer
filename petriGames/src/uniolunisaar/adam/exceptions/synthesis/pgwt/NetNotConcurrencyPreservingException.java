package uniolunisaar.adam.exceptions.synthesis.pgwt;

import uniol.apt.adt.pn.Transition;

/**
 *
 * Exception for stating that the net under consideration is not concurrency
 * preserving. I.e. not every transition preserves the number of token while
 * firing.
 *
 * Saves on of the not concurrency-preserving transitions of the net.
 *
 * @author Manuel Gieseking
 */
public class NetNotConcurrencyPreservingException extends Exception {

    private static final long serialVersionUID = 1L;
    private final Transition witness;

    /**
     * Constructor with the message stating that the net is not
     * concurrency-preserving and the id of a responsible transition.
     *
     * @param witness - a transition which makes the net not
     * concurrency-preserving.
     */
    public NetNotConcurrencyPreservingException(Transition witness) {
        super("Net is not concurrency-preserving.\n"
                + "Transition: " + witness.getId());
        this.witness = witness;
    }

    /**
     * Returns a transition of the net which is not concurrency-preserving.
     *
     * @return - a responsible transition for the net not to be
     * concurrency-preserving.
     */
    public Transition getWitness() {
        return witness;
    }
}
