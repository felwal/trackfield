package com.example.trackfield.ui.rec.distance;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GoalAdapterDelegate;
import com.example.trackfield.ui.common.GraphRecAdapterDelegate;
import com.example.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class DistanceAdapter extends BaseAdapter {

    DistanceAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items, int originId,
        int distance) {
        delegatesManager
            .addDelegate(new DistanceExerciseAdapterDelegate(activity, listener, this, originId, distance))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphRecAdapterDelegate(activity))
            .addDelegate(new GoalAdapterDelegate(activity))
            .addDelegate(new HeaderSmallAdapterDelegate(activity, listener));

        setItems(items);
    }

}
