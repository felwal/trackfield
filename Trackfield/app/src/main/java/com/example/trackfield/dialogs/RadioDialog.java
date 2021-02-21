package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.toolbox.Prefs;

import java.util.ArrayList;

public class RadioDialog extends BaseDialog {

    protected DialogListener listener;

    private ArrayList<String> radioTexts;
    private int selectedIndex;

    // bundle
    public static final String BUNDLE_RADIO_TEXTS = "radioTexts";
    public static final String BUNDLE_SELECTED_INDEX = "selectedIndex";

    public static final String TAG_DEFAULT = "radioDialog";

    ////

    public static RadioDialog newInstance(String title, String message, ArrayList<String> radioButtonTexts, int selectedIndex, String tag) {

        RadioDialog instance = new RadioDialog();
        Bundle bundle = putBundleBase(title, message, -1, tag);

        bundle.putStringArrayList(BUNDLE_RADIO_TEXTS, radioButtonTexts);
        bundle.putInt(BUNDLE_SELECTED_INDEX, selectedIndex);

        instance.setArguments(bundle);

        return instance;
    }

    // on

    @Override public void onAttach(@NonNull Context context) {

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
        final RadioGroup group = view.findViewById(R.id.radioGroup_dialog);

        builder.setView(view).setTitle(title)
                .setNegativeButton(negBtnTxtId, (dialog, id) -> getDialog().cancel());

        // inflate radio buttons
        for (int i = 0; i < radioTexts.size(); i++) {

            RadioButton btn = (RadioButton) inflater.inflate(R.layout.dialog_radio_item, group, false);
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
