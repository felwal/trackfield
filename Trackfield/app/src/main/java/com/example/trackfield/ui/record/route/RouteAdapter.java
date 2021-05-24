package com.example.trackfield.ui.record.route;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GoalAdapterDelegate;
import com.example.trackfield.ui.common.GraphRecAdapterDelegate;
import com.example.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

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
