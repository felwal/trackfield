package com.felwal.trackfield.ui.recorddetail.intervaldetail;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import com.felwal.android.widget.dialog.TextDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbWriter;
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
                TextDialog.newInstance(getString(R.string.dialog_title_rename_interval), "", interval,
                    "", R.string.dialog_btn_rename, R.string.fw_dialog_btn_cancel, DIALOG_RENAME_INTERVAL, null)
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
    public void onTextDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_RENAME_INTERVAL)) {
            if (input.equals("")) return;

            DbWriter.get(this).updateInterval(interval, input);

            finish();
            startActivity(this, input, originId);
        }
    }

}
