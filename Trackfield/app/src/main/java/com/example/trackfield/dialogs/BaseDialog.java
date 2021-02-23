package com.example.trackfield.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;

public abstract class BaseDialog extends DialogFragment {

    protected Activity a;
    protected AlertDialog.Builder builder;
    protected LayoutInflater inflater;

    // arguments
    protected String title, message, tag;
    @StringRes protected int posBtnTxtId = R.string.dialog_btn_ok;
    @StringRes protected int negBtnTxtId = R.string.dialog_btn_cancel;

    public final static int NO_TEXT = -1;
    public final static int NO_RES = -1;

    // bundle
    protected static final String BUNDLE_TITLE = "title";
    protected static final String BUNDLE_MESSAGE = "message";
    protected static final String BUNDLE_POSITIVE_BUTTON = "positiveButtonTextId";
    protected static final String BUNDLE_NEGATIVE_BUTTON = "negativeButtonTextId";
    protected final static String BUNDLE_TAG = "tag";

    ////

    // extends DialogFragment

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        unpackBundle();
        a = getActivity();
        builder = new AlertDialog.Builder(a);
        inflater = requireActivity().getLayoutInflater();

        return customizeDialog(buildDialog());
    }

    // tools

    protected static Bundle putBundleBase(String title, String message, @StringRes int posBtnTxtId, String tag) {

        Bundle bundle = new Bundle();

        bundle.putString(BUNDLE_TITLE, title);
        bundle.putString(BUNDLE_MESSAGE, message);
        bundle.putInt(BUNDLE_POSITIVE_BUTTON, posBtnTxtId);
        bundle.putString(BUNDLE_TAG, tag);

        return bundle;
    }

    protected Bundle unpackBundleBase(String defaultTag) {

        Bundle bundle = getArguments();

        if (bundle != null) {
            title = bundle.getString(BUNDLE_TITLE, "");
            message = bundle.getString(BUNDLE_MESSAGE, "");
            posBtnTxtId = bundle.getInt(BUNDLE_POSITIVE_BUTTON, posBtnTxtId);
            negBtnTxtId = bundle.getInt(BUNDLE_NEGATIVE_BUTTON, negBtnTxtId);
            tag = bundle.getString(BUNDLE_TAG, defaultTag);
        }

        return bundle;
    }

    protected AlertDialog customizeDialog(AlertDialog dialog) {

        // title
        int titleId = getResources().getIdentifier( "alertTitle", "id", "android" );
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
        super.show(fm, tag);
    }

    // abstract

    protected abstract void unpackBundle();

    protected abstract AlertDialog buildDialog();

}
