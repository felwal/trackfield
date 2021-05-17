package com.example.trackfield.ui.rec.interval;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.model.SortMode;

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
        return new ExerciseMediumViewHolder(inflater.inflate(R.layout.item_exercise_distance, parent, false));
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseMediumViewHolder vh, @Nullable List<Object> payloads) {
        String values = item.printDistance()
            + AppConsts.TAB + item.printTime()
            + AppConsts.TAB + item.printPace();

        String date = item.getDate().format(
            adapter.getSortMode() == SortMode.Mode.DATE || item.isYear(LocalDate.now().getYear()) ?
                AppConsts.FORMATTER_REC_NOYEAR : AppConsts.FORMATTER_REC);

        vh.primaryTv.setText(date);
        vh.secondaryTv.setText(values);
        vh.captionTv.setText(item.getRoute());
        vh.originMarker.setVisibility(item.hasId(originId) ? View.VISIBLE : View.GONE);
        vh.recordMarker.setVisibility(View.GONE);
    }

    // vh

    class ExerciseMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primaryTv;
        public TextView secondaryTv;
        public TextView captionTv;
        public View originMarker;
        public View recordMarker;

        public ExerciseMediumViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.textView_primary);
            secondaryTv = itemView.findViewById(R.id.textView_secondary);
            captionTv = itemView.findViewById(R.id.textView_caption);
            originMarker = itemView.findViewById(R.id.view_orignMarker);
            recordMarker = itemView.findViewById(R.id.view_recordMarker);
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
