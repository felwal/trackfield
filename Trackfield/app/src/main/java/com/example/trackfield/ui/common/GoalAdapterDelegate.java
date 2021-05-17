package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.common.model.Goal;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class GoalAdapterDelegate extends BaseAdapterDelegate<Goal, RecyclerItem, GoalAdapterDelegate.GoalViewHolder> {

    public GoalAdapterDelegate(Activity a) {
        super(a, null);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Goal;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new GoalViewHolder(inflater.inflate(R.layout.item_goal, parent, false));
    }

    @Override
    public void onBindViewHolder(Goal item, GoalViewHolder vh, @Nullable List<Object> payloads) {
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    public static class GoalViewHolder extends RecyclerView.ViewHolder {

        public TextView primaryTv;
        public TextView secondaryTv;

        public GoalViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.textView_primary);
            secondaryTv = itemView.findViewById(R.id.textView_secondary);
        }

    }

}
