package me.felwal.trackfield.ui.main.exerciselist;

import android.app.Activity;

import java.util.List;

import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.adapterdelegate.GraphWeekAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.HeaderBigAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.HeaderMediumAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.HeaderSmallAdapterDelegate;
import me.felwal.trackfield.ui.common.adapterdelegate.SorterAdapterDelegate;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

class ExerciseListDelegationAdapter extends BaseListAdapter {

    public ExerciseListDelegationAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new ExerciseListAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a))
            .addDelegate(new HeaderBigAdapterDelegate(a, listener))
            .addDelegate(new HeaderMediumAdapterDelegate(a, listener))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
