package com.example.trackfield.adapters.recycler_adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;

public class DiRecyclerAdapter extends RecyclerAdapter {

    public DiRecyclerAdapter(ArrayList<RecyclerItem> itemList, Context c) {
        super(itemList, c);
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.layout_item_rec, parent, false);
            return new DistanceVH(rl);
        }
        else if (viewType == ITEM_GRAPH_OLD) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.chart, parent, false);
            return new GraphVH(parent, cl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
