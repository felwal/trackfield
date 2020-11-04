package com.example.trackfield.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.fragments.dialogs.Dialogs;
import com.example.trackfield.fragments.RecyclerFragments;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Route;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.M;

import java.util.ArrayList;

public class RecActivity {

    public static abstract class Base extends AppCompatActivity {

        protected Helper.Reader reader;
        protected Helper.Writer writer;
        private ActionBar ab;
        protected FrameLayout frame;
        protected RecyclerFragments.Base recyclerFragment;

        protected int originId = -1;

        public static final String EXTRA_ORIGIN_ID = "orignId";

        ////

        @Override protected void onCreate(Bundle savedInstanceState) {

            D.updateTheme(this);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rec);
            frame = findViewById(R.id.frameLayout_scrollerFrameRec);

            //reader = new Helper.Reader(this);
            reader = Helper.getReader(this);
            writer = Helper.getWriter(this);
            getExtras(getIntent());
        }

        protected abstract void getExtras(Intent intent);
        protected abstract int getToolbarMenuRes();

        protected void selectFragment(RecyclerFragments.Base recyclerFragment) {
            this.recyclerFragment = recyclerFragment;
            getSupportFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();
        }
        public Helper.Reader getReader() {
            return reader;
        }

        // toolbar
        protected void setToolbar(String title) {
            final Toolbar tb = findViewById(R.id.toolbar_rec);
            setSupportActionBar(tb);
            ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true); // up btn
            ab.setTitle(title);
        }
        public void setToolbarTitle(String title) {
            ab.setTitle(title);
        }

        @Override public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(getToolbarMenuRes(), menu);
            return true;
        }
        @Override public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case android.R.id.home: finish(); return true;
                default: return super.onOptionsItemSelected(item);
            }
        }

        @Override protected void onRestart() {
            super.onRestart();
            recyclerFragment.updateRecycler();
            //recreate();
        }
        @Override protected void onDestroy() {
            //reader.close();
            //Helper.closeReader();
            //Helper.closeWriter();
            super.onDestroy();
        }

    }

    public static class DistanceActivity extends Base implements Dialogs.BinaryDialog.DialogListener, Dialogs.TimeDialog.DialogListener, Dialogs.FilterDialog.DialogListener {

        private Distance distance;
        private int length;
        public static final String EXTRA_DISTANCE = "distance";

        ////

        public static void startActivity(Context c, int distance) {
            Intent intent = new Intent(c, DistanceActivity.class);
            intent.putExtra(EXTRA_DISTANCE, distance);
            c.startActivity(intent);
        }
        public static void startActivity(Context c, int distance, int originId) {
            Intent intent = new Intent(c, DistanceActivity.class);
            intent.putExtra(EXTRA_DISTANCE, distance);
            if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
            c.startActivity(intent);
        }

        @Override protected void getExtras(Intent intent) {

            if (!intent.hasExtra(EXTRA_DISTANCE)) return;
            length = intent.getIntExtra(EXTRA_DISTANCE, 0);
            originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

            distance = reader.getDistance(length);
            setToolbar(M.prefix(length, 2, "m"));
            selectFragment(RecyclerFragments.DistanceExerciseRF.newInstance(length, originId));
        }
        @Override protected int getToolbarMenuRes() {
            return R.menu.menu_toolbar_rec_distance;
        }

        @Override public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_filter:
                    Dialogs.FilterDistance.newInstance(D.distanceVisibleTypes, getSupportFragmentManager());
                    return true;

                case R.id.action_deleteDistance:
                    Dialogs.DeleteDistance.newInstance(getSupportFragmentManager());
                    return true;

                case R.id.action_setGoal:
                    //Helper.Reader reader = new Helper.Reader(this);
                    float goalPace = reader.getDistanceGoal(length);
                    //reader.close();

                    int minutes, seconds;
                    if (goalPace == Distance.NO_GOAL_PACE) {
                        minutes = Dialogs.Base.NO_TEXT;
                        seconds = Dialogs.Base.NO_TEXT;
                    }
                    else {
                        float[] timeParts = M.getTimeParts(goalPace);
                        minutes = (int) timeParts[1];
                        seconds = (int) timeParts[0];
                    }

                    Dialogs.GoalDistance.newInstance(minutes, seconds, getSupportFragmentManager());
                    return true;

                default: return super.onOptionsItemSelected(item);
            }
        }
        @Override public boolean onPrepareOptionsMenu(Menu menu) {

            // goal
            MenuItem goalItem = menu.findItem(R.id.action_setGoal);
            goalItem.setChecked(distance.hasGoalPace());
            goalItem.setIcon(distance.hasGoalPace() ? R.drawable.ic_goal_checked_24dp : R.drawable.ic_goal_24dp);

            return super.onPrepareOptionsMenu(menu);
        }

        @Override public void onBinaryDialogPositiveClick(String tag) {

            if (D.distances.contains(length)) {
                D.distances.remove(D.distances.indexOf(length));
                D.sortDistancesData();
            }

            //Helper.Writer writer = new Helper.Writer(this);
            writer.deleteDistance(distance);
            //writer.close();

            finish();
        }
        @Override public void onTimeDialogPositiveClick(int input1, int input2, String tag) {

            distance.setGoalPace(M.seconds(0, input1, input2));
            //Helper.Writer writer = new Helper.Writer(this);
            writer.updateDistance(distance);
            //writer.close();

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
        @Override public void onTimeDialogNegativeClick(String tag) {

            distance.removeGoalPace();
            //Helper.Writer writer = new Helper.Writer(this);
            writer.updateDistance(distance);
            //writer.close();

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
        @Override public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
            D.distanceVisibleTypes = checkedTypes;
            recyclerFragment.updateRecycler();
        }

    }
    public static class RouteActivity extends Base implements Dialogs.TextDialog.DialogListener, Dialogs.TimeDialog.DialogListener, Dialogs.FilterDialog.DialogListener {

        //private int routeId;
        private Route route;
        public static final String EXTRA_ROUTE_ID = "routeId";

        ////

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

        @Override protected void getExtras(Intent intent) {

            if (!intent.hasExtra(EXTRA_ROUTE_ID)) return;
            route = reader.getRoute(intent.getIntExtra(EXTRA_ROUTE_ID, -1));
            originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

            setToolbar(route.getName());

            selectFragment(RecyclerFragments.RouteExerciseRF.newInstance(route.get_id(), originId));
        }
        @Override protected int getToolbarMenuRes() {
            return R.menu.menu_toolbar_rec_route;
        }

        @Override public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_filter:
                    Dialogs.FilterRoute.newInstance(D.routeVisibleTypes, getSupportFragmentManager());
                    return true;

                case R.id.action_renameRoute:
                    if (route != null) Dialogs.RenameRoute.newInstance(route.getName(), getSupportFragmentManager());
                    return true;

                case R.id.action_setGoal:
                    int minutes, seconds;
                    if (route.getGoalPace() == Route.NO_GOAL_PACE) {
                        minutes = Dialogs.Base.NO_TEXT;
                        seconds = Dialogs.Base.NO_TEXT;
                    }
                    else {
                        float[] timeParts = M.getTimeParts(route.getGoalPace());
                        minutes = (int) timeParts[1];
                        seconds = (int) timeParts[0];
                    }
                    Dialogs.GoalRoute.newInstance(minutes, seconds, getSupportFragmentManager());
                    return true;

                case R.id.action_hideRoute:
                    route.invertHidden();
                    //Helper.Writer writer = new Helper.Writer(this);
                    writer.updateRoute(route);
                    //writer.close();
                    invalidateOptionsMenu();
                    return true;

                default: return super.onOptionsItemSelected(item);
            }
        }
        @Override public boolean onPrepareOptionsMenu(Menu menu) {

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

        @Override public void onTextDialogPositiveClick(String input, String tag) {
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
        @Override public void onTimeDialogPositiveClick(int input1, int input2, String tag) {

            route.setGoalPace(M.seconds(0, input1, input2));
            //Helper.Writer writer = new Helper.Writer(this);
            writer.updateRoute(route);
            //writer.close();

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
        @Override public void onTimeDialogNegativeClick(String tag) {

            route.removeGoalPace();
            //Helper.Writer writer = new Helper.Writer(this);
            writer.updateRoute(route);
            //writer.close();

            invalidateOptionsMenu();
            recyclerFragment.updateRecycler();
        }
        @Override public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
            D.routeVisibleTypes = checkedTypes;
            recyclerFragment.updateRecycler();
        }

    }
    public static class IntervalActivity extends Base implements Dialogs.TextDialog.DialogListener {

        private String interval;

        public static final String EXTRA_INTERVAL = "interval";
        private static final String TAG_RENAME_INTERVAL = "renameInterval";

        ////

        public static void startActivity(Context c, String interval) {
            Intent intent = new Intent(c, IntervalActivity.class);
            intent.putExtra(EXTRA_INTERVAL, interval);
            c.startActivity(intent);
        }
        public static void startActivity(Context c, String interval, int originId) {
            Intent intent = new Intent(c, IntervalActivity.class);
            intent.putExtra(EXTRA_INTERVAL, interval);
            if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
            c.startActivity(intent);
        }

        @Override protected void getExtras(Intent intent) {
            if (!intent.hasExtra(EXTRA_INTERVAL)) return;

            interval = intent.getStringExtra(EXTRA_INTERVAL);
            setToolbar(interval);
            originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;
            selectFragment(RecyclerFragments.IntervalExerciseRF.newInstance(interval, originId));
        }
        @Override protected int getToolbarMenuRes() {
            return R.menu.menu_toolbar_rec_interval;
        }

        public String getInterval() {
            return interval;
        }

        @Override public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_renameInterval:
                    if (interval != null) {
                        //Dialogs.TextDialog.bundle(getString(R.string.dialog_title_rename_interval), "", "", interval, R.string.dialog_btn_rename, getSupportFragmentManager(), TAG_RENAME_INTERVAL);
                        Dialogs.RenameInterval.newInstance(interval, getSupportFragmentManager());
                    }
                    return true;

                default: return super.onOptionsItemSelected(item);
            }
        }
        @Override public void onTextDialogPositiveClick(String input, String tag) {
            if (input.equals("")) return;

            //Helper.Writer writer = new Helper.Writer(this);
            writer.updateInterval(interval, input);
            //writer.close();

            ArrayList<Exercise> exercises = D.filterByInterval(interval);
            for (Exercise e : exercises) { e.setInterval(input); }

            finish();
            startActivity(this, input, originId);
        }

    }

}
