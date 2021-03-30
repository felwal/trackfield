package com.example.trackfield.ui.main.exercises;

import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.ui.main.RecyclerFragment;
import com.example.trackfield.ui.main.exercises.exercise.ViewActivity;
import com.example.trackfield.ui.main.ExercisesRecyclerAdapter;
import com.example.trackfield.ui.main.RecyclerAdapter;
import com.example.trackfield.service.database.Reader;
import com.example.trackfield.view.graphs.Graph;
import com.example.trackfield.view.graphs.GraphData;
import com.example.trackfield.ui.main.recs.general.items.Header;
import com.example.trackfield.ui.main.recs.general.items.RecyclerItem;
import com.example.trackfield.ui.main.recs.general.items.Sorter;
import com.example.trackfield.service.toolbox.C;
import com.example.trackfield.service.toolbox.L;
import com.example.trackfield.service.file.Prefs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeMap;

public class ExercisesRecyclerFragment extends RecyclerFragment {

    private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
    private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.DISTANCE, C.SortMode.TIME, C.SortMode.PACE };
    private final boolean[] smallestFirsts = { false, false, false, true };

    private String search = "";

    ////

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        Reader.get(a);
        ArrayList<Exerlite> exerliteList = reader.getExerlitesBySearch(search, sortMode, smallestFirst);
        ArrayList<RecyclerItem> itemList = new ArrayList<>();

        // sorter & charts
        if (exerliteList.size() != 0) {
            Sorter sorter = getNewSorter(sortModes, sortModesTitle);

            if (Prefs.isDailyChartShown()) {
                /*dailyChart = new ChartOld(D.weekDailyDistance());
                dailyChart.setType(ChartOld.TYPE_DAILY);
                itemList.add(dailyChart);*/

                //TreeMap<Float, Float> nodes = Reader.get(a).aggregateDistance(Prefs.getExerciseVisibleTypes(),
                //    M.atStartOfWeek(LocalDate.now()).toLocalDate(), 7, ChronoUnit.DAYS);

                TreeMap<Float, Float> nodes = Reader.get(a).weekDailyDistance(Prefs.getExerciseVisibleTypes(),
                    LocalDate.now());

                GraphData weekData = new GraphData(nodes, GraphData.GRAPH_BAR, false, false);

                Graph weekGraph = new Graph(weekData, false, false, false, false, false, true, false, true);
                weekGraph.setTag(RecyclerItem.TAG_GRAPH_WEEK);
                itemList.add(weekGraph);
            }
            else itemList.add(sorter);
            if (Prefs.isWeekChartShown()) {
                //if (D.weekDistance) { itemList.add(new Chart(D.weekDistances, D.weeks)); }
                //else { itemList.add(new Chart(D.weekActivities, D.weeks)); }
            }
            if (sortMode != C.SortMode.DATE) {
                itemList.addAll(exerliteList);
                return itemList;
            }
            else fadeOutEmpty();
        }
        else fadeInEmpty();

        // headers
        Header yearHeader = new Header("", Header.Type.YEAR, itemList.size());
        Header monthHeader = new Header("", Header.Type.MONTH, itemList.size());
        Header weekHeader = new Header("", Header.Type.WEEK, itemList.size());
        int year = -1;
        int month = -1;
        int week = -1;
        int newYear, newMonth, newWeek;
        int yearOfLast = -1;
        boolean notCurrentYear = false;

        for (Exerlite e : exerliteList) {
            // year
            if ((newYear = e.getDate().getYear()) != year) {
                monthHeader.setLastIndex(itemList.size());
                yearHeader.setLastIndex(itemList.size());
                yearHeader = new Header(newYear + "", Header.Type.YEAR, itemList.size());
                itemList.add(yearHeader);
                //itemList.add(new Chart(D.yearWeeklyDistance(newYear), Chart.TYPE_YEAR));
                yearOfLast = year;
                year = newYear;
                notCurrentYear = year != LocalDate.now().getYear();
            }
            // month
            if ((newMonth = e.getMonthValue()) != month || e.getDate().getYear() != yearOfLast) {
                String title = e.getMonth();
                if (notCurrentYear) {
                    title += " " + year;
                }
                monthHeader.setLastIndex(itemList.size());
                monthHeader = new Header(title, Header.Type.MONTH, itemList.size());
                itemList.add(monthHeader);
                month = newMonth;
            }
            // week
            if ((((newWeek = e.getWeek()) != week) || e.getDate().getYear() != yearOfLast) &&
                Prefs.isWeekHeadersShown()) {
                weekHeader.setLastIndex(itemList.size());
                weekHeader = new Header("" + newWeek, Header.Type.WEEK, itemList.size());
                itemList.add(weekHeader);
                week = newWeek;
            }
            yearOfLast = year;

            // add values
            yearHeader.addValue(e.getDistance());
            monthHeader.addValue(e.getDistance());
            weekHeader.addValue((int) e.getTime());
            itemList.add(e);
        }

        // set last index for last headers
        monthHeader.setLastIndex(itemList.size());
        yearHeader.setLastIndex(itemList.size());
        weekHeader.setLastIndex(itemList.size());

        return itemList;
    }

    @Override
    protected void setSortModes() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISES);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISES);
    }

    @Override
    protected void getAdapter() {
        adapter = new ExercisesRecyclerAdapter(items, a);
    }

    @Override
    protected void getPrefs() {
        sortMode = Prefs.getSortModePref(C.Layout.EXERCISES);
        smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISES);
    }

    @Override
    protected void setPrefs() {
        Prefs.setSortModePref(C.Layout.EXERCISES, sortMode);
        Prefs.setSmallestFirstPref(C.Layout.EXERCISES, smallestFirst);
    }

    @Override
    protected void setEmptyPage() {

        if (search.equals("")) {
            emptyTitle.setText(R.string.empty_title_exercises);
            emptyMessage.setText(R.string.empty_message_exercises);
            emptyImage.setImageResource(R.drawable.ic_empty_exercise_24dp);
        }
        else {
            emptyTitle.setText(R.string.empty_title_search);
            emptyMessage.setText(R.string.empty_message_search);
            emptyImage.setImageResource(R.drawable.ic_empty_search_24dp);
        }
    }

    @Override
    public void onItemLongClick(View view, int position, int itemType) {
        RecyclerItem item = adapter.getItem(position);

        if (item instanceof Header) {
            Header header = (Header) item;

            if (false && header.isType(Header.Type.YEAR)) {
                ArrayList<RecyclerItem> newItems = new ArrayList<>(items);

                GraphData yearData = new GraphData(
                    Reader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                    GraphData.GRAPH_BAR, false, false);
                Graph yearGraph = new Graph(yearData, false, false, false, false, false, true, false, true);
                yearGraph.setTag(RecyclerItem.TAG_GRAPH_YEAR);
                newItems.add(position + 1, yearGraph);

                updateRecycler(newItems);
            }
            else if (header.isType(Header.Type.MONTH)) {

            }
        }
    }

    //

    public void updateSearch(String search) {
        this.search = search;
        if (isAdded()) setEmptyPage();
        updateRecycler();
    }

    private RecyclerItem getItemOfAll(int pos) {
        return allItems.get(pos);
    }

    // implements RecyclerAdapter

    @Override
    public void onItemClick(View view, int position, int itemType) {
        RecyclerItem item;

        if (itemType == RecyclerAdapter.ITEM_ITEM) {
            ViewActivity.startActivity(a, ((Exerlite) items.get(position)).get_id());
        }
        else if ((item = adapter.getItem(position)) instanceof Header) {

            Header header = (Header) item;
            if (header.isType(Header.Type.WEEK)) return;
            header.invertExpanded();

            // animate children
            for (int childPos = header.getFirstIndex(); childPos <= header.getLastIndex(); childPos++) {
                RecyclerItem childItem = getItemOfAll(childPos);
                childItem.changeVisibility(header.areChildrenExpanded());
                //L.expandOrCollapseHeight(manager.findViewByPosition(childPos), M.px(62), childItem.isVisible());

                //adapter.notifyItemChanged(childPos);
            }
            updateRecycler(getVisibleItems());
            //adapter.notifyItemRangeChanged(header.getFirstIndex(), header.getChildItemCount());
            //adapter.notifyDataSetChanged();

            // animate header
            int collapsedHeight = header.isType(Header.Type.MONTH) ?
                (int) getResources().getDimension(R.dimen.layout_header_month_collapsed) :
                (int) getResources().getDimension(R.dimen.layout_header_year_collapsed);
            int expandedHeight =
                header.isType(Header.Type.MONTH) ? (int) getResources().getDimension(R.dimen.layout_header_month) :
                    (int) getResources().getDimension(R.dimen.layout_header_year);
            View itemView = manager.findViewByPosition(position);
            L.animateHeight(itemView, collapsedHeight, expandedHeight, !header.areChildrenExpanded());
            //L.animateColor(itemView, 000000, a.getResources().getColor(R.color.colorGrey2), !header.isExpanded());

        }
        super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
    }

}