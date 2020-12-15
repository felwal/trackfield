package com.example.trackfield.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.trackfield.R;
import com.example.trackfield.activities.MainActivity;

public class DevFragment extends MainActivity.MainFragment {

    private View view;
    private FrameLayout frame;
    private RecyclerFragments.DevRF recyclerFragment;

    ////

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        //setEnterTransition(transitionInflater.inflateTransition(R.transition.fade));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dev, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        frame = view.findViewById(R.id.frameLayout_scrollerFrameDev);
        recyclerFragment = new RecyclerFragments.DevRF();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_dev, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_dev));
    }
    @Override protected void scrollToTop() {

    }
    @Override protected void updateFragment() {
        if (recyclerFragment != null) recyclerFragment.updateRecycler();
    }

}