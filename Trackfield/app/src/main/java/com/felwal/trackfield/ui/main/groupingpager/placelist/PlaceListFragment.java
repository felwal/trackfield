package com.felwal.trackfield.ui.main.groupingpager.placelist;

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
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.groupdetail.placedetail.PlaceDetailActivity;
import com.felwal.trackfield.ui.main.groupingpager.GroupingPagerFragment;
import com.felwal.trackfield.ui.main.groupingpager.placelist.model.PlaceItem;
import com.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class PlaceListFragment extends RecyclerFragment {

    private final SorterItem sorter = new SorterItem(
        //new SortMode("Recent", SorterItem.Mode.DATE, false),
        new SortMode("Name", SorterItem.Mode.NAME, true),
        //new SortMode("Amount", SorterItem.Mode.AMOUNT, false),
        new SortMode("Latitude", SorterItem.Mode.START_LAT, false),
        new SortMode("Longitude", SorterItem.Mode.START_LNG, true)
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
        menu.clear(); // remove R.menu.menu_toolbar_main_recs
        inflater.inflate(R.menu.menu_toolbar_main_placelist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_placelist_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_placelist_msg));
        emptyImage.setImageDrawable(ResUtilsKt.withTint(ResUtilsKt.getDrawableCompat(a, R.drawable.ic_place),
            a.getColor(R.color.colorAccOrange)));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.PLACE_LIST),
            Prefs.getSorterInversion(AppConsts.Layout.PLACE_LIST));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new PlaceListDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<PlaceItem> placeItemList = reader.getPlaceItems(sorter.getMode(), sorter.getAscending());

        itemList.add(sorter.copy());
        itemList.addAll(placeItemList);
        if (placeItemList.size() == 0) {
            itemList.remove(sorter);
            fadeInEmpty();
        }
        else fadeOutEmpty();

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.PLACE_LIST, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof PlaceItem) {
            PlaceDetailActivity.startActivity(a, ((PlaceItem) items.get(position)).getId());
        }

        super.onDelegateClick(item);
    }

}
