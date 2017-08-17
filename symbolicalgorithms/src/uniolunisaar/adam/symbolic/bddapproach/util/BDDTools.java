package uniolunisaar.adam.symbolic.bddapproach.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGame;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDTools {

    private static final boolean print = false;

    public static String place2BinID(Place p, int digits) {
        String binID = Integer.toBinaryString((Integer) p.getExtension("id"));
        binID = String.format("%" + digits + "s", binID).replace(' ', '0');
        return new StringBuilder(binID).reverse().toString();
    }

    public static String place2BinID(Place s3, BDDPetriGame game, int token) {
        int digits = getBinLength(game, token);
        return place2BinID(s3, digits);
    }

//    public static String getPlace2BinIDMapping(PetriGame game) {
//        String ret = "";
//        for (Place p : game.getNet().getPlaces()) {
//            ret += p.getId() + ":" + place2BinID(p, game.getPL_CODE_LEN()) + ", ";
//        }
//        return ret;
//    }
//
//    public static void printPlace2BinIDMapping(PetriGame game, boolean force) {
//        if (print || force) {
//            System.out.println(getPlace2BinIDMapping(game));
//        }
//    }
    public static void printDecisionSets(BDD dcs) {
        printDecisionSets(dcs, false);
    }

    public static void printDecisionSets(BDD dcs, boolean force) {
        if (print || force) {
            @SuppressWarnings("unchecked")
            List<byte[]> l = dcs.allsat();
            System.out.println(l.size());
            for (byte[] sol : l) {
                // required for buddy library
                if (sol == null) {
                    continue;
                }
                System.out.println(Arrays.toString(sol));
            }
        }
    }

//    /**
//     * Deprecated. Do not fit when domains aren't created in the exact expected
//     * order.
//     */
//    public static void printDecisionSets(BDD dcs, BDDPetriGame game, boolean force) {
//        if (print || force) {
//            System.out.println(getDecisionSet(dcs, game));
//        }
//    }
//    /**
//     * Deprecated. Do not fit when domains aren't created in the exact expected
//     * order.
//     *
//     * @param dcs
//     * @param game
//     * @return
//     */
//    public static String getDecisionSetDeprecated(BDD dcs, BDDPetriGame game) {
//        StringBuilder s = new StringBuilder("");
//        @SuppressWarnings("unchecked")
//        List<byte[]> l = dcs.allsat();
//        for (byte[] sol : l) {
//            // required for buddy library
//            if (sol == null) {
//                continue;
//            }
//            s.append("|");
//            int counter = 0;
//            for (int i = 0; i < getBinLength(game, 0); i++) {
//                s.append(sol[counter++]);
//            }
//            for (int i = 1; i < game.getMaxTokenCount(); i++) {
//                s.append("|");
//                for (int j = 0; j < getBinLength(game, i); j++) {
//                    s.append(sol[counter++]);
//                }
//                s.append("|").append(sol[counter++]);
//                s.append("|").append(sol[counter++]).append("|");
//                for (int j = 0; j < game.getTransitions()[i - 1].size(); j++) {
//                    s.append(sol[counter++]);
//                }
//            }
//            s.append("| -> |");
//            for (int i = 0; i < getBinLength(game, 0); i++) {
//                s.append(sol[counter++]);
//            }
//            for (int i = 1; i < game.getMaxTokenCount(); i++) {
//                s.append("|");
//                for (int j = 0; j < getBinLength(game, i); j++) {
//                    s.append(sol[counter++]);
//                }
//                s.append("|").append(sol[counter++]);
//                s.append("|").append(sol[counter++]).append("|");
//                for (int j = 0; j < game.getTransitions()[i - 1].size(); j++) {
//                    s.append(sol[counter++]);
//                }
//            }
//            s.append("|\n");
//        }
//        return s.toString();
//    }
    public static void printDecodedDecisionSets(BDD dcs, BDDSolver<? extends WinningCondition> solver, boolean force) {
        if (print || force) {
            System.out.println(getDecodedDecisionSets(dcs, solver));
        }
    }

    public static int getBinLength(BDDPetriGame game, int token) {
        int add = (game.isConcurrencyPreserving()) ? -1 : 0;
        return Integer.toBinaryString(game.getPlaces()[token].size() + add).length();
    }
    
     /**
     * Deprecated. Do not fit when domains aren't created in the exact expected
     * order.
     *
     * @param dcs
     * @param solver
     * @return
     */
    public static String getDecodedDecisionSetsDeprecated(BDD dcs, BDDSolver<? extends WinningCondition> solver) {
        BDDPetriGame game = solver.getGame();
        // Decoding of places
        Map<String, String>[] pls = new Map[game.getMaxTokenCountInt()];
        for (int i = 0; i < game.getMaxTokenCount(); i++) {
            pls[i] = new HashMap<>();
            for (Place pl : game.getPlaces()[i]) {
                pls[i].put(place2BinID(pl, getBinLength(game, i)), pl.getId());
            }
        }
//        Map<Integer, String> transitions = new HashMap<>();
//        for (Transition t : game.getNet().getTransitions()) {
//            transitions.put((Integer) t.getExtension("id"), t.getId());
//        }
        String out = "";
        @SuppressWarnings("unchecked")
        List<byte[]> l = dcs.allsat();
        for (byte[] sol : l) {
            // required for buddy library
            if (sol == null) {
                continue;
            }
            String envBin = "";
            int counter = 0;
            String zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin.equals(zeros)) {
                envBin = pls[0].get(envBin);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                    envBin += ", " + sol[counter++];
                }
            } else {
                envBin = "-";
            }

            String tokens = "";
            for (int i = 1; i < game.getMaxTokenCount(); i++) {
                String id = "";
                zeros = "";
                for (int j = 0; j < getBinLength(game, i); j++) {
                    id += sol[counter++];
                    zeros += "0";
                }
                if (game.isConcurrencyPreserving() || !id.equals(zeros)) {
                    tokens += "(" + pls[i].get(id) + ", ";
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY) { // add type for safety
                        tokens += (sol[counter++] == 1) ? "1, " : (sol[counter - 1] == 0) ? "2, " : "-, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                        tokens += sol[counter++] + ", ";
                    }
                    tokens += (sol[counter++] == 1) ? "T, {" : (sol[counter - 1] == 0) ? "!T, {" : "-, {";
                    List<Transition> transitions = game.getTransitions()[i - 1];
                    for (int j = 0; j < transitions.size(); ++j) {
                        Transition trans = transitions.get(j);
                        byte t = sol[counter++];
                        if (t == 1) {
                            tokens += trans.getId() + ",";
                        } else if (t == -1) {
                            tokens += "-" + trans.getId() + ",";
                        }
                    }
                    tokens += "})\n";
                } else {
                    tokens += "( - )\n";
                    counter += 1 + game.getTransitions()[i - 1].size();
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // jump over
                        ++counter;
                    }
                }
            }
            String envBin_ = "";
            zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin_ += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin_.equals(zeros)) {
                envBin_ = pls[0].get(envBin_);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                    envBin_ += ", " + sol[counter++];
                }

            } else {
                envBin_ = "-";
            }
            String tokens_ = "";
            for (int i = 1; i < game.getMaxTokenCount(); i++) {
                String id = "";
                zeros = "";
                for (int j = 0; j < getBinLength(game, i); j++) {
                    id += sol[counter++];
                    zeros += "0";
                }
                if (game.isConcurrencyPreserving() || !id.equals(zeros)) {
                    tokens_ += "(" + pls[i].get(id);
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY) { // add type for safety
                        tokens_ += (sol[counter++] == 1) ? ", 1, " : (sol[counter - 1] == 0) ? ", 2, " : ", -, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                        tokens_ += sol[counter++] + ", ";
                    }
                    tokens_ += (sol[counter++] == 1) ? "T, {" : (sol[counter - 1] == 0) ? "!T, {" : "-, {";
                    List<Transition> transitions = game.getTransitions()[i - 1];
                    for (int j = 0; j < transitions.size(); ++j) {
                        Transition trans = transitions.get(j);
                        byte t = sol[counter++];
                        if (t == 1) {
                            tokens_ += trans.getId() + ",";
                        } else if (t == -1) {
                            tokens_ += "-" + trans.getId() + ",";
                        }
                    }
                    tokens_ += "})\n";
                } else {
                    tokens_ += "( - )\n";
                    counter += 1 + game.getTransitions()[i - 1].size();
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // jump over
                        ++counter;
                    }
                }
            }
            out += envBin + ",\n" + tokens + " ->\n" + envBin_ + ",\n" + tokens_ + "\n";
        }
        return out;
    }

    /**
     * Do not fit when domains aren't created in the exact expected
     * order. This could be fixed by using PLACES[0][i - 1].vars() to get 
     * the indizes of the domain variables. TODO: do it...
     *
     * @param dcs
     * @param solver
     * @return
     */
    public static String getDecodedDecisionSets(BDD dcs, BDDSolver<? extends WinningCondition> solver) {
        BDDPetriGame game = solver.getGame();
        // Decoding of places
        Map<String, String>[] pls = new Map[game.getMaxTokenCountInt()];
        for (int i = 0; i < game.getMaxTokenCount(); i++) {
            pls[i] = new HashMap<>();
            for (Place pl : game.getPlaces()[i]) {
                pls[i].put(place2BinID(pl, getBinLength(game, i)), pl.getId());
            }
        }
//        Map<Integer, String> transitions = new HashMap<>();
//        for (Transition t : game.getNet().getTransitions()) {
//            transitions.put((Integer) t.getExtension("id"), t.getId());
//        }
        String out = "";
        @SuppressWarnings("unchecked")
        List<byte[]> l = dcs.allsat();
        for (byte[] sol : l) {
            // required for buddy library
            if (sol == null) {
                continue;
            }
            // Enviroment
            String envBin = "";
            int counter = 0;
            String zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin.equals(zeros)) {
                envBin = pls[0].get(envBin);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                    envBin += ", " + sol[counter++];
                }
            } else {
                envBin = "-";
            }

            String tokens = "";
            for (int i = 1; i < game.getMaxTokenCount(); i++) {
                String id = "";
                zeros = "";
                for (int j = 0; j < getBinLength(game, i); j++) {
                    id += sol[counter++];
                    zeros += "0";
                }
                if (game.isConcurrencyPreserving() || !id.equals(zeros)) {
                    tokens += "(" + pls[i].get(id) + ", ";
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY) { // add type for safety
                        tokens += (sol[counter++] == 1) ? "1, " : (sol[counter - 1] == 0) ? "2, " : "-, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                        tokens += sol[counter++] + ", ";
                    }
                    tokens += (sol[counter++] == 1) ? "T, {" : (sol[counter - 1] == 0) ? "!T, {" : "-, {";
                    List<Transition> transitions = game.getTransitions()[i - 1];
                    for (int j = 0; j < transitions.size(); ++j) {
                        Transition trans = transitions.get(j);
                        byte t = sol[counter++];
                        if (t == 1) {
                            tokens += trans.getId() + ",";
                        } else if (t == -1) {
                            tokens += "-" + trans.getId() + ",";
                        }
                    }
                    tokens += "})\n";
                } else {
                    tokens += "( - )\n";
                    int buf = counter;
                    counter += 1 + game.getTransitions()[i - 1].size();                 
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // jump over
                        ++counter;
                    }
//                    for (int j = buf; j < counter; j++) {
//                        System.out.print(sol[j]);
//                    }
                }
            }
            String envBin_ = "";
            zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin_ += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin_.equals(zeros)) {
                envBin_ = pls[0].get(envBin_);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                    envBin_ += ", " + sol[counter++];
                }

            } else {
                envBin_ = "-";
            }
            String tokens_ = "";
            for (int i = 1; i < game.getMaxTokenCount(); i++) {
                String id = "";
                zeros = "";
                for (int j = 0; j < getBinLength(game, i); j++) {
                    id += sol[counter++];
                    zeros += "0";
                }
                if (game.isConcurrencyPreserving() || !id.equals(zeros)) {
                    tokens_ += "(" + pls[i].get(id);
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY) { // add type for safety
                        tokens_ += (sol[counter++] == 1) ? ", 1, " : (sol[counter - 1] == 0) ? ", 2, " : ", -, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // add newly occupied for buchi
                        tokens_ += sol[counter++] + ", ";
                    }
                    tokens_ += (sol[counter++] == 1) ? "T, {" : (sol[counter - 1] == 0) ? "!T, {" : "-, {";
                    List<Transition> transitions = game.getTransitions()[i - 1];
                    for (int j = 0; j < transitions.size(); ++j) {
                        Transition trans = transitions.get(j);
                        byte t = sol[counter++];
                        if (t == 1) {
                            tokens_ += trans.getId() + ",";
                        } else if (t == -1) {
                            tokens_ += "-" + trans.getId() + ",";
                        }
                    }
                    tokens_ += "})\n";
                } else {
                    tokens_ += "( - )\n";
                    counter += 1 + game.getTransitions()[i - 1].size();
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.BUCHI) { // jump over
                        ++counter;
                    }
                }
            }
            out += envBin + ",\n" + tokens + " ->\n" + envBin_ + ",\n" + tokens_ + "\n";
//            System.out.println(out);
//            System.out.println(Arrays.toString(sol));
        }
        return out;
    }

    public static void saveStates2Pdf(String path, BDD bdds, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(states2Dot(bdds, solver));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", false);
        Runtime rt = Runtime.getRuntime();
        String exString = "dot -Tpdf " + path + ".dot -o " + path + ".pdf";
        Process p = rt.exec(exString);
        p.waitFor();
        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", false);
    }

    public static String states2Dot(BDD bdds, BDDSolver<? extends WinningCondition> solver) {
        final String mcutShape = "diamond";
        final String sysShape = "box";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph GraphGame {\n");

        // States
        sb.append("#states\n");
        BDD bdd = bdds.satOne(solver.getFirstBDDVariables(), false);
        int state_nb = 0;
        while (!bdd.isZero()) {
            // mcut?
            String shape = (solver.isEnvState(bdd)) ? mcutShape : sysShape;
            int penwidth = (false) ? 8 : 1;
            // Drawing
            sb.append(state_nb++).append("[shape=").append(shape);
            sb.append(", height=0.5, width=0.5, fixedsize=false,  penwidth=").append(penwidth);
//            sb.append(", xlabel=").append("\"").append(place.getId()).append("\"");
//            sb.append(", label=").append("\"").append(Tools.printDecodedDecisionSets(null, null, true)).append("\"");
            String value = getDecodedDecisionSets(bdd, solver);
            value = value.substring(0, value.indexOf("->"));
            sb.append(", label=\"").append(value).append("\"");
            sb.append(", xlabel=\"").append(state_nb).append("\"");
            sb.append("];\n");
            bdds = bdds.and(bdd.not());
            bdd = bdds.satOne(solver.getFirstBDDVariables(), false);
        }
        sb.append("overlap=false\n");
        sb.append("label=\"").append("states").append("\"\n");
        sb.append("fontsize=12\n\n");
        sb.append("}");
        return sb.toString();
    }

    public static String graph2Dot(BDDGraph g, BDDSolver<? extends WinningCondition> solver) {
        final String mcutShape = "diamond";
        final String sysShape = "box";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph GraphGame {\n");

        // States
        sb.append("#states\n");
        for (BDDState state : g.getStates()) {
            // mcut?
            String shape = (state.isMcut()) ? mcutShape : sysShape;
            int penwidth = (state.isSpecial()) ? 8 : 1;
            // Drawing
            sb.append(state.getId()).append("[shape=").append(shape);
            sb.append(", height=0.5, width=0.5, fixedsize=false,  penwidth=").append(penwidth);
//            sb.append(", xlabel=").append("\"").append(place.getId()).append("\"");
//            sb.append(", label=").append("\"").append(Tools.printDecodedDecisionSets(null, null, true)).append("\"");
            String value = getDecodedDecisionSets(state.getState(), solver);
            value = value.substring(0, value.indexOf("->"));
            sb.append(", label=\"").append(value).append("\"");
            sb.append(", xlabel=\"").append(state.getId()).append("\"");
            sb.append("];\n");
        }

        // Flows
        sb.append("\n#flows\n");
        for (Flow f : g.getFlows()) {
            sb.append(f.getSourceid()).append("->").append(f.getTargetid());
            Transition t = f.getTransition();
            if (t != null) {
                sb.append("[label=\"").append(t.getLabel()).append("\"]");
            }
            sb.append("\n");
        }
        sb.append("overlap=false\n");
        sb.append("label=\"").append(g.getName()).append("\"\n");
        sb.append("fontsize=12\n\n");

//        sb.append("rankdir=LR\n");
//        sb.append("node [shape=plaintext]\n");
//        sb.append("subgraph legend {\n");
//        sb.append("label = \"Legend\";\n");
//        sb.append("key [label=<<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" cellborder=\"0\">\n");
//        for (GraphState s : g.getStates()) {
//            sb.append("<tr><td align=\"right\" port=\"").append(s.getState().hashCode()).append("\">");
//            sb.append(s.getId()).append("</td></tr>\n");
//        }
//        sb.append("</table>>]\n");
//        sb.append("key2 [label=<<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" cellborder=\"0\">\n");
//        for (GraphState s : g.getStates()) {
//            String value = getDecodedDecisionSets(s.getState(), game);
//            value = value.substring(0, value.indexOf("->"));
//            sb.append("<tr><td align=\"right\" port=\"").append(s.getState().hashCode()).append("\">");
//            sb.append(value).append("</td></tr>\n");
//        }
//        sb.append("</table>>]\n");
//        for (GraphState s : g.getStates()) {
//            sb.append("key:").append(s.getState().hashCode()).append(":e -> key2:");
//            sb.append(s.getState().hashCode()).append(":w [color=gray]\n");
//        }
//        sb.append("\"").append(Tools.getPlace2BinIDMapping(game)).append("\"\n");
//        sb.append("\"").append(Tools.getTransition2IDMapping(game)).append("\"\n");
//        sb.append("}\n");
        sb.append("}");
        return sb.toString();
    }

    public static String graph2Tikz(BDDGraph g, BDDSolver<? extends WinningCondition> solver) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{tikzpicture}[\n");
        sb.append("sys/.style={\n");
        sb.append("rectangle,\n");
        sb.append("very thick,\n");
        sb.append("draw,\n");
        sb.append("align=center,\n");
        sb.append("minimum size=7mm,\n");
        sb.append("},\n");
        sb.append("env/.style={\n");
        sb.append("diamond,\n");
        sb.append("very thick,\n");
        sb.append("draw,\n");
        sb.append("align=center,\n");
        sb.append("minimum size=7mm,\n");
        sb.append("},\n");
        sb.append("]\n");

        // States
        Set<Integer> visited = new HashSet<>();
        BDDState state = g.getInitial();
        sb.append("% nodes\n");
        graphStates2Tikz(g, solver, state, null, null, visited, sb);

        // Flows
        sb.append("\n%flows\n");
        sb.append("\\path[->,thick,line width=1.5mm, >=stealth']\n");
        for (Flow f : g.getFlows()) {
            sb.append("   (").append(f.getSourceid()).append(") edge (").append(f.getTargetid()).append(")\n");
        }
        sb.append(";\n");
        sb.append("\\end{tikzpicture}\n");
        return sb.toString();
    }

    private static void graphStates2Tikz(BDDGraph g, BDDSolver<? extends WinningCondition> solver, BDDState state, Integer prev, Integer left, Set<Integer> visited, StringBuilder sb) {
        if (visited.contains(state.getId())) {
            return;
        }
        visited.add(state.getId());

        // mcut?
        String shape = (state.isMcut()) ? "env" : "sys";
        String positionTop = (prev != null) ? ", below=of " + prev : "";
        String positionLeft = (left != null) ? ", right=of " + left : "";
        String value = getDecodedDecisionSets(state.getState(), solver);
        value = value.replace("\n", "\\\\");
        value = value.replace("{", "\\{");
        value = value.replace("}", "\\}");
        value = value.substring(0, value.indexOf("->") - 3);
        sb.append("\\node[").append(shape).append(positionTop).append(positionLeft).append("] (").
                append(state.getId()).append(") {").append(value).append("};\n");
        Integer last = null;
        for (BDDState s : g.getPostset(state.getId())) {
            graphStates2Tikz(g, solver, s, state.getId(), last, visited, sb);
            last = s.getId();
        }
    }

    public static void saveGraph2DotAndPDF(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        saveGraph2Dot(path, g, solver);
        Runtime rt = Runtime.getRuntime();
        String exString = "dot -Tpdf " + path + ".dot -o " + path + ".pdf";
        Process p = rt.exec(exString);
        p.waitFor();
        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", false);
    }

    public static void saveGraph2PDF(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        saveGraph2DotAndPDF(path, g, solver);
        // Delete dot file
        new File(path + ".dot").delete();
    }

    public static void saveGraph2Dot(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(graph2Dot(g, solver));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", false);
    }
}
