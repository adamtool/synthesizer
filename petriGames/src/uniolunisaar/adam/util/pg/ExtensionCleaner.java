package uniolunisaar.adam.util.pg;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.IGraphListener;
import uniol.apt.adt.INode;

/**
 *
 *
 * @param <G>
 * @param <E>
 * @param <N>
 * @author Manuel Gieseking
 */
public class ExtensionCleaner<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
        implements IGraphListener<G, E, N> {

    private final String[] keys;

    /**
     * Constructor
     *
     * @param keys all keys which should be deleted by any structural changes of
     * the net
     */
    public ExtensionCleaner(String... keys) {
        this.keys = keys;
    }

    @Override
    public boolean changeOccurred(IGraph<G, E, N> graph) {
        for (String key : keys) {
            graph.removeExtension(key);
        }
        return false;
    }
}
