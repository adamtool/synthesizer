package uniolunisaar.adam.symbolic.bddapproach.util;

import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDFactory.GCStats;
import net.sf.javabdd.BDDFactory.ReorderStats;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * Bufferclass to redirect the GCCallback of JavaBDD from System.err to
 * System.out
 *
 * @author Manuel Gieseking
 */
public class JavaBDDCallback {

    private final BDDFactory bddfac;

    public JavaBDDCallback(BDDFactory bddfac) {
        this.bddfac = bddfac;
    }

    public void outGCStats(Integer pre, GCStats s) {
        if (pre == 0) {
            Logger.getInstance().addMessage(s.toString(), true);
        }
    }

    public void outReorderStats(Integer prestate, ReorderStats s) {
        int verbose = 1;
        if (verbose > 0) {
            if (prestate == 1) {
                Logger.getInstance().addMessage("Start reordering", true);
                s.usednum_before = bddfac.getNodeNum();
                s.time = System.currentTimeMillis();
            } else {
                s.time = System.currentTimeMillis() - s.time;
                s.usednum_after = bddfac.getNodeNum();
                Logger.getInstance().addMessage("End reordering. " + s, true);
            }
        }
    }

    public void outResizeStats(Integer oldsize, Integer newsize) {
        int verbose = 1;
        if (verbose > 0) {
            Logger.getInstance().addMessage("Resizing node table from " + oldsize + " to " + newsize, true);
        }
    }
}
