package com.example.trackfield.fragments.recyclerfragments;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.activities.ViewActivity;
import com.example.trackfield.adapters.recycleradapters.DistanceRecyclerAdapter;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.database.Helper;
import com.example.trackfield.graphing.Graph;
import com.example.trackfield.graphing.GraphData;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.Goal;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class DistanceRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Pace & Avg time", "Full distance" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.PACE, C.SortMode.DISTANCE };
    private final boolean[] smallestFirsts = { false, true, true, true };

    private int originId;
    private int distance;

    private final static String BUNDLE_DISTANCE = "distance";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    ////

    public static DistanceRecyclerFragment newInstance(int distance, int originId) {
        DistanceRecyclerFragment instance = new DistanceRecyclerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_DISTANCE, distance);
        bundle.putInt(BUNDLE_ORIGIN_ID, originId);
        instance.setArguments(bundle);
        return instance;
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            distance = bundle.getInt(BUNDLE_DISTANCE, -1);
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setDistanceVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : M.createList(Helper.getReader(a).getExercise(originId).getType()));
        }
    }

    @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sortMode, smallestFirst, Prefs.getDistanceVisibleTypes());
        ArrayList<Exerlite> chronoList = reader.getExerlitesByDistance(distance, C.SortMode.DATE, true, Prefs.getDistanceVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        D.markTop(exerliteList);

        if (exerliteList.size() != 0) {

            GraphData data = new GraphData(GraphData.ofExerlites(chronoList), GraphData.GRAPH_BEZIER, false, false);
            Graph graph = new Graph(data, true, false, false, true, true, false, true, false);
            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);
            }

            itemList.add(getNewSorter(sortModes, sortModesTitle));
            float goalPace = reader.getDistanceGoal(distance);
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
    @Override protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_DISTANCE);
    }
    @Override protected void getAdapter() {
        adapter = new DistanceRecyclerAdapter(items, distance, originId, a);
    }
    @Override protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_DISTANCE);
    }
    @Override protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.EXERCISE_DISTANCE, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.EXERCISE_DISTANCE, smallestFirst);
    }
    @Override protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_distance));
        emptyMessage.setText(getString(R.string.empty_message_distance));
        emptyImage.setImageResource(R.drawable.ic_empty_distance_24dp);
    }

    @Override public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_DISTANCE);
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
