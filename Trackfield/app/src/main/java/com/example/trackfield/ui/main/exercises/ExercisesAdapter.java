package com.example.trackfield.ui.main.exercises;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphWeekAdapterDelegate;
import com.example.trackfield.ui.common.HeaderBigAdapterDelegate;
import com.example.trackfield.ui.common.HeaderMediumAdapterDelegate;
import com.example.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

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
