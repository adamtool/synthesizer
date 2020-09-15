package uniolunisaar.adam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.AptPNParser;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.BoundingBox;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.ConcurrencyPreservingCalculator;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.logic.parser.transits.TransitParser;
import uniolunisaar.adam.tools.AdamProperties;
import uniolunisaar.adam.tools.processHandling.ExternalProcessHandler;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.processHandling.ProcessPool;
import uniolunisaar.adam.util.synthesis.NotSolvableWitness;
import uniolunisaar.adam.tools.Tools;
import static uniolunisaar.adam.util.PNWTTools.getTransitRelationFromTransitions;
import uniolunisaar.adam.util.pgwt.ExtensionCalculator;
import uniolunisaar.adam.util.pgwt.TransitCalculator;

/**
 *
 * @author Manuel Gieseking
 */
public class PGTools {

    private static boolean hasConditionAnnotation(PetriNet net) {
        return net.hasExtension(AdamExtensions.condition.name())
                || net.hasExtension(AdamExtensions.winningCondition.name())// todo: this is only for the fallback to the just-sythesis-version.
                ;
    }

    private static String getConditionAnnotation(PetriNet net) {
        return (String) net.getExtension(AdamExtensions.condition.name());
    }

    public static void setConditionAnnotation(PetriNet net, Condition.Objective con) {
        net.putExtension(AdamExtensions.condition.name(), con.name(), ExtensionProperty.WRITE_TO_FILE);
    }

    public static Condition.Objective parseConditionFromNetExtensionText(PetriNetWithTransits net) throws CouldNotFindSuitableConditionException {
        if (hasConditionAnnotation(net)) {
            try {
                // todo: this is only for the fallback to the just-synthesis-version.
                String con;
                if (net.hasExtension(AdamExtensions.winningCondition.name())) {
                    con = (String) net.getExtension(AdamExtensions.winningCondition.name());
                    net.removeExtension(AdamExtensions.winningCondition.name());
                    net.putExtension(AdamExtensions.condition.name(), con, ExtensionProperty.WRITE_TO_FILE);
                } else {
                    con = getConditionAnnotation(net);
                }
                Condition.Objective winCon = Condition.Objective.valueOf(con);
                return winCon;
            } catch (ClassCastException | IllegalArgumentException e) {
                String con = getConditionAnnotation(net);
                // Set some standards concerning existential or universal
                if (con.equals("SAFETY")) {
                    return Condition.Objective.A_SAFETY;
                }
                if (con.equals("REACHABILITY")) {
                    return Condition.Objective.E_REACHABILITY;
                }
                if (con.equals("BUCHI")) {
                    return Condition.Objective.E_BUCHI;
                }
                throw new CouldNotFindSuitableConditionException(net, e);
            }
        } else {
            throw new CouldNotFindSuitableConditionException(net);
        }
    }

    private static String deleteQuoteFromMaxTokenCount(String aptText, String key) {
        return aptText.replaceAll(key + "=\"([^\"]*)\"", key + "=$1");
    }

    /**
     * Uses a heuristic to set the coordinates of the strategy according to the
     * original nodes of the game. Starts with the initially marked places and
     * recursively adds the postsets of nodes with the original distances.
     *
     * Here we use a depth-first approach so every place which already exists in
     * this branch must be placed lower, repeating places in other branches lead
     * to an xshift.
     *
     * For a breadth-first approach one could save the past of the nodes to
     * decide whether a token has to be shifted, but this seems to be more
     * expensive.
     *
     * All places and transitions have to be labeled with the corresponding
     * place of the Petri game.
     *
     * @param game
     * @param strategy
     */
    public static void addCoordinates(PetriGameWithTransits game, PetriGameWithTransits strategy) {
        final double xshift = 100;
        final BoundingBox bb = PNTools.calculateBoundingBox(game);
        // a mapping collecting for each place of the game all corresponding places of the strategy
        final Map<String, List<Place>> mapping = new HashMap<>();
        final Set<Place> marking = new HashSet<>();
        // sets coords for the initial marking (same as in game)
        final Marking m = strategy.getInitialMarking();
        for (Place place : strategy.getPlaces()) {
            if (m.getToken(place).getValue() > 0) {
                // add the place
                marking.add(place);
                // set the original coordinates
                final Place orig = game.getPlace(PetriNetExtensionHandler.getOrigID(place));
                PetriNetExtensionHandler.setXCoord(place, PetriNetExtensionHandler.getXCoord(orig));
                PetriNetExtensionHandler.setYCoord(place, PetriNetExtensionHandler.getYCoord(orig));
                // add it to the mapping
                final List<Place> corr = new ArrayList<>();
                corr.add(place);
                mapping.put(orig.getId(), corr);
            }
        }
        annotateSuccessors(game, strategy, marking, mapping, xshift, bb);
    }

    /**
     * Takes an annotated marking and recursively in depth-first order annotates
     * the following markings.
     *
     * @param game
     * @param strategy
     * @param marking
     * @param mapping
     * @param xshift
     * @param bb
     */
    private static void annotateSuccessors(PetriGameWithTransits game, PetriGameWithTransits strategy, Set<Place> marking, Map<String, List<Place>> mapping,
            double xshift, BoundingBox bb) {
        // all fireable transitions (in a strategy that are all of the postset of the current marking)
        for (Place place : marking) {
            // $$$$ the successor transitions
            // get orig place coordinates
            Place origPlace = game.getPlace(PetriNetExtensionHandler.getLabel(place));
            double xdist = PetriNetExtensionHandler.getXCoord(place) - PetriNetExtensionHandler.getXCoord(origPlace);
            double ydist = PetriNetExtensionHandler.getYCoord(place) - PetriNetExtensionHandler.getYCoord(origPlace);
            for (Transition transition : place.getPostset()) {
                if (!PetriNetExtensionHandler.hasXCoord(transition)) { // only if not already handled
                    // get orig transition coordinates
                    Transition origTransition = game.getTransition(transition.getLabel());
                    double origX = PetriNetExtensionHandler.getXCoord(origTransition);
                    double origY = PetriNetExtensionHandler.getYCoord(origTransition);
                    // set the coordinates of the transition in the same distance of the input place
                    PetriNetExtensionHandler.setXCoord(transition, origX + xdist);
                    PetriNetExtensionHandler.setYCoord(transition, origY + ydist);
                    // $$$$ the successor places
                    for (Place post : transition.getPostset()) {
                        if (!PetriNetExtensionHandler.hasXCoord(post)) { // only if not already handled
                            // get orig place coordinates
                            Place origPost = game.getPlace(PetriNetExtensionHandler.getLabel(post));
                            origX = PetriNetExtensionHandler.getXCoord(origPost);
                            origY = PetriNetExtensionHandler.getYCoord(origPost);
                            double newX = origX + xdist;
                            double newY = origY + ydist;
                            // calc whether the new node is left or right of the middle of the original net
                            // to possible shift it the left or to the right respectivly
                            double mid = bb.getLeft() + (bb.getWidth() / 2.0f);
                            int direction = (newX < mid) ? -1 : 1;
                            // check if place already occupied (should only be possible by another place with the same label)
                            boolean isFree;
                            do {
                                isFree = true;
                                List<Place> corr = mapping.get(origPost.getId());
                                if (corr != null) {
                                    for (Place p : corr) {
                                        if (PetriNetExtensionHandler.getXCoord(p) == newX && PetriNetExtensionHandler.getYCoord(p) == newY) {
                                            newX += direction * xshift;
                                            isFree = false;
                                            break;
                                        }
                                    }
                                }
                            } while (!isFree);
                            // set the coordinates of the transition in the same distance of the input place
                            PetriNetExtensionHandler.setXCoord(post, newX);
                            PetriNetExtensionHandler.setYCoord(post, newY);
                            // add the place to the mapping
                            List<Place> map = mapping.get(origPost.getId());
                            if (map == null) {
                                map = new ArrayList<>();
                                mapping.put(origPost.getId(), map);
                            }
                            map.add(post);
                        }
                    }
                    // fire the transition
                    Set<Place> m = new HashSet<>(marking);
                    m.removeAll(transition.getPreset());
                    m.addAll(transition.getPostset());
                    annotateSuccessors(game, strategy, m, mapping, xshift, bb);
                }
            }
        }
    }

    /**
     * Uses a heuristic to set the coordinates of the strategy according to the
     * corresponding places of the given Petri game. This method only takes care
     * of the depths of the places. Duplicates in the breath are not handled
     * properly.
     *
     * All places and transitions have to be labeled with the corresponding
     * place of the Petri game.
     *
     * @param game
     * @param strategy
     */
    public static void addCoordinatesOnlyDepthCopies(PetriGameWithTransits game, PetriGameWithTransits strategy) {
        BoundingBox bb = PNTools.calculateBoundingBox(game);
        final double padding = 10;
        // collect all the places and transition with the same id
        Map<String, List<Node>> mapping = new HashMap<>();
        for (Node stratNode : strategy.getNodes()) {
            String label = PNTools.getLabel(strategy, stratNode);
            if (!mapping.containsKey(label)) {
                mapping.put(label, new ArrayList<>());
            }
            mapping.get(label).add(stratNode);
        }
        // add coordinates with the heuristic to keep the xcoordinates of the original node
        // and set the y-coordinate to depth*(width+padding), where depth is the time point
        // the node was created.
        Comparator<Node> comp = new Comparator<>() {
            /**
             * Compares places by their suffix '_StateID' of their ID, and
             * transition by the largest suffix of the prefixes.
             *
             * @param o1
             * @param o2
             * @return
             */
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getGraph() != o2.getGraph()) { // Petri nets don't match
                    throw new StructureException("Node '" + o1.getId() + "' and node '" + o2.getId() + "' does not belong to the same net.");
                }
                PetriNet net = o1.getGraph();
                if (net.containsPlace(o1.getId()) && net.containsPlace(o2.getId())) { // both are places
                    Integer id1 = Integer.parseInt(o1.getId().substring(o1.getId().lastIndexOf("_") + 1));
                    Integer id2 = Integer.parseInt(o2.getId().substring(o2.getId().lastIndexOf("_") + 1));
                    return id1.compareTo(id2);
                }
                if (net.containsTransition(o1.getId()) && net.containsTransition(o2.getId())) { // both are transitions
                    // take the largest suffix of the preset for comparison
                    Integer max1 = 0;
                    for (Node presetNode : o1.getPresetNodes()) {
                        Integer id = Integer.parseInt(o1.getId().substring(o1.getId().lastIndexOf("_") + 1));
                        if (id > max1) {
                            max1 = id;
                        }
                    }
                    Integer max2 = 0;
                    for (Node presetNode : o2.getPresetNodes()) {
                        Integer id = Integer.parseInt(o2.getId().substring(o2.getId().lastIndexOf("_") + 1));
                        if (id > max2) {
                            max2 = id;
                        }
                    }
                    return max1.compareTo(max2);
                }
                throw new StructureException("Only nodes of the same type (Place/Transition) can be compared.");
            }
        };
        // set the coordinates
        for (Map.Entry<String, List<Node>> entry : mapping.entrySet()) {
            String origID = entry.getKey();
            List<Node> nodes = entry.getValue();
            Collections.sort(nodes, comp); // sort them ascending
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                PetriNetExtensionHandler.setXCoord(node, PetriNetExtensionHandler.getXCoord(game.getNode(origID)));
                PetriNetExtensionHandler.setYCoord(node, PetriNetExtensionHandler.getYCoord(game.getNode(origID)) + i * (bb.getHeight() + padding));
            }
        }
    }

    public static void saveAPT(String path, PetriGameWithTransits game, boolean withAnnotationPartition, boolean withCoordinates) throws RenderException, FileNotFoundException {
        PNWTTools.saveAPT(path, game, withAnnotationPartition, withCoordinates);
    }

    public static String getAPT(PetriGameWithTransits game, boolean withAnnotationPartition, boolean withCoordinates) throws RenderException {
        String file = PNWTTools.getAPT(game, withAnnotationPartition, withCoordinates);
        // Since every value of the options is put as a String Object into the file,
        // delete the quotes for a suitable value (Integers can be parsed)
        // todo: maybe make the APT-Renderer more adaptive
        if (game.hasExtension(CalculatorIDs.MAX_TOKEN_COUNT.name())) {
            file = deleteQuoteFromMaxTokenCount(file, CalculatorIDs.MAX_TOKEN_COUNT.name());
        }
        return file;
    }

    /**
     * Creates a new Petri game with standard concurrency preserving and max
     * token count .
     *
     * @param name
     * @return
     */
    public static PetriGameWithTransits createPetriGame(String name) {
        return new PetriGameWithTransits(name, new ConcurrencyPreservingCalculator(), new MaxTokenCountCalculator());
    }

    /**
     * Creates a Petri game from the string in apt format given in content.
     *
     * @param content
     * @param skipTests
     * @param withAutomatic
     * @return
     * @throws NotSupportedGameException
     * @throws ParseException
     * @throws IOException
     * @throws
     * uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException
     */
    public static PetriGameWithTransits getPetriGameFromAPTString(String content, boolean skipTests, boolean withAutomatic) throws NotSupportedGameException, ParseException, IOException, CouldNotCalculateException {
        PetriNet pn = Tools.getPetriNetFromString(content);
        return getPetriGameFromParsedPetriNet(pn, skipTests, withAutomatic);
    }

    public static PetriGameWithTransits getPetriGame(String path, boolean skipTest, boolean withAutomatic) throws ParseException, IOException, NotSupportedGameException, CouldNotCalculateException {
        PetriNet pn = Tools.getPetriNet(path);
        return getPetriGameFromParsedPetriNet(pn, skipTest, withAutomatic);
    }

    public static PetriGameWithTransits getPetriGameFromParsedPetriNet(PetriNet net, boolean skipTests, boolean withAutomatic) throws NotSupportedGameException, ParseException, CouldNotCalculateException {
//        Condition.Condition win = parseConditionFromNetExtensionText(net);
        PetriGameWithTransits game = new PetriGameWithTransits(net, skipTests, new ConcurrencyPreservingCalculator(), new MaxTokenCountCalculator());
        parseAndCreateTransitsFromTransitionExtensionText(game, withAutomatic);
//        if (win == Condition.Condition.E_SAFETY
//                || win == Condition.Condition.A_REACHABILITY
//                || win == Condition.Condition.E_BUCHI
//                || win == Condition.Condition.A_BUCHI
//                || win == Condition.Condition.E_PARITY
//                || win == Condition.Condition.A_PARITY) {
//        PetriGameAnnotator.parseAndAnnotateTokenflow(game, true);
//        } else if (win == Condition.Condition.A_SAFETY
//                || win == Condition.Condition.E_REACHABILITY) {
////            try {
////                parseAndAnnotateTokenflow(net);
////            } catch (ParseException pe) {
////
////            }
//        }
        return game;
    }

    private static void parseAndCreateTransitsFromTransitionExtensionText(PetriGameWithTransits game, boolean withAutomatic) throws ParseException, CouldNotCalculateException {
        //todo: hack. change it, when the new implemenation of the flows is implmemented
        if (game.hasExtension(AdamExtensions.condition.name())) {
            if (game.getExtension(AdamExtensions.condition.name()).equals("A_SAFETY")
                    || game.getExtension(AdamExtensions.condition.name()).equals("SAFETY")
                    || game.getExtension(AdamExtensions.condition.name()).equals("E_REACHABILITY")
                    || game.getExtension(AdamExtensions.condition.name()).equals("REACHABILITY")) {
                return;
            }
        } else if (game.hasExtension(AdamExtensions.winningCondition.name())) { // todo: this is only for the fallback to the just-sythesis-version.
            if (game.getExtension(AdamExtensions.winningCondition.name()).equals("A_SAFETY")
                    || game.getExtension(AdamExtensions.winningCondition.name()).equals("SAFETY")
                    || game.getExtension(AdamExtensions.winningCondition.name()).equals("E_REACHABILITY")
                    || game.getExtension(AdamExtensions.winningCondition.name()).equals("REACHABILITY")) {
                return;
            }
        }

        for (Transition t : game.getTransitions()) {
            if (PNWTTools.hasTransitAnnotation(t)) {
                String flow = PNWTTools.getTransitAnnotation(t);
                if (!flow.isEmpty()) {
//                    System.out.println("flow_" + flow + "_ende");
                    TransitParser.parse(game, t, flow);
//                    System.out.println(tfl.toString());
//                   //  old manual parser
//                String[] tupels = flow.split(",");
//                for (String tupel : tupels) {
//                    String[] comp = tupel.split("->");
//                    if (comp.length != 2) {
//                        throw new ParseException(tupel + " is not in a suitable format 'p1->p2'");
//                    }
//                    try {
//                        p1 = net.getPlace(comp[0]);
//                        p2 = net.getPlace(comp[1]);
//                        if (!t.getPreset().contains(p1)) {
//                            throw new ParseException(p1.getId() + " is not in the preset of transition " + t.getId() + " as annotated in " + tupel);
//                        }
//                        if (!t.getPostset().contains(p2)) {
//                            throw new ParseException(p2.getId() + " is not in the postset of transition " + t.getId() + " as annotated in " + tupel);
//                        }
//                    } catch (NoSuchNodeException e) {
//                        throw new ParseException(tupel + " does not point to existing nodes of the net '" + net.getName() + "'.", e);
//                    }
//                }
//                    game.setTokenFlow(t, tfl);
                }
            } else if (withAutomatic) {
                TransitCalculator.automaticallyCreateTransitsForTransitlessTransition(game, t);
            }
        }
    }

    public static boolean isDeterministic(PetriGameWithTransits strat, CoverabilityGraph cover) {
        boolean det = true;
        for (Place place : strat.getPlaces()) {
            if (!strat.isEnvironment(place)) {
                Set<Transition> post = place.getPostset();
                for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
                    CoverabilityGraphNode next = iterator.next();
                    Marking m = next.getMarking();
                    boolean exTransition = false;
                    for (Transition transition : post) {
                        Set<Place> pre = transition.getPreset();
                        boolean isSubset = true;
                        for (Place place1 : pre) {
                            if (m.getToken(place1).getValue() <= 0) {
                                isSubset = false;
                            }
                        }
                        if (isSubset) {
                            if (exTransition) {
                                return false;
                            }
                            exTransition = true;
                        }
                    }
                }
            }
        }
        return det;
    }

    public static boolean isEnvTransition(PetriGameWithTransits game, Transition t) {
        for (Place p : t.getPreset()) {
            if (!game.isEnvironment(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given strategy restricts any purely environment
     * transition, i.e., which only has environment places in its preset. In the
     * case of only one environment player there is no need for the reachability
     * check because the place must be reachable, otherwise it would not be in
     * the strategy, and a purely environment transition can only have this
     * place in its preset. For more than one environment place we have to check
     * whether the purely environment transition is not in the strategy due to
     * the possibly not reachable other environment places.
     *
     * @param origNet
     * @param strat
     * @param withReachableCheck
     * @return
     */
    public static boolean restrictsEnvTransition(PetriGameWithTransits origNet, PetriGameWithTransits strat, boolean withReachableCheck) {
        for (Place place : strat.getPlaces()) { // every env place of the strategy
            if (strat.isEnvironment(place)) {
                String id = strat.getOrigID(place);
                Place origPlace = origNet.getPlace(id);
                Set<Transition> post = origPlace.getPostset();
                for (Transition transition : post) {
                    if (isEnvTransition(origNet, transition)) { // should not restrict a single env transition
                        boolean found = false;
                        for (Transition t : place.getPostset()) {
                            // we must find the id of transition "transition"
                            if (t.getLabel().equals(transition.getId())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            if (withReachableCheck) {
                                // check whether the reason that there is no corresponding
                                // transition in the strategy is that the original transition
                                // is not reachable in the strategy
                                Set<Place> origPre = transition.getPreset();
                                CoverabilityGraph cover = strat.getReachabilityGraph();
                                for (Iterator<CoverabilityGraphNode> it = cover.getNodes().iterator(); it.hasNext();) {
                                    CoverabilityGraphNode node = it.next();
                                    Marking m = node.getMarking();
                                    if (m.getToken(place).getValue() <= 0) { // skip the markings which does not contain the place under consideration.
                                        continue;
                                    }
                                    Set<Place> stratPre = new HashSet<>();
                                    for (Place stratPlace : strat.getPlaces()) {
                                        if (m.getToken(stratPlace).getValue() > 0) {
                                            if (origPre.contains(origNet.getPlace(strat.getOrigID(stratPlace)))) {
                                                stratPre.add(stratPlace);
                                            }
                                        }
                                    }
                                    if (origPre.size() == stratPre.size()) {
                                        return true;
                                    }
                                }
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDeadlockAvoiding(PetriGameWithTransits origNet, PetriGameWithTransits strat, CoverabilityGraph cover) {
        for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            Marking m = next.getMarking();
            // Get marking in original net
            Marking mappedMarking = new Marking(origNet);
            for (Place place : strat.getPlaces()) {
                int val = (int) m.getToken(place).getValue();
                if (val > 0) {
                    mappedMarking = mappedMarking.addTokenCount(strat.getOrigID(place), val);
                }
            }
            // if there's a transition in the original isfirable
            boolean firable = false;
            for (Transition t : origNet.getTransitions()) {
                if (t.isFireable(mappedMarking)) {
                    firable = true;
                    break;
                }
            }
            if (firable) { // there must also be a firable transition in the strategy                
                boolean stratfirable = false;
                for (Transition t : strat.getTransitions()) {
                    if (t.isFireable(m)) {
                        stratfirable = true;
                        break;
                    }
                }
                if (!stratfirable) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Set<Transition> getSolelySystemTransitions(PetriGameWithTransits game) {
        Set<Transition> systrans = new HashSet<>();
        for (Transition t : game.getTransitions()) {
            boolean add = true;
            for (Place p : t.getPreset()) {
                if (game.isEnvironment(p)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                systrans.add(t);
            }
        }
        return systrans;
    }

    public static Set<Transition> getSolelyEnviromentTransitions(PetriGameWithTransits game) {
        Set<Transition> envtrans = new HashSet<>();
        for (Transition t : game.getTransitions()) {
            boolean add = true;
            for (Place p : t.getPreset()) {
                if (!game.isEnvironment(p)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                envtrans.add(t);
            }
        }
        return envtrans;
    }

    /**
     * If it's solvable returns null, otherwise the witness
     *
     * @param game
     * @param cover
     * @return
     */
    public static NotSolvableWitness isSolvablePetriGame(PetriGameWithTransits game, CoverabilityGraph cover) {
        Set<Transition> systrans = getSolelySystemTransitions(game);
        Set<Transition> envtrans = getSolelyEnviromentTransitions(game);
        for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            Marking m = next.getMarking();
            boolean firable = false;
            for (Transition envtran : envtrans) {
                if (envtran.isFireable(m)) {
                    firable = true;
                    break;
                }
            }
            if (!firable) {
                continue;
            }
            for (Transition systran : systrans) {
                if (systran.isFireable(m) && systran.getPreset().size() > 1) {
                    NotSolvableWitness witness = checkReachableMarkingForSolvable(next, m, game, systran, envtrans);
                    if (witness != null) {
                        return witness;
                    }
                }
            }
        }
        return null;
    }

    private static NotSolvableWitness checkReachableMarkingForSolvable(CoverabilityGraphNode node, Marking m, PetriGameWithTransits game, Transition systran, Set<Transition> envtrans) {
        //                    PetriNet subnet = new PetriNet(net);
//                    subnet.setInitialMarking(new Marking(m));
//                    CoverabilityGraph g = CoverabilityGraph.getReachabilityGraph(subnet);
//        CoverabilityGraph g = CoverabilityGraph.getReachabilityGraph(net);
//        for (Iterator<CoverabilityGraphNode> it = g.getNodes().iterator(); it.hasNext();) {
//            CoverabilityGraphNode n = it.next();
        for (CoverabilityGraphNode n : getReachableEnvSuccessorNodes(node, envtrans, systran)) {
            Marking m2 = n.getMarking();
            if (systran.isFireable(m2)) {
                for (Place p : systran.getPreset()) {
                    for (Transition t2 : p.getPostset()) {
                        if (!systran.equals(t2)) {
                            if (t2.isFireable(m2) && !t2.isFireable(m) && t2.getPreset().size() > 1) {
                                return new NotSolvableWitness(systran, t2, m, m2);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static List<CoverabilityGraphNode> getReachableEnvSuccessorNodes(CoverabilityGraphNode node, Set<Transition> envtrans, Transition systran) {
        List<CoverabilityGraphNode> visited = new ArrayList<>();
        for (CoverabilityGraphEdge edge : node.getPostsetEdges()) {
            if (envtrans.contains(edge.getTransition())) {
                if (!visited.contains(edge.getTarget())) {
                    addReachableNodes(visited, edge.getTarget(), systran);
                }
            }
        }
        return visited;
    }

    private static void addReachableNodes(List<CoverabilityGraphNode> visited, CoverabilityGraphNode node, Transition systran) {
        visited.add(node);
        for (CoverabilityGraphEdge succEdge : node.getPostsetEdges()) {
            if (!visited.contains(succEdge.getTarget())) {
                Set<Place> intersect = new HashSet<>(systran.getPreset());
                intersect.retainAll(succEdge.getTransition().getPreset());
                if (intersect.isEmpty()) {
                    addReachableNodes(visited, succEdge.getTarget(), systran);
                }
            }
        }
    }

    public static boolean checkStrategy(PetriGameWithTransits origNet, PetriGameWithTransits strat, boolean withReachabillityCheck) {
        boolean isStrat = true;
        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(strat);
        // deadlock avoiding
        System.out.println("DLA " + isDeadlockAvoiding(origNet, strat, cover));
        isStrat &= isDeadlockAvoiding(origNet, strat, cover);
        // (S1)
        System.out.println("DET " + isDeterministic(strat, cover));
        isStrat &= isDeterministic(strat, cover);
        // (S2)
        System.out.println("ENV " + !restrictsEnvTransition(origNet, strat, withReachabillityCheck));
        isStrat &= !restrictsEnvTransition(origNet, strat, withReachabillityCheck);
        return isStrat;
    }

    public static void checkValidPartitioned(PetriGameWithTransits game) throws InvalidPartitionException {
        // No transition has places of the same partition in its pre- or postset respectively
        for (Transition transition : game.getTransitions()) {
            List<Integer> partition = new ArrayList<>();
            for (Place place : transition.getPreset()) {
                int part = game.getPartition(place);
                if (partition.contains(part)) {
                    throw new InvalidPartitionException("Transition " + transition.getId() + " has two places with partition " + part + " in its preset.");
                }
                partition.add(part);
            }
            partition.clear();
            for (Place place : transition.getPostset()) {
                int part = game.getPartition(place);
                if (partition.contains(part)) {
                    throw new InvalidPartitionException("Transition " + transition.getId() + " has two places with partition " + part + " in its postset.");
                }
                partition.add(part);
            }
        }
        // there is no reachable marking which contains two tokens belonging to the same partition
        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(game);
        for (CoverabilityGraphNode node : cover.getNodes()) {
            Marking m = node.getMarking();
            long maxToken = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
            boolean[] part = new boolean[Math.toIntExact(maxToken)];
            for (Place place : game.getPlaces()) {
                if (m.getToken(place).getValue() > 0) {
                    int partition = game.getPartition(place);
                    if (part[partition]) {
                        throw new InvalidPartitionException(game, m, place);
                    } else {
                        part[partition] = true;
                    }
                }
            }
        }
    }

    private static Optional<Marking> getMarkingWithMultipleTokenOfType(boolean env, PetriGameWithTransits game) {
        CoverabilityGraph cover = CoverabilityGraph.getReachabilityGraph(game);
        // only one env token is allowed (todo: do it less expensive ?)
        for (Iterator<CoverabilityGraphNode> iterator = cover.getNodes().iterator(); iterator.hasNext();) {
            CoverabilityGraphNode next = iterator.next();
            Marking m = next.getMarking();
            boolean first = false;
            for (Place place : game.getPlaces()) { //todo: very expensive because of the marking implementation
                if (m.getToken(place).getValue() > 0 && (game.isEnvironment(place) == env)) {
                    if (first) {
                        return Optional.of(m);
                    }
                    first = true;
                }
            }
        }
        return Optional.empty();
    }

    public static void checkOnlyOneEnvToken(PetriGameWithTransits game) throws NotSupportedGameException {
        Optional<Marking> illegalMarking = getMarkingWithMultipleTokenOfType(true, game);
        if (illegalMarking.isPresent()) {
            throw new NotSupportedGameException("There are two environment token in marking " + illegalMarking.get().toString() + ". The BDD approach only allows one external source of information.");
        }
    }

    public static void checkOnlyOneSysToken(PetriGameWithTransits game) throws NotSupportedGameException {
        Optional<Marking> illegalMarking = getMarkingWithMultipleTokenOfType(false, game);
        if (illegalMarking.isPresent()) {
            throw new NotSupportedGameException("There are two system token in marking " + illegalMarking.get().toString() + ". The BDD approach only allows one external source of information.");
        }
    }

    /**
     * It is a copy of the method pnwt2dot only adds the gray color for system
     * places. Debugging true results in colouring the places according to their
     * partition. the maxtoken calculator must be added for that.
     *
     * @param game
     * @param withLabel
     * @param withDebugging
     * @return
     */
    public static String pg2Dot(PetriGameWithTransits game, boolean withLabel, boolean withDebugging) {
        final String placeShape = "circle";
        final String specialPlaceShape = "doublecircle";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph PetriNet {\n");

        // Transitions
        sb.append("#transitions\n");
        sb.append("node [shape=box, height=0.5, width=0.5, fixedsize=true];\n");
        for (Transition t : game.getTransitions()) {
            String c = null;
            if (game.isStrongFair(t)) {
                c = "blue";
            }
            if (game.isWeakFair(t)) {
                c = "lightblue";
            }
            String color = (c != null) ? "style=filled, fillcolor=" + c : "";

            sb.append("\"").append(t.getId()).append("\"").append("[").append(color);
            if (withLabel) {
                if (game.isStrongFair(t) || game.isWeakFair(t)) {
                    sb.append(", ");
                }
                sb.append("xlabel=\"").append(t.getLabel()).append("\"");
            }
            sb.append("];\n");
        }
        sb.append("\n\n");

        // Places
        sb.append("#places\n");
        for (Place place : game.getPlaces()) {
            // special?
            String shape = (game.isBad(place) || game.isReach(place) || game.isBuchi(place)) ? specialPlaceShape : placeShape;
            // Initialtoken number
            Long token = place.getInitialToken().getValue();
            String tokenString = (token > 0) ? token.toString() : "";
            // Drawing
            sb.append("\"").append(place.getId()).append("\"").append("[shape=").append(shape);
            sb.append(", height=0.5, width=0.5, fixedsize=true");
            sb.append(", xlabel=").append("\"").append(place.getId()).append("\"");
            sb.append(", label=").append("\"").append(tokenString).append("\"");

            if (game.hasPartition(place) && !game.isEnvironment(place)) {
                int t = game.getPartition(place);
                if (t != 0) {  // should it be colored?
                    sb.append(", style=\"filled");
                    if (game.isInitialTransit(place)) {
                        sb.append(", dashed");
                    }
                    sb.append("\", fillcolor=");
                    if (!withDebugging) {
                        sb.append("gray");
                    } else {
                        long tokencount = game.getValue(CalculatorIDs.MAX_TOKEN_COUNT.name());
                        sb.append("\"");
                        float val = ((t + 1) * 1.f) / (tokencount * 1.f);
                        sb.append(val).append(" ").append(val).append(" ").append(val);
                        sb.append("\"");
                    }
                } else if (game.isInitialTransit(place)) {
                    sb.append(", style=dashed");
                }
            } else if (!game.isEnvironment(place)) {
                sb.append(", style=\"filled\", fillcolor=gray");
            }
            sb.append("];\n");
        }

        // Flows
        Map<Flow, String> map = getTransitRelationFromTransitions(game);
        sb.append("\n#flows\n");
        for (Flow f : game.getEdges()) {
            sb.append("\"").append(f.getSource().getId()).append("\"").append("->").append("\"").append(f.getTarget().getId()).append("\"");
            Integer w = f.getWeight();
            String weight = "\"" + ((w != 1) ? w.toString() + " : " : "");
            if (map.containsKey(f)) {
                weight += map.get(f);
            }
            weight += "\"";
            sb.append("[label=").append(weight);
            if (map.containsKey(f)) {
                String tfl = map.get(f);
                if (!tfl.contains(",")) {
                    sb.append(", color=\"");
                    Transit init = game.getInitialTransit(f.getTransition());
                    int max = game.getTransits(f.getTransition()).size() + ((init == null) ? 0 : init.getPostset().size() - 1);
                    int id = Tools.calcStringIDSmallPrecedenceReverse(tfl);
                    float val = ((id + 1) * 1.f) / (max * 1.f);
                    sb.append(val).append(" ").append(val).append(" ").append(val);
                    sb.append("\"");
                }
            }
            if (game.isInhibitor(f)) {
                sb.append(", dir=\"both\", arrowtail=\"odot\"");
            }
            sb.append("]\n");
        }
        sb.append("overlap=false\n");
        sb.append("label=\"").append(game.getName()).append("\"\n");
        sb.append("fontsize=12\n");
        sb.append("}");
        return sb.toString();
    }

//    public static String getFlowRepresentativ(int id) {
//        int start = 97; // a
//        if (id > 25 && id < 51) {
//            start = 65; //A
//            id -= 25;
//        } else {
//            start = 
//        }
//        return String.valueOf(start + id);
//    }
    public static String pg2Tikz(PetriGameWithTransits game) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{tikzpicture}[node distance=12mm,>=stealth',bend angle=15,auto]\n");
        // Places
        for (Place place : game.getPlaces()) {
            // Bad?
            String bad = (game.isBad(place) || game.isReach(place) || game.isBuchi(place)) ? ",bad" : "";
            // Initialtoken number
            Long token = place.getInitialToken().getValue();
            String tokenString = (token > 0) ? ",tokens=" + token.toString() : "";
            // Systemplace?
            String type = game.isEnvironment(place) ? "envplace" : "sysplace";
            sb.append("\\node [").append(type).append(bad).append(tokenString).append("] (").append(place.getId()).append(") [label=above:\\(").append(place.getId()).append("\\)] {};\n");
        }
        sb.append("\n\n");

        // Transitions
        for (Transition t : game.getTransitions()) {
            sb.append("\\node [transition] (").append(t.getId()).append(") {\\(").append(t.getLabel()).append("\\)};\n");
        }
        sb.append("\n\n");

        // Flows
        sb.append("\\draw[->] \n");
        for (Transition t : game.getTransitions()) {
            sb.append("(").append(t.getId()).append(")");
            for (Place p : t.getPreset()) {
                sb.append(" edge [pre] (").append(p.getId()).append(")\n");
            }
            for (Place p : t.getPostset()) {
                sb.append(" edge [post] (").append(p.getId()).append(")\n");
            }
        }
        sb.append(";\n");
        sb.append("\\end{tikzpicture}");
        return sb.toString();
    }

    public static String petriGame2Dot(PetriGameWithTransits game, boolean withLabel) {
        return pg2Dot(game, withLabel, false);
    }

    public static void savePG2Dot(String input, String output, boolean withLabel) throws IOException, ParseException, NotSupportedGameException {
        PetriGameWithTransits game = new PetriGameWithTransits(Tools.getPetriNet(input));
        savePG2Dot(output, game, withLabel);
    }

    public static void savePG2Dot(String path, PetriGameWithTransits game, boolean withLabel) throws FileNotFoundException {
        savePG2Dot(path, game, withLabel, false);
    }

    public static void savePG2Dot(String path, PetriGameWithTransits game, boolean withLabel, boolean withDebugging) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(pg2Dot(game, withLabel, withDebugging));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
    }

    public static Thread savePG2DotAndPDF(String input, String output, boolean withLabel) throws IOException, InterruptedException, ParseException, NotSupportedGameException {
        return savePG2DotAndPDF(input, output, withLabel, false);
    }

    public static Thread savePG2DotAndPDF(String input, String output, boolean withLabel, boolean withDebugging) throws IOException, InterruptedException, ParseException, NotSupportedGameException {
        PetriGameWithTransits game = withDebugging ? new PetriGameWithTransits(new AptPNParser().parseFile(input), new MaxTokenCountCalculator(), new ConcurrencyPreservingCalculator())
                : new PetriGameWithTransits(new AptPNParser().parseFile(input));
        return savePG2DotAndPDF(output, game, withLabel, withDebugging);
    }

    public static Thread savePG2DotAndPDF(String path, PetriGameWithTransits game, boolean withLabel) throws IOException, InterruptedException {
        return savePG2DotAndPDF(path, game, withLabel, false);
    }

    public static Thread savePG2DotAndPDF(String path, PetriGameWithTransits game, boolean withLabel, boolean withDebugging) throws IOException, InterruptedException {
        savePG2Dot(path, game, withLabel, withDebugging);
        String dot = AdamProperties.getInstance().getProperty(AdamProperties.DOT);
        String[] command = {dot, "-Tpdf", path + ".dot", "-o", path + ".pdf"};
        ExternalProcessHandler procH = new ExternalProcessHandler(true, command);
        ProcessPool.getInstance().putProcess(PetriNetExtensionHandler.getProcessFamilyID(game) + "#dot", procH);
        // start it in an extra thread
        Thread thread = new Thread(() -> {
            try {
                procH.startAndWaitFor();
                Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
//                    if (deleteDot) {
//                        // Delete dot file
//                        new File(path + ".dot").delete();
//                        Logger.getInstance().addMessage("Deleted: " + path + ".dot", true);
//                    }
            } catch (IOException | InterruptedException ex) {
                String errors = "";
                try {
                    errors = procH.getErrors();
                } catch (ProcessNotStartedException e) {
                }
                Logger.getInstance().addError("Saving pdf from dot failed.\n" + errors, ex);
            }
        });
        thread.start();
        return thread;
    }

    public static Thread savePG2PDF(String path, PetriGameWithTransits game, boolean withLabel) throws IOException, InterruptedException {
        return savePG2PDF(path, game, withLabel, false);
    }

    public static Thread savePG2PDF(String path, PetriGameWithTransits game, boolean withLabel, boolean withDebugging) throws IOException, InterruptedException {
        String bufferpath = path + "_" + System.currentTimeMillis();
        Thread dot;
        dot = savePG2DotAndPDF(bufferpath, game, withLabel, withDebugging);
        Thread mvPdf = new Thread(() -> {
            try {
                dot.join();
                // Delete dot file
                new File(bufferpath + ".dot").delete();
                Logger.getInstance().addMessage("Deleted: " + bufferpath + ".dot", true);
                // move to original name 
                Files.move(new File(bufferpath + ".pdf").toPath(), new File(path + ".pdf").toPath(), REPLACE_EXISTING);
                Logger.getInstance().addMessage("Moved: " + bufferpath + ".pdf --> " + path + ".pdf", true);
            } catch (IOException | InterruptedException ex) {
                Logger.getInstance().addError("Deleting the buffer files and moving the pdf failed", ex);
            }
        });
        mvPdf.start();
        return mvPdf;
    }

    /**
     * Creates a PetriGame which has automatically named nodes and the original
     * ids in the label of the node. This can be used to create a net which is
     * definitely readably be the APT parser.
     *
     * @param game
     * @return
     */
    public static PetriGameWithTransits createPetriGameWithIDsInLabel(PetriGameWithTransits game) {
        PetriGameWithTransits out = new PetriGameWithTransits(game.getName());
        PNWTTools.addElementsForPNWTWithIDsInLabel(game, out);
        for (Map.Entry<String, ExtensionCalculator<?>> calc : game.getCalculators().entrySet()) {
            out.addExtensionCalculator(calc.getKey(), calc.getValue(), true); // todo: to greedy to add all of them as to listen to changes.
        }
        return out;
    }

}
