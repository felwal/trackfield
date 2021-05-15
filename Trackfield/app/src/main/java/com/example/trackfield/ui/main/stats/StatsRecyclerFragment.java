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
import com.example.trackfield.ui.custom.graph.Borders;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;

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
    protected void setSorter() {
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        // month graph

        GraphData dataThisMonth = new GraphData(
            DbReader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);

        GraphData dataLastMonth = new GraphData(
            DbReader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusMonths(1)),
            GraphData.GRAPH_LINE, false, false);
        dataLastMonth.setPaint(R.attr.colorOnBackground, a);

        Graph monthGraph = new Graph(false, Borders.all(), true, false, true);
        monthGraph.addData(dataThisMonth, dataLastMonth);
        monthGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(monthGraph);

        // year graph

        GraphData dataThisYear = new GraphData(
            DbReader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);

        GraphData dataLastYear = new GraphData(
            DbReader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)),
            GraphData.GRAPH_LINE, false, false);
        dataLastYear.setPaint(R.attr.colorOnBackground, a);

        Graph yearGraph = new Graph(false, Borders.all(), true, false, true);
        yearGraph.addData(dataThisYear, dataLastYear);
        yearGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(yearGraph);

        // year chart

        GraphData dataThisYearMonthly = new GraphData(
            DbReader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_BAR,
            false, false);

        GraphData dataLastYearMonthly = new GraphData(
            DbReader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)),
            GraphData.GRAPH_BAR, false, false);
        dataLastYearMonthly.setPaint(R.attr.colorOnBackground, a);

        Graph yearBar = new Graph(false, Borders.all(), true, false, true);
        yearBar.addData(dataThisYearMonthly, dataLastYearMonthly);
        yearBar.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(yearBar);

        return itemList;
    }

    @Override
    protected void setAdapter() {
        adapter = new StatsAdapter(a, this, items);
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
    }

    // implements RecyclerAdapter

    @Override
    public void onDelegateClick(View view, int position) {
    }

}
