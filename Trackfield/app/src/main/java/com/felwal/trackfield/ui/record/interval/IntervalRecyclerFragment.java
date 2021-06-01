package com.felwal.trackfield.ui.record.interval;

import android.os.Bundle;
import android.view.View;

import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.Sorter;
import com.felwal.trackfield.ui.exercise.ViewActivity;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.model.SortMode;

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
        addItemsWithHeaders(itemList, exerliteList, sorter.getMode());

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
