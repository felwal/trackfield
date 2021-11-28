package com.felwal.trackfield.ui.recorddetail.distancedetail;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import com.felwal.android.util.CollectionUtilsKt;
import com.felwal.android.widget.dialog.BaseDialogKt;
import com.felwal.android.widget.dialog.AlertDialog;
import com.felwal.android.widget.dialog.ChipDialog;
import com.felwal.android.widget.dialog.MultiChoiceDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.db.model.Distance;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.recorddetail.RecordDetailActivity;
import com.felwal.trackfield.ui.widget.dialog.TimeDialog;
import com.felwal.trackfield.utils.MathUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DistanceDetailActivity extends RecordDetailActivity implements AlertDialog.DialogListener,
    TimeDialog.DialogListener, MultiChoiceDialog.DialogListener {

    // extras names
    private static final String EXTRA_DISTANCE = "distance";

    // dialog tags
    private static final String DIALOG_DELETE_DISTANCE = "deleteDistanceDialog";
    private static final String DIALOG_FILTER_DISTANCE = "filterDistanceDialog";
    private static final String DIALOG_GOAL_DISTANCE = "goalDistanceDialog";

    private int length;
    private Distance distance;

    //

    public static void startActivity(Context c, int distance) {
        if (distance == Distance.NO_DISTANCE) return;

        Intent intent = new Intent(c, DistanceDetailActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int distance, int originId) {
        if (distance == Distance.NO_DISTANCE) return;

        Intent intent = new Intent(c, DistanceDetailActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // goal
        MenuItem goalItem = menu.findItem(R.id.action_set_distance_goal);
        goalItem.setChecked(distance.hasGoalPace());
        goalItem.setIcon(distance.hasGoalPace() ? R.drawable.ic_goal_checked : R.drawable.ic_goal);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_exercises) {
            ArrayList<String> types = DbReader.get(this).getTypes();
            String[] items = new String[types.size()];
            types.toArray(items);

            int[] checkedItems = CollectionUtilsKt.indicesOf(items, Prefs.getDistanceVisibleTypes().toArray());

            ChipDialog.newInstance(getString(R.string.dialog_title_title_filter), items, checkedItems,
                R.string.dialog_btn_filter, R.string.fw_dialog_btn_cancel, DIALOG_FILTER_DISTANCE)
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_delete_distance) {
            AlertDialog.newInstance(getString(R.string.dialog_title_delete_distance),
                getString(R.string.dialog_msg_delete_distance), R.string.dialog_btn_delete,
                R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_DELETE_DISTANCE, null)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_set_distance_goal) {
            float goalPace = DbReader.get(this).getDistanceGoal(length);
            int minutes, seconds;
            if (goalPace == Distance.NO_GOAL_PACE) {
                minutes = BaseDialogKt.NULL_INT;
                seconds = BaseDialogKt.NULL_INT;
            }
            else {
                float[] timeParts = MathUtils.getTimeParts(goalPace);
                minutes = (int) timeParts[1];
                seconds = (int) timeParts[0];
            }
            TimeDialog.newInstance(getString(R.string.dialog_title_set_goal), "",
                minutes, seconds, "min", "sec", R.string.dialog_btn_delete,
                R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel, DIALOG_GOAL_DISTANCE)
                .show(getSupportFragmentManager());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // extends RecActivity

    @Override
    protected void getExtras(Intent intent) {
        if (!intent.hasExtra(EXTRA_DISTANCE)) return;
        length = intent.getIntExtra(EXTRA_DISTANCE, 0);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;
        distance = DbReader.get(this).getDistance(length);

        setToolbar(MathUtils.prefix(length, 2, "m"));
        selectFragment(DistanceDetailRecyclerFragment.newInstance(length, originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_distancedetail;
    }

    // implements dialogs

    @Override
    public void onAlertDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_DELETE_DISTANCE)) {
            DbWriter.get(this).deleteDistance(distance);
            finish();
        }
    }

    @Override
    public void onAlertDialogNeutralClick(@Nullable String s, @NonNull String s1) {
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.setGoalPace(MathUtils.seconds(0, input1, input2));
            DbWriter.get(this).updateDistance(distance);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNeutralClick(String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.removeGoalPace();
            DbWriter.get(this).updateDistance(distance);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onMultiChoiceDialogItemsSelected(@NonNull boolean[] checkedItems, @NonNull String tag) {
        if (tag.equals(DIALOG_FILTER_DISTANCE)) {
            ArrayList<String> visibleTypes = (ArrayList<String>)
                CollectionUtilsKt.filter(DbReader.get(this).getTypes(), checkedItems);

            Prefs.setDistanceVisibleTypes(visibleTypes);
            recyclerFragment.updateRecycler();
        }
    }

}
