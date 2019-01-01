package uniolunisaar.adam.exceptions.pg;

import uniolunisaar.adam.exceptions.pg.SolvingException;

/**
 *
 * @author Manuel Gieseking
 */
public class ParameterMissingException extends SolvingException {

    private static final long serialVersionUID = 1L;

    public ParameterMissingException(String message) {
        super(message);
    }

}
