package com.example.trackfield.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    // on

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        L.transStatusBar(getWindow());

        //adapter = new BoardingPagerAdapter(this, getSupportFragmentManager());
        //pager = findViewById(R.id.view_pager);
        //pager.setAdapter(adapter);

        setBtn();
    }

    // set

    private void setBtn() {
        final Button nextBtn = findViewById(R.id.button_next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                /*int pos = pager.getCurrentItem();
                if (pos < adapter.getCount() - 1) pager.setCurrentItem(pos + 1);
                else finish();
                nextBtn.setText(pos == adapter.getCount() - 2 ? "Finish" : "Next");*/

                Prefs.setFirstLogin(false);
                finish();
            }
        });
    }

}