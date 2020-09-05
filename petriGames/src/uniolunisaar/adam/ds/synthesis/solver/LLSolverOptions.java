package uniolunisaar.adam.ds.synthesis.solver;

/**
 *
 * @author Manuel Gieseking
 */
public class LLSolverOptions extends SolverOptions {

    private final boolean withAutomaticTransitAnnotation;

    public LLSolverOptions(String name, boolean skipTests, boolean withAutomaticTransitAnnotation) {
        super(name, skipTests);
        this.withAutomaticTransitAnnotation = withAutomaticTransitAnnotation;
    }

    public LLSolverOptions(String name, boolean withAutomaticTransitAnnotation) {
        super(name);
        this.withAutomaticTransitAnnotation = withAutomaticTransitAnnotation;
    }

    public LLSolverOptions(boolean skipTests, String name) {
        super(name, skipTests);
        this.withAutomaticTransitAnnotation = true;
    }

    public LLSolverOptions(String name) {
        super(name);
        this.withAutomaticTransitAnnotation = true;
    }

    public boolean isWithAutomaticTransitAnnotation() {
        return withAutomaticTransitAnnotation;
    }

}
