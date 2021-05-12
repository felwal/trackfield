package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphView;

import java.util.List;

public class GraphWeekAdapterDelegate extends
    BaseAdapterDelegate<Graph, RecyclerItem, GraphWeekAdapterDelegate.GraphWeekViewHolder> {

    public GraphWeekAdapterDelegate(Activity activity) {
        super(activity, null);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_WEEK);
    }

    @NonNull
    @Override
    public GraphWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new GraphWeekViewHolder(inflater.inflate(R.layout.item_graph_week, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Graph item, GraphWeekViewHolder vh, @Nullable List<Object> payloads) {
        vh.surface.restoreDefaultFocus();
        vh.surface.setGraph(item);
    }

    // vh

    static class GraphWeekViewHolder extends RecyclerView.ViewHolder {

        public GraphView surface;

        public GraphWeekViewHolder(View itemView) {
            super(itemView);
            surface = itemView.findViewById(R.id.graphSurface_base);
        }

    }

}
