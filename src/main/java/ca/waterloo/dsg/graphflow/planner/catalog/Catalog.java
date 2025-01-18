package ca.waterloo.dsg.graphflow.planner.Catalog;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes and holds stats about cost of intersections given labels & types for the current graph.
 */
public class Catalog {

    private static final Logger logger = LogManager.getLogger(Catalog.class);

    private List<QueryGraph> inSubgraphs;
    private Map<Integer/*inSubgraph idx*/, Map<String/*ALD */, Double>> iCost;
    private Map<Integer/*inSubgraph idx*/, Double> selectivity;

    public void setInSubgraphs(List<QueryGraph> inSubgraphs) {
        this.inSubgraphs = inSubgraphs;
    }

    public void setICost(Map<Integer/*inSubgraph idx*/, Map<String/*ALD */, Double>> iCost) {
        this.iCost = iCost;
    }

    public void setSelectivity(Map<Integer/*inSubgraph idx*/, Double> selectivity) {
        this.selectivity = selectivity;
    }

    public Catalog() {
        iCost = new HashMap<>();
        selectivity = new HashMap<>();
        inSubgraphs = new ArrayList<>();
    }

    public double getSelectivity(QueryGraph inSubgraph) {
        var versions = getEdgeVersionsAndSetToOLD(inSubgraph);
        for (var i = 0; i < inSubgraphs.size(); i++) {
            if (inSubgraph.isIsomorphicTo(inSubgraphs.get(i))) {
                setEdgeVersions(inSubgraph, versions);
                return selectivity.get(i);
            }
        }
        throw new IllegalArgumentException();
    }

    public double getICost(QueryGraph inSubgraph, List<AdjListDescriptor> ALDs) {
        var versions = getEdgeVersionsAndSetToOLD(inSubgraph);
        for (var i = 0; i < inSubgraphs.size(); i++) {
            if (inSubgraph.isIsomorphicTo(inSubgraphs.get(i))) {
                var ICostSum = 0.0;
                outer: for (var ALD : ALDs) {
                    var it = inSubgraph.getSubgraphMappingIterator(inSubgraphs.get(i));
                    while (it.hasNext()) {
                        var vertexMapping = it.next();
                        var strALD = getStrALD(ALD, vertexMapping);
                        if (iCost.get(i).containsKey(strALD)) {
                            ICostSum += iCost.get(i).get(strALD);
                            continue outer;
                        }
                    }
                }
                setEdgeVersions(inSubgraph, versions);
                return ICostSum;
            }
        }
        throw new IllegalArgumentException();
    }

    private Version[] getEdgeVersionsAndSetToOLD(QueryGraph inSubgraph) {
        var edges = inSubgraph.getEdges();
        var versions = new Version[edges.size()];
        for (var i = 0; i < edges.size(); i++) {
            versions[i] = edges.get(i).getVersion();
        }
        for (var edge : edges) {
            edge.setVersion(Version.OLD);
        }
        return versions;
    }

    private void setEdgeVersions(QueryGraph inSubgraph, Version[] versions) {
        var edges = inSubgraph.getEdges();
        for (var i = 0; i < edges.size(); i++) {
            edges.get(i).setVersion(versions[i]);
        }
    }

    public void populate(Graph graph, KeyStore store, List<QueryGraph> queryGraphs,
        String inputDirectory) throws IOException {
        var catalogPlans = new CatalogPlans(queryGraphs);
        catalogPlans.execute(graph, store);
        catalogPlans.addStatsToCatalog(this);
        log(inputDirectory);
    }

    public void addICost(QueryGraph inSubgraph, List<AdjListDescriptor> ALDs,
        double[] ICostValuesPerInputTuple) {
        var inSubgraphIdx = getSubgraphIdxAndInsertIfNecessary(inSubgraph);
        var vertexMapping = inSubgraphIdx == inSubgraphs.size() - 1 ?
            null : inSubgraph.getIsomorphicMappingIfAny(inSubgraphs.get(inSubgraphIdx));
        iCost.putIfAbsent(inSubgraphIdx, new HashMap<>());
        for (var i = 0; i < ALDs.size(); i++) {
            var ALD = ALDs.get(i);
            var strALD = getStrALD(ALD, vertexMapping);
            iCost.get(inSubgraphIdx).putIfAbsent(strALD, ICostValuesPerInputTuple[i]);
            iCost.get(inSubgraphIdx).put(strALD, /* avgICost: */
                (iCost.get(inSubgraphIdx).get(strALD) + ICostValuesPerInputTuple[i]) / 2.0);
        }
    }

    public void addSelectivity(QueryGraph outSubgraph, double selectivityPerInputTuple) {
        var outSubgraphIdx = getSubgraphIdxAndInsertIfNecessary(outSubgraph);
        selectivity.putIfAbsent(outSubgraphIdx, selectivityPerInputTuple);
        var avgSelectivity = (selectivity.get(outSubgraphIdx) + selectivityPerInputTuple) / 2.0;
        selectivity.put(outSubgraphIdx, avgSelectivity);
    }

    private int getSubgraphIdxAndInsertIfNecessary(QueryGraph subgraph) {
        var subgraphIdx = -1;
        for (var i = 0; i < inSubgraphs.size(); i++) {
            if (subgraph.isIsomorphicTo(inSubgraphs.get(i))) {
                subgraphIdx = i;
                break;
            }
        }
        if (subgraphIdx == -1) {
            subgraphIdx = inSubgraphs.size();
            inSubgraphs.add(subgraph);
        }
        return subgraphIdx;
    }

    public static String getStrALD(AdjListDescriptor ALD, Map<String, String> mapping) {
        var fromVertex = mapping == null ?
            ALD.getFromVertex() : mapping.get(ALD.getFromVertex());
        return "(" + fromVertex + ") " + ALD.getDirection() + "[" + ALD.getLabel() + "]";
    }

    public static String getALDsAsStr(List<AdjListDescriptor> ALDs, Map<String, String> mapping) {
        var ALDsAsStr = new ArrayList<String>();
        for (var ALD : ALDs) {
            var fromVertex = mapping == null ?
                ALD.getFromVertex() : mapping.get(ALD.getFromVertex());
            ALDsAsStr.add("(" + fromVertex + ") " + ALD.getDirection() + "[" + ALD.getLabel() + "]");
        }
        java.util.Collections.sort(ALDsAsStr);
        var builder = new StringBuilder();
        for (int i = 0; i < ALDsAsStr.size(); i++) {
            builder.append(ALDsAsStr.get(i));
            if (i < ALDsAsStr.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private void log(String inputDirectory) throws IOException {
        var icostFile = inputDirectory + "catalog-icost.txt";
        IOUtils.createNewFile(icostFile);
        var iCostWriter = new BufferedWriter(new FileWriter(icostFile));
        for (var key : iCost.keySet()) {
            var subgraph = inSubgraphs.get(key).toString();
            iCostWriter.write(subgraph + "\n");
            var ALDToICostMap = iCost.get(key);
            for (var ALDAsStr : ALDToICostMap.keySet()) {
                var icost = String.format("%.2f", ALDToICostMap.get(ALDAsStr));
                iCostWriter.write("\t" + ALDAsStr + ": " + icost + "\n");
            }
        }
        iCostWriter.close();

        var selectivityFile = inputDirectory + "catalog-selectivity.txt";
        IOUtils.createNewFile(selectivityFile);
        var selectivityWriter = new BufferedWriter(new FileWriter(selectivityFile));
        for (var key : selectivity.keySet()) {
            var subgraph = inSubgraphs.get(key).toString();
            var selectivity = this.selectivity.get(key);
            selectivityWriter.write(subgraph + ": " + String.format("%.2f", selectivity) + "\n");
        }
        selectivityWriter.close();
    }

    public void serialize(String directoryPath) throws IOException {
        logger.info("serializing the data graph's catalog.");
        IOUtils.serializeObjs(directoryPath, new Object[] {
            /* <filename , field to serialize> pair */
            "icost", iCost,
            "selectivity", selectivity,
            "inSubgraphs", inSubgraphs
        });
    }

    /**
     * Constructs a {@link Catalog} object from binary serialized data.
     *
     * @param directory is the directory to deserialize binary data from.
     * @return the constructed {@link Catalog} object.
     * @throws IOException if stream to file cannot be written to or closed.
     * @throws ClassNotFoundException if the object read is from input stream is not found.
     */
    @SuppressWarnings("unchecked") // casting.
    public static Catalog deserialize(String directory) throws IOException, ClassNotFoundException {
        var catalog = new Catalog();
        var inSubgraphs = (List<QueryGraph>) IOUtils.deserializeObj(directory + "inSubgraphs");
        catalog.setInSubgraphs(inSubgraphs);
        var selectivity = (Map<Integer, Double>) IOUtils.deserializeObj(directory + "selectivity");
        catalog.setSelectivity(selectivity);
        var icost = (Map<Integer, Map<String, Double>>) IOUtils.deserializeObj(directory +"icost");
        catalog.setICost(icost);
        return catalog;
    }
}
