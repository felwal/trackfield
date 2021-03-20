package com.example.trackfield.fragments.recyclerfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.activities.recactivity.DistanceActivity;
import com.example.trackfield.adapters.recycleradapters.DistancesRecyclerAdapter;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class DistancesRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Distance" };
    private final C.SortMode[] sortModes = { C.SortMode.DISTANCE };
    private final boolean[] smallestFirsts = { true };

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Inflates toolbar menu in place of {@link com.example.trackfield.fragments.RecsFragment#onCreateOptionsMenu(Menu,
     * MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_recs
        inflater.inflate(R.menu.menu_toolbar_main_recs_distances, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(Distance.SortMode.DISTANCE/*sortMode*/,
            smallestFirst, Prefs.getExerciseVisibleTypes());
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        Sorter sorter = getNewSorter(sortModes, sortModesTitle);
        itemList.add(sorter);
        itemList.addAll(distanceItemList);
        if (distanceItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.DISTANCES);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCES);
    }

    @Override
    protected void getAdapter() {
        adapter = new DistancesRecyclerAdapter(items, a);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.DISTANCES);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCES);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.DISTANCES, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.DISTANCES, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_distances));
        emptyMessage.setText(getString(R.string.empty_message_distances));
        emptyImage.setImageResource(R.drawable.ic_empty_distances_24dp);
    }

    // implements RecyclerAdapter

    @Override
    public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
