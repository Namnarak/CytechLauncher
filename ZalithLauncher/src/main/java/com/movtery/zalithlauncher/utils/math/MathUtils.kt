package com.movtery.zalithlauncher.utils.math

/**
 * Find the object T with the closest (or higher) value compared to targetValue
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/045018f/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/utils/MathUtils.java#L28-L44)
 * @param targetValue the target value
 * @param objects the list of objects that the search will be performed on
 * @param valueProvider the provider for each values
 * @return the RankedValue that wraps the object which has the closest value to targetValue, or null if values of all
 *         objects are less than targetValue
 * @param <T> the object type that is used for the search.
 */
fun <T> findNearestPositive(
    targetValue: Int,
    objects: List<T>,
    valueProvider: (T) -> Int
): RankedValue<T>? {
    var minDelta = Int.MAX_VALUE
    var selectedObject: T? = null

    for (obj in objects) {
        val value = valueProvider(obj)
        if (value < targetValue) continue

        val delta = value - targetValue
        if (delta == 0) return RankedValue(obj, 0)
        if (delta < minDelta) {
            minDelta = delta
            selectedObject = obj
        }
    }

    return selectedObject?.let { RankedValue(it, minDelta) }
}