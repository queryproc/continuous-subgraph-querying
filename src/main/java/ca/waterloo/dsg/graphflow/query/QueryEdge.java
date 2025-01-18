package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.storage.Graph.Version;

import java.io.Serializable;

/**
 * Represents a query edge.
 */
public class QueryEdge implements Serializable {

    private Version version;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    private String fromVertex;

    public String getFromVertex() {
        return fromVertex;
    }

    private String toVertex;

    public String getToVertex() {
        return toVertex;
    }

    private short fromType = 0;

    public short getFromType() {
        return fromType;
    }

    public void setFromType(short fromType) {
        this.fromType = fromType;
    }

    private short toType = 0;

    public short getToType() {
        return toType;
    }

    public void setToType(short toType) {
        this.toType = toType;
    }

    private short label = 0;

    public short getLabel() {
        return label;
    }

    public void setLabel(short label) {
        this.label = label;
    }

    /**
     * Constructs a {@link QueryEdge} object.
     *
     * @param fromVertex is the from query vertex of the query edge.
     * @param toVertex is the to query vertex of the query edge.
     * @param fromType is the from query vertex type.
     * @param toType is the to query vertex type.
     * @param label is the query edge label.
     */
    public QueryEdge(String fromVertex, String toVertex, short fromType, short toType,
        short label) {
        this(fromVertex, toVertex);
        this.fromType = fromType;
        this.toType = toType;
        this.label = label;
    }

    /**
     * Constructs a {@link QueryEdge} object.
     *
     * @param fromVertex is the from query vertex of the query edge.
     * @param toVertex is the to query vertex of the query edge.
     */
    public QueryEdge(String fromVertex, String toVertex) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    public QueryEdge copy() {
        return new QueryEdge(fromVertex, toVertex, fromType, toType, label);
    }
}
