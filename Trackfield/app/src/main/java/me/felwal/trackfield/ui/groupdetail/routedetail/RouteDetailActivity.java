package me.felwal.trackfield.ui.groupdetail.routedetail;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Route;
import me.felwal.trackfield.ui.groupdetail.GroupDetailActivity;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.map.RouteMapActivity;
import me.felwal.trackfield.ui.widget.dialog.TimeDialog;
import me.felwal.trackfield.utils.MathUtils;

import org.jetbrains.annotations.Nullable;

import me.felwal.android.fragment.dialog.AlertDialog;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.InputDialog;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.android.widget.control.InputOption;

public class RouteDetailActivity extends GroupDetailActivity implements InputDialog.DialogListener,
    TimeDialog.DialogListener, AlertDialog.DialogListener {

    // extras names
    public static final String EXTRA_ROUTE_ID = "routeId";

    // dialog tags
    private static final String DIALOG_RENAME_ROUTE = "renameRouteDialog";
    private static final String DIALOG_MERGE_ROUTES = "mergeRouteDialog";
    private static final String DIALOG_GOAL_ROUTE = "goalRouteDialog";

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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_exercises) {
            showFilterSheet();

            return true;
        }
        else if (itemId == R.id.action_rename_route) {
            if (route != null) {
                InputDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_rename_route),
                        getString(R.string.dialog_msg_rename_route),
                        R.string.dialog_btn_rename, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                        DIALOG_RENAME_ROUTE, null),
                    new InputOption(route.getName(), "", EditorInfo.TYPE_CLASS_TEXT))
                    .show(getSupportFragmentManager());
            }

            return true;
        }
        else if (itemId == R.id.action_set_group_goal) {
            int minutes, seconds;

            if (route.getGoalPace() == Route.GOAL_PACE_NONE) {
                minutes = BaseDialogKt.NULL_INT;
                seconds = BaseDialogKt.NULL_INT;
            }
            else {
                float[] timeParts = MathUtils.getTimeParts(route.getGoalPace());
                minutes = (int) timeParts[1];
                seconds = (int) timeParts[0];
            }

            TimeDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_set_goal), "",
                    R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel, R.string.dialog_btn_delete,
                    DIALOG_GOAL_ROUTE, null),
                minutes, seconds, "min", "sec")
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_hide_route) {
            route.invertHidden();
            DbWriter.get(this).updateRoute(route, this);
            // to get immediate check feedback (before the menu closes), update it here,
            // instead of calling invalidateOptionsMenu(), since that also resets the optional icon colors.
            item.setChecked(route.isHidden());

            return true;
        }
        else if (itemId == R.id.action_map_route) {
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
        selectFragment(RouteDetailFragment.newInstance(route.getId(), originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_group_routedetail;
    }

    // implements dialogs

    @Override
    public void onInputDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_RENAME_ROUTE)) {
            if (input.equals("") || input.equals(route.getName())) return;

            int existingIdForNewName = DbReader.get(this).getRouteId(input);

            // merge
            if (existingIdForNewName != Route.ID_NON_EXISTANT) {
                AlertDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_merge_routes),
                        getString(R.string.dialog_msg_merge_routes),
                        R.string.dialog_btn_merge, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                        DIALOG_MERGE_ROUTES, input))
                    .show(getSupportFragmentManager());
            }
            // rename
            else {
                DbWriter.get(this).updateRouteName(route.getName(), input);
                route.setName(input);
                DbWriter.get(this).updateRoute(route, this);

                MainActivity.updateFragmentOnRestart = true;
                finish();
                startActivity(this, route.getId(), originId);
            }
        }
    }

    @Override
    public void onAlertDialogPositiveClick(String tag, String passValue) {
        if (tag.equals(DIALOG_MERGE_ROUTES)) {
            DbWriter.get(this).updateRouteName(route.getName(), passValue);
            route.setName(passValue);
            int newId = DbWriter.get(this).updateRoute(route, this);

            MainActivity.updateFragmentOnRestart = true;
            finish();
            startActivity(this, newId, originId);
        }
    }

    @Override
    public void onAlertDialogNeutralClick(@Nullable String tag, String passValue) {
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.setGoalPace(MathUtils.seconds(0, input1, input2));
            DbWriter.get(this).updateRoute(route, this);

            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNeutralClick(String tag) {
        if (tag.equals(DIALOG_GOAL_ROUTE)) {
            route.removeGoalPace();
            DbWriter.get(this).updateRoute(route, this);

            recyclerFragment.updateRecycler();
        }
    }

}
