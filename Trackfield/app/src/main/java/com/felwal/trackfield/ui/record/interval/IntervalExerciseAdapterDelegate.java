package com.felwal.trackfield.ui.record.interval;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapter;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.model.SortMode;

import java.time.LocalDate;
import java.util.List;

public class IntervalExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, IntervalExerciseAdapterDelegate.ExerciseMediumViewHolder> {

    private final BaseAdapter adapter;
    private final int originId;

    //

    public IntervalExerciseAdapterDelegate(Activity activity, DelegateClickListener listener, BaseAdapter adapter,
        int originId) {

        super(activity, listener);
        this.adapter = adapter;
        this.originId = originId;
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
            adapter.getSortMode() == SortMode.Mode.DATE || item.isYear(LocalDate.now().getYear()) ?
                AppConsts.FORMATTER_REC_NOYEAR : AppConsts.FORMATTER_REC);

        vh.primaryTv.setText(date);
        vh.secondaryTv.setText(item.printValues());
        vh.captionTv.setText(item.getRoute());
        vh.originMarker.setVisibility(item.hasId(originId) ? View.VISIBLE : View.GONE);
        vh.recordMarker.setVisibility(View.GONE);
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