package com.felwal.trackfield.ui.main.groupingpager.placelist;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class PlaceListDelegationAdapter extends BaseListAdapter {

    public PlaceListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new PlaceListAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this));

        setItems(items);
    }

}
