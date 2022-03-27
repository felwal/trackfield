package me.felwal.trackfield.ui.main.groupingpager;

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

import com.google.android.material.tabs.TabLayout;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.main.MainFragment;
import me.felwal.trackfield.ui.main.groupingpager.distancelist.DistanceListFragment;
import me.felwal.trackfield.ui.main.groupingpager.intervallist.IntervalListFragment;
import me.felwal.trackfield.ui.main.groupingpager.placelist.PlaceListFragment;
import me.felwal.trackfield.ui.main.groupingpager.routelist.RouteListFragment;

public class GroupingPagerFragment extends MainFragment {

    private View view;
    private GroupingPagerStateAdapter pagerAdapter;

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

        view = inflater.inflate(R.layout.fragment_groupingpager, container, false);
        setHasOptionsMenu(true);

        setToolbarTitle();
        setPagerAdapter();

        return view;
    }

    /**
     * Inflates placeholder toolbar menu in order to cover time for recyclerfragments to inflate menus.
     *
     * @see DistanceListFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see RouteListFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see PlaceListFragment#onCreateOptionsMenu(Menu, MenuInflater)
     * @see IntervalListFragment#onCreateOptionsMenu(Menu, MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_groupingpager, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // set

    private void setPagerAdapter() {
        ViewPager viewPager = view.findViewById(R.id.vp_groupingpager);
        TabLayout tabLayout = view.findViewById(R.id.tl_groupingpager);

        viewPager.setOffscreenPageLimit(3);

        pagerAdapter = new GroupingPagerStateAdapter(viewPager, tabLayout, getActivity(), getChildFragmentManager());
        // set up pager with adapter
        viewPager.setAdapter(pagerAdapter);
        // set up tabs with pager
        tabLayout.setupWithViewPager(viewPager);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_title_groupingpager));
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
