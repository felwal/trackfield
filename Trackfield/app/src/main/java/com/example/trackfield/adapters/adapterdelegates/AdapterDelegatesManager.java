package com.example.trackfield.adapters.adapterdelegates;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterDelegatesManager<T> {

    protected SparseArrayCompat<AdapterDelegate<T>> delegates = new SparseArrayCompat<>();

    ////

    public AdapterDelegatesManager() {}

    public AdapterDelegatesManager(@NonNull AdapterDelegate<T>... delegates) {
        for (AdapterDelegate<T> delegate : delegates) {
            addDelegate(delegate);
        }
    }

    //

    public AdapterDelegatesManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
        int viewType = delegates.size();
        return addDelegate(viewType, delegate); // ?
    }

    private AdapterDelegatesManager<T> addDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        if (delegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered AdapterDelegate is "
                            + delegates.get(viewType));
        }

        delegates.put(viewType, delegate);
        return this; // ?
    }

    //

    public int getItemViewType(@NonNull T items, int position) {
        for (int i = 0; i < delegates.size(); i++) {
            AdapterDelegate<T> delegate = delegates.valueAt(i);
            if (delegate.isForViewType(items, position)) {
                return delegates.keyAt(i);
            }
        }

        throw new IllegalArgumentException("No delegate found");
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdapterDelegate<T> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }

        return delegate.onCreateViewHolder(parent);

        //throw new IllegalArgumentException("No delegate found");
    }

    public void onBindViewHolder(@NonNull T items, @NonNull RecyclerView.ViewHolder holder, int position) {
        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }

        delegate.onBindViewHolder(items, position, holder);
    }

    @Nullable
    public AdapterDelegate<T> getDelegateForViewType(int viewType) {
        return delegates.get(viewType);
    }

}
