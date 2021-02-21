package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.DecimalDialog;

public class EditMass extends DecimalDialog {

    public static EditMass newInstance(float text, FragmentManager fm) {
        EditMass instance = new EditMass();
        bundle(instance, text);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "editMass";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_mass);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_ok;
    }
    @Override protected String hint() {
        return "";
    }

}
