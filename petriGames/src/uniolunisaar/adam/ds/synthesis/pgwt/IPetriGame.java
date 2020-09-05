package uniolunisaar.adam.ds.synthesis.pgwt;

import uniolunisaar.adam.ds.objectives.Condition;

/**
 *
 * @author Manuel Gieseking
 */
public interface IPetriGame {

    public void initializeWinningCondition(Condition<? extends Condition<?>> winCon);
}
