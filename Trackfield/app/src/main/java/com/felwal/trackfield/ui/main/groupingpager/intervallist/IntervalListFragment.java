package com.felwal.trackfield.ui.main.groupingpager.intervallist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.main.groupingpager.GroupingPagerFragment;
import com.felwal.trackfield.ui.main.groupingpager.intervallist.model.IntervalItem;
import com.felwal.trackfield.ui.groupdetail.intervaldetail.IntervalDetailActivity;
import com.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class IntervalListFragment extends RecyclerFragment {

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate("Recent"),
        SorterItem.sortByName(),
        SorterItem.sortByAmount()
    );

    // extends Fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Inflates toolbar menu in place of {@link GroupingPagerFragment#onCreateOptionsMenu(Menu, MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_groupingpager
        inflater.inflate(R.menu.menu_toolbar_main_intervallist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_intervallist_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_intervallist_msg));
        emptyImage.setImageDrawable(ResUtilsKt.getDrawableCompatWithTint(a, R.drawable.ic_interval, R.attr.tf_colorInterval));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.INTERVAL_LIST),
            Prefs.getSorterInversion(AppConsts.Layout.INTERVAL_LIST));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new IntervalListDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<IntervalItem> intervalItemList = reader.getIntervalItems(sorter.getMode(), sorter.getAscending(),
            Prefs.areHiddenGroupsShown());

        itemList.add(sorter.copy());
        itemList.addAll(intervalItemList);
        if (intervalItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.INTERVAL_LIST, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof IntervalItem) {
            IntervalDetailActivity.startActivity(a, ((IntervalItem) items.get(position)).getInterval());
        }

        super.onDelegateClick(item);
    }

}
