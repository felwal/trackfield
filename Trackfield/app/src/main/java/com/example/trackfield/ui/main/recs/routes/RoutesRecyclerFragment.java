package com.example.trackfield.ui.main.recs.routes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.main.recs.RecsFragment;
import com.example.trackfield.ui.main.recs.routes.model.RouteItem;
import com.example.trackfield.ui.rec.route.RouteActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.model.SortMode;

import java.util.ArrayList;

public class RoutesRecyclerFragment extends RecyclerFragment {

    private final Sorter sorter = new Sorter(
        new SortMode("Recent", SortMode.Mode.DATE, false),
        new SortMode("Name", SortMode.Mode.NAME, true),
        new SortMode("Amount", SortMode.Mode.AMOUNT, false),
        new SortMode("Avg distance", SortMode.Mode.DISTANCE, false),
        new SortMode("Best pace", SortMode.Mode.PACE, true)
    );

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
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<RouteItem> routeItemList = reader.getRouteItems(sorter.getMode(), sorter.isAscending(),
            Prefs.areHiddenRoutesShown(), Prefs.getExerciseVisibleTypes());

        itemList.add(sorter.copy());
        itemList.addAll(routeItemList);
        if (routeItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.ROUTES),
            Prefs.getSorterInversion(AppConsts.Layout.ROUTES));
    }

    @Override
    protected void setAdapter() {
        adapter = new RoutesAdapter(a, this, items);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.empty_title_routes));
        emptyMessage.setText(getString(R.string.empty_message_routes));
        emptyImage.setImageResource(R.drawable.ic_empty_routes_24dp);
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.ROUTES, sorter.getSelectedIndex(), sorter.isOrderInverted());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof RouteItem) {
            RouteActivity.startActivity(a, ((RouteItem) items.get(position)).get_id());
        }

        super.onDelegateClick(item);
    }

}
