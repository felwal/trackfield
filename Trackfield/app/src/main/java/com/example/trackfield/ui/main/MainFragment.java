package com.example.trackfield.ui.main;

import androidx.fragment.app.Fragment;

public abstract class MainFragment extends Fragment {

    protected abstract void setToolbarTitle();

    protected abstract void scrollToTop();

    protected abstract void updateFragment();

    protected abstract void onSortSheetClick(int selectedIndex);

}
