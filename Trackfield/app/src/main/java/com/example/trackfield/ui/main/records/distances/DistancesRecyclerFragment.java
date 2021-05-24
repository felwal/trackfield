package com.example.trackfield.ui.main.records.distances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.ui.main.records.RecsFragment;
import com.example.trackfield.ui.main.records.distances.model.DistanceItem;
import com.example.trackfield.ui.record.distance.DistanceActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.model.SortMode;

import java.util.ArrayList;

public class DistancesRecyclerFragment extends RecyclerFragment {

    private final Sorter sorter = new Sorter(
        new SortMode("Distance", SortMode.Mode.DISTANCE, true)
        //new SortMode("Amount", SortMode.Mode.DISTANCE, false)
    );

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
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(sorter.getMode(), sorter.isAscending(),
            Prefs.getExerciseVisibleTypes());

        itemList.add(sorter.copy());
        itemList.addAll(distanceItemList);
        if (distanceItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCES),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCES));
    }

    @Override
    protected void setAdapter() {
        adapter = new DistancesAdapter(a, this, items);
    }

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distances_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distances_msg));
        emptyImage.setImageResource(R.drawable.ic_empty_distances);
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.DISTANCES, sorter.getSelectedIndex(), sorter.isOrderInverted());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof DistanceItem) {
            DistanceActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
        }

        super.onDelegateClick(item);
    }

}
