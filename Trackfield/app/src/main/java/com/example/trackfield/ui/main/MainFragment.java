package com.example.trackfield.ui.main;

import androidx.fragment.app.Fragment;

import com.example.trackfield.utils.AppConsts;

public abstract class MainFragment extends Fragment {

    protected abstract void setToolbarTitle();

    protected abstract void scrollToTop();

    protected abstract void updateFragment();

    protected abstract void onSortSheetDismiss(AppConsts.SortMode sortMode, boolean smallestFirst);

}
