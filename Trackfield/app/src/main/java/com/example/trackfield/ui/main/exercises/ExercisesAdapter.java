package com.example.trackfield.ui.main.exercises;

import android.app.Activity;

import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.GraphAdapterDelegate;
import com.example.trackfield.ui.common.HeaderBigAdapterDelegate;
import com.example.trackfield.ui.common.HeaderMediumAdapterDelegate;
import com.example.trackfield.ui.common.HeaderSmallAdapterDelegate;
import com.example.trackfield.ui.common.SorterAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

class ExercisesAdapter extends BaseAdapter {

    public ExercisesAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
        delegatesManager
            .addDelegate(new ExerciseAdapterDelegate(activity, listener))
            .addDelegate(new SorterAdapterDelegate(activity, listener, this))
            .addDelegate(new GraphAdapterDelegate(activity))
            .addDelegate(new HeaderBigAdapterDelegate(activity, listener))
            .addDelegate(new HeaderMediumAdapterDelegate(activity, listener))
            .addDelegate(new HeaderSmallAdapterDelegate(activity, listener));

        setItems(items);
    }

}
