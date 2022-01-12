package com.felwal.trackfield.ui.main.groupingpager.distancelist;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.main.groupingpager.distancelist.model.DistanceItem;
import com.felwal.trackfield.utils.MathUtils;

import java.util.List;

public class DistanceListAdapterDelegate extends
    BaseAdapterDelegate<DistanceItem, RecyclerItem, DistanceListAdapterDelegate.DistanceViewHolder> {

    public DistanceListAdapterDelegate(Activity a, DelegateClickListener listener) {
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
        return new DistanceViewHolder(inflater.inflate(R.layout.item_recycler_group, parent, false));
    }

    @Override
    public void onBindViewHolder(DistanceItem item, DistanceViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.printTitle());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class DistanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public DistanceViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_group_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_group_secondary);
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
