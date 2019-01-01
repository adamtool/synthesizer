package uniolunisaar.adam.ds.solver;

import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <W>
 */
public abstract class SolvingObject<G extends PetriGame, W extends Condition> {

    private final G game;
    private final W winCon;

    public SolvingObject(G game, W winCon) {
        this.game = game;
        this.winCon = winCon;
        winCon.buffer(game);
    }

    public G getGame() {
        return game;
    }

    public W getWinCon() {
        return winCon;
    }

    public abstract <SO extends SolvingObject<G, W>> SO getCopy();

}
