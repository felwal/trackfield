package me.felwal.trackfield.ui.groupdetail.routedetail;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.utils.AppConsts;

public class RouteDetailExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, RouteDetailExerciseAdapterDelegate.ExerciseSmallViewHolder> {

    private final BaseListAdapter adapter;
    private final int originId;

    //

    public RouteDetailExerciseAdapterDelegate(Activity a, DelegateClickListener listener, BaseListAdapter adapter,
        int originId) {

        super(a, listener);
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
    public ExerciseSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ExerciseSmallViewHolder(inflater.inflate(R.layout.item_recycler_exercise_tiny, parent, false));
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseSmallViewHolder vh, @Nullable List<Object> payloads) {
        String date = item.getDate().format(
            adapter.getSortMode() == SorterItem.Mode.DATE || item.isYear(LocalDate.now().getYear())
                ? AppConsts.FORMATTER_GROUP_NOYEAR : AppConsts.FORMATTER_GROUP);

        vh.primaryTv.setText(date);
        vh.secondaryTv.setText(item.printValues());
        vh.originMarker.setVisibility(item.hasId(originId) ? View.VISIBLE : View.GONE);
        vh.recordMarker.setVisibility(item.isTop() ? View.VISIBLE : View.GONE);
        vh.recordMarker.getBackground().setColorFilter(item.getMedalColor(c), PorterDuff.Mode.MULTIPLY);
    }

    // vh

    class ExerciseSmallViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;
        public final View originMarker;
        public final View recordMarker;

        public ExerciseSmallViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_exercise_secondary);
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
