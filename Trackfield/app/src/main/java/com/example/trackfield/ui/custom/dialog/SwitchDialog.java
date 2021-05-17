package com.example.trackfield.ui.custom.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.trackfield.R;
import com.example.trackfield.utils.TypeUtils;
import com.example.trackfield.utils.model.PairList;

import java.util.ArrayList;
import java.util.Arrays;

public class SwitchDialog extends BaseDialog {

    // bundle keys
    public static final String BUNDLE_SWITCH_TEXTS = "switchTexts";
    public static final String BUNDLE_SWITCH_STATES = "switchStates";

    // dialog tags
    public static final String TAG_DEFAULT = "switchDialog";

    private DialogListener listener;

    // arguments
    private ArrayList<String> switchTexts;
    private ArrayList<Boolean> switchStates;

    //

    public static SwitchDialog newInstance(@StringRes int titleRes, @StringRes int messageRes,
        @StringRes int posBtnTxtRes, PairList<String, Boolean> switchValues, String tag) {

        SwitchDialog instance = new SwitchDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);

        bundle.putStringArrayList(BUNDLE_SWITCH_TEXTS, switchValues.getFirsts());
        bundle.putBooleanArray(BUNDLE_SWITCH_STATES,
            TypeUtils.castToPrimitive(switchValues.getSeconds().toArray(new Boolean[0])));

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

        if (bundle != null) {
            switchTexts = bundle.getStringArrayList(BUNDLE_SWITCH_TEXTS);
            switchStates = new ArrayList<>(Arrays.asList(
                TypeUtils.castToGeneric(bundle.getBooleanArray(BUNDLE_SWITCH_STATES))));
        }
    }

    @Override
    protected AlertDialog buildDialog() {
        final View view = inflater.inflate(R.layout.dialog_switch, null);
        LinearLayout ll = view.findViewById(R.id.linearLayout_switchDialog);

        builder.setView(view).setTitle(title)
            .setPositiveButton(posBtnTxtRes, (dialog, id) -> listener.onSwitchDialogPositiveClick(switchStates, tag))
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        // inflate switches
        for (int i = 0; i < switchTexts.size(); i++) {
            Switch sw = (Switch) inflater.inflate(R.layout.dialog_switch_item, ll, false);
            sw.setText(switchTexts.get(i));
            sw.setChecked(switchStates.get(i));
            ll.addView(sw);

            int iFinal = i;
            sw.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                switchStates.set(iFinal, isChecked);
            });
        }

        return builder.show();
    }

    // interface

    public interface DialogListener {

        void onSwitchDialogPositiveClick(ArrayList<Boolean> switchStates, String tag);

    }

}
