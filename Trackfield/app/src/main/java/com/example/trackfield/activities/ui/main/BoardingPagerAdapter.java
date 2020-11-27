package com.example.trackfield.activities.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.trackfield.R;

public class BoardingPagerAdapter extends FragmentPagerAdapter {

    private final Context c;

    @StringRes private static final int[] TAB_TITLES = new int[] { R.string.tab_boarding1_title, R.string.tab_boarding2_title, R.string.tab_boarding3_title };
    @StringRes private static final int[] TAB_MESSAGES = new int[] { R.string.tab_boarding1_message, R.string.tab_boarding2_message, R.string.tab_boarding3_message };

    ////

    public BoardingPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        c = context;
    }

    @Override public Fragment getItem(int position) {
        return BoardingFragment.newInstance(position + 1);
    }
    @Nullable @Override public CharSequence getPageTitle(int position) {
        return c.getResources().getString(TAB_TITLES[position]);
    }
    public String getPageMessage(int position) {
        return c.getResources().getString(TAB_MESSAGES[position]);
    }
    @Override public int getCount() {
        return TAB_TITLES.length;
    }
}