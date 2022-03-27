package me.felwal.trackfield.ui.base;

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;

public abstract class BaseListAdapter extends ListDelegationAdapter<List<RecyclerItem>> {

    private SorterItem.Mode sortMode = SorterItem.Mode.DATE;

    // set

    public void setSortMode(SorterItem.Mode sortMode) {
        this.sortMode = sortMode;
    }

    // get

    public SorterItem.Mode getSortMode() {
        return sortMode;
    }

}
