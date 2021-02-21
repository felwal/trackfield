package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.TextDialog;

public class RenameInterval extends TextDialog {

    public static RenameInterval newInstance(String text, FragmentManager fm) {
        RenameInterval instance = new RenameInterval();
        bundle(instance, text);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "renameInterval";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_rename_interval);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected String hint() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_rename;
    }

}
