package com.felwal.trackfield.ui.groupdetail.distancedetail;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GoalAdapterDelegate;
import com.felwal.trackfield.ui.common.GraphRecAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

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
