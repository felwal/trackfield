package com.example.trackfield.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.database.ApiManager;
import com.example.trackfield.fragments.dialogs.Dialogs;
import com.example.trackfield.toolbox.Toolbox;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.F;
import com.example.trackfield.toolbox.Toolbox.M;

import java.time.LocalDate;

public class SettingsActivity extends ApiManager implements Dialogs.BaseWithListener.DialogListener, Dialogs.DecimalDialog.DialogListener {

    private Activity a;
    private LayoutInflater inflater;
    private LinearLayout ll;

    private final static String TAG_LESSER_ROUTES = "lesserRoutes";
    private final static String TAG_WEEK_HEADERS = "weekHeaders";
    private final static String TAG_DAILY_CHARTS = "dailyChart";
    private final static String TAG_WEEK_CHART = "weekChart";
    private final static String TAG_WEEK_CHART_DISTANCE = "weekChartDistance";

    private final static String TAG_STRAVA = "strava";
    private final static String TAG_GOOGLE_FIT = "googleFit";

    ////

    public static void startActivity(Context c) {
        c.startActivity(new Intent(c, SettingsActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        a = this;
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        inflater = getLayoutInflater();
        ll = findViewById(R.id.linearLayout_settings);

        toolbar();
        inflateViews();
        fileView();

        connectAPIs();

        // settings
        //displayListeners();
        //lookListeners();
        //profileListeners();
    }

    private void fileView() {

        // stats
        TextView totalDistanceTv = findViewById(R.id.textView_totalDistance);
        TextView totalTimeTv = findViewById(R.id.textView_totalTime);
        TextView totalActivitiesTv = findViewById(R.id.textView_totalActivities);
        totalDistanceTv.setText(D.totalDistance / 1000 + " km");
        totalTimeTv.setText(M.round(D.totalTime, 1) + " h");
        totalActivitiesTv.setText(D.exercises.size() + " activities");

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
    private void inflateViews() {

        inflateHeader("Display Options");
        inflateSwitchItem("Show lesser routes", D.prefs.showLesserRoutes(), false, TAG_LESSER_ROUTES);
        inflateSwitchItem("Week headers", D.prefs.showWeekHeaders(), false, TAG_WEEK_HEADERS);
        inflateSwitchItem("Daily chart", D.prefs.showDailyChart(), false, TAG_DAILY_CHARTS);
        inflateSwitchItem("Week chart", D.prefs.showWeekChart(), false, TAG_WEEK_CHART);
        inflateSwitchItem("Week chart distance", D.prefs.showWeekDistance(), true, TAG_WEEK_CHART_DISTANCE);

        inflateHeader("Look");
        inflateDialogItem("Theme", D.prefs.isThemeLight() ? "Light" : "Dark", false, new Dialogs.Theme());
        inflateDialogItem("Color", D.prefs.getColor() == 0 ? "Mono" : "Green", true, new Dialogs.Color());

        inflateHeader("Other Services");
        inflateClickItem("Strava", "Connected", false, TAG_STRAVA);
        inflateClickItem("Google Fit", "Not connected", true, TAG_GOOGLE_FIT);

        inflateHeader("Profile");
        inflateDialogItem("Mass", D.prefs.getMass() + " kg", false, new Dialogs.EditMass());

        final LocalDate bd = D.prefs.getBirthday();
        final View birth = inflateTextItem("Birthday", bd != null ? bd.format(Toolbox.C.FORMATTER_CAPTION) : "", true);
        birth.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int yearSelect, monthSelect, daySelect;
                if (bd == null) {
                    yearSelect = 1970;
                    monthSelect = 0;
                    daySelect = 1;
                }
                else {
                    yearSelect = bd.getYear();
                    monthSelect = bd.getMonthValue()-1;
                    daySelect = bd.getDayOfMonth();
                }
                DatePickerDialog picker = new DatePickerDialog(a, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        D.prefs.setBirthday(LocalDate.of(year, month+1, dayOfMonth));
                        ((TextView) birth.findViewById(R.id.textView_value)).setText(bd.format(Toolbox.C.FORMATTER_CAPTION));
                    }
                }, yearSelect, monthSelect, daySelect);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.show();
            }
        });


    }

    // inflate items
    private void inflateHeader(String title) {
        View v = inflater.inflate(R.layout.layout_settings_header, ll, false);
        ((TextView) v.findViewById(R.id.textView_sectionHeader)).setText(title);
        ll.addView(v);
    }
    private void inflateDialogItem(String title, String value, boolean hideDivider, final Dialogs.Base dialog) {
        View v = inflateTextItem(title, value, hideDivider);
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.show(getSupportFragmentManager(), dialog.tag());
            }
        });
    }
    private void inflateSwitchItem(String title, boolean checked, boolean hideDivider, final String tag) {
        View v = inflateSwitchItem(title, hideDivider);
        final Switch sw = v.findViewById(R.id.switch_setting);
        sw.setChecked(checked);
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                sw.setChecked(!sw.isChecked());
                itemSwitched(sw.isChecked(), tag);
            }
        });
    }
    private void inflateClickItem(String title, String value, boolean hideDivider, final String tag) {
        View v = inflateTextItem(title, value, hideDivider);
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                itemClicked(tag);
            }
        });
    }

    private View inflateTextItem(String title, String value, boolean hideDivider) {
        View v = inflater.inflate(R.layout.layout_settings_text, ll, false);
        Toolbox.L.ripple(v, this);
        ((TextView) v.findViewById(R.id.textView_title)).setText(title);
        ((TextView) v.findViewById(R.id.textView_value)).setText(value);
        if (hideDivider) v.findViewById(R.id.divider_setting).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }
    private View inflateSwitchItem(String title, boolean hideDivider) {
        View v = inflater.inflate(R.layout.layout_settings_switch, ll, false);
        Toolbox.L.ripple(v, this);
        ((TextView) v.findViewById(R.id.switch_setting)).setText(title);
        if (hideDivider) v.findViewById(R.id.divider_setting).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }

    // tools
    private void itemSwitched(boolean checked, String tag) {
        switch (tag) {
            case TAG_LESSER_ROUTES: D.prefs.setShowLesserRoutes(checked); break;
            case TAG_WEEK_HEADERS: D.prefs.setShowWeekHeaders(checked); break;
            case TAG_DAILY_CHARTS: D.prefs.setShowDailyChart(checked); break;
            case TAG_WEEK_CHART: D.prefs.setShowWeekChart(checked); break;
            case TAG_WEEK_CHART_DISTANCE: D.prefs.setWeekDistance(checked); break;
        }
    }
    private void itemClicked(String tag) {
        switch (tag) {
            case TAG_STRAVA: authenticateStrava(); break;
            case TAG_GOOGLE_FIT: break;
        }
    }

    // dialog
    @Override public void doRecreate() {
        MainActivity.recreate = true;
        recreate();
    }
    @Override public void onDecimalDialogPositiveClick(float input, String tag) {
        D.prefs.setMass(input);
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

    @Override protected void onDestroy() {
        F.savePrefs(this);
        super.onDestroy();
    }

}