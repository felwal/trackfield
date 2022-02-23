package com.felwal.trackfield.ui.groupdetail.placedetail;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.utils.AppConsts;

import java.time.LocalDate;
import java.util.List;

class PlaceDetailExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, PlaceDetailExerciseAdapterDelegate.ExerciseMediumViewHolder> {

    private final BaseListAdapter adapter;

    //

    PlaceDetailExerciseAdapterDelegate(Activity a, DelegateClickListener listener, BaseListAdapter adapter) {
        super(a, listener);
        this.adapter = adapter;
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Exerlite;
    }

    @NonNull
    @Override
    public ExerciseMediumViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ExerciseMediumViewHolder(inflater.inflate(R.layout.item_recycler_exercise_small, parent, false));
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseMediumViewHolder vh, @Nullable List<Object> payloads) {
        String date = item.getDate().format(
            adapter.getSortMode() == SorterItem.Mode.DATE || item.isYear(LocalDate.now().getYear())
                ? AppConsts.FORMATTER_GROUP_NOYEAR : AppConsts.FORMATTER_GROUP);

        vh.primaryTv.setText(date);
        vh.secondaryTv.setText(item.printValues());
        vh.captionTv.setText(item.getRoute());
        vh.originMarker.setVisibility(View.GONE);
        vh.recordMarker.setVisibility(item.isTop() ? View.VISIBLE : View.GONE);
        vh.recordMarker.getBackground().setColorFilter(item.getMedalColor(c), PorterDuff.Mode.MULTIPLY);
    }

    // vh

    class ExerciseMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;
        public final TextView captionTv;
        public final View originMarker;
        public final View recordMarker;

        public ExerciseMediumViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_secondary);
            captionTv = itemView.findViewById(R.id.tv_recycler_item_exercise_caption);
            originMarker = itemView.findViewById(R.id.v_recycler_item_originmarker);
            recordMarker = itemView.findViewById(R.id.v_recycler_item_recordmarker);
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
