package com.felwal.trackfield.ui.main.groupingpager.intervallist;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GraphWeekAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class IntervalListDelegationAdapter extends BaseListAdapter {

    public IntervalListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new IntervalListAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a));

        setItems(items);
    }

}
