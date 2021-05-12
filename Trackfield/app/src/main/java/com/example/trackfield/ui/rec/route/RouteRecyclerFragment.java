package com.example.trackfield.ui.rec.route;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.Goal;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.custom.graph.Borders;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

import java.util.ArrayList;
import java.util.TreeMap;

public class RouteRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
    private final AppConsts.SortMode[] sortModes = { AppConsts.SortMode.DATE, AppConsts.SortMode.DISTANCE, AppConsts.SortMode.TIME, AppConsts.SortMode.PACE };
    private final boolean[] smallestFirsts = { false, false, true, true };

    private Route route;
    private int originId;

    private final static String BUNDLE_ROUTE_ID = "routeId";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    //

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
            route = DbReader.get(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, -1));
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setRouteVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : MathUtils.createList(DbReader.get(a)
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
            GraphData data = new GraphData(
                DbReader.get(a).getPaceNodesByRoute(route.get_id(), Prefs.getRouteVisibleTypes()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);
            }

            itemList.add(getNewSorter(sortModes, sortModesTitle));
            route = DbReader.get(a).getRoute(route.get_id());
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
        sortMode = Prefs.getSortModePref(AppConsts.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.ROUTE);
    }

    @Override
    protected void getAdapter() {
        adapter = new RouteAdapter(a, this, items, originId);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(AppConsts.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.ROUTE);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(AppConsts.Layout.ROUTE, sortMode);
        Prefs.setSmallestFirstPref(AppConsts.Layout.ROUTE, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_route));
        emptyMessage.setText(getString(R.string.empty_message_route));
        emptyImage.setImageResource(R.drawable.ic_empty_route_24dp);
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_ROUTE);
        }

        super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
