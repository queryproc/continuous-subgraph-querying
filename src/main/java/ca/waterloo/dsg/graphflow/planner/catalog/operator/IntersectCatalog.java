package ca.waterloo.dsg.graphflow.planner.Catalog.operator;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.plan.operator.extend.EI;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.collection.MapUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Given input tuples from a prev {@link Operator}, Intersect extends the tuples by one vertex.
 */
public class IntersectCatalog extends EI implements Serializable {

    // Used to return output of the intersection for vertex & edge Ids.
    private transient int[] outNeighbourIds;
    private transient AdjListSlice outSlice;
    private transient int[] tempNeighbourIds;    /* used when intersecting. */
    private double[] ICostValues;

    public IntersectCatalog(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph) {
        super(toVertex, ALDs, outSubgraph, null /* inSubgraph */);
        ICostValues = new double[ALDs.size()];
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        super.init(tuple, graph, store);
        allocateBuffers(getLargestAdjListSize(ALDs));
    }

    /**
     * @see Operator#processNewTuple()
     */
    @Override
    public void processNewTuple() {
        var ALD = ALDs.get(0);
        var ID = tuple[ALD.getFromPos()];
        var adjList = graph.getAdjList(ID, ALD.getDirection(), ALD.getVersion());
        adjList.setNeighbourIds(ALD.getLabel(), outSlice);
        ICostValues[0] += (outSlice.endIdx - outSlice.startIdx);
        ALD = ALDs.get(1);
        ID = tuple[ALD.getFromPos()];
        ICostValues[1] += graph.getAdjList(ID, ALD.getDirection(), ALD.getVersion()).intersect(
            adjList.getNeighbourIds(), outSlice.startIdx, outSlice.endIdx, ALD.getLabel(),
                outNeighbourIds, outSlice);
        for (var i = 2; i < ALDs.size(); i++) {
            var temp = outNeighbourIds;
            outNeighbourIds = tempNeighbourIds;
            tempNeighbourIds = temp;
            ALD = ALDs.get(i);
            ID = tuple[ALD.getFromPos()];
            ICostValues[i] += graph.getAdjList(ID, ALD.getDirection(), ALD.getVersion()).intersect(
                tempNeighbourIds, outSlice.startIdx, outSlice.endIdx, ALD.getLabel(),
                    outNeighbourIds, outSlice);
        }
        numOutTuples += (outSlice.endIdx - outSlice.startIdx);
        for (var idx = outSlice.startIdx; idx < outSlice.endIdx; idx++) {
            tuple[outIdx] = outNeighbourIds[idx];
            if (null != next) {
                for (var nextOperator : next) {
                    nextOperator.processNewTuple();
                }
            }
        }
    }

    @Override
    public void addStatsToCatalog(Catalog catalog) {
        var numInputTuples = (double) prev.getNumOutTuples();
        for (var i = 0; i < ICostValues.length; i++) {
            ICostValues[i] = numInputTuples != 0 ? ICostValues[i] / numInputTuples : 0.0;
        }
        var selectivityPerInputTuple = numInputTuples != 0 ? numOutTuples / numInputTuples : 0.0;
        catalog.addICost(prev.getOutSubgraph(), ALDs, ICostValues);
        catalog.addSelectivity(outSubgraph, selectivityPerInputTuple);
        super.addStatsToCatalog(catalog);
    }

    /**
     * Initializes the extension data structured used when intersecting.
     */
    private void allocateBuffers(int largestAdjListSize) {
        outNeighbourIds = new int[largestAdjListSize];
        outSlice = new AdjListSlice();
        if (ALDs.size() > 2) {
            tempNeighbourIds = new int[largestAdjListSize];
        }
    }

    private int getLargestAdjListSize(List<AdjListDescriptor> ALDs) {
        var largestAdjListSize = 0;
        for (var ALD : ALDs) {
            var adjListSize = graph.getLargestAdjListSize(ALD.getLabel(), ALD.getDirection());
            if (adjListSize > largestAdjListSize) {
                largestAdjListSize = adjListSize;
            }
        }
        return largestAdjListSize;
    }

    public Operator copy() {
        var intersectCopy = new IntersectCatalog(toVertex, ALDs, outSubgraph);
        if (null != next) {
            var nextOperators = new Operator[next.length];
            for (var i = 0; i < nextOperators.length; i++) {
                nextOperators[i] = next[i].copy();
                nextOperators[i].setPrev(intersectCopy);
            }
            intersectCopy.next = nextOperators;
        }
        intersectCopy.vertexToIdxMap = MapUtils.copy(vertexToIdxMap);
        intersectCopy.expectedNumOutTuples = expectedNumOutTuples;
        intersectCopy.expectedICost = expectedICost;
        return intersectCopy;
    }
}
