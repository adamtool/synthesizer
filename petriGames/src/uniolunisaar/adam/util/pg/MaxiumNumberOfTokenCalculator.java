package uniolunisaar.adam.util.pg;

import uniolunisaar.adam.tools.Logger;
import java.util.Iterator;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;

/**
 * very primitive, just first naive idea for testing.
 * 
 * @author Manuel Gieseking
 */
public class MaxiumNumberOfTokenCalculator {

    public static long getMaximumNumberOfToken(PetriNet pn) {
        Logger.getInstance().addMessage("Calculate maximum number of token... ");
        CoverabilityGraph cv = CoverabilityGraph.get(pn);
        long max = 0;
        for (Iterator<CoverabilityGraphNode> it = cv.getNodes().iterator(); it.hasNext();) {
            CoverabilityGraphNode node = it.next();
            long nb = numberOfToken(node.getMarking());
            if (nb > max) {
                max = nb;
            }
        }        
        Logger.getInstance().addMessage("... calculation of maximum number of token done.");
        return max;
    }

    private static long numberOfToken(Marking m) {
        long nb = 0;
        for (Place p : m.getNet().getPlaces()) {
            nb += m.getToken(p).getValue();
        }
        return nb;
    }
}
