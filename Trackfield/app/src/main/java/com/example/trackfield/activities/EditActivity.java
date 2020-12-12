package com.example.trackfield.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Map;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.Toolbox.C;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.L;
import com.example.trackfield.toolbox.Toolbox.M;

import java.time.LocalDate;
import java.util.ArrayList;

public class EditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Exercise exercise;
    private int _id;
    private boolean driveDistance = false;
    private boolean edit;

    private TextView sTv;
    private EditText routeEt, routeVarEt, intervalEt, noteEt, distanceEt, hoursEt, minutesEt, secondsEt, dataSourceEt, recordingMethodEt, dateEt;
    private Spinner typeSpinner;
    private ArrayList<View> subViews = new ArrayList<>();

    // extras
    public static final String EXTRA_ID = "_id";

    ////

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), EditActivity.class);
        c.startActivity(startIntent);
    }
    public static void startActivity(Context c, int _id) {
        Intent startIntent = new Intent(c.getApplicationContext(), EditActivity.class);
        startIntent.putExtra(EXTRA_ID, _id);
        c.startActivity(startIntent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setToolbar();

        // extras
        _id = getIntent().getIntExtra(EXTRA_ID, -1);
        edit = _id != -1;

        //writer = new Helper.Writer(this);

        findEditTexts();
        setTexts();
        addSubViewBtnListener();
    }

    private void findEditTexts() {

        routeEt             = findViewById(R.id.editText_route);
        routeVarEt          = findViewById(R.id.editText_routeVar);
        intervalEt          = findViewById(R.id.editText_interval);
        dateEt              = findViewById(R.id.editText_date);
        noteEt              = findViewById(R.id.editText_note);
        distanceEt          = findViewById(R.id.editText_distance);
        hoursEt             = findViewById(R.id.editText_hours);
        minutesEt           = findViewById(R.id.editText_minutes);
        secondsEt           = findViewById(R.id.editText_seconds);
        dataSourceEt        = findViewById(R.id.editText_dataSource);
        recordingMethodEt   = findViewById(R.id.editText_recordingMethod);
        typeSpinner         = findViewById(R.id.spinner_type);
        sTv                 = findViewById(R.id.textView_s);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Exercise.TYPES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerAdapter);
        typeSpinner.setOnItemSelectedListener(this);

    }

    // set texts
    private void setTexts() {

        if (edit) setTextsEdit();
        else setTextsCreate();

        driveDistanceTvListener();
        dateEtListener();
    }
    private void setTextsCreate() {

        //_id = D.exercises.size();
        dateEt.setText(LocalDate.now().format(C.FORMATTER_EDIT));
    }
    private void setTextsEdit() {

        //exercise = D.exercises.get(id);
        exercise = Helper.getReader(this).getExercise(_id);

        // subs
        for (int i = 0; i < exercise.subCount(); i++) {
            Sub sub = exercise.getSub(i);

            // add views
            final View subView = getLayoutInflater().inflate(R.layout.layout_sub_edit, null);
            final LinearLayout ll = findViewById(R.id.linearLayout_edit);
            ll.addView(subView, ll.getChildCount()-1);
            subViews.add(subView);
            removeSubViewBtnListener(ll, subView);

            final EditText sDistanceEt  = subViews.get(i).findViewById(R.id.editText_distance_sub);
            final EditText sHoursEt     = subViews.get(i).findViewById(R.id.editText_hours_sub);
            final EditText sMinutesEt   = subViews.get(i).findViewById(R.id.editText_minutes_sub);
            final EditText sSecondsEt   = subViews.get(i).findViewById(R.id.editText_seconds_sub);

            float[] time = M.getTimeParts(sub.getTime());

            sDistanceEt .setText((float) sub.getDistance() / 1000 + "");
            sHoursEt    .setText((int) time[2] + "");
            sMinutesEt  .setText((int) time[1] + "");
            sSecondsEt  .setText(time[0] + "");
        }

        float[] time = M.getTimeParts(exercise.getTimePrimary());

        // set texts
        routeEt             .setText(exercise.getRoute());
        routeVarEt          .setText(exercise.getRouteVar());
        dateEt              .setText(exercise.getDate().format(C.FORMATTER_EDIT));
        noteEt              .setText(exercise.getNote());
        distanceEt          .setText((float) exercise.getDistancePrimary() / 1000 + "");
        hoursEt             .setText((int) time[2] + "");
        minutesEt           .setText((int) time[1] + "");
        secondsEt           .setText(time[0] + "");
        dataSourceEt        .setText(exercise.getDataSource());
        recordingMethodEt   .setText(exercise.getRecordingMethod());
        typeSpinner         .setSelection(exercise.getType());

        // show or hide interval
        if (exercise.isType(Exercise.TYPE_INTERVALS)) {
            intervalEt.setVisibility(View.VISIBLE);
            intervalEt.setText(exercise.getInterval());
        }
        else intervalEt.setVisibility(View.GONE);

        if (exercise.isDistanceDriven()) {
            distanceEt.setEnabled(false);
            sTv.setText("s.");
            driveDistance = true;
        }
    }

    // parse
    private void parseAndSave() {

        try {

            // parse
            String route            = routeEt.getText().toString();
            String routeVar         = routeVarEt.getText().toString();
            LocalDate date          = LocalDate.parse(dateEt.getText(), C.FORMATTER_EDIT);
            String note             = noteEt.getText().toString();
            int distance            = !driveDistance ? (int) (Float.parseFloat(distanceEt.getText().toString()) * 1000) : Exercise.DISTANCE_DRIVEN;
            int hours               = Integer.parseInt(hoursEt.getText().toString());
            int minutes             = Integer.parseInt(minutesEt.getText().toString());
            float seconds           = Float.parseFloat(secondsEt.getText().toString());
            float time              = hours*3600 + minutes*60 + seconds;
            String dataSource       = dataSourceEt.getText().toString();
            String recordingMethod  = recordingMethodEt.getText().toString();
            int type                = typeSpinner.getSelectedItemPosition();
            ArrayList<Sub> subs     = parseSubs();
            String interval         = type == Exercise.TYPE_INTERVALS ? intervalEt.getText().toString() : "";

            // new route
            boolean newRoute  = true;
            if (type != Exercise.TYPE_RUN) { /*newRoute = false;*/ }
            else {
                for (int id = 0; id < D.routes.size(); id++) {
                    if (D.routes.get(id).equalsIgnoreCase(route)) {
                        newRoute = false;
                    }
                }
            }
            if (newRoute) {
                D.routes.add(route);
                D.sortRoutesData();
            }

            // routeId
            //Helper.Reader reader = new Helper.Reader(this);
            int routeId = Helper.getReader(this).getRouteIdOrCreate(route, this);
            //reader.close();

            // save
            if (exercise == null) {
                exercise = new Exercise(-1, D.exercises.size(), type, M.dateTime(date), routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time, subs, (Trail) null);
                L.toast(Helper.getWriter(this).addExercise(exercise, this), this);
                D.exercises.add(exercise);
            }
            else {
                exercise = new Exercise(exercise.get_id(), exercise.getId(), type, M.dateTime(date), routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time, subs, exercise.getTrail());
                L.toast(Helper.getWriter(this).updateExercise(exercise), this);
                D.exercises.set(exercise.getId(), exercise);
            }

            D.edited();
            finish();
        }
        catch (NumberFormatException e) { L.toast("Can't save empty", this); }
        catch (Exception e) { L.handleError(e, this); }
    }
    private ArrayList<Sub> parseSubs() {

        ArrayList<Sub> subs = new ArrayList<>();

        // add
        for (int i = 0; i < subViews.size(); i++) {
            View v = subViews.get(i);

            EditText sDistanceEt    = v.findViewById(R.id.editText_distance_sub);
            EditText sHoursEt       = v.findViewById(R.id.editText_hours_sub);
            EditText sMinutesEt     = v.findViewById(R.id.editText_minutes_sub);
            EditText sSecondsEt     = v.findViewById(R.id.editText_seconds_sub);

            int distance    = (int) (Float.parseFloat(sDistanceEt.getText().toString()) * 1000);
            int hours       = Integer.parseInt(sHoursEt.getText().toString());
            int minutes     = Integer.parseInt(sMinutesEt.getText().toString());
            float seconds   = Float.parseFloat(sSecondsEt.getText().toString());
            float time      = hours*3600 + minutes*60 + seconds;
            int _subId      = (exercise != null && i < exercise.subCount()) ? exercise.getSub(i).get_id() : -1;

            subs.add(new Sub(_subId, _id, distance, time));
        }

        // delete
        for (int i = 0; exercise != null && i < exercise.subCount(); i++) {
            if (i >= subs.size()) { Helper.getWriter(this).deleteSub(exercise.getSub(i)); }
        }

        return subs;
    }

    // listeners
    private void addSubViewBtnListener() {

        final Button addSubBtn = findViewById(R.id.button_addSub);
        addSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final View subView = getLayoutInflater().inflate(R.layout.layout_sub_edit, null);
                final LinearLayout ll = findViewById(R.id.linearLayout_edit);
                ll.addView(subView, ll.getChildCount()-1);
                subViews.add(subView);

                removeSubViewBtnListener(ll, subView);
            }
        });
    }
    private void removeSubViewBtnListener(final LinearLayout ll, final View subView) {

        final Button removeBtn = subView.findViewById(R.id.button_removeSub);
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ll.removeView(subView);
                subViews.remove(subView);
            }
        });
    }
    private void driveDistanceTvListener() {

        sTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!driveDistance) {
                    distanceEt.setEnabled(false);
                    sTv.setText("s.");
                    driveDistance = true;
                }
                else {
                    distanceEt.setEnabled(true);
                    sTv.setText("s");
                    driveDistance = false;
                }
            }
        });
    }
    private void dateEtListener() {

        dateEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) return;
                LocalDate dateSelect = LocalDate.parse(dateEt.getText(), C.FORMATTER_EDIT);

                DatePickerDialog dialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateEt.setText(LocalDate.of(year, month+1, dayOfMonth).format(C.FORMATTER_EDIT));
                    }
                }, dateSelect.getYear(), dateSelect.getMonthValue()-1, dateSelect.getDayOfMonth());

                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dialog.show();
                dateEt.clearFocus();
            }
        });
    }

    // toolbar
    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_edit);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.activity_edit));
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_cancel_24dp);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_edit, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_save:
                parseAndSave();
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override protected void onDestroy() {
        //writer.close();
        //Helper.closeReader();
        //Helper.closeWriter();
        super.onDestroy();
    }

    // spinner
    @Override public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        L.setVisibleOrGone(intervalEt, pos == Exercise.TYPE_INTERVALS);
    }
    @Override public void onNothingSelected(AdapterView<?> adapterView) {
        L.setVisibleOrGone(intervalEt, false);
    }

}
