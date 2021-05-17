package com.example.trackfield.ui.main.stats;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class StatsAdapter extends BaseAdapter {

    public StatsAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new GraphAdapterDelegate(a));

        setItems(items);
    }

}
