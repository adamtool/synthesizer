package uniolunisaar.adam.symbolic.bddapproach.solver;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraphBuilder;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDSolvingObject;
import uniolunisaar.adam.symbolic.bddapproach.petrigame.BDDPetriGameStrategyBuilder;
import uniolunisaar.adam.util.benchmarks.Benchmarks;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.symbolic.bddapproach.util.JavaBDDCallback;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class BDDSolver<W extends Condition> extends Solver<BDDSolvingObject<W>, BDDSolverOptions> {

    // BDD settings
    private BDDFactory bddfac;
    private boolean initialized = false;

    // the length of the decision sets
    private int dcsLength;

    // Domains for predecessor and successor for each token    
    BDDDomain[][] PLACES;
    BDDDomain[][] TRANSITIONS;
    BDDDomain[][] TOP;

    //Buffered BDDs (todo:necessary?)  
    private BDD environment = null;
    private BDD system = null;
    private BDD exEnvSucc = null;
    private BDD exSysSucc = null;
    private BDD winDCSs = null;
    private BDD DCSs = null;
    private BDD ndet = null;

    /**
     * Creates a new solver for the given game.
     *
     * @param game - the games which should be solved.
     * @param skipTests
     * @param winCon
     * @param opts
     * @throws uniolunisaar.adam.exceptions.pg.NotSupportedGameException
     * @throws uniolunisaar.adam.exceptions.pg.NetNotSafeException
     * @throws
     * uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException
     * @throws uniolunisaar.adam.exceptions.pg.InvalidPartitionException
     */
    protected BDDSolver(PetriGame game, boolean skipTests, W winCon, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(new BDDSolvingObject<>(game, winCon, skipTests), opts);
        //todo: make it dependable of the given winning conditions but since I'm in a hurry, be  more conservative             
//        // Need at least one env place
//        if (getGame().getEnvPlaces().isEmpty()) {
//            throw new NotSupportedGameException("BDD solving need at least one environment place.");
//        }
//        // Need at least one sys place
//        boolean hasSystem = false;
//        for (Place p : getGame().getNet().getPlaces()) {
//            if (!AdamExtensions.isEnvironment(p)) {
//                hasSystem = true;
//                break;
//            }
//        }
//        if (!hasSystem) {
//            throw new NotSupportedGameException("BDD solving need at least one system place.");
//        }
        // hopefully not neccessary anymore
        // every set of system places annotated with the same token need at least one place in it, which has a transition in it postset
//        for (int i = 1; i < getGame().getPlaces().length; i++) {
//            Set<Place> placeSet = getGame().getPlaces()[i];
//            boolean hasSuccesor = false;
//            for (Place place : placeSet) {
//                if (!place.getPostset().isEmpty()) {
//                    hasSuccesor = true;
//                    break;
//                }
//            }
//            if (!hasSuccesor) {
//                throw new NotSupportedGameException("BDD solving need at least one successor in the set of system places annotated with the same token. Set '"
//                        + placeSet.toString() + "' is missing a succesor.");
//            }
//        }
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//
//    /**
//     * Here should those BDDs be calculated which should be precalculated at
//     * creation time of this object.
//     */
//    abstract void precalculateSpecificBDDs();
    /**
     * Here the initialisation of the Solver is done.
     *
     * The BDDFactory is initialised and some BDDs are precalculated.
     */
    public void initialize() {
        Logger.getInstance().addMessage("Initialize BDD data ...");
        if (bddfac != null && bddfac.isInitialized()) {
            Logger.getInstance().addMessage("BDDFactory reinitialized.");
            bddfac.done();
        }
        String libName = getSolverOpts().getLibraryName();
        int nodenum = getSolverOpts().getInitNodeNb();
        int cachesize = getSolverOpts().getCacheSize();
        int maxIncrease = getSolverOpts().getMaxIncrease();
        bddfac = BDDFactory.init(libName, nodenum, cachesize);
        // Redirect the GCSTats stream from standard System.err to Logger
        // and the Reorder and ResizeStats from System.out to Logger
        try {
            Method m = JavaBDDCallback.class.getMethod("outGCStats", Integer.class, BDDFactory.GCStats.class);
            bddfac.registerGCCallback(new JavaBDDCallback(bddfac), m);
            m = JavaBDDCallback.class.getMethod("outReorderStats", Integer.class, BDDFactory.ReorderStats.class);
            bddfac.registerReorderCallback(new JavaBDDCallback(bddfac), m);
            m = JavaBDDCallback.class.getMethod("outResizeStats", Integer.class, Integer.class);
            bddfac.registerResizeCallback(new JavaBDDCallback(bddfac), m);
        } catch (NoSuchMethodException | SecurityException ex) {
        }
        bddfac.setMaxIncrease(maxIncrease);
        Logger.getInstance().addMessage("Using BDDLibrary: " + bddfac.getVersion());
        Logger.getInstance().addMessage("BDD cachesize: " + cachesize);
        Logger.getInstance().addMessage("BDD nodenumber: " + nodenum);
        Logger.getInstance().addMessage("BDD max increase number: " + maxIncrease);
        // Variables and BDDs
        Logger.getInstance().addMessage("Creating variables ...");
        createVariables();
        Logger.getInstance().addMessage("Number of variables: " + bddfac.varNum());
        Logger.getInstance().addMessage("... creating variables done.");
        Logger.getInstance().addMessage("Reordering variables ...");
        reorderVariables();
        Logger.getInstance().addMessage("... reordering variables done.");
        Logger.getInstance().addMessage("... BDD Initialisation done.");
        initialized = true;
    }

    /**
     * Codierung: p_i_0 - Environment Token n - TokenCount
     * |p_i_0|p_i_1|top|t_1|...|t_m| ... |p_i_n|top|t_1|...|t_m|
     */
    void createVariables() {
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

    String decodeDCS(byte[] dcs, int pos) {
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

    public String decode(byte[] dcs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            if (BDDTools.notUsedByBin(dcs, getDcs_length(), i)) {
                sb.append("(not defined)\n");
            } else {
                sb.append(decodeDCS(dcs, i));
            }
            if (i == 0) {
                sb.append(" -> \n");
            }
        }
        return sb.toString();
    }

    /**
     * The variables of the BDDs are reordered since for a transition D->D' it
     * is much more faster to have the variables of D and D' interleaved since
     * many operations have to compare the variables of the predecessor and the
     * successor of a transition.
     */
    private void reorderVariables() {
        // interleaving of D and D' for D->D'
        int[] order = new int[bddfac.varNum()];
        int count = 0;
        for (int i = 0; i < this.dcsLength; i++) {
            order[count++] = i;
            order[count++] = dcsLength + i;
        }
        bddfac.setVarOrder(order);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% END INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Calculates the wellformed BDDs for the predecessor.
     *
     * @return BDD of wellformed predecessors.
     */
    BDD wellformed() {
        return wellformed(0);//.andWith(wellformed(1));
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
    BDD wellformed(int pos) {
        BDD well = bddfac.one();

        // only the places belonging to the token (or zero) are allowed in the positions
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD place = bddfac.zero();
            for (Place p : getSolvingObject().getDevidedPlaces()[i]) {
                place.orWith(codePlace(p, pos, i));
            }
            if (i == 0) { // env case
                place.orWith(codePlace(0, pos, 0));
            } else {
                place.orWith(notUsedToken(pos, i));
            }
            well.andWith(place);
        }

        // only transitions in the postset of pi are allowed in the commitments
        BDD com = bddfac.one();
        for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
            for (Transition t : getSolvingObject().getDevidedTransitions()[i - 1]) {
                BDD place = bddfac.zero();
                for (Place p : t.getPreset()) {
                    if (!getSolvingObject().getGame().isEnvironment(p) && i == getSolvingObject().getGame().getPartition(p)) {
                        place.orWith(codePlace(p, pos, i));
                    }
                }
                int id = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t);
                com.andWith(bddfac.nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(place));
                // further reduction of the commitment sets by idea of valentin spreckels:
                // when transition only has one place in preset no other transitions are 
                // allowed in the commitment set
                if (t.getPreset().size() == 1) {
                    // all others have to be zero
                    BDD trans = bddfac.one();
                    for (Transition t1 : getSolvingObject().getDevidedTransitions()[i - 1]) {
                        if (!t1.equals(t)) {
                            int id1 = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t1);
                            trans.andWith(bddfac.nithVar(TRANSITIONS[pos][i - 1].vars()[id1]));
                        }
                    }
                    com.andWith(bddfac.nithVar(TRANSITIONS[pos][i - 1].vars()[id]).orWith(trans));
                }
            }
        }
        well.andWith(com);

        //well.andWith(mixedTypes(pos)); //nicht so oder so not?
        return well;
    }

    /**
     * Calculates the BDD belonging to the initial marking with all tops set to
     * false.
     *
     * @return BDD for the initial marking.
     */
    BDD initial() {
        BDD init = marking2BDD(getGame().getInitialMarking());
        init.andWith(getNotTop());
        return init;//.and(getWellformed());
    }

    /**
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * This is for the original Defition with checking ndet within the strategy.
     * Since our scheduling does not considere every marking, some non
     * determinism could be overseen. Thus, we changed the definition of non
     * determinism for the Petri game itself and check now non determinism of
     * strategy transitions within the original Net.
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
     * Calculates a BDD with all situations where nondeterminism has been
     * encountered.
     *
     * Since our scheduling does not consider every marking, some non
     * determinism with the original version could be overseen.
     *
     * Thus, here we changed the definition of non determinism for the Petri
     * game itself and check now non determinism of strategy transitions within
     * the original Net.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor
     * variables.
     *
     * @return BDD with all nondeterministic situations.
     */
    BDD ndetStates2(int pos) {
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
                    BDD sharedPlaces = getZero();
                    for (Place place : intersect) {
                        if (!getSolvingObject().getGame().isEnvironment(place)) {
                            shared = true;
                            sharedPlaces.orWith(codePlace(place, pos, getSolvingObject().getGame().getPartition(place)));
                        }
                    }
                    if (shared && this.getGame().eventuallyEnabled(t1, t2)) { // here check added for firing in the original game
                        BDD first = sharedPlaces.andWith(chosen(t1, pos).andWith(chosen(t2, pos)));
                        nondet = nondet.orWith(first);
                    }
                }
            }
        }
        return nondet;//.andWith(wellformed());
    }

    /**
     * There could ndet states be missed because of the scheduling. A conjecture
     * is that it is enough to take the states where ndet encountered and add
     * all of those states with which we could reach such states by firing
     * transitions which are only dependent of an environment token in the
     * preset. Todo: check if this es really enough! That's not enough and there
     * is most likely no fix in this reduction for this problem.
     *
     * ATTENTION: fixes only a very special case!
     *
     * @return BDD with all nondeterministic situations.
     */
    BDD ndetEncountered() {
        BDD Q = getOne();
        BDD Q_ = getBufferedNDet();
        Set<Transition> envTrans = getGame().getEnvTransitions();
        int[] pres = new int[envTrans.size()];
        int[] posts = new int[envTrans.size()];
        int i = 0;
        for (Transition t : envTrans) {
            for (Place pre : t.getPreset()) {
                if (getSolvingObject().getGame().isEnvironment(pre)) {
                    pres[i] = getSolvingObject().getGame().getID(pre);
                }
            }
            for (Place post : t.getPostset()) {
                if (getSolvingObject().getGame().isEnvironment(post)) {
                    posts[i] = getSolvingObject().getGame().getID(post);
                }
            }
            ++i;
        }
        while (!Q_.equals(Q)) {
            Q = Q_;
            BDD pre = getZero(); // get all predecessors of the current ndetEncountered-States with only env transitions
            for (int j = 0; j < pres.length; j++) {
                //replace the env place at the front and check if the postset is set at the end
                pre.orWith(Q.exist(PLACES[0][0].set()).and(codePlace(pres[j], 0, 0)) // preset and postset should be singleton
                        .and(shiftFirst2Second(Q).and(codePlace(posts[j], 1, 0))));
            }
            pre = pre.exist(getVariables(1)); // only predecessors are interesting
            Q_ = pre.or(Q);
        }
//        System.out.println("ndet %%%%%%%");
//        BDDTools.printDecodedDecisionSets(Q_, this, true);
//        return getZero();
        return Q_;
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
//        return nTop;
        return nTop;//.andWith(getWellformed());
    }

    /**
     * precondition: variables to shift to are not within the BDD @bdd todo: the
     * above precondition is old (belonging to the uncommented code) and try to
     * find a better way. It's really a hack.
     *
     * @param bdd
     * @return
     */
    BDD shiftFirst2Second(BDD bdd) {
        BDD res = bdd.and(preBimpSucc());
        return res.exist(getFirstBDDVariables());
    }

    /**
     * precondi. first variables have to be -1 postcondi. second variables are
     * -1
     *
     * @param bdd
     * @return
     */
    BDD shiftSecond2First(BDD bdd) {
        BDD res = bdd.and(preBimpSucc());
        return res.exist(getSecondBDDVariables());
    }

    public BDD pre(BDD succ, BDD allTrans, BDD existsTrans) {
        BDD succ_shifted = shiftFirst2Second(succ);
        BDD forall = ((allTrans.imp(succ_shifted)).forAll(getSecondBDDVariables())).and(allTrans.id().exist(getSecondBDDVariables()));
        BDD exists = (existsTrans.and(succ_shifted)).exist(getSecondBDDVariables());
        return forall.or(exists).and(wellformed());
    }

    /**
     * @param succ
     * @return
     */
    public BDD preEnv(BDD succ) {
//        BDD succ_shifted = shiftFirst2Second(succ);
//        BDD forall = ((getBufferedSystemTransition().imp(succ_shifted)).forAll(getSecondBDDVariables())).and(getBufferedExSysSucc());
//        BDD exists = (getBufferedEnvTransitions().and(succ_shifted)).exist(getSecondBDDVariables());
//        return forall.or(exists).and(wellformed());
        return pre(succ, getBufferedSystemTransitions(), getBufferedEnvTransitions());
    }

    /**
     * pre of player 0
     *
     * @param succ
     * @return
     */
    public BDD preSys(BDD succ) {
//        BDD succ_shifted = shiftFirst2Second(succ);
//        BDD forall = (getBufferedEnvTransitions().imp(succ_shifted)).forAll(getSecondBDDVariables()).and(getBufferedExEnvSucc());
//        BDD exists = (getBufferedSystemTransition().and(succ_shifted)).exist(getSecondBDDVariables());
//        return forall.or(exists).and(wellformed());
        return pre(succ, getBufferedEnvTransitions(), getBufferedSystemTransitions());
    }

    protected BDD marking2BDD(Marking m) {
        BDD marking = bddfac.one();
        List<Integer> tokens = new ArrayList<>();
        for (Place place : getGame().getPlaces()) {
            if (m.getToken(place).getValue() > 0) {
                int token = getSolvingObject().getGame().getPartition(place);
                tokens.add(token);
                marking.andWith(codePlace(place, 0, token));
            }
        }
        if (!getSolvingObject().isConcurrencyPreserving()) {
            for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) {
                if (!tokens.contains(i)) {
                    marking.andWith(codePlace(0, 0, i));
                }
            }
        }
        return marking;
    }

    BDD chosen(Transition t, int pos) {
        BDD c = getOne();
        for (Place p : t.getPreset()) {
            if (!getSolvingObject().getGame().isEnvironment(p)) {
                // Sys places
//                    //if pi=p and it's not top, then t has to be set to one (old version mit not top?)
//                    BDD pl = codePlace(binID, offset).and(bddfac.nithVar(offset + PL_CODE_LEN + 1));
                int token = getSolvingObject().getGame().getPartition(p);
                int id = getSolvingObject().getDevidedTransitions()[token - 1].indexOf(t);
//                c.andWith(getFactory().ithVar(TRANSITIONS[pos][token - 1].vars()[id]));                
                //todo: change on 2019/03/20  the previous line
                //      check if this previously was an optimization which I'm currently not getting?!     
                //      this is needed for the new nondeterminsm
                BDD pl = codePlace(p, pos, token);
                pl.impWith(getFactory().ithVar(TRANSITIONS[pos][token - 1].vars()[id]));
                c.andWith(pl);
            }
        }
        return c;//.andWith(getWellformed());
    }

    /**
     * Enabled iff all preset places of t are set and the type fits.
     *
     * @param t
     * @param pos
     * @return
     */
    protected BDD enabled(Transition t, int pos) {
        BDD en = getOne();
        for (Place place : t.getPreset()) {
            if (getSolvingObject().getGame().isEnvironment(place)) {
                // Env place
                en.andWith(codePlace(place, pos, 0));
            } else {
                // Sys places
                int token = getSolvingObject().getGame().getPartition(place);
                BDD pl = codePlace(place, pos, token);
                en.andWith(pl);
            }
        }
        return en;//.andWith(getWellformed());
    }

    protected BDD firable(Transition t, int pos) {
        return enabled(t, pos).andWith(chosen(t, pos));
    }

    boolean isFirable(Transition t, BDD source) {
        return !source.and(firable(t, 0)).isZero();
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

    BDD notUsedToken(int pos, int token) {
        BDD zero = codePlace(0, pos, token);
        zero.andWith(TOP[pos][token - 1].ithVar(0));
        zero.andWith(nothingChosen(pos, token));
        return zero;
    }

    void setPresetAndNeededZeros(Set<Place> pre_sys, List<Integer> visitedToken, BDD all) {
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
    void setNotAffectedPositions(BDD all, List<Integer> visitedToken) {
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

    BDD envPart(Transition t) {
        BDD all = getOne();
        // todo: one environment token case
        List<Place> pre = getSolvingObject().getSplittedPreset(t).getFirst();
        List<Place> post = getSolvingObject().getSplittedPostset(t).getFirst();
        if (!pre.isEmpty()) { // not really necessary since CP, but for no envtoken at all
            all.andWith(codePlace(pre.get(0), 0, 0));
        } else {
            all.andWith(codePlace(0, 0, 0));
        }
        if (!post.isEmpty()) { // not really necessary since CP, but for no envtoken at all
            all.andWith(codePlace(post.get(0), 1, 0));
        } else {
            all.andWith(codePlace(0, 1, 0));
        }
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        all.andWith(getMcut());
        return all;
    }

    protected BDD envTransitionCP(Transition t) {
        if (!getSolvingObject().getSysTransition().contains(t)) {
            Set<Place> pre_sys = t.getPreset();
            BDD all = firable(t, 0);
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
                all.andWith(pl);
            }
            // Environmentpart                
            all.andWith(envPart(t));
            return all;
        }
        return getZero();
    }

    protected BDD envTransitionNotCP(Transition t) {
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
                }
            }

            // set the dcs for the places in the preset
            setPresetAndNeededZeros(pre_sys, visitedToken, all);

            // --------------------------
            // Positions in dcs not set with places of pre- or postset
            setNotAffectedPositions(all, visitedToken);

            // Environmentpart                
            all.andWith(envPart(t));
            return all;
        }
        return getZero();
    }

    BDD sysTopPart() {
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
            // \not topi=>ti=ti'
            BDD impl = TOP[0][i - 1].ithVar(0).impWith(commitmentsEqual(i));
            sysT.andWith(impl);
        }
        return sysT;
    }

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
                } else {
                    //pre_i=post_i'
                    inner.andWith(codePlace(getSuitableSuccessor(place, t), 1, i));
                }
                pl.orWith(inner);
            }
            sysN.andWith(pl);
            // top'=0
            sysN.andWith(TOP[1][i - 1].ithVar(0));
        }
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());

        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        return sys;
    }

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
            }
        }
        // set the dcs for the places in the preset
        setPresetAndNeededZeros(pre_sys, visitedToken, sysN);
        // Positions in dcs not set with places of pre- or postset
        setNotAffectedPositions(sysN, visitedToken);
        sysN = (top.not()).impWith(sysN);

        // top part
        BDD sysT = top.impWith(sysTopPart());
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut
        sys.andWith(sysN);
        sys.andWith(sysT);
        // p0=p0'        
        sys = sys.andWith(placesEqual(0));

        return sys;
    }

    public boolean hasFired(Transition t, BDD source, BDD target) {
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }

        boolean cp = getSolvingObject().isConcurrencyPreserving();
        BDD trans = source.and(shiftFirst2Second(target));
        BDD out;

        // if purely system transition
        if (cp) {
            out = sysTransitionCP(t).and(trans);
        } else {
            out = sysTransitionNotCP(t).and(trans);
        }
        if (!out.isZero()) {
            return true;
        }

        // is env transition
        if (cp) {
            out = envTransitionCP(t).andWith(trans);
        } else {
            out = envTransitionNotCP(t).andWith(trans);
        }
        return !out.isZero();
    }

    /**
     *
     * @param t
     * @param source
     * @param target
     * @return
     */
    @Deprecated
    public boolean hasFiredManually(Transition t, BDD source, BDD target) {
        if (hasTop(source)) { // in a top state nothing could have been fired
            return false;
        }
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }
        // here the preset of t is fitting the source (and the commitment set)! Do not need to test it extra

        // Create bdd mantarget with the postset of t and the rest -1
        // So with "and" we can test if the postset of t also fit to the target
        // additionally create a copy of the target BDD with the places of the postset set to -1
        Pair<List<Place>, List<Place>> post = getSolvingObject().getSplittedPostset(t);
        // Environment place
        // todo: one environment token case
        BDD manTarget = getOne();
        BDD restTarget = target.id();
        if (!post.getFirst().isEmpty()) {
            manTarget.andWith(codePlace(post.getFirst().get(0), 0, 0));
            restTarget = restTarget.exist(getTokenVariables(0, 0));
        }

        // System places        
        List<Place> postSys = post.getSecond();
        BDD sysPlacesTarget = getOne();
        for (Place p : postSys) {
            int token = getSolvingObject().getGame().getPartition(p);
            sysPlacesTarget.andWith(codePlace(p, 0, token));
            restTarget = restTarget.exist(getTokenVariables(0, token));
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

        // now test if the places not in pre- or postset of t stayed equal between source and target
        return !(restTarget.and(restSource)).isZero();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    BDD attractor(BDD F, boolean p1) throws CalculationInterruptedException {
        return attractor(F, p1, getBufferedDCSs());
    }

    protected BDD attractor(BDD F, boolean p1, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        return attractor(F, p1, getBufferedDCSs(), distance);
    }

    BDD attractor(BDD F, boolean p1, BDD gameGraph) throws CalculationInterruptedException {
        return attractor(F, p1, gameGraph, null);
    }

    BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // Calculate the possibly restricted transitions to the given game graph
        BDD graphSuccs = this.shiftFirst2Second(gameGraph);
        BDD envTrans = getBufferedEnvTransitions().and(gameGraph).and(graphSuccs);
        BDD sysTrans = getBufferedSystemTransitions().and(gameGraph).and(graphSuccs);

        BDD Q = getZero();
        BDD Q_ = F;
        int i = 0;
        while (!Q_.equals(Q)) {
            if (Thread.currentThread().isInterrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, Q_);
            }
            Q = Q_;
            BDD pre = p1 ? pre(Q, sysTrans, envTrans) : pre(Q, envTrans, sysTrans);
            Q_ = pre.or(Q);
        }
        return Q_.andWith(wellformed());
    }

    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * with strategy building from zimmermanns lecture script
     *
     * @return
     */
    BDD buchi(BDD buchiStates, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        return buchi(buchiStates, distance, true);
    }

    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * with strategy building from zimmermanns lecture script
     *
     * @return
     */
    BDD buchi(BDD buchiStates, Map<Integer, BDD> distance, boolean player1) throws CalculationInterruptedException {
        BDD S = getBufferedDCSs().id();
        BDD W = getZero();
        BDD W_;
        BDD B = buchiStates;
        do {
            B = B.and(S);
            if (distance != null) {
                distance.clear();
            }
            BDD R = player1 ? attractor(B, !player1, S, distance) : attractor(B, !player1, S);
//            System.out.println("R states");
//            BDDTools.printDecodedDecisionSets(R, this, true);
//            System.out.println("END R staes");
//            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%% attr reach ");
//            BDDTools.printDecodedDecisionSets(R, this, true);
            BDD Tr = S.and(R.not());
//            System.out.println("TR states");
//            BDDTools.printDecodedDecisionSets(Tr, this, true);
//            System.out.println("END TR states");
//            System.out.println("%%%%%%%%%%%%%%%% TR");
//            BDDTools.printDecodedDecisionSets(Tr, this, true);         
            W_ = player1 ? attractor(Tr, player1, S) : attractor(Tr, player1, S, distance);

//            System.out.println("W_ states");
//            BDDTools.printDecodedDecisionSets(W_, this, true);
//            System.out.println("END W_ states");
//            System.out.println("%%%%%%%%%%%%%%%% atrrroktor TR");
//            BDDTools.printDecodedDecisionSets(W_, this, true);
            W = W.or(W_);
            S.andWith(W_.not());
        } while (!W_.isZero());
        //        System.out.println("%%%%%%%%%%%% W");
//        BDDTools.printDecodedDecisionSets(W, this, true);
        W = W.not().and(getBufferedDCSs());
        // Save attr0(recurm(F))\recurm(F) at position -1
        if (distance != null) {
//            attractor(B, false, getBufferedDCSs(), distance);
//            System.out.println("hier" + distance.toString());
            if (player1) {
                distance.put(-1, B);
            } else {
                distance.put(-1, B.not().and(getBufferedDCSs()));
            }
        }
//        System.out.println("%%%%%%%%%%%% return");
//        BDDTools.printDecodedDecisionSets(endStates(0), this, true);
        return W;
    }

    /**
     * Calculates all states reachable from the initial state.
     *
     * @return BDD with all reachable states
     * @throws uniolunisaar.adam.exceptions.pg.CalculationInterruptedException
     */
    protected BDD calcDCSs() throws CalculationInterruptedException {
        BDD Q = getZero();
        BDD Q_ = getInitialDCSs();
        while (!Q_.equals(Q)) {
            if (Thread.currentThread().isInterrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Q = Q_;
            // if it is an mcut or not is already coded in the transitions itself
            BDD succs = getBufferedEnvTransitions().or(getBufferedSystemTransitions());
            succs = succs.and(Q);
            Q_ = getSuccs(succs).or(Q);
        }
        return Q.and(wellformed(0));
    }

    /**
     * Returns the winning decisionsets for the system players.
     *
     * @param distance
     * @return - A BDD containing all winning states for the system.
     * @throws uniolunisaar.adam.exceptions.pg.CalculationInterruptedException
     */
    protected abstract BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException;

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        if (!initialized) {
            initialize();
        }
        return !((getBufferedWinDCSs().and(getInitialDCSs())).isZero());
    }

    public BDDGraph getGraphGame() throws CalculationInterruptedException {
        if (!initialized) {
            initialize();
        }
        return BDDGraphBuilder.getInstance().builtGraph(this);
    }

    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph g = BDDGraphBuilder.getInstance().builtGraphStrategy(this, null);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        return g;
    }

    public BDDGraph getGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        if (!initialized) {
            initialize();
        }
        return calculateGraphStrategy();
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGame pn = BDDPetriGameStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    public Pair<BDDGraph, PetriGame> getStrategies() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGame pstrat = BDDPetriGameStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return new Pair<>(gstrat, pstrat);
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
    BDD getVariables(int pos) {
        // Existential variables
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
     * sucessor as BDD.
     *
     * This means the varibles for the coding of the place, the top-flag, and
     * the belonging commitment set for a system token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    BDD getTokenVariables(int pos, int token) {
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
    BDD preBimpSucc() {
        BDD preBimpSucc = PLACES[0][0].buildEquals(PLACES[1][0]);
        for (int i = 0; i < getSolvingObject().getMaxTokenCount() - 1; ++i) {
            preBimpSucc.andWith(PLACES[0][i + 1].buildEquals(PLACES[1][i + 1]));
            preBimpSucc.andWith(TOP[0][i].buildEquals(TOP[1][i]));
            preBimpSucc.andWith(TRANSITIONS[0][i].buildEquals(TRANSITIONS[1][i]));
        }
        return preBimpSucc;
    }

    /**
     * Returns a BDD where all predecessors of environment transitions are coded
     * and the successor are arbitrary.
     *
     * @return - All environment states.
     */
    private BDD calcExEnvSucc() {
        return getBufferedEnvTransitions().id().exist(getSecondBDDVariables());
    }

    /**
     * Returns a BDD where all predecessors of system transitions are coded and
     * the successor are arbitrary.
     *
     * @return - All environment states.
     */
    private BDD calcExSysSucc() {
        return getBufferedSystemTransitions().id().exist(getSecondBDDVariables());
    }

    /**
     * Returns a BDD where the places of the predecessor and the successor of
     * the given token are equal.
     *
     * @param token - the token where the places should be equal.
     * @return - A BDD where the places are equal at the given token.
     */
    BDD placesEqual(int token) {
        return PLACES[0][token].buildEquals(PLACES[1][token]);
    }

    /**
     * Returns a BDD where the commitment sets of the predecessor and the
     * successor of the given token are equal.
     *
     * @param token - the token where the commitment sets should be equal.
     * @return - A BDD where the commitment sets are equal at the given token.
     */
    BDD commitmentsEqual(int token) {
        return TRANSITIONS[0][token - 1].buildEquals(TRANSITIONS[1][token - 1]);
    }

    /**
     * Returns a BDD where the commitment set of the predecessor or the
     * successor is the emptyset for a given token.
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - the token where the commitment set should be empty.
     * @return - A BDD with an empty commitment set at the given position for
     * the given token.
     */
    BDD nothingChosen(int pos, int token) {
        return TRANSITIONS[pos][token - 1].ithVar(0);
    }

    /**
     * Returns a BDD where the given id of the place is coded at the given
     * position in the transition.
     *
     * @param id - the id of the place which should be coded.
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - the token to which the place belongs and thus should be
     * coded at.
     * @return - A BDD with the id coded at the given position.
     */
    BDD codePlace(int id, int pos, int token) {
        return PLACES[pos][token].ithVar(id);
    }

    /**
     * Returns a BDD where the place is coded at the given position in the
     * transition.
     *
     * @param place - the place which should be coded.
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @param token - the token to which the place belongs and thus should be
     * coded at.
     * @return - A BDD with the id coded at the given position.
     */
    protected BDD codePlace(Place place, int pos, int token) {
        assert (getSolvingObject().getGame().getPartition(place) == token);
        return codePlace(getSolvingObject().getGame().getID(place), pos, token);
    }

    BDD getEnvironmentTransitions() {
        BDD env = getZero();
        boolean cp = getSolvingObject().isConcurrencyPreserving();
        for (Transition t : getGame().getTransitions()) {
            env.orWith(cp ? envTransitionCP(t) : envTransitionNotCP(t));
        }
        // no nondeterministic successors
        return env;//.andWith(ndet(1).not().andWith(ndet(0).not()));
    }

    BDD getSystemTransitions() {
        BDD sys = getZero();
        boolean cp = getSolvingObject().isConcurrencyPreserving();
        for (Transition t : getSolvingObject().getSysTransition()) {
            sys.orWith(cp ? sysTransitionCP(t) : sysTransitionNotCP(t));
        }
        // no nondeterministic successors
        return sys;//.andWith(ndet(1).not().andWith(ndet(0).not()));
    }

    /**
     * Returns the variables of the predecessor of a transition.
     *
     * @return - the predecessor variables.
     */
    public BDD getFirstBDDVariables() {
        return getVariables(0);
    }

    /**
     * Returns the variables of the successor of a transition.
     *
     * @return - the successor variables.
     */
    BDD getSecondBDDVariables() {
        return getVariables(1);
    }

    /**
     * Searches for a transition which could have been fired to get the target
     * BDD of the source BDD.
     *
     * @param source - the source BDD.
     * @param target - the target BDD.
     * @return - A transition which could have been fired to connect source and
     * target.
     */
    public Transition getTransition(BDD source, BDD target) {
        for (Transition t : getGame().getTransitions()) {
            if (hasFired(t, source, target)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Searches for all transitions which could have been fired to get the
     * target BDD of the source BDD.
     *
     * @param source - the source BDD.
     * @param target - the target BDD.
     * @return - A transition which could have been fired to connect source and
     * target.
     */
    public List<Transition> getAllTransitions(BDD source, BDD target) {
        List<Transition> ret = new ArrayList<>();
        for (Transition t : getGame().getTransitions()) {
            if (hasFiredManually(t, source, target)) {
                ret.add(t);
            }
        }
        return ret;
    }

    /**
     * Returns a BDD where the successors of the given BDD are shifted to the
     * predecessor and the successors are arbitrary.
     *
     * This means given D->D' returing D'-> -1.
     *
     * @param trans - The BDD where the successors should be shifted to the
     * predecessors.
     * @return - The successor states of the given transition.
     */
    public BDD getSuccs(BDD trans) {
        trans = trans.exist(getFirstBDDVariables());
        trans = shiftSecond2First(trans);
        return trans;
    }

    /**
     * @param pre
     * @param t
     * @return
     */
    public Place getSuitableSuccessor(Place pre, Transition t) {
        for (Place place : t.getPostset()) {
            if (getSolvingObject().getGame().getPartition(pre) == getSolvingObject().getGame().getPartition(place)) {
                return place;
            }
        }
        // no suitable successor found
        return null;
    }

    public boolean hasTop(BDD bdd) {
        return !bdd.and(getTop()).isZero();
    }

    protected BDD getTop() {
        return nTop().not();
    }

    BDD getNotTop() {
        return nTop();
    }

    public BDD getSystemSuccTransitions(BDD state) {
//        System.out.println("sys");
//        BDDTools.getDecodedDecisionSets(state.and(getBufferedSystemTransitions()), this);
        return state.and(getBufferedSystemTransitions());
    }

    public BDD getEnvSuccTransitions(BDD state) {
//        System.out.println("env");
//        BDDTools.printDecodedDecisionSets((getBufferedEnvTransitions()), this, true);
        return state.and(getBufferedEnvTransitions());
    }

    public BDD getInitialDCSs() {
        return initial();
    }

    public boolean isEnvState(BDD state) {
        // it's an env state when it's an mcut
        return !state.and(getMcut()).isZero();
    }

    public BDD getMcut() {
        return mcut(0);
    }

    BDD getEnabled(Transition t) {
        return enabled(t, 0);
    }

    BDD getChosen(Transition t) {
        return chosen(t, 0);
    }

// %%%%%%%%%%%%%%%%%%%%%% Precalculated results / BDDs %%%%%%%%%%%%%%%%%%%%%%%%%
    public void setBufferedWinDCSs(BDD win) {
        winDCSs = win;
    }

    public BDD getBufferedWinDCSs() throws CalculationInterruptedException {
        if (winDCSs == null) {
            winDCSs = calcWinningDCSs(null);
        }
        return winDCSs;
    }

    public BDD getBufferedDCSs() throws CalculationInterruptedException {
        if (DCSs == null) {
            DCSs = calcDCSs();
        }
        return DCSs;
    }

    protected BDD getBufferedSystemTransitions() {
        if (system == null) {
            system = getSystemTransitions();
        }
        return system;
    }

    protected BDD getBufferedEnvTransitions() {
        if (environment == null) {
            environment = getEnvironmentTransitions();
        }
        return environment;
    }

    protected BDD getBufferedNDet() {
        if (ndet == null) {
//            ndet = ndetStates(0);
            ndet = ndetStates2(0);
            //fixes one special case of the ndet problem but takes longer
//            ndet = ndetEncountered();
        }
        return ndet;
    }

    BDD getBufferedExEnvSucc() {
        if (exEnvSucc == null) {
            exEnvSucc = calcExEnvSucc();
        }
        return exEnvSucc;
    }

    BDD getBufferedExSysSucc() {
        if (exSysSucc == null) {
            exSysSucc = calcExSysSucc();
        }
        return exSysSucc;
    }

    /**
     * TODO: didn't wanted this to have from outside, but maybe need so
     *
     * @return
     */
    public BDD getWellformed() {
        return wellformed(0);
    }

    protected BDDDomain getTransitionDomain(int pos, int partition) {
        return TRANSITIONS[pos][partition];
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% DELEGATED METHODS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public BDD getZero() {
        return bddfac.zero();
    }

    public BDD getOne() {
        return bddfac.one();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% GETTER / SETTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public int getDcs_length() {
        return dcsLength;
    }

    void setDCSLength(int dscLength) {
        this.dcsLength = dscLength;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% Getter/Setter for BDD library %%%%%%%%%%%%%%%%%%%%%
    protected BDDFactory getFactory() {
        return bddfac;
    }

    public int getVariableNumber() {
        return bddfac.varNum();
    }
}
