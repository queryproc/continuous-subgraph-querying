package ca.waterloo.dsg.graphflow.planner.Catalog.operator;

import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.plan.operator.scan.Scan;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Samples edges from an array of edges. Edges are pushed to the next operators one at a time.
 */
public class ScanSampling extends Scan {

    private static int NUM_SAMPLED_EDGES = 5;
    private BlockingQueue<int[]> edgesQueue;

    /**
     * Constructs a {@link ScanSampling} operator.
     *
     * @param outSubgraph is the subgraph, with one query edge, matched by the output tuples.
     */
    public ScanSampling(QueryGraph outSubgraph) {
        super(outSubgraph);
    }

    /**
     * @see Operator#init(int[], Graph, KeyStore)
     */
    @Override
    public void init(int[] tuple, Graph graph, KeyStore store) {
        if (null == this.tuple) {
            this.tuple = tuple;
            for (var nextOperator : next) {
                nextOperator.init(tuple, graph, store);
            }
        }
    }

    /**
     * @see Operator#execute().
     */
    @Override
    public void execute() {
        try {
            numOutTuples += NUM_SAMPLED_EDGES;
            while (true) {
                var edge = edgesQueue.remove(); // throws NoSuchElementException if empty.
                tuple[0] = edge[0];
                tuple[1] = edge[1];
                for (var nextOperator : next) {
                    nextOperator.processNewTuple();
                }
            }
        } catch (NoSuchElementException e) {
            // queue is empty.
        }
    }

    /**
     * @param edges is a list of edges to sample from.
     */
    public void setEdgeIndicesToSample(int[] edges) {
        var randomNumGen = new Random(0);
        var numEdges = edges.length / 2;
        var set = new HashSet<Integer>();
        edgesQueue = new LinkedBlockingQueue<>();
        while (edgesQueue.size() < NUM_SAMPLED_EDGES) {
            var edgeIdx = randomNumGen.nextInt(numEdges);
            if (set.contains(edgeIdx)) continue;
            set.add(edgeIdx);
            edgesQueue.add(new int[] {
                edges[edgeIdx * 2]     /* fromVertex */,
                edges[edgeIdx * 2 + 1] /* toVertex   */
            });
        }
    }
}
