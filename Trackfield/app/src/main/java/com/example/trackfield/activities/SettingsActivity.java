package com.example.trackfield.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.trackfield.R;
import com.example.trackfield.fragments.dialogs.Dialogs;
import com.example.trackfield.toolbox.Toolbox;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.F;
import com.example.trackfield.toolbox.Toolbox.M;

import java.time.LocalDate;

public class SettingsActivity extends AppCompatActivity implements Dialogs.BaseWithListener.DialogListener, Dialogs.DecimalDialog.DialogListener {

    private Activity a;

    private static final String TAG_UPDATE_MASS = "updateMass";

    ////

    public static void startActivity(Context c) {
        c.startActivity(new Intent(c, SettingsActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        a = this;
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar();
        setTexts();

        // settings
        fileListeners();
        displayListeners();
        lookListeners();
        profileListeners();
    }

    private void setTexts() {

        TextView totalDistanceTv = findViewById(R.id.textView_totalDistance);
        TextView totalTimeTv = findViewById(R.id.textView_totalTime);
        TextView totalActivitiesTv = findViewById(R.id.textView_totalActivities);

        totalDistanceTv.setText(D.totalDistance / 1000 + " km");
        totalTimeTv.setText(M.round(D.totalTime, 1) + " h");
        totalActivitiesTv.setText(D.exercises.size() + " activities");
    }

    // listeners
    private void fileListeners() {

        // save
        Button saveBtn = findViewById(R.id.button_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                F.saveExternal(a);
                F.savePrefs(a);
                Toast.makeText(a, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        // load
        Button loadBtn = findViewById(R.id.button_load);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                F.loadExternal(a);
                F.loadPrefs(a);
                Toast.makeText(a, "Loaded", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        // import routes
        Button getRoutesBtn = findViewById(R.id.button_importRoutes);
        getRoutesBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                D.importRoutes();
                Toast.makeText(a, "imported", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void displayListeners() {

        // lesser routes
        final Switch lesserSw = findViewById(R.id.switch_showLesserRoutes);
        lesserSw.setChecked(D.showLesserRoutes);
        ConstraintLayout lesserCl = findViewById(R.id.constraintLayout_lesserRoutes);
        lesserCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                lesserSw.setChecked(!lesserSw.isChecked());
                D.showLesserRoutes = lesserSw.isChecked();
            }
        });

        // week headers
        final Switch wHeadersSw = findViewById(R.id.switch_showWeekHeaders);
        wHeadersSw.setChecked(D.showWeekHeaders);
        ConstraintLayout wHeadersCl = findViewById(R.id.constraintLayout_weekHeaders);
        wHeadersCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                wHeadersSw.setChecked(!wHeadersSw.isChecked());
                D.showWeekHeaders = wHeadersSw.isChecked();
            }
        });

        // daily chart
        final Switch dailySw = findViewById(R.id.switch_showDailyChart);
        dailySw.setChecked(D.showDailyChart);
        ConstraintLayout dailyCl = findViewById(R.id.constraintLayout_dailyChart);
        dailyCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dailySw.setChecked(!dailySw.isChecked());
                D.showDailyChart = dailySw.isChecked();
            }
        });

        // weekly chart
        final Switch weeklySw = findViewById(R.id.switch_showWeekChart);
        weeklySw.setChecked(D.showWeekChart);
        ConstraintLayout weeklyCl = findViewById(R.id.constraintLayout_weekChart);
        weeklyCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                weeklySw.setChecked(!weeklySw.isChecked());
                D.showWeekChart = weeklySw.isChecked();
            }
        });

        // weekly chart distance
        final Switch weeklyDistanceSw = findViewById(R.id.switch_weekDistance);
        weeklyDistanceSw.setChecked(D.weekDistance);
        ConstraintLayout weeklyDistanceCl = findViewById(R.id.constraintLayout_weekDistance);
        weeklyDistanceCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                weeklyDistanceSw.setChecked(!weeklyDistanceSw.isChecked());
                D.weekDistance = weeklyDistanceSw.isChecked();
            }
        });

        // weekly chart weeks
        TextView weeklyWeeksTv = findViewById(R.id.textView_weeksState);
        weeklyWeeksTv.setText(D.weekAmount + "");
        ConstraintLayout weeklyWeeksCl = findViewById(R.id.constraintLayout_weeks);
        weeklyWeeksCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Dialogs.WeekChartWeeks dialog = new Dialogs.WeekChartWeeks();
                dialog.show(getSupportFragmentManager(), dialog.TAG);
            }
        });

    }
    private void lookListeners() {

        // theme
        TextView themeTv = findViewById(R.id.textView_lightThemeState);
        if (D.theme) { themeTv.setText("Light"); }
        else { themeTv.setText("Dark"); }
        ConstraintLayout themeCl = findViewById(R.id.constraintLayout_lightTheme);
        themeCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Dialogs.Theme dialog = new Dialogs.Theme();
                dialog.show(getSupportFragmentManager(), dialog.TAG);
            }
        });


        // color
        TextView colorTv = findViewById(R.id.textView_colorThemeState);
        if (D.color == 0) { colorTv.setText("Mono"); }
        else { colorTv.setText("Green"); }
        ConstraintLayout colorCl = findViewById(R.id.constraintLayout_colorTheme);
        colorCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Dialogs.Color dialog = new Dialogs.Color();
                dialog.show(getSupportFragmentManager(), dialog.TAG);
            }
        });

    }
    private void profileListeners() {

        // mass
        TextView massTv = findViewById(R.id.textView_massState);
        massTv.setText(D.mass + " kg");
        ConstraintLayout massCl = findViewById(R.id.constraintLayout_mass);
        massCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Dialogs.EditMass.newInstance(D.mass, getSupportFragmentManager());
            }
        });

        // birthday
        final TextView birthdayTv = findViewById(R.id.textView_birthdayState);
        if (D.birthday != null) { birthdayTv.setText(D.birthday.format(Toolbox.C.FORMATTER_CAPTION)); }
        ConstraintLayout birthdatCl = findViewById(R.id.constraintLayout_birthday);
        birthdatCl.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int yearSelect, monthSelect, daySelect;
                if (D.birthday == null) {
                    yearSelect = 1970;
                    monthSelect = 0;
                    daySelect = 1;
                }
                else {
                    yearSelect = D.birthday.getYear();
                    monthSelect = D.birthday.getMonthValue()-1;
                    daySelect = D.birthday.getDayOfMonth();
                }
                DatePickerDialog dpd = new DatePickerDialog(a, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        D.birthday = LocalDate.of(year, month+1, dayOfMonth);
                        birthdayTv.setText(D.birthday.format(Toolbox.C.FORMATTER_CAPTION));
                    }
                }, yearSelect, monthSelect, daySelect);
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
        });
    }

    // dialog
    @Override public void doRecreate() {
        MainActivity.recreate = true;
        recreate();
    }
    @Override public void onDecimalDialogPositiveClick(float input, String tag) {
        D.mass = input;
    }

    // toolbar
    private void toolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_settings);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.fragment_settings));
        ab.setDisplayHomeAsUpEnabled(true);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_settings, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default: return super.onOptionsItemSelected(item);
        }

    }

}