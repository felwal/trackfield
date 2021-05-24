package com.example.trackfield.ui.main.records;

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

import com.example.trackfield.R;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.main.MainFragment;
import com.example.trackfield.ui.main.records.distances.DistancesRecyclerFragment;
import com.example.trackfield.ui.main.records.intervals.IntervalsRecyclerFragment;
import com.example.trackfield.ui.main.records.routes.RoutesRecyclerFragment;
import com.google.android.material.tabs.TabLayout;

public class RecsFragment extends MainFragment {

    private View view;
    private RecsPagerAdapter pagerAdapter;

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

        view = inflater.inflate(R.layout.fragment_recs, container, false);
        setHasOptionsMenu(true);

        setToolbarTitle();
        setPagerAdapter();

        return view;
    }

    /**
     * Inflates placeholder toolbar menu in order to cover time for recyclerfragments to inflate menus.
     *
     * @see DistancesRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see RoutesRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see IntervalsRecyclerFragment#onCreateOptionsMenu(Menu, MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_recs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // set

    private void setPagerAdapter() {
        ViewPager viewPager = view.findViewById(R.id.vp_recs);
        TabLayout tabLayout = view.findViewById(R.id.tl_cs);

        pagerAdapter = new RecsPagerAdapter(viewPager, tabLayout, getActivity(), getChildFragmentManager());
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
