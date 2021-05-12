package com.example.trackfield.ui.rec.distance;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.ui.common.model.Goal;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;

import java.util.ArrayList;
import java.util.TreeMap;

public class DistanceRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Pace & Avg time", "Full distance" };
    private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.PACE, Constants.SortMode.DISTANCE };
    private final boolean[] smallestFirsts = { false, true, true, true };

    private int originId;
    private int distance;

    private final static String BUNDLE_DISTANCE = "distance";
    private final static String BUNDLE_ORIGIN_ID = "originId";

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
            Prefs.setDistanceVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : MathUtils
                .createList(DbReader.get(a)
                .getExercise(originId).getType()));
        }
    }

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sortMode, smallestFirst,
            Prefs.getDistanceVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        if (exerliteList.size() != 0) {
            TreeMap<Float, Float> nodes = DbReader.get(a).getPaceNodesByDistance(distance,
                Prefs.getDistanceVisibleTypes());
            GraphData data = new GraphData(nodes, GraphData.GRAPH_BEZIER, false, false);
            Graph graph = new Graph(data, true, false, false, true, true, false, true, false);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);
            }

            itemList.add(getNewSorter(sortModes, sortModesTitle));
            float goalPace = DbReader.get(a).getDistanceGoal(distance);
            if (goalPace != Distance.NO_GOAL_PACE) {
                Goal goal = new Goal(goalPace, distance);
                itemList.add(goal);
            }
            addHeadersAndItems(itemList, exerliteList);

            fadeOutEmpty();
        }
        else fadeInEmpty();

        return itemList;
    }

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCE);
    }

    @Override
    protected void getAdapter() {
        adapter = new DistanceAdapter(a, this, items, originId, distance);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCE);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(Constants.Layout.DISTANCE, sortMode);
        Prefs.setSmallestFirstPref(Constants.Layout.DISTANCE, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_distance));
        emptyMessage.setText(getString(R.string.empty_message_distance));
        emptyImage.setImageResource(R.drawable.ic_empty_distance_24dp);
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_DISTANCE);
        }

        super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
