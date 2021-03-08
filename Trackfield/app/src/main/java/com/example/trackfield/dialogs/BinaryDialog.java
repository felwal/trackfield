package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class BinaryDialog extends BaseDialog {

    protected DialogListener listener;

    @Nullable
    private String passValue = null;

    private final static String BUNDLE_PASS_VALUE = "passValue";

    private final static String TAG_DEFAULT = "binaryDialog";

    ////

    public static BinaryDialog newInstance(@StringRes int titleRes, @StringRes int messageRes, @StringRes int posBtnTxtRes, String tag) {

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

        default void onBinaryDialogPositiveClick(String passValue, String tag) {}

    }

}
