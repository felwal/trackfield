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
import com.example.trackfield.ui.custom.dialog.TextDialog;

public class StravaSettingsActivity extends SettingsActivity implements TextDialog.DialogListener {

    private StravaApi strava;

    // dialog tags
    private static final String DIALOG_RECORDING_METHOD = "methodDialog";

    //

    public static void startActivity(@NonNull Context c) {
        Intent intent = new Intent(c, com.example.trackfield.ui.setting.StravaSettingsActivity.class);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        strava = StravaApi.getInstance(this);
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
        final Toolbar tb = findViewById(R.id.toolbar_settings);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.fragment_settings_strava));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void inflateViews() {
        // connection
        inflateHeader("Connection");
        inflateClickItem("Authorize", "", true, v -> strava.authorizeStrava());

        // requests
        inflateHeader("Request activities");
        //inflateClickItem("Request last", "", false, v -> strava.requestLastActivity());
        inflateClickItem("Request last 5", "", false, v -> strava.requestLastActivities(5));
        inflateClickItem("Request all", "", false, v -> strava.requestAllActivities());
        inflateClickItem("Pull all", "", true, v -> strava.pullAllActivities());

        // preferences
        inflateHeader("Request defaults");
        inflateDialogItem("Recording method", Prefs.getRecordingMethod(), true,
                TextDialog.newInstance(R.string.dialog_title_recording_method,
                        R.string.dialog_message_recording_method, Prefs.getRecordingMethod(),
                        "GPS, Galileo, Glonass etc...", R.string.dialog_btn_set, DIALOG_RECORDING_METHOD));
    }

    // implements dialogs

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RECORDING_METHOD)) {
            Prefs.setRecordingMethod(input);
            recreate();
        }
    }

}
