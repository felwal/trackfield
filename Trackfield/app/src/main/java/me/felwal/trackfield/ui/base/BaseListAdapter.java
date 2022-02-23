package me.felwal.trackfield.ui.base;

import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

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
