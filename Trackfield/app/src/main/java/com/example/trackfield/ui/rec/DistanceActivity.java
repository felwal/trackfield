package com.example.trackfield.ui.rec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.DelegateClickListener;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.FilterDialog;
import com.example.trackfield.ui.custom.dialog.TimeDialog;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.RecyclerFragment;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.Goal;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
            float goalPace = DbReader.get(this).getDistanceGoal(length);
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

        distance = DbReader.get(this).getDistance(length);
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
            DbWriter.get(this).deleteDistance(distance);
            finish();
        }
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
    public void onTimeDialogNegativeClick(String tag) {
        if (tag.equals(DIALOG_GOAL_DISTANCE)) {
            distance.removeGoalPace();
            DbWriter.get(this).updateDistance(distance);

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
                Prefs.setDistanceVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : MathUtils.createList(DbReader.get(a)
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
                TreeMap<Float, Float> nodes = DbReader.get(a).getPaceNodesByDistance(distance,
                    Prefs.getDistanceVisibleTypes());
                GraphData data = new GraphData(nodes, GraphData.GRAPH_BEZIER, false, false);
                Graph graph = new Graph(data, true, false, false, true, true, false, true, false);

                if (graph.hasMoreThanOnePoint()) {
                    graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                    itemList.add(graph);
                }

                itemList.add(getNewSorter(sortModes, sortModesTitle));
                float goalPace = DbReader.get(a).getDistanceGoal(distance);
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
            adapter = new DistanceAdapter(a, this, items, originId, distance);
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

        // implements DelegateClickListener

        @Override
        public void onDelegateClick(View view, int position) {
            RecyclerItem item = getItem(position);

            if (item instanceof Exerlite) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_DISTANCE);
            }

            super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

        // adapter

        private static class DistanceAdapter extends BaseAdapter {

            private DistanceAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items, int originId,
                int distance) {
                delegatesManager
                    .addDelegate(new ExerciseDistanceAdapterDelegate(activity, listener, this, originId, distance))
                    .addDelegate(new SorterAdapterDelegate(activity, listener, this))
                    .addDelegate(new GraphRecAdapterDelegate(activity))
                    .addDelegate(new GoalAdapterDelegate(activity))
                    .addDelegate(new HeaderSmallAdapterDelegate(activity, listener));

                // Set the items from super class.
                setItems(items);
            }

            // delegate

            private static class ExerciseDistanceAdapterDelegate extends
                BaseAdapterDelegate<Exerlite, RecyclerItem, ExerciseDistanceAdapterDelegate.ExerciseMediumViewHolder> {

                private BaseAdapter adapter;
                private int originId;
                private int distance;

                //

                private ExerciseDistanceAdapterDelegate(Activity activity, DelegateClickListener listener, BaseAdapter adapter,
                    int originId, int distance) {
                    super(activity, listener);
                    this.adapter = adapter;
                    this.originId = originId;
                    this.distance = distance;
                }

                // extends AbsListItemAdapterDelegate

                @Override
                public boolean isForViewType(@NonNull RecyclerItem item) {
                    return item instanceof Exerlite;
                }

                @NonNull
                @Override
                public ExerciseMediumViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                    return new ExerciseMediumViewHolder(inflater.inflate(R.layout.item_exercise_distance, parent, false));
                }

                @Override
                public void onBindViewHolder(Exerlite item, ExerciseMediumViewHolder vh, @Nullable List<Object> payloads) {
                    String values = item.printDistance()
                        + Constants.TAB + item.printTimeByDistance(distance)
                        + Constants.TAB + item.printPace();

                    String date = item.getDate().format(
                        adapter.getSortMode() == Constants.SortMode.DATE || item.isYear(LocalDate.now().getYear()) ?
                            Constants.FORMATTER_REC_NOYEAR : Constants.FORMATTER_REC);

                    vh.primary.setText(date);
                    vh.secondary.setText(values);
                    vh.caption.setText(item.getRoute());
                    vh.originMarker.setVisibility(item.has_id(originId) ? View.VISIBLE : View.GONE);
                    vh.recordMarker.setVisibility(item.isTop() ? View.VISIBLE : View.GONE);
                    vh.recordMarker.getBackground().setColorFilter(context.getColor(
                        item.isTop(1) ? R.color.colorGold : item.isTop(2) ? R.color.colorSilver : R.color.colorBronze),
                        PorterDuff.Mode.MULTIPLY);
                }

                // vh

                class ExerciseMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    public TextView primary;
                    public TextView secondary;
                    public TextView caption;
                    public View originMarker;
                    public View recordMarker;

                    public ExerciseMediumViewHolder(View itemView) {
                        super(itemView);
                        primary = itemView.findViewById(R.id.textView_primary);
                        secondary = itemView.findViewById(R.id.textView_secondary);
                        caption = itemView.findViewById(R.id.textView_caption);
                        originMarker = itemView.findViewById(R.id.view_orignMarker);
                        recordMarker = itemView.findViewById(R.id.view_recordMarker);
                        itemView.setOnClickListener(this);
                    }

                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onDelegateClick(view, getAdapterPosition());
                        }
                    }

                }

            }

        }

    }

}
