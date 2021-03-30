package com.example.trackfield.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.trackfield.R;
import com.example.trackfield.fragments.recyclerfragments.DistancesRecyclerFragment;
import com.example.trackfield.fragments.recyclerfragments.IntervalsRecyclerFragment;
import com.example.trackfield.fragments.recyclerfragments.RecyclerFragment;
import com.example.trackfield.fragments.recyclerfragments.RoutesRecyclerFragment;
import com.example.trackfield.toolbox.C;

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

    public void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        getCurrentFragment().onSortSheetDismiss(sortMode, smallestFirst);
    }

}
