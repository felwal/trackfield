package com.example.trackfield.ui.main.recs.routes;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class RoutesAdapter extends BaseAdapter {

    public RoutesAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new RouteAdapterDelegate(activity, listener))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphAdapterDelegate(activity));

        setItems(items);
    }

}
