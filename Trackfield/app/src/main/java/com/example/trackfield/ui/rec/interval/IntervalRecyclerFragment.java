package com.example.trackfield.ui.rec.interval;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class IntervalRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
    private final AppConsts.SortMode[] sortModes = { AppConsts.SortMode.DATE, AppConsts.SortMode.DISTANCE, AppConsts.SortMode.TIME, AppConsts.SortMode.PACE };
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
        sortMode = Prefs.getSortModePref(AppConsts.Layout.INTERVAL);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.INTERVAL);
    }

    @Override
    protected void getAdapter() {
        adapter = new IntervalAdapter(a, this, items, originId);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(AppConsts.Layout.INTERVAL);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.INTERVAL);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(AppConsts.Layout.INTERVAL, sortMode);
        Prefs.setSmallestFirstPref(AppConsts.Layout.INTERVAL, smallestFirst);
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int _id = ((Exerlite) items.get(position)).get_id();
            if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_INTERVAL);
        }

        super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}