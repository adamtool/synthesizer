package uniolunisaar.adam.exceptions.pg;

/**
 * Exception for stating that the net under consideration is not safe. I.e.
 * there is a place of the net and a marking in the coverability graph, such
 * that the place is occupied by more than one token in the marking.
 *
 * Saves one of the not 1-bounded places and a sequence of transitions how to
 * reach a marking where this place carries more than one token.
 *
 * @author Manuel Gieseking
 */
public class NetNotSafeException extends NotSupportedGameException {

    private static final long serialVersionUID = 1L;
    private final String unsafePlace;
    private final String sequence;

    /**
     * Constructor with the message stating that the net is not safe, a liable
     * place and a sequence of transitions how achieve a unsafe situation of
     * this place.
     *
     * @param unsafePlace - a not 1-bounded place in the net.
     * @param sequence - a sequence how to get more than one token on the place.
     */
    public NetNotSafeException(String unsafePlace, String sequence) {
        super("Net is not safe.\n"
                + "Unsafe place: " + unsafePlace + "\n"
                + "Sequence: " + sequence);
        this.unsafePlace = unsafePlace;
        this.sequence = sequence;
    }

    /**
     * Returns a not 1-bounded place.
     *
     * @return - a not 1-bounded place.
     */
    public String getUnsafePlace() {
        return unsafePlace;
    }

    /**
     * Returns a sequence of transitions how to reach a marking where the saved
     * not 1-bounded places carries more than one token.
     *
     * @return - a sequence how to reach a marking where the saved not 1-bounded
     * places carries more than one token.
     */
    public String getSequence() {
        return sequence;
    }
}
