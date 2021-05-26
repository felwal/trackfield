package com.example.trackfield.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.data.network.StravaApi;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.custom.dialog.SwitchDialog;
import com.example.trackfield.ui.custom.dialog.TextDialog;
import com.example.trackfield.utils.LayoutUtils;

public class StravaSettingsActivity extends SettingsActivity implements TextDialog.DialogListener,
    SwitchDialog.DialogListener {

    // dialog tags
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

        inflateHeader("Connection");

        inflateClickItem("Authorize", "", true, v -> strava.authorizeStrava());

        // requests

        inflateHeader("Request activities");

        inflateClickItem("Request new", "", false, v -> strava.requestNewActivities((successCount, errorCount) ->
            StravaApi.toastResponse(successCount, errorCount, this)));

        inflateDialogItem("Request all", "", !Prefs.isDeveloper(), BinaryDialog.generic(DIALOG_REQUEST_ALL));

        if (Prefs.isDeveloper()) {
            inflateDialogItem("Pull all", "", true, BinaryDialog.generic(DIALOG_PULL_ALL));
        }

        // request options

        inflateHeader("Request options");

        inflateDialogItem("Recording method", Prefs.getRecordingMethod(), false,
            TextDialog.newInstance(R.string.dialog_title_recording_method,
                R.string.dialog_msg_recording_method, Prefs.getRecordingMethod(),
                "GPS, Galileo, Glonass etc...", R.string.dialog_btn_set, DIALOG_RECORDING_METHOD));

        inflateDialogItem("Pull policy", "", true,
            SwitchDialog.newInstance(R.string.dialog_title_pull_policy, BaseDialog.NO_RES, R.string.dialog_btn_set,
                Prefs.getPullPolicy(), DIALOG_PULL_POLICY));
    }

    // implements dialogs

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RECORDING_METHOD)) {
            Prefs.setRecordingMethod(input);
            reflateViews();
        }
    }

    @Override
    public void onSwitchDialogPositiveClick(boolean[] checked, String tag) {
        if (tag.equals(DIALOG_PULL_POLICY)) {
            Prefs.setPullSettings(checked);
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
