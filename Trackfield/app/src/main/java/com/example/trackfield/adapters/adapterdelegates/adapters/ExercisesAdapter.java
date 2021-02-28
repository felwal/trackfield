package com.example.trackfield.adapters.adapterdelegates.adapters;

import android.content.Context;

import com.example.trackfield.adapters.adapterdelegates.AdapterDelegate;
import com.example.trackfield.adapters.adapterdelegates.ListDelegationAdapter;
import com.example.trackfield.adapters.adapterdelegates.delegates.ExerciseAdapterDelegate;
import com.example.trackfield.adapters.adapterdelegates.delegates.SorterAdapterDelegate;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.toolbox.C;

import java.util.ArrayList;

public class ExercisesAdapter extends ListDelegationAdapter {

    protected C.SortMode sortMode = C.SortMode.DATE;

    public ExercisesAdapter(ArrayList<RecyclerItem> items, Context c, AdapterDelegate.ItemClickListener l) {
        super();

        delegatesManager.addDelegate(new ExerciseAdapterDelegate(c, this));
        delegatesManager.addDelegate(new SorterAdapterDelegate(c, this));

        setItems(items);
    }

}
