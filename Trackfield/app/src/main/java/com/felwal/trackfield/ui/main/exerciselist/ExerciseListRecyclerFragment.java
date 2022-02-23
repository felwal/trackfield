package com.felwal.trackfield.ui.main.exerciselist;

import android.view.ScaleGestureDetector;
import android.view.View;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.Header;
import com.felwal.trackfield.ui.common.model.HeaderValue;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import com.felwal.trackfield.ui.widget.graph.Borders;
import com.felwal.trackfield.ui.widget.graph.Graph;
import com.felwal.trackfield.ui.widget.graph.GraphData;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.LayoutUtils;

import java.time.LocalDate;
import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;

public class ExerciseListRecyclerFragment extends RecyclerFragment {

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByDistance(),
        SorterItem.sortByTime(),
        SorterItem.sortByPace()
    );

    private String search = "";

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        if (search.equals("")) {
            emptyTitle.setText(R.string.tv_text_empty_exerciselist_title);
            emptyMessage.setText(R.string.tv_text_empty_exerciselist_message);
            emptyImage.setImageDrawable(ResourcesKt.withTint(ResourcesKt.getDrawableCompat(a, R.drawable.ic_run),
                ResourcesKt.getColorByAttr(a, R.attr.colorSecondary)));
        }
        else {
            emptyTitle.setText(R.string.tv_text_empty_search_title);
            emptyMessage.setText(R.string.tv_text_empty_search_msg);
            emptyImage.setImageDrawable(ResourcesKt.withTint(ResourcesKt.getDrawableCompat(a, R.drawable.ic_search),
                ResourcesKt.getColorByAttr(a, android.R.attr.textColorPrimary)));
        }
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.EXERCISE_LIST),
            Prefs.getSorterInversion(AppConsts.Layout.EXERCISE_LIST));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new ExerciseListDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesBySearch(search, sorter.getMode(), sorter.getAscending());

        // sorter & charts
        if (exerliteList.size() != 0) {
            if (Prefs.isDailyChartShown()) {
                GraphData weekData = new GraphData(
                    a, DbReader.get(a).getWeekDailyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                    GraphData.GRAPH_BAR, false, false);

                Graph weekGraph = new Graph(false, Borders.none(), true, false, true);
                weekGraph.addData(weekData);
                weekGraph.setTag(RecyclerItem.TAG_GRAPH_WEEK);
                itemList.add(weekGraph);
            }
            else {
                itemList.add(sorter.copy());
            }

            if (sorter.getMode() != SorterItem.Mode.DATE) {
                itemList.addAll(exerliteList);
                return itemList;
            }
            else fadeOutEmpty();
        }
        else fadeInEmpty();

        // headers

        Header yearHeader = new Header("", Header.Type.YEAR, itemList.size() + 1);
        Header monthHeader = new Header("", Header.Type.MONTH, itemList.size() + 1);
        Header weekHeader = new Header("", Header.Type.WEEK, itemList.size() + 1);
        int year = -1;
        int month = -1;
        int week = -1;
        int newYear, newMonth, newWeek;
        boolean notCurrentYear = false;
        LocalDate lastDate = LocalDate.MIN;

        for (Exerlite e : exerliteList) {
            // add new year header
            if ((newYear = e.getDate().getYear()) != year) {
                // conclude last headers
                // concluding month header here makes sure it doesnt collapse the year header that comes before
                // the next month header -- "January 2021" header should not collapse "2021" header
                monthHeader.setLastIndex(itemList.size() - 1);
                yearHeader.setLastIndex(itemList.size() - 1);

                // create and add new header
                yearHeader = new Header(newYear + "", Header.Type.YEAR, itemList.size() + 1,
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(yearHeader);

                year = newYear;
                notCurrentYear = year != LocalDate.now().getYear();
            }

            // add new month header
            if ((newMonth = e.getMonthValue()) != month || e.getDate().getYear() != lastDate.getYear()) {
                // conclude last header
                String title = e.getMonth();
                if (notCurrentYear) {
                    // "August 2020" if the year is not 2020, otherwise "August"
                    title += " " + year;
                }
                monthHeader.setLastIndex(itemList.size() - 1);

                // create and add new header
                monthHeader = new Header(title, Header.Type.MONTH, itemList.size() + 1,
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(monthHeader);

                month = newMonth;
            }

            // add new week header
            if (Prefs.isWeekHeadersShown() &&
                (((newWeek = e.getWeek()) != week) || Math.abs(e.getDate().toEpochDay() - lastDate.toEpochDay()) > 7)) {
                // conclude last header
                weekHeader.setLastIndex(itemList.size() - 1);

                // create and add new header
                weekHeader = new Header("" + newWeek, Header.Type.WEEK, itemList.size() + 1,
                    new HeaderValue("km", 0),
                    new HeaderValue("h", 1),
                    new HeaderValue("", 0));
                itemList.add(weekHeader);

                week = newWeek;
            }

            lastDate = e.getDate();

            // add values
            yearHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            monthHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            weekHeader.addValues(e.getDistance() / 1000f, e.getTime() / 3600f, 1);
            itemList.add(e);
        }

        // set last index for last headers
        monthHeader.setLastIndex(itemList.size() - 1);
        yearHeader.setLastIndex(itemList.size() - 1);
        weekHeader.setLastIndex(itemList.size() - 1);

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.EXERCISE_LIST, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    //

    public void updateSearch(String search) {
        this.search = search;
        if (isAdded()) setEmptyPage();
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            ExerciseDetailActivity.startActivity(a, ((Exerlite) item).getId());
        }
        else if (item instanceof Header) {
            Header header = (Header) item;
            if (header.isType(Header.Type.WEEK)) return;
            header.invertExpanded();

            // animate children
            for (int childPos = header.getFirstIndex(); childPos <= header.getLastIndex(); childPos++) {
                RecyclerItem childItem = getItemOfAll(childPos);
                childItem.changeVisibility(header.areChildrenExpanded());
            }
            updateRecycler(getVisibleItems());

            // animate header
            int collapsedHeight = header.isType(Header.Type.MONTH)
                ? (int) getResources().getDimension(R.dimen.layout_recycler_header_month_collapsed)
                : (int) getResources().getDimension(R.dimen.layout_recycler_header_year_collapsed);
            int expandedHeight = header.isType(Header.Type.MONTH)
                ? (int) getResources().getDimension(R.dimen.layout_recycler_header_month)
                : (int) getResources().getDimension(R.dimen.layout_recycler_header_year);
            View itemView = manager.findViewByPosition(position);
            LayoutUtils.animateHeight(itemView, collapsedHeight, expandedHeight, !header.areChildrenExpanded());
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
                    a, DbReader.get(a).getYearMonthlyDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()),
                    GraphData.GRAPH_BAR, false, false);

                Graph yearGraph = new Graph(false, Borders.none(), true, false, true);
                yearGraph.addData(yearData);
                yearGraph.setTag(RecyclerItem.TAG_GRAPH_BASE);
                newItems.add(position + 1, yearGraph);

                updateRecycler(newItems);
            }
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // expand and collapse headers by zooming in and out

        float scale = detector.getScaleFactor();

        if (scale < 1) {
            collapseAll();
            // consider this event as handled
            return true;
        }
        else if (scale > 1) {
            expandAll();
            // consider this event as handled
            return true;
        }

        // don't consider this event as handled
        return false;
    }

}
