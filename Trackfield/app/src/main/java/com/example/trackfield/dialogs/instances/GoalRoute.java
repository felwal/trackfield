package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.TimeDialog;

public class GoalRoute extends TimeDialog {

    public static GoalRoute newInstance(int text1, int text2, FragmentManager fm) {
        GoalRoute instance = new GoalRoute();
        bundle(instance, text1, text2);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "setGoalRoute";
    }
    @Override protected String title() {
        return getString(R.string.dialog_title_set_goal);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected String hint1() {
        return "";
    }
    @Override protected String hint2() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_set;
    }
    @Override protected int neutralBtnTxtId() {
        return R.string.dialog_btn_delete;
    }

}
