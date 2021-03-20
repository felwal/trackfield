package com.example.trackfield.fragments.recyclerfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.activities.recactivity.RouteActivity;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.adapters.recycleradapters.RoutesRecyclerAdapter;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class RoutesRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Recent", "Name", "Amount", "Avg distance", "Best pace" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.NAME, C.SortMode.AMOUNT, C.SortMode.DISTANCE,
        C.SortMode.PACE };
    private final boolean[] smallestFirsts = { false, true, false, false, true };

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
     * Inflates toolbar menu in place of {@link com.example.trackfield.fragments.RecsFragment#onCreateOptionsMenu(Menu,
     * MenuInflater)}
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

        ArrayList<RouteItem> routeItemList = reader.getRouteItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown(),
            Prefs.getExerciseVisibleTypes()); //reader.getRoutes(rList);
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

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
    }

    @Override
    protected void getAdapter() {
        adapter = new RoutesRecyclerAdapter(items, a);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.ROUTE, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.ROUTE, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_routes));
        emptyMessage.setText(getString(R.string.empty_message_routes));
        emptyImage.setImageResource(R.drawable.ic_empty_routes_24dp);
    }

    // implements RecyclerAdapter

    @Override
    public void onItemClick(View view, int position, int itemType) {
        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            RouteActivity.startActivity(a, ((RouteItem) items.get(position)).get_id());
        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}
