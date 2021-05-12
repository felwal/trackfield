package com.example.trackfield.ui.main.recs.distances;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class DistancesAdapter extends BaseAdapter {

    public DistancesAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new DistanceAdapterDelegate(activity, listener))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphAdapterDelegate(activity));

        setItems(items);
    }

}
