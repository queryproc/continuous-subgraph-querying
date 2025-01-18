package ca.waterloo.dsg.graphflow.storage;

import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The input graph data stored.
 */
public class Graph {

    private static final Logger logger = LogManager.getLogger(Graph.class);

    /**
     * Used to identify the edge direction in the graph representation.
     */
    public enum Direction {
        FWD /* forward  */,
        BWD /* backward */
    }

    /**
     * Used to represent different versions of the graph.
     */
    public enum Version {
        OLD,
        NEW,
        DELTA
    }

    // Adjacency lists containing the neighbour vertex IDs sorted by ID.
    private AdjacencyList[] fwdAdjLists;
    private AdjacencyList[] bwdAdjLists;
    // Graph metadata.
    private int numEdges = 0;
    private int[] numEdgesPerLabel;
    private int highestVertexId = -1;
    private int largestFwdAdjListSize;
    private int largestBwdAdjListSize;
    private int[] labelToLargestFwdAdjListSize;
    private int[] labelToLargestBwdAdjListSize;

    // delta and new edges.
    private int[][] deltaEdges;
    private int[] deltaSizes;
    private int fwdPoolIdx;
    private int bwdPoolIdx;
    private AdjacencyList[] fwdAdjListsPool;
    private AdjacencyList[] bwdAdjListsPool;
    private Map<Integer /* fromVertex ID */, AdjacencyList> newFwdAdjLists;
    private Map<Integer /* toVertex   ID */, AdjacencyList> newBwdAdjLists;
    private int numLabels;

    public static final int EDGE_BATCH_SIZE = 5;

    public void setFwdAdjLists(AdjacencyList[] fwdAdjLists) {
        this.fwdAdjLists = fwdAdjLists;
    }

    public void setBwdAdjLists(AdjacencyList[] bwdAdjLists) {
        this.bwdAdjLists = bwdAdjLists;
    }

    public int[] getDeltaSizes() {
        return deltaSizes;
    }

    public int[][] getDeltaEdges() {
        return deltaEdges;
    }

    public void setNumEdges(int numEdges) {
        this.numEdges = numEdges;
    }

    public void setNumEdgesPerLabel(int[] numEdgesPerLabel) {
        this.numEdgesPerLabel = numEdgesPerLabel;
    }

    public void setLargestFwdAdjListSize(int largestFwdAdjListSize) {
        this.largestFwdAdjListSize = largestFwdAdjListSize;
    }

    public void setLargestBwdAdjListSize(int largestBwdAdjListSize) {
        this.largestBwdAdjListSize = largestBwdAdjListSize;
    }

    public void setLabelToLargestFwdAdjListSize(int[] labelToLargestFwdAdjListSize) {
        this.labelToLargestFwdAdjListSize = labelToLargestFwdAdjListSize;
    }

    public void setLabelToLargestBwdAdjListSize(int[] labelToLargestBwdAdjListSize) {
        this.labelToLargestBwdAdjListSize = labelToLargestBwdAdjListSize;
    }

    public int getHighestVertexId() {
        return highestVertexId;
    }

    public void setHighestVertexId(int highestVertexId) {
        this.highestVertexId = highestVertexId;
    }

    /**
     * Constructs a {@link Graph} object.
     *
     * @param numLabels is the total number of labels in the graph.
     */
    public Graph(int numLabels) {
        this.deltaEdges = new int[numLabels][EDGE_BATCH_SIZE * 2];
        this.deltaSizes = new int[numLabels];
        this.newFwdAdjLists = new HashMap<>(EDGE_BATCH_SIZE);
        this.newBwdAdjLists = new HashMap<>(EDGE_BATCH_SIZE);
        this.fwdAdjListsPool = new AdjacencyList[EDGE_BATCH_SIZE];
        this.bwdAdjListsPool = new AdjacencyList[EDGE_BATCH_SIZE];
        this.numLabels = numLabels;
    }

    public void initAdjListsPool() {
        for (var i = 0; i < EDGE_BATCH_SIZE; i++) {
            this.fwdAdjListsPool[i] = new AdjacencyList(numLabels, largestFwdAdjListSize);
            this.bwdAdjListsPool[i] = new AdjacencyList(numLabels, largestBwdAdjListSize);
        }
    }

    /**
     * Constructs a {@link Graph} object.
     *
     * @param fwdAdjLists are the forward adjacency lists.
     * @param bwdAdjLists are the backward adjacency lists.
     * @param highestVertexId is the highest vertex ID.
     */
    public Graph(AdjacencyList[] fwdAdjLists, AdjacencyList[] bwdAdjLists, int highestVertexId,
        int numLabels) {
        this(numLabels);
        this.fwdAdjLists = fwdAdjLists;
        this.bwdAdjLists = bwdAdjLists;
        this.highestVertexId = highestVertexId;
    }

    public void insertEdgeTemporarily(int fromVertex, int toVertex, short label) {
        var maxNewVertexId = Integer.max(fromVertex, toVertex);
        if (maxNewVertexId > highestVertexId) {
            throw new IllegalArgumentException();
        }
        var idx = deltaSizes[label] * 2;
        deltaEdges[label][idx] = fromVertex;
        deltaEdges[label][idx + 1] = toVertex;
        deltaSizes[label]++;
        createNewAdjList(fromVertex, toVertex, label, newFwdAdjLists, fwdAdjLists);
        createNewAdjList(toVertex, fromVertex, label, newBwdAdjLists, bwdAdjLists);
    }

    public void finalizeChanges() {
        fwdPoolIdx = 0;
        bwdPoolIdx = 0;
        updateAdjLists(fwdAdjLists, newFwdAdjLists);
        updateAdjLists(bwdAdjLists, newBwdAdjLists);
        for (var i = 0; i < deltaSizes.length; i++) {
            deltaSizes[i] = 0;
        }
    }

    private void updateAdjLists(AdjacencyList[] adjLists, Map<Integer, AdjacencyList> newAdjLists) {
        for (var fromVertex : newAdjLists.keySet()) {
            adjLists[fromVertex].copy(newAdjLists.get(fromVertex));
        }
        newAdjLists.clear();
    }

    private void createNewAdjList(int fromVertex, int toVertex, short label,
        Map<Integer, AdjacencyList> mergedAdjLists, AdjacencyList[] adjLists) {
        if (mergedAdjLists.containsKey(fromVertex)) {
            mergedAdjLists.get(fromVertex).insert(toVertex, label);
        } else {
            var oldAdjList = adjLists[fromVertex];
            var newAdjList = adjLists == fwdAdjLists ?
                fwdAdjListsPool[fwdPoolIdx++] : bwdAdjListsPool[bwdPoolIdx++];
            newAdjList.copy(oldAdjList);
            newAdjList.insert(toVertex, label);
            mergedAdjLists.put(fromVertex, newAdjList);
        }
    }

    @SuppressWarnings("fallthrough")
    public AdjacencyList getAdjList(int vertexId, Direction direction, Version version) {
        switch (version) {
            case NEW:
                if (direction == Direction.FWD && newFwdAdjLists.containsKey(vertexId)) {
                    return newFwdAdjLists.get(vertexId);
                } else if (direction == Direction.BWD && newBwdAdjLists.containsKey(vertexId)) {
                    return newBwdAdjLists.get(vertexId);
                }
            case OLD:
                return direction == Direction.FWD ? fwdAdjLists[vertexId] : bwdAdjLists[vertexId];
        }
        // never happens.
        throw new IllegalArgumentException();
    }

    /**
     * @param label is the edge label.
     * @param direction is the direction of extension as forward or backward.
     * @return The largest adjacency list size.
     */
    public int getLargestAdjListSize(short label, Direction direction) {
        return (Direction.FWD == direction ?
            labelToLargestFwdAdjListSize[label] : labelToLargestBwdAdjListSize[label]) + 100 *
                Graph.EDGE_BATCH_SIZE; // <- TODO: instead update labelToLargestFwdAdjListSize.
    }

    /**
     * @param numLabels is the number of labels in ...
     */
    void setEdgeCountsAndLargestAdjListSizes(int numLabels) {
        // set the largest adjacency list sizes for forward and backward directions per label.
        numEdgesPerLabel = new int[numLabels];
        labelToLargestFwdAdjListSize = new int[numLabels];
        labelToLargestBwdAdjListSize = new int[numLabels];
        for (var vertexId = 0; vertexId <= highestVertexId; vertexId++) {
            var fwdAdjListSize = fwdAdjLists[vertexId].size();
            if (fwdAdjListSize > largestFwdAdjListSize) {
                largestFwdAdjListSize = fwdAdjListSize;
            }
            var bwdAdjListSize = bwdAdjLists[vertexId].size();
            if (bwdAdjListSize > largestBwdAdjListSize) {
                largestBwdAdjListSize = bwdAdjListSize;
            }
            for (short label = 0; label < numLabels; label++) {
                var adjListSize = fwdAdjLists[vertexId].size(label);
                numEdges += adjListSize;
                numEdgesPerLabel[label] += adjListSize;
                if (adjListSize > labelToLargestFwdAdjListSize[label]) {
                    labelToLargestFwdAdjListSize[label] = adjListSize;
                }
            }
            for (short label = 0; label < numLabels; label++) {
                var adjListSize = bwdAdjLists[vertexId].size(label);
                if (adjListSize > labelToLargestBwdAdjListSize[label]) {
                    labelToLargestBwdAdjListSize[label] = adjListSize;
                }
            }
        }
        largestFwdAdjListSize *= 2 /* multiplier */;
        largestBwdAdjListSize *= 2 /* multiplier */;
    }

    // Warning: Not scalable implementation. Meant to work only with few million edge datasets.
    public int[] getFlatEdges(short label) {
        var edges = new int[2 * numEdgesPerLabel[label]];
        var idx = 0;
        for (var vertexId = 0; vertexId <= highestVertexId; vertexId++) {
            var adjList = fwdAdjLists[vertexId];
            if (null != adjList) {
                var startIdx = adjList.getPartitionOffsets()[label];
                var endIdx = adjList.getPartitionOffsets()[label + 1];
                for (var i = startIdx; i < endIdx; i++) {
                    edges[idx++] = vertexId;
                    edges[idx++] = adjList.getNeighbourId(i);
                }
            }
        }
        return edges;
    }

    /**
     * Serializes the graph by persisting different fields into different files.
     *
     * @param directoryPath is the directory to which the graph's serialized objects are persisted.
     * @throws IOException if stream to file cannot be written to or closed.
     */
    public void serialize(String directoryPath) throws IOException {
        logger.info("Serializing the data graph.");
        IOUtils.serializeObjs(directoryPath, new Object[] {
            /* <filename , field to serialize> pair */
            "numEdges", numEdges,
            "fwdAdjLists", fwdAdjLists,
            "bwdAdjLists", bwdAdjLists,
            "highestVertexId", highestVertexId,
            "numEdgesPerLabel", numEdgesPerLabel,
            "largestFwdAdjListSize", largestFwdAdjListSize,
            "largestBwdAdjListSize", largestBwdAdjListSize,
            "labelToLargestFwdAdjListSize", labelToLargestFwdAdjListSize,
            "labelToLargestBwdAdjListSize", labelToLargestBwdAdjListSize
        });
    }
}
