package com.felwal.trackfield.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
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

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.Header;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.main.MainActivity;
import com.felwal.trackfield.utils.LayoutUtils;

import java.util.ArrayList;

import me.felwal.android.fragment.sheet.SortSheet;
import me.felwal.android.widget.control.SheetOption;

public abstract class RecyclerFragment extends Fragment implements DelegateClickListener,
    ScaleGestureDetector.OnScaleGestureListener {

    protected Activity a;
    protected DbReader reader;
    protected RecyclerView.LayoutManager manager;

    // empty page contents
    protected TextView emptyTitle;
    protected TextView emptyMessage;
    protected ImageView emptyImage;

    // items
    protected ArrayList<RecyclerItem> allItems = new ArrayList<>();
    protected final ArrayList<RecyclerItem> items = new ArrayList<>();

    private RecyclerView recycler;
    private BaseListAdapter adapter;
    private ConstraintLayout emptyCl;

    private ScaleGestureDetector scaleDetector;

    // extends Fragment

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        a = getActivity();
        reader = DbReader.get(a);

        recycler = view.findViewById(R.id.rv_recycler);
        manager = new LinearLayoutManager(a);
        recycler.setLayoutManager(manager);
        recycler.setHasFixedSize(true);

        // empty page
        emptyCl = view.findViewById(R.id.cl_recycler_empty);
        emptyTitle = emptyCl.findViewById(R.id.tv_recycler_empty);
        emptyMessage = emptyCl.findViewById(R.id.tv_recycler_message);
        emptyImage = emptyCl.findViewById(R.id.iv_recycler_empty);
        emptyCl.setVisibility(View.GONE);

        setEmptyPage();
        setSorter();
        setAdapter();

        // set scroll listener
        if (a instanceof MainActivity) {
            ((MainActivity) a).setRecyclerScrollListener(recycler, this);
        }

        scaleDetector = new ScaleGestureDetector(a, this);
        recycler.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            return false;
        });

        return view;
    }

    // set

    protected abstract void setEmptyPage();

    protected abstract void setSorter();

    private void setAdapter() {
        if (adapter == null) {
            new Thread(() -> {
                // add items
                if (items.size() == 0) {
                    items.addAll(getRecyclerItems());
                    allItems.addAll(items);
                }
                a.runOnUiThread(() -> {
                    // set adapter
                    recycler.setAdapter(adapter = getAdapter());
                    LayoutUtils.crossfadeRecycler(recycler);
                });
            }).start();
        }
        else recycler.setAdapter(adapter);
    }

    // get

    protected abstract BaseListAdapter getAdapter();

    protected abstract ArrayList<RecyclerItem> getRecyclerItems();

    protected RecyclerItem getItem(int position) {
        if (position < 0 || position >= adapter.getItems().size()) return null;
        return adapter.getItems().get(position);
    }

    protected RecyclerItem getItemOfAll(int pos) {
        return allItems.get(pos);
    }

    protected ArrayList<RecyclerItem> getVisibleItems() {
        ArrayList<RecyclerItem> visibleItems = new ArrayList<>();

        for (RecyclerItem item : allItems) {
            if (item.isVisible()) visibleItems.add(item);
        }
        return visibleItems;
    }

    // update

    public void updateRecycler(final ArrayList<RecyclerItem> newItems) {
        class RecyclerItemCallback extends DiffUtil.Callback {

            private final ArrayList<RecyclerItem> oldList;
            private final ArrayList<RecyclerItem> newList;

            private RecyclerItemCallback(ArrayList<RecyclerItem> oldList, ArrayList<RecyclerItem> newList) {
                this.oldList = oldList;
                this.newList = newList;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                RecyclerItem oldItem = oldList.get(oldItemPosition);
                RecyclerItem newItem = newList.get(newItemPosition);
                return oldItem.sameItemAs(newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                RecyclerItem oldItem = oldList.get(oldItemPosition);
                RecyclerItem newItem = newList.get(newItemPosition);
                return oldItem.sameContentAs(newItem);
            }

        }
        final DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new RecyclerItemCallback(items, newItems));

        items.clear();
        items.addAll(newItems);
        //allItems.clear();
        //allItems.addAll(items);
        diff.dispatchUpdatesTo(adapter);
    }

    public void updateRecycler() {
        new Thread(() -> {
            allItems = getRecyclerItems();
            final ArrayList<RecyclerItem> newItems = getVisibleItems();
            a.runOnUiThread(() -> updateRecycler(newItems));
        }).start();
    }

    public void collapseAll() {
        for (RecyclerItem item : allItems) {
            if (item instanceof Header && ((Header) item).isType(Header.Type.YEAR)) {
                ((Header) item).setExpanded(false);
            }
            else if (item instanceof Header && ((Header) item).isType(Header.Type.MONTH)) {
                ((Header) item).setExpanded(false);
                item.setCollapsedLevel(1);
            }
            else if (item instanceof Exerlite || item instanceof Header && ((Header) item).isType(Header.Type.WEEK)) {
                item.setCollapsedLevel(2);
            }
        }

        updateRecycler(getVisibleItems());
    }

    public void expandAll() {
        for (RecyclerItem item : allItems) {
            if (item instanceof Header && ((Header) item).isType(Header.Type.YEAR)) {
                ((Header) item).setExpanded(true);
            }
            else if (item instanceof Header && ((Header) item).isType(Header.Type.MONTH)) {
                ((Header) item).setExpanded(true);
                item.setCollapsedLevel(0);
            }
            else if (item instanceof Exerlite || item instanceof Header && ((Header) item).isType(Header.Type.WEEK)) {
                item.setCollapsedLevel(0);
            }
        }

        updateRecycler(getVisibleItems());
    }

    public void scrollToTop() {
        //recycler.scrollToPosition(25);
        recycler.smoothScrollToPosition(0);
    }

    // item clicks

    /**
     * Delegation of delegate click to handle common item {@link Sorter}
     */
    protected void onDelegateClick(RecyclerItem item) {
        if (item instanceof SorterItem) {
            SorterItem sorterItem = (SorterItem) item;

            SortSheet sheet = SortSheet.newInstance(
                new SheetOption("Sort by", "", "sorter", null),
                sorterItem.getSorter());
            sheet.show(getChildFragmentManager());

            getChildFragmentManager().executePendingTransactions();
        }
    }

    public abstract void onSortSheetDismiss(int selectedIndex);

    // add items

    protected void addItemsWithHeaders(ArrayList<RecyclerItem> toItemList, ArrayList<Exerlite> fromExerliteList,
        SorterItem.Mode sortMode) {

        switch (sortMode) {
            case DATE: addItemsWithYearHeaders(toItemList, fromExerliteList); break;
            case START_LAT: addItemsWithLatHeaders(toItemList, fromExerliteList); break;
            case START_LNG: addItemsWithLngHeaders(toItemList, fromExerliteList); break;
            default: addItemsWithIndexHeaders(toItemList, fromExerliteList); break;
        }
    }

    private void addItemsWithYearHeaders(ArrayList<RecyclerItem> toItemList, ArrayList<Exerlite> fromExerliteList) {
        int year = -1;
        int newYear;

        for (Exerlite e : fromExerliteList) {
            if ((newYear = e.getDate().getYear()) != year) {
                toItemList.add(new Header(newYear + "", Header.Type.GROUP));
                year = newYear;
            }

            toItemList.add(e);
        }
    }

    private void addItemsWithLatHeaders(ArrayList<RecyclerItem> toItemList, ArrayList<Exerlite> fromExerliteList) {
        int[] zoneTopBorders = getEvenlySpacedArray(-90, 1, 180);
        int lastZone = -1;

        for (Exerlite e : fromExerliteList) {
            int zone = getZoneIndex(e.getStartLat(), zoneTopBorders);

            if (zone != lastZone) {
                String title = withSignUnit(zoneTopBorders[zone], "°N", "°S");
                Header header = new Header(title, Header.Type.GROUP);
                toItemList.add(header);
                lastZone = zone;
            }

            toItemList.add(e);
        }
    }

    private void addItemsWithLngHeaders(ArrayList<RecyclerItem> toItemList, ArrayList<Exerlite> fromExerliteList) {
        int[] zoneLeftBorders = getEvenlySpacedArray(0, 1, 360);
        int lastZone = -1;

        for (Exerlite e : fromExerliteList) {
            int zone = getZoneIndex(e.getStartLng(), zoneLeftBorders);

            if (zone != lastZone) {
                String title = withSignUnit(zoneLeftBorders[zone], "°E", "°W");
                Header header = new Header(title, Header.Type.GROUP);
                toItemList.add(header);
                lastZone = zone;
            }

            toItemList.add(e);
        }
    }

    private void addItemsWithIndexHeaders(ArrayList<RecyclerItem> toItemList, ArrayList<Exerlite> fromExerliteList) {
        for (int i = 0; i < fromExerliteList.size(); i++) {
            if (i == 0) toItemList.add(new Header("Top " + 3, Header.Type.GROUP));
            else if (i == 3) toItemList.add(new Header("Top " + 10, Header.Type.GROUP));
            else if (i % 10 == 0) toItemList.add(new Header("Top " + (i + 10), Header.Type.GROUP));

            toItemList.add(fromExerliteList.get(i));
        }
    }

    // tools

    private int getZoneIndex(double value, int[] borders) {
        for (int i = 1; i < borders.length; i++) {
            if (value < borders[i]) {
                return i - 1;
            }
        }

        return borders.length - 1;
    }

    private int[] getEvenlySpacedArray(int start, int step, int count) {
        int[] arr = new int[count];

        for (int i = 0; i < count; i++) {
            arr[i] = start + i * step;
        }

        return arr;
    }

    private String withSignUnit(int value, String posUnit, String negUnit) {
        if (value >= 0) return value + posUnit;
        return -1 * value + negUnit;
    }

    protected void fadeInEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeIn(emptyCl, 1));
    }

    protected void fadeOutEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeOut(emptyCl));
    }

    // implements OnScaleGestureListener

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // consider this event as handled
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // continue recognizing this gesture
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // currently zooming is only enabled in ExerciseListRecyclerFragment
    }

}
