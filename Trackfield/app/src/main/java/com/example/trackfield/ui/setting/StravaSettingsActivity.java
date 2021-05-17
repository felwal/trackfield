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
import com.example.trackfield.utils.model.PairList;

import java.util.ArrayList;

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
        inflateDialogItem("Request all", "", false, BinaryDialog.generic(DIALOG_REQUEST_ALL));
        inflateDialogItem("Pull all", "", true, BinaryDialog.generic(DIALOG_PULL_ALL));

        // request options
        inflateHeader("Request options");
        inflateDialogItem("Recording method", Prefs.getRecordingMethod(), false,
            TextDialog.newInstance(R.string.dialog_title_recording_method,
                R.string.dialog_message_recording_method, Prefs.getRecordingMethod(),
                "GPS, Galileo, Glonass etc...", R.string.dialog_btn_set, DIALOG_RECORDING_METHOD));
        inflateDialogItem("Pull policy", "", true,
            SwitchDialog.newInstance(R.string.dialog_title_pull, BaseDialog.NO_RES, R.string.dialog_btn_set,
                Prefs.getPullSettings(), DIALOG_PULL_POLICY));
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
    public void onSwitchDialogPositiveClick(ArrayList<Boolean> switchStates, String tag) {
        if (tag.equals(DIALOG_PULL_POLICY)) {
            PairList<String, Boolean> pullSettings = Prefs.getPullSettings();
            pullSettings.setSeconds(switchStates);
            Prefs.setPullSettings(pullSettings);
            reflateViews();
        }
    }

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_REQUEST_ALL)) {
            strava.requestAllActivities();
        }
        else if (tag.equals(DIALOG_PULL_ALL)) {
            strava.pullAllActivities();
        }
    }

}
