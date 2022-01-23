package com.felwal.trackfield.ui.groupdetail.placedetail;

import android.os.Bundle;
import android.view.View;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.model.Place;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.BaseListAdapter;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.common.model.Exerlite;
import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import com.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class PlaceDetailFragment extends RecyclerFragment {

    // bundle keys
    private final static String BUNDLE_PLACE_ID = "placeId";

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByDistance(),
        SorterItem.sortByTime(),
        SorterItem.sortByPace(),
        SorterItem.sortByLat()
    );

    private Place place;

    //

    public static PlaceDetailFragment newInstance(int placeId) {
        PlaceDetailFragment instance = new PlaceDetailFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(BUNDLE_PLACE_ID, placeId);

        instance.setArguments(bundle);
        return instance;
    }

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            int placeId = bundle.getInt(BUNDLE_PLACE_ID, -1);
            place = DbReader.get(a).getPlace(placeId);

            // filtering
            Prefs.setDistanceVisibleTypes(Prefs.getExerciseVisibleTypes());
        }
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_placedetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_placedetail_msg));
        emptyImage.setImageDrawable(ResUtilsKt.getDrawableCompatWithTint(a, R.drawable.ic_run, R.attr.tf_colorPlace));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.PLACE_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.PLACE_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new PlaceDetailDelegationAdapter(a, this, items);
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByPlace(place, sorter.getMode(),
            sorter.getAscending(), Prefs.getDistanceVisibleTypes());

        if (exerliteList.size() != 0) {
            itemList.add(sorter.copy());
            addItemsWithHeaders(itemList, exerliteList, sorter.getMode());

            fadeOutEmpty();
        }
        else fadeInEmpty();

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.PLACE_DETAIL, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            ExerciseDetailActivity.startActivity(a, id);
        }

        super.onDelegateClick(item);
    }

}
