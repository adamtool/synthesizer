package uniolunisaar.adam.exceptions.pg;

/**
 *
 * Super class for all problems concerning not the right class of games for
 * solving.
 *
 * @author Manuel Gieseking
 */
public class NotSupportedGameException extends SolvingException {

    private static final long serialVersionUID = 1L;

    public NotSupportedGameException(String message) {
        super(message);
    }

}
