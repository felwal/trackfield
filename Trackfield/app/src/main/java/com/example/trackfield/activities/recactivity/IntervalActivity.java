package com.example.trackfield.activities.recactivity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.example.trackfield.R;
import com.example.trackfield.database.Writer;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.TextDialog;
import com.example.trackfield.fragments.recyclerfragments.IntervalRecyclerFragment;

public class IntervalActivity extends RecActivity implements TextDialog.DialogListener {

    private String interval;

    public static final String EXTRA_INTERVAL = "interval";
    private static final String DIALOG_RENAME_INTERVAL = "renameIntervalDialog";

    //

    public static void startActivity(Context c, String interval) {
        Intent intent = new Intent(c, IntervalActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, String interval, int originId) {
        Intent intent = new Intent(c, IntervalActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_renameInterval) {
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
        setToolbar(interval);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;
        selectFragment(IntervalRecyclerFragment.newInstance(interval, originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_interval;
    }

    // implements TextDialog

    @Override public void onTextDialogPositiveClick(String input, String tag) {
        if (tag.equals(DIALOG_RENAME_INTERVAL)) {
            if (input.equals("")) return;

            Writer.get(this).updateInterval(interval, input);

            finish();
            startActivity(this, input, originId);
        }
    }

}
