package me.felwal.trackfield.ui.main.groupingpager.routelist;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.main.groupingpager.routelist.model.RouteItem;

import java.util.List;

public class RouteListRouteAdapterDelegate extends
    BaseAdapterDelegate<RouteItem, RecyclerItem, RouteListRouteAdapterDelegate.RouteViewHolder> {

    public RouteListRouteAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof RouteItem;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new RouteViewHolder(inflater.inflate(R.layout.item_recycler_group, parent, false));
    }

    @Override
    public void onBindViewHolder(RouteItem item, RouteViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getName());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public RouteViewHolder(View itemView) {
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
