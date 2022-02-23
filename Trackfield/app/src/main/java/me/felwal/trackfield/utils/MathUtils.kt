package me.felwal.trackfield.utils

import kotlin.math.PI
import kotlin.math.sin

const val TAU = 2 * PI

fun sinusoidalIncrease(xMin: Float, xMax: Float, yMin: Float, yMax: Float): (Float) -> Float {
    if (xMin >= xMax) throw IndexOutOfBoundsException("xMax must be greater than xMin")
    if (yMin >= yMax) throw IndexOutOfBoundsException("yMax must be greater than yMin")

    val amp = (yMax - yMin) / 2
    val period = (xMax - xMin) * 2
    val xShift = period / 4 + xMin
    val yShift = (yMax + yMin) / 2
    val xFactor = TAU.toFloat() / period

    return y@{ x ->
        return@y when {
            x <= xMin -> yMin
            x >= xMax -> yMax
            else -> amp * sin(xFactor * (x - xShift)) + yShift
        }
    }
}
