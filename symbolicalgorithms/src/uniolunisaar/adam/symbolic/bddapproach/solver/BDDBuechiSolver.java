package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.winningconditions.Buchi;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.logic.util.benchmark.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
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
 * TODO: Only works when there is at least one system place since this one is
 * used to create loop place.
 *
 * @author Manuel Gieseking
 */
public class BDDBuechiSolver extends BDDSolver<Buchi> {

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
     * @throws UnboundedPGException - Thrown if the given net is not bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    BDDBuechiSolver(PetriNet net, boolean skipTests, BDDSolverOptions opts) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(net, skipTests, new Buchi(), opts);
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
    void createVariables() {
        int tokencount = getGame().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        NOCC = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        LOOP = new BDDDomain[2];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (getGame().isConcurrencyPreserving()) ? 0 : 1;
            PLACES[i][0] = getFactory().extDomain(getGame().getPlaces()[0].size() + add);
            NOCC[i][0] = getFactory().extDomain(2);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getGame().getPlaces()[j + 1].size() + add);
                // newly occupied
                NOCC[i][j + 1] = getFactory().extDomain(2);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getGame().getTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
            LOOP[i] = getFactory().extDomain(2);
        }
        setDCSLength(getFactory().varNum() / 2 - 2);
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% START Special loop stuff %%%%%%%%%%%%%%%%%%%%
    /**
     *
     * Problem for concurrency preserving nets, since there zero is an id of a
     * place and so it is posible to have this loopState as a sen
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
        for (int i = start; i < start + getDcs_length(); i++) {
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
        Set<Transition> trans = getGame().getNet().getTransitions();
        for (Transition transition : trans) {
            term.andWith(firable(transition, pos).not());
        }
        return term;
    }

    private BDD loops() {
        BDD buchi = buchiStates();
        BDD term = endStates(0);
        // Terminating not buchi states add selfloop
        BDD termNBuchi = term.and(buchi.not()).andWith(wellformed(0));
        BDD loops = termNBuchi.andWith(preBimpSucc());
        // Terminating buchi states add transition to new looping state (all 
//        loops.orWith(term.and(buchi).and(loopState(1)));
//        // add loop
//        loops.orWith(loopState(0).andWith(loopState(1)));
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
        for (Place place : getWinningCondition().getBuchiPlaces()) {
            int token = (Integer) place.getExtension("token");
            // is a buchi place and is newly occupied, than it's a buchi state
            buchi.orWith(codePlace(place, 0, token).andWith(NOCC[0][token].ithVar(1)));
        }
        return buchi.andWith(wellformed(0));
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

//%%%%%%%%%%%%%%%% ADAPTED to NOCC  / Overriden CODE %%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    BDD wellformed(int pos) {
        BDD well = super.wellformed(pos);
        well.andWith(LOOP[0].ithVar(0));
        well.andWith(LOOP[1].ithVar(0));
        well.orWith(loopState(pos));
        return well;
    }

    @Override
    public BDD initial() {
        BDD init = super.initial();
        // all newly occupied flags to 0
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            init.andWith(NOCC[0][i].ithVar(0));
        }
        init.andWith(ndetStates(0).not());
        return init;
    }

    @Override
    BDD envTransitionsCP() {
        BDD env = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
            if (!getGame().getSysTransition().contains(t)) { // take only those transitions which have an env-place in preset
                Set<Place> pre_sys = t.getPreset();
                BDD all = firable(t, 0); // the transition should be enabled and choosen!
                // Systempart
                for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                    BDD pl = getZero();
                    for (Place place : getGame().getPlaces()[i]) {
                        if (place.hasExtension("env")) {
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
                // todo: one environment token case
                List<Place> pre = getGame().getSplittedPreset(t).getFirst();
                List<Place> post = getGame().getSplittedPostset(t).getFirst();
                if (!pre.isEmpty()) { // not really necessary since CP, but for no envtoken at all
                    all.andWith(codePlace(pre.get(0), 0, 0));
                } else {
                    all.andWith(codePlace(0, 0, 0));
                }
                if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
                    all.andWith(codePlace(post.get(0), 1, 0));
                    // occ' = 1 
                    all.andWith(NOCC[1][0].ithVar(1));
                } else {
                    all.andWith(codePlace(0, 1, 0));
                    all.andWith(NOCC[1][0].ithVar(0));
                }
                dis.orWith(all);
            }
        }
        env.andWith(LOOP[0].ithVar(0));
        env.andWith(LOOP[1].ithVar(0));
        env.andWith(dis).orWith(loops());
        return env;
    }

    @Override
    BDD notUsedToken(int pos, int token) {
        BDD zero = super.notUsedToken(pos, token);
        zero.andWith(NOCC[pos][token].ithVar(0));
        return zero;
    }

    @Override
    void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
        // Positions in dcs not set with places of pre- or postset
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
            if (visitedToken.contains(i)) { // jump over already visited token
                continue;
            }
            BDD pl = getZero();
            for (Place place : getGame().getPlaces()[i]) {
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
    BDD envTransitionsNotCP() {
        BDD mcut = getMcut();
        BDD dis = getZero();
        for (Transition t : getGame().getNet().getTransitions()) {
            if (!getGame().getSysTransition().contains(t)) {
                Set<Place> pre_sys = t.getPreset();
                BDD all = firable(t, 0);

                List<Integer> visitedToken = new ArrayList<>();

                // set the dcs for the place of the postset 
                for (Place post : t.getPostset()) {
                    int token = (Integer) post.getExtension("token");
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

                // --------------------------
                // Environmentpart
                // todo: one environment token case
                List<Place> pre = getGame().getSplittedPreset(t).getFirst();
                List<Place> post = getGame().getSplittedPostset(t).getFirst();
                if (!pre.isEmpty()) {
                    all.andWith(codePlace(pre.get(0), 0, 0));
                } else {
                    all.andWith(codePlace(0, 0, 0));
                    all.andWith(NOCC[0][0].ithVar(0));
                }
                if (!post.isEmpty()) {
                    all.andWith(codePlace(post.get(0), 1, 0));
                    all.andWith(NOCC[1][0].ithVar(1));
                } else {
                    all.andWith(codePlace(0, 1, 0));
                    all.andWith(NOCC[1][0].ithVar(0));
                }
                dis.orWith(all);
            }
        }
        mcut.andWith(LOOP[0].ithVar(0));
        mcut.andWith(LOOP[1].ithVar(0));

        mcut.andWith(dis).orWith(loops());
        return mcut;//.andWith(wellformedTransition());//.andWith(oldType2());//.andWith(wellformedTransition()));
    }

    @Override
    BDD sysTransitionsCP() {
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();

        // not all tops are zero
        BDD top = getTop();

        // normal part
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre = t.getPreset();
            BDD all = firable(t, 0);
            for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
                BDD pl = getZero();
                for (Place place : getGame().getPlaces()[i]) {// these are all system places                    
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
                all.andWith(pl);
                // top'=0
                all.andWith(TOP[1][i - 1].ithVar(0));
            }
            sysN.orWith(all);
        }
        // in not top case set the newly occupation flag of the env place to zero
        sysN = sysN.andWith(NOCC[1][0].ithVar(0));
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getGame().getMaxTokenCount(); i++) {
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
            // \not topi=>(ti=ti'\wedge nocc'=0)
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(NOCC[1][i].ithVar(0)));
            // topi=> nocc'=1            
            BDD impl1 = TOP[0][i - 1].ithVar(1).impWith(NOCC[1][i].ithVar(1));
            sysT.andWith(impl).andWith(impl1);
        }
        sysT = top.impWith(sysT);
        // in top case just copy the newly occupation flag of the env place
        sysT = sysT.andWith(NOCC[0][0].buildEquals(NOCC[1][0]));

        sys.andWith(sysN);
        sys.andWith(sysT);

        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));
        // p0=p0'        
        sys = sys.andWith(placesEqual(0)).orWith(loops());

        return sys.andWith(ndetStates(0).not());
    }

    @Override
    BDD sysTransitionsNotCP() {
        // Only useable if it's not an mcut
        BDD sys = getMcut().not();

        // not all tops are zero
        BDD top = getTop();

        // normal part        
        BDD sysN = getZero();
        for (Transition t : getGame().getSysTransition()) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);

            List<Integer> visitedToken = new ArrayList<>();

            // set the dcs for the place of the postset 
            for (Place post : t.getPostset()) {
                int token = (Integer) post.getExtension("token");
                if (token != 0) { // jump over environment, could not appear...
                    visitedToken.add(token);
                    //pre_i=post_j'
                    all.andWith(codePlace(post, 1, token));
                    // top'=0
                    all.andWith(TOP[1][token - 1].ithVar(0));
                    // nocc'=1
                    all.andWith(NOCC[1][token].ithVar(1));
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            sysN.orWith(all);
        }
        // in not top case set the newly occupation flag of the env place to zero
        sysN = sysN.andWith(NOCC[1][0].ithVar(0));

        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = getOne();
        for (int i = 1; i < getGame().getMaxTokenCount(); ++i) {
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
            // \not topi=>(ti=ti'\wedge nocc'=0)
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i).andWith(NOCC[1][i].ithVar(0)));
            // topi=> nocc'=1            
            BDD impl1 = TOP[0][i - 1].ithVar(1).impWith(NOCC[1][i].ithVar(1));
            sysT.andWith(impl).andWith(impl1);
        }
        sysT = top.impWith(sysT);
        // in top case just copy the newly occupation flag of the env place
        sysT = sysT.andWith(NOCC[0][0].buildEquals(NOCC[1][0]));

        sys.andWith(sysN);
        sys.andWith(sysT);
        sys.andWith(LOOP[0].ithVar(0));
        sys.andWith(LOOP[1].ithVar(0));
        // p0=p0'        
        sys = sys.andWith(placesEqual(0)).orWith(loops());;
        return sys.andWith(ndetStates(0).not());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * @return
     */
    private BDD buchi() {
        BDD S = wellformed();
        BDD W = getZero();
        BDD W_;
        BDD B = buchiStates();
        do {
            B = B.and(S);
            BDD R = attractor(B, false);
//            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%% attr reach ");
//            BDDTools.printDecodedDecisionSets(R, this, true);
            BDD Tr = S.and(R.not());
//            System.out.println("%%%%%%%%%%%%%%%% TR");
//            BDDTools.printDecodedDecisionSets(Tr, this, true);
            W_ = attractor(Tr, true);
//            System.out.println("%%%%%%%%%%%%%%%% atrrroktor TR");
//            BDDTools.printDecodedDecisionSets(W_, this, true);
            W = W.or(W_);
            S.andWith(W_.not());
        } while (!W_.isZero());
//        System.out.println("%%%%%%%%%%%% W");
//        BDDTools.printDecodedDecisionSets(W, this, true);
        W = W.not().and(wellformed());
//        System.out.println("%%%%%%%%%%%% return");
//        BDDTools.printDecodedDecisionSets(W, this, true);
        return W;
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     */
    @Override
    BDD calcWinningDCSs() {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = buchi().andWith(ndetStates(0).not()).andWith(wellformed(0)); // not really necesarry, since those don't have any successor.
//        BDDTools.printDecodedDecisionSets(fixedPoint, this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    @Override
    public BDDGraph getGraphGame() {
        BDDGraph graph = super.getGraphGame();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !buchiStates().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return graph;
    }

    @Override
    public BDDGraph getGraphStrategy() throws NoStrategyExistentException {
        BDDGraph strat = super.getGraphStrategy();
        try {
            BDDTools.saveStates2Pdf("./states", buchiStates(), this);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BDDBuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BDDBuechiSolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("loops");
        BDDTools.printDecisionSets(loops(), true);
        BDDTools.printDecodedDecisionSets(loops(), this, true);
        for (BDDState state : strat.getStates()) { // mark all special states
            if (!strat.getInitial().equals(state) && !buchiStates().and(state.getState()).isZero()) {
                state.setSpecial(true);
            }
        }
        return strat;
    }
// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations %%%%%%%%%%%%%%%%
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment + nocc-flag
     * sets.
     *
     * So only adds the variables for the newly occupied places.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor
     * variables.
     * @return - the variables of the predecessor or the sucessor of a
     * transition.
     */
    @Override
    BDD getVariables(int pos) {
        // Existential variables
        BDD variables = super.getVariables(pos);
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            variables.andWith(NOCC[pos][i].set());
        }
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
    BDD getTokenVariables(int pos, int token) {
        BDD variables = super.getTokenVariables(pos, token);
        variables.andWith(NOCC[pos][token].set());
        return variables;
    }

    /**
     * Create a BDD which is true, when the predesseccor und the successor of a
     * transition are equal.
     *
     * Only adds the conditions for the nocc-flag.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    BDD preBimpSucc() {
        BDD preBimpSucc = super.preBimpSucc();
        for (int i = 0; i < getGame().getMaxTokenCount(); ++i) {
            preBimpSucc.andWith(NOCC[0][i].buildEquals(NOCC[1][i]));
        }
        return preBimpSucc;
    }
}
