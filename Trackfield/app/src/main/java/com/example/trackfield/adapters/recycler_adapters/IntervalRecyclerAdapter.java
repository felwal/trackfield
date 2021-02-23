package com.example.trackfield.adapters.recycler_adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;

public class IntervalRecyclerAdapter extends RecyclerAdapter {

    public IntervalRecyclerAdapter(ArrayList<RecyclerItem> itemList, int originId, Context c) {
        super(itemList, c);
        this.originId = originId;
    }
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_exercise_distance, parent, false);
            return new IntervalExerciseVH(cl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
