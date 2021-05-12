package com.example.trackfield.ui.rec.route;

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

    public RouteAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new RouteExerciseAdapterDelegate(activity, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphRecAdapterDelegate(activity))
            .addDelegate(new GoalAdapterDelegate(activity))
            .addDelegate(new HeaderSmallAdapterDelegate(activity, listener));

        setItems(items);
    }

}
