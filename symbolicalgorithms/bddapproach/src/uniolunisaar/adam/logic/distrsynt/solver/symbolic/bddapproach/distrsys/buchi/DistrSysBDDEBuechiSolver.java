package uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.buchi;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.logic.distrsynt.builder.graph.symbolic.bddapproach.BDDBuchiGraphAndGStrategyBuilder;
import uniolunisaar.adam.logic.distrsynt.builder.petrigame.symbolic.bddapproach.BDDPetriGameWithInitialEnvStrategyBuilder;
import uniolunisaar.adam.logic.distrsynt.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * Problem what to do with the non-deterministic states? Already a fixed-point
 * combi of safety and reachability? It is not possible to totally omit them
 * because when a ndet state is a successor of an env state, then the env state
 * would errorously marked as good. Furthermore, if it's the only successor of a
 * sys state, then also this sys state would errorously marked as good. Solve it
 * the same way how it is done for reachability. It's also the same that no
 * deadlock and not type2 analysis necessary.
 *
 * Problem 2: Terminating of the game is not allowed. We have to add selfloops
 * at every state which isn't a buchi state and if a buchi state doesn't have
 * any successor than a succesor with a selfloop has to be added.
 *
 *
 * @author Manuel Gieseking
 */
public class DistrSysBDDEBuechiSolver extends DistrSysBDDSolver<Buchi> {

    // Domains for predecessor and successor for each token
    private BDDDomain[][] NOCC;
    private BDDDomain[] LOOP;

    /**
     * Creates a new Buchi solver for a given game.
     *
     * @param net - the Petri game to solve.
     * @param skipTests - should the tests for safe and bounded and other
     * preconditions be skipped?
     * @param opts - the options for the solver.
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    DistrSysBDDEBuechiSolver(DistrSysBDDSolvingObject<Buchi> obj, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, opts);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Creates the variables for this solver. This have to be overriden since
     * the flag if the place has been newly occupied in this state has to be
     * coded additionally.
     *
     * Codierung: p_i_0 - Environment Token n - TokenCount occ=1 states newly
     * occupied in this state.
     *
     * |p_i_0|occ|p_i_1|occ|top|t_1|...|t_m| ... |p_i_n|occ|top|t_1|...|t_m|
     */
    @Override
    protected void createVariables() {
        int tokencount = getSolvingObject().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        NOCC = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        LOOP = new BDDDomain[2];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (!getSolvingObject().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0;
            PLACES[i][0] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[0].size() + add);
            NOCC[i][0] = getFactory().extDomain(2);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[j + 1].size() + add);
                // newly occupied
                NOCC[i][j + 1] = getFactory().extDomain(2);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getSolvingObject().getDevidedTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
            LOOP[i] = getFactory().extDomain(2);
        }
        setDCSLength(getFactory().varNum() / 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    protected String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        if (BDDTools.isLoopByBin(dcs, LOOP[pos])) {
            sb.append("LOOP");
        } else {
            // Env place
            sb.append("(");
            String id = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][0], getSolvingObject().getDevidedPlaces()[0], getSolvingObject().isConcurrencyPreserving());
            sb.append(id);
            if (!id.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getNewlyOccupiedFlagByBin(dcs, NOCC[pos][0]));
            }
            sb.append(")").append("\n");
            for (int j = 0; j < getSolvingObject().getMaxTokenCount() - 1; j++) {
                sb.append("(");
                String sid = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][j + 1], getSolvingObject().getDevidedPlaces()[j + 1], getSolvingObject().isConcurrencyPreserving());
                sb.append(sid);
                if (!sid.equals("-")) {
                    sb.append(", ");
                    sb.append(BDDTools.getNewlyOccupiedFlagByBin(dcs, NOCC[pos][j + 1]));
                    sb.append(", ");
                    sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                    sb.append(", ");
                    sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], getSolvingObject().getDevidedTransitions()[j]));
                }
                sb.append(")").append("\n");
            }
        }
        return sb.toString();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% START Special loop stuff %%%%%%%%%%%%%%%%%%%%
    /**
     *
     * Problem for concurrency preserving nets, since there zero is an id of a
     * place and so it is possible to have this loopState as a sen
     *
     * Since they are only id's it's also not possible to code e.g. a sys place
     * at the env position.
     *
     * So added a new LOOP variable which should be 0 at any case a apart from
     * the loop state
     *
     * @param pos
     * @return
     */
    private BDD loopState(int pos) {
//        Place sys = this.getGame().getPlaces()[0].iterator().next();
//        BDD nearlyZero = this.getZero().exist(PLACES[pos][0].domain());
//        BDDTools.printDecisionSets(nearlyZero, true);
//        return nearlyZero.andWith(codePlace(sys, pos, 0));
        BDD loop = getOne();
        int start = (pos == 0) ? 0 : getDcs_length();
        for (int i = start; i < start + getDcs_length() - 1; i++) {
            loop.andWith(getFactory().nithVar(i));
        }
        loop.andWith(LOOP[pos].ithVar(1));
        return loop;
    }

    /**
     * @param pos
     * @return
     */
    private BDD endStates(int pos) {
        BDD term = getOne();
        Set<Transition> trans = getGame().getTransitions();
        for (Transition transition : trans) {
            term.andWith(firable(transition, pos).not());
        }
        term.andWith(getNotTop());
        return term;
    }

    private BDD loops() {
        BDD buchi = buchiStates();
        BDD term = endStates(0);
        // Terminating not buchi states add selfloop
        BDD termNBuchi = term.and(buchi.not().andWith(wellformed(0)));
        BDD loops = termNBuchi.andWith(preBimpSucc());
        // Terminating buchi states add transition to new looping state (all 
        loops.orWith(term.and(buchi).and(loopState(1)));
//        // add loop
        loops.orWith(loopState(0).andWith(loopState(1)));
//        System.out.println("end states");
//System.out.println("term abnd buchi");
//        BDDTools.printDecodedDecisionSets(loops.andWith(wellformed(0)), this, true);
//        System.out.println("tern abd");
//        BDDTools.printDecisionSets(term.and(buchi), true);
//        System.out.println("END");
        return loops.andWith(wellformed(0));
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END Special loop stuff %%%%%%%%%%%%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Returns all states as a BDD which have a buchi place coded and this place
     * is newly occupied in this state.
     *
     * @return - All states with a newly occupied buchi place
     */
    private BDD buchiStates() {
        BDD buchi = getZero();
        for (Place place : getSolvingObject().getWinCon().getBuchiPlaces()) {
            int token = getSolvingObject().getGame().getPartition(place);
            // is a buchi place and is newly occupied, than it's a buchi state
            buchi.orWith(codePlace(place, 0, token).andWith(NOCC[0][token].ithVar(1)));
        }
        return buchi.andWith(wellformed(0));
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

//%%%%%%%%%%%%%%%% ADAPTED to NOCC  / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    protected BDD wellformed(int pos) {
        BDD well = super.wellformed(pos);
        well.andWith(LOOP[pos].ithVar(0));
        well.orWith(loopState(pos));
        return well;
    }

//    @Override
//    BDD calcDCSs() {
//        BDD all = super.calcDCSs();
//        all.andWith(LOOP[0].ithVar(0));
//        all.andWith(LOOP[1].ithVar(0));
//        all.orWith(loopState(0));
//        return all;
//    }
    @Override
    public BDD initial() {
        BDD init = super.initial();
        // one version, then in the most cases there is one unfolding of the place
//        // all newly occupied flags to 0
//        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
//            init.andWith(NOCC[0][i].ithVar(0));
//        }
        // all newly ocupied flags for the initial token are 1 the others zero
        Marking m = getGame().getInitialMarking();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            boolean occ = false;
            for (Place p : getSolvingObject().getDevidedPlaces()[i]) {
                if (m.getToken(p).getValue() > 0) {
                    occ = true;
                    break;
                }
            }
            init.andWith(NOCC[0][i].ithVar(occ ? 1 : 0));
        }
        init.andWith(LOOP[0].ithVar(0));
        init.andWith(getBufferedNDet().not());
        return init;
    }

    @Override
    protected BDD notUsedToken(int pos, int token) {
        BDD zero = super.notUsedToken(pos, token);
        zero.andWith(NOCC[pos][token].ithVar(0));
        return zero;
    }

    @Override
    protected void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
        // Positions in dcs not set with places of pre- or postset
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            if (visitedToken.contains(i)) { // jump over already visited token
                continue;
            }
            BDD pl = getZero();
            for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
                // only sys places which are not within the preset of t
                // are possible to occure here
                BDD inner = getOne();
                inner.andWith(codePlace(place, 0, i));
                // pi=pi'
                inner.andWith(codePlace(place, 1, i));
                // ti=ti'
                inner.andWith(commitmentsEqual(i));
                // top'=0
                inner.andWith(TOP[1][i - 1].ithVar(0));
                // nocc'=0
                inner.andWith(NOCC[1][i].ithVar(0));
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    @Override
    protected BDD envPart(Transition t) {
        BDD env = super.envPart(t);
        // todo: one environment token case
        List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
        List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
        if (pre.isEmpty()) { // not really necessary since CP, but for no envtoken at all                    
            env.andWith(NOCC[0][0].ithVar(0));
        }
        if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
            // occ' = 1 
            env.andWith(NOCC[1][0].ithVar(1));
        } else {
            env.andWith(NOCC[1][0].ithVar(0));
        }
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        env.andWith(LOOP[0].ithVar(0));
        env.andWith(LOOP[1].ithVar(0));
        return env;
    }

    @Override
    protected BDD envTransitionCP(Transition t) {
        BDD env = loops();
        if (!getSolvingObject().getSysTransition().contains(t)) { // take only those transitions which have an env-place in preset
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0); // the transition should be enabled and choosen!
            // Systempart
            for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
                    if (getSolvingObject().getGame().isEnvironment(place)) {
                        throw new RuntimeException("Should not appear!"
                                + "An enviromental place could not appear here!");
                        //                            continue;
                    }
                    BDD inner = getOne();
                    inner.andWith(codePlace(place, 0, i));
                    if (!pre_sys.contains(place)) { // the place wasn't in the preset of the transition, thus nothing can be changed here
                        // pi=pi'
                        inner.andWith(codePlace(place, 1, i));
                        // ti=ti'
                        inner.andWith(commitmentsEqual(i));
                        // top'=0
                        inner.andWith(TOP[1][i - 1].ithVar(0));
                        // nocc'=0
                        inner.andWith(NOCC[1][i].ithVar(0));
                    } else { // the place was in the preset of the transition, thus find a suitable sucessor and code it
                        //pre_i=post_i'
                        inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                        // top'=1
                        inner.andWith(TOP[1][i - 1].ithVar(1));
                        // all t_i'=0
                        inner.andWith(nothingChosen(1, i));
                        // occ' = 0 (newly occupied only in the case where we solve the top)
                        inner.andWith(NOCC[1][i].ithVar(0));
                    }
                    pl.orWith(inner);
                }
                all.andWith(pl);
            }
            // Environmentpart                
            all.andWith(envPart(t));
            env.orWith(all);
        }
        return env;
    }

    @Override
    protected BDD envTransitionNotCP(Transition t) {
        BDD env = loops();
        if (!getSolvingObject().getSysTransition().contains(t)) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = getSolvingObject().getGame().getPartition(post);
                if (token != 0) { // jump over environment
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=1
                    all.andWith(TOP[1][token - 1].ithVar(1));
                    // all t_i'=0
                    all.andWith(nothingChosen(1, token));
                    // occ' = 0 (newly occupied only in the case where we solve the top)
                    all.andWith(NOCC[1][token].ithVar(0));
                }
            }
            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);
            // --------------------------
            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            // Environmentpart                
            all.andWith(envPart(t));
            env.orWith(all);
        }
        return env;
    }

    @Override
    protected BDD sysTopPart() {
        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); i++) {
//            // \not topi=>topi'=0
//            BDD topPart = bddfac.nithVar(offset + PL_CODE_LEN + 1);
//            topPart.impWith(bddfac.nithVar(DCS_LENGTH + offset + PL_CODE_LEN + 1));
//            sysT.andWith(topPart);
            // topi'=0
            sysT.andWith(TOP[1][i - 1].ithVar(0));
            // type = type' todo: document anpassen
            //sysT.andWith(bddfac.ithVar(offset + PL_CODE_LEN).biimp(bddfac.ithVar(DCS_LENGTH + offset + PL_CODE_LEN)));
            // pi=pi'
            sysT.andWith(placesEqual(i));
            // \not topi=>(ti=ti'\wedge nocc'=0) set to 0 enough see ncp case
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(NOCC[1][i].ithVar(0)));
            // topi=> nocc'=1            
            BDD impl1 = TOP[0][i - 1].ithVar(1).impWith(NOCC[1][i].ithVar(1));
            sysT.andWith(impl).andWith(impl1);
        }
        // in top case just copy the newly occupation flag of the env place
        sysT.andWith(NOCC[0][0].buildEquals(NOCC[1][0]));
        return sysT;
    }

    @Override
    protected BDD sysTransitionCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();
        // not all tops are zero
        BDD top = getTop();
        // normal part
        Set<Place> pre = t.getPreset();
        BDD sysN = firable(t, 0);
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD pl = getZero();
            for (Place place : getSolvingObject().getDevidedPlaces()[i]) {// these are all system places                    
                BDD inner = getOne();
                inner.andWith(codePlace(place, 0, i));
                if (!pre.contains(place)) {
                    // pi=pi'
                    inner.andWith(codePlace(place, 1, i));
                    // ti=ti'
                    inner.andWith(commitmentsEqual(i));
                    // nocc'=0
                    inner.andWith(NOCC[1][i].ithVar(0));
                } else {
                    //pre_i=post_i'
                    inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                    // nocc'=1
                    inner.andWith(NOCC[1][i].ithVar(1));
                }
                pl.orWith(inner);
            }
            sysN.andWith(pl);
            // top'=0
            sysN.andWith(TOP[1][i - 1].ithVar(0));
        }
        // in not top case set the newly occupation flag of the env place to zero
        sysN.andWith(NOCC[1][0].ithVar(0));
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());

        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);

        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));
        // p0=p0'        
        sys.andWith(placesEqual(0));
        sys.orWith(loops());

        return sys.andWith(getBufferedNDet().not());
    }

    @Override
    protected BDD sysTransitionNotCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();
        // not all tops are zero
        BDD top = getTop();

        // normal part      
        Set<Place> pre_sys = t.getPreset();
        BDD sysN = firable(t, 0);
        List<Integer> visitedToken = new ArrayList<>();
        // set the dcs for the place of the postset 
        for (Place post : t.getPostset()) {
            int token = getSolvingObject().getGame().getPartition(post);
            if (token != 0) { // jump over environment, could not appear...
                visitedToken.add(token);
                //pre_i=post_j'
                sysN.andWith(codePlace(post, 1, token));
                // top'=0
                sysN.andWith(TOP[1][token - 1].ithVar(0));
                // nocc'=1
                sysN.andWith(NOCC[1][token].ithVar(1));
            }
        }
        // set the dcs for the places in the preset
        setPresetAndNeededZeros(pre_sys, visitedToken, sysN);
        // Positions in dcs not set with places of pre- or postset
        setNotAffectedPositions(sysN, visitedToken);
        // in not top case set the newly occupation flag of the env place to zero
        sysN.andWith(NOCC[1][0].ithVar(0));
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));
        // p0=p0'        
        sys.andWith(placesEqual(0));
        sys.orWith(loops());
        return sys.andWith(getBufferedNDet().not());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * with strategy building from zimmermanns lecture script
     *
     * @return
     */
    private BDD buchi(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        return buchi(buchiStates(), distance);
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = buchi(distance);//.andWith(ndetStates(0).not()).andWith(wellformed(0)); // not really necesarry, since those don't have any successor.
//        BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        try {
//            BDDTools.saveStates2Pdf("./states", buchi(), this);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(BDDBuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(BDDBuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return fixedPoint;
    }

    @Override
    protected BDD calcBadDCSs() {
        return getBufferedNDet();
    }

    @Override
    protected BDD calcSpecialDCSs() {
        return buchiStates();
    }

    @Override
    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        HashMap<Integer, BDD> distance = new HashMap<>();
        BDD win = calcWinningDCSs(distance);
        super.setBufferedWinDCSs(win);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph strat = BDDBuchiGraphAndGStrategyBuilder.getInstance().builtGraphStrategy(this, distance);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        return strat;
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pn = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    @Override
    public Pair<BDDGraph, PetriGameWithTransits> getStrategies() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pstrat = BDDPetriGameWithInitialEnvStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations %%%%%%%%%%%%%%%%
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment + nocc-flag
     * sets.
     *
     * So only adds the variables for the newly occupied places and the LOOP.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor
     * variables.
     * @return - the variables of the predecessor or the sucessor of a
     * transition.
     */
    @Override
    protected BDD getVariables(int pos) {
        // Existential variables
        BDD variables = super.getVariables(pos);
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            variables.andWith(NOCC[pos][i].set());
        }
        variables.andWith(LOOP[pos].set());
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * successor as BDD.
     *
     * This means the variables for the coding of the place, the top-flag, the
     * newly occupied-flag and the belonging commitment set for a system token.
     *
     * So only add the variables for the nocc-flag.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    @Override
    protected BDD getTokenVariables(int pos, int token) {
        BDD variables = super.getTokenVariables(pos, token);
        variables.andWith(NOCC[pos][token].set());
        return variables;
    }

    /**
     * Create a BDD which is true, when the predesseccor und the successor of a
     * transition are equal.
     *
     * Only adds the conditions for the nocc-flag and the loop flag.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    protected BDD preBimpSucc() {
        BDD preBimpSucc = super.preBimpSucc();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            preBimpSucc.andWith(NOCC[0][i].buildEquals(NOCC[1][i]));
        }
        preBimpSucc.andWith(LOOP[0].buildEquals(LOOP[1]));
        return preBimpSucc;
    }

    /**
     * Only in not looping states there could have a transition fired.
     *
     * @param t
     * @param source
     * @param target
     * @return
     */
    @Override
    @Deprecated
    public boolean hasFiredManually(Transition t, BDD source, BDD target) {
        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        if (!source.and(LOOP[0].ithVar(1)).isZero()) {
            return false;
        }
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }

        if (!isFirable(t, source)) {
            return false;
        }
        // here the preset of t is fitting the source (and the commitment set)! Do not need to test it extra

        // Create bdd mantarget with the postset of t and the rest -1
        // So with "and" we can test if the postset of t also fit to the target
        // additionally create a copy of the target BDD with the places of the postset set to -1
        Pair<List<Place>, List<Place>> post = getSolvingObject().getSplittedPostset(t);
        List<Integer> usedToken = new ArrayList<>();
        // Environment place
        // todo: one environment token case
        BDD manTarget = getOne();
        BDD restTarget = target.id();
        if (!post.getFirst().isEmpty()) {
            manTarget.andWith(codePlace(post.getFirst().get(0), 0, 0));
            manTarget.andWith(NOCC[0][0].ithVar(1));
            restTarget = restTarget.exist(getTokenVariables(0, 0));
            usedToken.add(0);
        }

        // System places        
        List<Place> postSys = post.getSecond();
        BDD sysPlacesTarget = getOne();
        for (Place p : postSys) {
            int token = getSolvingObject().getGame().getPartition(p);
            sysPlacesTarget.andWith(codePlace(p, 0, token));
            if (post.getFirst().isEmpty()) { // single system transition
                sysPlacesTarget.andWith(TOP[0][token - 1].ithVar(0));
                sysPlacesTarget.andWith(NOCC[0][token].ithVar(1));
            } else {
                sysPlacesTarget.andWith(TOP[0][token - 1].ithVar(1));
                sysPlacesTarget.andWith(NOCC[0][token].ithVar(0));
            }
            restTarget = restTarget.exist(getTokenVariables(0, token));
            usedToken.add(token);
        }
        manTarget.andWith(sysPlacesTarget);

        if ((manTarget.and(target)).isZero()) {
            return false;
        }

        // Create now a copy of the source with all positions set to -1 where preset is set
        Pair<List<Place>, List<Place>> pre = getSolvingObject().getSplittedPreset(t);
        // todo: one environment token case
        BDD restSource = source.id();
        if (!pre.getFirst().isEmpty()) {
            restSource = restSource.exist(getTokenVariables(0, 0));
        }

        List<Place> preSys = pre.getSecond();
        for (Place p : preSys) {
            restSource = restSource.exist(getTokenVariables(0, getSolvingObject().getGame().getPartition(p)));
        }

        // %%%%%%%%%% change to super method %%%%%%%%%%%%%%%%%%%%%%%
        // The flag indication that the place is newly occupied, may have changed
        for (int i = 0; i < getSolvingObject().getMaxTokenCountInt(); i++) {
            if (!usedToken.contains(i)) {
                restSource = restSource.exist(NOCC[0][i].set());
                restTarget.andWith(NOCC[0][i].ithVar(0));
            }
        }
        // %%%%%%%%%% end change to super method %%%%%%%%%%%%%%%%%%%%%%%

        // now test if the places not in pre- or postset of t stayed equal between source and target
        return !(restTarget.and(restSource)).isZero();
    }

}
