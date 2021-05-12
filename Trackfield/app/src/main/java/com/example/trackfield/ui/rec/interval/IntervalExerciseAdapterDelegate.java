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
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.utils.Constants;

import java.time.LocalDate;
import java.util.List;

public class IntervalExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, IntervalExerciseAdapterDelegate.ExerciseMediumViewHolder> {

    private BaseAdapter adapter;
    private int originId;

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
            + Constants.TAB + item.printTime()
            + Constants.TAB + item.printPace();

        String date = item.getDate().format(
            adapter.getSortMode() == Constants.SortMode.DATE || item.isYear(LocalDate.now().getYear()) ?
                Constants.FORMATTER_REC_NOYEAR : Constants.FORMATTER_REC);

        vh.primary.setText(date);
        vh.secondary.setText(values);
        vh.caption.setText(item.getRoute());
        vh.originMarker.setVisibility(item.has_id(originId) ? View.VISIBLE : View.GONE);
        vh.recordMarker.setVisibility(View.GONE);
    }

    // vh

    class ExerciseMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView primary;
        public TextView secondary;
        public TextView caption;
        public View originMarker;
        public View recordMarker;

        public ExerciseMediumViewHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.textView_primary);
            secondary = itemView.findViewById(R.id.textView_secondary);
            caption = itemView.findViewById(R.id.textView_caption);
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
