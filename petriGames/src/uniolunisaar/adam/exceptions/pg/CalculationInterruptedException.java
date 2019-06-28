package uniolunisaar.adam.exceptions.pg;

/**
 *
 * @author Manuel Gieseking
 */
public class CalculationInterruptedException extends InterruptedException {

    private static final long serialVersionUID = 1L;

    public CalculationInterruptedException() {
        super("Calculation has been interrupted!");
    }

    public CalculationInterruptedException(String message) {
        super(message);
    }

}
