package com.example.trackfield.view.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.trackfield.R;
import com.example.trackfield.service.toolbox.L;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class TimeDialog extends BaseDialog {

    protected DialogListener listener;

    // arguments
    private int text1, text2;
    private String hint1, hint2;
    @StringRes private int neuBtnTxtId;

    // bundle
    private final static String BUNDLE_TEXT1 = "text1";
    private final static String BUNDLE_TEXT2 = "text2";
    private final static String BUNDLE_HINT1 = "hint1";
    private final static String BUNDLE_HINT2 = "hint2";
    private final static String BUNDLE_NEUTRAL_BUTTON_RES = "neutralButtonTextId";

    private final static String TAG_DEFAULT = "timeDialog";

    ////

    public static TimeDialog newInstance(@StringRes int titleRes, @StringRes int messageRes, int text1, int text2, String hint1, String hint2,
                                         @StringRes int posBtnTxtRes, @StringRes int neuBtnTxtRes, String tag) {

        TimeDialog instance = new TimeDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);

        bundle.putInt(BUNDLE_TEXT1, text1);
        bundle.putInt(BUNDLE_TEXT2, text2);
        bundle.putString(BUNDLE_HINT1, hint1);
        bundle.putString(BUNDLE_HINT2, hint2);
        bundle.putInt(BUNDLE_NEUTRAL_BUTTON_RES, neuBtnTxtRes);

        instance.setArguments(bundle);

        return instance;
    }

    // on

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    // extends

    @Override
    protected void unpackBundle() {
        Bundle bundle = unpackBundleBase(TAG_DEFAULT);

        text1 = bundle.getInt(BUNDLE_TEXT1, 0);
        text2 = bundle.getInt(BUNDLE_TEXT2, 0);
        hint1 = bundle.getString(BUNDLE_HINT1, "");
        hint2 = bundle.getString(BUNDLE_HINT2, "");
        neuBtnTxtId = bundle.getInt(BUNDLE_NEUTRAL_BUTTON_RES, R.string.action_delete);
    }

    @Override
    protected AlertDialog buildDialog() {

        final View dialogView = inflater.inflate(R.layout.dialog_time, null);
        final EditText et1 = dialogView.findViewById(R.id.editText_numberField1);
        final EditText et2 = dialogView.findViewById(R.id.editText_numberField2);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_goal);

        et1.setHint(hint1);
        et2.setHint(hint2);
        if (text1 != NO_FLOAT_TEXT) et1.setText(Integer.toString(text1));
        if (text2 != NO_FLOAT_TEXT) et2.setText(Integer.toString(text2));
        if (!message.equals("")) builder.setMessage(message);

        // require selection
        chipGroup.setVisibility(View.GONE);
        //setChipGroup(chipGroup);
        //chipGroup.setOnCheckedChangeListener((group, checkedId) -> setChipGroup(group));

        builder.setView(dialogView).setTitle(title)
                .setPositiveButton(posBtnTxtRes, (dialog, id) -> {
                    try {
                        final int input1 = Integer.parseInt(et1.getText().toString());
                        final int input2 = Integer.parseInt(et2.getText().toString());
                        listener.onTimeDialogPositiveClick(input1, input2, tag);
                    }
                    catch (NumberFormatException e) {
                        L.toast(R.string.toast_err_no_input, a);
                    }
                })
                .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel())
                .setNeutralButton(neuBtnTxtId, (dialog, id) -> listener.onTimeDialogNegativeClick(tag));

        return builder.show();
    }

    // tools

    // TODO time / pace
    private void setChipGroup(ChipGroup chipGroup) {
        Chip checkedChip = (Chip) chipGroup.getChildAt(chipGroup.getCheckedChipId());

        if (checkedChip != null) {
            for (int i = 0; i < chipGroup.getChildCount(); ++i) {
                chipGroup.getChildAt(i).setClickable(true);
            }
            checkedChip.setClickable(false);
        }
    }

    // interface

    public interface DialogListener {

        void onTimeDialogPositiveClick(int input1, int input2, String tag);

        void onTimeDialogNegativeClick(String tag);
        //void onTimeDialogNeutralClick(String tag);
    }

}
