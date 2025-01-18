package ca.waterloo.dsg.graphflow.util.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for generic setAdjListSortOrder data structures.
 */
public class SetUtils {

    public static <T> List<T> subtract(List<T> set, List<T> otherSet) {
        return set.
            stream().
            filter(element -> !otherSet.contains(element)).
            collect(Collectors.toList());
    }

    public static List<List<Integer>> generateUniquePermutations(List<Integer> set, int len) {
        List<List<Integer>> permutations = new ArrayList<>();
        getUniquePermutationsGivenLen(set, len, 0 /* pos */, new ArrayList<>(), permutations);
        return permutations;
    }

    private static void getUniquePermutationsGivenLen(List<Integer> set, int len, int pos,
        List<Integer> temp, List<List<Integer>> permutation) {
        if (len == pos) {
            permutation.add(new ArrayList<>(temp));
            return;
        }
        for (int i = 0; i < set.size(); ++i) {
            if (pos == 0 || temp.get(temp.size() - 1) < set.get(i)) {
                if (pos == 0) {
                    temp.clear();
                }
                if (temp.size() < pos + 1) {
                    temp.add(set.get(i));
                } else {
                    temp.set(pos, set.get(i));
                }
                getUniquePermutationsGivenLen(set, len, pos + 1, temp, permutation);
            }
        }
    }

    /**
     * Generates a power setAdjListSortOrder of as a {@code List<List<T>>} given set excluding the
     * empty set.
     *
     * @param set The original setAdjListSortOrder as a {@code List<T>} to generate a power set for.
     * @return The power setAdjListSortOrder of the given setAdjListSortOrder.
     */
    public static <T> List<List<T>> getPowerSet(List<T> set, int minSize) {
        return generatePowerSet(set)
            .stream()
            .filter(subset -> subset.size() >= minSize && subset.size() <= set.size())
            .collect(Collectors.toList());
    }

    private static <T> List<List<T>> generatePowerSet(List<T> originalSet) {
        var sets = new ArrayList<List<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new ArrayList<>());
            return sets;
        }
        var list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        var rest = new ArrayList<T>(list.subList(1, list.size()));
        for (var set : generatePowerSet(rest)) {
            var newSet = new ArrayList<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }
}
