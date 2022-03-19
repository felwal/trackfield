package me.felwal.trackfield.ui.main.groupingpager.distancelist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.groupdetail.distancedetail.DistanceDetailActivity;
import me.felwal.trackfield.ui.main.groupingpager.GroupingPagerFragment;
import me.felwal.trackfield.ui.main.groupingpager.distancelist.model.DistanceItem;
import me.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;

public class DistanceListFragment extends RecyclerFragment {

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDistance()
        //SorterItem.sortByAmount() TODO
    );

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Inflates toolbar menu in place of {@link GroupingPagerFragment#onCreateOptionsMenu(Menu,
     * MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_groupingpager
        inflater.inflate(R.menu.menu_toolbar_main_distancelist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_distancelist_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_distancelist_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_distance,
            R.attr.tf_colorDistance));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.DISTANCE_LIST),
            Prefs.getSorterInversion(AppConsts.Layout.DISTANCE_LIST));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new DistanceListDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<DistanceItem> distanceItemList = reader.getDistanceItems(sorter.getMode(), sorter.getAscending(),
            Prefs.getMainFilter());

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
        Prefs.setSorter(AppConsts.Layout.DISTANCE_LIST, sorter.getSelectedIndex(), sorter.getOrderReversed());
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
