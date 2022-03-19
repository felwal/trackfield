package me.felwal.trackfield.ui.main.statistics;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.widget.graph.Borders;
import me.felwal.trackfield.ui.widget.graph.Graph;
import me.felwal.trackfield.ui.widget.graph.GraphData;

import java.time.LocalDate;
import java.util.ArrayList;

public class StatisticsRecyclerFragment extends RecyclerFragment {

    // extends Fragment

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_main_statistics, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends DelegateClickListener

    @Override
    protected void setEmptyPage() {
        // Statistics currently never displays an empty page
    }

    @Override
    protected void setSorter() {
        // Statistics currently has no need for sorting
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new StatisticsDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        // month graph

        GraphData dataThisMonth = new GraphData(
            a, DbReader.get(a).getMonthDailyIntegralDistance(Prefs.getMainFilter(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);

        GraphData dataLastMonth = new GraphData(
            a, DbReader.get(a).getMonthDailyIntegralDistance(Prefs.getMainFilter(), LocalDate.now().minusMonths(1)),
            GraphData.GRAPH_LINE, false, false);
        dataLastMonth.setPaint(android.R.attr.listDivider, a);

        Graph monthGraph = new Graph(false, Borders.all(), true, false, true);
        monthGraph.addData(dataThisMonth, dataLastMonth);
        monthGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(monthGraph);

        // year graph

        GraphData dataThisYear = new GraphData(
            a, DbReader.get(a).getYearWeeklyIntegralDistance(Prefs.getMainFilter(), LocalDate.now()),
            GraphData.GRAPH_LINE, false, false);

        GraphData dataLastYear = new GraphData(
            a, DbReader.get(a).getYearWeeklyIntegralDistance(Prefs.getMainFilter(), LocalDate.now().minusYears(1)),
            GraphData.GRAPH_LINE, false, false);
        dataLastYear.setPaint(android.R.attr.listDivider, a);

        Graph yearGraph = new Graph(false, Borders.all(), true, false, true);
        yearGraph.addData(dataThisYear, dataLastYear);
        yearGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(yearGraph);

        // year chart

        GraphData dataThisYearMonthly = new GraphData(
            a, DbReader.get(a).getYearMonthlyDistance(Prefs.getMainFilter(), LocalDate.now()), GraphData.GRAPH_BAR,
            false, false);

        GraphData dataLastYearMonthly = new GraphData(
            a, DbReader.get(a).getYearMonthlyDistance(Prefs.getMainFilter(), LocalDate.now().minusYears(1)),
            GraphData.GRAPH_BAR, false, false);
        dataLastYearMonthly.setPaint(android.R.attr.listDivider, a);

        Graph yearBar = new Graph(false, Borders.all(), true, false, true);
        yearBar.addData(dataThisYearMonthly, dataLastYearMonthly);
        yearBar.setTag(RecyclerItem.TAG_GRAPH_BASE);
        itemList.add(yearBar);

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        // Statistics currently has no need for sorting
    }

    // implements RecyclerAdapter

    @Override
    public void onDelegateClick(View view, int position) {
        // Statistics currently has no delegates supporting clicks
    }

}
