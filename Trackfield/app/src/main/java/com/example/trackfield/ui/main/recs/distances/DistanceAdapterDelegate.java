package com.example.trackfield.ui.main.recs.distances;

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
import com.example.trackfield.ui.main.recs.distances.model.DistanceItem;
import com.example.trackfield.utils.MathUtils;

import java.util.List;

public class DistanceAdapterDelegate extends
    BaseAdapterDelegate<DistanceItem, RecyclerItem, DistanceAdapterDelegate.DistanceViewHolder> {

    public DistanceAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof DistanceItem;
    }

    @NonNull
    @Override
    public DistanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new DistanceViewHolder(inflater.inflate(R.layout.item_rec, parent, false));
    }

    @Override
    public void onBindViewHolder(DistanceItem item, DistanceViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(MathUtils.prefix(item.getDistance(), 2, "m"));
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class DistanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primaryTv;
        public TextView secondaryTv;

        public DistanceViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.textView_primary);
            secondaryTv = itemView.findViewById(R.id.textView_secondary);
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
