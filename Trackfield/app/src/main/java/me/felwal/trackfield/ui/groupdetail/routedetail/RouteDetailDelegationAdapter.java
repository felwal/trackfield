package me.felwal.trackfield.ui.groupdetail.routedetail;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.GoalAdapterDelegate;
import me.felwal.trackfield.ui.common.GraphRecAdapterDelegate;
import me.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class RouteDetailDelegationAdapter extends BaseListAdapter {

    public RouteDetailDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new RouteDetailExerciseAdapterDelegate(a, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphRecAdapterDelegate(a))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
