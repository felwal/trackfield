package me.felwal.trackfield.ui.widget.graph

import me.felwal.android.util.sign
import me.felwal.android.util.toInt
import kotlin.math.max
import kotlin.math.min

class Axis(
    private val yInverted: Boolean = false,
    private val zeroAsMin: Boolean = false
) {
    val data = ArrayList<GraphData>()

    var start = 0f
    var end = 0f
    var min = 0f
    var max = 0f

    val hasData get() = data.size > 0
    val hasMoreThanOnePoint get() = data.any { it.pointCount > 1 }

    // set

    /**
     * Adds data sets, topmost first.
     */
    fun addData(vararg data: GraphData) {
        for (datum in data) {
            if (datum.isEmpty) continue
            updateDomainAndRange(datum)
            this.data.add(0, datum)
        }
    }

    private fun updateDomainAndRange(newData: GraphData) {
        if (hasData) {
            start = min(start, newData.start)
            end = max(end, newData.end)
            min = if (zeroAsMin) 0f else min(min, newData.min)
            max = max(max, newData.max)
        }
        else {
            start = newData.start
            end = newData.end
            min = if (zeroAsMin) 0f else newData.min
            max = newData.max
        }
    }

    // calc

    fun bias(y: Float): Float {
        return if (max == min) 0f
        else yInverted.toInt() + (!yInverted).sign * (y - min) / (max - min)
    }

    // compare

    fun sameArgsAs(axis: Axis) = yInverted == axis.yInverted && zeroAsMin == axis.zeroAsMin

    fun sameDataAs(axis: Axis) =
        data.size == axis.data.size
            && (data zip axis.data).all { it.first.sameDataPointsAs(it.second) }

}
