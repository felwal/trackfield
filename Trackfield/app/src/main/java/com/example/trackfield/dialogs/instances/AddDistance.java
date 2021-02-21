package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.DecimalDialog;

// decimal
public class AddDistance extends DecimalDialog {

    public static AddDistance newInstance(float text, FragmentManager fm) {
        AddDistance instance = new AddDistance();
        bundle(instance, text);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "addExercise";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_add_distance);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_add;
    }
    @Override protected String hint() {
        return "";
    }

}
