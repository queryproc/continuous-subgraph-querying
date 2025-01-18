package ca.waterloo.dsg.graphflow.plan;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.plan.operator.extend.EI;
import ca.waterloo.dsg.graphflow.plan.operator.scan.Scan;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph.Direction;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a delta query, enumerates a plan per connected QVO.
 */
public class PlansEnumeratorSingle {

    private QueryGraph queryGraph;
    private boolean isForCatalog;

    /**
     * Constructs a {@link PlansEnumeratorSingle} object.
     */
    public PlansEnumeratorSingle(QueryGraph queryGraph, boolean isForCatalog) {
        this.queryGraph = queryGraph;
        this.isForCatalog = isForCatalog;
    }

    /**
     * Constructs a {@link PlansEnumeratorSingle} object.
     */
    public PlansEnumeratorSingle(QueryGraph queryGraph) {
        this(queryGraph, false /* isForCatalog */);
    }

    /**
     * @return The list of generated plans.
     */
    public List<Plan> generatePlans() {
        List<List<String>> QVOs = new ArrayList<>();
        addInitialDeltaEdgeOrAllEdges(QVOs);
        QVOs = fillQVOs(QVOs);
        var plans = new ArrayList<Plan>();
        for (var QVO : QVOs) {
            plans.add(getPhysicalPlan(QVO));
        }
        for (var plan : plans) {
            plan.getLastOperator().setNumTimesAsFinalOperator(1 /* is final op */);
        }
        return plans;
    }

    private void addInitialDeltaEdgeOrAllEdges(List<List<String>> QVOs) {
        var QVO = new ArrayList<String>();
        for (var edge : queryGraph.getEdges()) {
            if (edge.getVersion() == Version.DELTA) {
                QVO.add(edge.getFromVertex());
                QVO.add(edge.getToVertex());
                break;
            }
        }
        if (QVO.size() > 0) {
            QVOs.add(QVO);
        } else {
            for (var edge : queryGraph.getEdges()) {
                var scannedEdge = new ArrayList<String>();
                scannedEdge.add(edge.getFromVertex());
                scannedEdge.add(edge.getToVertex());
                QVOs.add(scannedEdge);
            }
        }
    }

    private List<List<String>> fillQVOs(List<List<String>> QVOs) {
        if (queryGraph.getNumVertices() == QVOs.get(0).size()) {
            return QVOs;
        }
        List<List<String>> newQVOs = new ArrayList<>();
        for (var QVO : QVOs) {
            for (var neighbor : queryGraph.getNeighbors(QVO)) {
                var copyQVO = new ArrayList<String>(QVO);
                copyQVO.add(neighbor);
                newQVOs.add(copyQVO);
            }
        }
        return fillQVOs(newQVOs);
    }

    private Plan getPhysicalPlan(List<String> QVO) {
        var partialQVO = new ArrayList<String>();
        partialQVO.add(QVO.get(0));
        partialQVO.add(QVO.get(1));
        QueryGraph inSubgraph;
        var outSubgraph = queryGraph.getProjection(partialQVO);
        Scan scan = new Scan(outSubgraph);
        Operator prevOperator = scan;
        for (var i = 2; i < QVO.size(); i++) {
            var toVertex = QVO.get(i);
            var ALDs = getALDs(outSubgraph, toVertex);
            partialQVO.add(toVertex);
            inSubgraph = prevOperator.getOutSubgraph();
            outSubgraph = queryGraph.getProjection(partialQVO);
            var nextOperator = EI.make(QVO.get(i), ALDs, outSubgraph, inSubgraph, isForCatalog);
            nextOperator.setPrev(prevOperator);
            prevOperator.setNext(nextOperator);
            prevOperator = nextOperator;
        }
        var vertexToIdxMap = getVertexToIdxMap(QVO);
        scan.setVertexToIdxMap(vertexToIdxMap);
        scan.setALDsFromPos(vertexToIdxMap);
        return new Plan(scan, prevOperator);
    }

    private Map<String, Integer> getVertexToIdxMap(List<String> QVO) {
        var vertexToIdxMap = new HashMap<String, Integer>();
        for (var i = 0; i < QVO.size(); i++) {
            vertexToIdxMap.put(QVO.get(i), i);
        }
        return vertexToIdxMap;
    }

    private List<AdjListDescriptor> getALDs(QueryGraph queryGraph, String toVertex) {
        var ALDs = new ArrayList<AdjListDescriptor>();
        for (var edge : this.queryGraph.getNeighborEdges(toVertex)) {
            var neighbour = edge.getFromVertex().equals(toVertex) ?
                edge.getToVertex(): edge.getFromVertex();
            if (!queryGraph.containsQueryVertex(neighbour)) {
                continue;
            }
            var direction = edge.getFromVertex().equals(neighbour) ? Direction.FWD : Direction.BWD;
            var label = edge.getLabel();
            ALDs.add(new AdjListDescriptor(neighbour, direction, label, edge.getVersion()));
        }
        return ALDs;
    }
}
