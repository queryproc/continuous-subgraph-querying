package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.planner.Catalog.operator.IntersectCatalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.Graph.Direction;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * A base operator for {@link Extend} and {@link Intersect} operators.
 */
public abstract class EI extends Operator {

    /**
     * Enum type of the caching used.
     */
    public enum CachingType {
        NONE,
        FULL_CACHING,
        PARTIAL_CACHING
    }

    /**
     * A slice of an adjacency list matching an edge label.
     */
    public static class AdjListSlice {
        public int[] Ids;
        public int startIdx, endIdx;

        public void reset() {
            startIdx = 0;
            endIdx = 0;
        }
    }

    protected Graph graph;

    protected String toVertex;
    protected short toType;
    protected List<AdjListDescriptor> ALDs;

    protected CachingType cachingType = CachingType.NONE;
    transient boolean isIntersectionCached;
    transient int[] lastVertexIdsIntersected;

    protected int outIdx;

    public String getToVertex() {
        return toVertex;
    }

    public short getToType() {
        return toType;
    }

    public List<AdjListDescriptor> getALDs() {
        return ALDs;
    }

    public CachingType getCachingType() {
        return cachingType;
    }

    public boolean isIntersectionCached() {
        return isIntersectionCached;
    }

    public void setOutIdx(int outIdx) {
        this.outIdx = outIdx;
    }

    /**
     * Constructs an {@link EI} operator.
     *
     * @param toVertex is the query vertex the operator extends to.
     * @param ALDs are the {@link AdjListDescriptor}s the prefixes extended to need to follow.
     * @param outSubgraph is the subgraph matched by the output tuples.
     */
    public static EI make(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph, boolean isForCatalog) {
        if (1 == ALDs.size()) {
            return new Extend(toVertex, ALDs, outSubgraph, inSubgraph);
        } else if (isForCatalog) {
            return new IntersectCatalog(toVertex, ALDs, outSubgraph);
        } else {
            return new Intersect(toVertex, ALDs, outSubgraph, inSubgraph);
        }
    }

    /**
     * Constructs a {@link EI} object.
     *
     * @param toVertex is the query vertex the operator extends to.
     * @param ALDs are the adjacency list descriptors representing the adj. lists from the
     * variables in the matched subgraph to the toVertex.
     * @param outSubgraph is the subgraph matched by the output tuples.
     * @param inSubgraph is the subgraph matched by the input tuples.
     */
    public EI(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(outSubgraph, inSubgraph);
        this.ALDs = ALDs;
        this.toVertex = toVertex;
        this.toType = outSubgraph.getVertexType(toVertex);
        this.outIdx = outSubgraph.getNumVertices() - 1;
        setOperatorName();
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        this.tuple = tuple;
        this.graph = graph;
        if (store.getNextTypeKey() == 0) {
            this.toType = KeyStore.ANY;
        }
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

    void intersect(int[] neighbourIds, int idx, int endIdx, int[] otherNeighbourIds,
        int otherIdx, int otherEndIdx, int[] outNeighbourIds, AdjListSlice outSlice) {
        outSlice.reset();
        while (idx < endIdx && otherIdx < otherEndIdx) {
            if (neighbourIds[idx] < otherNeighbourIds[otherIdx]) {
                do idx++;
                while (idx < endIdx &&
                    neighbourIds[idx] < otherNeighbourIds[otherIdx]);
            } else if (neighbourIds[idx] > otherNeighbourIds[otherIdx]) {
                do otherIdx++;
                while (otherIdx < otherEndIdx &&
                    neighbourIds[idx] > otherNeighbourIds[otherIdx]);
            } else {
                outNeighbourIds[outSlice.endIdx] = neighbourIds[idx];
                outSlice.endIdx++;
                idx++;
                otherIdx++;
            }
        }
    }

    void copyNextOperators(EI operator) {
        if (null != next) {
            var nextOperators = new Operator[next.length];
            for (var i = 0; i < nextOperators.length; i++) {
                nextOperators[i] = next[i].copy();
                nextOperators[i].setPrev(operator);
            }
            operator.next = nextOperators;
        }
    }

    void copyNextIntersectRemainingOperators(EI operator) {
        if (null != EIRemaining) {
            var nextOperators = new Operator[EIRemaining.length];
            for (var i = 0; i < nextOperators.length; i++) {
                nextOperators[i] = EIRemaining[i].copy();
                nextOperators[i].setPrev(operator);
            }
            operator.setNext(nextOperators);
        }
    }

    @Override
    public void addLastOperators(List<Operator> lastOperators) {
        if (numTimesAsFinalOperator != 0) {
            lastOperators.add(this);
        }
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.addLastOperators(lastOperators);
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.addLastOperators(lastOperators);
            }
        }
    }

    @Override
    public void setALDsFromPos(Map<String, Integer> vertexToIdxMap) {
        for (var ALD : ALDs) {
            var fromVertex = ALD.getFromVertex();
            var idx = vertexToIdxMap.get(fromVertex);
            ALD.setFromPos(idx);
        }
        vertexToIdxMap.put(toVertex, outSubgraph.getNumVertices() - 1);
        super.setALDsFromPos(vertexToIdxMap);
    }

    @Override
    public double getExpectedTotalICost(Catalog catalog, Map<Integer, Double> idxToNumOutTuples) {
        var iCostSum = getExpectedICostConsideringCaching(catalog, idxToNumOutTuples);
        idxToNumOutTuples.put(outTupleLen - 1, expectedNumOutTuples);
        if (null != next) {
            for (var nextOperator : next) {
                iCostSum += nextOperator.getExpectedTotalICost();
            }
        }
        return iCostSum;
    }

    @Override
    public void setEstimatedICostAndSelectivity(Catalog catalog,
        Map<Integer, Double> fromPosToNumOutTuples) {
        var prevExpectedNumOutTuples = fromPosToNumOutTuples.get(outTupleLen - 2);
        expectedNumOutTuples = prevExpectedNumOutTuples * catalog.getSelectivity(outSubgraph);
        fromPosToNumOutTuples.put(outTupleLen - 1, expectedNumOutTuples);
        expectedICost = getExpectedICostConsideringCaching(catalog, fromPosToNumOutTuples);
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.setEstimatedICostAndSelectivity(catalog, fromPosToNumOutTuples);
            }
        }
    }

    double getExpectedICostConsideringCaching(Catalog catalog,
        Map<Integer, Double> fromIdxToNumOutTuples) {
        var prevExpectedNumOutTuples = fromIdxToNumOutTuples.get(outTupleLen - 2);
        initCachingType();
        double expectedICost;
        if (cachingType == CachingType.NONE) {
            expectedICost = prevExpectedNumOutTuples * catalog.getICost(inSubgraph, ALDs);
        } else {
            var ALDsToCache = ALDs.stream()
                .filter(ALD -> ALD.getFromPos() <= lastRepeatedVertexIdx)
                .collect(Collectors.toList());
            var lastCachedEstimatedNumOutTuples = 1.0;
            for (var ALD : ALDsToCache) {
                var fromPos = ALD.getFromPos();
                var newLastCachedEstimatedNumOutTuples = fromIdxToNumOutTuples.get(fromPos);
                if (newLastCachedEstimatedNumOutTuples > lastCachedEstimatedNumOutTuples) {
                    lastCachedEstimatedNumOutTuples = newLastCachedEstimatedNumOutTuples;
                }
            }
            var outTuplesToProcess = prevExpectedNumOutTuples / lastCachedEstimatedNumOutTuples;
            expectedICost = outTuplesToProcess * catalog.getICost(inSubgraph, ALDsToCache);
            if (cachingType == CachingType.PARTIAL_CACHING) {
                var ALDsNotCached = ALDs
                    .stream()
                    .filter(ALD -> ALD.getFromPos() > prev.getLastRepeatedVertexIdx())
                    .collect(Collectors.toList());
                var icostPerTuple = catalog.getICost(inSubgraph, ALDsNotCached);
                expectedICost += (prevExpectedNumOutTuples * icostPerTuple);
            }
            // added to make caching effect on cost more robust.
            /* var leftOutTuplesToProcess = prevExpectedNumOutTuples - outTuplesToProcess;
               expectedICost += (expectedNumOutTuples * leftOutTuplesToProcess);           */
        }
        return expectedICost;
    }

    @Override
    public void setIdxToNumOutTuples(Map<Integer, Double> idxToNumOutTuples, Catalog catalog) {
        prev.setIdxToNumOutTuples(idxToNumOutTuples, catalog);
        var prevExpectedNumOutTuples = idxToNumOutTuples.get(outTupleLen - 2);
        expectedNumOutTuples = prevExpectedNumOutTuples * catalog.getSelectivity(outSubgraph);
        idxToNumOutTuples.put(outTupleLen - 1, expectedNumOutTuples);
    }

    public int getPrevLastRepeatedVertexIdx() {
        return prev == null ? 0 /* Scan */ : prev.getLastRepeatedVertexIdx();
    }

    /**
     * Initializes the cache related fields to be used by the operator i.e. key and ALD indices.
     */
    void initCachingType() {
        cachingType = CachingType.NONE;
        if (!CACHING_ENABLED) {
            return;
        }
        var numCachedALDs = 0;
        for (var ALD : ALDs) {
            if (ALD.getFromPos() <= getPrevLastRepeatedVertexIdx()) {
                numCachedALDs++;
            }
        }
        lastVertexIdsIntersected = new int[numCachedALDs];
        Arrays.fill(lastVertexIdsIntersected, Integer.MIN_VALUE);
        if (numCachedALDs > 1) {
            cachingType = numCachedALDs == ALDs.size() ?
                CachingType.FULL_CACHING : CachingType.PARTIAL_CACHING;
        }
    }

    private void setOperatorName() {
        var vertices = new String[ALDs.size()];
        for (int i = 0; i < ALDs.size(); i++) {
            vertices[i] = ALDs.get(i).getFromVertex() + "[" +
                (ALDs.get(i).getDirection() == Direction.FWD ? "FWD" : "BWD") + "]";
        }
        Arrays.sort(vertices);
        var strJoiner = new StringJoiner("-");
        for (var vertex : vertices) {
            strJoiner.add(vertex);
        }
        name = "Extend TO (" + toVertex + ") From (" + strJoiner.toString() + ")";
    }
}
