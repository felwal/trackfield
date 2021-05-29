package com.felwal.trackfield.ui.record.route;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GoalAdapterDelegate;
import com.felwal.trackfield.ui.common.GraphRecAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class RouteAdapter extends BaseAdapter {

    public RouteAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new RouteExerciseAdapterDelegate(a, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphRecAdapterDelegate(a))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
