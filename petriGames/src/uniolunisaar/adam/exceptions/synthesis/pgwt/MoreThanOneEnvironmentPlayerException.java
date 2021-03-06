package uniolunisaar.adam.exceptions.synthesis.pgwt;

import uniol.apt.adt.pn.Marking;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class MoreThanOneEnvironmentPlayerException extends SolvingException {

    private static final long serialVersionUID = 1L;

    public MoreThanOneEnvironmentPlayerException(String message) {
        super(message);
    }

    public MoreThanOneEnvironmentPlayerException(PetriGameWithTransits game, Marking m) {
        super("There are more than one environment player in the game. Witness marking: " + m.toString() + "."
                + "  The 'distributed system' approach  only allows one external source of information.");
    }
}
