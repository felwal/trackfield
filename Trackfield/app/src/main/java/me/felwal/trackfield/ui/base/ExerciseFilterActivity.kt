package me.felwal.trackfield.ui.base

import me.felwal.trackfield.R
import me.felwal.trackfield.data.db.DbReader

private const val SHEET_FILTER_TYPE = "filterByTypeSheet"
private const val SHEET_FILTER_LABEL = "filterByLabelSheet"
private const val SHEET_FILTER_CLEAR = "clearFilters"

class ExerciseFilter(
    val visibleTypes: ArrayList<String>,
    val visibleLabels: ArrayList<String>
) {
    val count get() = visibleTypes.size + visibleLabels.size
    val isActive get() = count >= 1
}

abstract class ExerciseFilterActivity : MultidimFilterActivity() {

    abstract val filter: ExerciseFilter

    //

    override val dimensions: Array<FilterDimension>
        get() {
            val dims = mutableListOf(
                FilterDimension(
                    getString(R.string.sheet_item_type), R.drawable.ic_sport, getString(R.string.dialog_title_filter_types),
                    DbReader.get(this).types, filter.visibleTypes, SHEET_FILTER_TYPE
                ),
                FilterDimension(
                    getString(R.string.sheet_item_label), R.drawable.ic_label,
                    getString(R.string.dialog_title_filter_labels), DbReader.get(this).getLabels(true),
                    filter.visibleLabels, SHEET_FILTER_LABEL
                )
            )

            // add a "clear filters" label if filters are already applied
            if (filter.count > 0) {
                val clearLabel = "Clear filters (${filter.count})"
                dims.add(FilterDimension.shallow(clearLabel, R.drawable.ic_close, SHEET_FILTER_CLEAR))
            }

            return dims.toTypedArray()
        }

    override fun onMultidimFilter(itemStates: BooleanArray, tag: String) {
        when (tag) {
            SHEET_FILTER_TYPE -> {
                val visibleTypes = getCheckedLabels(DbReader.get(this).types, itemStates)
                applyTypeFilter(visibleTypes)
                showFilterSheet()
            }
            SHEET_FILTER_LABEL -> {
                val visibleLabels = getCheckedLabels(DbReader.get(this).getLabels(true), itemStates)
                applyLabelFilter(visibleLabels)
                showFilterSheet()
            }
        }

        onExerciseFilter()
    }

    override fun onShallowItemClick(tag: String) {
        when (tag) {
            SHEET_FILTER_CLEAR ->{
                applyTypeFilter(arrayListOf())
                applyLabelFilter(arrayListOf())
                onExerciseFilter()
            }
        }
    }

    //

    abstract fun applyTypeFilter(visibleTypes: ArrayList<String>)

    abstract fun applyLabelFilter(visibleLabels: ArrayList<String>)

    abstract fun onExerciseFilter()

}
