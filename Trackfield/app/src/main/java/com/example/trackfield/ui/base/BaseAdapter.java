package com.example.trackfield.ui.base;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.Constants;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

public abstract class BaseAdapter extends ListDelegationAdapter<List<RecyclerItem>> {

    private Constants.SortMode sortMode = Constants.SortMode.DATE;

    // set

    public void setSortMode(Constants.SortMode sortMode) {
        this.sortMode = sortMode;
    }

    // get

    public Constants.SortMode getSortMode() {
        return sortMode;
    }

}
