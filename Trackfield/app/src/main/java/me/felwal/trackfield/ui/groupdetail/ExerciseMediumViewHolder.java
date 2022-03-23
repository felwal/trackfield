package me.felwal.trackfield.ui.groupdetail;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.DelegateClickListener;

public class ExerciseMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public final TextView primaryTv;
    public final TextView secondaryTv;
    public final TextView captionTv;
    public final View originMarker;
    public final View recordMarker;

    private final DelegateClickListener listener;

    public ExerciseMediumViewHolder(DelegateClickListener listener, View itemView) {
        super(itemView);
        primaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_primary);
        secondaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_secondary);
        captionTv = itemView.findViewById(R.id.tv_recycler_item_exercise_caption);
        originMarker = itemView.findViewById(R.id.v_recycler_item_originmarker);
        recordMarker = itemView.findViewById(R.id.v_recycler_item_recordmarker);
        itemView.setOnClickListener(this);
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onDelegateClick(view, getAdapterPosition());
        }
    }

}
