package uniolunisaar.adam.exceptions.synthesis.pgwt;

import uniol.apt.adt.pn.PetriNet;

/**
 *
 * @author Manuel Gieseking
 */
public class CouldNotFindSuitableConditionException extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotFindSuitableConditionException(PetriNet net) {
        super("Could not find a suitable winning condition for the Petri game: " + net.getName() + ". Are you sure you have equipped the net with the needed conditions?");
    }

    public CouldNotFindSuitableConditionException(PetriNet net, Exception e) {
        super("Could not find a suitable winning condition for the Petri game: " + net.getName() + ". Are you sure you have equipped the net with the needed conditions?", e);
    }
}
