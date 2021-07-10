package uniolunisaar.adam.logic.transformers;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.BoundingBox;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.transformers.petrinet.PetriNet2TikzRenderer;

/**
 *
 * @author Manuel Gieseking
 */
public class PGWT2TikzRenderer extends PetriNet2TikzRenderer<PetriGameWithTransits> {

    private final String SPECIAL_STYLE = "double=black!20";
    private final String SYS_PLACE_STYLE = "circle,thick,draw=black!75,fill=black!20,minimum size=6mm";
    private final String ENV_PLACE_STYLE = "circle,thick,draw=black!75,fill=white,minimum size=6mm";

    @Override
    protected String header() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\tikzstyle{envplace}=[").append(ENV_PLACE_STYLE).append("]\n");
        sb.append("\\tikzstyle{sysplace}=[").append(SYS_PLACE_STYLE).append("]\n");
        sb.append("\\tikzstyle{special}=[").append(SPECIAL_STYLE).append("]\n");
        sb.append(super.header());
        return sb.toString();
    }

    String places(PetriGameWithTransits game, BoundingBox bb) {
        StringBuilder sb = new StringBuilder();
        for (Place place : game.getPlaces()) {
            double xcoord = norm(PetriNetExtensionHandler.getXCoord(place), bb.getLeft(), bb.getRight());
            double ycoord = -1 * norm(PetriNetExtensionHandler.getYCoord(place), bb.getTop(), bb.getBottom());
            // Bad?
            String special = (game.isBad(place) || game.isReach(place) || game.isBuchi(place)) ? ",bad" : "";
            // Initialtoken number
            Long token = place.getInitialToken().getValue();
            String tokenString = (token > 0) ? ", tokens=" + token.toString() : "";
            // Systemplace?
            String type = game.isEnvironment(place) ? "envplace" : "sysplace";
            sb.append("\\node[").
                    append(type).
                    append(special).
                    append(tokenString).
                    append("] at (").append(xcoord).append(", ").append(ycoord).append(")").
                    append(" (").append(place.getId()).
                    append(") [label=above:\\(\\mathit{").append(place.getId()).append("}").
                    append("\\)] {};\n");
        }
        sb.append("\n\n");
        return sb.toString();
    }

}
