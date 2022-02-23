package me.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.GoalAdapterDelegate;
import me.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class IntervalDetailDelegationAdapter extends BaseListAdapter {

    public IntervalDetailDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items, int originId) {
        delegatesManager
            .addDelegate(new IntervalDetailAdapterDelegate(a, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
