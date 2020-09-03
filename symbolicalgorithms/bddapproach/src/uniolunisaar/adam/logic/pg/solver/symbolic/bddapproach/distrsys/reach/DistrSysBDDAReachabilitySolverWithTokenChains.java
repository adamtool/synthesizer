package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.reach;

//package uniolunisaar.adam.symbolic.bddapproach.solver;
//
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import net.sf.javabdd.BDD;
//import net.sf.javabdd.BDDDomain;
//import uniol.apt.adt.pn.Marking;
//import uniol.apt.adt.pn.Place;
//import uniol.apt.adt.pn.Transition;
//import uniol.apt.util.Pair;
//import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
//import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
//import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
//import uniolunisaar.adam.ds.winningconditions.Reachability;
//import uniolunisaar.adam.ds.exceptions.SolverDontFitPetriGameException;
//import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
//import uniolunisaar.adam.ds.petrigame.PetriGame;
//import uniolunisaar.adam.logic.tokenflow.TokenChainGenerator;
//import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
//import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
//import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
//import uniolunisaar.adam.symbolic.bddapproach.graph.BDDReachabilityGraphBuilder;
//import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameWithInitialEnvStrategyBuilder;
//import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
//import uniolunisaar.adam.tools.Logger;
//
///**
// * Never really finished. The idea with token chains never really worked out
// * well.
// *
// * todo: adapt text but this ones uses chains but has the problem that in this
// * way really every chain must be good, also when the system can decide to not
// * use them. Solves Petri games with a reachability objective by simply using an
// * attractor function. Don't need any type2 analysis or deadlock-avoiding
// * constraint.
// *
// * Problem what to do with the non-deterministic states? Already a fixed-point
// * combi of safety and reachability? It is not possible to totally omit them
// * because when a ndet state is a successor of an env state, then the env state
// * would errorously marked as good. Furthermore, if it's the only successor of a
// * sys state, then also this sys state would errorously marked as good.
// *
// * We solve it by marking every non-deterministic state as end-state and
// * deleting non-determinisic states from the set of good states to reach and
// * from the inital states.
// *
// * @author Manuel Gieseking
// */
//@Deprecated
//public class BDDAReachabilitySolverWithTokenChains extends BDDSolver<Reachability> {
//
//    private BDDDomain[] TOKENCHAIN_WON;
//    private BDDDomain[] TOKENCHAIN_ACTIVE;
//
//    /**
//     * Creates a new universal reachability solver for a given game.
//     *
//     * Already creates the needed variables and precalculates some BDDs. Creates
//     * and annotates the token trees.
//     *
//     * @param game - the game to solve.
//     * @throws SolverDontFitPetriGameException - Is thrown if the winning
//     * condition of the game is not a reachability condition.
//     */
//    BDDAReachabilitySolverWithTokenChains(PetriGame game, boolean skipTests, Reachability win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException {
//        super(game, skipTests, win, opts);
//        TokenChainGenerator.createAndAnnotateTokenChains(getGame());
//    }
//
//    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//    /**
//     * Creates the variables for this solver. This have to be overriden since
//     * the flags for the tokentrees have to be added.
//     *
//     * Codierung: p_i_0 - Environment token n - TokenCount m - number of
//     * transitions c_i - token trees
//     *
//     * |p_i_0|occ|p_i_1|top|t_1|...|t_m| ... |p_i_n|top|t_1|...|t_m|c_1|...|c_l]
//     */
//    @Override
//    void createVariables() {
//        int tokencount = getSolvingObject().getMaxTokenCountInt();
//        PLACES = new BDDDomain[2][tokencount];
//        TOP = new BDDDomain[2][tokencount - 1];
//        TRANSITIONS = new BDDDomain[2][tokencount - 1];
//        TOKENCHAIN_WON = new BDDDomain[2];
//        TOKENCHAIN_ACTIVE = new BDDDomain[2];
//        for (int i = 0; i < 2; ++i) {
//            // Env-place
//            int add = (!getSolvingObject().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0;
//            PLACES[i][0] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[0].size() + add);
//            //for any token
//            for (int j = 0; j < tokencount - 1; ++j) {
//                // Place
//                PLACES[i][j + 1] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[j + 1].size() + add);
//                // top
//                TOP[i][j] = getFactory().extDomain(2);
//                // transitions                
//                BigInteger maxTrans = BigInteger.valueOf(2);
//                int anzTransitions = getSolvingObject().getDevidedTransitions()[j].size();
////                if (anzTransitions > 0) { todo: problem when there are no transitions
//                maxTrans = maxTrans.pow(anzTransitions);
//                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
////                }
//            }
//            // one flag for each token chain (hell yeah this is expencive)
//            BigInteger nbChains = BigInteger.valueOf(2).pow(TokenChainGenerator.getTokenChains(getGame()).size());
//            TOKENCHAIN_WON[i] = getFactory().extDomain(nbChains);
//            TOKENCHAIN_ACTIVE[i] = getFactory().extDomain(nbChains);
//        }
//        setDCSLength(getFactory().varNum() / 2);
//    }
//// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//
//    @Override
//    String decodeDCS(byte[] dcs, int pos) {
//        // todo: should be adapted to TOKENTREE_WON and _ACT
//        return super.decodeDCS(dcs, pos);
//    }
//
//    /**
//     */
//    private BDD winningStates() {
//        // When token chain had been reached, then it also must had reached a reachable place
//        BDD reach = getOne();
//        for (int i = 0; i < TokenChainGenerator.getTokenChains(getGame()).size(); i++) {
//            reach.andWith(getFactory().ithVar(TOKENCHAIN_ACTIVE[0].vars()[i]).imp(getFactory().ithVar(TOKENCHAIN_WON[0].vars()[i])));
//        }
//        return reach;
//    }
//
//    @Override
//    BDD initial() {
//        BDD init = super.initial();
//        init.andWith(getBufferedNDet().not());
//        Marking initial = getGame().getInitialMarking();
//        List<Integer> alreadySetIds = new ArrayList<>();
//        List<Integer> alreadySetActIds = new ArrayList<>();
//        for (Place place : getGame().getPlaces()) {
//            if (initial.getToken(place).getValue() > 0) {
//                if (getSolvingObject().getWinCon().getPlaces2Reach().contains(place)) {
//                    init.andWith(setChainIDs(place, 0, true, alreadySetIds)); // set all which are winning and initial to 1 on all chains
//                }
//                init.andWith(setChanActiveIDs(place, 0, true, alreadySetActIds));
//            }
//        }
////        BDDTools.printDecodedDecisionSets(init, this, true);
////        BDDTools.printDecisionSets(init, true);
//        init.andWith(setAllRemainingIDsToZero(alreadySetIds, alreadySetActIds, 0));
////        BDDTools.printDecodedDecisionSets(init, this, true);
//        return init;
//    }
//
//    /**
//     * Sets all activated ids to the flag "toOne" and adds the ids to the list
//     *
//     * @param place
//     * @param pos
//     * @param toOne
//     * @return
//     */
//    private BDD setChanActiveIDs(Place place, int pos, boolean toOne, List<Integer> alreadySetIds) {
//        BDD res = getOne();
//        List<Integer> chainIds = BDDTools.getChainIDsContainingPlace(place, getGame());
//        for (Integer chanId : chainIds) {
//            if (toOne) {
//                res.andWith(getFactory().ithVar(TOKENCHAIN_ACTIVE[pos].vars()[chanId]));
//            } else {
//                res.andWith(getFactory().nithVar(TOKENCHAIN_ACTIVE[pos].vars()[chanId]));
//            }
//        }
//        alreadySetIds.addAll(chainIds);
//        return res;
//    }
//
//    /**
//     * Sets all token chain ids to the flag "toOne" and adds the ids to the list
//     *
//     * @param place
//     * @param pos
//     * @param toOne
//     * @return
//     */
//    private BDD setChainIDs(Place place, int pos, boolean toOne, List<Integer> alreadySetIds) {
//        BDD res = getOne();
//        List<Integer> chainIds = BDDTools.getChainIDsContainingPlace(place, getGame());
//        for (Integer chainId : chainIds) {
//            if (toOne) {
//                res.andWith(getFactory().ithVar(TOKENCHAIN_WON[pos].vars()[chainId]));
//            } else {
//                res.andWith(getFactory().nithVar(TOKENCHAIN_WON[pos].vars()[chainId]));
//            }
//        }
//        alreadySetIds.addAll(chainIds);
//        return res;
//    }
//
//    private BDD setAllRemainingIDsToZero(List<Integer> alreadySetIds, List<Integer> alreadySetActIds, int pos) {
//        BDD res = getOne();
//        for (int i = 0; i < TokenChainGenerator.getTokenChains(getGame()).size(); i++) {
//            if (!alreadySetIds.contains(i)) {
//                res.andWith(getFactory().nithVar(TOKENCHAIN_WON[pos].vars()[i]));
//            }
//            if (!alreadySetActIds.contains(i)) {
//                res.andWith(getFactory().nithVar(TOKENCHAIN_ACTIVE[pos].vars()[i]));
//            }
//        }
//        return res;
//    }
//
//    private BDD setSuitableRemainingSuccChainIDsToZero(List<Integer> alreadySetIds, List<Integer> alreadySetActIds) {
//        BDD res = getOne();
//        for (int i = 0; i < TokenChainGenerator.getTokenChains(getGame()).size(); i++) {
//            if (!alreadySetIds.contains(i)) {
//                // if pre not 1 => post 0
//                BDD pre = getFactory().ithVar(TOKENCHAIN_WON[0].vars()[i]).not();
//                BDD post = getFactory().nithVar(TOKENCHAIN_WON[1].vars()[i]);
//                res.andWith(pre.impWith(post));
////            res.andWith(post);
////            System.out.println("id" + treeId);
////            BDDTools.printDecisionSets(res, true);
//            }
//            if (!alreadySetActIds.contains(i)) {
//                // if pre not 1 => post 0
//                BDD pre = getFactory().ithVar(TOKENCHAIN_ACTIVE[0].vars()[i]).not();
//                BDD post = getFactory().nithVar(TOKENCHAIN_ACTIVE[1].vars()[i]);
//                res.andWith(pre.impWith(post));
//            }
//        }
//        return res;
//    }
//
//    private BDD keepOnesForChains() {
//        BDD ones = getOne();
//        for (int i = 0; i < TokenChainGenerator.getTokenChains(getGame()).size(); i++) {
//            ones.andWith(getFactory().ithVar(TOKENCHAIN_WON[0].vars()[i]).impWith(getFactory().ithVar(TOKENCHAIN_WON[1].vars()[i])));
//            ones.andWith(getFactory().ithVar(TOKENCHAIN_ACTIVE[0].vars()[i]).impWith(getFactory().ithVar(TOKENCHAIN_ACTIVE[1].vars()[i])));
//        }
//        return ones;
//    }
//
////    @Override
//    BDD envTransitionsCP() {
//        BDD env = getMcut();
//        BDD dis = getZero();
//        for (Transition t : getGame().getTransitions()) {
//            if (!getSolvingObject().getSysTransition().contains(t)) {
//                Set<Place> pre_sys = t.getPreset();
//                BDD all = firable(t, 0);
//                // Systempart
//                for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
//                    BDD pl = getZero();
//                    for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
//                        if (getSolvingObject().getGame().isEnvironment(place)) {
//                            throw new RuntimeException("Should not appear!"
//                                    + "An enviromental place could not appear here!");
//                            //                            continue;
//                        }
//                        BDD inner = getOne();
//                        inner.andWith(codePlace(place, 0, i));
//                        if (!pre_sys.contains(place)) {
//                            // pi=pi'
//                            inner.andWith(codePlace(place, 1, i));
//                            // ti=ti'
//                            inner.andWith(commitmentsEqual(i));
//                            // top'=0
//                            inner.andWith(TOP[1][i - 1].ithVar(0));
//                        } else {
//                            Place succ = getSuitableSuccessor(place, t);
//                            //pre_i=post_i'
//                            inner.andWith(codePlace(succ, 1, i));
//                            // top'=1
//                            inner.andWith(TOP[1][i - 1].ithVar(1));
//                            // all t_i'=0
//                            inner.andWith(nothingChosen(1, i));
//                        }
//                        pl.orWith(inner);
//                    }
//                    all.andWith(pl);
//                }
//                // Environmentpart                
//                // todo: one environment token case
//                List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
//                List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
//                if (!pre.isEmpty()) { // not really necessary since CP, but for no envtoken at all
//                    all.andWith(codePlace(pre.get(0), 0, 0));
//                } else {
//                    all.andWith(codePlace(0, 0, 0));
//                }
//                if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
//                    Place place = post.get(0);
//                    all.andWith(codePlace(place, 1, 0));
//                } else {
//                    all.andWith(codePlace(0, 1, 0));
//                }
//                // update the token chains               
//                List<Integer> alreadyVisitedChainIds = new ArrayList<>();
//                List<Integer> alreadyVisitedChainActIds = new ArrayList<>();
//                for (Place place : t.getPostset()) {
//                    // if it's a reachable place set it's token chain to 1
//                    if (getSolvingObject().getWinCon().getPlaces2Reach().contains(place)) {
//                        all.andWith(setChainIDs(place, 1, true, alreadyVisitedChainIds));
//                    }
//                    all.andWith(setChanActiveIDs(place, 1, true, alreadyVisitedChainActIds));
//                }
//                all.andWith(this.setSuitableRemainingSuccChainIDsToZero(alreadyVisitedChainIds, alreadyVisitedChainActIds));
////                all.andWith(this.setAllRemainingIDsToZero(alreadyVisitedTreeIds, 1));
//                dis.orWith(all);
//            }
//        }
//        env.andWith(dis);
//        // 1 for a chain in preset => 1 for the chain in postset
//        env.andWith(keepOnesForChains());
//        return env;
//    }
//
////    @Override
//    BDD envTransitionsNotCP() {
//        BDD mcut = getMcut();
//        BDD dis = getZero();
//        for (Transition t : getGame().getTransitions()) {
//            if (!getSolvingObject().getSysTransition().contains(t)) {
//                Set<Place> pre_sys = t.getPreset();
//                BDD all = firable(t, 0);
//
//                List<Integer> visitedToken = new ArrayList<>();
//
//                // set the dcs for the place of the postset 
//                for (Place post : t.getPostset()) {
//                    int token = getSolvingObject().getGame().getPartition(post);
//                    if (token != 0) { // jump over environment
//                        visitedToken.add(token);
//                        //pre_i=post_j'
//                        all.andWith(codePlace(post, 1, token));
//                        // top'=1
//                        all.andWith(TOP[1][token - 1].ithVar(1));
//                        // all t_i'=0
//                        all.andWith(nothingChosen(1, token));
//                    }
//                }
//
//                // set the dcs for the places in the preset
//                setPresetAndNeededZeros(pre_sys, visitedToken, all);
//
//                // --------------------------
//                // Positions in dcs not set with places of pre- or postset
//                setNotAffectedPositions(all, visitedToken);
//
//                // --------------------------
//                // Environmentpart
//                // todo: one environment token case
//                List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
//                List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
//                if (!pre.isEmpty()) {
//                    all.andWith(codePlace(pre.get(0), 0, 0));
//                } else {
//                    all.andWith(codePlace(0, 0, 0));
//                }
//                if (!post.isEmpty()) {
//                    Place place = post.get(0);
//                    all.andWith(codePlace(place, 1, 0));
//                } else {
//                    all.andWith(codePlace(0, 1, 0));
//                }
//                // update the token chains                
//                List<Integer> alreadyVisitedChainIds = new ArrayList<>();
//                List<Integer> alreadyVisitedChainActIds = new ArrayList<>();
//                for (Place place : t.getPostset()) {
//                    // if it's a reachable place set it's token chain to 1
//                    if (getSolvingObject().getWinCon().getPlaces2Reach().contains(place)) {
//                        all.andWith(setChainIDs(place, 1, true, alreadyVisitedChainIds));
//                    }
//                    all.andWith(setChanActiveIDs(place, 1, true, alreadyVisitedChainActIds));
//                }
//                all.andWith(this.setSuitableRemainingSuccChainIDsToZero(alreadyVisitedChainIds, alreadyVisitedChainActIds));
////                all.andWith(this.setAllRemainingIDsToZero(alreadyVisitedTreeIds, 1));
//                dis.orWith(all);
//            }
//        }
//
//        mcut.andWith(dis);
//        // 1 for a chain in preset => 1 for the chain in postset
//        mcut.andWith(keepOnesForChains());
//        return mcut;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
//    }
//
////    @Override
//    BDD sysTransitionsCP() {
//        // Only useable if it's not an mcut
//        BDD sys = getMcut().not();
//        // no successors for already reached states
////        sys1.andWith(reach(0).not());
//
//        // not all tops are zero
//        BDD top = getTop();
//
//        // normal part
//        BDD sysN = getZero();
//        for (Transition t : getSolvingObject().getSysTransition()) {
//            Set<Place> pre = t.getPreset();
//            BDD all = firable(t, 0);
//            for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
//                BDD pl = getZero();
//                for (Place place : getSolvingObject().getDevidedPlaces()[i]) {// these are all system places                    
//                    BDD inner = getOne();
//                    inner.andWith(codePlace(place, 0, i));
//                    if (!pre.contains(place)) {
//                        // pi=pi'
//                        inner.andWith(codePlace(place, 1, i));
//                        // ti=ti'
//                        inner.andWith(commitmentsEqual(i));
//                    } else {
//                        Place succ = getSuitableSuccessor(place, t);
//                        //pre_i=post_i'
//                        inner.andWith(codePlace(succ, 1, i));
//                    }
//                    pl.orWith(inner);
//                }
//                all.andWith(pl);
//                // top'=0
//                all.andWith(TOP[1][i - 1].ithVar(0));
//            }
//            // update the token chains                 
//            List<Integer> alreadyVisitedChainIds = new ArrayList<>();
//            List<Integer> alreadyVisitedChainActIds = new ArrayList<>();
//            for (Place place : t.getPostset()) {
//                // if it's a reachable place set it's tokenchains to 1
//                if (getSolvingObject().getWinCon().getPlaces2Reach().contains(place)) {
//                    all.andWith(setChainIDs(place, 1, true, alreadyVisitedChainIds));
//                }
//                all.andWith(setChanActiveIDs(place, 1, true, alreadyVisitedChainActIds));
//            }
//            all.andWith(this.setSuitableRemainingSuccChainIDsToZero(alreadyVisitedChainIds, alreadyVisitedChainActIds));
////            all.andWith(this.setAllRemainingIDsToZero(alreadyVisitedTreeIds, 1));
//            sysN.orWith(all);
//        }
//        sysN = (top.not()).impWith(sysN);
//
//        // top part
//        BDD sysT = getOne();
//        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); i++) {
////            // \not topi=>topi'=0
////            BDD topPart = bddfac.nithVar(offset + PL_CODE_LEN + 1);
////            topPart.impWith(bddfac.nithVar(DCS_LENGTH + offset + PL_CODE_LEN + 1));
////            sysT.andWith(topPart);
//            // topi'=0
//            sysT.andWith(TOP[1][i - 1].ithVar(0));
//            // type = type' todo: document anpassen
//            //sysT.andWith(bddfac.ithVar(offset + PL_CODE_LEN).biimp(bddfac.ithVar(DCS_LENGTH + offset + PL_CODE_LEN)));
//            // pi=pi'
//            sysT.andWith(placesEqual(i));
//            // \not topi=>ti=ti'
//            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i));
//            sysT.andWith(impl);
//        }
//        // keep token chain ids
//        sysT.andWith(TOKENCHAIN_WON[0].buildEquals(TOKENCHAIN_WON[1]));
//        sysT.andWith(TOKENCHAIN_ACTIVE[0].buildEquals(TOKENCHAIN_ACTIVE[1]));
//        sysT = top.impWith(sysT);
//
//        sys.andWith(sysN);
//        sys.andWith(sysT);
//        // p0=p0'        
//        sys = sys.andWith(placesEqual(0));
//
//        sys.andWith(getBufferedNDet().not());
//        // 1 for a chain in preset => 1 for the chain in postset
//        sys.andWith(keepOnesForChains());
//        return sys;
//    }
//
////    @Override
//    BDD sysTransitionsNotCP() {
//        // Only useable if it's not an mcut
//        BDD sys = getMcut().not();
//        // no successors for already reached states
////        sys1.andWith(reach(0).not());
//
//        // not all tops are zero
//        BDD top = getTop();
//
//        // normal part        
//        BDD sysN = getZero();
//        for (Transition t : getSolvingObject().getSysTransition()) {
//            Set<Place> pre_sys = t.getPreset();
//            BDD all = firable(t, 0);
//
//            List<Integer> visitedToken = new ArrayList<>();
//            // set the dcs for the place of the postset 
//            for (Place post : t.getPostset()) {
//                int token = getSolvingObject().getGame().getPartition(post);
//                if (token != 0) { // jump over environment, could not appear...
//                    visitedToken.add(token);
//                    //pre_i=post_j'
//                    all.andWith(codePlace(post, 1, token));
//                    // top'=0
//                    all.andWith(TOP[1][token - 1].ithVar(0));
//                }
//            }
//
//            // set the dcs for the places in the preset
//            setPresetAndNeededZeros(pre_sys, visitedToken, all);
//
//            // Positions in dcs not set with places of pre- or postset
//            setNotAffectedPositions(all, visitedToken);
//
//            // update the token chains        
//            List<Integer> alreadyVisitedChainIds = new ArrayList<>();
//            List<Integer> alreadyVisitedChainActIds = new ArrayList<>();
//            for (Place place : t.getPostset()) {
//                // if it's a reachable place set it's token chains to 1
//                if (getSolvingObject().getWinCon().getPlaces2Reach().contains(place)) {
//                    all.andWith(setChainIDs(place, 1, true, alreadyVisitedChainIds));
//                }
//                all.andWith(setChanActiveIDs(place, 1, true, alreadyVisitedChainActIds));
//            }
//            all.andWith(this.setSuitableRemainingSuccChainIDsToZero(alreadyVisitedChainIds, alreadyVisitedChainActIds));
////            all.andWith(this.setAllRemainingIDsToZero(alreadyVisitedTreeIds, 1));
//
//            sysN.orWith(all);
//        }
//
//        sysN = (top.not()).impWith(sysN);
//
//        // top part
//        BDD sysT = getOne();
//        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
////            // \not topi=>topi'=0
////            BDD topPart = bddfac.nithVar(offset + PL_CODE_LEN + 1);
////            topPart.impWith(bddfac.nithVar(DCS_LENGTH + offset + PL_CODE_LEN + 1));
////            sysT.andWith(topPart);
//            // topi'=0
//            sysT.andWith(TOP[1][i - 1].ithVar(0));
//            // type = type' todo: document anpassen
//            //sysT.andWith(bddfac.ithVar(offset + PL_CODE_LEN).biimp(bddfac.ithVar(DCS_LENGTH + offset + PL_CODE_LEN)));
//            // pi=pi'
//            sysT.andWith(placesEqual(i));
//            // \not topi=>ti=ti'
//            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i));
//            sysT.andWith(impl);
//        }
//        // keep token chains flags
//        sysT.andWith(TOKENCHAIN_WON[0].buildEquals(TOKENCHAIN_WON[1]));
//        sysT.andWith(TOKENCHAIN_ACTIVE[0].buildEquals(TOKENCHAIN_ACTIVE[1]));
//        sysT = top.impWith(sysT);
//
//        sys.andWith(sysN);
//        sys.andWith(sysT);
//        // p0=p0'        
//        sys = sys.andWith(placesEqual(0));
////TODO: mache den oldtype stuff
//        sys.andWith(getBufferedNDet().not());
//
//        // 1 for a chain in preset => 1 for the chain in postset
//        sys.andWith(keepOnesForChains());
//        return sys;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
//    }
//
//    /**
//     * Returns the winning decisionsets for the system players.
//     *
//     * In this case only an attractor to the reachable states.
//     *
//     * @return - A BDD containing all states from which a state with a reachable
//     * place is able the be reached against all behavior of the environment.
//     */
//    @Override
//    BDD calcWinningDCSs(Map<Integer, BDD> distance) {
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Logger.getInstance().addMessage("Calculating fixpoint ...");
//        BDD goodReach = winningStates().andWith(getBufferedNDet().not()).andWith(wellformed(0));
////        BDDTools.printDecodedDecisionSets(goodReach, this, true);
//        BDD fixedPoint = attractor(goodReach, false, distance);
//        //BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
//        Logger.getInstance().addMessage("... calculation of fixpoint done.");
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        return fixedPoint;
//    }
//
//    /**
//     * Returns the graph game for the reachability objective.
//     *
//     * Is the standard graph game, but before returning we are just marking the
//     * reachable states as special.
//     *
//     * @return - The graph game for the reachability objective.
//     */
//    @Override
//    public BDDGraph getGraphGame() {
//        BDDGraph graph = super.getGraphGame();
//        BDD reach = winningStates();
//        for (BDDState state : graph.getStates()) { // mark all special states
//            if (!graph.getInitial().equals(state)) {
//                if (!reach.and(state.getState()).isZero()) {
//                    state.setGood(true);
//                }
//                if (!getBufferedNDet().and(state.getState()).isZero()) {
//                    state.setBad(true);
//                }
//            }
//        }
//        return graph;
//    }
//
//    @Override
//    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException {
//        HashMap<Integer, BDD> distance = new HashMap<>();
//        BDD win = calcWinningDCSs(distance);
//        super.setBufferedWinDCSs(win);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
//        BDDGraph strat = BDDReachabilityGraphBuilder.getInstance().builtGraphStrategy(this, distance);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
//        for (BDDState state : strat.getStates()) { // mark all special states
//            if (!winningStates().and(state.getState()).isZero()) {
//                state.setGood(true);
//            }
//        }
//        return strat;
//    }
//
//    @Override
//    protected PetriGame calculateStrategy() throws NoStrategyExistentException {
//        BDDGraph gstrat = getGraphStrategy();
//        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        PetriGame pn = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        return pn;
//    }
//
//    @Override
//    public Pair<BDDGraph, PetriGame> getStrategies() throws NoStrategyExistentException {
//        BDDGraph gstrat = getGraphStrategy();
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        PetriGame pstrat = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        return new Pair<>(gstrat, pstrat);
//    }
//
//    /**
//     * Returns all variables of the predecessor or success as a BDD.
//     *
//     * This means the variables for: places + top-flags + commitment sets
//     *
//     * @param pos - 0 for the predecessor variables and 1 for the sucessor
//     * variables.
//     * @return - the variables of the predecessor or the sucessor of a
//     * transition.
//     */
//    @Override
//    BDD getVariables(int pos) {
//        // Existential variables
//        BDD variables = PLACES[pos][0].set();
//        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
//            variables.andWith(PLACES[pos][i + 1].set());
//            variables.andWith(TOP[pos][i].set());
//            variables.andWith(TRANSITIONS[pos][i].set());
//        }
//        variables.andWith(TOKENCHAIN_WON[pos].set());
//        variables.andWith(TOKENCHAIN_ACTIVE[pos].set());
//        return variables;
//    }
//
//    /**
//     * Create a BDD which is true, when the predesseccor und the successor of a
//     * transition are equal.
//     *
//     * Only adds the conditions for the nocc-flag and the loop flag.
//     *
//     * @return BDD with Pre <-> Succ
//     */
//    @Override
//    BDD preBimpSucc() {
//        BDD preBimpSucc = super.preBimpSucc();
//        for (int i = 0; i < TokenChainGenerator.getTokenChains(getGame()).size(); i++) {
//            preBimpSucc.andWith(TOKENCHAIN_WON[0].buildEquals(TOKENCHAIN_WON[1]));
//            preBimpSucc.andWith(TOKENCHAIN_ACTIVE[0].buildEquals(TOKENCHAIN_ACTIVE[1]));
//        }
//        return preBimpSucc;
//    }
//
//    /**
//     * tokentree values are allowed to change.
//     *
//     * @param t
//     * @param source
//     * @param target
//     * @return
//     */
//    @Override
//    public boolean hasFiredManually(Transition t, BDD source, BDD target) {
//        if (hasTop(source)) { // in a top state nothing could have been fired
//            return false;
//        }
//
//        if (!isFirable(t, source)) {
//            return false;
//        }
//        // here the preset of t is fitting the source (and the commitment set)! Do not need to test it extra
//
//        // Create bdd mantarget with the postset of t and the rest -1
//        // So with "and" we can test if the postset of t also fit to the target
//        // additionally create a copy of the target BDD with the places of the postset set to -1
//        Pair<List<Place>, List<Place>> post = getSolvingObject().getSplittedPostset(t);
//        // Environment place
//        // todo: one environment token case
//        BDD manTarget = getOne();
//        BDD restTarget = target.id();
//        if (!post.getFirst().isEmpty()) {
//            manTarget.andWith(codePlace(post.getFirst().get(0), 0, 0));
//            restTarget = restTarget.exist(getTokenVariables(0, 0));
//        }
//
//        // System places        
//        List<Place> postSys = post.getSecond();
//        BDD sysPlacesTarget = getOne();
//        for (Place p : postSys) {
//            int token = getSolvingObject().getGame().getPartition(p);
//            sysPlacesTarget.andWith(codePlace(p, 0, token));
//            restTarget = restTarget.exist(getTokenVariables(0, token));
//        }
//        manTarget.andWith(sysPlacesTarget);
//
//        if ((manTarget.and(target)).isZero()) {
//            return false;
//        }
//
//        // Create now a copy of the source with all positions set to -1 where preset is set
//        Pair<List<Place>, List<Place>> pre = getSolvingObject().getSplittedPreset(t);
//        // todo: one environment token case
//        BDD restSource = source.id();
//        if (!pre.getFirst().isEmpty()) {
//            restSource = restSource.exist(getTokenVariables(0, 0));
//        }
//
//        List<Place> preSys = pre.getSecond();
//        for (Place p : preSys) {
//            restSource = restSource.exist(getTokenVariables(0, getSolvingObject().getGame().getPartition(p)));
//        }
//
//        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
//        // The flags for the tokenchains, may have changed
//        restSource = restSource.exist(TOKENCHAIN_WON[0].set());
//        restTarget = restTarget.exist(TOKENCHAIN_WON[0].set());
//        restSource = restSource.exist(TOKENCHAIN_ACTIVE[0].set());
//        restTarget = restTarget.exist(TOKENCHAIN_ACTIVE[0].set());
//        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%
//
//        // now test if the places not in pre- or postset of t stayed equal between source and target
//        boolean ret = !(restTarget.and(restSource)).isZero();
////        if (ret == false) {
////            BDDTools.printDecodedDecisionSets(source, this, true);
////            System.out.println(t);
////            BDDTools.printDecodedDecisionSets(target, this, true);
////
////        }
//        return ret;
//    }
//}
