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
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.ScreenUtils;

public class SortSheet extends BaseSheet {

    private DismissListener listener;

    // arguments
    private AppConsts.SortMode[] sortModes;
    private AppConsts.SortMode sortMode;
    private String[] sortModesTitle;
    private boolean[] smallestFirsts;
    private boolean smallestFirst;

    private static final String TAG = "peekSheet";

    // bundle
    public static final String BUNDLE_SORTMODES = "sortModes";
    public static final String BUNDLE_SORTMODE = "sortMode";
    public static final String BUNDLE_SORTMODES_TITLE = "sortModesTitle";
    public static final String BUNDLE_SMALLESTFIRSTS = "smallestFirsts";
    public static final String BUNDLE_SMALLESTFIRST = "smallestFirst";

    ////

    public static SortSheet newInstance(AppConsts.SortMode[] sortModes, AppConsts.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {

        SortSheet instance = new SortSheet();
        Bundle bundle = new Bundle();

        bundle.putIntArray(BUNDLE_SORTMODES, AppConsts.SortMode.toInts(sortModes));
        bundle.putInt(BUNDLE_SORTMODE, AppConsts.SortMode.toInt(sortMode));
        bundle.putStringArray(BUNDLE_SORTMODES_TITLE, sortModesTitle);
        bundle.putBooleanArray(BUNDLE_SMALLESTFIRSTS, smallestFirsts);
        bundle.putBoolean(BUNDLE_SMALLESTFIRST, smallestFirst);

        instance.setArguments(bundle);

        return instance;
    }

    // extends DialogFragment

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sheet_sort, container, false);

        setClick();

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
            sortModes = AppConsts.SortMode.fromInts(bundle.getIntArray(BUNDLE_SORTMODES));
            sortMode = AppConsts.SortMode.fromInt(bundle.getInt(BUNDLE_SORTMODE, 0));
            sortModesTitle = bundle.getStringArray(BUNDLE_SORTMODES_TITLE);
            smallestFirsts = bundle.getBooleanArray(BUNDLE_SMALLESTFIRSTS);
            smallestFirst = bundle.getBoolean(BUNDLE_SMALLESTFIRST, false);
        }

        tag = TAG;

    }

    // set

    private void setClick() {

        LinearLayout scroller = view.findViewById(R.id.linearLayout_sortSheet_base);
        RelativeLayout[] layouts = new RelativeLayout[sortModesTitle.length];
        TextView[] textViews = new TextView[sortModesTitle.length];

        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.px(40));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        // add views
        for (int rl = 0; rl < sortModesTitle.length; rl++) {

            // layout
            layouts[rl] = new RelativeLayout(getContext());
            layouts[rl].setLayoutParams(rlParams);
            layouts[rl].setBackgroundColor(getResources().getColor(R.color.colorBG));
            LayoutUtils.setRipple(layouts[rl], a);

            // textView
            textViews[rl] = new TextView(getContext());
            textViews[rl].setLayoutParams(tvParams);
            textViews[rl].setGravity(Gravity.CENTER_VERTICAL);
            textViews[rl].setPadding(ScreenUtils.px(24), 0, 0, 0);
            textViews[rl].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.type_heading1));
            if (sortMode == sortModes[rl]) {
                textViews[rl].setTextColor(getResources().getColor(R.color.colorTextHighlight));
                textViews[rl].setTypeface(null, Typeface.BOLD);
                textViews[rl].setText(sortModesTitle[rl] + " " + AppConsts.ARROWS[MathUtils.heaviside(smallestFirst)]);
            } else {
                //textViews[rl].setTextColor(getResources().getColor(R.attr.colorOnSurface));
                textViews[rl].setTextAppearance(a, R.style.Heading1);
                textViews[rl].setText(sortModesTitle[rl]);
            }

            layouts[rl].addView(textViews[rl]);
            scroller.addView(layouts[rl]);

            // click
            final int RL = rl;
            layouts[rl].setOnClickListener(v -> {
                if (sortMode == sortModes[RL]) { smallestFirst = !smallestFirst; }
                else { sortMode = sortModes[RL]; smallestFirst = smallestFirsts[RL]; }
                listener.onSortSheetDismiss(sortMode, smallestFirst);
                dismiss();
            });

        }

    }

    // interface

    public interface DismissListener {
        void onSortSheetDismiss(AppConsts.SortMode sortMode, boolean smallestFirst);
    }

}