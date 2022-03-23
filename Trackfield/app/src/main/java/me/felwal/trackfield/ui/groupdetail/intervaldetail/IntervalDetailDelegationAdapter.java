package me.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.app.Activity;

import java.util.List;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.GoalAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

class IntervalDetailDelegationAdapter extends BaseListAdapter {

    public IntervalDetailDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items,
        int originId) {

        delegatesManager
            .addDelegate(new IntervalDetailExerciseAdapterDelegate(a, listener, this, originId))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
