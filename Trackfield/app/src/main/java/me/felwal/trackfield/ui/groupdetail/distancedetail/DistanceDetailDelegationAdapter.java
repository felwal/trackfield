package me.felwal.trackfield.ui.groupdetail.distancedetail;

import android.app.Activity;

import java.util.List;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.GoalAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.GraphRecAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

class DistanceDetailDelegationAdapter extends BaseListAdapter {

    DistanceDetailDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items, int originId, int distance) {
        delegatesManager
            .addDelegate(new DistanceDetailExerciseAdapterDelegate(a, listener, this, originId, distance))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphRecAdapterDelegate(a))
            .addDelegate(new GoalAdapterDelegate(a))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
