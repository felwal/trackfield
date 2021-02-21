package com.example.trackfield.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.trackfield.R;

public abstract class BaseDialog extends DialogFragment {

    protected Activity a;
    protected AlertDialog.Builder builder;
    protected LayoutInflater inflater;

    public final static int NO_TEXT = -1;

    ////

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        a = getActivity();
        builder = new AlertDialog.Builder(a);
        inflater = requireActivity().getLayoutInflater();

        return createDialog(buildDialog());
    }

    public abstract String tag();
    protected abstract String title();
    protected abstract String message();
    protected abstract int positiveBtnTxtId();
    protected int negativeBtnTxtId() {
        return R.string.dialog_btn_cancel;
    }

    protected abstract AlertDialog buildDialog();
    protected AlertDialog createDialog(AlertDialog dialog) {

        // title
        int titleId = getResources().getIdentifier( "alertTitle", "id", "android" );
        if (titleId > 0) {
            TextView titleTv = dialog.findViewById(titleId);
            titleTv.setTextAppearance(R.style.DialogTitle);
        }

        // message
        TextView messageTv = dialog.findViewById(android.R.id.message);
        messageTv.setTextAppearance(title().equals("") ? R.style.DialogMessageLone : R.style.DialogMessage);

        // bg
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_bg);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //dialog.getWindow().getDecorView().getBackground().set(R.style.DialogCustom);

        return dialog;
    }

}
