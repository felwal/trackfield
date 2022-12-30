package me.felwal.trackfield.ui.exercisedetail;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import me.felwal.android.fragment.dialog.AlertDialog;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.util.ResourcesKt;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.utils.AppConsts;
import me.felwal.trackfield.utils.LayoutUtils;
import me.felwal.trackfield.utils.MathUtils;
import me.felwal.trackfield.utils.ScreenUtils;
import me.felwal.trackfield.utils.TypeUtils;

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
    protected TextInputLayout labelTil;

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
        findEditTexts();
        loadData();
        setAdapters();
        setListeners();
        setEditTexts();
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

    protected void loadData() {
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
        Drawable homeIcon = ResourcesKt.getDrawableCompatWithFilter(this, R.drawable.ic_close,
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
        labelTil = findViewById(R.id.til_exerciseedit_label);
    }

    protected void setEditTexts() {
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
        et(labelTil).setText(exercise.getLabel());

        if (isDistanceDriven) {
            et(distanceTil).setEnabled(false);
            distanceTil.setEndIconDrawable(R.drawable.ic_drive_filled);
        }

        // polyline
        /*if (exercise.hasTrail()) {
            polylineEt.setText(exercise.getTrail().getPolyline());
            polylineEt.setFocusable(false);
        }*/
    }

    private void setAdapters() {
        // actv adapters
        setAdapter(routeTil, DbReader.get(this).getRouteNames());
        setAdapter(intervalTil, DbReader.get(this).getIntervals());
        setAdapter(typeTil, DbReader.get(this).getTypes(false, null));
        setAdapter(labelTil, DbReader.get(this).getLabels(false));
        setAdapter(deviceTil, DbReader.get(this).getDevices());
        setAdapter(recordingMethodTil, DbReader.get(this).getMethods());

        if (exercise != null) {
            setAdapter(routeVarTil, DbReader.get(this).getRouteVariations(exercise.getRouteId()));
        }
    }

    private void setListeners() {
        // distance checked driven listener
        distanceTil.setEndIconOnClickListener(v -> {
            isDistanceDriven = !isDistanceDriven;
            distanceTil.setEndIconDrawable(isDistanceDriven ? R.drawable.ic_drive_filled : R.drawable.ic_drive);
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
        AlertDialog.newInstance(
            new DialogOption(getString(R.string.dialog_title_discard), "",
                R.string.dialog_btn_discard, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                DIALOG_DISCARD, null))
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

            // number strings
            String distanceStr = et(distanceTil).getText().toString();
            String hoursStr = et(hoursTil).getText().toString();
            String minutesStr = et(minutesTil).getText().toString();
            String secondsStr = et(secondsTil).getText().toString();

            // numbers
            int distance = isDistanceDriven
                ? Exercise.DISTANCE_DRIVEN
                : distanceStr.equals("") ? 0 : (int) (Float.parseFloat(distanceStr) * 1000);
            int hours = hoursStr.equals("") ? 0 : Integer.parseInt(hoursStr);
            int minutes = minutesStr.equals("") ? 0 : Integer.parseInt(minutesStr);
            float seconds = secondsStr.equals("") ? 0 : Float.parseFloat(secondsStr);

            // strings
            String route = et(routeTil).getText().toString().trim();
            String routeVar = et(routeVarTil).getText().toString().trim();
            LocalDate date = LocalDate.parse(et(dateTil).getText(), AppConsts.FORMATTER_EDIT_DATE);
            LocalTime localTime = LocalTime.parse(et(timeTil).getText(), AppConsts.FORMATTER_EDIT_TIME);
            String note = et(noteTil).getText().toString().trim();
            String dataSource = et(deviceTil).getText().toString().trim();
            String recordingMethod = et(recordingMethodTil).getText().toString().trim();
            String type = TypeUtils.toWordCase(et(typeTil).getText().toString()).trim();
            String label = TypeUtils.toWordCase(et(labelTil).getText().toString()).trim();
            String interval = et(intervalTil).getText().toString().trim();

            // convert
            float time = hours * 3600 + minutes * 60 + seconds;
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

                exercise = new Exercise(Exercise.ID_NONE, Exercise.ID_NONE, Exercise.ID_NONE, type, label, dateTime, routeId,
                    route, routeVar, interval, note, dataSource, recordingMethod, distance, time,
                    Exercise.HEARTRATE_NONE, null,
                    false);

                boolean success = DbWriter.get(this).addExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }
            // save edit
            else {
                exercise = new Exercise(exercise.getId(), exercise.getStravaId(), exercise.getGarminId(), type,
                    label, dateTime, routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance,
                    time, exercise.getAvgHeartrate(), exercise.getTrail(), exercise.isTrailHidden());

                boolean success = DbWriter.get(this).updateExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }

            MainActivity.updateFragmentOnRestart = true;
            finish();
        }
        catch (NumberFormatException e) {
            LayoutUtils.toast(R.string.toast_err_parse, this);
        }
        catch (StringIndexOutOfBoundsException e) {
            //LayoutUtils.toast(R.string.toast_err_decode_polyline, this);
            LayoutUtils.toast(R.string.toast_err_parse, this);
        }
        catch (Exception e) {
            LayoutUtils.handleError(e, this);
        }
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
