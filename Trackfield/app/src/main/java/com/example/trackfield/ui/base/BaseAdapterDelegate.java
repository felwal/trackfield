package com.example.trackfield.ui.base;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.ui.base.DelegateClickListener;
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate;

import java.util.List;

public abstract class BaseAdapterDelegate<I extends T, T, VH extends RecyclerView.ViewHolder>
    extends AbsListItemAdapterDelegate<I, T, VH> {

    protected Context context;
    protected LayoutInflater inflater;
    protected DelegateClickListener listener;

    //

    public BaseAdapterDelegate(Activity activity, DelegateClickListener listener) {
        context = activity;
        inflater = activity.getLayoutInflater();
        this.listener = listener;
    }

    //

    @Override
    protected boolean isForViewType(@NonNull T item, @NonNull List<T> items, int position) {
        return isForViewType(item);
    }

    public abstract boolean isForViewType(@NonNull T item);

}
