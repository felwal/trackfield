package com.example.trackfield.activities.rec_activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.trackfield.R;
import com.example.trackfield.activities.map_activity.RouteMapActivity;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.FilterDialog;
import com.example.trackfield.dialogs.TextDialog;
import com.example.trackfield.dialogs.TimeDialog;
import com.example.trackfield.fragments.recycler_fragments.RoExRecyclerFragment;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Route;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class RouteRecActivity extends RecActivity implements TextDialog.DialogListener, TimeDialog.DialogListener, FilterDialog.DialogListener {

    //private int routeId;
    private Route route;
    public static final String EXTRA_ROUTE_ID = "routeId";

    ////

    public static void startActivity(Context c, int routeId) {
        Intent intent = new Intent(c, RouteRecActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        c.startActivity(intent);
    }
    public static void startActivity(Context c, int routeId, int originId) {
        Intent intent = new Intent(c, RouteRecActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // on

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // hide
        MenuItem hideItem = menu.findItem(R.id.action_hideRoute);
        hideItem.setChecked(route.isHidden());
        if (route.isHidden()) hideItem.setIcon(R.drawable.ic_unarchive_24dp).setTitle(R.string.action_unhide);
        else hideItem.setIcon(R.drawable.ic_archive_24dp).setTitle(R.string.action_hide);

        // goal
        MenuItem goalItem = menu.findItem(R.id.action_setGoal);
        goalItem.setChecked(route.hasGoalPace());
        goalItem.setIcon(route.hasGoalPace() ? R.drawable.ic_goal_checked_24dp : R.drawable.ic_goal_24dp);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_filter:
                FilterDialog.newInstance(getString(R.string.dialog_title_filter), Prefs.getRouteVisibleTypes(), R.string.dialog_btn_filter, "filterRoute")
                        .show(getSupportFragmentManager());
                return true;

            case R.id.action_renameRoute:
                if (route != null) {
                    TextDialog.newInstance(getString(R.string.dialog_title_rename_route), "", route.getName(), "", R.string.dialog_btn_rename, "renameRoute")
                            .show(getSupportFragmentManager());
                }
                return true;

            case R.id.action_setGoal:
                int minutes, seconds;
                if (route.getGoalPace() == Route.NO_GOAL_PACE) {
                    minutes = BaseDialog.NO_TEXT;
                    seconds = BaseDialog.NO_TEXT;
                }
                else {
                    float[] timeParts = M.getTimeParts(route.getGoalPace());
                    minutes = (int) timeParts[1];
                    seconds = (int) timeParts[0];
                }

                TimeDialog.newInstance(getString(R.string.dialog_title_set_goal), "", minutes, seconds, "min", "sec", R.string.dialog_btn_set, "routeGoal")
                        .show(getSupportFragmentManager());
                return true;

            case R.id.action_hideRoute:
                route.invertHidden();
                writer.updateRoute(route);
                invalidateOptionsMenu();
                return true;

            case R.id.action_routeMap:
                RouteMapActivity.startActivity(route.get_id(), this);

            default: return super.onOptionsItemSelected(item);
        }
    }

    // extend

    @Override
    protected void getExtras(Intent intent) {

        if (!intent.hasExtra(EXTRA_ROUTE_ID)) return;
        route = reader.getRoute(intent.getIntExtra(EXTRA_ROUTE_ID, -1));
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

        setToolbar(route.getName());

        selectFragment(RoExRecyclerFragment.newInstance(route.get_id(), originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_route;
    }

    // implement

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (input.equals("") || input.equals(route.getName())) return;

        ArrayList<Exercise> exercises = D.filterByRoute(route.getName());
        for (Exercise e : exercises) { e.setRoute(input); }
        D.importRoutes();

        //Helper.Writer writer = new Helper.Writer(this);
        writer.updateRouteName(route.getName(), input);
        route.setName(input);
        writer.updateRoute(route);
        //writer.close();

        finish();
        startActivity(this, route.get_id(), originId);
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {

        route.setGoalPace(M.seconds(0, input1, input2));
        //Helper.Writer writer = new Helper.Writer(this);
        writer.updateRoute(route);
        //writer.close();

        invalidateOptionsMenu();
        recyclerFragment.updateRecycler();
    }

    @Override
    public void onTimeDialogNegativeClick(String tag) {

        route.removeGoalPace();
        //Helper.Writer writer = new Helper.Writer(this);
        writer.updateRoute(route);
        //writer.close();

        invalidateOptionsMenu();
        recyclerFragment.updateRecycler();
    }

    @Override
    public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
        Prefs.setRouteVisibleTypes(checkedTypes);
        recyclerFragment.updateRecycler();
    }

}
