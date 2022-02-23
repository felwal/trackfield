package me.felwal.trackfield.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.utils.ScreenUtils;

public class OnboardingActivity extends AppCompatActivity {

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), OnboardingActivity.class);
        c.startActivity(startIntent);
    }

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ScreenUtils.makeStatusBarTransparent(getWindow(), !ScreenUtils.isThemeLight(this), null);

        setViews();
    }

    // set

    private void setViews() {
        final Button nextBtn = findViewById(R.id.btn_onboarding_start);
        nextBtn.setOnClickListener(view -> {
            Prefs.setFirstLogin(false);
            finish();
        });
    }

}