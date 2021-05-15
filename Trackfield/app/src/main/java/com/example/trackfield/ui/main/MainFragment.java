package com.example.trackfield.ui.main;

import androidx.fragment.app.Fragment;

import com.example.trackfield.utils.model.SortMode;

public abstract class MainFragment extends Fragment {

    protected abstract void setToolbarTitle();

    protected abstract void scrollToTop();

    protected abstract void updateFragment();

    protected abstract void onSortSheetDismiss(int selectedIndex);

}
