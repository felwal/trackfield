package me.felwal.trackfield.ui.groupdetail.distancedetail;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.List;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.groupdetail.ExerciseMediumViewHolder;
import me.felwal.trackfield.utils.AppConsts;

class DistanceDetailExerciseAdapterDelegate extends
    BaseAdapterDelegate<Exerlite, RecyclerItem, ExerciseMediumViewHolder> {

    private final BaseListAdapter adapter;
    private final int originId;
    private final int distance;

    //

    DistanceDetailExerciseAdapterDelegate(Activity a, DelegateClickListener listener, BaseListAdapter adapter, int originId,
        int distance) {

        super(a, listener);
        this.adapter = adapter;
        this.originId = originId;
        this.distance = distance;
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Exerlite;
    }

    @NonNull
    @Override
    public ExerciseMediumViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_recycler_exercise_small, parent, false);
        return new ExerciseMediumViewHolder(listener, view);
    }

    @Override
    public void onBindViewHolder(Exerlite item, ExerciseMediumViewHolder vh, @Nullable List<Object> payloads) {
        String date = item.getDate().format(
            adapter.getSortMode() == SorterItem.Mode.DATE || item.isYear(LocalDate.now().getYear())
                ? AppConsts.FORMATTER_GROUP_NOYEAR : AppConsts.FORMATTER_GROUP);

        vh.primaryTv.setText(date);
        vh.secondaryTv.setText(item.printValues(distance));
        vh.captionTv.setText(item.getRoute());
        vh.originMarker.setVisibility(item.hasId(originId) ? View.VISIBLE : View.GONE);
        vh.recordMarker.setVisibility(item.isTop() ? View.VISIBLE : View.GONE);
        vh.recordMarker.getBackground().setColorFilter(item.getMedalColor(c), PorterDuff.Mode.MULTIPLY);
    }

}
