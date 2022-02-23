package me.felwal.trackfield.ui.main.statistics;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.main.MainFragment;

public class StatisticsFragment extends MainFragment {

    private StatisticsRecyclerFragment recyclerFragment;

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
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        FrameLayout frame = view.findViewById(R.id.fl_statistics);
        recyclerFragment = new StatisticsRecyclerFragment();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_statistics, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_title_statistics));
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