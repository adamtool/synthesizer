package uniolunisaar.adam.exceptions.pg;

import uniol.apt.adt.pn.PetriNet;

/**
 *
 * @author Manuel Gieseking
 */
public class NoCalculatorProvidedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoCalculatorProvidedException(PetriNet net, String method) {
        super("You did not provided a calculator for calculating " + method + "of the Petri game: " + net.getName() + ".");
    }

    public NoCalculatorProvidedException(PetriNet net, String method, Exception e) {
        super("You did not provided a calculator for calculating " + method + "of the Petri game: " + net.getName() + ".", e);
    }
}
