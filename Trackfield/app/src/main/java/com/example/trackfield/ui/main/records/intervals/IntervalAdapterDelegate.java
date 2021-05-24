package com.example.trackfield.ui.main.records.intervals;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.main.records.intervals.model.IntervalItem;

import java.util.List;

public class IntervalAdapterDelegate extends
    BaseAdapterDelegate<IntervalItem, RecyclerItem, IntervalAdapterDelegate.IntervalViewHolder> {

    public IntervalAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof IntervalItem;
    }

    @NonNull
    @Override
    public IntervalViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new IntervalViewHolder(inflater.inflate(R.layout.item_recycler_record, parent, false));
    }

    @Override
    public void onBindViewHolder(IntervalItem item, IntervalViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getInterval());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class IntervalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primaryTv;
        public TextView secondaryTv;

        public IntervalViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_record_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_record_secondary);
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
