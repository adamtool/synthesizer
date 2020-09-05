package uniolunisaar.adam.util.benchmarks;

/**
 *
 * @author Manuel Gieseking
 */
public class Benchmark {

    private final String name;
    private long startTime;
    private long stopTime;
    private long startMemory;
    private long stopMemory;

    public Benchmark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getStartMemory() {
        return startMemory;
    }

    public void setStartMemory(long startMemory) {
        this.startMemory = startMemory;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopMemory() {
        return stopMemory;
    }

    public void setStopMemory(long stopMemory) {
        this.stopMemory = stopMemory;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append("\n");
        sb.append("elapsed time: ").append(stopTime - startTime).append("\n");
        sb.append("memory: ").append(stopMemory - startMemory);
        return sb.toString();
    }

    public long getElapsedTime() {
        return stopTime - startTime;
    }

    public long getElapsedMemory() {
        return stopMemory - startMemory;
    }
}
