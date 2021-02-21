package com.example.trackfield.activities.rec_activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.BinaryDialog;
import com.example.trackfield.dialogs.FilterDialog;
import com.example.trackfield.dialogs.TimeDialog;
import com.example.trackfield.fragments.recycler_fragments.DiExRecyclerFragment;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class DistanceRecActivity extends RecActivity implements BinaryDialog.DialogListener, TimeDialog.DialogListener, FilterDialog.DialogListener {

    private Distance distance;
    private int length;
    public static final String EXTRA_DISTANCE = "distance";

    ////

    public static void startActivity(Context c, int distance) {
        Intent intent = new Intent(c, DistanceRecActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int distance, int originId) {
        Intent intent = new Intent(c, DistanceRecActivity.class);
        intent.putExtra(EXTRA_DISTANCE, distance);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // on

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

        switch (item.getItemId()) {
            case R.id.action_filter:
                FilterDialog.newInstance(getString(R.string.dialog_title_filter), Prefs.getDistanceVisibleTypes(), R.string.dialog_btn_filter, "filterDistance")
                        .show(getSupportFragmentManager());
                return true;

            case R.id.action_deleteDistance:
                BinaryDialog.newInstance(getString(R.string.dialog_title_delete_distance), getString(R.string.dialog_message_delete_distance),
                        R.string.dialog_btn_delete, "deleteDistance").show(getSupportFragmentManager());
                return true;

            case R.id.action_setGoal:
                //Helper.Reader reader = new Helper.Reader(this);
                float goalPace = reader.getDistanceGoal(length);
                //reader.close();

                int minutes, seconds;
                if (goalPace == Distance.NO_GOAL_PACE) {
                    minutes = BaseDialog.NO_TEXT;
                    seconds = BaseDialog.NO_TEXT;
                }
                else {
                    float[] timeParts = M.getTimeParts(goalPace);
                    minutes = (int) timeParts[1];
                    seconds = (int) timeParts[0];
                }

                TimeDialog.newInstance(getString(R.string.dialog_title_set_goal), "", minutes, seconds, "min", "sec", R.string.dialog_btn_set, "distanceGoal")
                        .show(getSupportFragmentManager());
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    // extend

    @Override
    protected void getExtras(Intent intent) {

        if (!intent.hasExtra(EXTRA_DISTANCE)) return;
        length = intent.getIntExtra(EXTRA_DISTANCE, 0);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

        distance = reader.getDistance(length);
        setToolbar(M.prefix(length, 2, "m"));
        selectFragment(DiExRecyclerFragment.newInstance(length, originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_distance;
    }

    // implement

    @Override
    public void onBinaryDialogPositiveClick(String tag) {

        if (D.distances.contains(length)) {
            D.distances.remove(D.distances.indexOf(length));
            D.sortDistancesData();
        }

        //Helper.Writer writer = new Helper.Writer(this);
        writer.deleteDistance(distance);
        //writer.close();

        finish();
    }

    @Override
    public void onTimeDialogPositiveClick(int input1, int input2, String tag) {

        distance.setGoalPace(M.seconds(0, input1, input2));
        //Helper.Writer writer = new Helper.Writer(this);
        writer.updateDistance(distance);
        //writer.close();

        invalidateOptionsMenu();
        recyclerFragment.updateRecycler();
    }

    @Override
    public void onTimeDialogNegativeClick(String tag) {

        distance.removeGoalPace();
        //Helper.Writer writer = new Helper.Writer(this);
        writer.updateDistance(distance);
        //writer.close();

        invalidateOptionsMenu();
        recyclerFragment.updateRecycler();
    }

    @Override
    public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
        Prefs.setDistanceVisibleTypes(checkedTypes);
        recyclerFragment.updateRecycler();
    }

}
