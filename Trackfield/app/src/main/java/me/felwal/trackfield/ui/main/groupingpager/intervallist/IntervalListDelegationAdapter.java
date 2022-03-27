package me.felwal.trackfield.ui.main.groupingpager.intervallist;

import android.app.Activity;

import java.util.List;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.GraphWeekAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

public class IntervalListDelegationAdapter extends BaseListAdapter {

    public IntervalListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new IntervalListIntervalAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a));

        setItems(items);
    }

}
