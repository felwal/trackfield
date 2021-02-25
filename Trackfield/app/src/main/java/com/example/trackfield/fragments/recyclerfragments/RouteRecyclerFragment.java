package com.example.trackfield.fragments.recyclerfragments;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.activities.ViewActivity;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.adapters.recycleradapters.RouteRecyclerAdapter;
import com.example.trackfield.database.Reader;
import com.example.trackfield.graphing.Graph;
import com.example.trackfield.graphing.GraphData;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.Goal;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.objects.Route;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class RouteRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.DISTANCE, C.SortMode.TIME, C.SortMode.PACE };
    private final boolean[] smallestFirsts = { false, false, true, true };

    private Route route;
    private int originId;

    private final static String BUNDLE_ROUTE_ID = "routeId";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    ////

    public static RouteRecyclerFragment newInstance(int routeId, int originId) {
        RouteRecyclerFragment instance = new RouteRecyclerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ROUTE_ID, routeId);
        bundle.putInt(BUNDLE_ORIGIN_ID, originId);
        instance.setArguments(bundle);
        return instance;
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
    }

    private void getBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            //reader.close();
            //reader = new Helper.Reader(a);
            route = Reader.get(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, -1));
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setRouteVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : M.createList(Reader.get(a).getExercise(originId).getType()));
        }
    }

    @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.get_id(), sortMode, smallestFirst, Prefs.getRouteVisibleTypes());
        ArrayList<Exerlite> chronoList = reader.getExerlitesByRoute(route.get_id(), C.SortMode.DATE, true, Prefs.getRouteVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        D.markTop(exerliteList);

        if (exerliteList.size() != 0) {

            GraphData data = new GraphData(GraphData.ofExerlites(chronoList), GraphData.GRAPH_BEZIER,false, false);
            Graph graph = new Graph(data, true, false, false, true, true, false, true, false);
            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);
            }

            itemList.add(getNewSorter(sortModes, sortModesTitle));
            route = Reader.get(a).getRoute(route.get_id());
            if (route.getGoalPace() != Route.NO_GOAL_PACE) {
                Goal goal = new Goal(route.getGoalPace());
                itemList.add(goal);
            }
            addHeadersAndItems(itemList, exerliteList);

            fadeOutEmpty();
        }
        else fadeInEmpty();

        return itemList;
    }
    @Override protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
    }
    @Override protected void getAdapter() {
        adapter = new RouteRecyclerAdapter(items, originId, a);
    }
    @Override protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
    }
    @Override protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.EXERCISE_ROUTE, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.EXERCISE_ROUTE, smallestFirst);
    }
    @Override protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_route));
        emptyMessage.setText(getString(R.string.empty_message_route));
        emptyImage.setImageResource(R.drawable.ic_empty_route_24dp);
    }

    @Override public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_ROUTE);
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
