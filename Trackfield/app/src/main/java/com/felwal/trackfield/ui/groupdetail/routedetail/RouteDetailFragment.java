package com.felwal.trackfield.ui.groupdetail.routedetail;

import android.os.Bundle;
import android.view.View;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.model.Route;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.Goal;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.widget.graph.Borders;
import com.felwal.trackfield.ui.widget.graph.Graph;
import com.felwal.trackfield.ui.widget.graph.GraphData;
import com.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.TypeUtils;

import java.util.ArrayList;

public class RouteDetailFragment extends RecyclerFragment {

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
    private int originId;

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
            route = DbReader.get(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, -1));
            originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

            // filtering depending on origin
            Prefs.setRouteVisibleTypes(originId == -1
                ? Prefs.getExerciseVisibleTypes()
                : TypeUtils.createList(DbReader.get(a).getExercise(originId).getType()));
        }
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_routedetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_routedetail_msg));
        emptyImage.setImageDrawable(ResUtilsKt.getDrawableCompatWithTint(a, R.drawable.ic_run, R.attr.tf_colorRoute));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.ROUTE_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.ROUTE_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new RouteDetailDelegationAdapter(a, this, items, originId);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.getId(), sorter.getMode(),
            sorter.getAscending(), Prefs.getRouteVisibleTypes());

        if (exerliteList.size() != 0) {
            GraphData data = new GraphData(
                a, DbReader.get(a).getPaceNodesByRoute(route.getId(), Prefs.getRouteVisibleTypes()),
                GraphData.GRAPH_BEZIER, false, false);

            Graph graph = new Graph(true, Borders.horizontal(), false, true, false);
            graph.addData(data);

            if (graph.hasMoreThanOnePoint()) {
                graph.setTag(RecyclerItem.TAG_GRAPH_GROUP);
                itemList.add(graph);
            }

            itemList.add(sorter.copy());
            route = DbReader.get(a).getRoute(route.getId());
            if (route.getGoalPace() != Route.NO_GOAL_PACE) {
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

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            if (originId != id) ExerciseDetailActivity.startActivity(a, id, ExerciseDetailActivity.FROM_ROUTE);
        }

        super.onDelegateClick(item);
    }

}
