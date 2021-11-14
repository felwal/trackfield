package com.felwal.trackfield.ui.setting;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.felwal.android.widget.dialog.BaseDialog;
import com.felwal.android.widget.dialog.AlertDialog;
import com.felwal.android.widget.dialog.BaseDialogKt;
import com.felwal.android.widget.dialog.DecimalDialog;
import com.felwal.android.widget.dialog.RadioDialog;
import com.felwal.android.widget.dialog.SingleChoiceDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.main.MainActivity;
import com.felwal.trackfield.ui.onboarding.OnboardingActivity;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.FileUtils;
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class SettingsActivity extends AppCompatActivity implements SingleChoiceDialog.DialogListener,
    DecimalDialog.DialogListener, AlertDialog.DialogListener {

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

        inflateHeader(getString(R.string.tv_text_settings_header_display));

        inflateSwitchItem(getString(R.string.tv_text_settings_title_week_headers), Prefs.isWeekHeadersShown(), false,
            Prefs::showWeekHeaders);

        inflateSwitchItem(getString(R.string.tv_text_settings_title_singletion_routes),
            Prefs.areSingletonRoutesHidden(), true, Prefs::hideSingletonRoutes);

        // look

        inflateHeader(getString(R.string.tv_text_settings_header_look));

        inflateDialogItem(getString(R.string.tv_text_settings_title_theme),
            AppConsts.themeNames.get(Prefs.getTheme()), false,
            RadioDialog.newInstance(getString(R.string.dialog_title_theme), AppConsts.themeNames, Prefs.getTheme(),
                BaseDialogKt.NO_RES, R.string.fw_dialog_btn_cancel, DIALOG_THEME));

        inflateDialogItem(getString(R.string.tv_text_settings_title_color),
            AppConsts.colorNames.get(Prefs.getColor()), true,
            RadioDialog.newInstance(getString(R.string.dialog_title_color), AppConsts.colorNames,
                Prefs.getColor(), BaseDialogKt.NO_RES, R.string.fw_dialog_btn_cancel, DIALOG_COLOR));

        // third party services

        inflateHeader(getString(R.string.tv_text_settings_header_services));

        inflateClickItem(getString(R.string.tv_text_settings_title_strava), ". . .", true,
            v -> StravaSettingsActivity.startActivity(this));

        // file

        inflateHeader(getString(R.string.tv_text_settings_header_file));

        inflateDialogItem(getString(R.string.tv_text_settings_title_export), "", false,
            AlertDialog.newInstance(getString(R.string.dialog_title_export), getString(R.string.dialog_msg_export),
                R.string.dialog_btn_export, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_EXPORT, null));

        inflateDialogItem(getString(R.string.tv_text_settings_title_import), "", true,
            AlertDialog.newInstance(getString(R.string.dialog_title_import), getString(R.string.dialog_msg_import),
                R.string.dialog_btn_import, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_IMPORT, null));

        // profile

        inflateHeader(getString(R.string.tv_text_settings_header_profile));

        inflateDialogItem(getString(R.string.tv_text_settings_title_mass), Prefs.getMass() + " kg", false,
            DecimalDialog.newInstance(getString(R.string.dialog_title_mass), "", Prefs.getMass(),
                getString(R.string.tv_text_settings_hint_mass), R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel,
                DIALOG_MASS));

        final LocalDate bd = Prefs.getBirthday();
        final View birth = inflateTextView(getString(R.string.tv_text_settings_title_birthday), bd == null
            ? ""
            : bd.format(AppConsts.FORMATTER_CAPTION), true);
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
            inflateHeader(getString(R.string.tv_text_settings_header_developer));

            inflateClickItem(getString(R.string.tv_text_settings_title_reboard), "", false, v -> {
                Prefs.setFirstLogin(true);
                OnboardingActivity.startActivity(this);
            });

            inflateDialogItem(getString(R.string.tv_text_settings_title_recreate), "", true,
                AlertDialog.newInstance("Recreate database?", "", R.string.fw_dialog_btn_ok,
                    R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_RECREATE_DB, null));
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
        final SwitchMaterial sw = v.findViewById(R.id.sw_settings_item);
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
    public void onSingleChoiceDialogItemSelected(int checkedItem, @NonNull String tag) {
        if (tag.equals(DIALOG_THEME)) {
            Prefs.setTheme(checkedItem);
        }
        else if (tag.equals(DIALOG_COLOR)) {
            Prefs.setColor(checkedItem);
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
    public void onAlertDialogPositiveClick(String passValue, String tag) {
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

    @Override
    public void onAlertDialogNeutralClick(@Nullable String s, @NonNull String s1) {
    }

    // interface

    interface OnSwitchListener {

        void onSwitch(boolean checked);

    }

}