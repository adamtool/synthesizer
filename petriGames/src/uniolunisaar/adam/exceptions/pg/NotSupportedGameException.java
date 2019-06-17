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

    public NotSupportedGameException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportedGameException(Throwable cause) {
        super(cause);
    }

    public NotSupportedGameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
