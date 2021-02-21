package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BinaryDialog;

// binary
public class DeleteExercise extends BinaryDialog {

    public static DeleteExercise newInstance(FragmentManager fm) {
        DeleteExercise instance = new DeleteExercise();
        //bundle(instance, fm);
        instance.show(fm, instance.tag());
        return instance;
    }

    @Override public String tag() {
        return "deleteExercise";
    }
    @Override protected String title() {
        return "";
    }
    @Override protected String message() {
        return getString(R.string.dialog_title_delete_exercise);
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_delete;
    }

}
