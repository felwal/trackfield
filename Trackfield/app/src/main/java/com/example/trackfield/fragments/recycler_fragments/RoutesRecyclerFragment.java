package com.example.trackfield.fragments.recycler_fragments;

import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.activities.rec_activity.RouteActivity;
import com.example.trackfield.adapters.recycler_adapters.RecyclerAdapter;
import com.example.trackfield.adapters.recycler_adapters.RoutesRecyclerAdapter;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class RoutesRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Recent", "Name", "Amount", "Avg distance", "Best pace" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.NAME, C.SortMode.AMOUNT, C.SortMode.DISTANCE, C.SortMode.PACE };
    private final boolean[] smallestFirsts = { false, true, false, false, true };

    ////

    @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<RouteItem> routeItemList = reader.getRouteItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown(), Prefs.getExerciseVisibleTypes()); //reader.getRoutes(rList);
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        Sorter sorter = getNewSorter(sortModes, sortModesTitle);
        itemList.add(sorter);
        itemList.addAll(routeItemList);
        if (routeItemList.size() == 0) {
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
        adapter = new RoutesRecyclerAdapter(items, a);
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
        emptyTitle.setText(getString(R.string.empty_title_routes));
        emptyMessage.setText(getString(R.string.empty_message_routes));
        emptyImage.setImageResource(R.drawable.ic_empty_routes_24dp);
    }

    @Override public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            RouteActivity.startActivity(a, ((RouteItem) items.get(position)).get_id());
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
