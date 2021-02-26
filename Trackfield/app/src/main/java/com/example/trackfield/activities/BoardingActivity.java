package com.example.trackfield.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.trackfield.R;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;

public class BoardingActivity extends AppCompatActivity {

    ////

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), BoardingActivity.class);
        c.startActivity(startIntent);
    }

    // extends AppCompatActivity

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        L.makeStatusBarTransparent(getWindow(), true, findViewById(R.id.textView_boardingSubtitle));

        //adapter = new BoardingPagerAdapter(this, getSupportFragmentManager());
        //pager = findViewById(R.id.view_pager);
        //pager.setAdapter(adapter);

        setBtn();
    }

    // set

    private void setBtn() {
        final Button nextBtn = findViewById(R.id.button_start);
        nextBtn.setOnClickListener(view -> {
            Prefs.setFirstLogin(false);
            finish();
        });
    }

}