package uniolunisaar.adam.symbolic.bddapproach.solver;

import uniolunisaar.adam.ds.solver.SolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDSolverOptions extends SolverOptions {

    //"buddy", "cudd", "cal", "j", "java", "jdd", "test", "typed",
    private String LIBRARY_NAME = "buddy";
    private int MAX_INCREASE = 100000000;
    private int INIT_NODE_NB = 1000000;
    private int CACHE_SIZE = 1000000;

    public BDDSolverOptions() {
        super("buddy");
    }

    public BDDSolverOptions(String name, String LIBRARY_NAME, int MAX_INCREASE, int INIT_NODE_NB, int CACHE_SIZE) {
        super(name);
        this.LIBRARY_NAME = LIBRARY_NAME;
        this.MAX_INCREASE = MAX_INCREASE;
        this.INIT_NODE_NB = INIT_NODE_NB;
        this.CACHE_SIZE = CACHE_SIZE;
    }

    public String getLIBRARY_NAME() {
        return LIBRARY_NAME;
    }

    public int getMAX_INCREASE() {
        return MAX_INCREASE;
    }

    public int getINIT_NODE_NB() {
        return INIT_NODE_NB;
    }

    public int getCACHE_SIZE() {
        return CACHE_SIZE;
    }
}
