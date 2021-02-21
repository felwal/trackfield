package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.TextDialog;

// text
public class RenameRoute extends TextDialog {

    public static RenameRoute newInstance(String text, FragmentManager fm) {
        RenameRoute instance = new RenameRoute();
        bundle(instance, text);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "renameRoute";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_rename_route);
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
