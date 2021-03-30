package com.example.trackfield.ui.main.recs.intervals;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.main.recs.general.items.RecyclerItem;
import com.example.trackfield.ui.main.RecyclerAdapter;

import java.util.ArrayList;

public class IntervalsRecyclerAdapter extends RecyclerAdapter {

    public IntervalsRecyclerAdapter(ArrayList<RecyclerItem> itemList, Context c) {
        super(itemList, c);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_ITEM) {
            RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.item_rec, parent, false);
            return new IntervalVH(rl);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

}
