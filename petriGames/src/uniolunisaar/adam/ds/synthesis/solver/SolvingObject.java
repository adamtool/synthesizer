package uniolunisaar.adam.ds.synthesis.solver;

import uniolunisaar.adam.ds.synthesis.pgwt.IPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <W>
 * @param <SO>
 */
public abstract class SolvingObject<G extends IPetriGame, W extends Condition<W>, SO extends SolvingObject<G, W, SO>> {

    private final G game;
    private final W winCon;

    public SolvingObject(G game, W winCon) {
        this.game = game;
        this.winCon = winCon;
        game.initializeWinningCondition(winCon);
    }

    public G getGame() {
        return game;
    }

    public W getWinCon() {
        return winCon;
    }

    public abstract SO getCopy();

}
