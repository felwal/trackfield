package com.felwal.trackfield.ui.main.exerciselist;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class ExerciseListAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, ExerciseListAdapterDelegate.ExerciseViewHolder> {

    public ExerciseListAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Exerlite;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ExerciseViewHolder(inflater.inflate(R.layout.item_recycler_exercise, parent, false));
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.printTitle());
        vh.secondaryTv.setText(item.printValues());
        vh.captionTv.setText(item.printCaption());
    }

    // vh

    class ExerciseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;
        public final TextView captionTv;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_secondary);
            captionTv = itemView.findViewById(R.id.tv_recycler_item_exercise_caption);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onDelegateClick(view, getAdapterPosition());
            }
        }

    }

}
