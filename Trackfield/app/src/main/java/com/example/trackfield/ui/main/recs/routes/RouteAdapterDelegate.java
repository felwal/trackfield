package com.example.trackfield.ui.main.recs.routes;

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
import com.example.trackfield.ui.main.recs.routes.model.RouteItem;

import java.util.List;

public class RouteAdapterDelegate extends
    BaseAdapterDelegate<RouteItem, RecyclerItem, RouteAdapterDelegate.RouteViewHolder> {

    public RouteAdapterDelegate(Activity a, DelegateClickListener listener) {
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
        return new RouteViewHolder(inflater.inflate(R.layout.item_rec, parent, false));
    }

    @Override
    public void onBindViewHolder(RouteItem item, RouteViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getName());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primaryTv;
        public TextView secondaryTv;

        public RouteViewHolder(View itemView) {
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
