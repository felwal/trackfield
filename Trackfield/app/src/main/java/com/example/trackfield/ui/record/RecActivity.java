package com.example.trackfield.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.RecyclerFragment;
import com.example.trackfield.ui.custom.sheet.SortSheet;
import com.example.trackfield.utils.ScreenUtils;

public abstract class RecActivity extends AppCompatActivity implements SortSheet.SheetListener {

    // extras names
    protected static final String EXTRA_ORIGIN_ID = "orignId";

    protected RecyclerFragment recyclerFragment;
    protected int originId = -1;

    private FrameLayout frame;

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        frame = findViewById(R.id.fl_record);

        getExtras(getIntent());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recyclerFragment.updateRecycler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getToolbarMenuRes(), menu);
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

    protected void setToolbar(String title) {
        final Toolbar tb = findViewById(R.id.tb_record);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true); // up btn
        ab.setTitle(title);
    }

    // tools

    protected void selectFragment(RecyclerFragment recyclerFragment) {
        this.recyclerFragment = recyclerFragment;
        getSupportFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();
    }

    // abstract

    protected abstract void getExtras(Intent intent);

    protected abstract int getToolbarMenuRes();

    // implements SortSheet

    @Override
    public void onSortSheetClick(int selectedIndex) {
        recyclerFragment.onSortSheetDismiss(selectedIndex);
    }

}
