package uniolunisaar.adam.util.pgwt;

import java.util.Collections;
import java.util.Set;
import uniol.apt.adt.extension.ExtensionProperty;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

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

    public abstract O calculate(PetriGameWithTransits game);

    public Set<ExtensionProperty> getProperties() {
        return prop;
    }

    public String getKey() {
        return key;
    }
}
