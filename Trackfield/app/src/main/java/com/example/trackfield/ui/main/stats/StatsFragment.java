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
import com.example.trackfield.data.db.Reader;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.main.recs.adapters.RoutesRecyclerAdapter;
import com.example.trackfield.utils.Constants;

import java.time.LocalDate;
import java.util.ArrayList;

public class StatsFragment extends MainActivity.MainFragment {

    private View view;
    private FrameLayout frame;
    private StatsRecyclerFragment recyclerFragment;

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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)  {
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

    }

    @Override
    protected void updateFragment() {
        if (recyclerFragment != null) recyclerFragment.updateRecycler();
    }

    @Override
    protected void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst) {
        recyclerFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

    // fragment

    public static class StatsRecyclerFragment extends RecyclerFragment {

        // extends Fragment

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_toolbar_main_stats, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        // extends RecyclerFragment

        @Override
        protected void setSortModes() {
        }

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            //GraphData dataGoalMonth = new GraphData(Reader.get(a).monthIntegralDistanceGoal(LocalDate.now()),
            //    GraphData.GRAPH_LINE, false, true);
            //dataGoalMonth.setPaint("#003E3F43", "#FF252528");
            GraphData dataThisMonth = new GraphData(
                Reader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                GraphData.GRAPH_LINE, false, false);
            GraphData dataLastMonth = new GraphData(
                Reader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusMonths(1)),
                GraphData.GRAPH_LINE, false, false);
            dataLastMonth.setPaint(R.attr.colorOnBackground, a);

            Graph monthGraph = new Graph(dataThisMonth, false, true, true, true, true, true, false, true);
            monthGraph.addData(dataLastMonth);
            //monthGraph.addData(dataGoalMonth);
            itemList.add(monthGraph);

            //GraphData dataGoalYear = new GraphData(Reader.get(a).yearIntegralDistanceGoal(LocalDate.now()),
            //    GraphData.GRAPH_LINE, false, true);
            //dataGoalYear.setPaint("#003E3F43", "#FF252528");
            GraphData dataThisYear = new GraphData(
                Reader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                GraphData.GRAPH_LINE, false, false);
            GraphData dataLastYear = new GraphData(
                Reader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)),
                GraphData.GRAPH_LINE, false, false);
            dataLastYear.setPaint(R.attr.colorOnBackground, a);

            Graph yearGraph = new Graph(dataThisYear, false, true, true, true, true, true, false, true);
            yearGraph.addData(dataLastYear);
            //yearGraph.addData(dataGoalYear);
            itemList.add(yearGraph);

            //GraphData dataGoalYear2 = new GraphData(Reader.get(a).yearMonthlyDistanceGoal(), GraphData.GRAPH_BAR, false,
            //    false);
            GraphData dataThisYear2 = new GraphData(
                Reader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_BAR,
                false, false);
            //dataGoalYear2.setPaint("#FF3E3F43", "#FF252528");

            Graph yearGraph2 = new Graph(dataThisYear2, false, true, true, true, true, true, false, true);
            //yearGraph2.addData(dataGoalYear2);
            itemList.add(yearGraph2);

            return itemList;
        }

        @Override
        protected void getAdapter() {
            adapter = new RoutesRecyclerAdapter(items, a);
        }

        @Override
        protected void getPrefs() {
        }

        @Override
        protected void setPrefs() {
        }

        // implements RecyclerAdapter

        @Override
        public void onItemClick(View view, int position, int itemType) {
        }

    }

}