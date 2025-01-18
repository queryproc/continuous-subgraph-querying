package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringJoiner;

/**
 * A join query graph.
 */
public class QueryGraph implements Serializable {

    // Represents a map from a from to a to query vertex & the query edge between them.
    private Map<String, Map<String, QueryEdge>> vertexToEdgesMap = new HashMap<>();
    private Map<String, Short> vertexToTypeMap = new HashMap<>();
    private Map<String, int[]> vertexToDegMap = new HashMap<>();
    private List<QueryEdge> edges = new ArrayList<>();

    // Mapping iterator used to decide if two query graphs are isomorphic.
    private IsomorphismIterator it = null;

    public Map<String, Short> getVertexToTypeMap() {
        return vertexToTypeMap;
    }

    public Short getVertexType(String queryVertex) {
        return vertexToTypeMap.get(queryVertex) == null ? 0 : vertexToTypeMap.get(queryVertex);
    }

    public List<QueryEdge> getEdges() {
        return edges;
    }

    public void setEdgeVersion(int i) {
        for (var j = 0; j < i; j++) {
            edges.get(j).setVersion(Version.NEW);
        }
        edges.get(i).setVersion(Version.DELTA);
        for (var j = i + 1; j < edges.size(); j++) {
            edges.get(j).setVersion(Version.OLD);
        }
    }

    /**
     * Adds a relation to the {@link QueryGraph}. The relation is stored both in forward and
     * backward direction. There can be multiple relations with different directions and relation
     * types between two vertices. A backward relation between fromVertex and toVertex is
     * represented by a {@link QueryEdge} from toVertex to fromVertex.
     *
     * @param queryEdge The relation to be added.
     */
    public void addEdge(QueryEdge queryEdge) {
        if (queryEdge == null) {
            return;
        }
        // Get the vertex IDs.
        var fromVertex = queryEdge.getFromVertex();
        var toVertex = queryEdge.getToVertex();
        var fromType = queryEdge.getFromType();
        var toType = queryEdge.getToType();
        vertexToTypeMap.putIfAbsent(fromVertex, KeyStore.ANY);
        vertexToTypeMap.putIfAbsent(toVertex, KeyStore.ANY);
        if (KeyStore.ANY != fromType) {
            vertexToTypeMap.put(fromVertex, fromType);
        }
        if (KeyStore.ANY != toType) {
            vertexToTypeMap.put(toVertex, toType);
        }
        // Set the in and out degrees for each variable.
        if (!vertexToDegMap.containsKey(fromVertex)) {
            int[] degrees = new int[2];
            vertexToDegMap.put(fromVertex, degrees);
        }
        vertexToDegMap.get(fromVertex)[0]++;
        if (!vertexToDegMap.containsKey(toVertex)) {
            int[] degrees = new int[2];
            vertexToDegMap.put(toVertex, degrees);
        }
        vertexToDegMap.get(toVertex)[1]++;
        // Add fwd edge fromVertex -> toVertex to the vertexToEdgesMap.
        addQEdgeToQGraph(fromVertex, toVertex, queryEdge);
        // Add bwd edge toVertex <- fromVertex to the vertexToEdgesMap.
        addQEdgeToQGraph(toVertex, fromVertex, queryEdge);
        edges.add(queryEdge);
    }

    /**
     * @param fromVertices The set of {@code String} vertices to get their to vertices.
     * @return The set of {@code String} to vertices.
     */
    public Set<String> getNeighbors(Collection<String> fromVertices) {
        var toVertices = new HashSet<String>();
        fromVertices.forEach(fromVertex -> toVertices.addAll(getNeighbors(fromVertex)));
        toVertices.removeAll(fromVertices);
        return toVertices;
    }

    public QueryGraph getProjection(List<String> vertices) {
        var projectedSubgraph = new QueryGraph();
        for (var i = 0; i < vertices.size() - 1; i++) {
            var vertex = vertices.get(i);
            for (var j = i + 1; j < vertices.size(); j++) {
                var otherVertex = vertices.get(j);
                projectedSubgraph.addEdge(vertexToEdgesMap.get(vertex).get(otherVertex));
            }
        }
        return projectedSubgraph;
    }

    /**
     * Adds the new {@link QueryEdge} to the query vertex to query edges map.
     *
     * @param fromQVertex is the from query vertex.
     * @param toQVertex is the to query vertex.
     * @param qEdge is the {@link QueryEdge} containing the query vertices and their types.
     */
    private void addQEdgeToQGraph(String fromQVertex, String toQVertex, QueryEdge qEdge) {
        vertexToEdgesMap.putIfAbsent(fromQVertex, new HashMap<>());
        vertexToEdgesMap.get(fromQVertex).put(toQVertex, qEdge);
    }

    /**
     * @param queryGraph is the {@link QueryGraph} to get isomorphic mappings for.
     * @return The mapping iterator.
     */
    public IsomorphismIterator getSubgraphMappingIterator(QueryGraph queryGraph) {
        if (null == it) {
            it = new IsomorphismIterator(new ArrayList<>(vertexToEdgesMap.keySet()));
        }
        it.init(queryGraph);
        return it;
    }

    /**
     * @return The number of query vertices present in the query.
     */
    public int getNumVertices() {
        return vertexToEdgesMap.size();
    }

    /**
     * @param vertex The from variable.
     * @param neighborVertex The to variable.
     * @return A list of {@link QueryEdge}s representing all the relations present between
     * {@code variable} and {@code neighborVertex}.
     * @throws NoSuchElementException if the {@code variable} is not present in the
     * {@link QueryGraph}.
     */
    public QueryEdge getEdge(String vertex, String neighborVertex) {
        if (!vertexToEdgesMap.containsKey(vertex) ||
                !vertexToEdgesMap.get(vertex).containsKey(neighborVertex)) {
            return null;
        }
        return vertexToEdgesMap.get(vertex).get(neighborVertex);
    }

    /**
     * @param fromVariable The {@code String} variable to get its to vertices.
     * @return The setAdjListSortOrder of {@code String} to vertices.
     */
    public Collection<String> getNeighbors(String fromVariable) {
        if (!vertexToEdgesMap.containsKey(fromVariable)) {
            throw new NoSuchElementException("Vertex '" + fromVariable + "' is not present.");
        }
        return new ArrayList<>(vertexToEdgesMap.get(fromVariable).keySet());
    }

    /**
     * @param fromVertex The {@code String} variable to get its to vertices.
     * @return The setAdjListSortOrder of {@code String} to vertices.
     */
    public Collection<QueryEdge> getNeighborEdges(String fromVertex) {
        if (!vertexToEdgesMap.containsKey(fromVertex)) {
            throw new NoSuchElementException("Vertex '" + fromVertex + "' is not present.");
        }
        var fromEdges = new ArrayList<QueryEdge>();
        for (var toVertex : vertexToEdgesMap.get(fromVertex).keySet()) {
            fromEdges.add(vertexToEdgesMap.get(fromVertex).get(toVertex));
        }
        return fromEdges;
    }

    public boolean containsQueryVertex(String vertex) {
        return vertexToEdgesMap.keySet().contains(vertex);
    }

    /**
     * Check if the {@link QueryGraph} is isomorphic to another given {@link QueryGraph}.
     *
     * @param otherQueryGraph The other {@link QueryGraph} to check for isomorphism.
     * @return True, if the query graph and otherQueryGraph are isomorphic. False, otherwise.
     */
    public boolean isIsomorphicTo(QueryGraph otherQueryGraph) {
        return null != otherQueryGraph && getNumVertices() == otherQueryGraph.getNumVertices() &&
            null != getSubgraphMappingIfAny(otherQueryGraph);
    }

    public Map<String, String> getIsomorphicMappingIfAny(QueryGraph otherQueryGraph) {
        if (!isIsomorphicTo(otherQueryGraph)) {
            return null;
        }
        return getSubgraphMappingIfAny(otherQueryGraph);
    }

    private Map<String, String> getSubgraphMappingIfAny(QueryGraph otherQueryGraph) {
        var it = getSubgraphMappingIterator(otherQueryGraph);
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    private Set<String> getQueryVertices() {
        return new HashSet<>(vertexToEdgesMap.keySet());
    }

    /**
     * @return A copy of the {@link QueryGraph} object.
     */
    public QueryGraph copy() {
        var copy = new QueryGraph();
        edges.forEach(edge -> copy.addEdge(edge.copy()));
        return copy;
    }

    @Override
    public String toString() {
        var strJoiner = new StringJoiner("");
        var isFirstQueryEdge = true;
        for (var fromVertex : vertexToEdgesMap.keySet()) {
            var fromType = vertexToTypeMap.get(fromVertex);
            for (var toVertex : vertexToEdgesMap.get(fromVertex).keySet()) {
                var toType = vertexToTypeMap.get(toVertex);
                var edge = vertexToEdgesMap.get(fromVertex).get(toVertex);
                var label = edge.getLabel();
                if (edge.getFromVertex().equals(fromVertex)) {
                    if (isFirstQueryEdge) {
                        strJoiner.add(String.format("(%s:%s)-[%s]->(%s:%s)", fromVertex, fromType,
                            label, toVertex, toType));
                        isFirstQueryEdge = false;
                    } else {
                        strJoiner.add(String.format(", (%s:%s)-[%s]->(%s:%s)", fromVertex, fromType,
                            label, toVertex, toType));
                    }
                }

            }
        }
        return strJoiner.toString();
    }

    /**
     * An iterator over a set of possible mappings between two query graphs.
     */
    public class IsomorphismIterator implements Iterator<Map<String, String>>, Serializable {

        List<String> vertices;
        List<String> otherVertices;
        QueryGraph otherQueryGraph;

        boolean isNextComputed;
        Map<String, String> nextMapping;

        int[] otherVertexIdxMapping;
        Stack<String> currMapping = new Stack<>();
        List<List<String>> possibleVertexMappings = new ArrayList<>();

        /**
         * Constructs an iterator for variable mappings between two query graphs.
         *
         * @param queryVertices are the query vertices of 'this' query graph.
         */
        IsomorphismIterator(List<String> queryVertices) {
            this.vertices = queryVertices;
            this.nextMapping = new HashMap<>();
            for (var variable : this.vertices) {
                nextMapping.put(variable, "");
            }
        }

        /**
         * @see Iterator#next()
         */
        @Override
        public Map<String, String> next() {
            if (!hasNext()) {
                throw new UnsupportedOperationException("Has no nextMapping mappings.");
            }
            nextMapping.clear();
            for (int i = 0; i < otherVertices.size(); i++) {
                nextMapping.put(currMapping.get(i), otherVertices.get(i));
            }
            isNextComputed = false;
            return nextMapping;
        }

        /**
         * Initialized the iterator based on the other query graph to map vertices to.
         *
         * @param otherQueryGraph The {@link QueryGraph} to map vertices to.
         */
        void init(QueryGraph otherQueryGraph) {
            // OtherQueryGraph is expected to be isomorphic or a subgraph.
            this.otherQueryGraph = otherQueryGraph;
            this.otherVertices = new ArrayList<>(otherQueryGraph.getQueryVertices());
            if (otherVertices.size() > vertices.size()) {
                isNextComputed = true;
                return;
            }

            this.otherVertexIdxMapping = new int[otherVertices.size()];
            clearPossibleVertexMappings(otherVertices.size());
            currMapping.clear();
            // Find possible vertex mappings.
            for (int i = 0; i < otherVertices.size(); i++) {
                var otherVertex = otherVertices.get(i);
                var otherType = otherQueryGraph.getVertexType(otherVertex);
                var otherDeg = otherQueryGraph.vertexToDegMap.get(otherVertex);
                for (var j = 0; j < vertices.size(); j++) {
                    var vertex = vertices.get(j);
                    // Ensure the vertices, have the same type.
                    if (!vertexToTypeMap.get(vertex).equals(otherType)) {
                        continue;
                    }

                    // If the other query graph and this query graph have the same number of
                    // vertices, each other vertex has to exactly match the number of incoming
                    // and outgoing edges as that of a vertex in the query vertex.
                    // Else, the other query graph has less vertices than this query graph,
                    // therefore each other vertex has to have an equal or less number of
                    // incoming and outgoing edges.
                    var vertexDeg = vertexToDegMap.get(vertex);
                    if (Arrays.equals(otherDeg, vertexDeg) ||
                        (otherVertices.size() < vertices.size() &&
                            vertexDeg[0] >= otherDeg[0] && vertexDeg[1] >= otherDeg[1])) {
                        possibleVertexMappings.get(i).add(vertex);
                    }
                }
                // if the otherVertex has no possible vertex mappings, next is computed.
                if (0 == possibleVertexMappings.get(i).size()) {
                    isNextComputed = true;
                    return;
                }
            }
            isNextComputed = false;
            hasNext();
        }

        private void clearPossibleVertexMappings(int otherVerticesSize) {
            for (int i = 0; i < otherVerticesSize; i++) {
                if (possibleVertexMappings.size() <= i) {
                    possibleVertexMappings.add(new ArrayList<>());
                } else {
                    possibleVertexMappings.get(i).clear();
                }
            }
        }

        /**
         * @see Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            if (!isNextComputed) {
                if (currMapping.size() == otherVertices.size()) {
                    currMapping.pop();
                }
                Outer: do {
                    var nextIdx = currMapping.size();
                    var isNextMappingPossible = otherVertexIdxMapping[nextIdx] <
                        possibleVertexMappings.get(nextIdx).size();
                    if (nextIdx == 0 && isNextMappingPossible) {
                        currMapping.add(possibleVertexMappings.get(0).get(
                            otherVertexIdxMapping[0]++));
                    } else if (isNextMappingPossible) {
                        var newVertexMapping = possibleVertexMappings.get(nextIdx).get(
                            otherVertexIdxMapping[nextIdx]++);
                        var otherVertex = otherVertices.get(nextIdx);
                        for (var i = 0; i < currMapping.size(); i++) {
                            var prevVertexMapping = currMapping.get(i);
                            if (prevVertexMapping.equals(newVertexMapping)) {
                                continue Outer;
                            }
                            var prevOtherVertex = otherVertices.get(i);
                            var edge = getEdge(newVertexMapping, prevVertexMapping);
                            var otherEdge = otherQueryGraph.getEdge(otherVertex, prevOtherVertex);
                            if (edge == null && otherEdge == null) {
                                continue;
                            }
                            if (edge == null) { // && otherEdge != null
                                continue Outer;
                            }
                            if (otherEdge == null) { // edge != null
                                continue;
                            }
                            if (edge.getLabel() != otherEdge.getLabel() ||
                                    edge.getVersion() != otherEdge.getVersion()) {
                                continue Outer;
                            }
                            if (!((edge.getFromVertex().equals(prevVertexMapping) &&
                                    otherEdge.getFromVertex().equals(prevOtherVertex)) ||
                                (edge.getFromVertex().equals(newVertexMapping) &&
                                    otherEdge.getFromVertex().equals(otherVertex)))) {
                                continue Outer;
                            }
                        }
                        currMapping.add(newVertexMapping);
                    } else if (otherVertexIdxMapping[nextIdx] >=
                                    possibleVertexMappings.get(nextIdx).size()) {
                        currMapping.pop();
                        otherVertexIdxMapping[nextIdx] = 0;
                    }
                    if (currMapping.size() == otherVertices.size()) {
                        break;
                    }
                } while (!(currMapping.size() == 0 &&
                    otherVertexIdxMapping[0] >= possibleVertexMappings.get(0).size()));
                isNextComputed = true;
            }
            return !currMapping.isEmpty();
        }
    }
}
