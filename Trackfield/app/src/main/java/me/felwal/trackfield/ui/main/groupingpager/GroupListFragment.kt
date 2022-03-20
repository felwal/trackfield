package me.felwal.trackfield.ui.main.groupingpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import me.felwal.trackfield.R
import me.felwal.trackfield.data.prefs.Prefs
import me.felwal.trackfield.ui.base.RecyclerFragment
import me.felwal.trackfield.utils.fixIconCheckState

abstract class GroupListFragment : RecyclerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // set "show hidden" check state
        menu.findItem(R.id.action_show_hidden_groups)?.apply {
            fixIconCheckState()
            isChecked = Prefs.areHiddenGroupsShown()
        }
    }

}
