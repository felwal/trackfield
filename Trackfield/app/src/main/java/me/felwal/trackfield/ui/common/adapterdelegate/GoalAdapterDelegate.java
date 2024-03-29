package me.felwal.trackfield.ui.common.adapterdelegate;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.common.model.Goal;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

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
        return new GoalViewHolder(inflater.inflate(R.layout.item_recycler_goal, parent, false));
    }

    @Override
    public void onBindViewHolder(Goal item, GoalViewHolder vh, @Nullable List<Object> payloads) {
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    public static class GoalViewHolder extends RecyclerView.ViewHolder {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public GoalViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_goal_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_goal_secondary);
        }

    }

}
