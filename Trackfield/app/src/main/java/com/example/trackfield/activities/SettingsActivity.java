package com.example.trackfield.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.api.StravaAPI;
import com.example.trackfield.database.Helper;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.BaseDialogWithListener;
import com.example.trackfield.dialogs.instances.Color;
import com.example.trackfield.dialogs.DecimalDialog;
import com.example.trackfield.dialogs.instances.EditMass;
import com.example.trackfield.dialogs.instances.Theme;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.F;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;

import java.time.LocalDate;

public class SettingsActivity extends StravaAPI implements BaseDialogWithListener.DialogListener, DecimalDialog.DialogListener {

    private Activity a;
    private LayoutInflater inflater;
    private LinearLayout ll;

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

        connectAPI();
        handleIntent();

        // settings
        //displayListeners();
        //lookListeners();
        //profileListeners();
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent appLinkIntent = getIntent();
        //String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) finishAuthorization(appLinkData);
    }

    private void inflateViews() {

        inflateHeader("Display Options");
        inflateSwitchItem("Show hidden routes", Prefs.areHiddenRoutesShown(), false, Prefs::showHiddenRoutes);
        inflateSwitchItem("Week headers", Prefs.isWeekHeadersShown(), false, Prefs::showWeekHeaders);
        inflateSwitchItem("Daily chart", Prefs.isDailyChartShown(), false, Prefs::showDailyChart);
        //inflateSwitchItem("Week chart", Prefs.isWeekChartShown(), false, Prefs::showWeekChart);
        //inflateSwitchItem("Week chart distance", Prefs.isWeekDistanceShown(), true, Prefs::showWeekDistance);

        inflateHeader("Look");
        inflateDialogItem("Theme", Prefs.isThemeLight() ? "Light" : "Dark", false, new Theme());
        inflateDialogItem("Color", Prefs.getColor() == 0 ? "Mono" : "Green", true, new Color());

        inflateHeader("File");
        inflateClickItem("Export Json", "", false, v -> {
            F.exportJson(a);
        });
        inflateClickItem("Import Json", "", true, v -> {
            F.importJson(a);
        });

        inflateHeader("Other Services");
        inflateClickItem("Request last from Strava", "", false, v -> {
            requestLastActivity();
        });
        inflateClickItem("Request last 5 from Strava", "", false, v -> {
            requestLastActivities(5);
        });
        inflateClickItem("Request all from Strava", "", false, v -> {
            requestAllActivities();
        });
        inflateClickItem("Strava", Prefs.isRefreshTokenCurrent() ? "Connected" : "Not Connected", true, v -> {
            authorizeStrava();
        });
        //inflateClickItem("Google Fit", "Not connected", true, v -> {});

        inflateHeader("Profile");
        inflateDialogItem("Mass", Prefs.getMass() + " kg", false, new EditMass());

        final LocalDate bd = Prefs.getBirthday();
        final View birth = inflateTextView("Birthday", bd != null ? bd.format(C.FORMATTER_CAPTION) : "", true);
        birth.setOnClickListener(v -> {
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
            DatePickerDialog picker = new DatePickerDialog(a, (view, year, month, dayOfMonth) -> {
                Prefs.setBirthday(LocalDate.of(year, month+1, dayOfMonth));
                ((TextView) birth.findViewById(R.id.textView_value)).setText(bd.format(C.FORMATTER_CAPTION));
            }, yearSelect, monthSelect, daySelect);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });

        if (Prefs.isDeveloper()) {
            inflateHeader("Developer Options");
            inflateClickItem("Reboard", "", false, v -> {
                Prefs.setFirstLogin(true); BoardingActivity.startActivity(this);
            });
            inflateClickItem("Recreate database", "", false, v -> {
                Helper.getWriter(this).recreate();
            });
            inflateClickItem("Export .txt", "", false, v -> {
                F.exportTxt(a);
            });
            inflateClickItem("Import .txt", "", true, v -> {
                F.importTxt(a);
            });
        }

    }

    // inflate items
    private void inflateHeader(String title) {
        View v = inflater.inflate(R.layout.layout_settings_header, ll, false);
        ((TextView) v.findViewById(R.id.textView_sectionHeader)).setText(title);
        ll.addView(v);
    }
    private void inflateDialogItem(String title, String value, boolean hideDivider, final BaseDialog dialog) {
        View v = inflateTextView(title, value, hideDivider);
        v.setOnClickListener(v1 -> dialog.show(getSupportFragmentManager(), dialog.tag()));
    }
    private void inflateSwitchItem(String title, boolean checked, boolean hideDivider, OnSwitchListener listener) {
        View v = inflateSwitchView(title, hideDivider);
        final Switch sw = v.findViewById(R.id.switch_setting);
        sw.setChecked(checked);
        v.setOnClickListener(view -> {
            sw.setChecked(!sw.isChecked());
            listener.onSwitch(sw.isChecked());
        });
    }
    private void inflateClickItem(String title, String value, boolean hideDivider, View.OnClickListener listener) {
        View v = inflateTextView(title, value, hideDivider);
        v.setOnClickListener(listener);
    }

    private View inflateTextView(String title, String value, boolean hideDivider) {
        View v = inflater.inflate(R.layout.layout_settings_text, ll, false);
        L.ripple(v, this);
        ((TextView) v.findViewById(R.id.textView_route)).setText(title);
        ((TextView) v.findViewById(R.id.textView_value)).setText(value);
        if (hideDivider) v.findViewById(R.id.divider_setting).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }
    private View inflateSwitchView(String title, boolean hideDivider) {
        View v = inflater.inflate(R.layout.layout_settings_switch, ll, false);
        L.ripple(v, this);
        ((TextView) v.findViewById(R.id.switch_setting)).setText(title);
        if (hideDivider) v.findViewById(R.id.divider_setting).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }

    // tools
    interface OnSwitchListener {
        void onSwitch(boolean checked);
    }

    // dialog
    @Override public void doRecreate() {
        MainActivity.recreate = true;
        recreate();
    }
    @Override public void onDecimalDialogPositiveClick(float input, String tag) {
        Prefs.setMass(input);
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
        //F.savePrefs(this);
        super.onDestroy();
    }

}