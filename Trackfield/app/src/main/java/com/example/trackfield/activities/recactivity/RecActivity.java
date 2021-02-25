package com.example.trackfield.activities.recactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.database.Reader;
import com.example.trackfield.database.Writer;
import com.example.trackfield.dialogs.sheets.SortSheet;
import com.example.trackfield.fragments.recyclerfragments.RecyclerFragment;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;

public abstract class RecActivity extends AppCompatActivity implements SortSheet.DismissListener {

    protected Reader reader;
    protected Writer writer;
    private ActionBar ab;
    protected FrameLayout frame;
    protected RecyclerFragment recyclerFragment;

    protected int originId = -1;

    public static final String EXTRA_ORIGIN_ID = "orignId";

    ////

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec);
        frame = findViewById(R.id.frameLayout_scrollerFrameRec);

        //reader = new Helper.Reader(this);
        reader = Reader.get(this);
        writer = Writer.get(this);
        getExtras(getIntent());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recyclerFragment.updateRecycler();
        //recreate();
    }

    @Override
    protected void onDestroy() {
        //reader.close();
        //Helper.closeReader();
        //Helper.closeWriter();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getToolbarMenuRes(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    // set

    protected void setToolbar(String title) {
        final Toolbar tb = findViewById(R.id.toolbar_rec);
        setSupportActionBar(tb);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true); // up btn
        ab.setTitle(title);
    }

    // tools

    public void setToolbarTitle(String title) {
        ab.setTitle(title);
    }

    protected void selectFragment(RecyclerFragment recyclerFragment) {
        this.recyclerFragment = recyclerFragment;
        getSupportFragmentManager().beginTransaction().replace(frame.getId(), recyclerFragment).commit();
    }

    public Reader getReader() {
        return reader;
    }

    // abstract

    protected abstract void getExtras(Intent intent);

    protected abstract int getToolbarMenuRes();

    // implements SortSheet

    @Override public void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        recyclerFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

}
