package com.example.trackfield.fragments;

import android.os.Bundle;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.activities.MainActivity;
import com.example.trackfield.fragments.recyclerfragments.StatsRecyclerFragment;
import com.example.trackfield.toolbox.C;

public class StatsFragment extends MainActivity.MainFragment {

    private View view;
    private FrameLayout frame;
    private StatsRecyclerFragment recyclerFragment;

    ////

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        //setEnterTransition(transitionInflater.inflateTransition(R.transition.fade));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dev, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        frame = view.findViewById(R.id.frameLayout_scrollerFrameDev);
        recyclerFragment = new StatsRecyclerFragment();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_dev, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_stats));
    }

    @Override
    protected void scrollToTop() {

    }

    @Override
    protected void updateFragment() {
        if (recyclerFragment != null) recyclerFragment.updateRecycler();
    }

    @Override
    protected void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        recyclerFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

}