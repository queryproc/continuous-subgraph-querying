package ca.waterloo.dsg.graphflow.util.collection;

import ca.waterloo.dsg.graphflow.plan.operator.AdjListDescriptor;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        var resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<>());
        } else {
            var firstList = lists.get(0);
            var remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (var condition : firstList) {
                for (var remainingList : remainingLists) {
                    var resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    public static List<List<List<AdjListDescriptor>>> cartesianProductSameSize(
        List<List<List<AdjListDescriptor>>> ALDsPermutations) {
        var resultLists = new ArrayList<List<List<AdjListDescriptor>>>();
        if (ALDsPermutations.size() == 0) {
            resultLists.add(new ArrayList<>());
        } else {
            var ALDsPermutation = ALDsPermutations.get(0);
            var remainingALDsPermutations = cartesianProduct(ALDsPermutations.subList(1,
                ALDsPermutations.size()));
            for (var ALDs : ALDsPermutation) {
                for (var remainingALDsPermutation : remainingALDsPermutations) {
                    if (equalInSize(ALDs, remainingALDsPermutation)) {
                        var resultList = new ArrayList<List<AdjListDescriptor>>();
                        resultList.add(ALDs);
                        resultList.addAll(remainingALDsPermutation);
                        resultLists.add(resultList);
                    }
                }
            }
        }
        return resultLists;
    }

    private static boolean equalInSize(List<AdjListDescriptor> ALDs,
        List<List<AdjListDescriptor>> otherALDsList) {
        for (var otherALDs : otherALDsList) {
            if (ALDs.size() != otherALDs.size()) {
                return false;
            }
        }
        return true;
    }
}
