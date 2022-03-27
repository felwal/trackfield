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

private const val SHEET_FILTER = "filterSheet"

class FilterDimension(
    val label: String,
    @DrawableRes val icon: Int,
    val title: String,
    val featureLabels: ArrayList<String>,
    val checkedFeatureLabels: ArrayList<String>,
    val tag: String
) {
    val featureIcons get() = listOf(icon).repeated(featureLabels.size).toIntArray()
    val isShallow get() = featureLabels.size == 0

    companion object {
        fun shallow(label: String, @DrawableRes icon: Int, tag: String) =
            FilterDimension(label, icon, "", arrayListOf(), arrayListOf(), tag)
    }
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

            if (dim.isShallow) {
                onShallowItemClick(dim.tag)
                return
            }

            val checkedIndices = dim.featureLabels.indicesOf(dim.checkedFeatureLabels).toMutableList().apply {
                // if some checkedFeatureLabels don't exist in featureLabels, we get -1's.
                // this occurs when a type is removed, but still saved in prefs.
                // as a solution, just remove those -1's. the outdated prefs will be updated automatically.
                removeAll { it == -1 }
            }.toIntArray()

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

    abstract fun onShallowItemClick(tag: String)

}
