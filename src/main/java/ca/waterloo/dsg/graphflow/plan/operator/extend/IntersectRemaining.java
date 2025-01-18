package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.io.FileWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Given input tuples from a prev {@link Operator}, Intersect extends the tuples by one vertex.
 */
public class IntersectRemaining extends Intersect implements Serializable {

    transient int[] extraNeighbourIds;
    transient AdjListSlice extraSlice;
    transient CachingType extraIntersectionCachingType = CachingType.NONE;

    transient EI prevEI;

    IntersectRemaining(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(toVertex, ALDs, outSubgraph, inSubgraph);
    }

    @Override
    public void processNewTuple(int[] neighbourIds, AdjListSlice slice) {
        switch (prevEI.cachingType) {
            case NONE:
            case PARTIAL_CACHING:
                intersectAdjacencyLists();
                intersect(neighbourIds, slice.startIdx, slice.endIdx, outNeighbourIds, outSlice.
                    startIdx, outSlice.endIdx, extraNeighbourIds, extraSlice);
                pushOutputTuplesToNextOperators(extraNeighbourIds, extraSlice);
            case FULL_CACHING:
                if (!prevEI.isIntersectionCached()) {
                    intersectAdjacencyLists();
                    intersect(neighbourIds, slice.startIdx, slice.endIdx, outNeighbourIds, outSlice.
                        startIdx, outSlice.endIdx, extraNeighbourIds, extraSlice);
                    pushOutputTuplesToNextOperators(extraNeighbourIds, extraSlice);
                } else {
                    if (cachingType == CachingType.NONE || !isLastIntersectionCached()) {
                        var ID = tuple[fromPosWhenCaching[0]];
                        graph.getAdjList(ID, directionWhenCaching[0], versionWhenCaching[0]).intersect(
                            neighbourIds, slice.startIdx, slice.endIdx, labelWhenCaching[0],
                            cachedNeighbourIds, cachedSlice);
                        for (var i = 1; i < fromPosWhenCaching.length; i++) {
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
                    pushOutputTuplesToNextOperators(outNeighbourIds, outSlice);
                }
        }
    }

    @Override
    double getExpectedICostConsideringCaching(Catalog catalog,
        Map<Integer, Double> fromIdxToNumOutTuples) {
        double outTuplesToProcess = fromIdxToNumOutTuples.get(outTupleLen - 2);
        if (cachingType == CachingType.FULL_CACHING &&
            prevEI.cachingType == CachingType.FULL_CACHING) {
            outTuplesToProcess /= fromIdxToNumOutTuples.get(ALDs.get(0).getFromPos());
        }
        return outTuplesToProcess * catalog.getICost(inSubgraph, ALDs);
    }

    @Override
    public void processNewTuple() {
        throw new UnsupportedOperationException();
    }

    @Override
    void allocateBuffers(int largestAdjListSize) {
        super.allocateBuffers(largestAdjListSize);
        extraSlice = new AdjListSlice();
        extraNeighbourIds = new int[largestAdjListSize];
    }

    @Override
    public int getPrevLastRepeatedVertexIdx() {
        return prev.getPrev().getLastRepeatedVertexIdx();
    }

    @Override
    void initCachingType() {
        super.initCachingType();
        prevEI = (EI) prev;
    }
}
