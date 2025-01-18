package ca.waterloo.dsg.graphflow.plan;

import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.plan.operator.scan.Scan;
import ca.waterloo.dsg.graphflow.planner.Catalog.operator.ScanSampling;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.container.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query Plan (QP) representing left-deep binary plans, bushy binary plans, worst-case optimal
 * plans, and hybrid plans.
 */
public class Plan implements Serializable {

    public Scan scan;
    public Operator lastOperator;
    public List<Operator> lastOperators;

    public Scan getScan() {
        return scan;
    }

    public Operator getLastOperator() {
        return lastOperator;
    }

    public List<Operator> getLastOperators() {
        return lastOperators;
    }

    private long icost = -1;


    private long numOutTuples = -1;
    private long numIntermediateTuples = -1;

    private double expectedICost = 0.0;

    public double getExpectedICost() {
        return expectedICost;
    }

    Plan(Scan scan, Operator lastOperator) {
        this.scan = scan;
        this.lastOperator = lastOperator;
        this.lastOperators = new ArrayList<>();
        this.lastOperators.add(lastOperator);
    }

    /**
     * Executes the {@link Plan}.
     */
    void execute() {
        scan.execute();
    }

    void resetCache() {
        scan.resetCache();
    }

    /**
     * Initialize the plan by initializing all of its operators.
     *
     * @param graph is the input data graph.
     * @param store is the labels and types key store.
     */
    public void init(Graph graph, KeyStore store) {
        scan.setLevelRecursively(0 /* first level */);
        var largestTupleLen = 2;
        for (var lastOperator : lastOperators) {
            var numVertices = lastOperator.getOutSubgraph().getNumVertices();
            if (numVertices > largestTupleLen) {
                largestTupleLen = numVertices;
            }
        }
        var tuple = new int[largestTupleLen];
        scan.init(tuple, graph, store);
    }

    public void addALDSharing(Catalog catalog) {
        scan.shareALDsIfPossible(catalog, this /* plan */);
        lastOperators = new ArrayList<>();
        if (null != scan.getNext()) {
            for (var nextOperator : scan.getNext()) {
                nextOperator.addLastOperators(lastOperators);
            }
        }
    }

    public void reduceICost(double expectedIcostRemoved) {
        expectedICost -= expectedIcostRemoved;
    }

    public void setExpectedICost(double expectedICost) {
        this.expectedICost += expectedICost;
    }

    public void setExpectedICost() {
        expectedICost = scan.getExpectedTotalICost();
    }

    public void setEstimatedICostAndSelectivity(Catalog catalog) {
        var fromPosToNumOutTuples = new HashMap<Integer, Double>();
        fromPosToNumOutTuples.put(0 /* from */, (double) Graph.EDGE_BATCH_SIZE);
        fromPosToNumOutTuples.put(1 /* to   */, (double) Graph.EDGE_BATCH_SIZE);
        for (var nextOperator : scan.getNext()) {
            nextOperator.setEstimatedICostAndSelectivity(catalog, fromPosToNumOutTuples);
        }
    }

    public void addLastOperator(Operator anotherLastOperator) {
        lastOperators.add(anotherLastOperator);
    }

    long getNumIntermediateTuples() {
        if (numIntermediateTuples == -1) {
            numIntermediateTuples = scan.getNumIntermediateTuples();
        }
        return numIntermediateTuples;
    }

    long getICost() {
        if (icost == -1) {
            icost = scan.getTotalICost();
        }
        return icost;
    }

    long getNumOutTuples() {
        if (numOutTuples == -1) {
            numOutTuples = 0;
            lastOperators.forEach(op ->
                numOutTuples += op.getNumTimesAsFinalOperator() * op.getNumOutTuples());
        }
        return numOutTuples;
    }

    int getNumOperators() {
        return scan.getNumOperators();
    }

    void getLevelToICostMap(Map<Integer, Long> levelToICostMap) {
        scan.getLevelToICostMap(levelToICostMap);
    }

    public Pair<Operator, Operator /* to share */> findOperatorToShare(Plan plan,
        boolean startAtLastOperator) {
        var operator = startAtLastOperator ? plan.lastOperator : plan.lastOperator.getPrev();
        Operator operatorToShare = null;
        while (operator != null) {
            operatorToShare = scan.findOperatorToShare(operator.getOutSubgraph());
            if (null != operatorToShare) {
                break;
            }
            operator = operator.getPrev();
        }
        return new Pair<>(operator, operatorToShare);
    }

    void setScansToSample(Graph graph) {
        var label = scan.getOutSubgraph().getEdges().get(0).getLabel();
        var edges = graph.getFlatEdges(label);
        var scanSampling = new ScanSampling(scan.getOutSubgraph());
        scanSampling.setEdgeIndicesToSample(edges);
        scanSampling.setNext(scan.getNext());
        for (var nextOperator : scan.getNext()) {
            nextOperator.setPrev(scanSampling);
        }
        scan = scanSampling;
    }

    public Plan copy() {
        var scanCopy = scan.copy();
        Operator lastOperatorCopy = scanCopy;
        while (true) {
            if (null == lastOperatorCopy.getNextOperator()) {
                break;
            }
            lastOperatorCopy = lastOperatorCopy.getNextOperator();
        }
        var plan = new Plan(scanCopy, lastOperatorCopy);
        plan.expectedICost = expectedICost;
        return plan;
    }
}
