package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BinaryDialog;

public class FinishTracking extends BinaryDialog {

    public static FinishTracking newInstance(FragmentManager fm) {
        FinishTracking instance = new FinishTracking();
        //bundle(instance, fm);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "finishTracking";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_finish_recording);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_finish;
    }

}
