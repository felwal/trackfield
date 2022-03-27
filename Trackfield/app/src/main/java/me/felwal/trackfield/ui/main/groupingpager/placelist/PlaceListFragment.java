package me.felwal.trackfield.ui.main.groupingpager.placelist;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.groupdetail.placedetail.PlaceDetailActivity;
import me.felwal.trackfield.ui.main.groupingpager.GroupListFragment;
import me.felwal.trackfield.ui.main.groupingpager.GroupingPagerFragment;
import me.felwal.trackfield.ui.main.groupingpager.placelist.model.PlaceItem;
import me.felwal.trackfield.utils.AppConsts;

public class PlaceListFragment extends GroupListFragment {

    private final SorterItem sorter = new SorterItem(
        //SorterItem.sortByDate("Recent"), TODO
        SorterItem.sortByName(),
        //SorterItem.sortByAmount(), TODO
        SorterItem.sortByLat(),
        SorterItem.sortByLng()
    );

    // extends Fragment

    /**
     * Inflates toolbar menu in place of {@link GroupingPagerFragment#onCreateOptionsMenu(Menu, MenuInflater)}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear(); // remove R.menu.menu_toolbar_main_groupingpager
        inflater.inflate(R.menu.menu_toolbar_main_placelist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_placelist_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_placelist_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_place, R.attr.tf_colorPlace));
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
        ArrayList<PlaceItem> placeItemList = reader.getPlaceItems(sorter.getMode(), sorter.getAscending(),
            Prefs.areHiddenGroupsShown());

        if (placeItemList.size() != 0) {
            itemList.add(sorter.copy());
            itemList.addAll(placeItemList);

            fadeOutEmpty();
        }
        else fadeInEmpty();

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
