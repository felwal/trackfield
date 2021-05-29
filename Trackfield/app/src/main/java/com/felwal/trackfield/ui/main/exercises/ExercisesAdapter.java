package com.felwal.trackfield.ui.main.exercises;

import android.app.Activity;

import com.felwal.trackfield.ui.base.BaseAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.GraphWeekAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderBigAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderMediumAdapterDelegate;
import com.felwal.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.felwal.trackfield.ui.common.SorterAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class ExercisesAdapter extends BaseAdapter {

    public ExercisesAdapter(Activity a, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new ExerciseAdapterDelegate(a, listener))
            .addDelegate(new SorterAdapterDelegate(a, listener, this))
            .addDelegate(new GraphWeekAdapterDelegate(a))
            .addDelegate(new HeaderBigAdapterDelegate(a, listener))
            .addDelegate(new HeaderMediumAdapterDelegate(a, listener))
            .addDelegate(new HeaderSmallAdapterDelegate(a, listener));

        setItems(items);
    }

}
