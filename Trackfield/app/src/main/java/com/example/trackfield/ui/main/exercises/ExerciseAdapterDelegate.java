package com.example.trackfield.ui.main.exercises;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Exerlite;

import java.util.List;

public class ExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, ExerciseAdapterDelegate.ExerciseViewHolder> {

    public ExerciseAdapterDelegate(Activity activity, DelegateClickListener listener) {
        super(activity, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Exerlite;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ExerciseViewHolder(inflater.inflate(R.layout.item_exercise, parent, false));
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseViewHolder vh, @Nullable List<Object> payloads) {
        vh.primary.setText(item.printPrimary());
        vh.secondary.setText(item.printDistanceTimePace());
        vh.caption.setText(item.printCaption());
    }

    // vh

    class ExerciseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primary;
        public TextView secondary;
        public TextView caption;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.textView_primary);
            secondary = itemView.findViewById(R.id.textView_secondary);
            caption = itemView.findViewById(R.id.textView_caption);
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
