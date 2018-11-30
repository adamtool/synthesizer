package uniolunisaar.adam.symbolic.mtbdd.solver;

import uniolunisaar.adam.ds.solver.SolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class MTBDDSolverOptions extends SolverOptions {

    private boolean gg = false;
    private boolean ggs = false;
    private boolean pgs = true;

    public MTBDDSolverOptions() {
        super("mtbdd");
    }

    public MTBDDSolverOptions(String name, String libraryName, int maxIncrease, int initNodeNb, int cacheSize) {
        super(name);       
    }

    public MTBDDSolverOptions(boolean gg, boolean ggs, boolean pgs) {
        super("mtbdd");
        this.gg = gg;
        this.ggs = ggs;
        this.pgs = pgs;
    }

    public boolean isGg() {
        return gg;
    }

    public void setGg(boolean gg) {
        this.gg = gg;
    }

    public boolean isGgs() {
        return ggs;
    }

    public void setGgs(boolean ggs) {
        this.ggs = ggs;
    }

    public boolean isPgs() {
        return pgs;
    }

    public void setPgs(boolean pgs) {
        this.pgs = pgs;
    }
}
