package com.example.trackfield.ui.setting;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.DecimalDialog;
import com.example.trackfield.ui.custom.dialog.RadioDialog;
import com.example.trackfield.ui.main.MainActivity;
import com.example.trackfield.ui.onboarding.OnboardingActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.FileUtils;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.ScreenUtils;

import java.time.LocalDate;

public class SettingsActivity extends AppCompatActivity implements RadioDialog.DialogListener,
    DecimalDialog.DialogListener, BinaryDialog.DialogListener {

    // dialog tags
    private static final String DIALOG_THEME = "themeDialog";
    private static final String DIALOG_COLOR = "colorDialog";
    private static final String DIALOG_MASS = "massDialog";
    private static final String DIALOG_EXPORT = "exportDialog";
    private static final String DIALOG_IMPORT = "importDialog";
    private static final String DIALOG_RECREATE_DB = "recreateDbDialog";

    private LayoutInflater inflater;
    private LinearLayout ll;

    //

    public static void startActivity(@NonNull Context c) {
        Intent intent = new Intent(c, SettingsActivity.class);
        c.startActivity(intent);
    }

    // extends AppcompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        inflater = getLayoutInflater();
        ll = findViewById(R.id.ll_settings);

        setToolbar();
        inflateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // set

    protected void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_settings);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.fragment_title_settings));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    protected void inflateViews() {
        // display options

        inflateHeader("Display options");

        inflateSwitchItem("Week headers", Prefs.isWeekHeadersShown(), false, Prefs::showWeekHeaders);

        inflateSwitchItem("Hide singleton routes", Prefs.areSingletonRoutesHidden(), true, Prefs::hideSingletonRoutes);

        // look

        inflateHeader("Look");

        inflateDialogItem("Theme", Prefs.printTheme(), false,
            RadioDialog.newInstance(R.string.dialog_title_theme, BaseDialog.NO_RES,
                AppConsts.themeNames, Prefs.getThemeInt(), DIALOG_THEME));

        inflateDialogItem("Color", Prefs.printColor(), true,
            RadioDialog.newInstance(R.string.dialog_title_color, BaseDialog.NO_RES,
                AppConsts.colorNames, Prefs.getColor(), DIALOG_COLOR));

        // third party services

        inflateHeader("Third party services");

        inflateClickItem("Strava", ". . .", true, v -> StravaSettingsActivity.startActivity(this));

        // file

        inflateHeader("File");

        inflateDialogItem("Export json", "", false,
            BinaryDialog.newInstance(R.string.dialog_title_export, R.string.dialog_msg_export,
                R.string.dialog_btn_export, DIALOG_EXPORT));

        inflateDialogItem("Import json", "", true,
            BinaryDialog.newInstance(R.string.dialog_title_import, R.string.dialog_msg_import,
                R.string.dialog_btn_import, DIALOG_IMPORT));

        // profile

        inflateHeader("Profile");

        inflateDialogItem("Mass", Prefs.getMass() + " kg", false,
            DecimalDialog.newInstance(R.string.dialog_title_mass, BaseDialog.NO_RES,
                Prefs.getMass(), "Kg", R.string.dialog_btn_set, DIALOG_MASS));

        final LocalDate bd = Prefs.getBirthday();
        final View birth = inflateTextView("Birthday", bd == null ? "" : bd.format(AppConsts.FORMATTER_CAPTION), true);
        birth.setOnClickListener(v -> {
            int yearSelect, monthSelect, daySelect;
            if (bd == null) {
                yearSelect = 1970;
                monthSelect = 0;
                daySelect = 1;
            }
            else {
                yearSelect = bd.getYear();
                monthSelect = bd.getMonthValue() - 1;
                daySelect = bd.getDayOfMonth();
            }
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Prefs.setBirthday(LocalDate.of(year, month + 1, dayOfMonth));
                recreate();
                //((TextView) birth.findViewById(R.id.textView_value)).setText(bd.format(C.FORMATTER_CAPTION));
            }, yearSelect, monthSelect, daySelect);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });

        // developer options

        if (Prefs.isDeveloper()) {
            inflateHeader("Developer options");

            inflateClickItem("Reboard", "", false, v -> {
                Prefs.setFirstLogin(true);
                OnboardingActivity.startActivity(this);
            });

            inflateDialogItem("Recreate database", "", true, BinaryDialog.generic(DIALOG_RECREATE_DB));
        }
    }

    protected void reflateViews() {
        ll.removeAllViews();
        inflateViews();
    }

    // inflate items

    protected void inflateHeader(String title) {
        View v = inflater.inflate(R.layout.item_settings_header, ll, false);
        ((TextView) v.findViewById(R.id.tv_settings_item_header_title)).setText(title);
        ll.addView(v);
    }

    protected void inflateDialogItem(String title, String value, boolean hideDivider, final BaseDialog dialog) {
        View v = inflateTextView(title, value, hideDivider);
        v.setOnClickListener(v1 -> dialog.show(getSupportFragmentManager()));
    }

    protected void inflateSwitchItem(String title, boolean checked, boolean hideDivider, OnSwitchListener listener) {
        View v = inflateSwitchView(title, hideDivider);
        final Switch sw = v.findViewById(R.id.sw_settings_item);
        sw.setChecked(checked);
        v.setOnClickListener(view -> {
            sw.setChecked(!sw.isChecked());
            listener.onSwitch(sw.isChecked());
        });
    }

    protected void inflateClickItem(String title, String value, boolean hideDivider, View.OnClickListener listener) {
        View v = inflateTextView(title, value, hideDivider);
        v.setOnClickListener(listener);
    }

    // inflate views

    private View inflateTextView(String title, String value, boolean hideDivider) {
        View v = inflater.inflate(R.layout.item_settings_text, ll, false);
        LayoutUtils.setRipple(v, this);
        ((TextView) v.findViewById(R.id.tv_settings_item_text_title)).setText(title);
        ((TextView) v.findViewById(R.id.tv_settings_item_text_value)).setText(value);
        if (hideDivider) v.findViewById(R.id.v_settings_item_divider).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }

    private View inflateSwitchView(String title, boolean hideDivider) {
        View v = inflater.inflate(R.layout.item_settings_switch, ll, false);
        LayoutUtils.setRipple(v, this);
        ((TextView) v.findViewById(R.id.sw_settings_item)).setText(title);
        if (hideDivider) v.findViewById(R.id.v_settings_item_divider).setVisibility(View.INVISIBLE);
        ll.addView(v);
        return v;
    }

    // implements dialogs

    @Override
    public void onRadioDialogClick(int index, String tag) {
        if (tag.equals(DIALOG_THEME)) {
            switch (index) {
                case 0:
                    if (Prefs.isThemeLight()) Prefs.setTheme(false);
                    break;
                case 1:
                    if (!Prefs.isThemeLight()) Prefs.setTheme(true);
                    break;
                default:
                    break;
            }
        }
        else if (tag.equals(DIALOG_COLOR)) {
            switch (index) {
                case 0:
                    if (!Prefs.isColorMono()) Prefs.setColorMono();
                    break;
                case 1:
                    if (!Prefs.isColorGreen()) Prefs.setColorGreen();
                    break;
            }
        }

        MainActivity.recreateOnRestart = true;
        recreate();
    }

    @Override
    public void onDecimalDialogPositiveClick(float input, String tag) {
        if (tag.equals(DIALOG_MASS)) {
            Prefs.setMass(input);
            reflateViews();
        }
    }

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_EXPORT)) {
            LayoutUtils.toast(R.string.toast_json_exporting, this);
            new Thread(() -> {
                boolean success = FileUtils.exportJson(this);
                runOnUiThread(() -> LayoutUtils.toast(success
                        ? R.string.toast_json_export_successful
                        : R.string.toast_json_export_err,
                    this));
            }).start();
        }
        else if (tag.equals(DIALOG_IMPORT)) {
            LayoutUtils.toast(R.string.toast_json_importing, this);
            new Thread(() -> {
                boolean success = FileUtils.importJson(this);
                runOnUiThread(() -> LayoutUtils.toast(success
                        ? R.string.toast_json_import_successful
                        : R.string.toast_json_import_err,
                    this));
            }).start();
        }
        else if (tag.equals(DIALOG_RECREATE_DB)) {
            DbWriter.get(this).recreate();
        }
    }

    // interface

    interface OnSwitchListener {

        void onSwitch(boolean checked);

    }

}