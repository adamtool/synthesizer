package uniolunisaar.adam.exceptions.synthesis.pgwt;

import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;

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
