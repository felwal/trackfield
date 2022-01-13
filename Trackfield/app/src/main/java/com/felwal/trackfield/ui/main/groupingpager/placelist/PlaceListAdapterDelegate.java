package com.felwal.trackfield.ui.main.groupingpager.placelist;

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
import com.felwal.trackfield.ui.main.groupingpager.placelist.model.PlaceItem;

import java.util.List;

public class PlaceListAdapterDelegate extends
    BaseAdapterDelegate<PlaceItem, RecyclerItem, PlaceListAdapterDelegate.LocationViewHolder> {

    public PlaceListAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof PlaceItem;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new LocationViewHolder(inflater.inflate(R.layout.item_recycler_group, parent, false));
    }

    @Override
    public void onBindViewHolder(PlaceItem item, LocationViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getName());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public LocationViewHolder(View itemView) {
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
