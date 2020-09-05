package uniolunisaar.adam.exceptions.synthesis.pgwt;

import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;

/**
 *
 * @author Manuel Gieseking
 */
public class CouldNotCalculateException extends SolvingException {

    private static final long serialVersionUID = 1L;

    public CouldNotCalculateException() {
    }

    public CouldNotCalculateException(String message) {
        super(message);
    }

    public CouldNotCalculateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouldNotCalculateException(Throwable cause) {
        super(cause);
    }

    public CouldNotCalculateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
