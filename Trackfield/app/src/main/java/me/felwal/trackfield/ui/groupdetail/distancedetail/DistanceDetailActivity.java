package me.felwal.trackfield.ui.groupdetail.distancedetail;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Distance;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.groupdetail.GroupDetailActivity;
import me.felwal.trackfield.ui.widget.dialog.TimeDialog;
import me.felwal.trackfield.utils.MathUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import me.felwal.android.fragment.dialog.AlertDialog;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.CheckDialog;
import me.felwal.android.fragment.dialog.MultiChoiceDialog;
import me.felwal.android.util.CollectionsKt;
import me.felwal.android.widget.control.CheckListOption;
import me.felwal.android.widget.control.DialogOption;

public class DistanceDetailActivity extends GroupDetailActivity implements AlertDialog.DialogListener,
    TimeDialog.DialogListener, MultiChoiceDialog.DialogListener {

    // extras names
    private static final String EXTRA_DISTANCE = "distance";

    // dialog tags
    private static final String DIALOG_DELETE_DISTANCE = "deleteDistanceDialog";
    private static final String DIALOG_FILTER = "filterDialog";
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_exercises) {
            ArrayList<String> types = DbReader.get(this).getTypes(null);
            String[] items = new String[types.size()];
            types.toArray(items);

            int[] checkedItems = CollectionsKt.indicesOf(items, Prefs.getDistanceVisibleTypes().toArray());

            CheckDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_title_filter), "",
                    R.string.dialog_btn_filter, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_FILTER, null),
                new CheckListOption(items, checkedItems, null))
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_delete_distance) {
            AlertDialog.newInstance(
                new DialogOption(
                    getString(R.string.dialog_title_delete_distance), getString(R.string.dialog_msg_delete_distance),
                    R.string.dialog_btn_delete, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_DELETE_DISTANCE, null))
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
            TimeDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_set_goal), "",
                    R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel, R.string.dialog_btn_delete,
                    DIALOG_GOAL_DISTANCE, null),
                minutes, seconds, "min", "sec")
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

        setToolbar(distance.printTitle());
        selectFragment(DistanceDetailFragment.newInstance(length, originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_group_distancedetail;
    }

    // implements dialogs

    @Override
    public void onAlertDialogPositiveClick(String tag, String passValue) {
        if (tag.equals(DIALOG_DELETE_DISTANCE)) {
            DbWriter.get(this).deleteDistance(distance);
            finish();
        }
    }

    @Override
    public void onAlertDialogNeutralClick(@NonNull String tag, String passValue) {
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.setGoalPace(MathUtils.seconds(0, input1, input2));
            DbWriter.get(this).updateDistance(distance);

            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNeutralClick(String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.removeGoalPace();
            DbWriter.get(this).updateDistance(distance);

            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onMultiChoiceDialogItemsSelected(@NonNull boolean[] checkedItems, @NonNull String tag,
        @Nullable String passValue) {

        if (tag.equals(DIALOG_FILTER)) {
            ArrayList<String> visibleTypes = (ArrayList<String>)
                CollectionsKt.filter(DbReader.get(this).getTypes(null), checkedItems);

            Prefs.setDistanceVisibleTypes(visibleTypes);
            recyclerFragment.updateRecycler();
        }
    }

}
