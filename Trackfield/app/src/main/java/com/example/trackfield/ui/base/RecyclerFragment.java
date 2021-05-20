package com.example.trackfield.ui.base;

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
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.ui.common.model.Exerlite;
import com.example.trackfield.ui.common.model.Header;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.custom.sheet.SortSheet;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.model.SortMode;
import com.example.trackfield.utils.annotations.Unfinished;

import java.util.ArrayList;

public abstract class RecyclerFragment extends Fragment implements DelegateClickListener {

    protected static Thread bgThread;

    protected Activity a;
    protected DbReader reader;
    protected RecyclerView recycler;
    protected RecyclerView.LayoutManager manager;
    protected BaseAdapter adapter;

    // empty page
    protected ConstraintLayout emptyCl;
    protected TextView emptyTitle;
    protected TextView emptyMessage;
    protected ImageView emptyImage;

    // items
    protected ArrayList<RecyclerItem> allItems = new ArrayList<>();
    protected ArrayList<RecyclerItem> items = new ArrayList<>();

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        a = getActivity();
        reader = DbReader.get(a);

        recycler = view.findViewById(R.id.recyclerView);
        manager = new LinearLayoutManager(a);
        recycler.setLayoutManager(manager);
        recycler.setHasFixedSize(true);

        // empty page
        emptyCl = view.findViewById(R.id.constraintLayout_empty);
        emptyTitle = emptyCl.findViewById(R.id.textView_emptyTitle);
        emptyMessage = emptyCl.findViewById(R.id.textView_emptyMessage);
        emptyImage = emptyCl.findViewById(R.id.imageView_emptyImage);
        emptyCl.setVisibility(View.GONE);
        setEmptyPage();

        setSorter();

        // set adapter
        if (adapter == null) {
            ((Threader) () -> {
                // add items
                if (items.size() == 0) {
                    items.addAll(getRecyclerItems());
                    allItems.addAll(items);
                }
                a.runOnUiThread(() -> {
                    setAdapter();
                    recycler.setAdapter(adapter);
                    LayoutUtils.crossfadeRecycler(recycler);
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
            recycler.setAdapter(adapter);
        }

        // set scroll listener
        if (a instanceof MainActivity) {
            ((MainActivity) a).setRecyclerScrollListener(recycler, this);
        }

        return view;
    }

    // get

    protected RecyclerItem getItem(int position) {
        if (position < 0 || position >= adapter.getItems().size()) return null;
        return adapter.getItems().get(position);
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

    // item clicks

    /**
     * Delegation of delegate click to handle common item {@link Sorter}
     */
    protected void onDelegateClick(RecyclerItem item) {
        if (item instanceof Sorter) {
            Sorter sorter = (Sorter) item;

            SortSheet sheet = SortSheet.newInstance(sorter);
            sheet.show(getChildFragmentManager());
            getChildFragmentManager().executePendingTransactions();
        }
    }

    // add items

    protected void addHeadersAndItems(ArrayList<RecyclerItem> itemList, ArrayList<Exerlite> exerliteList,
        SortMode.Mode sortMode) {

        if (sortMode == SortMode.Mode.DATE) {
            int year = -1;
            int newYear;
            for (Exerlite e : exerliteList) {
                if ((newYear = e.getDate().getYear()) != year) {
                    itemList.add(new Header(newYear + "", Header.Type.REC));
                    year = newYear;
                }
                itemList.add(e);
            }
        }
        else {
            for (int i = 0; i < exerliteList.size(); i++) {
                if (i == 0) itemList.add(new Header("Top " + 3, Header.Type.REC));
                else if (i == 3) itemList.add(new Header("Top " + 10, Header.Type.REC));
                else if (i % 10 == 0) itemList.add(new Header("Top " + (i + 10), Header.Type.REC));
                itemList.add(exerliteList.get(i));
            }
        }
    }

    // empty page

    protected void setEmptyPage() {
        // use the default empty page if not overridden
    }

    protected void fadeInEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeIn(emptyCl, 1));
    }

    protected void fadeOutEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeOut(emptyCl));
    }

    // abstract

    protected abstract ArrayList<RecyclerItem> getRecyclerItems();

    protected abstract void setSorter();

    protected abstract void setAdapter();

    public abstract void onSortSheetDismiss(int selectedIndex);

    // interface

    @Unfinished
    interface Threader {

        void run();

        default void interruptAndStart() {
            if (bgThread != null) bgThread.interrupt();
            bgThread = new Thread(Threader.this::run);
            bgThread.start();
        }

    }

}
