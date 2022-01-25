package com.felwal.trackfield.ui.main.exerciselist;

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

import com.felwal.android.util.ViewUtilsKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.main.MainActivity;
import com.felwal.trackfield.ui.main.MainFragment;

public class ExerciseListFragment extends MainFragment {

    private ExerciseListRecyclerFragment recyclerFragment;

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);

        // set transiton
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        //setEnterTransition(transitionInflater.inflateTransition(R.transition.fade));
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exerciselist, container, false);
        setHasOptionsMenu(true);
        setToolbarTitle();

        FrameLayout frame = view.findViewById(R.id.fl_exerciselist);
        recyclerFragment = new ExerciseListRecyclerFragment();
        getChildFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_exerciselist, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // search

        final MenuItem searchItem = menu.findItem(R.id.action_search_exercises);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search");
        ViewUtilsKt.setActionItemRipple(ViewUtilsKt.getCloseIcon(searchView));
        searchView.setBackgroundResource(R.drawable.layer_searchview_bg);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // clear focus to not reopen keyboard when activity is resumed
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerFragment.updateSearch(newText);
                return true;
            }

        });
    }

    // extends MainFragment

    @Override
    protected void setToolbarTitle() {
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.fragment_title_exerciselist));
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
    protected void onSortSheetClick(int selectedIndex) {
        recyclerFragment.onSortSheetDismiss(selectedIndex);
    }

}
