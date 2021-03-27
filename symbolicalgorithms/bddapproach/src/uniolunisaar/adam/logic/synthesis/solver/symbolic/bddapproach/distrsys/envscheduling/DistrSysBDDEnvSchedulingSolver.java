package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.envscheduling;

import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.objectives.global.GlobalSafety;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 * TODO: if this approach works merge this class with:
 *
 * uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver
 *
 * Currently does not support any type-2 case.
 *
 * @author Manuel Gieseking
 */
//public class DistrSysBDDEnvSchedulingSolver<W extends Condition<W>> extends BDDSolver<W, DistrSysBDDSolvingObject<W>, BDDSolverOptions> {
// just needed for the function baddcs could think of leave it generic and add a new class (especially when type 2)
public class DistrSysBDDEnvSchedulingSolver extends BDDSolver<GlobalSafety, DistrSysBDDSolvingObject<GlobalSafety>, BDDSolverOptions> {

    // Domains for predecessor and successor for each token   
    protected BDDDomain[][] TRANSITIONS;
    protected BDDDomain[][] TOP;

    //Buffered BDDs (todo:necessary?)  
    private BDD ndet = null;

    protected DistrSysBDDEnvSchedulingSolver(DistrSysBDDSolvingObject<GlobalSafety> solverObject, BDDSolverOptions options) {
        super(solverObject, options);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Codierung: p_i_0 - Environment Token n - TokenCount
     * |p_i_0|p_i_1|top|t_1|...|t_m| ... |p_i_n|top|t_1|...|t_m|
     */
    @Override
    protected void createVariables() {
        int tokencount = getSolvingObject().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (!getSolvingObject().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0; // add for no env place at all a dummy space
            PLACES[i][0] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[0].size() + add);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[j + 1].size() + add);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getSolvingObject().getDevidedTransitions()[j].size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    protected String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        // Env place
        sb.append("(");
        String id = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][0], getSolvingObject().getDevidedPlaces()[0], getSolvingObject().isConcurrencyPreserving());
        sb.append(id);
        sb.append(")").append("\n");
        for (int j = 0; j < getSolvingObject().getMaxTokenCount() - 1; j++) {
            sb.append("(");
            String sid = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][j + 1], getSolvingObject().getDevidedPlaces()[j + 1], getSolvingObject().isConcurrencyPreserving());
            sb.append(sid);
            if (!sid.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], getSolvingObject().getDevidedTransitions()[j]));
            }
            sb.append(")").append("\n");
        }
        return sb.toString();
    }

    /**
     * Calculates the wellformed BDDs for the predecessor or successor.
     *
     * Only successors of the place are allowed in its commitment sets.
     * Additional reduction technique of Valentin Spreckel's MA.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return a wellformed BDD
     */
    @Override
    protected BDD wellformed(int pos) {
        // only the places belonging to the token (or zero) are allowed in the positions
        BDD well = super.wellformed(pos);

        // only transitions in the postset of pi are allowed in the commitments
        BDD com = getOne();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            for (Transition t : getSolvingObject().getDevidedTransitions()[i - 1]) {
                BDD place = getZero();
                for (Place p : t.getPreset()) {
                    if (!getSolvingObject().getGame().isEnvironment(p) && i == getSolvingObject().getGame().getPartition(p)) {
                        place.orWith(codePlace(p, pos, i));
                    }
                }
                int id = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t);
                com.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(place));
                // further reduction of the commitment sets by idea of valentin spreckels:
                // when transition only has one place in preset no other transitions are 
                // allowed in the commitment set
                if (t.getPreset().size() == 1) {
                    // all others have to be zero
                    BDD trans = getOne();
                    for (Transition t1 : getSolvingObject().getDevidedTransitions()[i - 1]) {
                        if (!t1.equals(t)) {
                            int id1 = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t1);
                            trans.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id1]));
                        }
                    }
                    com.andWith(getFactory().nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(trans));
                }
            }
        }
        well.andWith(com);

        //well.andWith(mixedTypes(pos)); //nicht so oder so not?
        return well;
    }

    @Override
    protected BDD notUsedToken(int pos, int token) {
        BDD zero = codePlace(0, pos, token);
        if (token == 0) { //env case
            return zero;
        }
        zero.andWith(TOP[pos][token - 1].ithVar(0));
        zero.andWith(nothingChosen(pos, token));
        return zero;
    }

    /**
     * Calculates the BDD belonging to the initial marking with all tops set to
     * false.
     *
     * @return BDD for the initial marking.
     */
    @Override
    protected BDD initial() {
        BDD init = super.initial();
        init.andWith(getNotTop());
        return init;//.and(getWellformed());
    }

    /**
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * This is for the original definition with checking ndet within the
     * strategy.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return BDD with all nondeterministic situations.
     */
    BDD ndetStates(int pos) {
        BDD nondet = getZero();
        Set<Transition> trans = getGame().getTransitions();
        for (Transition t1 : trans) {
            for (Transition t2 : trans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<Place> pre1 = t1.getPreset();
                    Set<Place> pre2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(pre1);
                    intersect.retainAll(pre2);
                    boolean shared = false;
                    for (Place place : intersect) {
                        if (!getSolvingObject().getGame().isEnvironment(place)) {
                            shared = true;
                        }
                    }
                    if (shared) {
                        BDD first = firable(t1, pos).andWith(firable(t2, pos));
                        nondet = nondet.orWith(first);
                    }
                }
            }
        }
        return nondet;//.andWith(wellformed());
    }

    /**
     * Calculates a BDD representing all decision sets where the system decided
     * not to choose any enabled transition, but there exists at least one.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing the deadlocks of the Petri game.
     */
    private BDD deadSysDCS(int pos) {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, pos).not());
            buf.orWith(enabled(t, pos));
        }
        dead.andWith(buf);
        return dead.andWith(getTop().not());//.andWith(wellformed());
    }

    private BDD baddcs(int pos) {
        BDD bad = getZero();
        for (List<Place> marking : getSolvingObject().getWinCon().getMarkings()) {
            BDD m = getOne();
            for (Place place : marking) {
                m.andWith(codePlace(place, pos, getSolvingObject().getGame().getPartition(place)));
            }
            bad.orWith(m);
        }
        return bad;
    }

    /**
     * Calculates the BDD where all tops are zero.
     *
     * Should be expensive, since it compares variables over wide ranges. This
     * should be expensive for BDDs.
     *
     * @return a BDD with all tops set to zero.
     */
    private BDD nTop() {
        BDD nTop = getFactory().one();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD top = TOP[0][i - 1].ithVar(0);
            // todo: really necessary?
            if (!getSolvingObject().isConcurrencyPreserving()) {
                BDD place = codePlace(0, 0, i);
                nTop.andWith(place.not().impWith(top));
            } else {
                nTop.andWith(top);
            }
        }
        return nTop;//.andWith(getWellformed());
    }

    protected BDD chosen(Transition t, int pos) {
        BDD c = getOne();
        for (Place p : t.getPreset()) {
            if (!getSolvingObject().getGame().isEnvironment(p)) {
                // Sys places
                int token = getSolvingObject().getGame().getPartition(p);
                int id = getSolvingObject().getDevidedTransitions()[token - 1].indexOf(t);
                BDD pl = codePlace(p, pos, token);
                pl.impWith(getFactory().ithVar(TRANSITIONS[pos][token - 1].vars()[id]));
                c.andWith(pl);
            }
        }
        return c;//.andWith(getWellformed());
    }

    protected BDD firable(Transition t, int pos) {
        return enabled(t, pos).andWith(chosen(t, pos));
    }

    @Override
    protected boolean isFirable(Transition t, BDD source) {
        return !source.and(firable(t, 0)).isZero();
    }

    @Override
    public boolean hasFired(Transition t, BDD source, BDD target) {
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }
//        return super.hasFired(t, source, target);

        boolean cp = getSolvingObject().isConcurrencyPreserving();
        BDD trans = source.and(shiftFirst2Second(target));
        BDD out;
        // is env transition
        if (cp) {
            out = player1EdgesCP(t).andWith(trans);
        } else {
            out = player1EdgesNotCP(t).andWith(trans);
        }
        return !out.isZero();
    }

    private BDD mcut(int pos) {
        BDD mc = getOne();
        // all tops should be zero
        mc.andWith(getNotTop());
        for (Transition t : getSolvingObject().getSysTransition()) {
            mc.andWith(firable(t, pos).not());
        }
        return mc.andWith(wellformed());
    }

    protected void setPresetAndNeededZeros(Set<Place> pre_sys, List<Integer> visitedToken, BDD all) {
        List<Integer> postTokens = new ArrayList<>(visitedToken);
        // set the dcs for the places in the preset
        for (Place pre : pre_sys) {
            if (!getSolvingObject().getGame().isEnvironment(pre)) { // jump over environment
                int token = getSolvingObject().getGame().getPartition(pre);
                visitedToken.add(token);
                all.andWith(codePlace(pre, 0, token));
                // if this position in the dcs is free after this position
                if (!postTokens.contains(token)) {
                    all.andWith(notUsedToken(1, token));
                } else {
                    postTokens.remove((Integer) token);
                }
            }
        }

        // those where there is a post but no preset for this token
        for (Integer token : postTokens) {
            all.andWith(notUsedToken(0, token));
        }
    }

    /**
     * Sets all successors-bits to the predecessor-bits for the token which had
     * not been affected by this transition.
     *
     * @param all
     * @param visitedToken
     */
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
                pl.orWith(inner);
            }
            BDD zero = notUsedToken(0, i).and(notUsedToken(1, i));
            all.andWith(pl.orWith(zero));
        }
    }

    /**
     * These are the edges of Player 0. In this case these are only the top
     * resolving edges.
     *
     * @return
     */
    @Override
    protected BDD calcSystemTransitions() {
        // the env place has to stay the same
        BDD player0 = placesEqual(0);
        // Systempart
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            // also all other places have to stay the same
            player0.andWith(placesEqual(i));
            // !top -> (c = c' AND !top')
            player0.andWith(TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i)));
            // top' = 0
            player0.andWith(TOP[1][i - 1].ithVar(0));
        }
        return player0;
    }

    protected BDD envPart(Transition t) {
        BDD all = getOne();
        // todo: one environment token case
        List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
        List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
        if (!pre.isEmpty()) {
            all.andWith(codePlace(pre.get(0), 0, 0));
            if (!post.isEmpty()) {
                all.andWith(codePlace(post.get(0), 1, 0));
            } else {
                all.andWith(codePlace(0, 1, 0));
            }
        } else {
            if (!post.isEmpty()) {
                all.andWith(codePlace(0, 0, 0));
                all.andWith(codePlace(post.get(0), 1, 0));
            } else {
                all.andWith(placesEqual(0));
            }
        }
        return all;
    }

    /**
     * The edges belonging to Player 1 (the environment player)
     *
     * @param t
     * @return
     */
    protected BDD player1EdgesCP(Transition t) {
        Set<Place> pre_sys = t.getPreset();

        // handle the environment token
        BDD edges = envPart(t);
        // the transition must be fireable
        edges.andWith(firable(t, 0));
        // here no top is allowed to be true
        edges.andWith(nTop());
        // handle the system tokens
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            // set all possible places on this position and when the place is 
            // in the preset set a corresponding postset place, otherwise 
            // keep the values in the successor.
            BDD pl = getZero();
            for (Place place : getSolvingObject().getDevidedPlaces()[i]) {
                if (getSolvingObject().getGame().isEnvironment(place)) {
                    throw new RuntimeException("Should not appear!"
                            + "An enviromental place could not appear here!");
                    //                            continue;
                }
                BDD inner = codePlace(place, 0, i);
                if (!pre_sys.contains(place)) {
                    // pi=pi'
                    inner.andWith(codePlace(place, 1, i));
                    // ti=ti'
                    inner.andWith(commitmentsEqual(i));
                    // top'=0
                    inner.andWith(TOP[1][i - 1].ithVar(0));
                } else {
                    //pre_i=post_i'
                    inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                    // top'=1
                    inner.andWith(TOP[1][i - 1].ithVar(1));
                    // all t_i'=0
                    inner.andWith(nothingChosen(1, i));
                }
                pl.orWith(inner);
            }
            edges.andWith(pl);
        }
        return edges;
    }

    /**
     * The edges belonging to Player 1 (the environment player)
     *
     * @param t
     * @return
     */
    protected BDD player1EdgesNotCP(Transition t) {
        Set<Place> pre_sys = t.getPreset();

        // handle the environment token            
        BDD edges = envPart(t);
        // the transition must be fireable
        edges.andWith(firable(t, 0));
        // here no top is allowed to be true
        edges.andWith(nTop());

        List<Integer> visitedToken = new ArrayList<>();

        // set the dcs for the place of the postset 
        for (Place post : t.getPostset()) {
            int token = getSolvingObject().getGame().getPartition(post);
            if (token != 0) { // jump over environment
                visitedToken.add(token);
                //pre_i=post_j'
                edges.andWith(codePlace(post, 1, token));
                // top'=1
                edges.andWith(TOP[1][token - 1].ithVar(1));
                // all t_i'=0
                edges.andWith(nothingChosen(1, token));
            }
        }

        // set the dcs for the places in the preset
        setPresetAndNeededZeros(pre_sys, visitedToken, edges);

        // Positions in dcs not set with places of pre- or postset
        setNotAffectedPositions(edges, visitedToken);

        return edges;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Some helping calculations
    /**
     * Returns all variables of the predecessor or success as a BDD.
     *
     * This means the variables for: places + top-flags + commitment sets
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor
     * variables.
     * @return - the variables of the predecessor or the sucessor of a
     * transition.
     */
    @Override
    protected BDD getVariables(int pos) {
        BDD variables = PLACES[pos][0].set();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
            variables.andWith(PLACES[pos][i + 1].set());
            variables.andWith(TOP[pos][i].set());
            variables.andWith(TRANSITIONS[pos][i].set());
        }
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * successor as BDD.
     *
     * This means the variables for the coding of the place, the top-flag, and
     * the belonging commitment set for a system token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    @Override
    protected BDD getTokenVariables(int pos, int token) {
        if (token == 0) {
            return PLACES[pos][token].set();
        } else {
            BDD variables = (PLACES[pos][token].set());
            variables.andWith(TOP[pos][token - 1].set());
            variables.andWith(TRANSITIONS[pos][token - 1].set());
            return variables;
        }
    }

    /**
     * Creates a BDD which is true, when the predesseccor und the successor of a
     * transition are equivalent.
     *
     * @return BDD with Pre <-> Succ
     */
    @Override
    protected BDD preBimpSucc() {
        BDD preBimpSucc = PLACES[0][0].buildEquals(PLACES[1][0]);
        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
            preBimpSucc.andWith(PLACES[0][i + 1].buildEquals(PLACES[1][i + 1]));
            preBimpSucc.andWith(TOP[0][i].buildEquals(TOP[1][i]));
            preBimpSucc.andWith(TRANSITIONS[0][i].buildEquals(TRANSITIONS[1][i]));
        }
        return preBimpSucc;
    }

    /**
     * Returns a BDD where the commitment sets of the predecessor and the
     * successor of the given token are equal.
     *
     * @param token - the token where the commitment sets should be equal.
     * @return - A BDD where the commitment sets are equal at the given token.
     */
    protected BDD commitmentsEqual(int token) {
        return TRANSITIONS[0][token - 1].buildEquals(TRANSITIONS[1][token - 1]);
    }

    /**
     * Returns a BDD where the commitment set of the predecessor or the
     * successor is the empty set for a given token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @param token - the token where the commitment set should be empty.
     * @return - A BDD with an empty commitment set at the given position for
     * the given token.
     */
    protected BDD nothingChosen(int pos, int token) {
        return TRANSITIONS[pos][token - 1].ithVar(0);
    }

    /**
     * These are the edges belonging to Player 1. This means all but the top
     * resolving edges.
     *
     * @param t
     * @return
     */
    @Override
    protected BDD calcEnvironmentTransition(Transition t) {
        boolean cp = getSolvingObject().isConcurrencyPreserving();
        return cp ? player1EdgesCP(t) : player1EdgesNotCP(t);
    }

    /**
     * These are the edges belonging to Player 0. In this approach these are
     * only the top resolving edges. This is not really meaning for this
     * approach to have it for a specific transition. Thus, here we return just
     * zero.
     *
     * @param t
     * @return
     */
    @Override
    protected BDD calcSystemTransition(Transition t) {
        return getZero();
    }

    public boolean hasTop(BDD bdd) {
        return !bdd.and(getTop()).isZero();
    }

    protected BDD getTop() {
        return nTop().not();
    }

    protected BDD getNotTop() {
        return nTop();
    }

    /**
     * Here the state belongs to Player 1 iff it does not contain any top.
     *
     * @param state
     * @return
     */
    @Override
    public boolean isEnvState(BDD state) {
        return !hasTop(state);
    }

    @Override
    public boolean isBadState(BDD state) {
        return !getBadDCSs().and(state).isZero();
    }

    @Override
    public boolean isSpecialState(BDD state) {
        return !getSpecialDCSs().and(state).isZero();
    }

    @Override
    public boolean isBufferState(BDD state) {
        return hasTop(state);
    }

    public BDD getMcut() {
        return mcut(0);
    }

    BDD getChosen(Transition t) {
        return chosen(t, 0);
    }

    protected BDD getBadDCSs() {
        return calcBadDCSs();
    }

    BDD getSpecialDCSs() {
        return calcSpecialDCSs();
    }

// %%%%%%%%%%%%%%%%%%%%%% Precalculated results / BDDs %%%%%%%%%%%%%%%%%%%%%%%%%
    protected BDD getBufferedNDet() {
        if (ndet == null) {
            ndet = ndetStates(0);
        }
        return ndet;
    }

    protected BDDDomain getTransitionDomain(int pos, int partition) {
        return TRANSITIONS[pos][partition];
    }

    protected BDDDomain getTopDomain(int pos, int partition) {
        return TOP[pos][partition];
    }

    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = attractor(getBadDCSs(), true, distance, false, null).not().and(getBufferedDCSs());
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    @Override
    protected BDD calcBadDCSs() {
        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
    }

    @Override
    protected BDD calcSpecialDCSs() {
        return getFactory().zero();
    }

}
