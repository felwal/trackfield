package com.example.trackfield.ui.rec.route;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.data.db.Reader;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.Goal;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;

import java.util.ArrayList;
import java.util.TreeMap;

public class RouteRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
    private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.DISTANCE, Constants.SortMode.TIME, Constants.SortMode.PACE };
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

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            route = Reader.get(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, -1));
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setRouteVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : MathUtils.createList(Reader.get(a)
                .getExercise(originId).getType()));
        }
    }

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.get_id(), sortMode, smallestFirst,
            Prefs.getRouteVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        if (exerliteList.size() != 0) {
            TreeMap<Float, Float> nodes = Reader.get(a).getPaceNodesByRoute(route.get_id(),
                Prefs.getRouteVisibleTypes());
            GraphData data = new GraphData(nodes, GraphData.GRAPH_BEZIER, false, false);
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

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(Constants.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTE);
    }

    @Override
    protected void getAdapter() {
        adapter = new RouteRecyclerAdapter(items, originId, a);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(Constants.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTE);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(Constants.Layout.ROUTE, sortMode);
        Prefs.setSmallestFirstPref(Constants.Layout.ROUTE, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_route));
        emptyMessage.setText(getString(R.string.empty_message_route));
        emptyImage.setImageResource(R.drawable.ic_empty_route_24dp);
    }

    // implements RecyclerAdapter

    @Override
    public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_ROUTE);
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
