package me.felwal.trackfield.ui.groupdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import me.felwal.android.fragment.sheet.SortSheet;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.prefs.ExerciseFilter;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.ExerciseFilterActivity;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.utils.ScreenUtils;
import me.felwal.trackfield.utils.UtilsKt;

public abstract class GroupDetailActivity extends ExerciseFilterActivity implements SortSheet.SheetListener {

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
        setContentView(R.layout.activity_groupdetail);
        frame = findViewById(R.id.fl_groupdetail);

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
        UtilsKt.createOptionalIcons(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UtilsKt.prepareOptionalIcons(menu, this);
        return super.onPrepareOptionsMenu(menu);
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
        final Toolbar tb = findViewById(R.id.tb_groupdetail);
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

    @NonNull @Override
    public ExerciseFilter getFilter() {
        return Prefs.getGroupFilter();
    }

    @Override
    public void applyTypeFilter(@NonNull ArrayList<String> visibleTypes) {
        Prefs.setGroupVisibleTypes(visibleTypes);
    }

    @Override
    public void applyLabelFilter(@NonNull ArrayList<String> visibleLabels) {
        Prefs.setGroupVisibleLabels(visibleLabels);
    }

    // abstract

    protected abstract void getExtras(Intent intent);

    @MenuRes
    protected abstract int getToolbarMenuRes();

    // implements dialogs

    @Override
    public void onExerciseFilter() {
        recyclerFragment.updateRecycler();
    }

    @Override
    public void onSortSheetItemClick(int selectedIndex) {
        recyclerFragment.onSortSheetDismiss(selectedIndex);
    }

}
