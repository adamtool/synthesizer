package uniolunisaar.adam.symbolic.bddapproach.petrigame;

import uniolunisaar.adam.symbolic.bddapproach.partitioning.MaxiumNumberOfTokenCalculator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.conpres.ConcurrencyPreserving;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.UnboundedPGException;
import uniolunisaar.adam.symbolic.bddapproach.partitioning.Partitioner;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.symbolic.bddapproach.util.benchmark.Benchmarks;
import uniolunisaar.adam.util.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDPetriGame extends PetriGame {

    //todo: not final since java do not have a keyword for methods just used in
    // a constructor.
    private int TOKENCOUNT = -1;
    private boolean concurrencyPreserving;
    // saves places devided into groups for each token
    private Set<Place>[] places;
    // saves transitions belonging in the presets of the places to each token
    private List<Transition>[] transitions;

    public BDDPetriGame(PetriNet pn) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        this(pn, false);
    }

    public BDDPetriGame(PetriNet pn, boolean skipChecks) throws UnboundedPGException, NetNotSafeException, NoSuitableDistributionFoundException {
        super(pn, skipChecks);
        if (!skipChecks) {
            if (!super.getBounded().isSafe()) {
                throw new NetNotSafeException(super.getBounded().unboundedPlace.toString(), super.getBounded().sequence.toString());
            }
        } else {
            Logger.getInstance().addMessage("Attention: You decided to skip the tests. We cannot ensure that the net is safe!", false);
        }

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.CONCURRENCY_PRESERVING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        ConcurrencyPreserving con = new ConcurrencyPreserving(pn);
        Logger.getInstance().addMessage("Check concurrency preserving ...");
        concurrencyPreserving = con.check();
        Logger.getInstance().addMessage("Concurrency preserving: " + concurrencyPreserving);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.CONCURRENCY_PRESERVING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        Logger.getInstance().addMessage("Buffer data ...");
        bufferData();
        Logger.getInstance().addMessage("... buffering of data done.");
    }

    // todo: proof that it's a suitable slicing, such that every environment place
    // belongs to one single invariant
    private void bufferData() throws NoSuitableDistributionFoundException {
        //todo:  all comments are old version, before cavarti
//        if (net.hasExtension("MAXTOKEN")) {
//            TOKENCOUNT = (Integer) net.getExtension("MAXTOKEN");
//            Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (read from net)");
//        } else
        if (concurrencyPreserving) {
            TOKENCOUNT = 0;
            Marking m = getNet().getInitialMarking();
            for (Place p : getNet().getPlaces()) {
                if (m.getToken(p).getValue() > 0) {
                    ++TOKENCOUNT;
                }
            }
            Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (number of initial token, since concurrency preserving)");
        } else {
            int maxToken = 0;
            for (Place place : getNet().getPlaces()) {
                if (place.hasExtension("token")) {
                    int token = (Integer) place.getExtension("token");
                    if (maxToken < token) {
                        maxToken = token;
                    }
                }
            }
            TOKENCOUNT = maxToken + 1;
            // todo  all comments are old version, before cavarti
            // todo: hack, since not extension of nets are saved to the files
//            for (Place place : net.getPlaces()) {
//                if (place.hasExtension("token")) {
//                    TOKENCOUNT = Integer.parseInt((String) place.getExtension("MAXTOKEN"));
//                    Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (read from file)");
//                    break;
//                }
//            }
//            if (TOKENCOUNT == -1) {
            if (maxToken == 0) {
                TOKENCOUNT = MaxiumNumberOfTokenCalculator.getMaximumNumberOfToken(getNet());
                Logger.getInstance().addMessage("Maximal number of token: " + TOKENCOUNT + " (through coverability graph)");
            }
        }

        // add suitable partition to the places (extension token)
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Partitioner.doIt(this);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.PARTITIONING);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS

        // split places and add an id
        int additional = (concurrencyPreserving) ? 0 : 1;
        places = new Set[TOKENCOUNT];
        for (Place place : getNet().getPlaces()) {
            int token = (Integer) place.getExtension("token");
            if (places[token] == null) {
                places[token] = new HashSet<>();
            }
            int add = additional;
            place.putExtension("id", places[token].size() + add);
            places[token].add(place);
        }

        // todo: test no two places in one group are marked at the same time        
//            if (!skip) {
//                CoverabilityGraph cv = CoverabilityGraph.get(net);
//                for (Iterator<CoverabilityGraphNode> it = cv.getNodes().iterator(); it.hasNext();) {
//                    CoverabilityGraphNode node = it.next();
//                    Marking m = node.getMarking();
//                    
//                }
//            }
        // Calculate the possible transitions for the places of a special token
        transitions = new List[TOKENCOUNT - 1];
        for (int i = 1; i < TOKENCOUNT; ++i) {
            transitions[i - 1] = new ArrayList<>();
            for (Place place : places[i]) {
                transitions[i - 1].addAll(place.getPostset());
            }
        }
    }

    public int getTOKENCOUNT() {
        return TOKENCOUNT;
    }

    public Set<Place>[] getPlaces() {
        return places;
    }

    public List<Transition>[] getTransitions() {
        return transitions;
    }

    public boolean isConcurrencyPreserving() {
        return concurrencyPreserving;
    }
}
