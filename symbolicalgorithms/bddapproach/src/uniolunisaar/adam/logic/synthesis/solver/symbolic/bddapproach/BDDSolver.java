package uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.solver.Solver;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach.BDDGraphAndGStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.builder.pgwt.symbolic.bddapproach.BDDPetriGameStrategyBuilder;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.util.symbolic.bddapproach.JavaBDDCallback;
import uniolunisaar.adam.tools.Logger;

/**
 * An abstract class for providing some features for BDD solvers.As BDD domain
 * only places are provided.
 *
 * @author Manuel Gieseking
 * @param <W>
 * @param <SOP>
 * @param <SO>
 */
public abstract class BDDSolver<W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions> extends Solver<PetriGameWithTransits, W, BDDSolvingObject<W>, SOP> {

    // BDD settings
    private BDDFactory bddfac;
    private boolean initialized = false;

    // the length of the decision sets
    private int dcsLength;

    // Domains for predecessor and successor for each token    
    protected BDDDomain[][] PLACES;

    //Buffered BDDs (todo:necessary?)  
    private BDD environment = null;
    private BDD system = null;
    private BDD exEnvSucc = null;
    private BDD exSysSucc = null;
    private BDD winDCSs = null;
    private BDD DCSs = null;

    public BDDSolver(SO solverObject, SOP options) {
        super(solverObject, options);
    }

    protected abstract BDD calcSystemTransition(Transition t);

    protected abstract BDD calcEnvironmentTransition(Transition t);

    public abstract boolean isEnvState(BDD state);

    public abstract boolean isBadState(BDD state);

    public abstract boolean isSpecialState(BDD state);

    public abstract boolean isBufferState(BDD state);

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% START INIT %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Here the initialization of the Solver is done.
     *
     * The BDDFactory is initialized and some BDDs are pre-calculated.
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
     * Encoding: |p_0|...|p_n|
     */
    protected void createVariables() {
        int tokencount = getSolvingObject().getMaxTokenCountInt();
        PLACES = new BDDDomain[2][tokencount];
        for (int i = 0; i < 2; ++i) {
            int add = getSolvingObject().isConcurrencyPreserving() ? 0 : 1;
            //for all partitions
            for (int j = 0; j < tokencount; ++j) {
                // Place
                PLACES[i][j] = getFactory().extDomain(getSolvingObject().getDevidedPlaces()[j].size() + add);
            }
        }
        setDCSLength(getFactory().varNum() / 2);
    }

    /**
     * The variables of the BDDs are reordered since for a transition D->D' it
     * is much faster to have the variables of D and D' interleaved since many
     * operations have to compare the variables of the predecessor and the
     * successor of a transition. todo: could be even faster just to interleave
     * the domains
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
    protected String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int j = 0; j < getSolvingObject().getMaxTokenCount(); j++) {
            String sid = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][j], getSolvingObject().getDevidedPlaces()[j], getSolvingObject().isConcurrencyPreserving());
            sb.append(sid);
            sb.append(",");
        }
        sb.append(")");
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
     * Calculates the wellformed BDDs for the predecessor.
     *
     * @return BDD of wellformed predecessors.
     */
    protected BDD wellformed() {
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
    protected BDD wellformed(int pos) {
        BDD well = getOne();

        // only the places belonging to the token (or zero) are allowed in the positions
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            BDD place = getZero();
            for (Place p : getSolvingObject().getDevidedPlaces()[i]) {
                place.orWith(codePlace(p, pos, i));
            }
            place.orWith(notUsedToken(pos, i));
            well.andWith(place);
        }
        return well;
    }

    protected BDD notUsedToken(int pos, int token) {
        BDD zero = codePlace(0, pos, token);
        return zero;
    }

    /**
     * Calculates the BDD belonging to the initial marking.
     *
     * @return BDD for the initial marking.
     */
    protected BDD initial() {
        BDD init = marking2BDD(getGame().getInitialMarking());
        return init;//.and(getWellformed());
    }

    protected BDD calcEnvironmentTransitions() {
        BDD env = getZero();
        for (Transition t : getGame().getTransitions()) {
            env.orWith(calcEnvironmentTransition(t));
        }
        return env;
    }

    protected BDD calcSystemTransitions() {
        BDD sys = getZero();
        for (Transition t : getGame().getTransitions()) {
            sys.orWith(calcSystemTransition(t));
        }
        return sys;
    }

    /**
     * precondition: variables to shift to are not within the BDD @bdd todo: the
     * above precondition is old (belonging to the uncommented code) and try to
     * find a better way. It's really a hack.
     *
     * @param bdd
     * @return
     */
    protected BDD shiftFirst2Second(BDD bdd) {
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
    protected BDD shiftSecond2First(BDD bdd) {
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
        BDD marking = getOne();
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

    protected boolean isFirable(Transition t, BDD source) {
        return !source.and(enabled(t, 0)).isZero();
    }

    public boolean hasFired(Transition t, BDD source, BDD target) {
        if (!isFirable(t, source)) { // here source tested 
            return false;
        }

        BDD trans = source.and(shiftFirst2Second(target));
        BDD out;

        // if purely system transition
        out = calcSystemTransition(t).and(trans);
        if (!out.isZero()) {
            return true;
        }

        // is env transition
        out = calcEnvironmentTransition(t).andWith(trans);
        return !out.isZero();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    BDD attractor(BDD F, boolean p1) throws CalculationInterruptedException {
        return attractor(F, p1, getBufferedDCSs());
    }

    protected BDD attractor(BDD F, boolean p1, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        return attractor(F, p1, getBufferedDCSs(), distance);
    }

    protected BDD attractor(BDD F, boolean p1, BDD gameGraph) throws CalculationInterruptedException {
        return attractor(F, p1, gameGraph, null);
    }

    protected BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // Calculate the possibly restricted transitions to the given game graph
        BDD graphSuccs = this.shiftFirst2Second(gameGraph);
        BDD envTrans = getBufferedEnvTransitions().and(gameGraph).and(graphSuccs);
        BDD sysTrans = getBufferedSystemTransitions().and(gameGraph).and(graphSuccs);

        BDD Q = getZero();
        BDD Q_ = F;
        int i = 0;
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
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
     * @param buchiStates
     * @param distance
     * @return
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     */
    protected BDD buchi(BDD buchiStates, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        return buchi(buchiStates, distance, true);
    }

    /**
     * Compare Algorithm for Buchi Games by Krish
     *
     * with strategy building from zimmermanns lecture script
     *
     * @param buchiStates
     * @param distance
     * @param player1
     * @return
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     */
    protected BDD buchi(BDD buchiStates, Map<Integer, BDD> distance, boolean player1) throws CalculationInterruptedException {
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
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     */
    protected BDD calcDCSs() throws CalculationInterruptedException {
        BDD Q = getZero();
        BDD Q_ = getInitialDCSs();
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
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
     * @throws uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException
     */
    protected abstract BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException;

    protected abstract BDD calcBadDCSs();

    protected abstract BDD calcSpecialDCSs();

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        if (!initialized) {
            initialize();
        }
        return !((getBufferedWinDCSs().and(getInitialDCSs())).isZero());
    }

    protected BDDGraph calculateGraphGame() throws CalculationInterruptedException {
        return BDDGraphAndGStrategyBuilder.getInstance().builtGraph(this);
    }

    public BDDGraph getGraphGame() throws CalculationInterruptedException {
        if (!initialized) {
            initialize();
        }
        return calculateGraphGame();
    }

    protected BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph g = BDDGraphAndGStrategyBuilder.getInstance().builtGraphStrategy(this, null);
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
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pn = BDDPetriGameStrategyBuilder.getInstance().builtStrategy(this, gstrat);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return pn;
    }

    public Pair<BDDGraph, PetriGameWithTransits> getStrategies() throws NoStrategyExistentException, CalculationInterruptedException {
        BDDGraph gstrat = getGraphStrategy();
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PG_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        PetriGameWithTransits pstrat = BDDPetriGameStrategyBuilder.getInstance().builtStrategy(this, gstrat);
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
    protected BDD getVariables(int pos) {
        // Existential variables
        BDD variables = getFactory().one();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            variables.andWith(PLACES[pos][i].set());
            // does not seem to make a big different if this or the other version is used
//            variables = variables.and(PLACES[pos][i + 1].set());
        }
        return variables;
    }

    /**
     * Returns the variables belonging to one token in a predecessor or in a
     * sucessor as BDD.
     *
     * This means just the varibles for the coding of the place
     *
     * @param pos - 0 for the predecessor variables and 1 for the sucessor.
     * @param token - for which token the variables should be return.
     * @return - the variables of the given token of the predecessor or the
     * successor.
     */
    protected BDD getTokenVariables(int pos, int token) {
        return PLACES[pos][token].set();
    }

    /**
     * Creates a BDD which is true, when the predesseccor und the successor of a
     * transition are equivalent.
     *
     * @return BDD with Pre <-> Succ
     */
    protected BDD preBimpSucc() {
        BDD preBimpSucc = getFactory().one();
        for (int i = 0; i < getSolvingObject().getMaxTokenCount(); ++i) {
            // does not seem to make a big different if this or the other version is used
//            preBimpSucc = preBimpSucc.and(PLACES[0][i + 1].buildEquals(PLACES[1][i + 1]));
//            preBimpSucc = preBimpSucc.and(TOP[0][i].buildEquals(TOP[1][i]));
//            preBimpSucc = preBimpSucc.and(TRANSITIONS[0][i].buildEquals(TRANSITIONS[1][i]));
            preBimpSucc.andWith(PLACES[0][i].buildEquals(PLACES[1][i]));
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
    protected BDD placesEqual(int token) {
        return PLACES[0][token].buildEquals(PLACES[1][token]);
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
    protected BDD codePlace(int id, int pos, int token) {
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
    protected BDD getSecondBDDVariables() {
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
            if (hasFired(t, source, target)) {
                ret.add(t);
            }
        }
        // do the top case
        if (ret.isEmpty()) {
            ret.add(null);
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

    BDD getEnabled(Transition t) {
        return enabled(t, 0);
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
//            DCSs = wellformed();
            DCSs = calcDCSs();
        }
        return DCSs;
    }

    protected BDD getBufferedSystemTransitions() {
        if (system == null) {
            system = calcSystemTransitions();
        }
        return system;
    }

    protected BDD getBufferedEnvTransitions() {
        if (environment == null) {
            environment = calcEnvironmentTransitions();
        }
        return environment;
    }

    protected BDD getBufferedExEnvSucc() {
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

    protected void setDCSLength(int dscLength) {
        this.dcsLength = dscLength;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * TODO: didn't wanted this to have from outside, but maybe need so
     *
     * @param pos
     * @return
     */
    public BDD getWellformed(int pos) {
        return wellformed(pos);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% Getter/Setter for BDD library %%%%%%%%%%%%%%%%%%%%%
    protected BDDFactory getFactory() {
        return bddfac;
    }

    public int getVariableNumber() {
        return bddfac.varNum();
    }

    @Override
    public SO getSolvingObject() {
        return (SO) super.getSolvingObject(); // todo: hacky check why the generics won't work
    }

}
