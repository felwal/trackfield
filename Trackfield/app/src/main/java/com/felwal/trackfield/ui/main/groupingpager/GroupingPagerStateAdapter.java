package com.felwal.trackfield.ui.main.groupingpager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.main.groupingpager.distancelist.DistanceListFragment;
import com.felwal.trackfield.ui.main.groupingpager.intervallist.IntervalListFragment;
import com.felwal.trackfield.ui.main.groupingpager.placelist.PlaceListFragment;
import com.felwal.trackfield.ui.main.groupingpager.routelist.RouteListFragment;
import com.google.android.material.tabs.TabLayout;

public class GroupingPagerStateAdapter extends FragmentPagerAdapter {

    @StringRes private static final int[] TAB_TITLES = new int[] {
        R.string.tab_title_groupingpager_distances, R.string.tab_title_groupingpager_routes,
        R.string.tab_title_groupingpager_places, R.string.tab_title_groupingpager_intervals };

    private static final int POS_DISTANCES = 0;
    private static final int POS_ROUTES = 1;
    private static final int POS_PLACES = 2;
    private static final int POS_INTERVALS = 3;

    private final Context c;
    private final ViewPager viewPager;

    private RecyclerFragment distancesFragment;
    private RecyclerFragment routesFragment;
    private RecyclerFragment placesFragment;
    private RecyclerFragment intervalsFragment;

    //

    public GroupingPagerStateAdapter(ViewPager viewPager, TabLayout tabLayout, Context c, FragmentManager fm) {
        super(fm);
        this.c = c;
        this.viewPager = viewPager;

        // add tab listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // we are only interested in reselection
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // we are only interested in reselection
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.isSelected()) scrollToTop();
            }
        });
    }

    // extends FragmentPagerAdapter

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POS_INTERVALS: return (intervalsFragment = new IntervalListFragment());
            case POS_ROUTES: return (routesFragment = new RouteListFragment());
            case POS_PLACES: return (placesFragment = new PlaceListFragment());
            case POS_DISTANCES:
            default: return (distancesFragment = new DistanceListFragment());
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

    // delegate from GroupingPagerFragment to RecyclerFragment

    public void scrollToTop() {
        getCurrentFragment().scrollToTop();
    }

    public void updateAdapter() {
        if (distancesFragment != null) distancesFragment.updateRecycler();
        if (routesFragment != null) routesFragment.updateRecycler();
        if (placesFragment != null) placesFragment.updateRecycler();
        if (intervalsFragment != null) intervalsFragment.updateRecycler();
    }

    public void onSortSheetClick(int selectedIndex) {
        getCurrentFragment().onSortSheetDismiss(selectedIndex);
    }

}
