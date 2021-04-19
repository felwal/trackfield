package com.example.trackfield.ui.main.recs.distances;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.main.RecyclerAdapter;

import java.util.ArrayList;

public class DistancesRecyclerAdapter extends RecyclerAdapter {

    public DistancesRecyclerAdapter(ArrayList<RecyclerItem> itemList, Context c) {
        super(itemList, c);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.item_rec, parent, false);
            return new DistanceVH(rl);
        }
        else if (viewType == ITEM_GRAPH_OLD) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.dep_chart, parent, false);
            return new GraphVH(parent, cl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
