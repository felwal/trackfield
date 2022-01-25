package com.felwal.trackfield.ui.exercisedetail;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.felwal.trackfield.R;
import com.felwal.trackfield.utils.AppConsts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExerciseAddActivity extends ExerciseEditActivity {

    private String creationDate;
    private String creationTime;

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), ExerciseAddActivity.class);
        c.startActivity(startIntent);
    }

    // set

    @Override
    protected void setEditTexts() {
        dateEt.setText(creationDate = LocalDate.now().format(AppConsts.FORMATTER_EDIT_DATE));
        timeEt.setText(creationTime = LocalDateTime.now().format(AppConsts.FORMATTER_EDIT_TIME));
    }

    // get

    @Override
    protected int getToolbarTitleRes() {
        return R.string.activity_title_exerciseadd;
    }

    @Override
    protected boolean haveEditsBeenMade() {
        Resources res = getResources();

        return !routeActv.getText().toString().equals("")
            || !routeVarActv.getText().toString().equals("")
            || !dateEt.getText().toString().equals(creationDate)
            || !timeEt.getText().toString().equals(creationTime)
            || !noteEt.getText().toString().equals("")
            || !distanceEt.getText().toString().equals(res.getString(R.string.et_text_exerciseedit_distance))
            || !hoursEt.getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !minutesEt.getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !secondsEt.getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !deviceActv.getText().toString().equals("")
            || !recordingMethodActv.getText().toString().equals("")
            || !typeActv.getText().toString().equals("")
            || drivenSw.isChecked();
    }

}
