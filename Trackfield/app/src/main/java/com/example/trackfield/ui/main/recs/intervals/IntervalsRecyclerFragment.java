package com.example.trackfield.ui.main.recs.intervals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.model.recycleritems.IntervalItem;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.recs.general.RecsFragment;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.model.recycleritems.RecyclerItem;
import com.example.trackfield.model.recycleritems.Sorter;
import com.example.trackfield.service.toolbox.C;
import com.example.trackfield.service.file.Prefs;
import com.example.trackfield.ui.main.recs.intervals.interval.IntervalActivity;

import java.util.ArrayList;

public class IntervalsRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Recent", "Amount" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.AMOUNT };
    private final boolean[] smallestFirsts = { false, false };

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // show hidden
        MenuItem hiddenItem = menu.findItem(R.id.action_showHidden);
        hiddenItem.setChecked(Prefs.areHiddenRoutesShown());
        if (Prefs.areHiddenRoutesShown()) hiddenItem.setIcon(R.drawable.ic_hidden_24dp)
            .setTitle(R.string.action_hide_hidden);
        else hiddenItem.setIcon(R.drawable.ic_hide_24dp).setTitle(R.string.action_show_hidden);
    }

    /**
     * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu, MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_recs
        inflater.inflate(R.menu.menu_toolbar_main_recs_routes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        Sorter sorter = getNewSorter(sortModes, sortModesTitle);
        itemList.add(sorter);
        itemList.addAll(intervalItemList);
        if (intervalItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.INTERVALS);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.INTERVALS);
    }

    @Override
    protected void getAdapter() {
        adapter = new IntervalsRecyclerAdapter(items, a);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.INTERVALS);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.INTERVALS);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.INTERVALS, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.INTERVALS, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_intervals));
        emptyMessage.setText(getString(R.string.empty_message_intervals));
        emptyImage.setImageResource(R.drawable.ic_empty_interval_24dp);
    }

    // implements RecyclerAdapter

    @Override
    public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            IntervalActivity.startActivity(a, ((IntervalItem) items.get(position)).getInterval());
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
