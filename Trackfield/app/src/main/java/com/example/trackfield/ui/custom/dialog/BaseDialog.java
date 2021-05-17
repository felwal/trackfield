package com.example.trackfield.ui.custom.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;

public abstract class BaseDialog extends DialogFragment {

    public static final int NO_RES = -1;
    public static final int NO_FLOAT_TEXT = -1;

    // bundle keys
    protected static final String BUNDLE_TITLE_RES = "title";
    protected static final String BUNDLE_MESSAGE_RES = "message";
    protected static final String BUNDLE_POSITIVE_BUTTON_RES = "positiveButtonText";
    protected static final String BUNDLE_NEGATIVE_BUTTON_RES = "negativeButtonText";
    protected static final String BUNDLE_TAG = "tag";

    protected Activity a;
    protected AlertDialog.Builder builder;
    protected LayoutInflater inflater;

    // arguments
    protected String title;
    protected String message;
    protected String tag;
    @StringRes protected int posBtnTxtRes = R.string.dialog_btn_ok;
    @StringRes protected int negBtnTxtRes = R.string.dialog_btn_cancel;

    // extends DialogFragment

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        a = getActivity();
        builder = new AlertDialog.Builder(a);
        inflater = requireActivity().getLayoutInflater();

        unpackBundle();

        return customizeDialog(buildDialog());
    }

    // tools

    protected static Bundle putBundleBase(@StringRes int titleRes, @StringRes int messageRes,
        @StringRes int posBtnTxtRes, String tag) {

        Bundle bundle = new Bundle();

        bundle.putInt(BUNDLE_TITLE_RES, titleRes);
        bundle.putInt(BUNDLE_MESSAGE_RES, messageRes);
        bundle.putInt(BUNDLE_POSITIVE_BUTTON_RES, posBtnTxtRes);
        bundle.putString(BUNDLE_TAG, tag);

        return bundle;
    }

    protected Bundle unpackBundleBase(String defaultTag) {
        Bundle bundle = getArguments();

        if (bundle != null) {
            int titleRes = bundle.getInt(BUNDLE_TITLE_RES, NO_RES);
            int messageRes = bundle.getInt(BUNDLE_MESSAGE_RES, NO_RES);
            posBtnTxtRes = bundle.getInt(BUNDLE_POSITIVE_BUTTON_RES, posBtnTxtRes);
            negBtnTxtRes = bundle.getInt(BUNDLE_NEGATIVE_BUTTON_RES, negBtnTxtRes);
            tag = bundle.getString(BUNDLE_TAG, defaultTag);

            // convert from res
            title = titleRes == NO_RES ? "" : getString(titleRes);
            message = messageRes == NO_RES ? "" : getString(messageRes);
        }

        return bundle;
    }

    protected AlertDialog customizeDialog(AlertDialog dialog) {
        // title
        int titleId = getResources().getIdentifier("alertTitle", "id", "android");
        if (titleId > 0) {
            TextView titleTv = dialog.findViewById(titleId);
            titleTv.setTextAppearance(R.style.DialogTitle);
        }

        // message
        TextView messageTv = dialog.findViewById(android.R.id.message);
        messageTv.setTextAppearance(title.equals("") ? R.style.DialogMessageLone : R.style.DialogMessage);

        // bg
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_bg);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //dialog.getWindow().getDecorView().getBackground().set(R.style.DialogCustom);

        return dialog;
    }

    public void show(FragmentManager fm) {
        if (!isAdded()) super.show(fm, tag);
    }

    // abstract

    protected abstract void unpackBundle();

    protected abstract AlertDialog buildDialog();

}
