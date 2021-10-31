package com.felwal.trackfield.ui.widget.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.felwal.trackfield.R;
import com.felwal.trackfield.utils.model.SwitchChain;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SwitchDialog extends BaseDialog {

    // bundle keys
    public static final String BUNDLE_SWITCH_TEXTS = "texts";
    public static final String BUNDLE_SWITCH_CHECKED = "checked";

    // dialog tags
    public static final String TAG_DEFAULT = "switchDialog";

    private DialogListener listener;

    // arguments
    private String[] texts;
    private boolean[] checked;

    //

    public static SwitchDialog newInstance(@StringRes int titleRes, @StringRes int messageRes,
        @StringRes int posBtnTxtRes, SwitchChain chain, String tag) {

        SwitchDialog instance = new SwitchDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);

        bundle.putStringArray(BUNDLE_SWITCH_TEXTS, chain.getTexts());
        bundle.putBooleanArray(BUNDLE_SWITCH_CHECKED, chain.getChecked());

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
            texts = bundle.getStringArray(BUNDLE_SWITCH_TEXTS);
            checked = bundle.getBooleanArray(BUNDLE_SWITCH_CHECKED);
        }
    }

    @Override
    protected AlertDialog buildDialog() {
        final View view = inflater.inflate(R.layout.dialog_switch, null);
        LinearLayout ll = view.findViewById(R.id.ll_dialog_switch);

        builder.setView(view).setTitle(title)
            .setPositiveButton(posBtnTxtRes, (dialog, id) -> listener.onSwitchDialogPositiveClick(checked, tag))
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        // inflate switches
        for (int i = 0; i < texts.length; i++) {
            SwitchMaterial sw = (SwitchMaterial) inflater.inflate(R.layout.item_dialog_switch, ll, false);
            sw.setText(texts[i]);
            sw.setChecked(checked[i]);
            ll.addView(sw);

            int iFinal = i;
            sw.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                checked[iFinal] = isChecked);
        }

        return builder.show();
    }

    // interface

    public interface DialogListener {

        void onSwitchDialogPositiveClick(boolean[] checked, String tag);

    }

}
