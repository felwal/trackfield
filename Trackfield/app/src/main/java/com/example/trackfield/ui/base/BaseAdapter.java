package com.example.trackfield.ui.base;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.model.SortMode;
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
