package uniolunisaar.adam.exceptions.pg;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class InvalidPartitionException extends SolvingException {

    private static final long serialVersionUID = 1L;

    public InvalidPartitionException(String message) {
        super(message);
    }

    public InvalidPartitionException(PetriGame game, Marking m, Place p) {
        super("The partition is not disjunct. In marking: " + m.toString() + " the partition " + game.getPartition(p) + " of place " + p.getId() + " is used at least twice.");
    }
}
