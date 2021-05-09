package com.example.trackfield.ui.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.trackfield.R;
import com.example.trackfield.ui.DelegateClickListener;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.rec.model.DistanceItem;
import com.example.trackfield.ui.rec.model.IntervalItem;
import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.ui.rec.model.RouteItem;
import com.example.trackfield.ui.main.model.Sorter;
import com.example.trackfield.ui.RecyclerFragment;
import com.example.trackfield.ui.rec.DistanceActivity;
import com.example.trackfield.ui.rec.IntervalActivity;
import com.example.trackfield.ui.rec.RouteActivity;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class RecsPagerAdapter extends FragmentPagerAdapter {

    private final Context c;

    private final ViewPager viewPager;
    private RecyclerFragment distanceFragment;
    private RecyclerFragment routeFragment;
    private RecyclerFragment intervalFragment;

    @StringRes private static final int[] TAB_TITLES = new int[]{ R.string.tab_distances, R.string.tab_routes, R.string.tab_intervals };
    private static final int POS_DISTANCES = 0;
    private static final int POS_ROUTES = 1;
    private static final int POS_INTERVALS = 2;

    //

    public RecsPagerAdapter(ViewPager viewPager, Context context, FragmentManager fm) {
        super(fm);
        c = context;
        this.viewPager = viewPager;
    }

    // extends FragmentPagerAdapter

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POS_ROUTES:
                return (routeFragment = new RoutesRecyclerFragment());
            case POS_INTERVALS:
                return (intervalFragment = new IntervalsRecyclerFragment());
            case POS_DISTANCES:
            default:
                return (distanceFragment = new DistancesRecyclerFragment());
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return c.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    // get

    private RecyclerFragment getCurrentFragment() {
        return (RecyclerFragment) instantiateItem(viewPager, viewPager.getCurrentItem());
    }

    // delegate from RecsFragment to RecyclerFragment

    public void scrollToTop() {
        getCurrentFragment().scrollToTop();
    }

    public void updateAdapter() {
        if (distanceFragment != null) distanceFragment.updateRecycler();
        if (routeFragment != null) routeFragment.updateRecycler();
        if (intervalFragment != null) intervalFragment.updateRecycler();
    }

    public void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst) {
        getCurrentFragment().onSortSheetDismiss(sortMode, smallestFirst);
    }

    // fragments

    public static class DistancesRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Distance" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DISTANCE };
        private final boolean[] smallestFirsts = { true };

        // extends Fragment

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        /**
         * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu,
         * MenuInflater)}
         */
        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            menu.clear(); // remove R.menu.menu_toolbar_main_recs
            inflater.inflate(R.menu.menu_toolbar_main_recs_distances, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        // extends RecyclerFragment

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {
            ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(Distance.SortMode.DISTANCE/*sortMode*/,
                smallestFirst, Prefs.getExerciseVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = getNewSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(distanceItemList);
            if (distanceItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();

            return itemList;
        }

        @Override
        protected void setSortModes() {
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCES);
        }

        @Override
        protected void getAdapter() {
            adapter = new DistancesAdapter(a, this, items);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.DISTANCES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.DISTANCES);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.DISTANCES, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.DISTANCES, smallestFirst);
        }

        @Override
        protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_distances));
            emptyMessage.setText(getString(R.string.empty_message_distances));
            emptyImage.setImageResource(R.drawable.ic_empty_distances_24dp);
        }

        // implements DelegateClickListener

        @Override
        public void onDelegateClick(View view, int position) {
            RecyclerItem item = getItem(position);

            if (item instanceof DistanceItem) {
                DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
            }

            super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

        // adapter

        public static class DistancesAdapter extends BaseAdapter {

            public DistancesAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
                delegatesManager
                    .addDelegate(new DistanceAdapterDelegate(activity, listener))
                    .addDelegate(new SorterAdapterDelegate(activity, listener, this))
                    .addDelegate(new GraphAdapterDelegate(activity));

                // Set the items from super class.
                setItems(items);
            }

            // delegate

            public static class DistanceAdapterDelegate extends
                BaseAdapterDelegate<DistanceItem, RecyclerItem, DistanceAdapterDelegate.DistanceViewHolder> {

                public DistanceAdapterDelegate(Activity activity, DelegateClickListener listener) {
                    super(activity, listener);
                }

                // extends AbsListItemAdapterDelegate

                @Override
                public boolean isForViewType(@NonNull RecyclerItem item) {
                    return item instanceof DistanceItem;
                }

                @NonNull
                @Override
                public DistanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                    return new DistanceViewHolder(inflater.inflate(R.layout.item_rec, parent, false));
                }

                @Override
                public void onBindViewHolder(DistanceItem item, DistanceViewHolder vh, @Nullable List<Object> payloads) {
                    vh.primary.setText(MathUtils.prefix(item.getDistance(), 2, "m"));
                    vh.secondary.setText(item.printValues());
                }

                // vh

                class DistanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    public TextView primary;
                    public TextView secondary;

                    public DistanceViewHolder(View itemView) {
                        super(itemView);
                        primary = itemView.findViewById(R.id.textView_primary);
                        secondary = itemView.findViewById(R.id.textView_secondary);
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

    public static class RoutesRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Recent", "Name", "Amount", "Avg distance", "Best pace" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.NAME, Constants.SortMode.AMOUNT, Constants.SortMode.DISTANCE,
            Constants.SortMode.PACE };
        private final boolean[] smallestFirsts = { false, true, false, false, true };

        // extends Fragment

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            // show hidden
            MenuItem hiddenItem = menu.findItem(R.id.action_showHidden);
            hiddenItem.setChecked(Prefs.areHiddenRoutesShown());
            if (Prefs.areHiddenRoutesShown()) hiddenItem.setIcon(R.drawable.ic_hidden_24dp)
                .setTitle(R.string.action_hide_hidden);
            else hiddenItem.setIcon(R.drawable.ic_hide_24dp).setTitle(R.string.action_show_hidden);
        }

        /**
         * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu,
         * MenuInflater)}
         */
        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            menu.clear(); // remove R.menu.menu_toolbar_main_recs
            inflater.inflate(R.menu.menu_toolbar_main_recs_routes, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        // extends RecyclerFragment

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {
            ArrayList<RouteItem> routeItemList = reader.getRouteItems(sortMode, smallestFirst, Prefs.areHiddenRoutesShown(),
                Prefs.getExerciseVisibleTypes());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = getNewSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(routeItemList);
            if (routeItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();

            return itemList;
        }

        @Override
        protected void setSortModes() {
            sortMode = Prefs.getSortModePref(Constants.Layout.ROUTES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTES);
        }

        @Override
        protected void getAdapter() {
            adapter = new RoutesAdapter(a, this, items);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.ROUTES);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.ROUTES);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.ROUTES, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.ROUTES, smallestFirst);
        }

        @Override
        protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_routes));
            emptyMessage.setText(getString(R.string.empty_message_routes));
            emptyImage.setImageResource(R.drawable.ic_empty_routes_24dp);
        }

        // implements DelegateClickListener

        @Override
        public void onDelegateClick(View view, int position) {
            RecyclerItem item = getItem(position);

            if (item instanceof RouteItem) {
                RouteActivity.startActivity(a, ((RouteItem) items.get(position)).get_id());
            }

            super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

        // adapter

        public static class RoutesAdapter extends BaseAdapter {

            public RoutesAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
                delegatesManager
                    .addDelegate(new RouteAdapterDelegate(activity, listener))
                    .addDelegate(new SorterAdapterDelegate(activity, listener, this))
                    .addDelegate(new GraphAdapterDelegate(activity));

                // Set the items from super class.
                setItems(items);
            }

            // delegate

            public static class RouteAdapterDelegate extends
                BaseAdapterDelegate<RouteItem, RecyclerItem, RouteAdapterDelegate.RouteViewHolder> {

                public RouteAdapterDelegate(Activity activity, DelegateClickListener listener) {
                    super(activity, listener);
                }

                // extends AbsListItemAdapterDelegate

                @Override
                public boolean isForViewType(@NonNull RecyclerItem item) {
                    return item instanceof RouteItem;
                }

                @NonNull
                @Override
                public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                    return new RouteViewHolder(inflater.inflate(R.layout.item_rec, parent, false));
                }

                @Override
                public void onBindViewHolder(RouteItem item, RouteViewHolder vh, @Nullable List<Object> payloads) {
                    vh.primary.setText(item.getName());
                    vh.secondary.setText(item.printValues());
                }

                // vh

                class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    public TextView primary;
                    public TextView secondary;

                    public RouteViewHolder(View itemView) {
                        super(itemView);
                        primary = itemView.findViewById(R.id.textView_primary);
                        secondary = itemView.findViewById(R.id.textView_secondary);
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

    public static class IntervalsRecyclerFragment extends RecyclerFragment {

        private final String[] sortModesTitle = { "Recent", "Amount" };
        private final Constants.SortMode[] sortModes = { Constants.SortMode.DATE, Constants.SortMode.AMOUNT };
        private final boolean[] smallestFirsts = { false, false };

        // extends Fragment

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            // show hidden
            MenuItem hiddenItem = menu.findItem(R.id.action_showHidden);
            hiddenItem.setChecked(Prefs.areHiddenRoutesShown());
            if (Prefs.areHiddenRoutesShown()) hiddenItem.setIcon(R.drawable.ic_hidden_24dp)
                .setTitle(R.string.action_hide_hidden);
            else hiddenItem.setIcon(R.drawable.ic_hide_24dp).setTitle(R.string.action_show_hidden);
        }

        /**
         * Inflates toolbar menu in place of {@link RecsFragment#onCreateOptionsMenu(Menu, MenuInflater)}
         */
        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            menu.clear(); // remove R.menu.menu_toolbar_main_recs
            inflater.inflate(R.menu.menu_toolbar_main_recs_routes, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        // extends RecyclerFragment

        @Override
        protected ArrayList<RecyclerItem> getRecyclerItems() {
            ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sortMode, smallestFirst,
                Prefs.areHiddenRoutesShown());
            ArrayList<RecyclerItem> itemList = new ArrayList<>();

            Sorter sorter = getNewSorter(sortModes, sortModesTitle);
            itemList.add(sorter);
            itemList.addAll(intervalItemList);
            if (intervalItemList.size() == 0) {
                itemList.remove(sorter);
                fadeInEmpty();
            }
            else fadeOutEmpty();

            return itemList;
        }

        @Override
        protected void setSortModes() {
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVALS);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVALS);
        }

        @Override
        protected void getAdapter() {
            adapter = new IntervalsAdapter(a, this, items);
        }

        @Override
        protected void getPrefs() {
            sortMode = Prefs.getSortModePref(Constants.Layout.INTERVALS);
            smallestFirst = Prefs.getSmallestFirstPref(Constants.Layout.INTERVALS);
        }

        @Override
        protected void setPrefs() {
            Prefs.setSortModePref(Constants.Layout.INTERVALS, sortMode);
            Prefs.setSmallestFirstPref(Constants.Layout.INTERVALS, smallestFirst);
        }

        @Override
        protected void setEmptyPage() {
            emptyTitle.setText(getString(R.string.empty_title_intervals));
            emptyMessage.setText(getString(R.string.empty_message_intervals));
            emptyImage.setImageResource(R.drawable.ic_empty_interval_24dp);
        }

        // implements DelegateClickListener

        @Override
        public void onDelegateClick(View view, int position) {
            RecyclerItem item = getItem(position);

            if (item instanceof IntervalItem) {
                IntervalActivity.startActivity(a, ((IntervalItem) items.get(position)).getInterval());
            }

            super.onDelegateClick(item, sortModes, sortMode, sortModesTitle, smallestFirsts, smallestFirst);
        }

        // adapter

        public static class IntervalsAdapter extends BaseAdapter {

            public IntervalsAdapter(Activity activity, DelegateClickListener listener, List<RecyclerItem> items) {
                delegatesManager
                    .addDelegate(new IntervalAdapterDelegate(activity, listener))
                    .addDelegate(new SorterAdapterDelegate(activity, listener, this))
                    .addDelegate(new GraphAdapterDelegate(activity));

                // Set the items from super class.
                setItems(items);
            }

            // delegate

            public static class IntervalAdapterDelegate extends
                BaseAdapterDelegate<IntervalItem, RecyclerItem, IntervalAdapterDelegate.IntervalViewHolder> {

                public IntervalAdapterDelegate(Activity activity, DelegateClickListener listener) {
                    super(activity, listener);
                }

                // extends AbsListItemAdapterDelegate

                @Override
                public boolean isForViewType(@NonNull RecyclerItem item) {
                    return item instanceof IntervalItem;
                }

                @NonNull
                @Override
                public IntervalViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
                    return new IntervalViewHolder(inflater.inflate(R.layout.item_rec, parent, false));
                }

                @Override
                public void onBindViewHolder(IntervalItem item, IntervalViewHolder vh, @Nullable List<Object> payloads) {
                    vh.primary.setText(item.getInterval());
                    vh.secondary.setText(item.printValues());
                }

                // vh

                class IntervalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    public TextView primary;
                    public TextView secondary;

                    public IntervalViewHolder(View itemView) {
                        super(itemView);
                        primary = itemView.findViewById(R.id.textView_primary);
                        secondary = itemView.findViewById(R.id.textView_secondary);
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

}
