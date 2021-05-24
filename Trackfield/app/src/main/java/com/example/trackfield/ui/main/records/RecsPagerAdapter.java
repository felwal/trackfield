package com.example.trackfield.ui.main.records;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.main.records.distances.DistancesRecyclerFragment;
import com.example.trackfield.ui.main.records.intervals.IntervalsRecyclerFragment;
import com.example.trackfield.ui.main.records.routes.RoutesRecyclerFragment;
import com.google.android.material.tabs.TabLayout;

public class RecsPagerAdapter extends FragmentPagerAdapter {

    @StringRes private static final int[] TAB_TITLES = new int[] {
        R.string.tab_title_records_distances, R.string.tab_title_records_routes, R.string.tab_title_records_intervals };

    private static final int POS_DISTANCES = 0;
    private static final int POS_ROUTES = 1;
    private static final int POS_INTERVALS = 2;

    private final Context c;
    private final ViewPager viewPager;

    private RecyclerFragment distancesFragment;
    private RecyclerFragment routesFragment;
    private RecyclerFragment intervalsFragment;

    //

    public RecsPagerAdapter(ViewPager viewPager, TabLayout tabLayout, Context context, FragmentManager fm) {
        super(fm);
        c = context;
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
            case POS_INTERVALS: return (intervalsFragment = new IntervalsRecyclerFragment());
            case POS_ROUTES: return (routesFragment = new RoutesRecyclerFragment());
            case POS_DISTANCES:
            default: return (distancesFragment = new DistancesRecyclerFragment());
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
        if (distancesFragment != null) distancesFragment.updateRecycler();
        if (routesFragment != null) routesFragment.updateRecycler();
        if (intervalsFragment != null) intervalsFragment.updateRecycler();
    }

    public void onSortSheetClick(int selectedIndex) {
        getCurrentFragment().onSortSheetDismiss(selectedIndex);
    }

}
