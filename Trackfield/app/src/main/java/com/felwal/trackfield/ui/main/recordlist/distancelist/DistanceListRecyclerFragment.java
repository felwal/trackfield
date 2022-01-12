package com.felwal.trackfield.ui.main.recordlist.distancelist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.android.widget.sheet.SortMode;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.BaseAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.main.recordlist.RecordListFragment;
import com.felwal.trackfield.ui.main.recordlist.distancelist.model.DistanceItem;
import com.felwal.trackfield.ui.recorddetail.distancedetail.DistanceDetailActivity;
import com.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class DistanceListRecyclerFragment extends RecyclerFragment {

    private final SorterItem sorter = new SorterItem(
        new SortMode("Distance", SorterItem.Mode.DISTANCE, true)
        //new SortMode("Amount", SorterItem.Mode.DISTANCE, false)
    );

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Inflates toolbar menu in place of {@link RecordListFragment#onCreateOptionsMenu(Menu,
     * MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_recs
        inflater.inflate(R.menu.menu_toolbar_main_distancelist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distancelist_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distancelist_msg));
        emptyImage.setImageDrawable(ResUtilsKt.withTint(ResUtilsKt.getDrawableCompat(a, R.drawable.ic_distance),
            a.getColor(R.color.colorAccBlue)));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCES),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCES));
    }

    @Override
    protected BaseAdapter getAdapter() {
        return new DistanceListAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(sorter.getMode(), sorter.getAscending(),
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
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.DISTANCES, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof DistanceItem) {
            DistanceDetailActivity.startActivity(a, ((DistanceItem) items.get(position)).getDistance());
        }

        super.onDelegateClick(item);
    }

}
