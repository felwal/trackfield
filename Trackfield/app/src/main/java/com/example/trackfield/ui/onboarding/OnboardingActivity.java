package com.example.trackfield.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.trackfield.R;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.utils.ScreenUtils;

public class OnboardingActivity extends AppCompatActivity {

    //

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), OnboardingActivity.class);
        c.startActivity(startIntent);
    }

    // extends AppCompatActivity

    @Override protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ScreenUtils.makeStatusBarTransparent(getWindow(), !ScreenUtils.isThemeLight(), findViewById(R.id.textView_onboardingSubtitle));

        //adapter = new BoardingPagerAdapter(this, getSupportFragmentManager());
        //pager = findViewById(R.id.view_pager);
        //pager.setAdapter(adapter);

        setViews();
    }

    // set

    private void setViews() {
        final Button nextBtn = findViewById(R.id.button_start);
        nextBtn.setOnClickListener(view -> {
            Prefs.setFirstLogin(false);
            finish();
        });
    }

}