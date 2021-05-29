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

public class GraphAdapterDelegate extends
    BaseAdapterDelegate<Graph, RecyclerItem, GraphAdapterDelegate.GraphViewHolder> {

    public GraphAdapterDelegate(Activity a) {
        super(a, null);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_BASE);
    }

    @NonNull
    @Override
    public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new GraphViewHolder(inflater.inflate(R.layout.item_recycler_graph, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Graph item, GraphViewHolder vh, @Nullable List<Object> payloads) {
        vh.graphView.restoreDefaultFocus();
        vh.graphView.setGraph(item);
    }

    // vh

    static class GraphViewHolder extends RecyclerView.ViewHolder {

        public final GraphView graphView;

        public GraphViewHolder(View itemView) {
            super(itemView);
            graphView = itemView.findViewById(R.id.cv_recycler_item_graph_surface);
        }

    }

}
