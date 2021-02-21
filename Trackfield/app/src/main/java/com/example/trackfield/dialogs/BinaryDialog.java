package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public abstract class BinaryDialog extends BaseDialog {

    protected DialogListener listener;
    //private String title, message;
    //private int positiveButtonTextId;
    //private String tag;

    // extras
    //private final static String BUNDLE_TITLE = "title";
    //private final static String BUNDLE_MESSAGE = "message";
    //private final static String BUNDLE_POSITIVE_BUTTON = "positiveButton";
    private final static String BUNDLE_TAG = "tag";

    ////

    protected static void bundle(BinaryDialog dialog, FragmentManager fm) {
        //BinaryDialog instance = new BinaryDialog();
        Bundle bundle = new Bundle();
        //bundle.putString(BUNDLE_TITLE, title);
        //bundle.putString(BUNDLE_MESSAGE, message);
        //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
        bundle.putString(BUNDLE_TAG, dialog.tag());
        dialog.setArguments(bundle);
        dialog.show(fm, dialog.tag());
    }

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            //title = bundle.getString(BUNDLE_TITLE, "");
            //message = bundle.getString(BUNDLE_MESSAGE, "");
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

        if (!message().equals("")) builder.setMessage(message());
        builder.setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), (dialog, id) -> listener.onBinaryDialogPositiveClick(tag()))
                .setNegativeButton(negativeBtnTxtId(), (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    public interface DialogListener {
        void onBinaryDialogPositiveClick(String tag);
    }

}
