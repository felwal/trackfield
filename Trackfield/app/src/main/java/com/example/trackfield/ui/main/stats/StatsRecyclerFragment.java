package com.example.trackfield.ui.main.stats;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.main.recs.routes.RoutesAdapter;

import java.time.LocalDate;
import java.util.ArrayList;

public class StatsRecyclerFragment extends RecyclerFragment {

    // extends Fragment

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_stats, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends DelegateClickListener

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
            DbReader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);
        GraphData dataLastMonth = new GraphData(
            DbReader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusMonths(1)),
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
            DbReader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);
        GraphData dataLastYear = new GraphData(
            DbReader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)),
            GraphData.GRAPH_LINE, false, false);
        dataLastYear.setPaint(R.attr.colorOnBackground, a);

        Graph yearGraph = new Graph(dataThisYear, false, true, true, true, true, true, false, true);
        yearGraph.addData(dataLastYear);
        //yearGraph.addData(dataGoalYear);
        itemList.add(yearGraph);

        //GraphData dataGoalYear2 = new GraphData(Reader.get(a).yearMonthlyDistanceGoal(), GraphData.GRAPH_BAR, false,
        //    false);
        GraphData dataThisYear2 = new GraphData(
            DbReader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_BAR,
            false, false);
        //dataGoalYear2.setPaint("#FF3E3F43", "#FF252528");

        Graph yearGraph2 = new Graph(dataThisYear2, false, true, true, true, true, true, false, true);
        //yearGraph2.addData(dataGoalYear2);
        itemList.add(yearGraph2);

        return itemList;
    }

    @Override
    protected void getAdapter() {
        adapter = new RoutesAdapter(a, this, items);
    }

    @Override
    protected void getPrefs() {
    }

    @Override
    protected void setPrefs() {
    }

    // implements RecyclerAdapter

    @Override
    public void onDelegateClick(View view, int position) {
    }

}
