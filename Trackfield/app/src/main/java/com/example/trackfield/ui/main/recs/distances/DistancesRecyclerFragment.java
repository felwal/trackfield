package com.example.trackfield.ui.main.recs.distances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.main.recs.RecsFragment;
import com.example.trackfield.ui.main.recs.distances.model.DistanceItem;
import com.example.trackfield.ui.rec.distance.DistanceActivity;
import com.example.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class DistancesRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Distance" };
    private final AppConsts.SortMode[] sortModes = { AppConsts.SortMode.DISTANCE };
    private final boolean[] smallestFirsts = { true };

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu,
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
        sortMode = Prefs.getSortModePref(AppConsts.Layout.DISTANCES);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.DISTANCES);
    }

    @Override
    protected void getAdapter() {
        adapter = new DistancesAdapter(a, this, items);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(AppConsts.Layout.DISTANCES);
        smallestFirst = Prefs.getSmallestFirstPref(AppConsts.Layout.DISTANCES);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(AppConsts.Layout.DISTANCES, sortMode);
        Prefs.setSmallestFirstPref(AppConsts.Layout.DISTANCES, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_distances));
        emptyMessage.setText(getString(R.string.empty_message_distances));
        emptyImage.setImageResource(R.drawable.ic_empty_distances_24dp);
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof DistanceItem) {
            DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
        }

        super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
