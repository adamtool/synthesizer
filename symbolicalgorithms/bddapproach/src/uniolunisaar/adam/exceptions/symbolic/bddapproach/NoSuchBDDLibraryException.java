package uniolunisaar.adam.exceptions.symbolic.bddapproach;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuchBDDLibraryException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String libName;

    public NoSuchBDDLibraryException(String libName) {
        super("The library '" + libName + "' is not supported.");
        this.libName = libName;
    }
}
