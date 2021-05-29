package com.example.trackfield.ui.exercise;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.example.trackfield.R;
import com.example.trackfield.utils.AppConsts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddActivity extends EditActivity {

    private String creationDate;
    private String creationTime;

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), AddActivity.class);
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
        return R.string.activity_title_add;
    }

    @Override
    protected boolean haveEditsBeenMade() {
        Resources res = getResources();

        return !routeActv.getText().toString().equals("")
            || !routeVarActv.getText().toString().equals("")
            || !dateEt.getText().toString().equals(creationDate)
            || !timeEt.getText().toString().equals(creationTime)
            || !noteEt.getText().toString().equals("")
            || !distanceEt.getText().toString().equals(res.getString(R.string.et_text_edit_distance))
            || !hoursEt.getText().toString().equals(res.getString(R.string.et_text_edit_time))
            || !minutesEt.getText().toString().equals(res.getString(R.string.et_text_edit_time))
            || !secondsEt.getText().toString().equals(res.getString(R.string.et_text_edit_time))
            || !deviceActv.getText().toString().equals("")
            || !recordingMethodActv.getText().toString().equals("")
            || !typeActv.getText().toString().equals("")
            || drivenSw.isChecked();
    }

}
