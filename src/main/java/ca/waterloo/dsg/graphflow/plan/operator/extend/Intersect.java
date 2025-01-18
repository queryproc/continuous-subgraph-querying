package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.Graph.Direction;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.collection.MapUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Given input tuples from a prev {@link Operator}, Intersect extends the tuples by one vertex.
 */
public class Intersect extends EI implements Serializable {

    transient int[] fromPosWhenCaching;
    transient short[] labelWhenCaching;
    transient Version[] versionWhenCaching;
    transient Direction[] directionWhenCaching;

    transient int[] fromPosAfterCaching;
    transient short[] labelAfterCaching;
    transient Version[] versionAfterCaching;
    transient Direction[] directionAfterCaching;

    // Used to return output of the intersection for vertex & edge Ids.
    transient int[] outNeighbourIds;
    transient AdjListSlice outSlice;

    // Used to hold intermediate output of the intersection for vertex & edge Ids.
    transient int[] tempNeighbourIds;    /* used when intersecting.             */
    transient int[] cachedNeighbourIds;  /* used to cache intersection results. */
    transient AdjListSlice cachedSlice;

    Intersect(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(toVertex, ALDs, outSubgraph, inSubgraph);
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        super.init(tuple, graph, store);
        initCachingType();
        initALDsAndIndexes();
        allocateBuffers(getLargestAdjListSize(ALDs));
    }

    /**
     * @see Operator#processNewTuple()
     */
    @Override
    public void processNewTuple() {
        intersectAdjacencyLists();
        pushOutputTuplesToNextOperators(outNeighbourIds, outSlice);
    }

    void pushOutputTuplesToNextOperators(int[] outNeighbourIds, AdjListSlice outSlice) {
        numOutTuples += (outSlice.endIdx - outSlice.startIdx);
        for (var idx = outSlice.startIdx; idx < outSlice.endIdx; idx++) {
            tuple[outIdx] = outNeighbourIds[idx];
            if (null != next) {
                for (var nextOperator : next) {
                    nextOperator.processNewTuple();
                }
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.processNewTuple(outNeighbourIds, outSlice);
            }
        }
    }

    void intersectAdjacencyLists() {
        if (cachingType == CachingType.NONE || !isLastIntersectionCached()) {
            var ID = tuple[fromPosWhenCaching[0]];
            var adjList = graph.getAdjList(ID, directionWhenCaching[0], versionWhenCaching[0]);
            adjList.setNeighbourIds(labelWhenCaching[0], cachedSlice);
            ICost += (cachedSlice.endIdx - cachedSlice.startIdx);
            ID = tuple[fromPosWhenCaching[1]];
            ICost += graph.getAdjList(ID, directionWhenCaching[1], versionWhenCaching[1]).
                intersect(cachedSlice.Ids, cachedSlice.startIdx, cachedSlice.endIdx,
                    labelWhenCaching[1], cachedNeighbourIds, cachedSlice);
            for (var i = 2; i < fromPosWhenCaching.length; i++) {
                var temp = cachedNeighbourIds;
                cachedNeighbourIds = tempNeighbourIds;
                tempNeighbourIds = temp;
                ID = tuple[fromPosWhenCaching[i]];
                ICost += graph.getAdjList(ID, directionWhenCaching[i], versionWhenCaching[i]).
                    intersect(tempNeighbourIds, cachedSlice.startIdx, cachedSlice.endIdx,
                        labelWhenCaching[i], cachedNeighbourIds, cachedSlice);
            }
        }
        switch (cachingType) {
            case NONE:
            case FULL_CACHING:
                outNeighbourIds = cachedNeighbourIds;
                outSlice = cachedSlice;
                break;
            case PARTIAL_CACHING:
                var ID = tuple[fromPosAfterCaching[0]];
                ICost += graph.getAdjList(ID, directionAfterCaching[0], versionAfterCaching[0]).
                    intersect(cachedNeighbourIds, cachedSlice.startIdx, cachedSlice.endIdx,
                        labelAfterCaching[0], outNeighbourIds, outSlice);
                for (var i = 1; i < fromPosAfterCaching.length; i++) {
                    var temp = outNeighbourIds;
                    outNeighbourIds = tempNeighbourIds;
                    tempNeighbourIds = temp;
                    ID = tuple[fromPosAfterCaching[i]];
                    ICost += graph.getAdjList(ID, directionAfterCaching[i], versionAfterCaching[i]).
                        intersect(tempNeighbourIds, outSlice.startIdx, outSlice.endIdx,
                            labelAfterCaching[i], outNeighbourIds, outSlice);
                }
                break;
        }
    }

    @Override
    public void resetCache() {
        if (null != lastVertexIdsIntersected) {
            Arrays.fill(lastVertexIdsIntersected, Integer.MIN_VALUE);
        }
        super.resetCache();
    }

    /**
     * Initializes the extension data structured used when intersecting.
     */
    void allocateBuffers(int largestAdjListSize) {
        cachedNeighbourIds = new int[largestAdjListSize];
        if (cachingType == CachingType.PARTIAL_CACHING) {
            outNeighbourIds = new int[largestAdjListSize];
        }
        if (ALDs.size() >= 2) {
            tempNeighbourIds = new int[largestAdjListSize];
        }
    }

    /**
     * Sets the sorted adjacency lists to intersect for faster lookups.
     */
    private void initALDsAndIndexes() {
        var numCachedALDs =
            lastVertexIdsIntersected != null && lastVertexIdsIntersected.length > 1 ?
            lastVertexIdsIntersected.length : ALDs.size();
        fromPosWhenCaching = new int[numCachedALDs];
        labelWhenCaching = new short[numCachedALDs];
        versionWhenCaching = new Version[numCachedALDs];
        directionWhenCaching = new Direction[numCachedALDs];
        if (cachingType == CachingType.PARTIAL_CACHING) {
            fromPosAfterCaching = new int[ALDs.size() - numCachedALDs];
            labelAfterCaching = new short[ALDs.size() - numCachedALDs];
            versionAfterCaching = new Version[ALDs.size() - numCachedALDs];
            directionAfterCaching = new Direction[ALDs.size() - numCachedALDs];
        }
        var idx = 0;
        var idxToCache = 0;
        for (var ALD : ALDs) {
            if (cachingType != CachingType.PARTIAL_CACHING ||
                    ALD.getFromPos() <= prev.getLastRepeatedVertexIdx()) {
                fromPosWhenCaching[idxToCache] = ALD.getFromPos();
                labelWhenCaching[idxToCache] = ALD.getLabel();
                versionWhenCaching[idxToCache] = ALD.getVersion();
                directionWhenCaching[idxToCache++] = ALD.getDirection();
            } else { // PARTIAL_CACHING && ALD.getFromPos() > prevLastRepeatedVertexIdx
                fromPosAfterCaching[idx] = ALD.getFromPos();
                labelAfterCaching[idx] = ALD.getLabel();
                versionAfterCaching[idx] = ALD.getVersion();
                directionAfterCaching[idx++] = ALD.getDirection();
            }
        }
        cachedSlice = new AdjListSlice();
        if (cachingType == CachingType.PARTIAL_CACHING) {
            outSlice = new AdjListSlice();
        }
    }

    boolean isLastIntersectionCached() {
        isIntersectionCached = true;
        for (var i = 0; i < lastVertexIdsIntersected.length; ++i) {
            if (lastVertexIdsIntersected[i] != tuple[fromPosWhenCaching[i]]) {
                isIntersectionCached = false;
                lastVertexIdsIntersected[i] = tuple[fromPosWhenCaching[i]];
            }
        }
        return isIntersectionCached;
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

    @Override
    public Operator copy() {
        var operator = new Intersect(toVertex, ALDs, outSubgraph, inSubgraph);
        operator.vertexToIdxMap = MapUtils.copy(vertexToIdxMap);
        operator.expectedNumOutTuples = expectedNumOutTuples;
        operator.expectedICost = expectedICost;
        operator.numTimesAsFinalOperator = numTimesAsFinalOperator;
        copyNextOperators(operator);
        copyNextIntersectRemainingOperators(operator);
        return operator;
    }
}
