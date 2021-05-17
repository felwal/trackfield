package com.example.trackfield.ui.main.stats;

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
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.main.MainFragment;

public class StatsFragment extends MainFragment {

    private StatsRecyclerFragment recyclerFragment;

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set transition
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        //setEnterTransition(transitionInflater.inflateTransition(R.transition.fade));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        FrameLayout frame = view.findViewById(R.id.frameLayout_scrollerFrameDev);
        recyclerFragment = new StatsRecyclerFragment();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_stats, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_stats));
    }

    @Override
    protected void scrollToTop() {
        recyclerFragment.scrollToTop();
    }

    @Override
    protected void updateFragment() {
        if (recyclerFragment != null) recyclerFragment.updateRecycler();
    }

    @Override
    protected void onSortSheetClick(int selectedIndex) {
        recyclerFragment.onSortSheetDismiss(selectedIndex);
    }

}