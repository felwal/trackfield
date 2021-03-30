package com.example.trackfield.adapters.recycleradapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.archive.GraphOld;

import java.util.ArrayList;

public class RouteRecyclerAdapter extends RecyclerAdapter {

    public RouteRecyclerAdapter(ArrayList<RecyclerItem> itemList, int originId, Context c) {
        super(itemList, c);
        this.originId = originId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.item_exercise_route, parent, false);
            return new RouteExerciseVH(cl);
        }
        else if (viewType == ITEM_GRAPH_OLD) {
            ConstraintLayout cl = GraphOld.inflateLayout(inflater, parent);
        /*ConstraintLayout[] elements = new ConstraintLayout[graphLength];
        for (int i = 0; i < graphLength; i++) {
            elements[i] = (ConstraintLayout) inflater.inflate(R.layout.chart_element_point, parent, false);
        }*/
            return new GraphVH(parent, cl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
