package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.Graph.Direction;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.collection.MapUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Given input tuples from the prev {@link Operator}, Extend extends the tuples by one vertex.
 */
public class Extend extends EI implements Serializable {

    private short label;
    private int vertexIndex;
    private Direction direction;
    private Version version;
    private transient AdjListSlice adjListSlice;

    /**
     * @see EI#make(String, List, QueryGraph, QueryGraph, boolean)
     */
    Extend(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(toVertex, ALDs, outSubgraph, inSubgraph);
        var ALD = ALDs.get(0);
        this.label = ALD.getLabel();
        this.version = ALD.getVersion();
        this.direction = ALD.getDirection();
        this.vertexIndex = ALD.getFromPos();
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        super.init(tuple, graph, store);
        this.adjListSlice = new AdjListSlice();
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.init(tuple, graph, store);
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.init(tuple, graph, store);
            }
        }
    }

    @Override
    public void setALDsFromPos(Map<String, Integer> vertexToIdxMap) {
        super.setALDsFromPos(vertexToIdxMap);
        this.vertexIndex = ALDs.get(0).getFromPos();
    }

    /**
     * @see Operator#processNewTuple()
     */
    @Override
    @SuppressWarnings("fallthrough")
    public void processNewTuple() {
        var vertexID = tuple[vertexIndex];
        graph.getAdjList(vertexID, direction, version).setNeighbourIds(label, adjListSlice);
        ICost += (adjListSlice.endIdx - adjListSlice.startIdx);
        numOutTuples += (adjListSlice.endIdx - adjListSlice.startIdx);
        for (var idx = adjListSlice.startIdx; idx < adjListSlice.endIdx; idx++) {
            tuple[outIdx] = adjListSlice.Ids[idx];
            if (null != next) {
                for (var nextOperator : next) {
                    nextOperator.processNewTuple();
                }
            }
        }
    }

    @Override
    public Operator copy() {
        var operator = new Extend(toVertex, ALDs, outSubgraph, inSubgraph);
        copyNextOperators(operator);
        operator.vertexToIdxMap = MapUtils.copy(vertexToIdxMap);
        operator.expectedNumOutTuples = expectedNumOutTuples;
        operator.expectedICost = expectedICost;
        operator.numTimesAsFinalOperator = numTimesAsFinalOperator;
        return operator;
    }

    @Override
    public void addStatsToCatalog(Catalog catalog) {
        var numInputTuples = (double) prev.getNumOutTuples();
        var iCostPerInputTuple = numInputTuples != 0 ? ICost / numInputTuples : 0.0;
        var selectivityPerInputTuple = numInputTuples != 0 ? numOutTuples / numInputTuples : 0.0;
        catalog.addICost(prev.getOutSubgraph(), ALDs, new double[] { iCostPerInputTuple });
        catalog.addSelectivity(outSubgraph, selectivityPerInputTuple);
        super.addStatsToCatalog(catalog);
    }
}
