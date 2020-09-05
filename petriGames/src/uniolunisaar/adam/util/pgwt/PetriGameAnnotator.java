package uniolunisaar.adam.util.pgwt;

/**
 *
 * @author Manuel Gieseking
 */
@Deprecated
public class PetriGameAnnotator {

//        public static void parseNetOptionsAndAnnotate(PetriNet net) throws CouldNotFindSuitableWinningConditionException, ParseException {
//        parseAndAnnotateWinningCondition(net);
//        WinningCondition.Objective win = AdamExtensions.getWinningCondition(net);
//        if (win == WinningCondition.Objective.E_SAFETY
//                || win == WinningCondition.Objective.A_REACHABILITY
//                || win == WinningCondition.Objective.E_BUCHI
//                || win == WinningCondition.Objective.A_BUCHI
//                || win == WinningCondition.Objective.E_PARITY
//                || win == WinningCondition.Objective.A_PARITY) {
//            parseAndAnnotateTokenflow(net);
//        } else if (win == WinningCondition.Objective.A_SAFETY
//                || win == WinningCondition.Objective.E_REACHABILITY) {
////            try {
////                parseAndAnnotateTokenflow(net);
////            } catch (ParseException pe) {
////
////            }
//        }
//    }
//    private static void parseAndAnnotateWinningCondition(PetriNet net) throws CouldNotFindSuitableWinningConditionException {
//        if (AdamExtensions.hasWinningCondition(net)) {
//            try {
//                WinningCondition.Objective winCon = WinningCondition.Objective.valueOf(AdamExtensions.getWinningConditionText(net));
//                AdamExtensions.setWinningCondition(net, winCon);
//            } catch (ClassCastException | IllegalArgumentException e) {
//                String win = AdamExtensions.getWinningConditionText(net);
//                // Set some standards concerning existential or universal
//                if (win.equals("SAFETY")) {
//                    AdamExtensions.setWinningCondition(net, WinningCondition.Objective.A_SAFETY);
//                    return;
//                }
//                if (win.equals("REACHABILITY")) {
//                    AdamExtensions.setWinningCondition(net, WinningCondition.Objective.E_REACHABILITY);
//                    return;
//                }
//                if (win.equals("BUCHI")) {
//                    AdamExtensions.setWinningCondition(net, WinningCondition.Objective.E_BUCHI);
//                    return;
//                }
//                throw new CouldNotFindSuitableWinningConditionException(net, e);
//            }
//        } else {
//            throw new CouldNotFindSuitableWinningConditionException(net);
//        }
//    }
//    public static void parseAndAnnotateTokenflow(PetriGame game) throws ParseException {
//        parseAndAnnotateTokenflow(game, true);
//    }
//
//    public static void parseAndAnnotateTokenflow(PetriGame game, boolean withAutomaticallyGuessing) throws ParseException {
//        //todo: hack. change it, when the new implemenation of the flows is implmemented
////        System.out.println(" BLUBLBLER : " + game.getExtension("winningCondition"));
//        if (game.getExtension("winningCondition").equals("A_SAFETY")
//                || game.getExtension("winningCondition").equals("SAFETY")
//                || game.getExtension("winningCondition").equals("E_REACHABILITY")
//                || game.getExtension("winningCondition").equals("REACHABILITY")) {
//            return;
//        }
//        for (Transition t : game.getTransitions()) {
//            if (game.hasTokenFlow(t)) {
//                String flow = PetriGameExtensionHandler.getTokenFlowAnnotation(t);
//                if (!flow.isEmpty()) {
////                    System.out.println("flow_" + flow + "_ende");
//                    List<TokenFlow> tfl = TokenFlowParser.parse(game, t, flow);
////                    System.out.println(tfl.toString());
////                   //  old manual parser
////                String[] tupels = flow.split(",");
////                for (String tupel : tupels) {
////                    String[] comp = tupel.split("->");
////                    if (comp.length != 2) {
////                        throw new ParseException(tupel + " is not in a suitable format 'p1->p2'");
////                    }
////                    try {
////                        p1 = net.getPlace(comp[0]);
////                        p2 = net.getPlace(comp[1]);
////                        if (!t.getPreset().contains(p1)) {
////                            throw new ParseException(p1.getId() + " is not in the preset of transition " + t.getId() + " as annotated in " + tupel);
////                        }
////                        if (!t.getPostset().contains(p2)) {
////                            throw new ParseException(p2.getId() + " is not in the postset of transition " + t.getId() + " as annotated in " + tupel);
////                        }
////                    } catch (NoSuchNodeException e) {
////                        throw new ParseException(tupel + " does not point to existing nodes of the net '" + net.getName() + "'.", e);
////                    }
////                }
//                    game.setTokenFlow(t, tfl);
//                }
//            } else if (withAutomaticallyGuessing) { // nothing annotated should mean the annotation is unique
//                List<TokenFlow> tfl = new ArrayList<>();
//                Place pre;
//                Set<Place> postset = new HashSet<>();
//                if (t.getPreset().size() == 1) {
//                    pre = t.getPreset().iterator().next();
//                    if (game.isEnvironment(pre)) {
//                        if (t.getPostset().size() == 1) {
//                            Place p2 = t.getPostset().iterator().next();
//                            if (game.isEnvironment(p2)) {
//                                postset.add(p2);
//                            } else {
//                                throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                            }
//                        } else {
//                            throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                        }
//                    } else {
//                        for (Place p : t.getPostset()) {
//                            if (game.isEnvironment(p)) {
//                                throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                            }
//                            postset.add(p);
//                        }
//                    }
//                    tfl.add(game.createTokenFlow(pre, t, postset.toArray(new Place[postset.size()])));
//                    game.setTokenFlow(t, tfl);
//                } else if (t.getPreset().size() == 2) {
//                    Iterator<Place> it = t.getPreset().iterator();
//                    Place p1 = it.next();
//                    Place p2 = it.next();
//                    Place env = null;
//                    Place sys = null;
//                    if (game.isEnvironment(p1) && !game.isEnvironment(p2)) {
//                        env = p1;
//                        sys = p2;
//                    } else if (game.isEnvironment(p2) && !game.isEnvironment(p1)) {
//                        env = p2;
//                        sys = p1;
//                    } else {
//                        throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                    }
//
//                    pre = env;
//                    for (Place p : t.getPostset()) {
//                        if (game.isEnvironment(p)) {
//                            postset.add(p);
//                        }
//                    }
//                    if (postset.isEmpty() && !t.getPostset().isEmpty()) {
//                        throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                    }
//                    tfl.add(game.createTokenFlow(pre, t, postset.toArray(new Place[postset.size()])));
//                    pre = sys;
//                    for (Place p : t.getPostset()) {
//                        if (!game.isEnvironment(p)) {
//                            postset.add(p);
//                        }
//                    }
//
//                    tfl.add(game.createTokenFlow(pre, t, postset.toArray(new Place[postset.size()])));
//                    game.setTokenFlow(t, tfl);
//                } else {
//                    throw new ParseException("You didn't gave a flow annotation for transition '" + t.getId() + "' and it's not possible to guess it uniquely.");
//                }
//            }
////// Adds for all places im pre as well as im postset which are not mapped to tokenflows new tokenflows (I don't think that I want to have it anymore)
////            for (Place p : t.getPreset()) {
////                boolean found = false;
////                for (TokenFlow tokenFlow : tfl) {
////                    if (tokenFlow.getPresetPlace().equals(p)) {
////                        found = true;
////                        break;
////                    }
////                }
////                if (!found) {
////                    TokenFlow tf = game.createInitialTokenFlow(game, p, t);
////                    tfl.add(tf);
////                }
////            }
////            for (Place p : t.getPostset()) {
////                boolean found = false;
////                for (TokenFlow tokenFlow : tfl) {
////                    if (tokenFlow.getPostset().contains(p)) {
////                        found = true;
////                        break;
////                    }
////                }
////                if (!found) {
////                    TokenFlow tf = new TokenFlow(game, t);
////                    tf.addPostsetPlace(p);
////                    tfl.add(tf);
////                }
////            }
////            game.setTokenFlow(t, tfl);
//        }
//    }
}
