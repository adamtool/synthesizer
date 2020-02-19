package uniolunisaar.adam.ds.solver;

/**
 *
 * @author Manuel
 */
public class SolverOptions {

    private final String name;
    private final boolean skipTests;

    public SolverOptions(String name, boolean skipTests) {
        this.name = name;
        this.skipTests = skipTests;
    }

    public SolverOptions(String name) {
        this.name = name;
        this.skipTests = true;
    }

    public String getName() {
        return name;
    }

    public boolean isSkipTests() {
        return skipTests;
    }

}
