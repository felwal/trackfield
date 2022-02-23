package me.felwal.trackfield.ui.main.groupingpager.routelist;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.GraphWeekAdapterDelegate;
import me.felwal.trackfield.ui.common.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class RouteListDelegationAdapter extends BaseListAdapter {

    public RouteListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new RouteListRouteAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a));

        setItems(items);
    }

}
