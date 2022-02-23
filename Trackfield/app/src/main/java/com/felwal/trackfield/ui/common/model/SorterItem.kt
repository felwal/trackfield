package com.felwal.trackfield.ui.common.model

import com.felwal.android.widget.sheet.SortMode
import com.felwal.android.widget.sheet.Sorter

class SorterItem(vararg sortModes: SortMode<Mode>) : RecyclerItem() {

    constructor(sorter: Sorter<Mode>) : this() {
        this.sorter = sorter
    }

    enum class Mode {
        DATE, DATE_ALT, DISTANCE, TIME, PACE, NAME, AMOUNT, START_LAT, START_LNG
    }

    var sorter = Sorter(*sortModes)
        private set

    val sortModes get() = sorter.sortModes
    val selectedIndex get() = sorter.selectedIndex
    val orderReversed get() = sorter.orderReversed
    val ascending get() = sorter.ascending
    val sortMode get() = sorter.sortMode
    val mode get() = sorter.mode
    val title get() = sorter.layoutString

    //

    fun copy() = SorterItem(sorter.copy())

    fun select(index: Int) = sorter.select(index)

    fun setSelection(selectedIndex: Int, orderReversed: Boolean) = sorter.setSelection(selectedIndex, orderReversed)

    // extends RecyclerItem

    override fun sameItemAs(item: RecyclerItem?): Boolean {
        if (item !is SorterItem) return false
        return sortModes.contentEquals(item.sortModes)
    }

    override fun sameContentAs(item: RecyclerItem?): Boolean {
        if (item !is SorterItem) return false
        return selectedIndex == item.selectedIndex && orderReversed == item.orderReversed
    }

    //

    companion object {
        @JvmStatic @JvmOverloads
        fun sortByDate(label: String = "Date") = SortMode(label, Mode.DATE, false)

        @JvmStatic @JvmOverloads
        fun sortByDateAlt(label: String = "Date") = SortMode(label, Mode.DATE_ALT, false)

        @JvmStatic @JvmOverloads
        fun sortByDistance(label: String = "Distance") = SortMode(label, Mode.DISTANCE, false)

        @JvmStatic @JvmOverloads
        fun sortByTime(label: String = "Time") = SortMode(label, Mode.TIME, true)

        @JvmStatic @JvmOverloads
        fun sortByPace(label: String = "Pace") = SortMode(label, Mode.PACE, true)

        @JvmStatic @JvmOverloads
        fun sortByName(label: String = "Name") = SortMode(label, Mode.NAME, true)

        @JvmStatic @JvmOverloads
        fun sortByAmount(label: String = "Amount") = SortMode(label, Mode.AMOUNT, false)

        @JvmStatic @JvmOverloads
        fun sortByLat(label: String = "Latitude") = SortMode(label, Mode.START_LAT, false)

        @JvmStatic @JvmOverloads
        fun sortByLng(label: String = "Longitude") = SortMode(label, Mode.START_LNG, true)
    }

}
