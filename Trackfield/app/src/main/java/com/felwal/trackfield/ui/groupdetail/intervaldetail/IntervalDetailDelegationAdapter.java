package com.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GoalAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

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
