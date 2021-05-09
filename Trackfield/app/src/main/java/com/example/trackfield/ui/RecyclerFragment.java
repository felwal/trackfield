package com.example.trackfield.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.ui.custom.graph.Graph;
import com.example.trackfield.ui.custom.graph.GraphView;
import com.example.trackfield.ui.custom.sheet.SortSheet;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.Goal;
import com.example.trackfield.ui.main.model.Header;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.main.model.Sorter;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerFragment extends Fragment implements DelegateClickListener {

    protected Activity a;
    protected static Thread bgThread;
    protected DbReader reader;

    protected RecyclerView recycler;
    protected RecyclerView.LayoutManager manager;
    protected BaseAdapter adapter;

    protected ConstraintLayout emptyCl;
    protected TextView emptyTitle;
    protected TextView emptyMessage;
    protected ImageView emptyImage;

    protected ArrayList<RecyclerItem> allItems = new ArrayList<>();
    protected ArrayList<RecyclerItem> items = new ArrayList<>();
    protected Constants.SortMode sortMode = Constants.SortMode.DATE;
    protected boolean smallestFirst = false;

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        a = getActivity();
        reader = DbReader.get(a);

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

    protected Sorter getNewSorter(Constants.SortMode[] sortModes, String[] sortModesTitle) {
        return new Sorter(sortModes, sortModesTitle, sortMode, smallestFirst);
    }

    // calls

    public void updateRecycler(final ArrayList<RecyclerItem> newItems) {

        class RecyclerItemCallback extends DiffUtil.Callback {

            private ArrayList<RecyclerItem> oldList, newList;

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

    public void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst) {
        this.sortMode = sortMode;
        this.smallestFirst = smallestFirst;
        setPrefs();
        updateRecycler();
    }

    // item clicks

    protected void onDelegateClick(RecyclerItem item, Constants.SortMode[] sortModes, Constants.SortMode sortMode,
        String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {
        if (item instanceof Sorter) {
            onSorterClick(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }
    }

    protected void onSorterClick(Constants.SortMode[] sortModes, Constants.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {

        getPrefs();
        //final BottomSheetDialog sheet = new BottomSheetDialog(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst, this);
        final SortSheet sheet = SortSheet.newInstance(sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        //sheet.setDismissListener(this);

        sheet.show(getChildFragmentManager());
        getChildFragmentManager().executePendingTransactions();
    }

    // add items

    protected void addHeadersAndItems(ArrayList<RecyclerItem> itemList, ArrayList<Exerlite> exerliteList) {

        if (sortMode == Constants.SortMode.DATE) {
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
        else //(/*sortMode == C.SortMode.PACE &&*/ /*smallestFirst*/ /*&& exerliteList.size() > 3*/)
        {
            for (int i = 0; i < exerliteList.size(); i++) {
                if (i == 0) itemList.add(new Header("Top " + 3, Header.Type.REC));
                else if (i == 3) itemList.add(new Header("Top " + 10, Header.Type.REC));
                else if (i % 10 == 0) itemList.add(new Header("Top " + (i + 10), Header.Type.REC));
                itemList.add(exerliteList.get(i));
            }
        }
        //else itemList.addAll(exerliteList);
    }

    // empty page

    protected void setEmptyPage() {
    }

    protected void fadeInEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeIn(emptyCl, 1));
    }

    protected void fadeOutEmpty() {
        a.runOnUiThread(() -> LayoutUtils.crossfadeOut(emptyCl));
    }

    // abstract

    protected abstract void setSortModes();

    protected abstract ArrayList<RecyclerItem> getRecyclerItems();

    protected abstract void getAdapter();

    protected abstract void getPrefs();

    protected abstract void setPrefs();

    // interface

    interface Threader {

        void run();

        default void interruptAndStart() {
            if (bgThread != null) bgThread.interrupt();
            bgThread = new Thread(Threader.this::run);
            bgThread.start();
        }

    }

    // adapter

    public abstract static class BaseAdapter extends ListDelegationAdapter<List<RecyclerItem>> {

        private Constants.SortMode sortMode = Constants.SortMode.DATE;

        // set

        public void setSortMode(Constants.SortMode sortMode) {
            this.sortMode = sortMode;
        }

        // get

        public Constants.SortMode getSortMode() {
            return sortMode;
        }

        // delegate

        public abstract static class BaseAdapterDelegate<I extends T, T, VH extends RecyclerView.ViewHolder>
            extends AbsListItemAdapterDelegate<I, T, VH> {

            protected Context context;
            protected LayoutInflater inflater;
            protected DelegateClickListener listener;

            //

            public BaseAdapterDelegate(Activity activity, DelegateClickListener listener) {
                context = activity;
                inflater = activity.getLayoutInflater();
                this.listener = listener;
            }

            //

            @Override
            protected boolean isForViewType(@NonNull T item, @NonNull List<T> items, int position) {
                return isForViewType(item);
            }

            public abstract boolean isForViewType(@NonNull T item);

        }

        public static class GoalAdapterDelegate extends
            BaseAdapterDelegate<Goal, RecyclerItem, GoalAdapterDelegate.GoalViewHolder> {

            public GoalAdapterDelegate(Activity activity) {
                super(activity, null);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Goal;
            }

            @NonNull
            @Override
            public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new GoalViewHolder(inflater.inflate(R.layout.item_goal, parent, false));
            }

            @Override
            public void onBindViewHolder(Goal item, GoalViewHolder vh, @Nullable List<Object> payloads) {
                vh.secondary.setText(item.printValues());
            }

            // vh

            public static class GoalViewHolder extends RecyclerView.ViewHolder {

                public TextView primary;
                public TextView secondary;

                public GoalViewHolder(View itemView) {
                    super(itemView);
                    primary = itemView.findViewById(R.id.textView_primary);
                    secondary = itemView.findViewById(R.id.textView_secondary);
                }

            }

        }

        public static class GraphAdapterDelegate extends
            BaseAdapterDelegate<Graph, RecyclerItem, GraphAdapterDelegate.GraphViewHolder> {

            public GraphAdapterDelegate(Activity activity) {
                super(activity, null);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Graph && !item.hasTag(RecyclerItem.TAG_GRAPH_REC);
            }

            @NonNull
            @Override
            public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new GraphViewHolder(inflater.inflate(R.layout.item_graph_week, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull Graph item, GraphViewHolder vh, @Nullable List<Object> payloads) {
                vh.surface.restoreDefaultFocus();
                vh.surface.setGraph(item);
            }

            // vh

            static class GraphViewHolder extends RecyclerView.ViewHolder {

                public GraphView surface;

                public GraphViewHolder(View itemView) {
                    super(itemView);
                    surface = itemView.findViewById(R.id.graphSurface_base);
                }

            }

        }

        public static class GraphRecAdapterDelegate extends
            BaseAdapterDelegate<Graph, RecyclerItem, GraphRecAdapterDelegate.GraphRecViewHolder> {

            public GraphRecAdapterDelegate(Activity activity) {
                super(activity, null);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_REC);
            }

            @NonNull
            @Override
            public GraphRecViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new GraphRecViewHolder(inflater.inflate(R.layout.item_graph_rec, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull Graph item, GraphRecViewHolder vh, @Nullable List<Object> payloads) {
                vh.surface.restoreDefaultFocus();
                vh.surface.setGraph(item);
                vh.low.setText(MathUtils.stringTime(item.getMax(), true));
                vh.high.setText(MathUtils.stringTime(item.getMin(), true));
            }

            // vh

            static class GraphRecViewHolder extends RecyclerView.ViewHolder {

                public GraphView surface;
                public TextView low;
                public TextView high;

                public GraphRecViewHolder(View itemView) {
                    super(itemView);
                    surface = itemView.findViewById(R.id.graphSurface_rec);
                    low = itemView.findViewById(R.id.textView_low);
                    high = itemView.findViewById(R.id.textView_high);

                    // scroll to start
                    final HorizontalScrollView sv = itemView.findViewById(R.id.scrollView_graphSurface);
                    sv.post(() -> {
                        sv.fullScroll(View.FOCUS_RIGHT);
                        sv.scrollTo(sv.getWidth(), 0);
                    });
                }

            }

        }

        public static class HeaderBigAdapterDelegate extends
            BaseAdapterDelegate<Header, RecyclerItem, HeaderBigAdapterDelegate.HeaderBigViewHolder> {

            public HeaderBigAdapterDelegate(Activity activity, DelegateClickListener listener) {
                super(activity, listener);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Header && ((Header) item).isType(Header.Type.YEAR);
            }

            @NonNull
            @Override
            public HeaderBigViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new HeaderBigViewHolder(inflater.inflate(R.layout.item_header_big, parent, false));
            }

            @Override
            public void onBindViewHolder(Header item, @NonNull HeaderBigViewHolder vh, @Nullable List<Object> payloads) {
                vh.primary.setText(item.getTitle());
                vh.secondary.setText(item.printValues());

                // set height depending on if children are expanded
                int height = (int) context.getResources().getDimension(
                    item.areChildrenExpanded() ? R.dimen.layout_header_year : R.dimen.layout_header_year_collapsed);
                vh.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }

            // vh

            class HeaderBigViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                View.OnLongClickListener {

                public TextView primary;
                public TextView secondary;

                public HeaderBigViewHolder(View itemView) {
                    super(itemView);
                    primary = itemView.findViewById(R.id.textView_primary);
                    secondary = itemView.findViewById(R.id.textView_secondary);
                    itemView.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
                }

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onDelegateClick(view, getAdapterPosition());
                    }
                }

                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        listener.onDelegateLongClick(view, getAdapterPosition());
                    }
                    return true;
                }

            }

        }

        public static class HeaderMediumAdapterDelegate extends
            BaseAdapterDelegate<Header, RecyclerItem, HeaderMediumAdapterDelegate.HeaderMediumViewHolder> {

            public HeaderMediumAdapterDelegate(Activity activity, DelegateClickListener listener) {
                super(activity, listener);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Header && ((Header) item).isType(Header.Type.MONTH);
            }

            @NonNull
            @Override
            public HeaderMediumViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new HeaderMediumViewHolder(inflater.inflate(R.layout.item_header_medium, parent, false));
            }

            @Override
            public void onBindViewHolder(Header item, @NonNull HeaderMediumViewHolder vh, @Nullable List<Object> payloads) {
                vh.primary.setText(item.getTitle());
                vh.secondary.setText(item.printValues());

                // set height depending on if children are expanded
                int height = (int) context.getResources().getDimension(
                    item.areChildrenExpanded() ? R.dimen.layout_header_month : R.dimen.layout_header_month_collapsed);
                vh.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }

            // vh

            class HeaderMediumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                View.OnLongClickListener {

                public TextView primary;
                public TextView secondary;

                public HeaderMediumViewHolder(View itemView) {
                    super(itemView);
                    primary = itemView.findViewById(R.id.textView_primary);
                    secondary = itemView.findViewById(R.id.textView_secondary);
                    itemView.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
                }

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onDelegateClick(view, getAdapterPosition());
                    }
                }

                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        listener.onDelegateLongClick(view, getAdapterPosition());
                    }
                    return true;
                }

            }

        }

        public static class HeaderSmallAdapterDelegate extends
            BaseAdapterDelegate<Header, RecyclerItem, HeaderSmallAdapterDelegate.HeaderSmallViewHolder> {

            public HeaderSmallAdapterDelegate(Activity activity, DelegateClickListener listener) {
                super(activity, listener);
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Header && ((Header) item).isType(Header.Type.WEEK, Header.Type.REC);
            }

            @NonNull
            @Override
            public HeaderSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new HeaderSmallViewHolder(inflater.inflate(R.layout.item_header_small, parent, false));
            }

            @Override
            public void onBindViewHolder(Header item, @NonNull HeaderSmallViewHolder vh, @Nullable List<Object> payloads) {
                vh.primary.setText(item.getTitle());
                vh.secondary.setText(item.printValues());
            }

            // vh

            class HeaderSmallViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                View.OnLongClickListener {

                public TextView primary;
                public TextView secondary;

                public HeaderSmallViewHolder(View itemView) {
                    super(itemView);
                    primary = itemView.findViewById(R.id.textView_primary);
                    secondary = itemView.findViewById(R.id.textView_secondary);
                    itemView.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
                }

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onDelegateClick(view, getAdapterPosition());
                    }
                }

                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        listener.onDelegateLongClick(view, getAdapterPosition());
                    }
                    return true;
                }

            }

        }

        public static class SorterAdapterDelegate extends
            BaseAdapterDelegate<Sorter, RecyclerItem, SorterAdapterDelegate.SorterViewHolder> {

            private BaseAdapter adapter;

            //

            public SorterAdapterDelegate(Activity activity, DelegateClickListener listener, BaseAdapter adapter) {
                super(activity, listener);
                this.adapter = adapter;
            }

            // extends AbsListItemAdapterDelegate

            @Override
            public boolean isForViewType(@NonNull RecyclerItem item) {
                return item instanceof Sorter;
            }

            @NonNull
            @Override
            public SorterViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                return new SorterViewHolder(inflater.inflate(R.layout.item_sorter, parent, false));
            }

            @Override
            public void onBindViewHolder(Sorter item, SorterViewHolder vh, @Nullable List<Object> payloads) {
                vh.title.setText(item.getTitle());
                adapter.setSortMode(item.getSortMode());
            }

            // vh

            public class SorterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                public ConstraintLayout button;
                public TextView title;

                public SorterViewHolder(View itemView) {
                    super(itemView);
                    button = itemView.findViewById(R.id.constraintLayout_sort);
                    title = itemView.findViewById(R.id.textView_sort);
                    itemView.setOnClickListener(this);
                }

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onDelegateClick(view, getAdapterPosition());
                    }
                }

            }

        }

    }

}
