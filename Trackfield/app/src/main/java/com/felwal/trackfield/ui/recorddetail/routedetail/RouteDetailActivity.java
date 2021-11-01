package com.felwal.trackfield.ui.recorddetail.routedetail;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import com.felwal.android.util.CollectionUtilsKt;
import com.felwal.android.widget.dialog.BaseDialogKt;
import com.felwal.android.widget.dialog.BinaryDialog;
import com.felwal.android.widget.dialog.ChipDialog;
import com.felwal.android.widget.dialog.TextDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.db.model.Route;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.map.RouteMapActivity;
import com.felwal.trackfield.ui.recorddetail.RecordDetailActivity;
import com.felwal.trackfield.ui.widget.dialog.TimeDialog;
import com.felwal.trackfield.utils.MathUtils;

import java.util.ArrayList;

public class RouteDetailActivity extends RecordDetailActivity implements TextDialog.DialogListener,
    TimeDialog.DialogListener, ChipDialog.DialogListener, BinaryDialog.DialogListener {

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
        Intent intent = new Intent(c, RouteDetailActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int routeId, int originId) {
        Intent intent = new Intent(c, RouteDetailActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID, routeId);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // hide
        MenuItem hideItem = menu.findItem(R.id.action_hide_route);
        hideItem.setChecked(route.isHidden());
        if (route.isHidden()) hideItem.setIcon(R.drawable.ic_hide).setTitle(R.string.action_unhide_route);
        else hideItem.setIcon(R.drawable.ic_hidden).setTitle(R.string.action_hide_route);

        // goal
        MenuItem goalItem = menu.findItem(R.id.action_set_rec_goal);
        goalItem.setChecked(route.hasGoalPace());
        goalItem.setIcon(route.hasGoalPace() ? R.drawable.ic_goal_checked : R.drawable.ic_goal);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_exercises) {
            ArrayList<String> types = DbReader.get(this).getTypes();
            String[] items = new String[types.size()];
            types.toArray(items);

            int[] checkedItems = CollectionUtilsKt.indicesOf(items, Prefs.getRouteVisibleTypes().toArray());

            ChipDialog.newInstance(getString(R.string.dialog_title_title_filter),
                getString(R.string.tv_text_dialog_filter_msg), items, checkedItems,
                R.string.dialog_btn_filter, R.string.dialog_btn_cancel, DIALOG_FILTER_ROUTE)
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_rename_route) {
            if (route != null) {
                TextDialog.newInstance(getString(R.string.dialog_title_rename_route), "", route.getName(),
                    "", R.string.dialog_btn_rename, R.string.dialog_btn_cancel, DIALOG_RENAME_ROUTE)
                    .show(getSupportFragmentManager());
            }
            return true;
        }
        else if (itemId == R.id.action_set_rec_goal) {
            int minutes, seconds;
            if (route.getGoalPace() == Route.NO_GOAL_PACE) {
                minutes = BaseDialogKt.NULL_INT;
                seconds = BaseDialogKt.NULL_INT;
            }
            else {
                float[] timeParts = MathUtils.getTimeParts(route.getGoalPace());
                minutes = (int) timeParts[1];
                seconds = (int) timeParts[0];
            }

            TimeDialog.newInstance(getString(R.string.dialog_title_set_goal), "",
                minutes, seconds, "min", "sec", R.string.dialog_btn_delete,
                R.string.dialog_btn_set, R.string.dialog_btn_cancel, DIALOG_GOAL_ROUTE)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_hide_route) {
            route.invertHidden();
            DbWriter.get(this).updateRoute(route, this);
            invalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.action_map) {
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
        selectFragment(RouteDetailRecyclerFragment.newInstance(route.getId(), originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_routedetail;
    }

    // implements dialogs

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RENAME_ROUTE)) {
            if (input.equals("") || input.equals(route.getName())) return;

            int existingIdForNewName = DbReader.get(this).getRouteId(input);

            // update route
            if (existingIdForNewName != Route.ID_NON_EXISTANT) {
                BinaryDialog.newInstance(getString(R.string.dialog_title_merge_routes),
                    getString(R.string.dialog_msg_merge_routes), R.string.dialog_btn_merge, R.string.dialog_btn_cancel,
                    input, DIALOG_MERGE_ROUTES)
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
    public void onTimeDialogNeutralClick(String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.removeGoalPace();
            DbWriter.get(this).updateRoute(route, this);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onChipDialogPositiveClick(@NonNull boolean[] checkedItems, @NonNull String tag) {
        if (tag.equals(DIALOG_FILTER_ROUTE)) {
            ArrayList<String> visibleTypes = (ArrayList<String>)
                CollectionUtilsKt.filter(DbReader.get(this).getTypes(), checkedItems);

            Prefs.setRouteVisibleTypes(visibleTypes);
            recyclerFragment.updateRecycler();
        }
    }

}
