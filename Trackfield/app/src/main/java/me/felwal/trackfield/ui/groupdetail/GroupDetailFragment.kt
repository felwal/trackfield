package me.felwal.trackfield.ui.groupdetail

import android.view.View
import me.felwal.trackfield.data.db.DbReader
import me.felwal.trackfield.data.db.model.Exercise
import me.felwal.trackfield.data.prefs.Prefs
import me.felwal.trackfield.ui.base.RecyclerFragment
import me.felwal.trackfield.ui.common.model.Exerlite
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity

abstract class GroupDetailFragment : RecyclerFragment() {

    protected var originId = Exercise.ID_NONE

    abstract val from: Int

    //

    protected fun updateFilterByOrigin() {
        // propagate filtering from main and depending on origin
        Prefs.setGroupVisibleTypes(
            if (originId == Exercise.ID_NONE) Prefs.getMainVisibleTypes()
            else arrayListOf(DbReader.get(a).getExercise(originId)!!.type)
        )
        Prefs.setGroupVisibleLabels(Prefs.getMainVisibleLabels())
    }

    // implements DelegateClickListener

    override fun onDelegateClick(view: View?, position: Int) {
        val item = getItem(position)

        // navigate to exercise
        if (item is Exerlite) {
            // if trying to navigate to origin, just finish this activity
            if (originId == item.id) a.finish()
            else ExerciseDetailActivity.startActivity(a, item.id, from)
        }

        super.onDelegateClick(item)
    }

}
