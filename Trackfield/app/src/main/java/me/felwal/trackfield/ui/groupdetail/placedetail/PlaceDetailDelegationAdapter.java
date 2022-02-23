package me.felwal.trackfield.ui.groupdetail.placedetail;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class PlaceDetailDelegationAdapter extends BaseListAdapter {

    PlaceDetailDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new PlaceDetailExerciseAdapterDelegate(a, listener, this))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
