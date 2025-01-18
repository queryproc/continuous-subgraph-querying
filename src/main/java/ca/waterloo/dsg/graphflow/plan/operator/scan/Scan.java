package ca.waterloo.dsg.graphflow.plan.operator.scan;

import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.collection.MapUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Scans all edges in the forward adjacency list given an edge label, a source vertex type, and a
 * destination source type. Scanned edge are pushed to the next operators one at a time.
 */
public class Scan extends Operator implements Serializable {

    protected String fromVertex, toVertex;
    short fromType, toType, label;
    private int[] deltaEdges, deltaSizes;

    /**
     * Constructs a {@link Scan} operator.
     *
     * @param outSubgraph is the subgraph matched by the scanned output tuples.
     */
    public Scan(QueryGraph outSubgraph) {
        super(outSubgraph, null /* inSubgraph */);
        var queryEdge = outSubgraph.getEdges().get(0);
        fromType = queryEdge.getFromType();
        toType = queryEdge.getToType();
        label = queryEdge.getLabel();
        fromVertex = queryEdge.getFromVertex();
        toVertex = queryEdge.getToVertex();
        name = "SCAN (" + fromVertex + ")->(" + toVertex + ")";
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        this.tuple = tuple;
        this.deltaEdges = graph.getDeltaEdges()[label];
        this.deltaSizes = graph.getDeltaSizes();
        for (var nextOperator : next) {
            nextOperator.init(tuple, graph, store);
        }
    }

    /**
     * @see Operator#execute()
     */
    @Override
    public void execute() {
        for (var i = 0; i < deltaSizes[label]; i++) {
            tuple[0] = deltaEdges[i * 2];
            tuple[1] = deltaEdges[(i * 2)+ 1];
            numOutTuples++;
            for (var nextOperator : next) {
                nextOperator.processNewTuple();
            }
        }
    }

    @Override
    public void setIdxToNumOutTuples(Map<Integer, Double> idxToNumOutTuples, Catalog catalog) {
        expectedNumOutTuples = (double) Graph.EDGE_BATCH_SIZE;
        idxToNumOutTuples.put(0, expectedNumOutTuples);
        idxToNumOutTuples.put(1, expectedNumOutTuples);
    }

    public double getExpectedTotalICost() {
        var iCostSum = expectedICost;
        if (null != next) {
            for (var nextOperator : next) {
                iCostSum += nextOperator.getExpectedTotalICost();
            }
        }
        return iCostSum;
    }

    public Scan copy() {
        var scan = new Scan(outSubgraph);
        var nextOperators = new Operator[next.length];
        for (var i = 0; i < nextOperators.length; i++) {
            nextOperators[i] = next[i].copy();
            nextOperators[i].setPrev(scan);
        }
        scan.next = nextOperators;
        scan.vertexToIdxMap = MapUtils.copy(vertexToIdxMap);
        scan.expectedNumOutTuples = expectedNumOutTuples;
        scan.lastRepeatedVertexIdx = lastRepeatedVertexIdx;
        return scan;
    }
}
