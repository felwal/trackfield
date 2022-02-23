package me.felwal.trackfield.ui.main.groupingpager.distancelist;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.GraphWeekAdapterDelegate;
import me.felwal.trackfield.ui.common.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class DistanceListDelegationAdapter extends BaseListAdapter {

    public DistanceListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new DistanceListDistanceAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a));

        setItems(items);
    }

}
