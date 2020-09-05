package uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach;

import java.io.File;
import java.lang.reflect.Field;
import uniolunisaar.adam.ds.synthesis.solver.LLSolverOptions;
import uniolunisaar.adam.exceptions.distrsynt.symbolic.bddapproach.NoSuchBDDLibraryException;
import uniolunisaar.adam.tools.AdamProperties;

/**
 * This class is used to store solver specific options for the BDD solvers.
 *
 * @author Manuel Gieseking
 */
public class BDDSolverOptions extends LLSolverOptions {

    //"buddy", "cudd", "cal", "j", "java", "jdd", "test", "typed",
//    private String libraryName = "buddy";
    private String libraryName = "java";
    private int maxIncrease = 100000000;
    private int initNodeNb = 1000000;
    private int cacheSize = 1000000;
    private boolean gg = false;
    private boolean ggs = false;
    private boolean pgs = true;
    private boolean noType2 = false;

    public BDDSolverOptions() {
        super("bdd", true, true);
    }

    public BDDSolverOptions(boolean skip) {
        super(skip, "bdd");
    }

    public BDDSolverOptions(boolean skipTests, boolean withAutomaticTransitAnnotation) {
        super("bdd", skipTests, withAutomaticTransitAnnotation);
    }

//    public BDDSolverOptions(String name, String libraryName, int maxIncrease, int initNodeNb, int cacheSize) {
//        super(name);
//        this.libraryName = libraryName;
//        this.maxIncrease = maxIncrease;
//        this.initNodeNb = initNodeNb;
//        this.cacheSize = cacheSize;
//    }
//
//    public BDDSolverOptions(boolean gg, boolean ggs, boolean pgs) {
//        super("bdd");
//        this.gg = gg;
//        this.ggs = ggs;
//        this.pgs = pgs;
//    }
    public void setLibraryName(String libraryName) throws NoSuchBDDLibraryException {
        if (!(libraryName.equals("buddy") || libraryName.equals("cudd") || libraryName.equals("cal") || libraryName.equals("jdd") || libraryName.equals("java"))) {
            throw new NoSuchBDDLibraryException(libraryName);
        }
        if (!libraryName.equals("java")) {
            String libsPath = System.getProperty("java.library.path");
            String[] libs = libsPath.split(File.pathSeparator);
            boolean found = false;
            for (String lib : libs) {
                if (lib.equals(libraryName)) {
                    found = true;
                }
            }
            if (!found) {
                String libPath = AdamProperties.getInstance().getProperty(libraryName);
                System.setProperty("java.library.path", libsPath + File.pathSeparator + libPath);
                // todo: the next lines are a hack since java does not allow to 
                // change the java.library.path after the starting of the JVM
                // compare: https://stackoverflow.com/questions/5419039/is-djava-library-path-equivalent-to-system-setpropertyjava-library-path/24988095#24988095
                // another possibillity is to set it with -D as VM parameter or
                // to adapt the javabdd library to allow in the BuddyFactory and
                // all the other classes for having an external link to the library
                // The only way of getting rid of the warning seems to be :
                // System.err.close(); // maybe this is not the best way...
                // We cannot redirect, because they have saved the System.err in
                // an early state for putting their warning.
                try {
                    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                    fieldSysPath.setAccessible(true);
                    fieldSysPath.set(null, null);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
                    throw new NoSuchBDDLibraryException(libraryName + " (add to path error)");
                }
            }
        }
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

    public boolean isNoType2() {
        return noType2;
    }

    public void setNoType2(boolean noType2) {
        this.noType2 = noType2;
    }

}
