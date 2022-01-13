package com.felwal.trackfield.ui.common.model

import com.felwal.android.widget.sheet.SortMode
import com.felwal.android.widget.sheet.Sorter

class SorterItem(vararg sortModes: SortMode<Mode>) : RecyclerItem() {

    constructor(sorter: Sorter<Mode>) : this() {
        this.sorter = sorter
    }

    enum class Mode {
        DATE, DISTANCE, TIME, PACE, NAME, AMOUNT, START_LAT, START_LNG
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

}