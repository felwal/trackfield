package com.felwal.trackfield.ui.custom.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.felwal.trackfield.R;
import com.felwal.trackfield.utils.LayoutUtils;

public class DecimalDialog extends BaseDialog {

    // bundle keys
    private static final String BUNDLE_TEXT = "text";
    private static final String BUNDLE_HINT = "hint";

    // dialog tags
    private static final String TAG_DEFAULT = "decimalDialog";

    private DialogListener listener;

    // arguments
    private float text;
    private String hint;

    //

    public static DecimalDialog newInstance(@StringRes int titleRes, @StringRes int messageRes, float text, String hint,
        @StringRes int posBtnTxtRes, String tag) {

        DecimalDialog instance = new DecimalDialog();
        Bundle bundle = putBundleBase(titleRes, messageRes, posBtnTxtRes, tag);

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
        final EditText et = dialogView.findViewById(R.id.et_dialog_decimal);

        et.setHint(hint);
        if (text != NO_FLOAT_TEXT) et.setText(Float.toString(text));
        if (!message.equals("")) builder.setMessage(message);

        builder.setView(dialogView).setTitle(title)
            .setPositiveButton(posBtnTxtRes, (dialog, id) -> {
                try {
                    final float input = Float.parseFloat(et.getText().toString());
                    listener.onDecimalDialogPositiveClick(input, tag);
                }
                catch (NumberFormatException e) {
                    LayoutUtils.toast(R.string.toast_err_no_input, a);
                }
            })
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // interface

    public interface DialogListener {

        void onDecimalDialogPositiveClick(float input, String tag);

    }

}
