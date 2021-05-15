package com.example.trackfield.ui.main.exercises;

import android.view.View;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.ui.common.model.Header;
import com.example.trackfield.ui.common.model.HeaderValue;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.custom.graph.Borders;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphData;
import com.example.trackfield.ui.exercise.ViewActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.model.SortMode;

import java.time.LocalDate;
import java.util.ArrayList;

public class ExercisesRecyclerFragment extends RecyclerFragment {

    private final Sorter sorter = new Sorter(
        new SortMode("Date", SortMode.Mode.DATE, false),
        new SortMode("Distance", SortMode.Mode.DISTANCE, false),
        new SortMode("Time", SortMode.Mode.TIME, false),
        new SortMode("Pace", SortMode.Mode.PACE, true)
    );

    private String search = "";

    // extends RecyclerFragment

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        //DbReader.get(a); // TODO: varför är den här?
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesBySearch(search, sorter.getMode(), sorter.isAscending());

        // sorter & charts
        if (exerliteList.size() != 0) {
            if (Prefs.isDailyChartShown()) {
                //TreeMap<Float, Float> nodes = Reader.get(a).aggregateDistance(Prefs.getExerciseVisibleTypes(),
                //    M.atStartOfWeek(LocalDate.now()).toLocalDate(), 7, ChronoUnit.DAYS);

                GraphData weekData = new GraphData(
                    DbReader.get(a).weekDailyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                    GraphData.GRAPH_BAR, false, false);

                Graph weekGraph = new Graph(false, Borders.none(), true, false, true);
                weekGraph.addData(weekData);
                weekGraph.setTag(RecyclerItem.TAG_GRAPH_WEEK);
                itemList.add(weekGraph);
            }
            else {
                itemList.add(sorter.copy());
            }

            if (sorter.getMode() != SortMode.Mode.DATE) {
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
        LocalDate lastDate = LocalDate.MIN;

        for (Exerlite e : exerliteList) {
            // add new year header
            if ((newYear = e.getDate().getYear()) != year) {
                monthHeader.setLastIndex(itemList.size());
                yearHeader.setLastIndex(itemList.size());
                yearHeader = new Header(newYear + "", Header.Type.YEAR, itemList.size(),
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(yearHeader);
                yearOfLast = year;
                year = newYear;
                notCurrentYear = year != LocalDate.now().getYear();
            }
            // add new month header
            if ((newMonth = e.getMonthValue()) != month || e.getDate().getYear() != yearOfLast) {
                String title = e.getMonth();
                if (notCurrentYear) {
                    title += " " + year;
                }
                monthHeader.setLastIndex(itemList.size());
                monthHeader = new Header(title, Header.Type.MONTH, itemList.size(),
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(monthHeader);
                month = newMonth;
            }
            // add new week header
            if (Prefs.isWeekHeadersShown() &&
                (((newWeek = e.getWeek()) != week) || Math.abs(e.getDate().toEpochDay() - lastDate.toEpochDay()) > 7)) {
                weekHeader.setLastIndex(itemList.size());
                weekHeader = new Header("" + newWeek, Header.Type.WEEK, itemList.size(),
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(weekHeader);
                week = newWeek;
            }
            yearOfLast = year;
            lastDate = e.getDate();

            // add values
            yearHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            monthHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            weekHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            itemList.add(e);
        }

        // set last index for last headers
        monthHeader.setLastIndex(itemList.size());
        yearHeader.setLastIndex(itemList.size());
        weekHeader.setLastIndex(itemList.size());

        return itemList;
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.EXERCISES),
            Prefs.getSorterInversion(AppConsts.Layout.EXERCISES));
    }

    @Override
    protected void setAdapter() {
        adapter = new ExercisesAdapter(a, this, items);
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
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.EXERCISES, sorter.getSelectedIndex(), sorter.isOrderInverted());
        updateRecycler();
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

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            ViewActivity.startActivity(a, ((Exerlite) item).get_id());
        }
        else if (item instanceof Header) {
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
            LayoutUtils.animateHeight(itemView, collapsedHeight, expandedHeight, !header.areChildrenExpanded());
            //L.animateColor(itemView, 000000, a.getResources().getColor(R.color.colorGrey2), !header.isExpanded());
        }

        super.onDelegateClick(item);
    }

    @Override
    public void onDelegateLongClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Header) {
            Header header = (Header) item;

            // TODO: header longclick
            if (false && header.isType(Header.Type.YEAR)) {
                ArrayList<RecyclerItem> newItems = new ArrayList<>(items);

                GraphData yearData = new GraphData(
                    DbReader.get(a).yearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                    GraphData.GRAPH_BAR, false, false);

                Graph yearGraph = new Graph(false, Borders.none(), true, false, true);
                yearGraph.addData(yearData);
                yearGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
                newItems.add(position + 1, yearGraph);

                updateRecycler(newItems);
            }
        }
    }

}
