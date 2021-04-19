package com.example.trackfield.ui.rec;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.db.Reader;
import com.example.trackfield.data.db.Writer;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.FilterDialog;
import com.example.trackfield.ui.custom.dialog.TimeDialog;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.Goal;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.rec.adapters.DistanceRecyclerAdapter;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;

import java.util.ArrayList;
import java.util.TreeMap;

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
                float[] timeParts = MathUtils.getTimeParts(goalPace);
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
        setToolbar(MathUtils.prefix(length, 2, "m"));
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
            distance.setGoalPace(MathUtils.seconds(0, input1, input2));
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

    // fragment

    public static class DistanceRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Date", "Pace & Avg time", "Full distance" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.PACE, Constants.SortMode.DISTANCE };
        private final boolean[] smallestFirsts = { false, true, true, true };

        private int originId;
        private int distance;

        private final static String BUNDLE_DISTANCE = "distance";
        private final static String BUNDLE_ORIGIN_ID = "originId";

        //

        public static DistanceRecyclerFragment newInstance(int distance, int originId) {
            DistanceRecyclerFragment instance = new DistanceRecyclerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_DISTANCE, distance);
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
                distance = bundle.getInt(BUNDLE_DISTANCE, -1);
                originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);

                // filtering depending on origin
                Prefs.setDistanceVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : MathUtils.createList(Reader.get(a)
                    .getExercise(originId).getType()));
            }
        }

        // extends RecyclerFragment

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sortMode, smallestFirst,
                Prefs.getDistanceVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            if (exerliteList.size() != 0) {
                TreeMap<Float, Float> nodes = Reader.get(a).getPaceNodesByDistance(distance,
                    Prefs.getDistanceVisibleTypes());
                GraphData data = new GraphData(nodes, GraphData.GRAPH_BEZIER, false, false);
                Graph graph = new Graph(data, true, false, false, true, true, false, true, false);

                if (graph.hasMoreThanOnePoint()) {
                    graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                    itemList.add(graph);
                }

                itemList.add(getNewSorter(sortModes, sortModesTitle));
                float goalPace = Reader.get(a).getDistanceGoal(distance);
                if (goalPace != Distance.NO_GOAL_PACE) {
                    Goal goal = new Goal(goalPace, distance);
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
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCE);
        }

        @Override
        protected void getAdapter() {
            adapter = new DistanceRecyclerAdapter(items, distance, originId, a);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCE);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.DISTANCE, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.DISTANCE, smallestFirst);
        }

        @Override
        protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_distance));
            emptyMessage.setText(getString(R.string.empty_message_distance));
            emptyImage.setImageResource(R.drawable.ic_empty_distance_24dp);
        }

        // implements RecyclerAdapter

        @Override
        public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapter.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_DISTANCE);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }

}
