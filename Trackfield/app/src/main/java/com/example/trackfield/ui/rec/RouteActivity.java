package com.example.trackfield.ui.rec;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.Goal;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.map.RouteMapActivity;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.FilterDialog;
import com.example.trackfield.ui.custom.dialog.TextDialog;
import com.example.trackfield.ui.custom.dialog.TimeDialog;
import com.example.trackfield.ui.rec.adapters.RouteRecyclerAdapter;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;

import java.util.ArrayList;
import java.util.TreeMap;

public class RouteActivity extends RecActivity implements TextDialog.DialogListener,
        TimeDialog.DialogListener, FilterDialog.DialogListener, BinaryDialog.DialogListener {

    //private int routeId;
    private Route route;
    public static final String EXTRA_ROUTE_ID = "routeId";

    private static final String DIALOG_RENAME_ROUTE = "renameRouteDialog";
    private static final String DIALOG_MERGE_ROUTES = "mergeRouteDialog";
    private static final String DIALOG_GOAL_ROUTE = "goalRouteDialog";
    private static final String DIALOG_FILTER_ROUTE = "filterRouteDialog";

    //

    public static void startActivity(Context c, int routeId) {
        Intent intent = new Intent(c, RouteActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int routeId, int originId) {
        Intent intent = new Intent(c, RouteActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // hide
        MenuItem hideItem = menu.findItem(R.id.action_hideRoute);
        hideItem.setChecked(route.isHidden());
        if (route.isHidden()) hideItem.setIcon(R.drawable.ic_hide_24dp).setTitle(R.string.action_unhide_route);
        else hideItem.setIcon(R.drawable.ic_archive_24dp).setTitle(R.string.action_hide_route);

        // goal
        MenuItem goalItem = menu.findItem(R.id.action_setGoal);
        goalItem.setChecked(route.hasGoalPace());
        goalItem.setIcon(route.hasGoalPace() ? R.drawable.ic_goal_checked_24dp : R.drawable.ic_goal_24dp);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_filter) {
            FilterDialog.newInstance(R.string.dialog_title_filter, Prefs.getRouteVisibleTypes(),
                    R.string.dialog_btn_filter, DIALOG_FILTER_ROUTE)
                    .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_renameRoute) {
            if (route != null) {
                TextDialog.newInstance(R.string.dialog_title_rename_route, BaseDialog.NO_RES,
                        route.getName(), "", R.string.dialog_btn_rename, DIALOG_RENAME_ROUTE)
                        .show(getSupportFragmentManager());
            }
            return true;
        }
        else if (itemId == R.id.action_setGoal) {
            int minutes, seconds;
            if (route.getGoalPace() == Route.NO_GOAL_PACE) {
                minutes = BaseDialog.NO_FLOAT_TEXT;
                seconds = BaseDialog.NO_FLOAT_TEXT;
            }
            else {
                float[] timeParts = MathUtils.getTimeParts(route.getGoalPace());
                minutes = (int) timeParts[1];
                seconds = (int) timeParts[0];
            }

            TimeDialog.newInstance(R.string.dialog_title_set_goal, BaseDialog.NO_RES,
                    minutes, seconds, "min", "sec",
                    R.string.dialog_btn_set, R.string.dialog_btn_delete, DIALOG_GOAL_ROUTE)
                    .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_hideRoute) {
            route.invertHidden();
            DbWriter.get(this).updateRoute(route);
            invalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.action_routeMap) {
            RouteMapActivity.startActivity(route.get_id(), this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // extends RecActivity

    @Override
    protected void getExtras(Intent intent) {

        if (!intent.hasExtra(EXTRA_ROUTE_ID)) return;
        route = DbReader.get(this).getRoute(intent.getIntExtra(EXTRA_ROUTE_ID, -1));
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

        setToolbar(route.getName());

        selectFragment(RouteRecyclerFragment.newInstance(route.get_id(), originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_route;
    }

    // implements dialogs

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RENAME_ROUTE)) {
            if (input.equals("") || input.equals(route.getName())) return;

            //ArrayList<Exercise> exercises = D.filterByRoute(route.getName());
            //for (Exercise e : exercises) { e.setRoute(input); }
            //D.importRoutes();

            int existingIdForNewName = DbReader.get(this).getRouteId(input);

            // update route
            if (existingIdForNewName != Route.ID_NON_EXISTANT) {
                BinaryDialog.newInstance(R.string.dialog_title_merge_routes, R.string.dialog_message_merge_routes,
                        R.string.dialog_btn_merge, input, DIALOG_MERGE_ROUTES)
                        .show(getSupportFragmentManager());
            }
            else {
                DbWriter.get(this).updateRouteName(route.getName(), input);
                route.setName(input);
                DbWriter.get(this).updateRoute(route);
                finish();
                startActivity(this, route.get_id(), originId);
            }
        }
    }

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_MERGE_ROUTES)) {
            DbWriter.get(this).updateRouteName(route.getName(), passValue);
            route.setName(passValue);
            int newId = DbWriter.get(this).updateRoute(route);
            finish();
            startActivity(this, newId, originId);
        }
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.setGoalPace(MathUtils.seconds(0, input1, input2));
            DbWriter.get(this).updateRoute(route);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNegativeClick(String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.removeGoalPace();
            DbWriter.get(this).updateRoute(route);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onFilterDialogPositiveClick(@NonNull ArrayList<Integer> checkedTypes, String tag) {
        if (tag.equals(DIALOG_FILTER_ROUTE)) {
            Prefs.setRouteVisibleTypes(checkedTypes);
            recyclerFragment.updateRecycler();
        }
    }

    // fragment

    public static class RouteRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.DISTANCE, Constants.SortMode.TIME, Constants.SortMode.PACE };
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
                TreeMap<Float, Float> nodes = DbReader.get(a).getPaceNodesByRoute(route.get_id(),
                    Prefs.getRouteVisibleTypes());
                GraphData data = new GraphData(nodes, GraphData.GRAPH_BEZIER, false, false);
                Graph graph = new Graph(data, true, false, false, true, true, false, true, false);

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

}
