package me.felwal.trackfield.ui.groupdetail.distancedetail;

import android.os.Bundle;

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.model.Distance;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.Goal;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.ui.groupdetail.GroupDetailFragment;
import me.felwal.trackfield.ui.widget.graph.Borders;
import me.felwal.trackfield.ui.widget.graph.Graph;
import me.felwal.trackfield.ui.widget.graph.GraphData;
import me.felwal.trackfield.utils.AppConsts;

public class DistanceDetailFragment extends GroupDetailFragment {

    // bundle keys
    private final static String BUNDLE_DISTANCE = "distance";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByPace("Pace & Avg time"),
        SorterItem.sortByDistance("Full distance"),
        SorterItem.sortByLat(),
        SorterItem.sortByLng()
    );

    private int distance;

    //

    public static DistanceDetailFragment newInstance(int distance, int originId) {
        DistanceDetailFragment instance = new DistanceDetailFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(BUNDLE_DISTANCE, distance);
        bundle.putInt(BUNDLE_ORIGIN_ID, originId);

        instance.setArguments(bundle);
        return instance;
    }

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            distance = bundle.getInt(BUNDLE_DISTANCE, -1);
            setOriginId(bundle.getInt(BUNDLE_ORIGIN_ID, Exercise.ID_NONE));
        }

        updateFilterByOrigin();
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distancedetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distancedetail_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_exercise, R.attr.tf_colorDistance));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCE_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCE_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new DistanceDetailDelegationAdapter(a, this, items, getOriginId(), distance);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sorter.getMode(),
            sorter.getAscending(), Prefs.getGroupFilter());

        if (exerliteList.size() != 0) {
            GraphData data = new GraphData(
                a, DbReader.get(a).getPaceNodesByDistance(distance, Prefs.getGroupFilter()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_GROUP);
                itemList.add(graph);
            }

            itemList.add(sorter.copy());
            float goalPace = DbReader.get(a).getDistanceGoal(distance);
            if (goalPace != Distance.GOAL_PACE_NONE) {
                Goal goal = new Goal(goalPace, distance);
                itemList.add(goal);
            }
            addItemsWithHeaders(itemList, exerliteList, sorter.getMode());

            fadeOutEmpty();
        }
        else fadeInEmpty();

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.DISTANCE_DETAIL, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements GroupDetailFragment

    @Override
    public int getFrom() {
        return ExerciseDetailActivity.FROM_DISTANCE;
    }

}
