package com.example.trackfield.ui.rec.interval;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GoalAdapterDelegate;
import com.example.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class IntervalAdapter extends BaseAdapter {

    public IntervalAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new IntervalExerciseAdapterDelegate(activity, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GoalAdapterDelegate(activity))
            .addDelegate(new HeaderSmallAdapterDelegate(activity, listener));

        setItems(items);
    }

}
