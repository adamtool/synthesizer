package uniolunisaar.adam.exceptions.pg;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;

/**
 * Exception for stating that the net under consideration is unbounded and
 * therefore could not be investigated by ADAM. I.e. there exists a place which
 * could be occupied by an unbounded number of token during the game.
 *
 * Extends the UnboundedException of APT by also given a witness of the
 * unboundedness.
 *
 * @author Manuel Gieseking
 */
public class UnboundedPGException extends NotSupportedGameException {

    public static final long serialVersionUID = 0xdeadbeef00000018l;
    private final Place unboundedPlace;

    /**
     * Delegation to the corresponding constructor of the 'UnboundedException'.
     * The unbounded place is set to 'null'.
     *
     * @param pn - the Petri net under consideration.
     */
    public UnboundedPGException(PetriNet pn) {
        super("Petri net " + pn.getName() + " is unbounded, only bounded Petri Nets are supported.");
        unboundedPlace = null;
    }

    /**
     * Delegation to the constructor of the 'UnboundedException' for a Petri net
     * parameter. Additionally is the witness set to the given unbounded place.
     *
     * @param pn - the Petri net under consideration.
     * @param unboundedPlace - an unbounded place of the net.
     */
    public UnboundedPGException(PetriNet pn, Place unboundedPlace) {
        super("Petri net " + pn.getName() + " is unbounded, only bounded Petri Nets are supported.");
        this.unboundedPlace = unboundedPlace;
    }

    /**
     * Returns an unbounded place of the net.
     *
     * @return - a place of the net which can be occupied by an unbounded number
     * of tokens during the game.
     */
    public Place getUnboundedPlace() {
        return unboundedPlace;
    }

    /**
     * Overrides the 'getMessage' function of the 'UnboundedException' by adding
     * the witness to the message.
     *
     * @return - a message stating the unboundedness of the net and a witness.
     */
    @Override
    public String getMessage() {
        return super.getMessage() + " Place-id: " + unboundedPlace.getId();
    }
}
