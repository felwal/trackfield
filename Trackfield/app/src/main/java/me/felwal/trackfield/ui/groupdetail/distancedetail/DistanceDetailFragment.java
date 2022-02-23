package me.felwal.trackfield.ui.groupdetail.distancedetail;

import android.os.Bundle;
import android.view.View;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.model.Distance;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.Goal;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.ui.widget.graph.Borders;
import me.felwal.trackfield.ui.widget.graph.Graph;
import me.felwal.trackfield.ui.widget.graph.GraphData;
import me.felwal.trackfield.utils.AppConsts;
import me.felwal.trackfield.utils.TypeUtils;

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;

public class DistanceDetailFragment extends RecyclerFragment {

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

    private int originId;
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
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setDistanceVisibleTypes(originId == -1
                ? Prefs.getExerciseVisibleTypes()
                : TypeUtils.createList(DbReader.get(a).getExercise(originId).getType()));
        }
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distancedetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distancedetail_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_run, R.attr.tf_colorDistance));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCE_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCE_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new DistanceDetailDelegationAdapter(a, this, items, originId, distance);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sorter.getMode(),
            sorter.getAscending(), Prefs.getDistanceVisibleTypes());

        if (exerliteList.size() != 0) {
            GraphData data = new GraphData(
                a, DbReader.get(a).getPaceNodesByDistance(distance, Prefs.getDistanceVisibleTypes()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_GROUP);
                itemList.add(graph);
            }

            itemList.add(sorter.copy());
            float goalPace = DbReader.get(a).getDistanceGoal(distance);
            if (goalPace != Distance.NO_GOAL_PACE) {
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

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            if (originId != id) ExerciseDetailActivity.startActivity(a, id, ExerciseDetailActivity.FROM_DISTANCE);
        }

        super.onDelegateClick(item);
    }

}
