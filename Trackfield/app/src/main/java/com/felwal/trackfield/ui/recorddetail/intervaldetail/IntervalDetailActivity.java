package com.felwal.trackfield.ui.recorddetail.intervaldetail;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.MenuRes;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.ui.custom.dialog.BaseDialog;
import com.felwal.trackfield.ui.custom.dialog.TextDialog;
import com.felwal.trackfield.ui.recorddetail.RecordDetailActivity;

public class IntervalDetailActivity extends RecordDetailActivity implements TextDialog.DialogListener {

    // extras names
    private static final String EXTRA_INTERVAL = "interval";

    // dialog tags
    private static final String DIALOG_RENAME_INTERVAL = "renameIntervalDialog";

    private String interval;

    //

    public static void startActivity(Context c, String interval) {
        Intent intent = new Intent(c, IntervalDetailActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, String interval, int originId) {
        Intent intent = new Intent(c, IntervalDetailActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_rename_interval) {
            if (interval != null) {
                TextDialog.newInstance(R.string.dialog_title_rename_interval, BaseDialog.NO_RES,
                    interval, "", R.string.dialog_btn_rename, DIALOG_RENAME_INTERVAL)
                    .show(getSupportFragmentManager());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // get

    public String getInterval() {
        return interval;
    }

    // extends RecActivity

    @Override
    protected void getExtras(Intent intent) {
        if (!intent.hasExtra(EXTRA_INTERVAL)) return;
        interval = intent.getStringExtra(EXTRA_INTERVAL);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;

        setToolbar(interval);
        selectFragment(IntervalDetailRecyclerFragment.newInstance(interval, originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_intervaldetail;
    }

    // implements TextDialog

    @Override
    public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RENAME_INTERVAL)) {
            if (input.equals("")) return;

            DbWriter.get(this).updateInterval(interval, input);

            finish();
            startActivity(this, input, originId);
        }
    }

}
