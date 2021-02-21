package com.example.trackfield.fragments.recycler_fragments;

import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.activities.rec_activity.IntervalRecActivity;
import com.example.trackfield.adapters.recycler_adapters.InRecyclerAdapter;
import com.example.trackfield.adapters.recycler_adapters.RecyclerAdapter;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class InRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Recent", "Amount" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.AMOUNT};
    private final boolean[] smallestFirsts = { false, false};

    ////

    @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        Sorter sorter = newSorter(sortModes, sortModesTitle);
        itemList.add(sorter);
        itemList.addAll(intervalItemList);
        if (intervalItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }
    @Override protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
    }
    @Override protected void getAdapter() {
        adapter = new InRecyclerAdapter(items, a);
    }
    @Override protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
    }
    @Override protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.ROUTE, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.ROUTE, smallestFirst);
    }
    @Override protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_intervals));
        emptyMessage.setText(getString(R.string.empty_message_intervals));
        emptyImage.setImageResource(R.drawable.ic_empty_interval_24dp);
    }

    @Override public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            IntervalRecActivity.startActivity(a, ((IntervalItem) items.get(position)).getInterval() );
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
