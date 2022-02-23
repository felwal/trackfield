package me.felwal.trackfield.ui.main.statistics;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.GraphAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class StatisticsDelegationAdapter extends BaseListAdapter {

    public StatisticsDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new GraphAdapterDelegate(a));

        setItems(items);
    }

}
