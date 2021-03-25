package uniolunisaar.adam.ds.synthesis.pgwt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pnwt.InconsistencyException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoCalculatorProvidedException;
import uniolunisaar.adam.exceptions.pnwt.NotInitialPlaceException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.UnboundedPGException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.ds.petrinetwithtransits.Transit;
import uniolunisaar.adam.util.pgwt.ExtensionCalculator;
import uniolunisaar.adam.util.pgwt.ExtensionCleaner;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class PetriGameWithTransits extends PetriNetWithTransits implements IPetriGame {

    private boolean skip = false;
    private BoundedResult bounded = null;
    private final Set<Place> envPlaces = new HashSet<>();

    private final Map<String, ExtensionCalculator<?>> calculators = new HashMap<>();

    /**
     * Creates a new PetriGame with the given name.
     *
     * @param name Name of the Petri game as String.
     * @param calc
     */
    public PetriGameWithTransits(String name, ExtensionCalculator<?>... calc) {
        super(name);
        PetriNetExtensionHandler.setProcessFamilyID(this, name + Thread.currentThread().getName());
        for (ExtensionCalculator<?> extensionCalculator : calc) {
            addExtensionCalculator(extensionCalculator.getKey(), extensionCalculator);
        }
    }

    public PetriGameWithTransits(PetriNet pn, ExtensionCalculator<?>... calc) throws NotSupportedGameException {
        this(pn, true, calc);
    }

    public PetriGameWithTransits(PetriNet pn, boolean skipChecks, ExtensionCalculator<?>... calc) throws NotSupportedGameException {
        super(pn);
        PetriNetExtensionHandler.setProcessFamilyID(this, pn.getName() + Thread.currentThread().getName());
        for (ExtensionCalculator<?> extensionCalculator : calc) {
            addExtensionCalculator(extensionCalculator.getKey(), extensionCalculator);
        }
        skip = skipChecks;
        if (!skipChecks) {
            check(pn);
        } else {
            Logger.getInstance().addWarning("Attention: You decided to skip the tests. We cannot ensure that the net is bounded!");
        }

        Logger.getInstance().addMessage("Buffer data ...");
        bufferData();
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    /**
     * Copy-Constructor
     *
     * @param game
     */
    public PetriGameWithTransits(PetriGameWithTransits game) {
        super(game);
        this.skip = game.skip;
        this.bounded = game.bounded;
        bufferData();

        // the annotations of the objects are already copied by APT
//        for (Transition t : game.getTransitions()) {
//            getTransition(t.getId()).copyExtensions(t);
//        }
//        for (Place p : game.getPlaces()) {
//            getPlace(p.getId()).copyExtensions(p);
//        }
        for (Map.Entry<String, ExtensionCalculator<?>> calc : game.calculators.entrySet()) {
            addExtensionCalculator(calc.getKey(), calc.getValue(), true); // todo: to greedy to add all of them as to listen to changes.
        }
    }

    private void bufferData() {
        for (Place place : getPlaces()) {
            if (PetriGameExtensionHandler.isEnvironment(place)) {
                envPlaces.add(place);
            }
        }
    }

    private void check(PetriNet pn) throws UnboundedPGException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//            Benchmarks.getInstance().start(Benchmarks.Parts.BOUNDED);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Check bounded ...");
        bounded = Bounded.checkBounded(pn);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//            Benchmarks.getInstance().stop(Benchmarks.Parts.BOUNDED);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        if (!bounded.isBounded()) {
            throw new UnboundedPGException(pn, bounded.unboundedPlace);
        }
        // Check if the initial flow places are marked correctly
        for (Place p : getPlaces()) {
            if (isInitialTransit(p) && p.getInitialToken().getValue() <= 0) {
                throw new NotInitialPlaceException(p);
            }
        }
    }

    public Map<String, ExtensionCalculator<?>> getCalculators() {
        return calculators;
    }

    public <O> ExtensionCalculator<?> addExtensionCalculator(String key, ExtensionCalculator<O> calc) {
        return addExtensionCalculator(key, calc, true);
    }

    /**
     * If reCalc is true the value is recalculated whenever a structural change
     * has occurred.
     *
     * @param <O>
     * @param key
     * @param calc
     * @param reCalc
     * @return
     */
    public <O> ExtensionCalculator<?> addExtensionCalculator(String key, ExtensionCalculator<O> calc, boolean reCalc) {
        if (reCalc) {
            super.addListener(new ExtensionCleaner<>(key));
        }
        return calculators.put(key, calc);
    }

    /**
     * Returns the calculated value of the calculator or throws a runtime
     * exception if no calculator with the given key exists.
     *
     * @param <O>
     * @param calculatorKey
     * @return
     */
    public <O> O getValue(String calculatorKey) throws NoCalculatorProvidedException {
        if (!hasExtension(calculatorKey)) {
            ExtensionCalculator<?> calc = calculators.get(calculatorKey);
            if (calc == null) {
                throw new NoCalculatorProvidedException(this, calculatorKey);
            }
            Set<ExtensionProperty> properties = calc.getProperties();
            ExtensionProperty[] props = new ExtensionProperty[properties.size()];
            properties.toArray(props);
            putExtension(calculatorKey, (O) calc.calculate(this), props);
        }
        return (O) getExtension(calculatorKey);
    }

    public boolean recalculate(String calculatorKey) {
        ExtensionCalculator<?> calc = calculators.get(calculatorKey);
        if (calc == null) {
            return false;
        }
        putExtension(calculatorKey, calc.calculate(this));
        return true;
    }

    /**
     * Returns single environment transition. That are transitions where only
     * environment place are in the preset.
     *
     * @return
     */
    public Set<Transition> getEnvTransitions() {
        Set<Transition> out = new HashSet<>();
        for (Transition t : getTransitions()) {
            boolean add = true;
            for (Place p : t.getPreset()) {
                if (!PetriGameExtensionHandler.isEnvironment(p)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * Checks if both given transitions are eventually firable in any reachable
     * marking.
     *
     * @param t1
     * @param t2
     * @return
     */
    public boolean eventuallyEnabled(Transition t1, Transition t2) {
        CoverabilityGraph reach = getReachabilityGraph();
        for (CoverabilityGraphNode state : reach.getNodes()) {
            Marking m = state.getMarking();
            if (t1.isFireable(m) && t2.isFireable(m)) {
                return true;
            }
        }
        return false;
    }

    public Place createEnvPlace() {
        Place p = createPlace();
        PetriGameExtensionHandler.setEnvironment(p);
        envPlaces.add(p);
        return p;
    }

    public Place createEnvPlace(String id) {
        Place p = createPlace(id);
        PetriGameExtensionHandler.setEnvironment(p);
        envPlaces.add(p);
        return p;
    }

    public Place createEnvPlace(Place p) {
        Place pl = createPlace(p);
        PetriGameExtensionHandler.setEnvironment(pl);
        envPlaces.add(p);
        return pl;
    }

    public Place[] createEnvPlaces(Place... placeList) {
        Place[] pls = createPlaces(placeList);
        for (Place pl : pls) {
            PetriGameExtensionHandler.setEnvironment(pl);
            envPlaces.add(pl);
        }
        return pls;
    }

    public Place[] createEnvPlaces(String... idList) {
        Place[] pls = createPlaces(idList);
        for (Place pl : pls) {
            PetriGameExtensionHandler.setEnvironment(pl);
            envPlaces.add(pl);
        }
        return pls;
    }

    public Place[] createEnvPlaces(int count) {
        Place[] pls = createPlaces(count);
        for (Place pl : pls) {
            PetriGameExtensionHandler.setEnvironment(pl);
            envPlaces.add(pl);
        }
        return pls;
    }

    @Override
    public void checkTransitConsistency(Transit transit) {
        super.checkTransitConsistency(transit);
        Place prePlace = transit.getPresetPlace();
        Set<Place> postset = transit.getPostset();
        boolean allPostEnv = true;
        boolean allPostSys = true;
        for (Place p : postset) {
            if (PetriGameExtensionHandler.isEnvironment(p)) {
                allPostSys = false;
            } else {
                allPostEnv = false;
            }
        }
        if (!allPostEnv && !allPostSys) {
            throw new InconsistencyException("You mixed system and enviroment places in the postset of the transit of transition " + transit.getTransition().getId());
        }
//        if (!(preset.isEmpty() || postset.isEmpty())) {
        if (!(prePlace == null || postset.isEmpty())) {
            if ((PetriGameExtensionHandler.isEnvironment(prePlace) && allPostSys) || (!PetriGameExtensionHandler.isEnvironment(prePlace) && allPostEnv)) {
                throw new InconsistencyException("You mapped environment places to system places (or vice versa) in the transit of transition " + transit.getTransition().getId());
            }
        }
    }

    @Override
    public String toAPT(boolean withAnnotationPartition, boolean withCoordinates) throws RenderException {
        return PGTools.getAPT(this, withAnnotationPartition, withCoordinates);
    }

    // Getter/Setter
    public Set<Place> getEnvPlaces() {
        return envPlaces;
    }

    public boolean isSkip() {
        return skip;
    }

    public BoundedResult getBounded() {
        if (bounded == null) {
            bounded = Bounded.checkBounded(this);
        }
        return bounded;
    }

    // Overriden methods to handle env places   
    /**
     * The others methods do not have to be overriten since all fall back to
     * this method
     *
     * @param place
     */
    @Override
    public void removePlace(String place) {
        Place p = getPlace(place);
        if (isEnvironment(p)) {
            envPlaces.remove(p);
        }
        super.removePlace(place);
    }

    // Set extensions  
    // For places       
    public boolean isEnvironment(Place place) {
        return PetriGameExtensionHandler.isEnvironment(place);
    }

    public void setEnvironment(Place place) {
        PetriGameExtensionHandler.setEnvironment(place);
        envPlaces.add(place);
//        invokeListeners(); todo: don't want to invoke the listeners for the Petri net changes, implement an own
    }

    public boolean isSystem(Place place) {
        return PetriGameExtensionHandler.isSystem(place);
    }

    public void setSystem(Place place) {
        PetriGameExtensionHandler.setSystem(place);
        envPlaces.remove(place);
//        invokeListeners(); todo: don't want to invoke the listeners for the Petri net changes, implement an own
    }

    @Override
    public void initializeWinningCondition(Condition<? extends Condition<?>> winCon) {
        winCon.buffer(this);
    }

    public boolean isSpecial(Flow f) {
        return PetriGameExtensionHandler.isSpecial(f);
    }

    public void removeSpecial(Flow f) {
        PetriGameExtensionHandler.removeSpecial(f);
    }

    public void setSpecial(Flow f) {
        PetriGameExtensionHandler.setSpecial(f);
    }

}
