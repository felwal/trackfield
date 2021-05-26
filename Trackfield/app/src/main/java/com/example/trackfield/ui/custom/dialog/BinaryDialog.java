package com.example.trackfield.ui.custom.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.trackfield.R;
import com.example.trackfield.utils.annotations.Debug;

public class BinaryDialog extends BaseDialog {

    // bundle keys
    private static final String BUNDLE_PASS_VALUE = "passValue";

    // dialog tags
    private static final String TAG_DEFAULT = "binaryDialog";

    private DialogListener listener;

    // arguments
    @Nullable private String passValue = null;

    //

    public static BinaryDialog newInstance(@StringRes int titleRes, @StringRes int messageRes,
        @StringRes int posBtnTxtRes, String tag) {

        BinaryDialog instance = new BinaryDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);
        instance.setArguments(bundle);

        return instance;
    }

    public static BinaryDialog newInstance(@StringRes int titleRes, @StringRes int messageRes,
        @StringRes int posBtnTxtRes, @Nullable String passValue, String tag) {

        BinaryDialog instance = new BinaryDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);

        bundle.putString(BUNDLE_PASS_VALUE, passValue);

        instance.setArguments(bundle);
        return instance;
    }

    @Debug
    public static BinaryDialog generic(String tag) {
        return newInstance(R.string.dialog_title_continue, NO_RES, R.string.dialog_btn_continue, tag);
    }

    // extends DialogFragment

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

    // extends BaseDialog

    @Override
    protected void unpackBundle() {
        Bundle bundle = unpackBundleBase(TAG_DEFAULT);
        passValue = bundle.getString(BUNDLE_PASS_VALUE, null);
    }

    @Override
    protected AlertDialog buildDialog() {
        if (!message.equals("")) builder.setMessage(message);

        builder.setTitle(title)
            .setPositiveButton(posBtnTxtRes, (dialog, id) -> listener.onBinaryDialogPositiveClick(passValue, tag))
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // interface

    public interface DialogListener {

        void onBinaryDialogPositiveClick(String passValue, String tag);

    }

}
