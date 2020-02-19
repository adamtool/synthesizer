package uniolunisaar.adam.ds.petrigame;

import uniolunisaar.adam.ds.petrinet.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 */
public interface IPetriGame {

    public void initializeWinningCondition(Condition<? extends Condition<?>> winCon);
}
