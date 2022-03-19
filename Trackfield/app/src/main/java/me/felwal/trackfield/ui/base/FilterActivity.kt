package me.felwal.trackfield.ui.base

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import me.felwal.android.fragment.sheet.CheckSheet
import me.felwal.android.fragment.sheet.ListSheet
import me.felwal.android.fragment.sheet.MultiChoiceSheet
import me.felwal.android.fragment.sheet.SingleChoiceSheet
import me.felwal.android.util.filter
import me.felwal.android.util.indicesOf
import me.felwal.android.util.repeated
import me.felwal.android.widget.control.CheckListOption
import me.felwal.android.widget.control.ListOption
import me.felwal.android.widget.control.SheetOption
import me.felwal.trackfield.R
import me.felwal.trackfield.data.db.DbReader
import me.felwal.trackfield.data.prefs.ExerciseFilter

private const val SHEET_FILTER = "filterSheet"
private const val SHEET_FILTER_TYPE = "filterByTypeSheet"
private const val SHEET_FILTER_LABEL = "filterByLabelSheet"

class FilterDimension(
    val label: String,
    @DrawableRes val icon: Int,
    val title: String,
    val featureLabels: ArrayList<String>,
    val checkedFeatureLabels: ArrayList<String>,
    val tag: String
) {
    val featureIcons get() = listOf(icon).repeated(featureLabels.size).toIntArray()
}

abstract class MultidimFilterActivity :
    AppCompatActivity(),
    SingleChoiceSheet.SheetListener,
    MultiChoiceSheet.SheetListener {

    abstract val dimensions: Array<FilterDimension>

    private val dimensionLabels get() = dimensions.map { it.label }.toTypedArray()
    private val dimensionIcons get() = dimensions.map { it.icon }.toIntArray()
    private val dimensionTags get() = dimensions.map { it.tag }

    //

    fun showFilterSheet() =
        ListSheet.newInstance(
            SheetOption(getString(R.string.dialog_title_filter), "", SHEET_FILTER, null),
            ListOption(dimensionLabels, dimensionIcons)
        ).show(supportFragmentManager)

    fun getCheckedLabels(labels: List<String>, checkedIndices: BooleanArray) =
        labels.filter(checkedIndices) as ArrayList

    //

    override fun onSingleChoiceSheetItemSelected(selectedIndex: Int, tag: String, passValue: String?) {
        if (tag == SHEET_FILTER) {
            val dim = dimensions[selectedIndex]
            val checkedIndices = dim.featureLabels.indicesOf(dim.checkedFeatureLabels).toIntArray()

            CheckSheet.newInstance(
                SheetOption(dim.title, "", dim.tag, null),
                CheckListOption(dim.featureLabels.toTypedArray(), checkedIndices, dim.featureIcons)
            ).show(supportFragmentManager)
        }
    }

    override fun onMultiChoiceSheetItemsSelected(itemStates: BooleanArray, tag: String, passValue: String?) {
        if (tag in dimensionTags) {
            onMultidimFilter(itemStates, tag)
        }
    }

    abstract fun onMultidimFilter(itemStates: BooleanArray, tag: String)

}

abstract class ExerciseFilterActivity : MultidimFilterActivity() {

    abstract val filter: ExerciseFilter

    //

    override val dimensions: Array<FilterDimension>
        get() = arrayOf(
            FilterDimension(
                "Type", R.drawable.ic_sport, getString(R.string.dialog_title_filter_types),
                DbReader.get(this).types, filter.visibleTypes, SHEET_FILTER_TYPE
            ),
            FilterDimension(
                "Label", R.drawable.ic_label, getString(R.string.dialog_title_filter_labels),
                DbReader.get(this).getLabels(true), filter.visibleLabels, SHEET_FILTER_LABEL
            )
        )

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

    //

    abstract fun applyTypeFilter(visibleTypes: ArrayList<String>)

    abstract fun applyLabelFilter(visibleLabels: ArrayList<String>)

    abstract fun onExerciseFilter()

}
