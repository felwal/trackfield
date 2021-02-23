package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;

public class TextDialog extends BaseDialog {

    protected DialogListener listener;

    // arguments
    private String text, hint;

    // bundle
    private final static String BUNDLE_TEXT = "text";
    private final static String BUNDLE_HINT = "hint";

    private final static String TAG_DEFAULT = "textDialog";

    ////

    public static TextDialog newInstance(String title, String message, String text, String hint, @StringRes int posBtnTxtId, String tag) {

        TextDialog instance = new TextDialog();
        Bundle bundle = putBundleBase(title, message, posBtnTxtId, tag);

        bundle.putString(BUNDLE_TEXT, text);
        bundle.putString(BUNDLE_HINT, hint);

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
            text = bundle.getString(BUNDLE_TEXT, "");
            hint = bundle.getString(BUNDLE_HINT, "");
        }

    }

    @Override
    protected AlertDialog buildDialog() {

        final View dialogView = inflater.inflate(R.layout.dialog_text, null);
        final EditText et = dialogView.findViewById(R.id.editText_textField);

        et.setText(text);
        et.setHint(hint);
        if (!message.equals("")) builder.setMessage(message);

        builder.setView(dialogView).setTitle(title)
                .setPositiveButton(posBtnTxtId, (dialog, id) -> {
                    final String input = et.getText().toString();
                    listener.onTextDialogPositiveClick(input, tag);
                })
                .setNegativeButton(negBtnTxtId, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // interface

    public interface DialogListener {
        void onTextDialogPositiveClick(String input, String tag);
    }

}
