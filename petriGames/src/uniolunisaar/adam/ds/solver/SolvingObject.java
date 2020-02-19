package uniolunisaar.adam.ds.solver;

import uniolunisaar.adam.ds.petrigame.IPetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <W>
 */
public abstract class SolvingObject<G extends IPetriGame, W extends Condition<W>> {

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

    public abstract <SO extends SolvingObject<G, W>> SO getCopy();

}
