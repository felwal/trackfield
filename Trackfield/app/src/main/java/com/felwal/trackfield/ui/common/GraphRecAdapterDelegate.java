package com.felwal.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.widget.graph.Graph;
import com.felwal.trackfield.ui.widget.graph.GraphView;
import com.felwal.trackfield.utils.MathUtils;

import java.util.List;

public class GraphRecAdapterDelegate extends
    BaseAdapterDelegate<Graph, RecyclerItem, GraphRecAdapterDelegate.GraphRecViewHolder> {

    public GraphRecAdapterDelegate(Activity a) {
        super(a, null);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_GROUP);
    }

    @NonNull
    @Override
    public GraphRecViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new GraphRecViewHolder(inflater.inflate(R.layout.item_recycler_graph_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Graph item, GraphRecViewHolder vh, @Nullable List<Object> payloads) {
        vh.graphView.restoreDefaultFocus();
        vh.graphView.setGraph(item);
        vh.lowTv.setText(MathUtils.stringTime(item.getMax(), true));
        vh.highTv.setText(MathUtils.stringTime(item.getMin(), true));
    }

    // vh

    static class GraphRecViewHolder extends RecyclerView.ViewHolder {

        public final GraphView graphView;
        public final TextView lowTv;
        public final TextView highTv;

        public GraphRecViewHolder(View itemView) {
            super(itemView);
            graphView = itemView.findViewById(R.id.cv_recycler_item_graph_surface);
            lowTv = itemView.findViewById(R.id.tv_recycler_item_graph_low);
            highTv = itemView.findViewById(R.id.tv_recycler_item_graph_high);

            // scroll to start
            final HorizontalScrollView sv = itemView.findViewById(R.id.sv_recycler_item_graph);
            sv.post(() -> {
                sv.fullScroll(View.FOCUS_RIGHT);
                sv.scrollTo(sv.getWidth(), 0);
            });
        }

    }

}
