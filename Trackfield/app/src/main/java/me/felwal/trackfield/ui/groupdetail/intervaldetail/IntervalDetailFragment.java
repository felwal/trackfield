package me.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.os.Bundle;

import java.util.ArrayList;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.ui.groupdetail.GroupDetailFragment;
import me.felwal.trackfield.utils.AppConsts;

public class IntervalDetailFragment extends GroupDetailFragment {

    // bundle keys
    private final static String BUNDLE_INTERVAL = "interval";
    private final static String BUNDLE_ORIGIN_ID = "originId";

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByDistance(),
        SorterItem.sortByTime(),
        SorterItem.sortByPace()
    );

    private String interval;

    //

    public static IntervalDetailFragment newInstance(String interval, int originId) {
        IntervalDetailFragment instance = new IntervalDetailFragment();
        Bundle bundle = new Bundle();

        bundle.putString(BUNDLE_INTERVAL, interval);
        bundle.putInt(BUNDLE_ORIGIN_ID, originId);

        instance.setArguments(bundle);
        return instance;
    }

    // extends Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            interval = bundle.getString(BUNDLE_INTERVAL, "");
            setOriginId(bundle.getInt(BUNDLE_ORIGIN_ID, Exercise.ID_NONE));
        }

        updateFilterByOrigin();
    }

    // extends RecyclerFragment

    @Override
    protected void setEmptyPage() {
        emptyTitle.setText(getString(R.string.tv_text_empty_intervaldetail_title));
        emptyMessage.setText(getString(R.string.tv_text_empty_intervaldetail_msg));
        emptyImage.setImageDrawable(ResourcesKt.getDrawableCompatWithTint(a, R.drawable.ic_exercise,
            R.attr.tf_colorInterval));
    }

    @Override
    protected void setSorter() {
        sorter.setSelection(
            Prefs.getSorterIndex(AppConsts.Layout.INTERVAL_DETAIL),
            Prefs.getSorterInversion(AppConsts.Layout.INTERVAL_DETAIL));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new IntervalDetailDelegationAdapter(a, this, items, getOriginId());
    }

    @Override
    protected ArrayList<RecyclerItem> getRecyclerItems() {
        ArrayList<RecyclerItem> itemList = new ArrayList<>();
        ArrayList<Exerlite> exerliteList = reader.getExerlitesByInterval(interval, sorter.getMode(),
            sorter.getAscending(), Prefs.getGroupFilter());

        if (exerliteList.size() != 0) {
            itemList.add(sorter.copy());
            addItemsWithHeaders(itemList, exerliteList, sorter.getMode());

            fadeOutEmpty();
        }
        else {
            fadeInEmpty();
        }

        return itemList;
    }

    @Override
    public void onSortSheetDismiss(int selectedIndex) {
        sorter.select(selectedIndex);
        Prefs.setSorter(AppConsts.Layout.INTERVAL_DETAIL, sorter.getSelectedIndex(), sorter.getOrderReversed());
        updateRecycler();
    }

    // implements GroupDetailFragment

    @Override
    public int getFrom() {
        return ExerciseDetailActivity.FROM_INTERVAL;
    }

}
