package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphView;
import com.example.trackfield.utils.MathUtils;

import java.util.List;

public class GraphRecAdapterDelegate extends
    BaseAdapterDelegate<Graph, RecyclerItem, GraphRecAdapterDelegate.GraphRecViewHolder> {

    public GraphRecAdapterDelegate(Activity activity) {
        super(activity, null);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_REC);
    }

    @NonNull
    @Override
    public GraphRecViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new GraphRecViewHolder(inflater.inflate(R.layout.item_graph_rec, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Graph item, GraphRecViewHolder vh, @Nullable List<Object> payloads) {
        vh.surface.restoreDefaultFocus();
        vh.surface.setGraph(item);
        vh.low.setText(MathUtils.stringTime(item.getMax(), true));
        vh.high.setText(MathUtils.stringTime(item.getMin(), true));
    }

    // vh

    static class GraphRecViewHolder extends RecyclerView.ViewHolder {

        public GraphView surface;
        public TextView low;
        public TextView high;

        public GraphRecViewHolder(View itemView) {
            super(itemView);
            surface = itemView.findViewById(R.id.graphSurface_rec);
            low = itemView.findViewById(R.id.textView_low);
            high = itemView.findViewById(R.id.textView_high);

            // scroll to start
            final HorizontalScrollView sv = itemView.findViewById(R.id.scrollView_graphSurface);
            sv.post(() -> {
                sv.fullScroll(View.FOCUS_RIGHT);
                sv.scrollTo(sv.getWidth(), 0);
            });
        }

    }

}