package uniolunisaar.adam.logic.synthesis.pgwt.calculators;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.util.pgwt.ExtensionCalculator;
import uniolunisaar.adam.util.MaxiumNumberOfTokenCalculator;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class MaxTokenCountCalculator extends ExtensionCalculator<Long> {

    private long manuallyFixedTokencount = -1;

    public MaxTokenCountCalculator() {
        super(CalculatorIDs.MAX_TOKEN_COUNT.name());
    }

    @Override
    public Long calculate(PetriGameWithTransits game) {
        if (manuallyFixedTokencount != -1) {
            return manuallyFixedTokencount;
        }
        boolean concurrencyPreserving = game.getValue(CalculatorIDs.CONCURRENCY_PRESERVING.name());
        long tokencount = -1;
        // if concurrency preserving, then maxtokencount = nb of inital token
        if (concurrencyPreserving) {
            tokencount = 0;
            boolean hasEnv = false;
            Marking m = game.getInitialMarking();
            for (Place p : game.getPlaces()) {
                long val = m.getToken(p).getValue();
                if (val > 0) {
                    tokencount += val;
                }
                if (game.isEnvironment(p)) {
                    hasEnv = true;
                }
            }
            if (!hasEnv) { // todo: too hacky for no env token?
                tokencount++;
                Logger.getInstance().addWarning("We added +1 to the number of token, since there is no environment token.");
            }
            Logger.getInstance().addMessage("Maximal number of token: " + tokencount + " (number of initial token, since concurrency preserving)");
        } else {
            // if places are annotated with token numbers, then maxtokencount = max nb of annotated token nb + 1
            long maxToken = 0;
            for (Place place : game.getPlaces()) {
                if (game.hasPartition(place)) {
                    long token = game.getPartition(place);
                    if (maxToken < token) {
                        maxToken = token;
                    }
                }
            }
            tokencount = maxToken + 1;
            // if nothing is annotated get the maximum nb of token through the reachability graph
            if (maxToken == 0) {
                tokencount = MaxiumNumberOfTokenCalculator.getMaximumNumberOfToken(game);
                boolean hasEnv = false;// todo: too hacky for no env token?
                for (Place p : game.getPlaces()) {
                    if (game.isEnvironment(p)) {
                        hasEnv = true;
                    }
                }
                if (!hasEnv) {
                    tokencount++;
                    Logger.getInstance().addWarning("We added +1 to the number of token, since there is no environment token.");
                }
                Logger.getInstance().addMessage("Maximal number of token: " + tokencount + " (through coverability graph)");
            }
        }
        if (tokencount == 0) {
            Logger.getInstance().addWarning("To get the reasonable result we say that there is a token even though there is no token.");
            tokencount++;
        }
        return tokencount;
    }

    public void setManuallyFixedTokencount(PetriGameWithTransits game, long manuallyFixedTokencount) {
        this.manuallyFixedTokencount = manuallyFixedTokencount;
        game.recalculate(getKey());
    }
}
