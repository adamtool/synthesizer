package uniolunisaar.adam.symbolic.bddapproach.util.benchmark;

import uniol.apt.adt.pn.PetriNet;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.util.benchmark.Benchmarks;
import uniolunisaar.adam.util.benchmark.Benchmarks.Parts;

/**
 * Just toString and toCSVString overriden to add the nb of variables to the
 * statistics. All other methods are delegates.
 *
 * @author Manuel Gieseking
 */
public class BDDBenchmarks {

    private static BDDBenchmarks instance = null;

    private BDDBenchmarks() {
    }

    public static BDDBenchmarks getInstance() {
        if (instance == null) {
            instance = new BDDBenchmarks();
        }
        return instance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (Benchmarks.getInstance().getSolver() != null) {
            sb.append("Nb variables: ").append(((BDDSolver) Benchmarks.getInstance().getSolver()).getVariableNumber()).append("\n");
        }
        return sb.toString();
    }

    public String toCSVString() {
        StringBuilder sb = new StringBuilder(Benchmarks.getInstance().toCSVString());
        BDDSolver solver = (BDDSolver) Benchmarks.getInstance().getSolver();
        if (solver != null) {
            sb.append(", ").append(solver.getVariableNumber());
        }
        return sb.toString();
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% DELEGATES
    public void reset() {
        Benchmarks.getInstance().reset();
    }

    public void start(Parts part) {
        Benchmarks.getInstance().start(part);
    }

    public void stop(Parts part) {
        Benchmarks.getInstance().stop(part);
    }

    public void startTime(Parts part) {
        Benchmarks.getInstance().startTime(part);
    }

    public void stopTime(Parts part) {
        Benchmarks.getInstance().stopTime(part);
    }

    public void startMemory(Parts part) {
        Benchmarks.getInstance().startMemory(part);
    }

    public void stopMemory(Parts part) {
        Benchmarks.getInstance().stopMemory(part);
    }

    public void addData(Solver solver, PetriNet strat) {
        Benchmarks.getInstance().addData(solver, strat);
    }

    public Solver getSolver() {
        return Benchmarks.getInstance().getSolver();
    }

}
