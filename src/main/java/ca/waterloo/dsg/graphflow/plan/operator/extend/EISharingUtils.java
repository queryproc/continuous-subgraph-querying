package ca.waterloo.dsg.graphflow.plan.operator.extend;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.util.collection.ListUtils;
import ca.waterloo.dsg.graphflow.util.collection.SetUtils;
import ca.waterloo.dsg.graphflow.util.container.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EISharingUtils {

    public static Pair<Operator[], Double> GetNewNextGivenBestALDsToShare(Operator[] next,
        Catalog catalog) {
        var nextALDsCombos = ListUtils.cartesianProductSameSize(EISharingUtils.collectALDs(next));
        var indexes = IntStream.range(0, next.length).boxed().collect(Collectors.toList());
        Operator[] newNextToReturn = null;
        var oldICost = 0.0;
        for (var nextOperator : next) {
            oldICost += nextOperator.getExpectedICost();
        }
        var nextICost = oldICost;
        for (var nextALDsCombo : nextALDsCombos) {
            var numOperators = nextALDsCombo.size();
            while (numOperators >= 2) {
                // The permutation generates many of the same ALDs but that's okay for now.
                var idxPermutations = SetUtils.generateUniquePermutations(indexes, numOperators);
                for (var idxPermutation : idxPermutations) {
                    if (ALDsAreEquivalent(next, nextALDsCombo, idxPermutation)) {
                        var newNext = getNewNextEIs(next, nextALDsCombo, idxPermutation);
                        var fromPosToNumOutTuples = new HashMap<Integer, Double>();
                        next[0].getPrev().setIdxToNumOutTuples(fromPosToNumOutTuples, catalog);
                        var newICost = 0.0;
                        for (var nextOperator : newNext) {
                            newICost += ((EI) nextOperator).getExpectedICostConsideringCaching(
                                catalog, fromPosToNumOutTuples);
                        }
                        for (var nextOperator : newNext[newNext.length - 1].getEIRemaining()) {
                            newICost += ((EI) nextOperator).getExpectedICostConsideringCaching(
                                catalog, fromPosToNumOutTuples);
                        }
                        if (newICost < nextICost) {
                            nextICost = newICost;
                            newNextToReturn = newNext;
                        }
                    }
                }
                numOperators--;
            }
        }
         return new Pair<>(newNextToReturn, oldICost - nextICost);
    }

    private static Operator[] getNewNextEIs(Operator[] next,
        List<List<AdjListDescriptor>>ALDsToShare, List<Integer> indexes) {
        var remainingALDsList = EISharingUtils.getRemainingALDs(next, ALDsToShare, indexes);
        var numNextRemainingEIs = remainingALDsList
            .stream()
            .filter(ALDs -> null != ALDs && ALDs.size() > 0)
            .count();
        var newNextIdx = 0;
        var remainingEIsIdx = 0;
        var newNext = new Operator[next.length - indexes.size() + 1 /* sharedEI */];
        var remainingEIs = new IntersectRemaining[(int) numNextRemainingEIs];
        var numFinalOperators = 0;
        List<Operator> nexts = new ArrayList<>();
        for (var i = 0; i < next.length; i++) {
            if (indexes.contains(i)) {
                if (remainingALDsList.get(i).size() > 0) {
                    var nextEI = (EI) next[i];
                    var toVertex = nextEI.toVertex;
                    var ALDs = remainingALDsList.get(i);
                    var inSubgraph = nextEI.getInSubgraph();
                    var outSubgraph = nextEI.getOutSubgraph();
                    remainingEIs[remainingEIsIdx] = remainingALDsList.get(i).size() == 1 ?
                        new ExtendRemaining(toVertex, ALDs, outSubgraph, inSubgraph) :
                        new IntersectRemaining(toVertex, ALDs, outSubgraph, inSubgraph);
                    remainingEIs[remainingEIsIdx].setNext(nextEI.getNext());
                    remainingEIs[remainingEIsIdx].setEIRemaining(nextEI.getEIRemaining());
                    remainingEIs[remainingEIsIdx++].setNumTimesAsFinalOperator(nextEI.
                        getNumTimesAsFinalOperator());
                } else if (remainingALDsList.get(i).size() == 0) {
                    numFinalOperators += next[i].getNumTimesAsFinalOperator();
                    if (null != next[i].getNext()) {
                        nexts.addAll(Arrays.asList(next[i].getNext()));
                    }
                }
            } else {
                newNext[newNextIdx++] = next[i];
            }
        }
        newNext[newNext.length - 1] = getSharedEI(next, remainingEIs, nexts, ALDsToShare, indexes,
            numFinalOperators);
        newNext[newNext.length - 1].setPrev(next[0].getPrev());
        ((EI) newNext[newNext.length - 1]).initCachingType();
        Arrays.stream((newNext[newNext.length - 1]).getEIRemaining())
              .forEach(operator -> ((EI) operator).initCachingType());
        return newNext;
    }

    private static EI getSharedEI(Operator[] next, Operator[] remainingEIs, List<Operator> nexts,
        List<List<AdjListDescriptor>>ALDsToShare, List<Integer> indexes, int numFinalOperators) {
        var ANextEI = (EI) next[indexes.get(0)];
        var toVertex = ANextEI.toVertex;
        var ALDs = ALDsToShare.get(indexes.get(0));
        var inSubgraph = ANextEI.getInSubgraph();
        var outSubgraph = ANextEI.getOutSubgraph();
        var sharedEI = remainingEIs.length == indexes.size() ?
            new IntersectPartial(toVertex, ALDs, outSubgraph, inSubgraph) :
            EI.make(toVertex, ALDs, outSubgraph, inSubgraph, false /* isForCatalog */);
        var nextsCopy = new Operator[nexts.size()];
        for (var i = 0; i < nexts.size(); i++) {
            nextsCopy[i] = nexts.get(i).copy();
        }
        sharedEI.setNext(nextsCopy);
        sharedEI.setEIRemaining(remainingEIs);
        sharedEI.setNumTimesAsFinalOperator(numFinalOperators);
        return sharedEI;
    }

    private static List<List<AdjListDescriptor>> getRemainingALDs(Operator[] next,
        List<List<AdjListDescriptor>> ALDsToShare, List<Integer> indexes) {
        var remainingALDsList = new ArrayList<List<AdjListDescriptor>>();
        for (var i = 0; i < next.length; i++) {
            if (indexes.contains(i)) {
                remainingALDsList.add(SetUtils.subtract(((EI) next[i]).getALDs(),
                    ALDsToShare.get(i)));
            } else {
                remainingALDsList.add(null);
            }
        }
        return remainingALDsList;
    }

    private static List<List<List<AdjListDescriptor>>> collectALDs(Operator[] nextOperators) {
        return Arrays
            .stream(nextOperators)
            .map(nextOperator -> SetUtils.getPowerSet(((EI) nextOperator).ALDs, 2 /* minSize */))
            .collect(Collectors.toList());
    }

    private static boolean ALDsAreEquivalent(Operator[] nextEIs,
        List<List<AdjListDescriptor>> ALDsPermutation, List<Integer> indexes) {
        var firstIdx = indexes.get(0);
        var ALDs = ALDsPermutation.get(firstIdx);
        var inSubgraph = nextEIs[firstIdx].getInSubgraph();
        for (var idx = 1; idx < indexes.size(); idx++) {
            var nextIdx = indexes.get(idx);
            var otherALDs = ALDsPermutation.get(nextIdx);
            var otherInSubgraph = nextEIs[nextIdx].getInSubgraph();
            var it = inSubgraph.getSubgraphMappingIterator(otherInSubgraph);
            var areEquivalent = false;
            while (it.hasNext()) {
                var vertexMapping = it.next();
                var numEquivalentALDs = 0;
                ALDLoop: for (var ALD : ALDs) {
                    for (var otherALD : otherALDs) {
                        if (ALDsAreEquivalent(ALD, otherALD, vertexMapping)) {
                            numEquivalentALDs++;
                            continue ALDLoop;
                        }
                    }
                    areEquivalent = false;
                    break;
                }
                if (numEquivalentALDs == ALDs.size()) {
                    areEquivalent = true;
                    break;
                }
            }
            if (!areEquivalent) {
                return false;
            }
        }
        return true;
    }

    private static boolean ALDsAreEquivalent(AdjListDescriptor ALD, AdjListDescriptor otherALD,
        Map<String, String> vertexMapping) {
        return ALD.getFromVertex().equals(vertexMapping.get(otherALD.getFromVertex())) &&
            ALD.getDirection() == otherALD.getDirection() &&
            ALD.getFromPos() == otherALD.getFromPos() &&
            ALD.getVersion() == otherALD.getVersion() &&
            ALD.getLabel() == otherALD.getLabel();
    }
}
