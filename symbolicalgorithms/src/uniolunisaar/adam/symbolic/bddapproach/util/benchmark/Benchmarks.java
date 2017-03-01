package uniolunisaar.adam.symbolic.bddapproach.util.benchmark;

import java.util.HashMap;
import java.util.Map;
import uniol.apt.adt.pn.PetriNet;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class Benchmarks<W extends WinningCondition> {

    public enum Parts {

        OVERALL,
        BOUNDED,
        CONCURRENCY_PRESERVING,
        MAXIMUM_TOKEN_NB,
        PARTITIONING,
        INVARIANTS,
        ENVIRONMENT_TRANS,
        SYSTEM1_TRANS,
        SYSTEM2_TRANS,
        TYPE2_TRAP,
        FIXPOINT,
        GRAPH_STRAT,
        PG_STRAT,
        DOT_SAVING
    }
    private static Benchmarks instance = null;
    private final Map<Parts, Benchmark> benchs;
    private BDDSolver<W> solver = null;
    private PetriNet strategy = null;

    private Benchmarks() {
        benchs = new HashMap<>();
    }

    public static Benchmarks getInstance() {
        if (instance == null) {
            instance = new Benchmarks();
        }
        return instance;
    }

    public void reset() {
        instance = null;
    }

    public void start(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        Benchmark bench = benchs.get(part);
        bench.setStartTime(System.currentTimeMillis());
        Runtime rt = Runtime.getRuntime();
        bench.setStartMemory(rt.totalMemory() - rt.freeMemory());
    }

    public void stop(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        Benchmark bench = benchs.get(part);
        bench.setStopTime(System.currentTimeMillis());
        Runtime rt = Runtime.getRuntime();
        bench.setStopMemory(rt.totalMemory() - rt.freeMemory());
    }

    public void startTime(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        benchs.get(part).setStartTime(System.currentTimeMillis());
    }

    public void stopTime(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        benchs.get(part).setStopTime(System.currentTimeMillis());
    }

    public void startMemory(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        Runtime rt = Runtime.getRuntime();
        benchs.get(part).setStartMemory(rt.totalMemory() - rt.freeMemory());
    }

    public void stopMemory(Parts part) {
        if (!benchs.containsKey(part)) {
            benchs.put(part, new Benchmark(part.toString()));
        }
        Runtime rt = Runtime.getRuntime();
        benchs.get(part).setStopMemory(rt.totalMemory() - rt.freeMemory());
    }

    public void addData(BDDSolver<W> solver, PetriNet strat) {
        this.solver = solver;
        this.strategy = strat;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Benchmark bench : benchs.values()) {
            sb.append(bench.toString()).append("\n");
        }
        if (solver != null) {
            PetriNet net = solver.getNet();
            sb.append("Nb orig places: ").append(net.getPlaces().size()).append("\n");
            sb.append("Nb orig trans: ").append(net.getTransitions().size()).append("\n");
            sb.append("Nb token: ").append(solver.getGame().getTOKENCOUNT()).append("\n");
            sb.append("Is concurrency-preserving: ").append(solver.getGame().isConcurrencyPreserving()).append("\n");
            sb.append("Nb variables: ").append(solver.getVariableNumber()).append("\n");
        }
        if (strategy != null) {
            sb.append("Nb strategy places: ").append(strategy.getPlaces().size()).append("\n");
            sb.append("Nb strategy transitions: ").append(strategy.getTransitions().size()).append("\n");
        }
        return sb.toString();
    }

    public String toCSVString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OVERALL,").append("BOUNDED,").append("CONCURRENCY_PRESERVING,");
        sb.append("MAXIMUM_TOKEN_NB,").append("PARTITIONING,").append("INVARIANTS,");
        sb.append("ENVIRONMENT_TRANS,").append("SYSTEM1_TRANS,").append("SYSTEM2_TRANS,");
        sb.append("TYPE2_TRAP,").append("FIXPOINT,").append("GRAPH_STRAT,");
        sb.append("PG_STRAT,").append("DOT_SAVING,").append("NET_PL,").append("NET_TR,");
        sb.append("STRAT_PL,").append("STRAT_TR,").append("TOKENCOUNT,").append("CP,");
        sb.append("VAR_NB\n");
        Benchmark b = benchs.get(Parts.OVERALL);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.BOUNDED);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.CONCURRENCY_PRESERVING);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.MAXIMUM_TOKEN_NB);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.PARTITIONING);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.INVARIANTS);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.ENVIRONMENT_TRANS);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.SYSTEM1_TRANS);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.SYSTEM2_TRANS);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.TYPE2_TRAP);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.FIXPOINT);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.GRAPH_STRAT);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.PG_STRAT);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime()).append(",");
        } else {
            sb.append("-,");
        }
        b = benchs.get(Parts.DOT_SAVING);
        if (b != null) {
            sb.append(b.getStopTime() - b.getStartTime());
        } else {
            sb.append("-");
        }
        if (solver != null && strategy != null) {
            sb.append(",").append(solver.getNet().getPlaces().size()).append(",").append(solver.getNet().getTransitions().size());
            sb.append(",").append(strategy.getPlaces().size()).append(",").append(strategy.getTransitions().size());
            sb.append(",").append(solver.getGame().getTOKENCOUNT()).append(", ").append(((solver.getGame().isConcurrencyPreserving()) ? 1 : 0));
            sb.append(", ").append(solver.getVariableNumber());
        }
        return sb.toString();
    }
}
