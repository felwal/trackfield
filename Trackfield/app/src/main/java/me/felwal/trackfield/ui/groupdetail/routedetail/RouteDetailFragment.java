package me.felwal.trackfield.ui.groupdetail.routedetail;

import android.os.Bundle;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.db.model.Route;
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

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;

public class RouteDetailFragment extends GroupDetailFragment {

    // bundle extras
    private final static String BUNDLE_ROUTE_ID = "routeId";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByDistance(),
        SorterItem.sortByTime(),
        SorterItem.sortByPace()
    );

    private Route route;

    //

    public static RouteDetailFragment newInstance(int routeId, int originId) {
        RouteDetailFragment instance = new RouteDetailFragment();
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
            route = DbReader.get(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, Route.ID_NON_EXISTANT));
            setOriginId(bundle.getInt(BUNDLE_ORIGIN_ID, Exercise.ID_NONE));
        }

        updateFilterByOrigin();
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_routedetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_routedetail_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_exercise, R.attr.tf_colorRoute));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.ROUTE_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.ROUTE_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new RouteDetailDelegationAdapter(a, this, items, getOriginId());
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.getId(), sorter.getMode(),
            sorter.getAscending(), Prefs.getGroupFilter());

        if (exerliteList.size() != 0) {
            GraphData data = new GraphData(
                a, DbReader.get(a).getPaceNodesByRoute(route.getId(), Prefs.getGroupFilter()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_GROUP);
                itemList.add(graph);
            }

            itemList.add(sorter.copy());
            route = DbReader.get(a).getRoute(route.getId());
            if (route.getGoalPace() != Route.GOAL_PACE_NONE) {
                Goal goal = new Goal(route.getGoalPace());
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
        Prefs.setSorter(AppConsts.Layout.ROUTE_DETAIL, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements GroupDetailFragment

    @Override
    public int getFrom() {
        return ExerciseDetailActivity.FROM_ROUTE;
    }

}
