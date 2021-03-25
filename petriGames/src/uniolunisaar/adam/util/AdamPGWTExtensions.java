package uniolunisaar.adam.util;

/**
 * This enum contains all by the pgwt submodule used keys for saving object via
 * the Extensible interface of APT
 *
 * @author Manuel Gieseking
 */
public enum AdamPGWTExtensions implements IAdamExtensions {
    condition,
    winningCondition, // todo: this is only for the fallback to the just-sythesis-version. 
    env,
    t,
    strat_t,
    special
}
