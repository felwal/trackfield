package com.example.trackfield.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;

// dynamic
public class BinaryNew extends DialogFragment {

    protected Activity a;
    protected AlertDialog.Builder builder;
    protected LayoutInflater inflater;

    protected DialogListener listener;
    private String title, message;
    private int positiveButtonTextId;
    private String tag = "binary";

    // extras
    private static final String BUNDLE_TITLE = "title";
    private static final String BUNDLE_MESSAGE = "message";
    private static final String BUNDLE_POSITIVE_BUTTON = "posBtnTxtId";
    private final static String BUNDLE_TAG = "tag";

    ////

    public static BinaryNew newInstance(String title, String message, int posBtnTxtId, FragmentManager fm) {
        BinaryNew instance = new BinaryNew();

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE, title);
        bundle.putString(BUNDLE_MESSAGE, message);
        bundle.putInt(BUNDLE_POSITIVE_BUTTON, posBtnTxtId);
        bundle.putString(BUNDLE_TAG, instance.tag);
        instance.setArguments(bundle);

        instance.show(fm, instance.tag);
        return instance;
    }

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(BUNDLE_TITLE, "");
            message = bundle.getString(BUNDLE_MESSAGE, "");
            positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
            tag = bundle.getString(BUNDLE_TAG);
        }

        a = getActivity();
        builder = new AlertDialog.Builder(a);
        inflater = requireActivity().getLayoutInflater();

        return createDialog(buildDialog());
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try { listener = (DialogListener) context; }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }
    protected AlertDialog buildDialog() {

        if (!message.equals("")) builder.setMessage(message);
        builder.setTitle(title)
                .setPositiveButton(positiveButtonTextId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onBinaryDialogPositiveClick(tag);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        return builder.show();
    }

    public interface DialogListener {
        void onBinaryDialogPositiveClick(String tag);
    }
    protected AlertDialog createDialog(AlertDialog dialog) {

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

}
