package com.example.trackfield.ui.exercise;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.data.db.model.Sub;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.utils.ScreenUtils;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.annotations.Unimplemented;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class EditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
    BinaryDialog.DialogListener {

    // extras names
    private static final String EXTRA_ID = "id";

    // dialog tags
    private static final String DIALOG_DISCARD = "discardDialog";

    private EditText routeEt;
    private EditText routeVarEt;
    private EditText intervalEt;
    private EditText noteEt;
    private EditText distanceEt;
    private EditText hoursEt;
    private EditText minutesEt;
    private EditText secondsEt;
    private EditText dataSourceEt;
    private EditText recordingMethodEt;
    private EditText dateEt;
    private EditText timeEt;
    private Switch drivenSw;
    private Spinner typeSpinner;
    @Unimplemented private final ArrayList<View> subViews = new ArrayList<>();

    private String creationDate;
    private String creationTime;

    // arguments
    private int exerciseId;
    private Exercise exercise;
    private boolean edit;

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), EditActivity.class);
        c.startActivity(startIntent);
    }

    public static void startActivity(Context c, int exerciseId) {
        Intent startIntent = new Intent(c.getApplicationContext(), EditActivity.class);
        startIntent.putExtra(EXTRA_ID, exerciseId);
        c.startActivity(startIntent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setToolbar();

        // extras
        exerciseId = getIntent().getIntExtra(EXTRA_ID, -1);
        edit = exerciseId != -1;

        findEditTexts();
        setTexts();
        //setAddSubBtnListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_edit, menu);
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

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_edit);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.activity_title_edit));
        ab.setDisplayHomeAsUpEnabled(true);

        // set cancel icon as home
        Drawable homeIcon = getDrawable(R.drawable.ic_cancel).mutate();
        homeIcon.setColorFilter(LayoutUtils.getColorInt(R.attr.colorOnPrimary, this), PorterDuff.Mode.SRC_IN);
        ab.setHomeAsUpIndicator(homeIcon);
    }

    private void findEditTexts() {
        // edittexts
        routeEt = findViewById(R.id.et_route);
        routeVarEt = findViewById(R.id.et_routeVar);
        intervalEt = findViewById(R.id.et_interval);
        dateEt = findViewById(R.id.et_date);
        timeEt = findViewById(R.id.et_time);
        noteEt = findViewById(R.id.et_note);
        distanceEt = findViewById(R.id.et_edit_distance);
        hoursEt = findViewById(R.id.et_edit_hours);
        minutesEt = findViewById(R.id.et_edit_minutes);
        secondsEt = findViewById(R.id.et_edit_seconds);
        //polylineEt = findViewById(R.id.editText_polyline);
        dataSourceEt = findViewById(R.id.et_edit_data_source);
        recordingMethodEt = findViewById(R.id.et_edit_recording_method);
        drivenSw = findViewById(R.id.sw_edit_drive_distance);
        typeSpinner = findViewById(R.id.spn_edit_type);

        // spinner
        ArrayAdapter<String> spinnerAdapter =
            new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Exercise.TYPES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerAdapter);
        typeSpinner.setOnItemSelectedListener(this);
    }

    private void setTexts() {
        if (edit) setTextsEdit();
        else setTextsCreate();
        setListeners();
    }

    private void setTextsCreate() {
        dateEt.setText(creationDate = LocalDate.now().format(AppConsts.FORMATTER_EDIT_DATE));
        timeEt.setText(creationTime = LocalDateTime.now().format(AppConsts.FORMATTER_EDIT_TIME));
    }

    private void setTextsEdit() {
        exercise = DbReader.get(this).getExercise(exerciseId);

        // subs
        for (int i = 0; i < exercise.subCount(); i++) {
            Sub sub = exercise.getSub(i);

            // add views
            LinearLayout ll = findViewById(R.id.ll_edit);
            View subView = getLayoutInflater().inflate(R.layout.item_edit_sub, ll, false);
            ll.addView(subView, ll.getChildCount() - 1);
            subViews.add(subView);
            setRemoveSubBtnListener(ll, subView);

            EditText sDistanceEt = subViews.get(i).findViewById(R.id.et_edit_item_sub_distance);
            EditText sHoursEt = subViews.get(i).findViewById(R.id.et_edit_item_sub_hours);
            EditText sMinutesEt = subViews.get(i).findViewById(R.id.et_edit_item_sub_minutes);
            EditText sSecondsEt = subViews.get(i).findViewById(R.id.et_edit_item_sub_seconds);

            float[] time = MathUtils.getTimeParts(sub.getTime());

            sDistanceEt.setText((float) sub.getDistance() / 1000 + "");
            sHoursEt.setText((int) time[2] + "");
            sMinutesEt.setText((int) time[1] + "");
            sSecondsEt.setText(time[0] + "");
        }

        float[] time = MathUtils.getTimeParts(exercise.getTime());

        // set texts
        routeEt.setText(exercise.getRoute());
        routeVarEt.setText(exercise.getRouteVar());
        dateEt.setText(exercise.getDate().format(AppConsts.FORMATTER_EDIT_DATE));
        timeEt.setText(exercise.getDateTime().format(AppConsts.FORMATTER_EDIT_TIME));
        noteEt.setText(exercise.getNote());
        distanceEt.setText((float) exercise.getEffectiveDistance(this) / 1000 + "");
        hoursEt.setText((int) time[2] + "");
        minutesEt.setText((int) time[1] + "");
        secondsEt.setText(time[0] + "");
        dataSourceEt.setText(exercise.getDataSource());
        recordingMethodEt.setText(exercise.getRecordingMethod());
        drivenSw.setChecked(exercise.isDistanceDriven());
        typeSpinner.setSelection(exercise.getType());

        if (exercise.isDistanceDriven()) {
            distanceEt.setEnabled(false);
            distanceEt.setTextColor(LayoutUtils.getColorInt(android.R.attr.textColorSecondary, this));
        }

        // polyline
        /*if (exercise.hasTrail()) {
            polylineEt.setText(exercise.getTrail().getPolyline());
            polylineEt.setFocusable(false);
        }*/

        // set or hide interval
        if (exercise.isType(Exercise.TYPE_INTERVALS)) {
            intervalEt.setText(exercise.getInterval());
        }
        else intervalEt.setVisibility(View.GONE);
    }

    private void setListeners() {
        // distance driven listener
        drivenSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            distanceEt.setEnabled(!isChecked);
            distanceEt.setTextColor(LayoutUtils.getColorInt(isChecked ? android.R.attr.textColorSecondary
                : android.R.attr.textColorPrimary, this));
        });

        // date listener
        dateEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            LocalDate dateSelect = LocalDate.parse(dateEt.getText(), AppConsts.FORMATTER_EDIT_DATE);

            DatePickerDialog dialog = new DatePickerDialog(EditActivity.this, (picker, year, month, dayOfMonth) ->
                dateEt.setText(LocalDate.of(year, month + 1, dayOfMonth).format(AppConsts.FORMATTER_EDIT_DATE)),
                dateSelect.getYear(), dateSelect.getMonthValue() - 1, dateSelect.getDayOfMonth());

            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
            dateEt.clearFocus();
        });

        // time listener
        timeEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            LocalTime timeSelect = LocalTime.parse(timeEt.getText(), AppConsts.FORMATTER_EDIT_TIME);

            TimePickerDialog dialog = new TimePickerDialog(EditActivity.this, (picker, hour, minute) ->
                timeEt.setText(LocalTime.of(hour, minute, 0).format(AppConsts.FORMATTER_EDIT_TIME)),
                timeSelect.getHour(), timeSelect.getMinute(), true);

            dialog.show();
            timeEt.clearFocus();
        });

    }

    // get

    private boolean haveEditsBeenMade() {
        if (edit) {
            float[] time = MathUtils.getTimeParts(exercise.getTime());

            return !routeEt.getText().toString().equals(exercise.getRoute())
                || !routeVarEt.getText().toString().equals(exercise.getRouteVar())
                || !dateEt.getText().toString().equals(exercise.getDate().format(AppConsts.FORMATTER_EDIT_DATE))
                || !timeEt.getText().toString().equals(exercise.getDateTime().format(AppConsts.FORMATTER_EDIT_TIME))
                || !noteEt.getText().toString().equals(exercise.getNote())
                || !distanceEt.getText().toString().equals((float) exercise.getEffectiveDistance(this) / 1000 + "")
                || !hoursEt.getText().toString().equals((int) time[2] + "")
                || !minutesEt.getText().toString().equals((int) time[1] + "")
                || !secondsEt.getText().toString().equals(time[0] + "")
                || !dataSourceEt.getText().toString().equals(exercise.getDataSource())
                || !recordingMethodEt.getText().toString().equals(exercise.getRecordingMethod())
                || typeSpinner.getSelectedItemPosition() != exercise.getType()
                || drivenSw.isChecked() != exercise.isDistanceDriven();
        }
        else {
            return !routeEt.getText().toString().equals("Route")
                || !routeVarEt.getText().toString().equals("")
                || !dateEt.getText().toString().equals(creationDate)
                || !timeEt.getText().toString().equals(creationTime)
                || !noteEt.getText().toString().equals("")
                || !distanceEt.getText().toString().equals("0")
                || !hoursEt.getText().toString().equals("0")
                || !minutesEt.getText().toString().equals("0")
                || !secondsEt.getText().toString().equals("0")
                || !dataSourceEt.getText().toString().equals("")
                || !recordingMethodEt.getText().toString().equals("")
                || typeSpinner.getSelectedItemPosition() != 0
                || drivenSw.isChecked();

        }
    }

    private void showDiscardDialog() {
        BinaryDialog.newInstance(R.string.dialog_title_discard, BaseDialog.NO_RES, R.string.dialog_btn_discard,
            DIALOG_DISCARD)
            .show(getSupportFragmentManager());
    }

    // parse

    private void parseAndSave() {
        try {
            // parse
            String route = routeEt.getText().toString();
            String routeVar = routeVarEt.getText().toString();
            LocalDate date = LocalDate.parse(dateEt.getText(), AppConsts.FORMATTER_EDIT_DATE);
            LocalTime localTime = LocalTime.parse(timeEt.getText(), AppConsts.FORMATTER_EDIT_TIME);
            String note = noteEt.getText().toString();
            int distance = !drivenSw.isChecked() ? (int) (Float.parseFloat(distanceEt.getText().toString()) * 1000)
                : Exercise.DISTANCE_DRIVEN;
            int hours = Integer.parseInt(hoursEt.getText().toString());
            int minutes = Integer.parseInt(minutesEt.getText().toString());
            float seconds = Float.parseFloat(secondsEt.getText().toString());
            float time = hours * 3600 + minutes * 60 + seconds;
            String dataSource = dataSourceEt.getText().toString();
            String recordingMethod = recordingMethodEt.getText().toString();
            int type = typeSpinner.getSelectedItemPosition();
            ArrayList<Sub> subs = parseSubs();
            String interval = type == Exercise.TYPE_INTERVALS ? intervalEt.getText().toString() : "";

            int routeId = DbReader.get(this).getRouteIdOrCreate(route, this);
            LocalDateTime dateTime = LocalDateTime.of(date, localTime);

            // save create
            if (exercise == null) {
                // trail
                /*Trail trail = null;
                String polyline = polylineEt.getText().toString();
                if (!polyline.equals("")) {
                    trail = new Trail(PolyUtil.decode(polyline));
                }*/

                exercise = new Exercise(Exercise.NO_ID, Exercise.NO_ID, Exercise.NO_ID, type, dateTime, routeId,
                    route, routeVar, interval, note, dataSource, recordingMethod, distance, time, subs, null);

                boolean success = DbWriter.get(this).addExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }
            // save edit
            else {
                exercise = new Exercise(exercise.getId(), exercise.getStravaId(), exercise.getGarminId(), type,
                    dateTime, routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time,
                    subs, exercise.getTrail());

                boolean success = DbWriter.get(this).updateExercise(exercise, this);
                LayoutUtils.toast(success, this);
            }

            finish();
        }
        catch (NumberFormatException e) {
            LayoutUtils.toast(R.string.toast_err_save_empty, this);
        }
        catch (StringIndexOutOfBoundsException e) {
            LayoutUtils.toast(R.string.toast_err_decode_polyline, this);
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

            EditText sDistanceEt = v.findViewById(R.id.et_edit_item_sub_distance);
            EditText sHoursEt = v.findViewById(R.id.et_edit_item_sub_hours);
            EditText sMinutesEt = v.findViewById(R.id.et_edit_item_sub_minutes);
            EditText sSecondsEt = v.findViewById(R.id.et_edit_item_sub_seconds);

            int distance = (int) (Float.parseFloat(sDistanceEt.getText().toString()) * 1000);
            int hours = Integer.parseInt(sHoursEt.getText().toString());
            int minutes = Integer.parseInt(sMinutesEt.getText().toString());
            float seconds = Float.parseFloat(sSecondsEt.getText().toString());
            float time = hours * 3600 + minutes * 60 + seconds;
            int _subId = (exercise != null && i < exercise.subCount()) ? exercise.getSub(i).getId() : -1;

            subs.add(new Sub(_subId, exerciseId, distance, time));
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
        final Button addSubBtn = findViewById(R.id.btn_edit_add_sub);
        addSubBtn.setOnClickListener(v -> {
            final LinearLayout ll = findViewById(R.id.ll_edit);
            final View subView = getLayoutInflater().inflate(R.layout.item_edit_sub, ll, false);
            ll.addView(subView, ll.getChildCount() - 1);
            subViews.add(subView);

            setRemoveSubBtnListener(ll, subView);
        });
    }

    @Unimplemented
    private void setRemoveSubBtnListener(final LinearLayout ll, final View subView) {
        final Button removeBtn = subView.findViewById(R.id.btn_edit_item_sub_remove);
        removeBtn.setOnClickListener(v -> {
            ll.removeView(subView);
            subViews.remove(subView);
        });
    }

    // implements AdapterView

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        // type spinner adapter
        LayoutUtils.setVisibleOrGone(intervalEt, pos == Exercise.TYPE_INTERVALS);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // type spinner adapter
        LayoutUtils.setVisibleOrGone(intervalEt, false);
    }

    // implements BinaryDialog

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_DISCARD)) finish();
    }

}
