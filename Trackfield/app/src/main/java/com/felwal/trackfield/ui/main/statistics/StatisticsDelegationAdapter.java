package com.felwal.trackfield.ui.main.statistics;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GraphAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class StatisticsDelegationAdapter extends BaseListAdapter {

    public StatisticsDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new GraphAdapterDelegate(a));

        setItems(items);
    }

}
