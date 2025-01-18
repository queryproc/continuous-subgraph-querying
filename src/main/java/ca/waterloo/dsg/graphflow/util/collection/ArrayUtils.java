package ca.waterloo.dsg.graphflow.util.collection;

import java.util.Arrays;

public class ArrayUtils {

    final public static double RESIZE_MULTIPLIER = 1.2;

    /**
     * Checks if the capacity of {@code array} is at least {@code minCapacity} and increases the
     * capacity by a factor of {@code RESIZE_MULTIPLIER} if it isn't.
     *
     * @param minCapacity The minimum required size of the array.
     */
    public static Object[] resizeIfNeeded(Object[] array, int minCapacity) {
        return (minCapacity > array.length) ?
            Arrays.copyOf(array, ArrayUtils.getNewCapacity(array.length, minCapacity)) : array;
    }

    /**
     * @see #resizeIfNeeded(Object[], int)
     */
    public static int[] resizeIfNeeded(int[] array, int minCapacity) {
        return (minCapacity > array.length) ?
            Arrays.copyOf(array, getNewCapacity(array.length, minCapacity)) : array;
    }

    /**
     * @see #resizeIfNeeded(Object[], int)
     */
    private static int getNewCapacity(int oldCapacity, int minCapacity) {
        var newCapacity = (int) (oldCapacity * RESIZE_MULTIPLIER) + 1;
        return (newCapacity > minCapacity) ? newCapacity : minCapacity;
    }
}
