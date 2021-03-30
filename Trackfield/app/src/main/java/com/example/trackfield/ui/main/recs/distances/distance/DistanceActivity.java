package com.example.trackfield.ui.main.recs.distances.distance;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.service.database.Reader;
import com.example.trackfield.service.database.Writer;
import com.example.trackfield.view.dialogs.BaseDialog;
import com.example.trackfield.view.dialogs.BinaryDialog;
import com.example.trackfield.view.dialogs.FilterDialog;
import com.example.trackfield.view.dialogs.TimeDialog;
import com.example.trackfield.service.toolbox.M;
import com.example.trackfield.service.file.Prefs;
import com.example.trackfield.ui.main.recs.general.RecActivity;

import java.util.ArrayList;

public class DistanceActivity extends RecActivity implements BinaryDialog.DialogListener, TimeDialog.DialogListener,
    FilterDialog.DialogListener {

    private Distance distance;
    private int length;

    public static final String EXTRA_DISTANCE = "distance";
    private static final String DIALOG_DELETE_DISTANCE = "deleteDistanceDialog";
    private static final String DIALOG_FILTER_DISTANCE = "filterDistanceDialog";
    private static final String DIALOG_GOAL_DISTANCE = "goalDistanceDialog";

    //

    public static void startActivity(Context c, int distance) {
        if (distance == Distance.NO_DISTANCE) return;

        Intent intent = new Intent(c, DistanceActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int distance, int originId) {
        if (distance == Distance.NO_DISTANCE) return;

        Intent intent = new Intent(c, DistanceActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // goal
        MenuItem goalItem = menu.findItem(R.id.action_setGoal);
        goalItem.setChecked(distance.hasGoalPace());
        goalItem.setIcon(distance.hasGoalPace() ? R.drawable.ic_goal_checked_24dp : R.drawable.ic_goal_24dp);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_filter) {
            FilterDialog.newInstance(R.string.dialog_title_filter, Prefs.getDistanceVisibleTypes(),
                R.string.dialog_btn_filter, DIALOG_FILTER_DISTANCE)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_deleteDistance) {
            BinaryDialog.newInstance(R.string.dialog_title_delete_distance, R.string.dialog_message_delete_distance,
                R.string.dialog_btn_delete, DIALOG_DELETE_DISTANCE)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_setGoal) {
            float goalPace = Reader.get(this).getDistanceGoal(length);
            int minutes, seconds;
            if (goalPace == Distance.NO_GOAL_PACE) {
                minutes = BaseDialog.NO_FLOAT_TEXT;
                seconds = BaseDialog.NO_FLOAT_TEXT;
            }
            else {
                float[] timeParts = M.getTimeParts(goalPace);
                minutes = (int) timeParts[1];
                seconds = (int) timeParts[0];
            }
            TimeDialog.newInstance(R.string.dialog_title_set_goal, BaseDialog.NO_RES,
                minutes, seconds, "min", "sec",
                R.string.dialog_btn_set, R.string.dialog_btn_delete, DIALOG_GOAL_DISTANCE)
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

        distance = Reader.get(this).getDistance(length);
        setToolbar(M.prefix(length, 2, "m"));
        selectFragment(DistanceRecyclerFragment.newInstance(length, originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_distance;
    }

    // implements dialogs

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_DELETE_DISTANCE)) {
            Writer.get(this).deleteDistance(distance);
            finish();
        }
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.setGoalPace(M.seconds(0, input1, input2));
            Writer.get(this).updateDistance(distance);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onTimeDialogNegativeClick(String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.removeGoalPace();
            Writer.get(this).updateDistance(distance);

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onFilterDialogPositiveClick(@NonNull ArrayList<Integer> checkedTypes, String tag) {
        if (tag.equals(DIALOG_FILTER_DISTANCE)) {
            Prefs.setDistanceVisibleTypes(checkedTypes);
            recyclerFragment.updateRecycler();
        }
    }

}