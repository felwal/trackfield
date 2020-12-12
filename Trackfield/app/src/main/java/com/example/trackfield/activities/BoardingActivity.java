package com.example.trackfield.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.trackfield.R;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackfield.activities.ui.main.BoardingPagerAdapter;
import com.example.trackfield.toolbox.Prefs;
import com.example.trackfield.toolbox.Toolbox.*;

public class BoardingActivity extends AppCompatActivity {

    private BoardingPagerAdapter adapter;
    private ViewPager pager;

    ////

    public static void startActivity(Context c) {
        Intent startIntent = new Intent(c.getApplicationContext(), BoardingActivity.class);
        c.startActivity(startIntent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        L.transStatusBar(getWindow());

        //adapter = new BoardingPagerAdapter(this, getSupportFragmentManager());
        //pager = findViewById(R.id.view_pager);
        //pager.setAdapter(adapter);

        setBtn();
    }

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