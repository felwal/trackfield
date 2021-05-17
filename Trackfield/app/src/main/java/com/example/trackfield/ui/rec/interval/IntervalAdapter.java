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

    public IntervalAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new IntervalExerciseAdapterDelegate(a, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
