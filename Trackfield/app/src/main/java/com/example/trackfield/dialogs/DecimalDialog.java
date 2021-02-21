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

public abstract class DecimalDialog extends BaseDialog {

    protected DialogListener listener;
    private String title, message, hint;
    private float text;
    private int positiveButtonTextId;
    private String tag;

    // extras
    private final static String BUNDLE_TITLE = "title";
    private final static String BUNDLE_MESSAGE = "message";
    private final static String BUNDLE_HINT = "hint";
    private final static String BUNDLE_TEXT = "text";
    private final static String BUNDLE_POSITIVE_BUTTON = "positiveButton";
    private final static String BUNDLE_TAG = "tag";

    ////

    protected static void bundle(DecimalDialog dialog, float text) {
        //DecimalDialog instance = new DecimalDialog();
        Bundle bundle = new Bundle();
        //bundle.putString(BUNDLE_TITLE, title); new Bundle();
        //bundle.putString(BUNDLE_MESSAGE, message);
        //bundle.putString(BUNDLE_HINT, hint);
        bundle.putFloat(BUNDLE_TEXT, text);
        //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
        //bundle.putString(BUNDLE_TAG, tag);
        dialog.setArguments(bundle);
        //dialog.show(fm, tag);
        //return instance;
    }

    protected abstract String hint();

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            //title = bundle.getString(BUNDLE_TITLE, "");
            //message = bundle.getString(BUNDLE_MESSAGE, "");
            //hint = bundle.getString(BUNDLE_HINT, "");
            text = bundle.getFloat(BUNDLE_TEXT, 0);
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

        final View dialogView = inflater.inflate(R.layout.dialog_decimal, null);
        final EditText et = dialogView.findViewById(R.id.editText_numberDecimalField);
        et.setHint(hint());
        if (text != NO_TEXT) et.setText(Float.toString(text));

        if (!message().equals("")) builder.setMessage(message());
        builder.setView(dialogView).setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            final float input = Float.parseFloat(et.getText().toString());
                            listener.onDecimalDialogPositiveClick(input, tag());
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
                });

        return builder.show();
    }

    public interface DialogListener {
        void onDecimalDialogPositiveClick(float input, String tag);
    }

}
