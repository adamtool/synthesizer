package uniolunisaar.adam.util.pg;

import java.util.Collections;
import java.util.Set;
import uniol.apt.adt.extension.ExtensionProperty;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 * @param <O>
 */
public abstract class ExtensionCalculator<O> {

    private final String key;

    public ExtensionCalculator(String key) {
        this.key = key;
    }
    
    protected Set<ExtensionProperty> prop = Collections.emptySet();

    public abstract O calculate(PetriGame game);

    public Set<ExtensionProperty> getProperties() {
        return prop;
    }

    public String getKey() {
        return key;
    }
}
