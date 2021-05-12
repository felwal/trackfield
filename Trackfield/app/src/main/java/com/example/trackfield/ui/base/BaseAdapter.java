package com.example.trackfield.ui.base;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.AppConsts;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

public abstract class BaseAdapter extends ListDelegationAdapter<List<RecyclerItem>> {

    private AppConsts.SortMode sortMode = AppConsts.SortMode.DATE;

    // set

    public void setSortMode(AppConsts.SortMode sortMode) {
        this.sortMode = sortMode;
    }

    // get

    public AppConsts.SortMode getSortMode() {
        return sortMode;
    }

}
