package com.example.trackfield.ui.main.recs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.ui.main.model.DistanceItem;
import com.example.trackfield.ui.main.model.IntervalItem;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.main.model.RouteItem;
import com.example.trackfield.ui.main.model.Sorter;
import com.example.trackfield.ui.main.recs.adapters.DistancesRecyclerAdapter;
import com.example.trackfield.ui.main.recs.adapters.IntervalsRecyclerAdapter;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.recs.adapters.RoutesRecyclerAdapter;
import com.example.trackfield.ui.rec.DistanceActivity;
import com.example.trackfield.ui.rec.IntervalActivity;
import com.example.trackfield.ui.rec.RouteActivity;
import com.example.trackfield.utils.Constants;

import java.util.ArrayList;

public class RecsPagerAdapter extends FragmentPagerAdapter {

    private final Context c;

    private final ViewPager viewPager;
    private RecyclerFragment distanceFragment;
    private RecyclerFragment routeFragment;
    private RecyclerFragment intervalFragment;

    @StringRes private static final int[] TAB_TITLES = new int[]{ R.string.tab_distances, R.string.tab_routes, R.string.tab_intervals };
    private static final int POS_DISTANCES = 0;
    private static final int POS_ROUTES = 1;
    private static final int POS_INTERVALS = 2;

    //

    public RecsPagerAdapter(ViewPager viewPager, Context context, FragmentManager fm) {
        super(fm);
        c = context;
        this.viewPager = viewPager;
    }

    // extends FragmentPagerAdapter

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POS_ROUTES:
                return (routeFragment = new RoutesRecyclerFragment());
            case POS_INTERVALS:
                return (intervalFragment = new IntervalsRecyclerFragment());
            case POS_DISTANCES:
            default:
                return (distanceFragment = new DistancesRecyclerFragment());
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return c.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    // get

    private RecyclerFragment getCurrentFragment() {
        return (RecyclerFragment) instantiateItem(viewPager, viewPager.getCurrentItem());
    }

    // delegate from RecsFragment to RecyclerFragment

    public void scrollToTop() {
        getCurrentFragment().scrollToTop();
    }

    public void updateAdapter() {
        if (distanceFragment != null) distanceFragment.updateRecycler();
        if (routeFragment != null) routeFragment.updateRecycler();
        if (intervalFragment != null) intervalFragment.updateRecycler();
    }

    public void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst) {
        getCurrentFragment().onSortSheetDismiss(sortMode, smallestFirst);
    }

    // fragments

    public static class DistancesRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Distance" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DISTANCE };
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
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCES);
        }

        @Override
        protected void getAdapter() {
            adapter = new DistancesRecyclerAdapter(items, a);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCES);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.DISTANCES, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.DISTANCES, smallestFirst);
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

    public static class RoutesRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Recent", "Name", "Amount", "Avg distance", "Best pace" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.NAME, Constants.SortMode.AMOUNT, Constants.SortMode.DISTANCE,
            Constants.SortMode.PACE };
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
         * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu,
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
            sortMode = Prefs.getSortModePref(Constants.Layout.ROUTES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTES);
        }

        @Override
        protected void getAdapter() {
            adapter = new RoutesRecyclerAdapter(items, a);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.ROUTES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTES);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.ROUTES, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.ROUTES, smallestFirst);
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

    public static class IntervalsRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Recent", "Amount" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.AMOUNT };
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
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVALS);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVALS);
        }

        @Override
        protected void getAdapter() {
            adapter = new IntervalsRecyclerAdapter(items, a);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVALS);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVALS);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.INTERVALS, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.INTERVALS, smallestFirst);
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

}
