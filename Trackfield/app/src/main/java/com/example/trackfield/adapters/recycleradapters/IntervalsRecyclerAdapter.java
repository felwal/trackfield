package com.example.trackfield.adapters.recycleradapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;

public class IntervalsRecyclerAdapter extends RecyclerAdapter {

    public IntervalsRecyclerAdapter(ArrayList<RecyclerItem> itemList, Context c) {
        super(itemList, c);
    }
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.layout_item_rec, parent, false);
            return new IntervalVH(rl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
