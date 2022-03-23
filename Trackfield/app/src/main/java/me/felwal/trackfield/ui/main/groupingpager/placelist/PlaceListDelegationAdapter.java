package me.felwal.trackfield.ui.main.groupingpager.placelist;

import android.app.Activity;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class PlaceListDelegationAdapter extends BaseListAdapter {

    public PlaceListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new PlaceListPlaceAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this));

        setItems(items);
    }

}
