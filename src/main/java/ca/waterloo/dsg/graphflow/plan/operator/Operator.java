package ca.waterloo.dsg.graphflow.plan.operator;

import ca.waterloo.dsg.graphflow.plan.Plan;
import ca.waterloo.dsg.graphflow.plan.operator.extend.EI.AdjListSlice;
import ca.waterloo.dsg.graphflow.plan.operator.extend.EISharingUtils;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all database operators.
 */
public abstract class Operator implements Serializable {

    public static boolean CACHING_ENABLED = true;

    protected String name;

    public String getName() {
        return name;
    }
    protected Operator[] next;

    public Operator[] getNext() {
        return next;
    }

    protected Operator[] EIRemaining;

    public Operator[] getEIRemaining() {
        return EIRemaining;
    }

    protected Operator prev;

    public Operator getPrev() {
        return prev;
    }

    public void setPrev(Operator prev) {
        this.prev = prev;
    }

    private int level;

    protected int[] tuple;

    public void setTuple(int[] tuple) {
        this.tuple = tuple;
    }

    protected QueryGraph outSubgraph;

    public QueryGraph getOutSubgraph() {
        return outSubgraph;
    }

    public void setOutSubgraph(QueryGraph outSubgraph) {
        this.outSubgraph = outSubgraph;
    }

    protected QueryGraph inSubgraph;

    public QueryGraph getInSubgraph() {
        return inSubgraph;
    }

    public void setInSubgraph(QueryGraph inSubgraph) {
        this.inSubgraph = inSubgraph;
    }

    protected Map<String, Integer> vertexToIdxMap;

    public Map<String, Integer> getVertexToIdxMap() {
        return vertexToIdxMap;
    }

    protected int lastRepeatedVertexIdx;

    public int getLastRepeatedVertexIdx() {
        return lastRepeatedVertexIdx;
    }

    public void setLastRepeatedVertexIdx(int lastRepeatedVertexIdx) {
        this.lastRepeatedVertexIdx = lastRepeatedVertexIdx;
    }

    protected int outTupleLen;

    public int getOutTupleLen() {
        return outTupleLen;
    }

    protected long ICost = 0;

    public long getICost() {
        return ICost;
    }

    protected long numOutTuples = 0;

    public long getNumOutTuples() {
        return numOutTuples;
    }

    protected double expectedICost = 0;

    public double getExpectedICost() {
        return expectedICost;
    }

    protected double expectedNumOutTuples = Graph.EDGE_BATCH_SIZE;

    public double getExpectedNumOutTuples() {
        return expectedNumOutTuples;
    }

    protected int numTimesAsFinalOperator;

    public int getNumTimesAsFinalOperator() {
        return numTimesAsFinalOperator;
    }

    public void setNumTimesAsFinalOperator(int numTimesAsFinalOperator) {
        this.numTimesAsFinalOperator = numTimesAsFinalOperator;
    }

    /**
     * Constructs an {@link Operator} object.
     *
     * @param outSubgraph The subgraph matched by the output tuples.
     * @param inSubgraph The subgraph matched by the output tuples.
     */
    protected Operator(QueryGraph outSubgraph, QueryGraph inSubgraph) {
        this.outSubgraph = outSubgraph;
        this.inSubgraph = inSubgraph;
        this.outTupleLen = outSubgraph.getNumVertices();
        this.lastRepeatedVertexIdx = null == inSubgraph ? 0 /* scan */ : outTupleLen - 2;
    }

    /**
     * Initialize the operator e.g. memory allocation.
     *
     * @param tuple is the tuple processed throughout the query plan.
     * @param graph is the input data graph.
     * @param store is the labels and types key store.
     */
    public void init(int[] tuple, Graph graph, KeyStore store) {
        throw new UnsupportedOperationException();
    }

    /**
     * Process a new tuple and push the produced tuples to the next operator.
     */
    public void processNewTuple() {
        throw new UnsupportedOperationException();
    }

    /**
     * Process a new tuple based on a partial intersect and push the produced tuples to the next
     * operator.
     */
    public void processNewTuple(int[] neighbourIds, AdjListSlice slice) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the operator.
     */
    public void execute() {
        throw new UnsupportedOperationException();
    }

    public Operator getNextOperator() {
        if (null == next) {
            return null;
        }
        return next[0];
    }

    public void resetCache() {
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.resetCache();
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.resetCache();
            }
        }
    }

    public long getTotalICost() {
        var iCostSum = ICost;
        if (null != next) {
            for (var nextOperator : next) {
                iCostSum += nextOperator.getTotalICost();
            }
        }
        return iCostSum;
    }

    public double getExpectedTotalICost() {
        var iCostSum = expectedICost;
        if (null != next) {
            for (var nextOperator : next) {
                iCostSum += nextOperator.getExpectedTotalICost();
            }
        }
        return iCostSum;
    }

    public double getExpectedTotalICost(Catalog catalog, Map<Integer, Double> idxToNumOutTuples) {
        var iCostSum = 0.0;
        if (null != next) {
            for (var nextOperator : next) {
                iCostSum += nextOperator.getExpectedTotalICost();
            }
        }
        return iCostSum;
    }

    public long getNumIntermediateTuples() {
        var numIntermediateResults = 0L;
        if (numTimesAsFinalOperator == 0 /* not final operator */ ||
                (null != next && null != EIRemaining)) {
            numIntermediateResults += numOutTuples;
        }
        if (null != next) {
            for (var nextOperator : next) {
                numIntermediateResults += nextOperator.getNumIntermediateTuples();
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                if (null != nextOperator.next) {
                    for (var nextNextOperator : nextOperator.next) {
                        numIntermediateResults += nextNextOperator.getNumIntermediateTuples();
                    }
                }
            }
        }
        return numIntermediateResults;
    }

    public int getNumOperators() {
        return 1 /* this */ +
            (null == next ? 0 : Arrays.stream(next)
                                      .map(Operator::getNumOperators)
                                      .reduce(0, Integer::sum)) +
            (null == EIRemaining ? 0 : Arrays.stream(EIRemaining)
                                             .map(Operator::getNumOperators)
                                             .reduce(0, Integer::sum));
    }

    /**
     * @param operator The next operator to append prefixes to.
     */
    public void setNext(Operator operator) {
        next = new Operator[]{ operator };
        operator.setPrev(this);
    }

    /**
     * @param next The next operators to append prefixes to.
     */
    public void setNext(Operator[] next) {
        this.next = next;
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.setPrev(this);
            }
        }
    }

    /**
     * @param next The next operators to append prefixes to.
     */
    public void setEIRemaining(Operator[] next) {
        this.EIRemaining = next;
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.setPrev(this);
            }
        }
    }

    public void setVertexToIdxMap(Map<String, Integer> vertexToIdxMap) {
        this.vertexToIdxMap = vertexToIdxMap;
        if (null != next) {
            for (var operatorNext : next) {
                operatorNext.setVertexToIdxMap(vertexToIdxMap);
            }
        }
    }

    public void setALDsFromPos(Map<String, Integer> vertexToIdxMap) {
        if (null != next) {
            for (var operatorNext : next) {
                operatorNext.setALDsFromPos(vertexToIdxMap);
            }
        }
    }

    public void shareALDsIfPossible(Catalog catalog, Plan plan) {
        if (null != next) {
            if (next.length > 1) {
                shareALDsPartiallyIfPossible(catalog, plan);
            }
            for (var nextOperator : next) {
                nextOperator.shareALDsIfPossible(catalog, plan);
            }
        }
    }

    private void shareALDsPartiallyIfPossible(Catalog catalog, Plan plan) {
        var newNextAndICostReduction = EISharingUtils.GetNewNextGivenBestALDsToShare(next, catalog);
        if (null == newNextAndICostReduction.a) { // no ALDs to share.
            return;
        }
        plan.reduceICost(newNextAndICostReduction.b /* expectedIcostRemoved */);
        this.setNext(newNextAndICostReduction.a);
        // if more than 1 EI operator, continue finding ALD sharing opportunities if any.
        if (next.length > 1) {
            this.shareALDsPartiallyIfPossible(catalog, plan);
        }
    }

    public void setLevelRecursively(int level) {
        this.level = level;
        if (null != next) {
            var nextLevel = level + 1;
            for (var nextOperator : next) {
                nextOperator.setLevelRecursively(nextLevel);
            }
        }
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.setLevelRecursively(level);
            }
        }
    }

    public void getLevelToICostMap(Map<Integer, Long> levelToICostMap) {
        levelToICostMap.putIfAbsent(level, 0L);
        levelToICostMap.put(level, levelToICostMap.get(level) + ICost);
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.getLevelToICostMap(levelToICostMap);
            }
        }
    }

    /**
     * @param operator The shared operator to append prefixes to.
     */
    public void addNext(Operator operator, Plan plan) {
        var nextOperator = operator.getNextOperator();
        if (null == nextOperator) {
            numTimesAsFinalOperator++;
            if (!plan.getLastOperators().contains(this)) {
                plan.getLastOperators().add(this);
            }
            return;
        }
        nextOperator.setPrev(this);
        var newNext = new Operator[(next != null ? next.length : 0) + 1];
        if (next != null) {
            System.arraycopy(next, 0, newNext, 0, next.length);
        }
        newNext[next != null ? next.length : 0] = nextOperator;
        next = newNext;
        Operator lastOperator;
        do {
            lastOperator = nextOperator;
            nextOperator = nextOperator.getNextOperator();
        } while (null != nextOperator);
        plan.addLastOperator(lastOperator);
        var vertexMapping = outSubgraph.getIsomorphicMappingIfAny(operator.getOutSubgraph());
        var newVertexToIdxMap = updateVertexToIdxMap(vertexMapping, vertexToIdxMap);
        operator.getNextOperator().setALDsFromPos(newVertexToIdxMap);
        operator.getNextOperator().setVertexToIdxMap(newVertexToIdxMap);
    }

    private Map<String, Integer> updateVertexToIdxMap(Map<String, String> vertexMapping,
        Map<String, Integer> vertexToIdxMap) {
        var newVertexToIdxMap = new HashMap<String, Integer>();
        for (var vertex : vertexMapping.keySet()) {
            var mappedVertex = vertexMapping.get(vertex);
            var idx = vertexToIdxMap.get(vertex);
            newVertexToIdxMap.put(mappedVertex, idx);
        }
        return newVertexToIdxMap;
    }

    public void setEstimatedICostAndSelectivity(Catalog catalog,
        Map<Integer, Double> fromPosToNumOutTuples) {
        throw new UnsupportedOperationException();
    }

    public void addStatsToCatalog(Catalog catalog) {
        if (null != next) {
            for (var nextOperator : next) {
                nextOperator.addStatsToCatalog(catalog);
            }
        }
    }

    public void setIdxToNumOutTuples(Map<Integer, Double> idxToNumOutTuples, Catalog catalog) {
        throw new IllegalArgumentException();
    }

    public Operator findOperatorToShare(QueryGraph outSubgraph) {
        if (outSubgraph.isIsomorphicTo(this.outSubgraph)) {
            return this;
        } else {
            if (null != next) {
                for (var nextOperator : next) {
                    var operatorToShare = nextOperator.findOperatorToShare(outSubgraph);
                    if (null != operatorToShare) {
                        return operatorToShare;
                    }
                }
            }
        }
        return null;
    }

    public void addLastOperators(List<Operator> lastOperators) {
        throw new UnsupportedOperationException();
    }

    public Operator copy() {
        throw new UnsupportedOperationException();
    }
}
