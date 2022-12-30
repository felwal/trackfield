package me.felwal.trackfield.ui.exercisedetail;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.utils.AppConsts;

public class ExerciseAddActivity extends ExerciseEditActivity {

    private String creationDate;
    private String creationTime;
    private String defaultType = "";

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), ExerciseAddActivity.class);
        c.startActivity(startIntent);
    }

    // set

    @Override
    protected void loadData() {
        // nothing to load
    }

    @Override
    protected void setEditTexts() {
        // use 'now' as default
        et(dateTil).setText(creationDate = LocalDate.now().format(AppConsts.FORMATTER_EDIT_DATE));
        et(timeTil).setText(creationTime = LocalDateTime.now().format(AppConsts.FORMATTER_EDIT_TIME));

        // use the most common type as default
        ArrayList<String> mostCommonTypes = DbReader.get(this).getTypes(false, "1");
        if (mostCommonTypes.size() != 0) {
            defaultType = mostCommonTypes.get(0);
            et(typeTil).setText(defaultType);
        }
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
            || !et(typeTil).getText().toString().equals(defaultType)
            || isDistanceDriven;
    }

}
