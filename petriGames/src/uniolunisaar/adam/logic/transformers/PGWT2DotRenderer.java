package uniolunisaar.adam.logic.transformers;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.transformers.pnwt.PNWT2DotRenderer;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 */
public class PGWT2DotRenderer<G extends PetriGameWithTransits> extends PNWT2DotRenderer<G> {

    private final String SYSTEM_COLOR = "gray";

    @Override
    public String render(G net, boolean withLabel, boolean withOrigPlaces) {
        return render("PetriGameWithTransits", net, withLabel, withOrigPlaces);
    }

    @Override
    protected String getHeader(G net, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getHeader(net, name));
        // are there final markings? Add them
        if (!net.getFinalMarkings().isEmpty()) {
            sb.append("markings [shape=box, style=dashed, label=\"");
            for (Marking finalMarking : net.getFinalMarkings()) {
                sb.append("{");
                for (Place place : net.getPlaces()) {
                    if (finalMarking.getToken(place).getValue() > 0) {
                        sb.append(place.getId()).append(",");
                    }
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append("}\n");
            }
            sb.append("\"];\n");
        }
        return sb.toString();
    }

    @Override
    protected String getPlacesAdditionalStyles(G net, boolean withOrigPlaces, Place place) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getPlacesAdditionalStyles(net, withOrigPlaces, place));
        // add the coloring of the places according to env or system places
        // when it is not the mode where we want to see the partitions
        if (nb_partitions != null && !net.isEnvironment(place)) {
            sb.append(", style=\"filled\", fillcolor=" + SYSTEM_COLOR);
        }
        return sb.toString();
    }

    @Override
    protected String getFlowsAdditionalStyles(G net, Flow f) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getFlowsAdditionalStyles(net, f));
        if (net.isSpecial(f)) {
            sb.append(", style=dotted");
        } else {
            sb.append(", style=solid");
        }
        return sb.toString();
    }

}
