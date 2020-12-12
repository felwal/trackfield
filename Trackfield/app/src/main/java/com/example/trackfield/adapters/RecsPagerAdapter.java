package com.example.trackfield.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.trackfield.R;
import com.example.trackfield.fragments.RecyclerFragments;

public class RecsPagerAdapter extends FragmentPagerAdapter {

    private final Context c;

    private RecyclerFragments.Base selectedRF;
    private RecyclerFragments.DistanceRF distanceRF;
    private RecyclerFragments.RouteRF routeRF;
    private RecyclerFragments.IntervalRF intervalRF;

    @StringRes private static final int[] TAB_TITLES = new int[] { R.string.tab_distances, R.string.tab_routes, R.string.tab_intervals };

    ////

    public RecsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        c = context;
    }

    @Override public Fragment getItem(int position) {
        switch (position) {
            case 0: return selectedRF = distanceRF != null ? distanceRF : (distanceRF = new RecyclerFragments.DistanceRF());
            case 1: return selectedRF = routeRF != null ? routeRF : (routeRF = new RecyclerFragments.RouteRF());
            default: return selectedRF = intervalRF != null ? intervalRF : (intervalRF = new RecyclerFragments.IntervalRF());
        }
    }
    @Nullable @Override public CharSequence getPageTitle(int position) {
        return c.getResources().getString(TAB_TITLES[position]);
    }
    @Override public int getCount() {
        return TAB_TITLES.length;
    }

    public void scrollToTop() {
        selectedRF.scrollToTop();
    }
    public void updateAdapter() {
        if (distanceRF != null) distanceRF.updateRecycler();
        if (routeRF != null) routeRF.updateRecycler();
        if (intervalRF != null) intervalRF.updateRecycler();
    }

}