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
import com.example.trackfield.toolbox.L;

public class DecimalDialog extends BaseDialog {

    protected DialogListener listener;

    // arguments
    private float text;
    private String hint;

    // bundle
    private final static String BUNDLE_TEXT = "text";
    private final static String BUNDLE_HINT = "hint";

    private final static String TAG_DEFAULT = "decimalDialog";

    ////

    public static DecimalDialog newInstance(String title, String message, float text, String hint, @StringRes int posBtnTxtId, String tag) {

        DecimalDialog instance = new DecimalDialog();
        Bundle bundle = putBundleBase(title, message, posBtnTxtId, tag);

        bundle.putFloat(BUNDLE_TEXT, text);
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
            text = bundle.getFloat(BUNDLE_TEXT, 0);
            hint = bundle.getString(BUNDLE_HINT, "");
        }

    }

    @Override
    protected AlertDialog buildDialog() {

        final View dialogView = inflater.inflate(R.layout.dialog_decimal, null);
        final EditText et = dialogView.findViewById(R.id.editText_numberDecimalField);

        et.setHint(hint);
        if (text != NO_TEXT) et.setText(Float.toString(text));
        if (!message.equals("")) builder.setMessage(message);

        builder.setView(dialogView).setTitle(title)
                .setPositiveButton(posBtnTxtId, (dialog, id) -> {
                    try {
                        final float input = Float.parseFloat(et.getText().toString());
                        listener.onDecimalDialogPositiveClick(input, tag);
                    }
                    catch (NumberFormatException e) {
                        L.toast(a.getString(R.string.toast_err_no_input), a);
                    }
                })
                .setNegativeButton(negBtnTxtId, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // interface

    public interface DialogListener {
        void onDecimalDialogPositiveClick(float input, String tag);
    }

}
