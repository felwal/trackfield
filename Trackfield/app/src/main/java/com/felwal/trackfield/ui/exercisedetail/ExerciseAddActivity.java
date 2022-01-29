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
        et(dateTil).setText(creationDate = LocalDate.now().format(AppConsts.FORMATTER_EDIT_DATE));
        et(timeTil).setText(creationTime = LocalDateTime.now().format(AppConsts.FORMATTER_EDIT_TIME));
    }

    // get

    @Override
    protected int getToolbarTitleRes() {
        return R.string.activity_title_exerciseadd;
    }

    @Override
    protected boolean haveEditsBeenMade() {
        Resources res = getResources();

        return !et(routeTil).getText().toString().equals("")
            || !et(routeVarTil).getText().toString().equals("")
            || !et(dateTil).getText().toString().equals(creationDate)
            || !et(timeTil).getText().toString().equals(creationTime)
            || !et(noteTil).getText().toString().equals("")
            || !et(distanceTil).getText().toString().equals(res.getString(R.string.et_text_exerciseedit_distance))
            || !et(hoursTil).getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !et(minutesTil).getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !et(secondsTil).getText().toString().equals(res.getString(R.string.et_text_exerciseedit_time))
            || !et(deviceTil).getText().toString().equals("")
            || !et(recordingMethodTil).getText().toString().equals("")
            || !et(typeTil).getText().toString().equals("")
            || isDistanceDriven;
    }

}
