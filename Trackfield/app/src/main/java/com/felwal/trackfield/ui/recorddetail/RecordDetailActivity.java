package com.felwal.trackfield.ui.recorddetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.felwal.android.widget.sheet.SortSheet;
import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.utils.ScreenUtils;

public abstract class RecordDetailActivity extends AppCompatActivity implements SortSheet.SheetListener {

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
        setContentView(R.layout.activity_recorddetail);
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

    @MenuRes
    protected abstract int getToolbarMenuRes();

    // implements SortSheet

    @Override
    public void onSortSheetItemClick(int selectedIndex) {
        recyclerFragment.onSortSheetDismiss(selectedIndex);
    }

}