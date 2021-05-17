package com.example.trackfield.ui.rec.interval;

import android.os.Bundle;
import android.view.View;

import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.model.SortMode;

import java.util.ArrayList;

public class IntervalRecyclerFragment extends RecyclerFragment {

    // bundle keys
    private final static String BUNDLE_INTERVAL = "interval";
    private final static String BUNDLE_ORIGINID = "originId";

    private final Sorter sorter = new Sorter(
        new SortMode("Date", SortMode.Mode.DATE, false),
        new SortMode("Distance", SortMode.Mode.DISTANCE, false),
        new SortMode("Time", SortMode.Mode.TIME, true),
        new SortMode("Pace", SortMode.Mode.PACE, true)
    );

    private String interval;
    private int originId;

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
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByInterval(interval, sorter.getMode(),
            sorter.isAscending());

        itemList.add(sorter.copy());
        addHeadersAndItems(itemList, exerliteList, sorter.getMode());

        return itemList;
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.INTERVAL),
            Prefs.getSorterInversion(AppConsts.Layout.INTERVAL));
    }

    @Override
    protected void setAdapter() {
        adapter = new IntervalAdapter(a, this, items, originId);
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.INTERVAL, sorter.getSelectedIndex(), sorter.isOrderInverted());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            if (originId != id) ViewActivity.startActivity(a, id, ViewActivity.FROM_INTERVAL);
        }

        super.onDelegateClick(item);
    }

}
