package com.example.trackfield.fragments;

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
import com.example.trackfield.activities.MainActivity;
import com.example.trackfield.activities.MainActivity.MainFragment;
import com.example.trackfield.adapters.RecsPagerAdapter;
import com.example.trackfield.toolbox.C;
import com.google.android.material.tabs.TabLayout;

public class RecsFragment extends MainFragment {

    private View view;
    private RecsPagerAdapter recsPagerAdapter;

    ////

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setEnterTransition(transitionInflater.inflateTransition(R.transition.explode));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recs, container, false);
        //setHasOptionsMenu(true);

        setToolbarTitle();
        setPagerAdapter();

        return view;
    }

    /*@Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_recs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }*/

    // set

    private void setPagerAdapter() {
        // set pagerAdapter to viewPager
        final ViewPager viewPager = view.findViewById(R.id.view_pager);
        recsPagerAdapter = new RecsPagerAdapter(viewPager, getActivity().getApplicationContext(), getChildFragmentManager());
        viewPager.setAdapter(recsPagerAdapter);

        // set viewPager to tabs
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_records));
    }

    @Override
    public void scrollToTop() {
        recsPagerAdapter.scrollToTop();
    }

    @Override
    public void updateFragment() {
        recsPagerAdapter.updateAdapter();
    }

    // implements dialog

    @Override
    protected void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        recsPagerAdapter.onSortSheetDismiss(sortMode, smallestFirst);
    }

}
