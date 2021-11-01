package com.felwal.trackfield.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.felwal.android.widget.dialog.BinaryDialog;
import com.felwal.android.widget.dialog.CheckDialog;
import com.felwal.android.widget.dialog.TextDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.network.StravaApi;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.utils.LayoutUtils;

public class StravaSettingsActivity extends SettingsActivity implements TextDialog.DialogListener,
    CheckDialog.DialogListener {

    // dialog tags
    private static final String DIALOG_DEVICE = "deviceDialog";
    private static final String DIALOG_RECORDING_METHOD = "methodDialog";
    private static final String DIALOG_REQUEST_ALL = "requestAllDialog";
    private static final String DIALOG_PULL_ALL = "pullAllDialog";
    private static final String DIALOG_PULL_POLICY = "pullPolicyDialog";

    private StravaApi strava;

    //

    public static void startActivity(@NonNull Context c) {
        Intent intent = new Intent(c, StravaSettingsActivity.class);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        strava = new StravaApi(this);
        strava.handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        strava.handleIntent(getIntent());
    }

    // extends SettingsActivity

    @Override
    protected void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_settings);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.fragment_title_settings_strava));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void inflateViews() {
        // connection

        inflateHeader(getString(R.string.tv_text_settings_header_connection));

        inflateClickItem(getString(R.string.tv_text_settings_title_auth), "", true, v -> strava.authorizeStrava());

        // requests

        inflateHeader(getString(R.string.tv_text_settings_header_requests));

        inflateClickItem(getString(R.string.tv_text_settings_title_request_new), "", false,
            v -> strava.requestNewActivities((successCount, errorCount) ->
                StravaApi.toastResponse(successCount, errorCount, this)));

        inflateDialogItem(getString(R.string.tv_text_settings_title_request_all), "", !Prefs.isDeveloper(),
            BinaryDialog.newInstance("Request all exercises?", "", R.string.dialog_btn_ok, R.string.dialog_btn_cancel,
                DIALOG_REQUEST_ALL, null));

        if (Prefs.isDeveloper()) {
            inflateDialogItem(getString(R.string.tv_text_settings_title_pull_all), "", true,
                BinaryDialog.newInstance("Pull all exercises?", "", R.string.dialog_btn_ok, R.string.dialog_btn_cancel,
                    DIALOG_PULL_ALL, null));
        }

        // request options

        inflateHeader(getString(R.string.tv_text_settings_header_request_options));

        inflateDialogItem(getString(R.string.tv_text_settings_title_device), Prefs.getDefaultDevice(), false,
            TextDialog.newInstance(getString(R.string.dialog_title_device), getString(R.string.dialog_msg_device),
                Prefs.getDefaultDevice(), getString(R.string.tv_text_settings_hint_device), R.string.dialog_btn_set,
                R.string.dialog_btn_cancel, DIALOG_DEVICE));

        inflateDialogItem(getString(R.string.tv_text_settings_title_method), Prefs.getDefaultRecordingMethod(), false,
            TextDialog.newInstance(getString(R.string.dialog_title_recording_method),
                getString(R.string.dialog_msg_recording_method), Prefs.getDefaultRecordingMethod(),
                getString(R.string.tv_text_settings_hint_method), R.string.dialog_btn_set,
                R.string.dialog_btn_cancel, DIALOG_RECORDING_METHOD));

        inflateDialogItem(getString(R.string.tv_text_settings_title_policy), "", true,
            CheckDialog.newInstance(getString(R.string.dialog_title_pull_policy), "",
                Prefs.getPullPolicy().getTexts(), Prefs.getPullPolicy().getChecked(),
                R.string.dialog_btn_set, R.string.dialog_btn_cancel, DIALOG_PULL_POLICY));
    }

    // implements dialogs

    @Override
    public void onTextDialogPositiveClick(@NonNull String input, String tag) {
        if (tag.equals(DIALOG_DEVICE)) {
            Prefs.setDefaultDevice(input);
            reflateViews();
        }
        if (tag.equals(DIALOG_RECORDING_METHOD)) {
            Prefs.setDefaultRecordingMethod(input);
            reflateViews();
        }
    }

    @Override
    public void onCheckDialogPositiveClick(@NonNull boolean[] checkedItems, @NonNull String tag) {
        if (tag.equals(DIALOG_PULL_POLICY)) {
            Prefs.setPullSettings(checkedItems);
            reflateViews();
        }
    }

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_REQUEST_ALL)) {
            strava.requestAllActivities((successCount, errorCount) ->
                StravaApi.toastResponse(successCount, errorCount, this));
        }
        else if (tag.equals(DIALOG_PULL_ALL)) {
            strava.pullAllActivities(success -> {
                // we dont want to toast for every successfully requested activity
                if (!success) {
                    LayoutUtils.toast(R.string.toast_strava_pull_activity_err, this);
                }
            });
        }
    }

}
