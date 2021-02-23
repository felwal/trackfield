package com.example.trackfield.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.trackfield.R;
import com.example.trackfield.fragments.recycler_fragments.DistancesRecyclerFragment;
import com.example.trackfield.fragments.recycler_fragments.IntervalsRecyclerFragment;
import com.example.trackfield.fragments.recycler_fragments.RecyclerFragment;
import com.example.trackfield.fragments.recycler_fragments.RoutesRecyclerFragment;
import com.example.trackfield.toolbox.C;

public class RecsPagerAdapter extends FragmentPagerAdapter {

    private final Context c;

    private RecyclerFragment selectedRF;
    private DistancesRecyclerFragment distanceRF;
    private RoutesRecyclerFragment routeRF;
    private IntervalsRecyclerFragment intervalRF;

    @StringRes private static final int[] TAB_TITLES = new int[] { R.string.tab_distances, R.string.tab_routes, R.string.tab_intervals };

    ////

    public RecsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        c = context;
    }

    // extends FragmentPagerAdapter

    @NonNull @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return selectedRF = distanceRF != null ? distanceRF : (distanceRF = new DistancesRecyclerFragment());
            case 1: return selectedRF = routeRF != null ? routeRF : (routeRF = new RoutesRecyclerFragment());
            default: return selectedRF = intervalRF != null ? intervalRF : (intervalRF = new IntervalsRecyclerFragment());
        }
    }

    @Nullable @Override
    public CharSequence getPageTitle(int position) {
        return c.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    // delegate from RecsFragment to RecyclerFragments TODO: bara till den aktiva!

    public void scrollToTop() {
        selectedRF.scrollToTop();
    }

    public void updateAdapter() {
        if (distanceRF != null) distanceRF.updateRecycler();
        if (routeRF != null) routeRF.updateRecycler();
        if (intervalRF != null) intervalRF.updateRecycler();
    }

    public void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        if (distanceRF != null) distanceRF.onSortSheetDismiss(sortMode, smallestFirst);
        if (routeRF != null) routeRF.onSortSheetDismiss(sortMode, smallestFirst);
        if (intervalRF != null) intervalRF.onSortSheetDismiss(sortMode, smallestFirst);
    }

}
