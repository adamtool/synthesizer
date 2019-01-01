package uniolunisaar.adam.ds.graph;

import uniol.apt.adt.extension.Extensible;

/**
 * Rudimentary class for a state of the finite graph.
 *
 * This class only provides an id of the state and the possible to add
 * extension to it.
 *
 * @author Manuel Gieseking
 */
public class State extends Extensible {

    private int id = -1;

    /**
     * Constructor.
     *
     * Initially the id is set to -1.
     */
    public State() {
    }

    /**
     * Sets the id of the state to the given id.
     *
     * @param id - the id to set for the state.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the id of this state.
     *
     * @return - the id of the state.
     */
    public int getId() {
        return id;
    }
}
