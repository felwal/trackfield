package com.example.trackfield.ui.rec.route;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.FilterDialog;
import com.example.trackfield.ui.custom.dialog.TextDialog;
import com.example.trackfield.ui.custom.dialog.TimeDialog;
import com.example.trackfield.ui.map.RouteMapActivity;
import com.example.trackfield.ui.rec.RecActivity;
import com.example.trackfield.utils.MathUtils;

import java.util.ArrayList;

public class RouteActivity extends RecActivity implements TextDialog.DialogListener,
    TimeDialog.DialogListener, FilterDialog.DialogListener, BinaryDialog.DialogListener {

    // extras names
    public static final String EXTRA_ROUTE_ID = "routeId";

    // dialog tags
    private static final String DIALOG_RENAME_ROUTE = "renameRouteDialog";
    private static final String DIALOG_MERGE_ROUTES = "mergeRouteDialog";
    private static final String DIALOG_GOAL_ROUTE = "goalRouteDialog";
    private static final String DIALOG_FILTER_ROUTE = "filterRouteDialog";

    private Route route;

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
            DbWriter.get(this).updateRoute(route, this);
            invalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.action_routeMap) {
            RouteMapActivity.startActivity(route.getId(), this);
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
        selectFragment(RouteRecyclerFragment.newInstance(route.getId(), originId));
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
                DbWriter.get(this).updateRoute(route, this);
                finish();
                startActivity(this, route.getId(), originId);
            }
        }
    }

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_MERGE_ROUTES)) {
            DbWriter.get(this).updateRouteName(route.getName(), passValue);
            route.setName(passValue);
            int newId = DbWriter.get(this).updateRoute(route, this);
            finish();
            startActivity(this, newId, originId);
        }
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.setGoalPace(MathUtils.seconds(0, input1, input2));
            DbWriter.get(this).updateRoute(route, this);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNegativeClick(String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.removeGoalPace();
            DbWriter.get(this).updateRoute(route, this);

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

}
