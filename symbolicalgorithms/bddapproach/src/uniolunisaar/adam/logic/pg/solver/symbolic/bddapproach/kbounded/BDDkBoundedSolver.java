package uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.kbounded;

import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.SolverDontFitPetriGameException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.util.symbolic.bddapproach.JavaBDDCallback;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDkBoundedSolver extends Solver<PetriGame, BDDkBoundedSolvingObject<Safety>, BDDSolverOptions> {

    // BDD settings
    private BDDFactory bddfac;
    private boolean initialized = false;

    // the length of the decision sets
    private int dcsLength;

    // Domains for predecessor and successor for each token    
    BDDDomain[][] MULT;
    BDDDomain[][] PLACES;
    BDDDomain[][] TRANSITIONS;
    BDDDomain[][] TOP;

    /**
     * Creates a new solver for the given game.
     *
     * @param game - the games which should be solved.
     * @throws SolverDontFitPetriGameException - thrown if the created solver
     * don't fit the given winning objective specified in the given game.
     */
    BDDkBoundedSolver(BDDkBoundedSolvingObject<Safety> solvingObject, BDDSolverOptions opts) throws NotSupportedGameException, NoSuitableDistributionFoundException {
        super(solvingObject, opts);
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
        MULT = new BDDDomain[2][tokencount];
        PLACES = new BDDDomain[2][tokencount];
        TOP = new BDDDomain[2][tokencount - 1];
        TRANSITIONS = new BDDDomain[2][tokencount - 1];
        for (int i = 0; i < 2; ++i) {
            // Env-place
            int add = (!getSolvingObject().isConcurrencyPreserving() || getGame().getEnvPlaces().isEmpty()) ? 1 : 0; // add for no env place at all a dummy space
            PLACES[i][0] = getFactory().extDomain(getSolvingObject().getGame().getPlaces().size() + add);
            //for any token
            for (int j = 0; j < tokencount - 1; ++j) {
                Long k = getGame().getBounded().k;
                MULT[i][0] = getFactory().extDomain(k.intValue());
                // Place
                PLACES[i][j + 1] = getFactory().extDomain(getSolvingObject().getGame().getPlaces().size() + add);
                // top
                TOP[i][j] = getFactory().extDomain(2);
                // transitions                
                BigInteger maxTrans = BigInteger.valueOf(2);
                maxTrans = maxTrans.pow(getSolvingObject().getGame().getTransitions().size());
                TRANSITIONS[i][j] = getFactory().extDomain(maxTrans);
            }
        }
        System.out.println(getFactory().varNum() + "taddda");
        setDCSLength(getFactory().varNum() / 2);
    }

    String decodeDCS(byte[] dcs, int pos) {
        StringBuilder sb = new StringBuilder();
        // Env place
        sb.append("(");
        String id = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][0], getSolvingObject().getGame().getPlaces(), getSolvingObject().isConcurrencyPreserving());
        sb.append(id);
        sb.append(")").append("\n");
        for (int j = 0; j < getSolvingObject().getMaxTokenCount() - 1; j++) {
            sb.append("(");
            String sid = BDDTools.getPlaceIDByBin(getGame(), dcs, PLACES[pos][j + 1], getSolvingObject().getGame().getPlaces(), getSolvingObject().isConcurrencyPreserving());
            sb.append(sid);
            if (!sid.equals("-")) {
                sb.append(", ");
                sb.append(BDDTools.getTopFlagByBin(dcs, TOP[pos][j]));
                sb.append(", ");
                sb.append(BDDTools.getTransitionsByBin(dcs, TRANSITIONS[pos][j], new ArrayList<>(getSolvingObject().getGame().getTransitions())));
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

    @Override
    protected boolean exWinStrat() {
        if (!initialized) {
            initialize();
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    BDDFactory getFactory() {
        return bddfac;
    }

    public int getVariableNumber() {
        return bddfac.varNum();
    }
}
