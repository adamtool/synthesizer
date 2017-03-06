package uniolunisaar.adam.symbolic.bddapproach.solver;

import uniolunisaar.adam.ds.solver.SolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverOptions extends SolverOptions {

    //"buddy", "cudd", "cal", "j", "java", "jdd", "test", "typed",
    private String libraryName = "buddy";
    private int maxIncrease = 100000000;
    private int initNodeNb = 1000000;
    private int cacheSize = 1000000;

    public BDDSolverOptions() {
        super("buddy");
    }

    public BDDSolverOptions(String name, String libraryName, int maxIncrease, int initNodeNb, int cacheSize) {
        super(name);
        this.libraryName = libraryName;
        this.maxIncrease = maxIncrease;
        this.initNodeNb = initNodeNb;
        this.cacheSize = cacheSize;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public void setMaxIncrease(int maxIncrease) {
        this.maxIncrease = maxIncrease;
    }

    public void setInitNodeNb(int initNodeNb) {
        this.initNodeNb = initNodeNb;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public int getMaxIncrease() {
        return maxIncrease;
    }

    public int getInitNodeNb() {
        return initNodeNb;
    }

    public int getCacheSize() {
        return cacheSize;
    }
}
