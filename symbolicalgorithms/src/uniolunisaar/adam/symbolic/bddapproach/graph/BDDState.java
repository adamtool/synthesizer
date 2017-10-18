package uniolunisaar.adam.symbolic.bddapproach.graph;

import java.util.Objects;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.graph.State;

/**
 *
 * @author Manuel Gieseking
 */
public class BDDState extends State {

    private final BDD state;
    private boolean mcut;
    private int distance;
    private boolean good;
    private boolean bad;
    private String content;

    public BDDState(BDD state) {
        this(state, -1, "");
    }

    public BDDState(BDD state, int distance, String content) {
        this.state = state;
        this.distance = distance;
        this.content = content;
    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + Objects.hashCode(this.state);
//     //   hash = 29 * hash + (this.mcut ? 1 : 0);
//        return hash;
//    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + getId();
        hash = 97 * hash + Objects.hashCode(this.state);
        return hash;
    }
//    
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final GraphState other = (GraphState) obj;
//        if (!Objects.equals(this.state, other.state)) {
//            return false;
//        }
////        if (this.mcut != other.mcut) {
////            return false;
////        }
//        return true;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BDDState other = (BDDState) obj;
        if (getId() != other.getId()) {
            return false;
        }
        return Objects.equals(this.state, other.state);
    }

    public BDD getState() {
        return state;
    }

    public boolean isMcut() {
        return mcut;
    }

    public void setMcut(boolean mcut) {
        this.mcut = mcut;
    }

    public boolean isEqualTo(BDD succ) {
        return Objects.equals(this.state, succ);
    }

    public boolean isBad() {
        return bad;
    }

    public void setBad(boolean bad) {
        this.bad = bad;
    }

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }

    public int getDistance() {
        return distance;
    }

    public String getContent() {
        return content;
    }

}
