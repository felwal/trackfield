package com.example.trackfield.fragments.recycler_fragments;

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
import com.example.trackfield.adapters.recycler_adapters.RecyclerAdapter;
import com.example.trackfield.database.Helper;
import com.example.trackfield.dialogs.sheets.SortSheet;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.Header;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.L;

import java.util.ArrayList;

public abstract class RecyclerFragment extends Fragment implements RecyclerAdapter.ItemClickListener, SortSheet.DismissListener {

    protected Activity a;
    protected static Thread bgThread;
    protected Helper.Reader reader;
    protected RecyclerView recycler;
    protected RecyclerView.LayoutManager manager;
    protected RecyclerAdapter adapter;

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
                    adapter.setClickListener(RecyclerFragment.this);
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

    protected ArrayList<RecyclerItem> getVisibleItems() {

        ArrayList<RecyclerItem> visibleItems = new ArrayList<>();
        for (RecyclerItem item : allItems) {
            if (item.isVisible()) visibleItems.add(item);
        }
        return visibleItems;
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
        //allItems.clear();
        //allItems.addAll(items);
        diff.dispatchUpdatesTo(adapter);

    }
    public void updateRecycler() {

        ((Threader) () -> {
            allItems = getRecyclerItems();
            final ArrayList<RecyclerItem> newItems = getVisibleItems();//getRecyclerItems();
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
        if (itemType == RecyclerAdapter.ITEM_SORTER) {
            onSorterClick(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }
    }
    protected void onSorterClick(C.SortMode[] sortModes, C.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {

        getPrefs();
        //final BottomSheetDialog sheet = new BottomSheetDialog(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst, this);
        final SortSheet sheet = SortSheet.newInstance(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
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
    @Override public void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        this.sortMode = sortMode;
        this.smallestFirst = smallestFirst;
        setPrefs();
        updateRecycler();
    }

}
