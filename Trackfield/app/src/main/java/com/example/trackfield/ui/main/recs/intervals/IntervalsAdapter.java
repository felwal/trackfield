package com.example.trackfield.ui.main.recs.intervals;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphWeekAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class IntervalsAdapter extends BaseAdapter {

    public IntervalsAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new IntervalAdapterDelegate(activity, listener))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(activity));

        setItems(items);
    }

}
