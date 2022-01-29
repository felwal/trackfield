package com.felwal.trackfield.ui.exercisedetail;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.android.widget.dialog.AlertDialog;
import com.felwal.android.widget.dialog.BaseDialogKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.model.Exercise;
import com.felwal.trackfield.data.db.model.Sub;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.utils.ScreenUtils;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.TypeUtils;
import com.felwal.trackfield.utils.annotation.Unimplemented;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ExerciseEditActivity extends AppCompatActivity implements AlertDialog.DialogListener {

    // extras names
    private static final String EXTRA_ID = "id";

    // dialog tags
    private static final String DIALOG_DISCARD = "discardDialog";

    protected TextInputLayout routeTil;
    protected TextInputLayout routeVarTil;
    protected TextInputLayout intervalTil;
    protected TextInputLayout noteTil;
    protected TextInputLayout distanceTil;
    protected TextInputLayout hoursTil;
    protected TextInputLayout minutesTil;
    protected TextInputLayout secondsTil;
    protected TextInputLayout deviceTil;
    protected TextInputLayout recordingMethodTil;
    protected TextInputLayout dateTil;
    protected TextInputLayout timeTil;
    protected TextInputLayout typeTil;
    @Unimplemented private final ArrayList<View> subViews = new ArrayList<>();

    // arguments
    private int exerciseId;
    private Exercise exercise;

    protected boolean isDistanceDriven = false;

    //

    public static void startActivity(Context c, int exerciseId) {
        Intent startIntent = new Intent(c.getApplicationContext(), ExerciseEditActivity.class);
        startIntent.putExtra(EXTRA_ID, exerciseId);
        c.startActivity(startIntent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exerciseedit);
        setToolbar();

        loadData();
        findEditTexts();
        setEditTexts();
        setListeners();
        //setAddSubBtnListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_exerciseedit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            if (haveEditsBeenMade()) showDiscardDialog();
            else finish();
            return true;
        }
        else if (itemId == R.id.action_save_exercise) {
            parseAndSave();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (haveEditsBeenMade()) showDiscardDialog();
        else super.onBackPressed();
    }

    // set

    private void loadData() {
        // extras
        exerciseId = getIntent().getIntExtra(EXTRA_ID, -1);

        exercise = DbReader.get(this).getExercise(exerciseId);
        isDistanceDriven = exercise.isDistanceDriven();
    }

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_exerciseedit);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getToolbarTitleRes());
        ab.setDisplayHomeAsUpEnabled(true);

        // set close icon as home
        Drawable homeIcon = ResUtilsKt.getDrawableCompatWithFilter(this, R.drawable.ic_close,
            R.attr.tf_colorControlToolbar);
        ab.setHomeAsUpIndicator(homeIcon);
    }

    private void findEditTexts() {
        // edittexts
        routeTil = findViewById(R.id.til_exerciseedit_route);
        routeVarTil = findViewById(R.id.til_exerciseedit_routevar);
        intervalTil = findViewById(R.id.til_exerciseedit_interval);
        dateTil = findViewById(R.id.til_exerciseedit_date);
        timeTil = findViewById(R.id.til_exerciseedit_time);
        noteTil = findViewById(R.id.til_exerciseedit_note);
        distanceTil = findViewById(R.id.til_exerciseedit_distance);
        hoursTil = findViewById(R.id.til_exerciseedit_hours);
        minutesTil = findViewById(R.id.til_exerciseedit_minutes);
        secondsTil = findViewById(R.id.til_exerciseedit_seconds);
        deviceTil = findViewById(R.id.til_exerciseedit_device);
        recordingMethodTil = findViewById(R.id.til_exerciseedit_recordingmethod);
        typeTil = findViewById(R.id.til_exerciseedit_type);

        // actv adapters
        setAdapter(typeTil, DbReader.get(this).getTypes());
        setAdapter(routeTil, DbReader.get(this).getRouteNames());
        setAdapter(intervalTil, DbReader.get(this).getIntervals());
        setAdapter(deviceTil, DbReader.get(this).getDevices());
        setAdapter(recordingMethodTil, DbReader.get(this).getMethods());
    }

    protected void setEditTexts() {
        // subs
        for (int i = 0; i < exercise.subCount(); i++) {
            Sub sub = exercise.getSub(i);

            // add views
            LinearLayout ll = findViewById(R.id.ll_exerciseedit);
            View subView = getLayoutInflater().inflate(R.layout.item_exerciseedit_sub, ll, false);
            ll.addView(subView, ll.getChildCount() - 1);
            subViews.add(subView);
            setRemoveSubBtnListener(ll, subView);

            EditText sDistanceEt = subViews.get(i).findViewById(R.id.et_exerciseedit_item_sub_distance);
            EditText sHoursEt = subViews.get(i).findViewById(R.id.et_exerciseedit_item_sub_hours);
            EditText sMinutesEt = subViews.get(i).findViewById(R.id.et_exerciseedit_item_sub_minutes);
            EditText sSecondsEt = subViews.get(i).findViewById(R.id.et_exerciseedit_item_sub_seconds);

            float[] time = MathUtils.getTimeParts(sub.getTime());

            sDistanceEt.setText((float) sub.getDistance() / 1000 + "");
            sHoursEt.setText((int) time[2] + "");
            sMinutesEt.setText((int) time[1] + "");
            sSecondsEt.setText(time[0] + "");
        }

        float[] time = MathUtils.getTimeParts(exercise.getTime());
        String hoursTxt = (int) time[2] + "";
        String minutesTxt = (int) time[1] + "";
        String secondsTxt = time[0] + "";
        String distanceTxt = (float) exercise.getEffectiveDistance(this) / 1000 + "";

        // set texts
        et(routeTil).setText(exercise.getRoute());
        et(routeVarTil).setText(exercise.getRouteVar());
        et(intervalTil).setText(exercise.getInterval());
        et(dateTil).setText(exercise.getDate().format(AppConsts.FORMATTER_EDIT_DATE));
        et(timeTil).setText(exercise.getDateTime().format(AppConsts.FORMATTER_EDIT_TIME));
        et(noteTil).setText(exercise.getNote());
        et(distanceTil).setText(distanceTxt);
        et(hoursTil).setText(hoursTxt);
        et(minutesTil).setText(minutesTxt);
        et(secondsTil).setText(secondsTxt);
        et(deviceTil).setText(exercise.getDevice());
        et(recordingMethodTil).setText(exercise.getRecordingMethod());
        et(typeTil).setText(exercise.getType());

        if (isDistanceDriven) {
            et(distanceTil).setEnabled(false);
            distanceTil.setEndIconDrawable(R.drawable.ic_calculate_filled);
        }

        actv(routeTil).setDropDownVerticalOffset(0);

        // polyline
        /*if (exercise.hasTrail()) {
            polylineEt.setText(exercise.getTrail().getPolyline());
            polylineEt.setFocusable(false);
        }*/
    }

    private void setListeners() {
        // distance checked driven listener
        distanceTil.setEndIconOnClickListener(v -> {
            isDistanceDriven = !isDistanceDriven;
            distanceTil.setEndIconDrawable(isDistanceDriven ? R.drawable.ic_calculate_filled : R.drawable.ic_calculate);
            et(distanceTil).setEnabled(!isDistanceDriven);
        });

        // date focus listener
        et(dateTil).setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            LocalDate dateSelect = LocalDate.parse(et(dateTil).getText(), AppConsts.FORMATTER_EDIT_DATE);

            DatePickerDialog dialog = new DatePickerDialog(ExerciseEditActivity.this, (picker, year, month, dayOfMonth) ->
                et(dateTil).setText(LocalDate.of(year, month + 1, dayOfMonth).format(AppConsts.FORMATTER_EDIT_DATE)),
                dateSelect.getYear(), dateSelect.getMonthValue() - 1, dateSelect.getDayOfMonth());

            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
            dateTil.clearFocus();
        });

        // time focus listener
        et(timeTil).setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            LocalTime timeSelect = LocalTime.parse(et(timeTil).getText(), AppConsts.FORMATTER_EDIT_TIME);

            TimePickerDialog dialog = new TimePickerDialog(ExerciseEditActivity.this, (picker, hour, minute) ->
                et(timeTil).setText(LocalTime.of(hour, minute, 0).format(AppConsts.FORMATTER_EDIT_TIME)),
                timeSelect.getHour(), timeSelect.getMinute(), true);

            dialog.show();
            timeTil.clearFocus();
        });

        // route focus listener
        et(routeTil).setOnFocusChangeListener((v, hasFocus) -> {
            // update dropdown depending on typed route
            if (!hasFocus) {
                String route = et(routeTil).getText().toString();
                int routeId = DbReader.get(this).getRouteId(route);

               setAdapter(routeVarTil, DbReader.get(this).getRouteVariations(routeId));
            }
        });
    }

    // get

    @StringRes
    protected int getToolbarTitleRes() {
        return R.string.activity_title_exerciseedit;
    }

    protected boolean haveEditsBeenMade() {
        float[] time = MathUtils.getTimeParts(exercise.getTime());

        return !et(routeTil).getText().toString().equals(exercise.getRoute())
            || !et(routeVarTil).getText().toString().equals(exercise.getRouteVar())
            || !et(dateTil).getText().toString().equals(exercise.getDate().format(AppConsts.FORMATTER_EDIT_DATE))
            || !et(timeTil).getText().toString().equals(exercise.getDateTime().format(AppConsts.FORMATTER_EDIT_TIME))
            || !et(noteTil).getText().toString().equals(exercise.getNote())
            || !et(distanceTil).getText().toString().equals((float) exercise.getEffectiveDistance(this) / 1000 + "")
            || !et(hoursTil).getText().toString().equals((int) time[2] + "")
            || !et(minutesTil).getText().toString().equals((int) time[1] + "")
            || !et(secondsTil).getText().toString().equals(time[0] + "")
            || !et(deviceTil).getText().toString().equals(exercise.getDevice())
            || !et(recordingMethodTil).getText().toString().equals(exercise.getRecordingMethod())
            || !et(typeTil).getText().toString().equals(exercise.getType())
            || isDistanceDriven != exercise.isDistanceDriven();
    }

    // tools

    protected EditText et(TextInputLayout til) {
        return til.getEditText();
    }

    protected AutoCompleteTextView actv(TextInputLayout til) {
        return (AutoCompleteTextView) et(til);
    }

    private void showDiscardDialog() {
        AlertDialog.newInstance(getString(R.string.dialog_title_discard), "", R.string.dialog_btn_discard,
            R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_DISCARD, null)
            .show(getSupportFragmentManager());
    }

    private void setAdapter(TextInputLayout til, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        actv(til).setAdapter(adapter);
    }

    /**
     * Make an {@link AutoCompleteTextView} show its dropdown on focus and click
     */
    private void addDropDownListener(TextInputLayout til) {
        AutoCompleteTextView actv = actv(til);

        actv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actv.showDropDown();
        });
        actv.setOnClickListener(v -> actv.showDropDown());
    }

    // parse

    private void parseAndSave() {
        try {
            // parse
            String route = et(routeTil).getText().toString().trim();
            String routeVar = et(routeVarTil).getText().toString().trim();
            LocalDate date = LocalDate.parse(et(dateTil).getText(), AppConsts.FORMATTER_EDIT_DATE);
            LocalTime localTime = LocalTime.parse(et(timeTil).getText(), AppConsts.FORMATTER_EDIT_TIME);
            String note = et(noteTil).getText().toString().trim();
            int distance = isDistanceDriven ? Exercise.DISTANCE_DRIVEN
                : (int) (Float.parseFloat(et(distanceTil).getText().toString()) * 1000);
            int hours = Integer.parseInt(et(hoursTil).getText().toString());
            int minutes = Integer.parseInt(et(minutesTil).getText().toString());
            float seconds = Float.parseFloat(et(secondsTil).getText().toString());
            float time = hours * 3600 + minutes * 60 + seconds;
            String dataSource = et(deviceTil).getText().toString().trim();
            String recordingMethod = et(recordingMethodTil).getText().toString().trim();
            String type = TypeUtils.toWordCase(et(typeTil).getText().toString()).trim();
            ArrayList<Sub> subs = parseSubs();
            String interval = et(intervalTil).getText().toString().trim();

            int routeId = DbReader.get(this).getRouteIdOrCreate(route, this);
            LocalDateTime dateTime = LocalDateTime.of(date, localTime);

            // save add
            if (exercise == null) {
                // trail
                /*Trail trail = null;
                String polyline = polylineEt.getText().toString();
                if (!polyline.equals("")) {
                    trail = new Trail(PolyUtil.decode(polyline));
                }*/

                exercise = new Exercise(Exercise.NO_ID, Exercise.NO_ID, Exercise.NO_ID, type, dateTime, routeId,
                    route, routeVar, interval, note, dataSource, recordingMethod, distance, time, subs, null, false);

                boolean success = DbWriter.get(this).addExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }
            // save edit
            else {
                exercise = new Exercise(exercise.getId(), exercise.getStravaId(), exercise.getGarminId(), type,
                    dateTime, routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time,
                    subs, exercise.getTrail(), exercise.isTrailHidden());

                boolean success = DbWriter.get(this).updateExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }

            finish();
        }
        catch (NumberFormatException e) {
            LayoutUtils.toast(R.string.toast_err_save_empty, this);
        }
        catch (StringIndexOutOfBoundsException e) {
            //LayoutUtils.toast(R.string.toast_err_decode_polyline, this);
            LayoutUtils.toast(R.string.toast_err_save_empty, this);
        }
        catch (Exception e) {
            LayoutUtils.handleError(e, this);
        }
    }

    @Unimplemented
    private ArrayList<Sub> parseSubs() {
        ArrayList<Sub> subs = new ArrayList<>();

        // add
        for (int i = 0; i < subViews.size(); i++) {
            View v = subViews.get(i);

            EditText sDistanceEt = v.findViewById(R.id.et_exerciseedit_item_sub_distance);
            EditText sHoursEt = v.findViewById(R.id.et_exerciseedit_item_sub_hours);
            EditText sMinutesEt = v.findViewById(R.id.et_exerciseedit_item_sub_minutes);
            EditText sSecondsEt = v.findViewById(R.id.et_exerciseedit_item_sub_seconds);

            int distance = (int) (Float.parseFloat(sDistanceEt.getText().toString()) * 1000);
            int hours = Integer.parseInt(sHoursEt.getText().toString());
            int minutes = Integer.parseInt(sMinutesEt.getText().toString());
            float seconds = Float.parseFloat(sSecondsEt.getText().toString());
            float time = hours * 3600 + minutes * 60 + seconds;
            int subId = (exercise != null && i < exercise.subCount()) ? exercise.getSub(i).getId() : -1;

            subs.add(new Sub(subId, exerciseId, distance, time));
        }

        // delete
        for (int i = 0; exercise != null && i < exercise.subCount(); i++) {
            if (i >= subs.size()) {
                DbWriter.get(this).deleteSub(exercise.getSub(i));
            }
        }

        return subs;
    }

    // sub listeners

    @Unimplemented
    private void setAddSubBtnListener() {
        /*final Button addSubBtn = findViewById(R.id.btn_exerciseedit_add_sub);
        addSubBtn.setOnClickListener(v -> {
            final LinearLayout ll = findViewById(R.id.ll_exerciseedit);
            final View subView = getLayoutInflater().inflate(R.layout.item_exerciseedit_sub, ll, false);
            ll.addView(subView, ll.getChildCount() - 1);
            subViews.add(subView);

            setRemoveSubBtnListener(ll, subView);
        });*/
    }

    @Unimplemented
    private void setRemoveSubBtnListener(final LinearLayout ll, final View subView) {
        final Button removeBtn = subView.findViewById(R.id.btn_exerciseedit_item_sub_remove);
        removeBtn.setOnClickListener(v -> {
            ll.removeView(subView);
            subViews.remove(subView);
        });
    }

    // implements BinaryDialog

    @Override
    public void onAlertDialogPositiveClick(String tag, String passValue) {
        if (tag.equals(DIALOG_DISCARD)) finish();
    }

    @Override
    public void onAlertDialogNeutralClick(@NonNull String tag, String passValue) {
    }

}
