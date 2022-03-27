package me.felwal.trackfield.ui.main.statistics;

import android.app.Activity;

import java.util.List;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.GraphAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

public class StatisticsDelegationAdapter extends BaseListAdapter {

    public StatisticsDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new GraphAdapterDelegate(a));

        setItems(items);
    }

}
