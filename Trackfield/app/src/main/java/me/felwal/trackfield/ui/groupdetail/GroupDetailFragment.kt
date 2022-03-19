package me.felwal.trackfield.ui.groupdetail

import me.felwal.trackfield.data.db.DbReader
import me.felwal.trackfield.data.prefs.Prefs
import me.felwal.trackfield.ui.base.RecyclerFragment

abstract class GroupDetailFragment : RecyclerFragment() {

    protected fun updateFilterByOrigin(originId: Int? = null) {
        // propagate filtering from main and depending on origin
        Prefs.setGroupVisibleTypes(
            if (originId == null || originId == -1) Prefs.getMainVisibleTypes()
            else arrayListOf(DbReader.get(a).getExercise(originId)!!.type)
        )
        Prefs.setGroupVisibleLabels(Prefs.getMainVisibleLabels())
    }

}
