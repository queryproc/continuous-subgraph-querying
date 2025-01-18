package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.query.QueryGraph;

import java.io.Serializable;
import java.util.List;

/**
 * Given input tuples from a prev {@link Operator}, Intersect extends the tuples by one vertex.
 */
class IntersectPartial extends Intersect implements Serializable {

    IntersectPartial(String toVertex, List<AdjListDescriptor> ALDs, QueryGraph outSubgraph,
        QueryGraph inSubgraph) {
        super(toVertex, ALDs, outSubgraph, inSubgraph);
    }

    @Override
    void pushOutputTuplesToNextOperators(int[] outNeighbourIds, AdjListSlice outSlice) {
        if (null != EIRemaining) {
            for (var nextOperator : EIRemaining) {
                nextOperator.processNewTuple(outNeighbourIds, outSlice);
            }
        }
    }
}
