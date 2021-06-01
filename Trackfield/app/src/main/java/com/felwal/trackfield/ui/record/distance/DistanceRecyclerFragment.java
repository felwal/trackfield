package com.felwal.trackfield.ui.record.distance;

import android.os.Bundle;
import android.view.View;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.model.Distance;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.Goal;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.Sorter;
import com.felwal.trackfield.ui.custom.graph.Borders;
import com.felwal.trackfield.ui.custom.graph.Graph;
import com.felwal.trackfield.ui.custom.graph.GraphData;
import com.felwal.trackfield.ui.exercise.ViewActivity;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.TypeUtils;
import com.felwal.trackfield.utils.model.SortMode;

import java.util.ArrayList;

public class DistanceRecyclerFragment extends RecyclerFragment {

    // bundle keys
    private final static String BUNDLE_DISTANCE = "distance";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    private final Sorter sorter = new Sorter(
        new SortMode("Date", SortMode.Mode.DATE, false),
        new SortMode("Pace & Avg time", SortMode.Mode.PACE, true),
        new SortMode("Full distance", SortMode.Mode.DISTANCE, true)
    );

    private int originId;
    private int distance;

    //

    public static DistanceRecyclerFragment newInstance(int distance, int originId) {
        DistanceRecyclerFragment instance = new DistanceRecyclerFragment();
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
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sorter.getMode(),
            sorter.isAscending(), Prefs.getDistanceVisibleTypes());

        if (exerliteList.size() != 0) {
            GraphData data = new GraphData(
                DbReader.get(a).getPaceNodesByDistance(distance, Prefs.getDistanceVisibleTypes()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
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
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCE),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCE));
    }

    @Override
    protected void setAdapter() {
        adapter = new DistanceAdapter(a, this, items, originId, distance);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distance_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distance_msg));
        emptyImage.setImageResource(R.drawable.ic_empty_distance);
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.DISTANCE, sorter.getSelectedIndex(), sorter.isOrderInverted());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            if (originId != id) ViewActivity.startActivity(a, id, ViewActivity.FROM_DISTANCE);
        }

        super.onDelegateClick(item);
    }

}
