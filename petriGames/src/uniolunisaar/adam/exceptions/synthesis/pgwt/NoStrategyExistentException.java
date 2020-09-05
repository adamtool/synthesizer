package uniolunisaar.adam.exceptions.synthesis.pgwt;

/**
 * Exception stating that there isn't a deadlock-avoiding winning strategy of
 * the system players.
 *
 * This exception just overrides the 'getMessage' method of the class
 * 'Exception' for returning a specialized message.
 *
 * @author Manuel Gieseking
 */
public class NoStrategyExistentException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Overrides the method of the class 'Exception' to state that there isn't
     * any deadlock-avoiding winning strategy of the system players.
     * @return
     */
    @Override
    public String getMessage() {
        return "No deadlock-avoiding winning strategy of the system players is existent!";
    }

}
