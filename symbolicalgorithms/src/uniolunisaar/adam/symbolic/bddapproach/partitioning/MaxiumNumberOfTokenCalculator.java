package uniolunisaar.adam.symbolic.bddapproach.partitioning;

import java.util.Iterator;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniolunisaar.adam.symbolic.bddapproach.util.benchmark.Benchmarks;
import uniolunisaar.adam.util.Logger;

/**
 * very primitive, just first naiv idea for testing.
 * 
 * @author Manuel Gieseking
 */
public class MaxiumNumberOfTokenCalculator {

    public static int getMaximumNumberOfToken(PetriNet pn) {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.MAXIMUM_TOKEN_NB);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS

        Logger.getInstance().addMessage("Calculate maximum number of token... ");
        CoverabilityGraph cv = CoverabilityGraph.get(pn);
        int max = 0;
        for (Iterator<CoverabilityGraphNode> it = cv.getNodes().iterator(); it.hasNext();) {
            CoverabilityGraphNode node = it.next();
            int nb = numberOfToken(node.getMarking());
            if (nb > max) {
                max = nb;
            }
        }
        Logger.getInstance().addMessage("... calculation of maximum number of token done.");

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.MAXIMUM_TOKEN_NB);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return max;
    }

    private static int numberOfToken(Marking m) {
        int nb = 0;
        for (Place p : m.getNet().getPlaces()) {
            nb += m.getToken(p).getValue();
        }
        return nb;
    }
}
