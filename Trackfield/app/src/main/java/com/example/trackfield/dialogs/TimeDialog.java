package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.toolbox.L;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public abstract class TimeDialog extends BaseDialog {

    protected DialogListener listener;
    private int text1, text2;

    // extras
    private final static String BUNDLE_TEXT1 = "text1";
    private final static String BUNDLE_TEXT2 = "text2";

    ////

    protected static void bundle(TimeDialog dialog, int text1, int text2) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_TEXT1, text1);
        bundle.putInt(BUNDLE_TEXT2, text2);
        dialog.setArguments(bundle);
    }

    protected abstract String hint1();
    protected abstract String hint2();
    protected abstract int neutralBtnTxtId();

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            //title = bundle.getString(BUNDLE_TITLE, "");
            //message = bundle.getString(BUNDLE_MESSAGE, "");
            //hint = bundle.getString(BUNDLE_HINT, "");
            text1 = bundle.getInt(BUNDLE_TEXT1, NO_TEXT);
            text2 = bundle.getInt(BUNDLE_TEXT2, NO_TEXT);
            //positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
            //tag = bundle.getString(BUNDLE_TAG);
        }

        return super.onCreateDialog(savedInstanceState);
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try { listener = (DialogListener) context; }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }
    @Override protected AlertDialog buildDialog() {

        final View dialogView = inflater.inflate(R.layout.dialog_time, null);
        final EditText et1 = dialogView.findViewById(R.id.editText_numberField1);
        final EditText et2 = dialogView.findViewById(R.id.editText_numberField2);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_goal);
        et1.setHint(hint1());
        et2.setHint(hint2());
        if (text1 != NO_TEXT) et1.setText(Integer.toString(text1));
        if (text2 != NO_TEXT) et2.setText(Integer.toString(text2));

        // require selection
        setChipGroup(chipGroup);
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(ChipGroup group, int checkedId) {
                setChipGroup(group);
            }
        });

        if (!message().equals("")) builder.setMessage(message());
        builder.setView(dialogView).setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            final int input1 = Integer.parseInt(et1.getText().toString());
                            final int input2 = Integer.parseInt(et2.getText().toString());
                            listener.onTimeDialogPositiveClick(input1, input2, tag());
                        }
                        catch (NumberFormatException e) {
                            L.toast("No input", a);
                        }
                    }
                })
                .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                })
                .setNeutralButton(neutralBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onTimeDialogNegativeClick(tag());
                    }
                });

        AlertDialog dialog = builder.show();
        return dialog;
    }

    private void setChipGroup(ChipGroup chipGroup) {
        Chip checkedChip = (Chip) chipGroup.getChildAt(chipGroup.getCheckedChipId());
        if (checkedChip != null) {
            for (int i = 0; i < chipGroup.getChildCount(); ++i) {
                chipGroup.getChildAt(i).setClickable(true);
            }
            checkedChip.setClickable(false);
        }
    }

    public interface DialogListener {
        void onTimeDialogPositiveClick(int input1, int input2, String tag);
        void onTimeDialogNegativeClick(String tag);
        //void onTimeDialogNeutralClick(String tag);
    }

}
