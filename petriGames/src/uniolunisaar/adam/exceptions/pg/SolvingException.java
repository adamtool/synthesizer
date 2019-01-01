package uniolunisaar.adam.exceptions.pg;

/**
 *
 * @author Manuel Gieseking
 */
public class SolvingException extends Exception {

    private static final long serialVersionUID = 1L;

    public SolvingException() {
    }

    public SolvingException(String message) {
        super(message);
    }

    public SolvingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolvingException(Throwable cause) {
        super(cause);
    }

    public SolvingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
