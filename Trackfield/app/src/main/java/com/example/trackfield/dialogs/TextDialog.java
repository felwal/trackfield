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

public abstract class TextDialog extends BaseDialog {

    protected DialogListener listener;
    private String text;

    // bundle
    private final static String BUNDLE_TEXT = "text";

    ////

    protected static void bundle(TextDialog dialog, String text) {
        //TextDialog instance = new TextDialog();
        Bundle bundle = new Bundle();
        //bundle.putString(BUNDLE_TITLE, title);
        //bundle.putString(BUNDLE_MESSAGE, message);
        //bundle.putString(BUNDLE_HINT, hint);
        bundle.putString(BUNDLE_TEXT, text);
        //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
        //bundle.putString(BUNDLE_TAG, tag);
        dialog.setArguments(bundle);
        //instance.show(fm, tag);
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
            text = bundle.getString(BUNDLE_TEXT, "");
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

        final View dialogView = inflater.inflate(R.layout.dialog_text, null);
        final EditText et = dialogView.findViewById(R.id.editText_textField);
        et.setText(text);
        et.setHint(hint());

        if (!message().equals("")) builder.setMessage(message());
        builder.setView(dialogView).setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String input = et.getText().toString();
                        listener.onTextDialogPositiveClick(input, tag());
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
        void onTextDialogPositiveClick(String input, String tag);
    }

}
