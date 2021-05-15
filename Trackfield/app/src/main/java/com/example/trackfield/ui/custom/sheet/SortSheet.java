package com.example.trackfield.ui.custom.sheet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trackfield.R;
import com.example.trackfield.ui.common.model.Sorter;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.ScreenUtils;

public class SortSheet extends BaseSheet {

    public static final String BUNDLE_TITLES = "titles";
    public static final String BUNDLE_SELECTED_INDEX = "selectedIndex";
    private static final String TAG_DEFAULT = "sortSheet";

    private DismissListener listener;

    // arguments
    private String[] titles;
    private int selectedIndex;

    //

    public static SortSheet newInstance(Sorter sorter) {
        SortSheet instance = new SortSheet();
        Bundle bundle = new Bundle();

        bundle.putStringArray(BUNDLE_TITLES, sorter.getTitles());
        bundle.putInt(BUNDLE_SELECTED_INDEX, sorter.getSelectedIndex());

        instance.setArguments(bundle);
        return instance;
    }

    // extends DialogFragment

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.sheet_sort, container, false);

        buildSheet();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DismissListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    // extends BaseSheet

    @Override
    protected void unpackBundle() {
        Bundle bundle = getArguments();

        if (bundle != null) {
            titles = bundle.getStringArray(BUNDLE_TITLES);
            selectedIndex = bundle.getInt(BUNDLE_SELECTED_INDEX, 0);
        }

        tag = TAG_DEFAULT;
    }

    // set

    private void buildSheet() {
        LinearLayout ll = view.findViewById(R.id.linearLayout_sortSheet);

        for (int i = 0; i < titles.length; i++) {
            View item = inflater.inflate(R.layout.sheet_sort_item, ll, false);
            TextView tv = item.findViewById(R.id.textView_sheet_sort_item);

            // set title and styling
            tv.setText(titles[i]);
            if (i == selectedIndex) {
                tv.setTextColor(getResources().getColor(R.color.colorTextHighlight));
                tv.setTypeface(null, Typeface.BOLD);
            }

            ll.addView(item);

            // add listener
            int index = i;
            tv.setOnClickListener(v -> {
                listener.onSortSheetDismiss(index);
                dismiss();
            });
        }
    }

    // interface

    public interface DismissListener {

        void onSortSheetDismiss(int selectedIndex);

    }

}
