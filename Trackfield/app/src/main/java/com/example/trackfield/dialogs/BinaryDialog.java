package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

public class BinaryDialog extends BaseDialog {

    protected DialogListener listener;

    private final static String TAG_DEFAULT = "binaryDialog";

    ////

    public static BinaryDialog newInstance(String title, String message, @StringRes int posBtnTxtId, String tag) {

        BinaryDialog instance = new BinaryDialog();
        Bundle bundle = putBundleBase(title, message, posBtnTxtId, tag);

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
        unpackBundleBase(TAG_DEFAULT);
    }

    @Override
    protected AlertDialog buildDialog() {

        if (!message.equals("")) builder.setMessage(message);

        builder.setTitle(title)
                .setPositiveButton(posBtnTxtId, (dialog, id) -> listener.onBinaryDialogPositiveClick(tag))
                .setNegativeButton(negBtnTxtId, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // interface

    public interface DialogListener {
        void onBinaryDialogPositiveClick(String tag);
    }

}
