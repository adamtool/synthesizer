package uniolunisaar.adam.symbolic.bddapproach.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGame;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.petrigame.TokenChain;
import uniolunisaar.adam.ds.petrigame.TokenTree;
import uniolunisaar.adam.ds.util.AdamExtensions;
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
        String binID = Integer.toBinaryString(AdamExtensions.getID(p));
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
            Logger.getInstance().addMessage("" + l.size(), !force);
            for (byte[] sol : l) {
                // required for buddy library
                if (sol == null) {
                    continue;
                }
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < sol.length / 2; i++) {
                    sb.append(sol[i]);
                }
                sb.append(" -> ");
                for (int i = sol.length / 2; i < sol.length; i++) {
                    sb.append(sol[i]);
                }
                sb.append("]");
                Logger.getInstance().addMessage(sb.toString(), !force);
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
    @Deprecated
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
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // add newly occupied for buchi
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY) { // add type for safety
                        tokens += (sol[counter++] == 1) ? "1, " : (sol[counter - 1] == 0) ? "2, " : "-, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // add newly occupied for buchi
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // jump over
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
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // add newly occupied for buchi
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY) { // add type for safety
                        tokens_ += (sol[counter++] == 1) ? ", 1, " : (sol[counter - 1] == 0) ? ", 2, " : ", -, ";
                    } else if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // add newly occupied for buchi
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY || solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI) { // jump over
                        ++counter;
                    }
                }
            }
            out += envBin + ",\n" + tokens + " ->\n" + envBin_ + ",\n" + tokens_ + "\n";
        }
        return out;
    }

    public static String getPlaceIDByBin(byte[] dcs, BDDDomain bddDomain, Set<Place> places, boolean cp) {
        int[] ids = bddDomain.vars();
        String id = "";
        String zero = "";
        for (int i = 0; i < ids.length; i++) {
            id += dcs[ids[i]];
            zero += "0";
        }
        if (!cp && id.equals(zero)) {
            return "-";
        }
        for (Place p : places) {
            if (id.equals(place2BinID(p, ids.length))) {
                return p.getId();
            }
        }
        return id;
    }

    public static String getTopFlagByBin(byte[] dcs, BDDDomain bddDomain) {
        byte topFlag = dcs[bddDomain.vars()[0]];
        return (topFlag == 1) ? "T" : (topFlag == 0) ? "!T" : "-";
    }

    public static String getTransitionsByBin(byte[] dcs, BDDDomain bddDomain, List<Transition> transitions) {
        int[] ids = bddDomain.vars();
        StringBuilder sb = new StringBuilder("{");
        boolean added = false;
        for (int j = 0; j < transitions.size(); ++j) {
            Transition trans = transitions.get(j);
            byte t = dcs[ids[j]];
            if (t == 1) {
                if (added) {
                    sb.append(", ");
                } else {
                    added = true;
                }
                sb.append(trans.getId());
            } else if (t == -1) {
                if (added) {
                    sb.append(", ");
                } else {
                    added = true;
                }
                sb.append("-").append(trans.getId());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static String getGoodChainFlagByBin(byte[] dcs, BDDDomain bddDomain) {
        byte gcFlag = dcs[bddDomain.vars()[0]];
        return (gcFlag == 1) ? "g" : (gcFlag == 0) ? "!g" : "-";
    }

    public static String getNewlyOccupiedFlagByBin(byte[] dcs, BDDDomain bddDomain) {
        byte gcFlag = dcs[bddDomain.vars()[0]];
        return (gcFlag == 1) ? "*" : (gcFlag == 0) ? "!*" : "-";
    }

    public static String getTypeFlagByBin(byte[] dcs, BDDDomain bddDomain) {
        byte typeFlag = dcs[bddDomain.vars()[0]];
        return (typeFlag == 1) ? "1" : (typeFlag == 0) ? "2" : "-";
    }

    public static boolean notUsedByBin(byte[] dcs, int dcs_length, int pos) {
        int add = (pos == 0) ? 0 : dcs_length;
        for (int j = add; j < dcs_length + add; j++) {
            if (dcs[j] != -1) {
                return false;
            }
        }
        return true;
    }

    public static String getOverallBadByBin(byte[] dcs, BDDDomain bddDomain) {
        byte oBadFlag = dcs[bddDomain.vars()[0]];
        return (oBadFlag == 1) ? "BAD" : (oBadFlag == 0) ? "ok" : "-";
    }

    public static boolean isLoopByBin(byte[] dcs, BDDDomain bddDomain) {
        return dcs[bddDomain.vars()[0]] == 1;
    }

    public static String getDecodedDecisionSets(BDD dcs, BDDSolver<? extends WinningCondition> solver) {
        String out = "";
        @SuppressWarnings("unchecked")
        List<byte[]> l = dcs.allsat();
        for (byte[] sol : l) {
            // required for buddy library
            if (sol == null) {
                continue;
            }
            out += solver.decode(sol) + "\n\n";
        }
        if (out.equals("")) {
            throw new RuntimeException("State has no solution." + dcs.isZero());
        }
        return out;
    }

    /**
     * Do not fit when domains aren't created in the exact expected order. This
     * could be fixed by using PLACES[0][i - 1].vars() to get the indizes of the
     * domain variables. TODO: do it...
     *
     * @param dcs
     * @param solver
     * @return
     */
    public static String getDecodedDecisionSetsWithoutDomains(BDD dcs, BDDSolver<? extends WinningCondition> solver) {
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
            String pre = "";
            int counter = 0;
            // Enviroment
            String envBin = "";
            String zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin.equals(zeros)) {
                envBin = pls[0].get(envBin);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI // add newly occupied for buchi
                        || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // add newly occupied for buchi
                        ) {
                    envBin += (sol[counter++] == 1) ? ", * " : (sol[counter - 1] == 0) ? ", !* " : ", - ";
                }
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                        || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY// add good token chain 
                        ) {
                    envBin += (sol[counter++] == 1) ? ", g " : (sol[counter - 1] == 0) ? ", !g " : ", - ";
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI // add newly occupied for buchi
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // add newly occupied for buchi
                            ) {
                        tokens += (sol[counter++] == 1) ? " *, " : (sol[counter - 1] == 0) ? " !*, " : " -, ";
                    }
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY// add good token chain
                            ) {
                        tokens += (sol[counter++] == 1) ? " g, " : (sol[counter - 1] == 0) ? " !g, " : " -, ";
                    }
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY
                            || // add type for safety
                            solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) { // add type for for buchi
                        tokens += (sol[counter++] == 1) ? "1, " : (sol[counter - 1] == 0) ? "2, " : "-, ";
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
//                    int buf = counter;
                    counter += 1 + game.getTransitions()[i - 1].size();
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY) { // jump over type2 or newly occ or good chain flag
                        ++counter;
                        if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // jump over good token chain for buchi and type
                                ) {
                            ++counter;
                            ++counter;
                        }
                    }
//                    for (int j = buf; j < counter; j++) {
//                        System.out.print(sol[j]);
//                    }
                }
            }
            pre += envBin + ",\n" + tokens;

//            // Token chains
//            if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY) {
//                int size = AdamExtensions.getTokenChains(game.getNet()).size();
//                for (int i = 0; i < size; i++) {
//                    pre += sol[counter++] + ":" + sol[counter + (size - 1)] + ", ";
//                }
//                counter += size;
//                pre += "\n";
//            }
            // add overall bad
            if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY) {
                pre += sol[counter++] + "\n";
            }

            // Loop state
            if ((solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI
                    || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) && sol[counter++] == 1) {
                pre = "LOOP\n";
            }

            // add overall bad for Abuchi
            if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) {
                if (!pre.equals("LOOP\n")) {
                    pre += sol[counter++] + "\n";
                } else {
                    ++counter;
                }
            }
            // %%%%%%%%%%%%%%%%%%%%%%%%%%% POST %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            String post = "";
            // Successor
            String envBin_ = "";
            zeros = "";
            for (int i = 0; i < getBinLength(game, 0); i++) {
                envBin_ += sol[counter++];
                zeros += "0";
            }
            if (game.isConcurrencyPreserving() || !envBin_.equals(zeros)) {
                envBin_ = pls[0].get(envBin_);
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI // add newly occupied for buchi
                        || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // add newly occupied for buchi
                        ) {
                    envBin_ += (sol[counter++] == 1) ? ",* " : (sol[counter - 1] == 0) ? ",!* " : ",- ";
                }
                if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                        || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY// add good token chain 
                        ) {
                    envBin_ += (sol[counter++] == 1) ? ",g " : (sol[counter - 1] == 0) ? ",!g, " : ",- ";
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
                    tokens_ += "(" + pls[i].get(id) + ",";
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI // add newly occupied for buchi
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // add newly occupied for buchi
                            ) {
                        tokens_ += (sol[counter++] == 1) ? " *, " : (sol[counter - 1] == 0) ? " !*, " : " -, ";

                    }
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY // add good token chain
                            ) {
                        tokens_ += (sol[counter++] == 1) ? " g, " : (sol[counter - 1] == 0) ? " !g, " : " -, ";
                    }
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY // add type for safety
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) { // add type for buchi
                        tokens_ += (sol[counter++] == 1) ? " 1, " : (sol[counter - 1] == 0) ? " 2, " : " -, ";
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
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_SAFETY
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI
                            || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY) { // jump over type2 or newly occ or good chain flag
                        ++counter;
                    }
                    if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI // jump over good token chain for buchi and type
                            ) {
                        ++counter;
                        ++counter;
                    }
                }
            }
            post += envBin_ + ",\n" + tokens_;

//            // Token chains
//            if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY) {
//                int size = AdamExtensions.getTokenChains(game.getNet()).size();
//                for (int i = 0; i < size; i++) {
//                    post += sol[counter++] + ":" + sol[counter + (size - 1)] + ", ";
//                }
//                counter += size;
//                post += "\n";
//            }           
            // add overall bad
            if (solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_REACHABILITY
                    || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) {
                post += sol[counter++] + "\n";
            }

            // Loop state
            if ((solver.getWinningCondition().getObjective() == WinningCondition.Objective.E_BUCHI
                    || solver.getWinningCondition().getObjective() == WinningCondition.Objective.A_BUCHI) && sol[counter++] == 1) {
                post = "LOOP";
            }
            out += pre + " ->\n" + post + "\n";
//            System.out.println(out);
//            System.out.println(Arrays.toString(sol));
        }
        if (out.equals("")) {
            throw new RuntimeException("State has no solution." + dcs.isZero());
        }
        return out;
    }

    public static String states2Dot(BDD bdds, BDDSolver<? extends WinningCondition> solver) {
//        final String mcutShape = "diamond";
//        final String sysShape = "box";
        final String mcutColor = "white";
        final String sysColor = "gray";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph GraphGame {\n");

        // States
        sb.append("#states\n");
        BDD bdd = bdds.satOne(solver.getFirstBDDVariables(), false);
        int state_nb = 0;
        while (!bdd.isZero()) {
            // mcut?
//            String shape = (solver.isEnvState(bdd)) ? mcutShape : sysShape;
            String color = (solver.isEnvState(bdd)) ? mcutColor : sysColor;
            int penwidth = (false) ? 8 : 1;
            // Drawing
            sb.append(state_nb++).append("[shape=box, color=").append(color);
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
//        final String mcutShape = "diamond";
//        final String sysShape = "box";
        final String mcutColor = "white";
        final String sysColor = "gray";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph GraphGame {\n");

        // States
        sb.append("#states\n");
        for (BDDState state : g.getStates()) {
            // mcut?
//            String shape = (state.isMcut()) ? mcutShape : sysShape;
            String color = (solver.isEnvState(state.getState())) ? mcutColor : sysColor;
            int penwidth = (state.isBad()) ? 8 : 1;
            String shape = (state.isGood()) ? "doubleoctagon" : "box";
            // Drawing
            sb.append(state.getId()).append("[shape=").append(shape).append(", style=filled, fillcolor=").append(color);
            sb.append(", height=0.5, width=0.5, fixedsize=false,  penwidth=").append(penwidth);
//            sb.append(", xlabel=").append("\"").append(place.getId()).append("\"");
//            sb.append(", label=").append("\"").append(Tools.printDecodedDecisionSets(null, null, true)).append("\"");

            String value = getDecodedDecisionSets(state.getState(), solver);
//            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//            System.out.println(value);
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

    public static void saveGraph2Dot(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(graph2Dot(g, solver));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
    }

    public static void saveGraph2DotAndPDF(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        saveGraph2Dot(path, g, solver);
        Runtime rt = Runtime.getRuntime();
        String exString = "dot -Tpdf " + path + ".dot -o " + path + ".pdf";
        Process p = rt.exec(exString);
        p.waitFor();
        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
    }

    public static void saveGraph2PDF(String path, BDDGraph g, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        String bufferpath = path + System.currentTimeMillis();
        saveGraph2DotAndPDF(bufferpath, g, solver);
        // Delete dot file
        new File(bufferpath + ".dot").delete();
        Logger.getInstance().addMessage("Deleted: " + bufferpath + ".dot", true);
        // move to original name
        Files.move(new File(bufferpath + ".pdf").toPath(), new File(path + ".pdf").toPath(), REPLACE_EXISTING);
        Logger.getInstance().addMessage("Moved: " + bufferpath + ".pdf --> " + path + ".pdf", true);
    }

    public static void saveStates2Pdf(String path, BDD bdds, BDDSolver<? extends WinningCondition> solver) throws IOException, InterruptedException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(states2Dot(bdds, solver));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
        Runtime rt = Runtime.getRuntime();
        String exString = "dot -Tpdf " + path + ".dot -o " + path + ".pdf";
        Process p = rt.exec(exString);
        p.waitFor();
        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
    }

    public static List<Integer> getTreeIDsContainingPlace(Place place) {
        List<TokenTree> tokentrees = AdamExtensions.getTokenTrees(place.getGraph());
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < tokentrees.size(); i++) {
            TokenTree tree = tokentrees.get(i);
            if (tree.contains(place)) {
                ids.add(i);
            }
        }
        return ids;
    }

    public static List<Integer> getChainIDsContainingPlace(Place place) {
        List<TokenChain> tokenchains = AdamExtensions.getTokenChains(place.getGraph());
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < tokenchains.size(); i++) {
            TokenChain chain = tokenchains.get(i);
            if (chain.contains(place)) {
                ids.add(i);
            }
        }
        return ids;
    }

}
