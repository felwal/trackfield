package com.felwal.trackfield.ui.main.recordlist;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.main.MainActivity;
import com.felwal.trackfield.ui.main.MainFragment;
import com.felwal.trackfield.ui.main.recordlist.distancelist.DistanceListRecyclerFragment;
import com.felwal.trackfield.ui.main.recordlist.intervallist.IntervalListRecyclerFragment;
import com.felwal.trackfield.ui.main.recordlist.routelist.RouteListRecyclerFragment;
import com.google.android.material.tabs.TabLayout;

public class RecordListFragment extends MainFragment {

    private View view;
    private RecordListPagerAdapter pagerAdapter;

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);

        // set transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setEnterTransition(transitionInflater.inflateTransition(R.transition.explode));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_recordlist, container, false);
        setHasOptionsMenu(true);

        setToolbarTitle();
        setPagerAdapter();

        return view;
    }

    /**
     * Inflates placeholder toolbar menu in order to cover time for recyclerfragments to inflate menus.
     *
     * @see DistanceListRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see RouteListRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see IntervalListRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_recordlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // set

    private void setPagerAdapter() {
        ViewPager viewPager = view.findViewById(R.id.vp_recs);
        TabLayout tabLayout = view.findViewById(R.id.tl_cs);

        pagerAdapter = new RecordListPagerAdapter(viewPager, tabLayout, getActivity(), getChildFragmentManager());
        // set up pager with adapter
        viewPager.setAdapter(pagerAdapter);
        // set up tabs with pager
        tabLayout.setupWithViewPager(viewPager);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_title_records));
    }

    @Override
    public void scrollToTop() {
        pagerAdapter.scrollToTop();
    }

    @Override
    public void updateFragment() {
        pagerAdapter.updateAdapter();
    }

    // implements dialog

    @Override
    protected void onSortSheetClick(int selectedIndex) {
        pagerAdapter.onSortSheetClick(selectedIndex);
    }

}
