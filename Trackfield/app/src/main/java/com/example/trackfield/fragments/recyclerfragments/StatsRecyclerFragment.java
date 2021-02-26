package com.example.trackfield.fragments.recyclerfragments;

import com.example.trackfield.database.Reader;
import com.example.trackfield.graphing.Graph;
import com.example.trackfield.graphing.GraphData;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.toolbox.Prefs;

import java.time.LocalDate;
import java.util.ArrayList;

public class StatsRecyclerFragment extends RoutesRecyclerFragment {

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {

        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        GraphData dataGoal = new GraphData(Reader.get(a).monthIntegralDistanceGoal(LocalDate.now()), GraphData.GRAPH_LINE, false, true);
        dataGoal.setPaint("#003E3F43", "#FF252528");
        GraphData dataNow = new GraphData(Reader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_LINE, false, false);
        GraphData dataLastMonth = new GraphData(Reader.get(a).monthDailyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusMonths(1)), GraphData.GRAPH_LINE, false, false);
        dataLastMonth.setPaint("#FF3E3F43", "#FF252528");

        Graph monthGraph = new Graph(dataNow, false, true, true, true, true, true, false, true);
        monthGraph.addData(dataLastMonth);
        monthGraph.addData(dataGoal);
        itemList.add(monthGraph);

        GraphData dataGoalYear = new GraphData(Reader.get(a).yearIntegralDistanceGoal(LocalDate.now()), GraphData.GRAPH_LINE, false, true);
        dataGoalYear.setPaint("#003E3F43", "#FF252528");
        GraphData dataNowYear = new GraphData(Reader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_LINE, false, false);
        GraphData dataLastYear = new GraphData(Reader.get(a).yearWeeklyIntegralDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)), GraphData.GRAPH_LINE, false, false);
        dataLastYear.setPaint("#FF3E3F43", "#FF252528");

        Graph yearGraph = new Graph(dataNowYear, false, true, true, true, true, true, false, true);
        yearGraph.addData(dataLastYear);
        yearGraph.addData(dataGoalYear);
        itemList.add(yearGraph);

        GraphData dataNowYear2 = new GraphData(Reader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_BAR, false, false);
        GraphData dataGoalYear2 = new GraphData(Reader.get(a).yearMonthlyDistanceGoal(), GraphData.GRAPH_BAR, false, false);
        dataGoalYear2.setPaint("#FF3E3F43", "#FF252528");

        Graph yearGraph2 = new Graph(dataNowYear2, false, true, true, true, true, true, false, true);
        yearGraph2.addData(dataGoalYear2);
        itemList.add(yearGraph2);

        return itemList;
    }

}
