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

@Deprecated public class DistanceRecyclerAdapter extends RecyclerAdapter {

    int distance;

    ////

    public DistanceRecyclerAdapter(ArrayList<RecyclerItem> itemList, int distance, int originId, Context c) {
        super(itemList, c);
        this.distance = distance;
        this.originId = originId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.item_exercise_distance, parent, false);
            return new DistanceExerciseVH(cl);
        }
        else if (viewType == ITEM_GRAPH_OLD) {
            ConstraintLayout cl = GraphOld.inflateLayout(inflater, parent);
            return new GraphVH(parent, cl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
