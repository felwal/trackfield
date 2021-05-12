package com.example.trackfield.ui.main.exercises;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.example.trackfield.R;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.main.MainActivity.MainFragment;
import com.example.trackfield.utils.AppConsts;

public class ExercisesFragment extends MainFragment {

    private View view;
    private FrameLayout frame;
    private ExercisesRecyclerFragment recyclerFragment;

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        //setEnterTransition(transitionInflater.inflateTransition(R.transition.fade));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exercises, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        frame = view.findViewById(R.id.frameLayout_scrollerFrameMain);
        recyclerFragment = new ExercisesRecyclerFragment();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_exercises, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // search
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                recyclerFragment.updateSearch(newText);
                return false;
            }
        });

    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_exercises));
    }

    @Override
    public void scrollToTop() {
        recyclerFragment.scrollToTop();
    }

    @Override
    public void updateFragment() {
        if (recyclerFragment != null) recyclerFragment.updateRecycler();
    }

    @Override
    protected void onSortSheetDismiss(AppConsts.SortMode sortMode, boolean smallestFirst) {
        recyclerFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

}
