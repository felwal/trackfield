package com.example.trackfield.fragments.dialogs;

import android.app.Activity;
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
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortDialog extends BottomSheetDialogFragment {

    private Activity a;
    private View view;
    private DismissListener listener;

    private C.SortMode[] sortModes;
    private C.SortMode sortMode;
    private String[] sortModesTitle;
    private boolean[] smallestFirsts;
    private boolean smallestFirst;

    // bundle
    public static final String BUNDLE_SORTMODES = "sortModes";
    public static final String BUNDLE_SORTMODE = "sortMode";
    public static final String BUNDLE_SORTMODES_TITLE = "sortModesTitle";
    public static final String BUNDLE_SMALLESTFIRSTS = "smallestFirsts";
    public static final String BUNDLE_SMALLESTFIRST = "smallestFirst";

    ////

    public static SortDialog newInstance(C.SortMode[] sortModes, C.SortMode sortMode, String[] sortModesTitle, boolean[] smallestFirsts, boolean smallestFirst) {
        SortDialog instance = new SortDialog();
        Bundle bundle = new Bundle();
        bundle.putIntArray(BUNDLE_SORTMODES, C.SortMode.toInts(sortModes));
        bundle.putInt(BUNDLE_SORTMODE, C.SortMode.toInt(sortMode));
        bundle.putStringArray(BUNDLE_SORTMODES_TITLE, sortModesTitle);
        bundle.putBooleanArray(BUNDLE_SMALLESTFIRSTS, smallestFirsts);
        bundle.putBoolean(BUNDLE_SMALLESTFIRST, smallestFirst);
        instance.setArguments(bundle);
        return instance;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = getActivity();

        // arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            sortModes = C.SortMode.fromInts(bundle.getIntArray(BUNDLE_SORTMODES));
            sortMode = C.SortMode.fromInt(bundle.getInt(BUNDLE_SORTMODE, 0));
            sortModesTitle = bundle.getStringArray(BUNDLE_SORTMODES_TITLE);
            smallestFirsts = bundle.getBooleanArray(BUNDLE_SMALLESTFIRSTS);
            smallestFirst = bundle.getBoolean(BUNDLE_SMALLESTFIRST, false);
        }

    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_bottomsheet, container, false);

        sortClick();

        return view;
    }

    // buttons
    private void sortClick() {

        LinearLayout scroller = view.findViewById(R.id.linearLayout_sortSheet_base);
        RelativeLayout[] layouts = new RelativeLayout[sortModesTitle.length];
        TextView[] textViews = new TextView[sortModesTitle.length];

        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, L.px(40));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        // add views
        for (int rl = 0; rl < sortModesTitle.length; rl++) {

            // layout
            layouts[rl] = new RelativeLayout(getContext());
            layouts[rl].setLayoutParams(rlParams);
            layouts[rl].setBackgroundColor(getResources().getColor(R.color.colorBG));
            L.ripple(layouts[rl], a);

            // textView
            textViews[rl] = new TextView(getContext());
            textViews[rl].setLayoutParams(tvParams);
            textViews[rl].setGravity(Gravity.CENTER_VERTICAL);
            textViews[rl].setPadding(L.px(24), 0, 0, 0);
            textViews[rl].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.type_heading1));
            if (sortMode == sortModes[rl]) {
                textViews[rl].setTextColor(getResources().getColor(R.color.colorTextHighlight));
                textViews[rl].setTypeface(null, Typeface.BOLD);
                textViews[rl].setText(sortModesTitle[rl] + " " + C.ARROWS[M.heaviside(smallestFirst)]);
            } else {
                //textViews[rl].setTextColor(getResources().getColor(R.attr.colorOnSurface));
                textViews[rl].setTextAppearance(a, R.style.Heading1);
                textViews[rl].setText(sortModesTitle[rl]);
            }

            layouts[rl].addView(textViews[rl]);
            scroller.addView(layouts[rl]);

            // click
            final int RL = rl;
            layouts[rl].setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (sortMode == sortModes[RL]) { smallestFirst = !smallestFirst; }
                    else { sortMode = sortModes[RL]; smallestFirst = smallestFirsts[RL]; }
                    listener.onDismiss(sortMode, smallestFirst);
                    dismiss();
                }
            });

        }

    }

    // dismiss
    public void setDismissListener(DismissListener dismissListener) {
        listener = dismissListener;
    }
    public interface DismissListener {
        void onDismiss(C.SortMode sortMode, boolean smallestFirst);
    }

}
