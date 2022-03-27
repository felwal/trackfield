package me.felwal.trackfield.utils

fun Float.addValueToAverage(newValue: Float, newValueIndex: Int): Float =
    (this * newValueIndex + newValue) / (newValueIndex + 1)

fun Double.addValueToAverage(newValue: Double, newValueIndex: Int): Double =
    (this * newValueIndex + newValue) / (newValueIndex + 1)

fun dynamicAverage(prevAvg: Float, newValue: Float, newValueIndex: Int): Float =
    (prevAvg * newValueIndex + newValue) / (newValueIndex + 1)

fun <E> List<E>.split(count: Int): List<List<E>> {
    val sublists = mutableListOf<List<E>>()
    val indexStep = size / count

    for (i in 0 until count) {
        sublists.add(
            if (i == count - 1) subList(i * indexStep, size)
            else subList(i * indexStep, (i + 1) * indexStep)
        )
    }

    return sublists
}
