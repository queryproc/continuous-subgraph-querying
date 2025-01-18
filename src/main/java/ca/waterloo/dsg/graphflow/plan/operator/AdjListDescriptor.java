package ca.waterloo.dsg.graphflow.plan.operator;

import ca.waterloo.dsg.graphflow.storage.Graph.Direction;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;

import java.io.Serializable;

/**
 * An adjacency list descriptor consists of the following:
 * (1) An edge label.
 * (2) A graph version.
 * (3) A from variable indicating the vertex in the query graph that is being extended from.
 * (4) An index indicating the vertex value position in the processing from which we extend.
 * (5) A direction which indicates whether to extend from fwd or bwd adj list.
 */
public class AdjListDescriptor implements Serializable {

    private short label;
    private Version version;
    private String fromVertex;
    private Direction direction;
    private int fromPos = -1;

    public String getFromVertex() {
        return fromVertex;
    }

    public void setFromPos(int fromPos) {
        this.fromPos = fromPos;
    }

    public int getFromPos() {
        return fromPos;
    }

    public Direction getDirection() {
        return direction;
    }

    public short getLabel() {
        return label;
    }

    public Version getVersion() {
        return version;
    }

    /**
     * Constructs an {@link AdjListDescriptor} object.
     *
     * @param fromVertex is the from variable to extend from.
     * @param direction is the direction of extension.
     * @param label is the edge label.
     * @param version is the edge version.
     */
    public AdjListDescriptor(String fromVertex, Direction direction, short label, Version version) {
        this.label = label;
        this.version = version;
        this.direction = direction;
        this.fromVertex = fromVertex;
    }

    @Override
    public String toString() {
        return "(" + fromVertex + ") " + direction.name() + "[" + label + "]";
    }
}
