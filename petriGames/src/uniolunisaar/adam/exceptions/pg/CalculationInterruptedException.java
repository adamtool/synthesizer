package uniolunisaar.adam.exceptions.pg;

/**
 *
 * @author Manuel Gieseking
 */
public class CalculationInterruptedException extends Exception {

    private static final long serialVersionUID = 1L;

    public CalculationInterruptedException() {
        super("Calculation has been interrupted!");
    }

    public CalculationInterruptedException(String message) {
        super(message);
    }

    public CalculationInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculationInterruptedException(Throwable cause) {
        super(cause);
    }

    public CalculationInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
