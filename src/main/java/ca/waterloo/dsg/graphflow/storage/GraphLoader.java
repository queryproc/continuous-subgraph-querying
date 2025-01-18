package ca.waterloo.dsg.graphflow.storage;

import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.antlr.v4.runtime.misc.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Constructs a {@link Graph} object from CSV file and binary serialized data.
 */
public class GraphLoader {

    /**
     * Constructs a {@link Graph} object from binary serialized data.
     *
     * @param directory is the directory to deserialize binary data from.
     * @return the constructed {@link Graph} object.
     * @throws IOException if stream to file cannot be written to or closed.
     * @throws ClassNotFoundException if the object read is from input stream is not found.
     */
    @SuppressWarnings("unchecked") // casting.
    public Graph make(String directory, int numLabels) throws IOException, ClassNotFoundException {
        // load the initial fields to construct the graph.
        var fwdAdjLists = (AdjacencyList[]) IOUtils.deserializeObj(directory + "fwdAdjLists");
        var bwdAdjLists = (AdjacencyList[]) IOUtils.deserializeObj(directory + "bwdAdjLists");
        var highestVertexId = (Integer) IOUtils.deserializeObj(directory + "highestVertexId");
        // create the graph object and setAdjListSortOrder its vertices and basic statistics.
        var graph = new Graph(fwdAdjLists, bwdAdjLists, highestVertexId, numLabels);
        graph.setNumEdges((int) IOUtils.deserializeObj(directory + "numEdges"));
        graph.setNumEdgesPerLabel((int[]) IOUtils.deserializeObj(directory + "numEdgesPerLabel"));
        graph.setLargestFwdAdjListSize((int) IOUtils.deserializeObj(
            directory + "largestFwdAdjListSize"));
        graph.setLargestBwdAdjListSize((int) IOUtils.deserializeObj(
            directory + "largestBwdAdjListSize"));
        graph.setLabelToLargestFwdAdjListSize((int[]) IOUtils.deserializeObj(
            directory + "labelToLargestFwdAdjListSize"));
        graph.setLabelToLargestBwdAdjListSize((int[]) IOUtils.deserializeObj(
            directory + "labelToLargestBwdAdjListSize"));
        graph.initAdjListsPool();
        return graph;
    }

    /**
     * Constructs a {@link Graph} object from the edges csv file.
     *
     * @param edgesCSVFile is the edges csv file to load data from.
     * @param separator is the separator between various columns.
     * @param store is the vertex types and edge labelsOrToTypes key store.
     * @return the constructed {@link Graph} object.
     * @throws IOException if stream to file cannot be written to or closed.
     */
    public Graph make(String edgesCSVFile, String separator, KeyStore store) throws IOException {
        var graph = new Graph(store.getNextLabelKey() /* numLabels */);
        loadEdges(edgesCSVFile, separator, store, graph);
        graph.setEdgeCountsAndLargestAdjListSizes(store.getNextLabelKey());
        return graph;
    }

    private void loadEdges(String file, String separator, KeyStore store, Graph graph)
        throws IOException {
        var highestVertexId = insertLabelsAndGetHighestVertexId(file, separator, store);
        graph.setHighestVertexId(highestVertexId);
        var adjListsMetadata = getAdjListMetadata(file, separator, store, graph);
        var numVertices = highestVertexId + 1;
        var fwdAdjLists = new AdjacencyList[numVertices];
        var bwdAdjLists = new AdjacencyList[numVertices];
        var fwdAdjListCurrIdx = new HashMap<Integer, int[]>(numVertices);
        var bwdAdjListCurrIdx = new HashMap<Integer, int[]>(numVertices);
        var offsetSize = store.getNextLabelKey();
        for (var vertexId = 0; vertexId < numVertices; vertexId++) {
            fwdAdjLists[vertexId] = new AdjacencyList(adjListsMetadata.a.get(vertexId));
            fwdAdjListCurrIdx.put(vertexId, new int[offsetSize]);
            bwdAdjLists[vertexId] = new AdjacencyList(adjListsMetadata.b.get(vertexId));
            bwdAdjListCurrIdx.put(vertexId, new int[offsetSize]);
        }

        var reader = new BufferedReader(new FileReader(file));
        var line = reader.readLine();
        while (null != line) {
            var row = line.split(separator);
            var fromVertex = Integer.parseInt(row[0]);
            var toVertex = Integer.parseInt(row[1]);
            var label = store.getLabelKeyAsShort(row[2]);
            var idx = fwdAdjListCurrIdx.get(fromVertex)[label];
            var offset = adjListsMetadata.a.get(fromVertex)[label];
            fwdAdjListCurrIdx.get(fromVertex)[label] += 1;
            fwdAdjLists[fromVertex].setNeighbourId(toVertex, idx + offset);
            idx = bwdAdjListCurrIdx.get(toVertex)[label];
            offset = adjListsMetadata.b.get(toVertex)[label];
            bwdAdjListCurrIdx.get(toVertex)[label] += 1;
            bwdAdjLists[toVertex].setNeighbourId(fromVertex, idx + offset);
            line = reader.readLine();
        }
        for (var vertexId = 0; vertexId < numVertices; vertexId++) {
            fwdAdjLists[vertexId].sort();
            bwdAdjLists[vertexId].sort();
        }
        graph.setFwdAdjLists(fwdAdjLists);
        graph.setBwdAdjLists(bwdAdjLists);
    }

    private int insertLabelsAndGetHighestVertexId(String csvFile, String separator, KeyStore store)
        throws IOException {
        var reader = new BufferedReader(new FileReader(csvFile));
        var line = reader.readLine();
        var highestVertexId = Integer.MIN_VALUE;
        while (null != line) {
            var row = line.split(separator);
            var fromVertex = Integer.parseInt(row[0]);
            var toVertex = Integer.parseInt(row[1]);
            store.insertLabelKeyIfNeeded(row[2]);
            if (fromVertex > highestVertexId) {
                highestVertexId = fromVertex;
            }
            if (toVertex > highestVertexId) {
                highestVertexId = toVertex;
            }
            line = reader.readLine();
        }
        return highestVertexId;
    }

    private Pair<Map<Integer, int[]>, Map<Integer, int[]>> getAdjListMetadata(String file,
        String separator, KeyStore store, Graph graph) throws IOException {
        Map<Integer, int[]> fwdAdjListMetadata = new HashMap<>();
        Map<Integer, int[]> bwdAdjListMetadata = new HashMap<>();
        var nextLabel = store.getNextLabelKey();
        for (int i = 0; i <= graph.getHighestVertexId(); i++) {
            fwdAdjListMetadata.put(i, new int[nextLabel + 1]);
            bwdAdjListMetadata.put(i, new int[nextLabel + 1]);
        }
        var reader = new BufferedReader(new FileReader(file));
        var line = reader.readLine();
        while (null != line) {
            String[] row = line.split(separator);
            var fromVertex = Integer.parseInt(row[0]);
            var toVertex = Integer.parseInt(row[1]);
            var label = store.getLabelKeyAsShort(row[2]);
            fwdAdjListMetadata.get(fromVertex)[label + 1] += 1;
            bwdAdjListMetadata.get(toVertex)[label + 1] += 1;
            line = reader.readLine();
        }
        for (var offsets : fwdAdjListMetadata.values()) {
            for (var i = 1; i < offsets.length - 1; i++) {
                offsets[nextLabel] += offsets[i];
                offsets[i] += offsets[i - 1];
            }
        }
        for (var offsets : bwdAdjListMetadata.values()) {
            for (var i = 1; i < offsets.length - 1; i++) {
                offsets[nextLabel] += offsets[i];
                offsets[i] += offsets[i - 1];
            }
        }
        return new Pair<>(fwdAdjListMetadata, bwdAdjListMetadata);
    }
}
