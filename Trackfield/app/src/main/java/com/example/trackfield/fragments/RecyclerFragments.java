package com.example.trackfield.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.example.trackfield.toolbox.Prefs;
import com.example.trackfield.graphing.Graph;
import com.example.trackfield.graphing.GraphData;
import com.example.trackfield.items.headers.archive.ChartOld;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.Goal;
import com.example.trackfield.items.headers.Header;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.objects.Route;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.Toolbox.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class RecyclerFragments {

    public static abstract class Base extends Fragment implements RecyclerAdapters.Base.ItemClickListener, SortDialog.DismissListener {

        protected Activity a;
        protected static Thread bgThread;
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

        interface Threader {
            void run();
            default void interruptAndStart() {
                if (bgThread != null) bgThread.interrupt();
                bgThread = new Thread(() -> Threader.this.run());
                bgThread.start();
            }
        }

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

            // empty page
            emptyCl = view.findViewById(R.id.constraintLayout_empty);
            emptyTitle = emptyCl.findViewById(R.id.textView_emptyTitle);
            emptyMessage = emptyCl.findViewById(R.id.textView_emptyMessage);
            emptyImage = emptyCl.findViewById(R.id.imageView_emptyImage);
            emptyCl.setVisibility(View.GONE);
            setEmptyPage();

            //((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

            setSortModes();
            if (adapter == null) {

                ((Threader) () -> {
                    if (items.size() == 0) {
                        items.addAll(getRecyclerItems());
                        allItems.addAll(items);
                    }
                    a.runOnUiThread(() -> {
                        getAdapter();
                        adapter.setClickListener(Base.this);
                        recycler.setAdapter(adapter);
                        L.crossfadeRecycler(recycler);
                    });
                }).interruptAndStart();

                /*bgThread = new Thread(() -> {
                    items.addAll(getRecyclerItems());
                    allItems.addAll(items);
                    a.runOnUiThread(() -> {
                        getAdapter();
                        adapter.setClickListener(Base.this);
                        recycler.setAdapter(adapter);
                        L.crossfadeRecycler(recycler);
                    });
                });
                bgThread.start();*/
            }
            else {
                adapter.setClickListener(this);
                recycler.setAdapter(adapter);
            }

            if (a instanceof MainActivity) {
                ((MainActivity) a).setRecyclerScrollListener(recycler, this);
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
        protected void fadeInEmpty() {
            a.runOnUiThread(() -> L.crossfadeIn(emptyCl, 1));
        }
        protected void fadeOutEmpty() {
            a.runOnUiThread(() -> L.crossfadeOut(emptyCl));
        }

        // calls
        public void updateRecycler(final ArrayList<RecyclerItem> newItems) {

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
            final DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new RecyclerItemCallback(items, newItems));

            items.clear();
            items.addAll(newItems);
            diff.dispatchUpdatesTo(adapter);

            /*new Thread(new Runnable() {
                @Override public void run() {
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
                    final DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new RecyclerItemCallback(items, newItems));

                    items.clear();
                    items.addAll(newItems);
                    a.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            diff.dispatchUpdatesTo(adapter);
                        }
                    });
                }
            }).start();*/
        }
        public void updateRecycler() {

            ((Threader) () -> {
                final ArrayList<RecyclerItem> newItems = getRecyclerItems();
                a.runOnUiThread(() -> updateRecycler(newItems));
            }).interruptAndStart();

            /*bgThread = new Thread(() -> {
                final ArrayList<RecyclerItem> newItems = getRecyclerItems();
                a.runOnUiThread(() -> updateRecycler(newItems));
            });
            bgThread.start();*/
        }
        public void scrollToTop() {
            //recycler.scrollToPosition(25);
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

            ArrayList<Exerlite> exerliteList = reader.getExerlitesBySearch(search, sortMode, smallestFirst);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            // sorter & charts
            if (exerliteList.size() != 0) {
                Sorter sorter = newSorter(sortModes, sortModesTitle);

                ChartOld dailyChart;
                if (Prefs.isDailyChartShown()) {
                    /*dailyChart = new ChartOld(D.weekDailyDistance());
                    dailyChart.setType(ChartOld.TYPE_DAILY);
                    itemList.add(dailyChart);*/

                    GraphData weekData = new GraphData(Helper.getReader(a).weekDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_BAR, false, false);
                    Graph weekGraph = new Graph(weekData, false, false, false, false, false, true, false);
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
                if (Prefs.isWeekHeadersShown() && (newWeek = e.getWeek()) != week) {
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

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.ExerciseRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.EXERCISE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.EXERCISE, smallestFirst);
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

                    ChartOld chart = new ChartOld(D.yearMonthlyDistance(Integer.valueOf(header.getTitle())), C.M);
                    chart.setType(ChartOld.TYPE_WEEKS);
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

            ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(Distance.SortMode.DISTANCE/*sortMode*/, smallestFirst, Prefs.getExerciseVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(distanceItemList);
            if (distanceItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();



            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.DistanceRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.DISTANCE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.DISTANCE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.DISTANCE, smallestFirst);
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

            ArrayList<RouteItem> routeItemList = reader.getRouteItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown(), Prefs.getExerciseVisibleTypes()); //reader.getRoutes(rList);
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(routeItemList);
            if (routeItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.RouteRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.ROUTE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.ROUTE, smallestFirst);
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

            ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = newSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(intervalItemList);
            if (intervalItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.IntervalRA(items, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.ROUTE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.ROUTE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.ROUTE, smallestFirst);
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

                // filtering depending on origin
                Prefs.setDistanceVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : M.createList(Helper.getReader(a).getExercise(originId).getType()));
            }
        }

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByDistance(distance, sortMode, smallestFirst, Prefs.getDistanceVisibleTypes());
            ArrayList<Exerlite> chronoList = reader.getExerlitesByDistance(distance, C.SortMode.DATE, true, Prefs.getDistanceVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();
            D.markTop(exerliteList);

            if (exerliteList.size() != 0) {

                //Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
                GraphData data = new GraphData(GraphData.ofExerlites(chronoList), GraphData.GRAPH_BEZIER, false, false);
                Graph graph = new Graph(data, true, false, false, true, true, false, true);
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);

                itemList.add(newSorter(sortModes, sortModesTitle));

                float goalPace = reader.getDistanceGoal(distance);
                Goal goal = null;
                if (goalPace != Distance.NO_GOAL_PACE) {
                    goal = new Goal(goalPace, distance);
                    itemList.add(goal);
                }
                addHeadersAndItems(itemList, exerliteList);

                fadeOutEmpty();
            }
            else fadeInEmpty();


            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_DISTANCE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.DistanceExerciseRA(items, distance, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_DISTANCE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_DISTANCE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.EXERCISE_DISTANCE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.EXERCISE_DISTANCE, smallestFirst);
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

                // filtering depending on origin
                Prefs.setRouteVisibleTypes(originId == -1 ? Prefs.getExerciseVisibleTypes() : M.createList(Helper.getReader(a).getExercise(originId).getType()));
            }
        }

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<Exerlite> exerliteList = reader.getExerlitesByRoute(route.get_id(), sortMode, smallestFirst, Prefs.getRouteVisibleTypes());
            ArrayList<Exerlite> chronoList = reader.getExerlitesByRoute(route.get_id(), C.SortMode.DATE, true, Prefs.getRouteVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();
            D.markTop(exerliteList);

            if (exerliteList.size() != 0) {

                //Graph graph = new Graph(chronoList, Graph.DataX.INDEX, Graph.DataY.PACE);
                GraphData data = new GraphData(GraphData.ofExerlites(chronoList), GraphData.GRAPH_BEZIER,false, false);
                Graph graph = new Graph(data, true, false, false, true, true, false, true);
                graph.setTag(RecyclerItem.TAG_GRAPH_REC);
                itemList.add(graph);

                itemList.add(newSorter(sortModes, sortModesTitle));

                if (route.getGoalPace() != Route.NO_GOAL_PACE) {
                    Goal goal = new Goal(route.getGoalPace());
                    itemList.add(goal);
                }
                addHeadersAndItems(itemList, exerliteList);

                fadeOutEmpty();
            }
            else fadeInEmpty();

            return itemList;
        }
        @Override protected void setSortModes() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.RouteExerciseRA(items, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.EXERCISE_ROUTE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.EXERCISE_ROUTE, smallestFirst);
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
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
        }
        @Override protected void getAdapter() {
            adapter = new RecyclerAdapters.IntervalExerciseRA(items, originId, a);
        }
        @Override protected void getPrefs() {
            sortMode = Prefs.getSortModePref(C.Layout.EXERCISE_ROUTE);
            smallestFirst = Prefs.getSmallestFirstPref(C.Layout.EXERCISE_ROUTE);
        }
        @Override protected void setPrefs() {
            Prefs.setSortModePref(C.Layout.EXERCISE_ROUTE, sortMode);
            Prefs.setSmallestFirstPref(C.Layout.EXERCISE_ROUTE, smallestFirst);
        }

        @Override public void onItemClick(View view, int position, int itemType) {
            if (itemType == RecyclerAdapters.Base.ITEM_ITEM) {
                int _id = ((Exerlite) items.get(position)).get_id();
                if (originId != _id) ViewActivity.startActivity(a, _id, ViewActivity.FROM_INTERVAL);
            }
            super.onItemClick(itemType, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

    }

    public static class DevRF extends RouteRF {

        @Override protected ArrayList<RecyclerItem> getRecyclerItems() {

            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            GraphData dataGoal = new GraphData(Helper.getReader(a).monthDistanceGoal(LocalDate.now()), GraphData.GRAPH_LINE, false, true);
            dataGoal.setPaint("#003E3F43", "#FF252528");
            GraphData dataNow = new GraphData(Helper.getReader(a).monthDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_LINE, false, false);
            GraphData dataLastMonth = new GraphData(Helper.getReader(a).monthDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusMonths(1)), GraphData.GRAPH_LINE, false, false);
            dataLastMonth.setPaint("#FF3E3F43", "#FF252528");

            Graph monthGraph = new Graph(dataNow, false, true, true, true, true, true, false);
            monthGraph.addData(dataLastMonth);
            monthGraph.addData(dataGoal);
            itemList.add(monthGraph);


            GraphData dataGoalYear = new GraphData(Helper.getReader(a).yearDistanceGoal(LocalDate.now()), GraphData.GRAPH_LINE, false, true);
            dataGoalYear.setPaint("#003E3F43", "#FF252528");
            GraphData dataNowYear = new GraphData(Helper.getReader(a).yearDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now()), GraphData.GRAPH_LINE, false, false);
            GraphData dataLastYear = new GraphData(Helper.getReader(a).yearDistance(Prefs.getExerciseVisibleTypes(), LocalDate.now().minusYears(1)), GraphData.GRAPH_LINE, false, false);
            dataLastYear.setPaint("#FF3E3F43", "#FF252528");

            Graph yearGraph = new Graph(dataNowYear, false, true, true, true, true, true, false);
            yearGraph.addData(dataLastYear);
            yearGraph.addData(dataGoalYear);
            itemList.add(yearGraph);

            return itemList;
        }
    }

}
