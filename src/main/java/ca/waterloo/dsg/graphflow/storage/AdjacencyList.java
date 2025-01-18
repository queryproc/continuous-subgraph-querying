package ca.waterloo.dsg.graphflow.storage;

import ca.waterloo.dsg.graphflow.plan.operator.extend.EI.AdjListSlice;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents the adjacency list of a vertex. Stores the IDs of the vertex's initNeighbours, the
 * types, and the IDs of edges that the vertex has to these initNeighbours in sorted arrays. Arrays
 * are sorted first by neighbour IDs and then by edge {@code short} type values.
 */
public class AdjacencyList implements Serializable {

    // Partition per edge label.
    private final int[] partitionOffsets;
    private int[] neighbourIds;

    AdjacencyList(int numLabels, int largestAdjListSize) {
        partitionOffsets = new int[numLabels + 1];
        neighbourIds = new int[largestAdjListSize];
    }

    /**
     * Constructs a {@link AdjacencyList} object.
     */
    AdjacencyList(int[] offsets) {
        this.partitionOffsets = offsets;
        var exactSize = offsets[offsets.length - 1] != 0 && offsets[offsets.length - 1] > 10 ?
            offsets[offsets.length - 1] : 20 /* default */;
        var sizeToAllocate = (exactSize * 2 /* multiplier */);
        this.neighbourIds = new int[sizeToAllocate];
    }

    public int[] getPartitionOffsets() {
        return partitionOffsets;
    }

    public int[] getNeighbourIds() {
        return neighbourIds;
    }

    public void setNeighbourIds(int[] neighbourIds) {
        this.neighbourIds = neighbourIds;
    }

    public void copy(AdjacencyList srcAdjList) {
        System.arraycopy(srcAdjList.neighbourIds, 0, neighbourIds, 0, srcAdjList.size());
        System.arraycopy(srcAdjList.partitionOffsets, 0, partitionOffsets, 0,
            srcAdjList.partitionOffsets.length);
    }

    void insert(int neighbourId, short label) {
        var size = size();
        var startIdx = partitionOffsets[label];
        System.arraycopy(neighbourIds, startIdx, neighbourIds, startIdx + 1, size - startIdx);
        neighbourIds[startIdx] = neighbourId;
        for (var i = label + 1; i < partitionOffsets.length; i++) {
            partitionOffsets[i]++;
        }
        Arrays.sort(neighbourIds, partitionOffsets[label], partitionOffsets[label + 1]);
    }

    /**
     * @param idx is the index of the neighbour id to return.
     * @return the neighbour id at a particular idx.
     */
    int getNeighbourId(int idx) {
        return neighbourIds[idx];
    }

    /**
     * Sets a new neighbour with the given Id at a given index.
     *
     * @param neighbourId is the Id of the neighbour.
     * @param idx is the index of the neighbour in the internal array.
     */
    void setNeighbourId(int neighbourId, int idx) {
        neighbourIds[idx] = neighbourId;
    }

    public void setNeighbourIds(short label, AdjListSlice adjListSlice) {
        adjListSlice.Ids = neighbourIds;
        adjListSlice.startIdx = partitionOffsets[label];
        adjListSlice.endIdx = partitionOffsets[label + 1];
    }

    public int intersect(int[] otherNeighbourIds, int otherIdx, int otherEndIdx, short label,
        int[] outNeighbourIds, AdjListSlice outputSlice) {
        outputSlice.reset();
        var thisIdx = partitionOffsets[label];
        var thisEndIdx = partitionOffsets[label + 1];
        var size = thisEndIdx - thisIdx;
        while (thisIdx < thisEndIdx && otherIdx < otherEndIdx) {
            if (neighbourIds[thisIdx] < otherNeighbourIds[otherIdx]) {
                do thisIdx++;
                while (thisIdx < thisEndIdx &&
                        neighbourIds[thisIdx] < otherNeighbourIds[otherIdx]);
            } else if (neighbourIds[thisIdx] > otherNeighbourIds[otherIdx]) {
                do otherIdx++;
                while (otherIdx < otherEndIdx &&
                        neighbourIds[thisIdx] > otherNeighbourIds[otherIdx]);
            } else {
                outNeighbourIds[outputSlice.endIdx] = neighbourIds[thisIdx];
                outputSlice.endIdx++;
                thisIdx++;
                otherIdx++;
            }
        }
        return size;
    }

    /**
     * Sorts each list of neighbour Ids of a particular label.
     */
    void sort() {
        for (int i = 0; i < partitionOffsets.length - 1; i++) {
            Arrays.sort(neighbourIds, partitionOffsets[i], partitionOffsets[i + 1]);
        }
    }

    /**
     * @param label is the edge label.

     * @return the size of the adjacency list.
     */
    public int size(short label) {
        return partitionOffsets[label + 1] - partitionOffsets[label];
    }

    /**
     * @return the size of the adjacency list.
     */
    public int size() {
        var size = 0;
        for (var i = 1; i < partitionOffsets.length; i++) {
            size += (partitionOffsets[i] - partitionOffsets[i - 1]);
        }
        return size;
    }
}
