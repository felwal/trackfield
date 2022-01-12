package com.felwal.trackfield.ui.main.groupingpager.routelist;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GraphWeekAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class RouteListDelegationAdapter extends BaseListAdapter {

    public RouteListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new RouteListAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a));

        setItems(items);
    }

}
