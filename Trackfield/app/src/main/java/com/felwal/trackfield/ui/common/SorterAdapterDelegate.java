package com.felwal.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.Sorter;

import java.util.List;

public class SorterAdapterDelegate extends
    BaseAdapterDelegate<Sorter, RecyclerItem, SorterAdapterDelegate.SorterViewHolder> {

    private final BaseAdapter adapter;

    //

    public SorterAdapterDelegate(Activity a, DelegateClickListener listener, BaseAdapter adapter) {
        super(a, listener);
        this.adapter = adapter;
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Sorter;
    }

    @NonNull
    @Override
    public SorterViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new SorterViewHolder(inflater.inflate(R.layout.item_recycler_sorter, parent, false));
    }

    @Override
    public void onBindViewHolder(Sorter item, SorterViewHolder vh, @Nullable List<Object> payloads) {
        vh.titleTv.setText(item.getTitle());
        adapter.setSortMode(item.getMode());
    }

    // vh

    public class SorterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView titleTv;

        public SorterViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.tv_recycler_item_sorter);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onDelegateClick(view, getAdapterPosition());
            }
        }

    }

}
