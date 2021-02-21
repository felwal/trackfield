package com.example.trackfield.dialogs;

import android.content.Context;

@Deprecated public abstract class BaseDialogWithListener extends BaseDialog {

    protected DialogListener listener;

    ////

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try { listener = (DialogListener) context; }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    public interface DialogListener {
        void doRecreate();
    }

}
