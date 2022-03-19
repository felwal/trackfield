package me.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.os.Bundle;
import android.view.View;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.ui.groupdetail.GroupDetailFragment;
import me.felwal.trackfield.utils.AppConsts;

import java.util.ArrayList;

public class IntervalDetailFragment extends GroupDetailFragment {

    // bundle keys
    private final static String BUNDLE_INTERVAL = "interval";
    private final static String BUNDLE_ORIGINID = "originId";

    private final SorterItem sorter = new SorterItem(
        SorterItem.sortByDate(),
        SorterItem.sortByDistance(),
        SorterItem.sortByTime(),
        SorterItem.sortByPace()
    );

    private String interval;
    private int originId = Exercise.NO_ID;

    //

    public static IntervalDetailFragment newInstance(String interval, int originId) {
        IntervalDetailFragment instance = new IntervalDetailFragment();
        Bundle bundle = new Bundle();

        bundle.putString(BUNDLE_INTERVAL, interval);
        bundle.putInt(BUNDLE_ORIGINID, originId);

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
            originId = bundle.getInt(BUNDLE_ORIGINID, Exercise.NO_ID);
        }

        updateFilterByOrigin(originId);
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
        return new IntervalDetailDelegationAdapter(a, this, items, originId);
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

    // implements DelegateClickListener

    @Override
    public void onDelegateClick(View view, int position) {
        RecyclerItem item = getItem(position);

        if (item instanceof Exerlite) {
            int id = ((Exerlite) items.get(position)).getId();
            if (originId != id) ExerciseDetailActivity.startActivity(a, id, ExerciseDetailActivity.FROM_INTERVAL);
        }

        super.onDelegateClick(item);
    }

}
