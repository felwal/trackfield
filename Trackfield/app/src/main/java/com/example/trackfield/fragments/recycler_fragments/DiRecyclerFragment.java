package com.example.trackfield.fragments.recycler_fragments;

import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.activities.RecActivity;
import com.example.trackfield.adapters.recycler_adapters.DiRecyclerAdapter;
import com.example.trackfield.adapters.recycler_adapters.RecyclerAdapter;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class DiRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Distance", "Amount", "Best time", "Best pace" };
    private final C.SortMode[] sortModes = { C.SortMode.DISTANCE, C.SortMode.AMOUNT, C.SortMode.TIME, C.SortMode.PACE };
    private final boolean[] smallestFirsts = { true, false, false, true };

    ////

    @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(Distance.SortMode.DISTANCE/*sortMode*/, smallestFirst, Prefs.getExerciseVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        Sorter sorter = newSorter(sortModes, sortModesTitle);
        itemList.add(sorter);
        itemList.addAll(distanceItemList);
        if (distanceItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();



        return itemList;
    }
    @Override protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCE);
    }
    @Override protected void getAdapter() {
        adapter = new DiRecyclerAdapter(items, a);
    }
    @Override protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.DISTANCE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCE);
    }
    @Override protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.DISTANCE, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.DISTANCE, smallestFirst);
    }
    @Override protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_distances));
        emptyMessage.setText(getString(R.string.empty_message_distances));
        emptyImage.setImageResource(R.drawable.ic_empty_distances_24dp);
    }

    @Override public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            RecActivity.DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
