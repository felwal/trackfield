package com.example.trackfield.ui.rec;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.TextDialog;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.main.model.Sorter;
import com.example.trackfield.ui.rec.adapters.IntervalRecyclerAdapter;
import com.example.trackfield.utils.Constants;

import java.util.ArrayList;

public class IntervalActivity extends RecActivity implements TextDialog.DialogListener {

    private String interval;

    public static final String EXTRA_INTERVAL = "interval";
    private static final String DIALOG_RENAME_INTERVAL = "renameIntervalDialog";

    //

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

    // extends AppCompatActivity

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_renameInterval) {
            if (interval != null) {
                TextDialog.newInstance(R.string.dialog_title_rename_interval, BaseDialog.NO_RES,
                        interval, "", R.string.dialog_btn_rename, DIALOG_RENAME_INTERVAL)
                        .show(getSupportFragmentManager());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // get

    public String getInterval() {
        return interval;
    }

    // extends RecActivity

    @Override
    protected void getExtras(Intent intent) {
        if (!intent.hasExtra(EXTRA_INTERVAL)) return;

        interval = intent.getStringExtra(EXTRA_INTERVAL);
        setToolbar(interval);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;
        selectFragment(IntervalRecyclerFragment.newInstance(interval, originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_interval;
    }

    // implements TextDialog

    @Override public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RENAME_INTERVAL)) {
            if (input.equals("")) return;

            DbWriter.get(this).updateInterval(interval, input);

            finish();
            startActivity(this, input, originId);
        }
    }

    // fragment

    public static class IntervalRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.DISTANCE, Constants.SortMode.TIME, Constants.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, false, true, true };

        private String interval;
        private int originId;

        private final static String BUNDLE_INTERVAL = "interval";
        private final static String BUNDLE_ORIGINID = "originId";

        //

        public static IntervalRecyclerFragment newInstance(String interval, int originId) {
            IntervalRecyclerFragment instance = new IntervalRecyclerFragment();
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_INTERVAL, interval);
            bundle.putInt(BUNDLE_ORIGINID, originId);
            instance.setArguments(bundle);
            return instance;
        }

        // extends Fragment

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null) {
                interval = bundle.getString(BUNDLE_INTERVAL, "");
                originId = bundle.getInt(BUNDLE_ORIGINID, -1);
            }
        }

        // extends RecyclerFragment

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByInterval(interval, sortMode, smallestFirst);
            //ArrayList<Exerlite> chronoList = reader.getExerlitesByInterval(interval, C.SortMode.DATE, false);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            /*Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
            itemList.add(graph);*/
            Sorter sorter = getNewSorter(sortModes, sortModesTitle);
            itemList.add(sorter);

            addHeadersAndItems(itemList, exerliteList);
            /*if (sortMode == C.SortMode.DATE) {
                int year = -1; int newYear;
                for (Exerlite e : exerliteList) {
                    if ((newYear = e.getDate().getYear()) != year) {
                        itemList.add(new Header(newYear + "", Header.Type.REC));
                        year = newYear;
                    }
                    itemList.add(e);
                }
            }
            else if ((sortMode == C.SortMode.PACE || sortMode == C.SortMode.TIME) && smallestFirst && exerliteList.size() > 10) {
                for (int i = 0; i < exerliteList.size(); i++) {
                    if (i % 10 == 0) {
                        itemList.add(new Header("Top " + (i+10), Header.Type.REC));
                    }
                    itemList.add(exerliteList.get(i));
                }
            }
            else { itemList.addAll(exerliteList); }*/

            return itemList;
        }

        @Override
        protected void setSortModes() {
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVAL);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVAL);
        }

        @Override
        protected void getAdapter() {
            adapter = new IntervalRecyclerAdapter(items, originId, a);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVAL);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVAL);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.INTERVAL, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.INTERVAL, smallestFirst);
        }

        @Override
        public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapter.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_INTERVAL);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }

}
