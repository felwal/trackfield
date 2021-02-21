package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BinaryDialog;

public class DeleteDistance extends BinaryDialog {

    public static DeleteDistance newInstance(FragmentManager fm) {
        DeleteDistance instance = new DeleteDistance();
        //bundle(instance, fm);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "deleteDistance";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_delete_distance);
    }
    @Override protected String message() {
        return getString(R.string.dialog_message_delete_distance);
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_delete;
    }

}
