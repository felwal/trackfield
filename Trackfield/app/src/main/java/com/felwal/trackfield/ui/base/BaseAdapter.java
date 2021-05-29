package com.felwal.trackfield.ui.base;

import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.utils.model.SortMode;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

public abstract class BaseAdapter extends ListDelegationAdapter<List<RecyclerItem>> {

    private SortMode.Mode sortMode = SortMode.Mode.DATE;

    // set

    public void setSortMode(SortMode.Mode sortMode) {
        this.sortMode = sortMode;
    }

    // get

    public SortMode.Mode getSortMode() {
        return sortMode;
    }

}
