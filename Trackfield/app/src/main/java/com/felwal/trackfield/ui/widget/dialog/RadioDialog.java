package com.felwal.trackfield.ui.widget.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.felwal.trackfield.R;

import java.util.ArrayList;

public class RadioDialog extends BaseDialog {

    // bundle keys
    public static final String BUNDLE_RADIO_TEXTS = "radioTexts";
    public static final String BUNDLE_SELECTED_INDEX = "selectedIndex";

    // dialog tags
    public static final String TAG_DEFAULT = "radioDialog";

    private DialogListener listener;

    // arguments
    private ArrayList<String> radioTexts;
    private int selectedIndex;

    //

    public static RadioDialog newInstance(@StringRes int titleRes, @StringRes int messageRes,
        ArrayList<String> radioButtonTexts, int selectedIndex, String tag) {

        RadioDialog instance = new RadioDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, NO_RES, tag);

        bundle.putStringArrayList(BUNDLE_RADIO_TEXTS, radioButtonTexts);
        bundle.putInt(BUNDLE_SELECTED_INDEX, selectedIndex);

        instance.setArguments(bundle);
        return instance;
    }

    // on

    @Override
    public void onAttach(@NonNull Context c) {
        super.onAttach(c);

        try {
            listener = (DialogListener) c;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    // extends

    @Override
    protected void unpackBundle() {
        Bundle bundle = unpackBundleBase(TAG_DEFAULT);

        if (bundle != null) {
            radioTexts = bundle.getStringArrayList(BUNDLE_RADIO_TEXTS);
            selectedIndex = bundle.getInt(BUNDLE_SELECTED_INDEX, 0);
        }

        // keep in range
        if (selectedIndex >= radioTexts.size()) selectedIndex = radioTexts.size() - 1;
        else if (selectedIndex < 0) selectedIndex = 0;
    }

    @Override
    protected AlertDialog buildDialog() {
        final View view = inflater.inflate(R.layout.dialog_radio, null);
        final RadioGroup group = view.findViewById(R.id.rg_dialog_radio);

        builder.setView(view).setTitle(title)
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        // inflate radio buttons
        for (int i = 0; i < radioTexts.size(); i++) {
            RadioButton btn = (RadioButton) inflater.inflate(R.layout.item_dialog_radio, group, false);
            btn.setText(radioTexts.get(i));
            group.addView(btn);

            if (i == selectedIndex) btn.setChecked(true);

            // click
            int iFinal = i;
            btn.setOnClickListener(v -> {
                getDialog().cancel();
                listener.onRadioDialogClick(iFinal, tag);
            });
        }

        return builder.show();
    }

    // interface

    public interface DialogListener {

        void onRadioDialogClick(int index, String tag);

    }

}
