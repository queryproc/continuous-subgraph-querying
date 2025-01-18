package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Given input tuples from a prev {@link Operator}, Intersect extends the tuples by one vertex.
 */
public class ExtendRemaining extends IntersectRemaining implements Serializable {

    private transient int lastVertexID = Integer.MIN_VALUE;

    ExtendRemaining(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(toVertex, ALDs, outSubgraph, inSubgraph);
    }

    @Override
    public void processNewTuple(int[] neighbourIds, AdjListSlice slice) {
        switch (prevEI.cachingType) {
            case NONE:
            case PARTIAL_CACHING:
                var adjList = graph.getAdjList(tuple[fromPosWhenCaching[0]],
                    directionWhenCaching[0], versionWhenCaching[0]);
                adjList.setNeighbourIds(labelWhenCaching[0], outSlice);
                ICost += (outSlice.endIdx - outSlice.startIdx);
                intersect(neighbourIds, slice.startIdx, slice.endIdx, outSlice.Ids, outSlice.
                    startIdx, outSlice.endIdx, extraNeighbourIds, extraSlice);
                break;
            case FULL_CACHING:
                var vertexID = tuple[fromPosWhenCaching[0]];
                if (cachingType == CachingType.NONE || !prevEI.isIntersectionCached ||
                        vertexID != lastVertexID) {
                    lastVertexID = vertexID;
                    adjList = graph.getAdjList(vertexID, directionWhenCaching[0],
                        versionWhenCaching[0]);
                    adjList.setNeighbourIds(labelWhenCaching[0], outSlice);
                    ICost += (outSlice.endIdx - outSlice.startIdx);
                    intersect(neighbourIds, slice.startIdx, slice.endIdx, outSlice.Ids, outSlice.
                        startIdx, outSlice.endIdx, extraNeighbourIds, extraSlice);
                }
        }
        // Intersect with last adjacency list from prior E/I.
        pushOutputTuplesToNextOperators(extraNeighbourIds, extraSlice);
    }

    @Override
    public void initCachingType() {
        cachingType = CachingType.NONE;
        if (ALDs.get(0).getFromPos() <= getLastRepeatedVertexIdx()) {
            cachingType = CachingType.FULL_CACHING;
        }
        prevEI = (EI) prev;
        if (cachingType == CachingType.FULL_CACHING &&
                prevEI.cachingType == CachingType.FULL_CACHING) {
            extraIntersectionCachingType = CachingType.FULL_CACHING;
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
    void allocateBuffers(int largestAdjListSize) {
        super.allocateBuffers(largestAdjListSize);
        outSlice = new AdjListSlice();
    }
}
