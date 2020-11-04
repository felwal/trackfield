package com.example.trackfield.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.activities.MainActivity;
import com.example.trackfield.activities.RecActivity;
import com.example.trackfield.activities.ViewActivity;
import com.example.trackfield.adapters.RecyclerAdapters;
import com.example.trackfield.database.Helper;
import com.example.trackfield.fragments.dialogs.SortDialog;
import com.example.trackfield.items.distinct.Chart;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.distinct.Goal;
import com.example.trackfield.items.distinct.Graph;
import com.example.trackfield.items.distinct.Header;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.items.distinct.RecyclerItem;
import com.example.trackfield.objects.Route;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.items.distinct.Sorter;
import com.example.trackfield.toolbox.Toolbox.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class RecyclerFragments {

    public static abstract class Base extends Fragment implements RecyclerAdapters.Base.ItemClickListener, SortDialog.DismissListener {

        protected Activity a;
        protected Helper.Reader reader;
        protected RecyclerView recycler;
        protected RecyclerView.LayoutManager manager;
        protected RecyclerAdapters.Base adapter;

        protected ConstraintLayout emptyCl;
        protected TextView emptyTitle, emptyMessage;
        protected ImageView emptyImage;

        protected ArrayList<RecyclerItem> allItems = new ArrayList<>();
        protected ArrayList<RecyclerItem> items = new ArrayList<>();
        protected C.SortMode sortMode = C.SortMode.DATE;
        protected boolean smallestFirst = false;

        ////

        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
        @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_recycler, container, false);

            a = getActivity();
            //if (a instanceof MainActivity)          reader = ((MainActivity) a)     .getReader() != null ? ((MainActivity) a).getReader() : new Helper.Reader(a);
            //else if (a instanceof RecActivity.Base) reader = ((RecActivity.Base) a) .getReader() != null ? ((MainActivity) a).getReader() : new Helper.Reader(a);
            //else reader = new Helper.Reader(a);
            reader = Helper.getReader(a);


            recycler = view.findViewById(R.id.recyclerView);
            recycler.setHasFixedSize(true);
            manager = new LinearLayoutManager(a);
            recycler.setLayoutManager(manager);

            emptyCl = view.findViewById(R.id.constraintLayout_empty);
            emptyTitle = emptyCl.findViewById(R.id.textView_emptyTitle);
            emptyMessage = emptyCl.findViewById(R.id.textView_emptyMessage);
            emptyImage = emptyCl.findViewById(R.id.imageView_emptyImage);
            emptyCl.setVisibility(View.GONE);
            setEmptyPage();

            //((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

            setSortModes();
            if (adapter == null) {
                items.addAll(getRecyclerItems());
                allItems.addAll(items);
                getAdapter();
            }
            adapter.setClickListener(this);
            recycler.setAdapter(adapter);

            if (a instanceof MainActivity) {
                ((MainActivity) a).recyclerScrollListener(recycler, this);
            }

            return view;
        }

        // abstracts
        protected abstract void setSortModes();
        protected abstract ArrayList<RecyclerItem> getRecyclerItems();
        protected abstract void getAdapter();
        protected abstract void getPrefs();
        protected abstract void setPrefs();

        protected void setEmptyPage() {}

        // calls
        public void updateRecycler(ArrayList<RecyclerItem> newItems) {

            class RecyclerItemCallback extends DiffUtil.Callback {

                private ArrayList<RecyclerItem> oldList, newList;

                private RecyclerItemCallback(ArrayList<RecyclerItem> oldList, ArrayList<RecyclerItem> newList) {
                    this.oldList = oldList;
                    this.newList = newList;
                }

                @Override public int getOldListSize() {
                    return oldList.size();
                }
                @Override public int getNewListSize() {
                    return newList.size();
                }
                @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    RecyclerItem oldItem = oldList.get(oldItemPosition);
                    RecyclerItem newItem = newList.get(newItemPosition);
                    return oldItem.sameItemAs(newItem);
                }
                @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    RecyclerItem oldItem = oldList.get(oldItemPosition);
                    RecyclerItem newItem = newList.get(newItemPosition);
                    return oldItem.sameContentAs(newItem);
                }
            }
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new RecyclerItemCallback(items, newItems));

            items.clear();
            items.addAll(newItems);
            diff.dispatchUpdatesTo(adapter);
        }
        public void updateRecycler() {
            updateRecycler(getRecyclerItems());
        }
        public void scrollToTop() {
            recycler.smoothScrollToPosition(0);
        }

        // tools
        protected Sorter newSorter(C.SortMode[] sortModes, String[] sortModesTitle) {
            return new Sorter(sortModes, sortModesTitle, sortMode, smallestFirst);
        }
        protected String title(C.Layout layout, C.SortMode sortMode){

            switch (layout) {
                case EXERCISE: switch (sortMode) {
                    case DATE: return "Date";
                    case DISTANCE: return "Distance";
                    case TIME: return "Time";
                    case PACE: return "Pace";
                }
                case DISTANCE: switch (sortMode) {
                    case DISTANCE: return "Distance";
                    case AMOUNT: return "Amount";
                    case TIME: return "Best time";
                    case PACE: return "Best pace";
                }
                case ROUTE: switch (sortMode) {
                    case NAME: return "Name";
                    case AMOUNT: return "Amount";
                    case PACE: return "Best pace";
                    case DISTANCE: return "Avg distance";
                    case DATE: return "Recent";
                }
                case EXERCISE_DISTANCE: switch (sortMode) {
                    case DATE: return "Date";
                    case PACE: return "Pace & avg time";
                    case DISTANCE: return "Full distance";
                }
                case EXERCISE_ROUTE: switch (sortMode) {
                    case DATE: return "Date";
                    case DISTANCE: return "Distance";
                    case TIME: return "Time";
                    case PACE: return "Pace";
                }
            }

            return "???";
        }

        protected void onItemClick(int itemType, C.SortMode[] sortModes, C.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {
            if (itemType == RecyclerAdapters.Base.ITEM_SORTER) {
                onSorterClick(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
            }
        }
        protected void onSorterClick(C.SortMode[] sortModes, C.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {

            getPrefs();
            //final BottomSheetDialog sheet = new BottomSheetDialog(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst, this);
            final SortDialog sheet = SortDialog.newInstance(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
            sheet.setDismissListener(this);

            sheet.show(getChildFragmentManager(), sheet.getTag());
            getChildFragmentManager().executePendingTransactions();
        }
        protected void addHeadersAndItems(ArrayList<RecyclerItem> itemList, ArrayList<Exerlite> exerliteList) {

            if (sortMode == C.SortMode.DATE) {
                int year = -1; int newYear;
                for (Exerlite e : exerliteList) {
                    if ((newYear = e.getDate().getYear()) != year) {
                        itemList.add(new Header(newYear + "", Header.Type.REC));
                        year = newYear;
                    }
                    itemList.add(e);
                }
            }
            else //(/*sortMode == C.SortMode.PACE &&*/ /*smallestFirst*/ /*&& exerliteList.size() > 3*/)
            {
                for (int i = 0; i < exerliteList.size(); i++) {
                    if (i == 0) itemList.add(new Header("Top " + 3, Header.Type.REC));
                    else if (i == 3) itemList.add(new Header("Top " + 10, Header.Type.REC));
                    else if (i % 10 == 0) itemList.add(new Header("Top " + (i+10), Header.Type.REC));
                    itemList.add(exerliteList.get(i));
                }
            }
            //else itemList.addAll(exerliteList);
        }

        @Override public void onItemLongClick(View view, int position, int itemType) {}
        @Override public void onDismiss(C.SortMode sortMode, boolean smallestFirst) {
            this.sortMode = sortMode;
            this.smallestFirst = smallestFirst;
            setPrefs();
            updateRecycler();
        }
    }

    public static class ExerciseRF extends Base {

        private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.DISTANCE, C.SortMode.TIME, C.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, false, false, true };

        private String search = "";

        ////

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            //ArrayList<Exercise> exerciseList = searchTerm.equals("") ? D.sortExercises(D.exercises, smallestFirst, sortMode) : D.sortExercises(D.filterBySearch(searchTerm), smallestFirst, sortMode);
            ArrayList<Exerlite> exerliteList = reader.getExerlitesBySearch(search, sortMode, smallestFirst);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            Chart dailyChart = null;
            if (D.showDailyChart) {
                dailyChart = new Chart(D.weekDailyDistance());
                dailyChart.setType(Chart.TYPE_DAILY);
                itemList.add(dailyChart);
            }
            else itemList.add(sorter);
            if (D.showWeekChart) {
                //if (D.weekDistance) { itemList.add(new Chart(D.weekDistances, D.weeks)); }
                //else { itemList.add(new Chart(D.weekActivities, D.weeks)); }
            }
            if (sortMode != C.SortMode.DATE) {
                itemList.addAll(exerliteList);
                return itemList;
            }

            // headers
            Header yearHeader = new Header("", Header.Type.YEAR, itemList.size());
            Header monthHeader = new Header("", Header.Type.MONTH, itemList.size());
            Header weekHeader = new Header("", Header.Type.WEEK, itemList.size());
            int year = -1; int month = -1; int week = -1;
            int newYear, newMonth, newWeek;
            boolean oldYear = false;
            for (Exerlite e : exerliteList) {
                // year
                if ((newYear = e.getDate().getYear()) != year) {
                    monthHeader.setLastIndex(itemList.size());
                    yearHeader.setLastIndex(itemList.size());
                    yearHeader = new Header(newYear + "", Header.Type.YEAR, itemList.size());
                    itemList.add(yearHeader);
                    //itemList.add(new Chart(D.yearWeeklyDistance(newYear), Chart.TYPE_YEAR));
                    year = newYear;
                    oldYear = year != LocalDate.now().getYear();
                }
                // month
                if ((newMonth = e.getMonthValue()) != month) {
                    String title = e.getMonth();
                    if (oldYear) {
                        title += " " + year;
                    }
                    monthHeader.setLastIndex(itemList.size());
                    monthHeader = new Header(title, Header.Type.MONTH, itemList.size());
                    itemList.add(monthHeader);
                    month = newMonth;
                }
                // week
                if (D.showWeekHeaders && (newWeek = e.getWeek()) != week) {
                    weekHeader.setLastIndex(itemList.size());
                    weekHeader = new Header("" + newWeek, Header.Type.WEEK, itemList.size());
                    itemList.add(weekHeader);
                    week = newWeek;
                }

                yearHeader.addValue(e.getDistance());
                monthHeader.addValue(e.getDistance());
                weekHeader.addValue((int) e.getTime());
                itemList.add(e);
            }

            if (exerliteList.size() == 0) {
                itemList.remove(sorter);
                itemList.remove(dailyChart);
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.ExerciseRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.EXERCISE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.EXERCISE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {

            if (search.equals("")){
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

        public void updateSearch(String search) {
            this.search = search;
            if (isAdded()) setEmptyPage();
            updateRecycler();
        }
        private ArrayList<RecyclerItem> getVisibleItems() {

            ArrayList<RecyclerItem> visibleItems = new ArrayList<>();
            for (RecyclerItem item : allItems) {
                if (item.isVisible()) visibleItems.add(item);
            }
            return visibleItems;
        }
        private RecyclerItem getItemOfAll(int pos) {
            return allItems.get(pos);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            RecyclerItem item;

            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                ViewActivity.startActivity(a, ((Exerlite) items.get(position)).get_id());
            }
            else if ((item = adapter.getItem(position)) instanceof Header) {

                Header header = (Header) item;
                if (header.isType(Header.Type.WEEK)) return;
                header.invertExpanded();

                // animate children
                for (int childPos = header.getFirstIndex(); childPos <= header.getLastIndex(); childPos++) {
                    RecyclerItem childItem = getItemOfAll(childPos);
                    childItem.changeVisibility(header.isChildrenExpanded());
                    //L.expandOrCollapseHeight(manager.findViewByPosition(childPos), M.px(62), childItem.isVisible());

                    //adapter.notifyItemChanged(childPos);
                }
                updateRecycler(getVisibleItems());
                //adapter.notifyItemRangeChanged(header.getFirstIndex(), header.getChildItemCount());
                //adapter.notifyDataSetChanged();

                // animate header
                int collapsedHeight = header.isType(Header.Type.MONTH) ? (int) getResources().getDimension(R.dimen.layout_header_month_collapsed) :
                        (int) getResources().getDimension(R.dimen.layout_header_year_collapsed);
                int expandedHeight = header.isType(Header.Type.MONTH) ? (int) getResources().getDimension(R.dimen.layout_header_month) :
                        (int) getResources().getDimension(R.dimen.layout_header_year);
                View itemView = manager.findViewByPosition(position);
                L.animateHeight(itemView, collapsedHeight, expandedHeight, !header.isChildrenExpanded());
                //L.animateColor(itemView, 000000, a.getResources().getColor(R.color.colorGrey2), !header.isExpanded());

            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }
        @Override public void onItemLongClick(View view, int position, int itemType) {
            RecyclerItem item = adapter.getItem(position);

            if (item instanceof Header) {
                Header header = (Header) item;

                if (header.isType(Header.Type.YEAR)) {
                    ArrayList<RecyclerItem> newItems = new ArrayList<>(items);

                    Chart chart = new Chart(D.yearMonthlyDistance(Integer.valueOf(header.getTitle())), C.M);
                    chart.setType(Chart.TYPE_WEEKS);
                    newItems.add(position + 1, chart);

                    //Chart chart2 = new Chart(D.yearWeeklyDistance(Integer.valueOf(header.getTitle())));
                    //chart2.setType(Chart.TYPE_YEAR);
                    //newItems.add(position + 1, chart2);

                    updateRecycler(newItems);
                }
                else if (header.isType(Header.Type.MONTH)) {

                }


            }
        }

    }

    public static class DistanceRF extends Base {

        private final String[] sortModesTitle = { "Distance", "Amount", "Best time", "Best pace" };
        private final C.SortMode[] sortModes = { C.SortMode.DISTANCE, C.SortMode.AMOUNT, C.SortMode.TIME, C.SortMode.PACE };
        private final boolean[] smallestFirsts = { true, false, false, true };

        ////

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(sortMode, smallestFirst);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(distanceItemList);
            if (distanceItemList.size() == 0) {
                itemList.remove(sorter);
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.DISTANCE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.DISTANCE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.DistanceRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.DISTANCE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.DISTANCE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.DISTANCE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.DISTANCE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_distances));
            emptyMessage.setText(getString(R.string.empty_message_distances));
            emptyImage.setImageResource(R.drawable.ic_empty_distances_24dp);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                RecActivity.DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }
    public static class RouteRF extends Base {

        private final String[] sortModesTitle = { "Recent", "Name", "Amount", "Avg distance", "Best pace" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.NAME, C.SortMode.AMOUNT, C.SortMode.DISTANCE, C.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, true, false, false, true };

        ////

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            //ArrayList<String> rList = D.sortRoutes(D.routes, smallestFirst, sortMode, false);
            ArrayList<RouteItem> routeItemList = reader.getRouteItems(sortMode, smallestFirst, D.showLesserRoutes); //reader.getRoutes(rList);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(routeItemList);
            if (routeItemList.size() == 0) {
                itemList.remove(sorter);
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.RouteRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.ROUTE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_routes));
            emptyMessage.setText(getString(R.string.empty_message_routes));
            emptyImage.setImageResource(R.drawable.ic_empty_routes_24dp);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                RecActivity.RouteActivity.startActivity(a, ((RouteItem) items.get(position)).get_id());
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }
    public static class IntervalRF extends Base {

        private final String[] sortModesTitle = { "Recent", "Amount" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.AMOUNT};
        private final boolean[] smallestFirsts = { false, false};

        ////

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sortMode, smallestFirst, D.showLesserRoutes);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(intervalItemList);
            if (intervalItemList.size() == 0) {
                itemList.remove(sorter);
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.IntervalRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.ROUTE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.ROUTE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_intervals));
            emptyMessage.setText(getString(R.string.empty_message_intervals));
            emptyImage.setImageResource(R.drawable.ic_empty_interval_24dp);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                RecActivity.IntervalActivity.startActivity(a, ((IntervalItem) items.get(position)).getInterval() );
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }

    public static class DistanceExerciseRF extends Base {

        private final String[] sortModesTitle = { "Date", "Pace & Avg time", "Full distance" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.PACE, C.SortMode.DISTANCE };
        private final boolean[] smallestFirsts = { false, true, true, true };

        private int originId;
        private int distance;

        private final static String BUNDLE_DISTANCE = "distance";
        private final static String BUNDLE_ORIGIN_ID = "originId";

        ////

        public static DistanceExerciseRF newInstance(int distance, int originId) {
            DistanceExerciseRF instance = new DistanceExerciseRF();
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_DISTANCE, distance);
            bundle.putInt(BUNDLE_ORIGIN_ID, originId);
            instance.setArguments(bundle);
            return instance;
        }
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final Bundle bundle = getArguments();
            if (bundle != null) {
                distance = bundle.getInt(BUNDLE_DISTANCE, -1);
                originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);
            }
        }

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sortMode, smallestFirst);
            ArrayList<Exerlite> chronoList = reader.getExerlitesByDistance(distance, C.SortMode.DATE, false);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();
            D.markTop(exerliteList);

            Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(graph);
            itemList.add(sorter);
            float goalPace = reader.getDistanceGoal(distance);
            Goal goal = null;
            if (goalPace != Distance.NO_GOAL_PACE) {
                goal = new Goal(goalPace, distance);
                itemList.add(goal);
            }

            addHeadersAndItems(itemList, exerliteList);

            if (exerliteList.size() == 0) {
                itemList.remove(sorter);
                itemList.remove(graph);
                if (goal != null) {
                    itemList.remove(goal);
                    ((TextView) emptyCl.findViewById(R.id.textView_emptyMessage)).setText("Goal:" + C.TAB + goal.printValues());
                }
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_DISTANCE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_DISTANCE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.DistanceExerciseRA(items, distance, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_DISTANCE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_DISTANCE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.EXERCISE_DISTANCE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.EXERCISE_DISTANCE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_distance));
            emptyMessage.setText(getString(R.string.empty_message_distance));
            emptyImage.setImageResource(R.drawable.ic_empty_distance_24dp);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_DISTANCE);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }
    public static class RouteExerciseRF extends Base {

        private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.DISTANCE, C.SortMode.TIME, C.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, false, true, true };

        private Route route;
        private int originId;

        private final static String BUNDLE_ROUTE_ID = "routeId";
        private final static String BUNDLE_ORIGIN_ID = "originId";

        ////

        public static RouteExerciseRF newInstance(int routeId, int originId) {
            RouteExerciseRF instance = new RouteExerciseRF();
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_ROUTE_ID, routeId);
            bundle.putInt(BUNDLE_ORIGIN_ID, originId);
            instance.setArguments(bundle);
            return instance;
        }
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getBundle();
        }

        private void getBundle() {
            Bundle bundle = getArguments();
            if (bundle != null) {
                //reader.close();
                //reader = new Helper.Reader(a);
                route = Helper.getReader(a).getRoute(bundle.getInt(BUNDLE_ROUTE_ID, -1));
                originId = bundle.getInt(BUNDLE_ORIGIN_ID, -1);
            }
        }

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            //ArrayList<Exercise> exerciseList = D.sortExercises(filtered, smallestFirst, sortMode);
            ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.get_id(), sortMode, smallestFirst);
            ArrayList<Exerlite> chronoList = reader.getExerlitesByRoute(route.get_id(), C.SortMode.DATE, false);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();
            D.markTop(exerliteList);

            Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(graph);
            itemList.add(sorter);
            if (route.getGoalPace() != Route.NO_GOAL_PACE) {
                Goal goal = new Goal(route.getGoalPace());
                itemList.add(goal);
            }

            addHeadersAndItems(itemList, exerliteList);

            if (exerliteList.size() == 0) {
                itemList.remove(sorter);
                itemList.remove(graph);
                L.crossfadeIn(emptyCl, 1);
            }
            else L.crossfadeOut(emptyCl);

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.RouteExerciseRA(items, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()] = smallestFirst;
        }
        @Override protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_route));
            emptyMessage.setText(getString(R.string.empty_message_route));
            emptyImage.setImageResource(R.drawable.ic_empty_route_24dp);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_ROUTE);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }
    public static class IntervalExerciseRF extends Base {

        private final String[] sortModesTitle = { "Date", "Distance", "Time", "Pace" };
        private final C.SortMode[] sortModes = { C.SortMode.DATE, C.SortMode.DISTANCE, C.SortMode.TIME, C.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, false, true, true };

        private String interval;
        private int originId;

        private final static String BUNDLE_INTERVAL = "interval";
        private final static String BUNDLE_ORIGINID = "originId";

        ////

        public static IntervalExerciseRF newInstance(String interval, int originId) {
            IntervalExerciseRF instance = new IntervalExerciseRF();
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_INTERVAL, interval);
            bundle.putInt(BUNDLE_ORIGINID, originId);
            instance.setArguments(bundle);
            return instance;
        }
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null) {
                interval = bundle.getString(BUNDLE_INTERVAL, "");
                originId = bundle.getInt(BUNDLE_ORIGINID, -1);
            }
        }

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByInterval(interval, sortMode, smallestFirst);
            //ArrayList<Exerlite> chronoList = reader.getExerlitesByInterval(interval, C.SortMode.DATE, false);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            /*Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
            itemList.add(graph);*/
            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);

            addHeadersAndItems(itemList, exerliteList);
            /*if (sortMode == C.SortMode.DATE) {
                int year = -1; int newYear;
                for (Exerlite e : exerliteList) {
                    if ((newYear = e.getDate().getYear()) != year) {
                        itemList.add(new Header(newYear + "", Header.Type.REC));
                        year = newYear;
                    }
                    itemList.add(e);
                }
            }
            else if ((sortMode == C.SortMode.PACE || sortMode == C.SortMode.TIME) && smallestFirst && exerliteList.size() > 10) {
                for (int i = 0; i < exerliteList.size(); i++) {
                    if (i % 10 == 0) {
                        itemList.add(new Header("Top " + (i+10), Header.Type.REC));
                    }
                    itemList.add(exerliteList.get(i));
                }
            }
            else { itemList.addAll(exerliteList); }*/

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.IntervalExerciseRA(items, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
            smallestFirst = C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()];
        }
        @Override protected void setPrefs() {
            C.sortModePrefs[C.Layout.EXERCISE_ROUTE.ordinal()] = sortMode;
            C.smallestFirstPrefs[C.Layout.EXERCISE_ROUTE.ordinal()] = smallestFirst;
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_INTERVAL);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }

}
