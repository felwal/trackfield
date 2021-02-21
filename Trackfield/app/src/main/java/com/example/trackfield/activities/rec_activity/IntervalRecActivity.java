package com.example.trackfield.activities.rec_activity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.dialogs.TextDialog;
import com.example.trackfield.fragments.recycler_fragments.InExRecyclerFragment;

public class IntervalRecActivity extends RecActivity implements TextDialog.DialogListener {

    private String interval;

    public static final String EXTRA_INTERVAL = "interval";
    private static final String TAG_RENAME_INTERVAL = "renameInterval";

    ////

    public static void startActivity(Context c, String interval) {
        Intent intent = new Intent(c, IntervalRecActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, String interval, int originId) {
        Intent intent = new Intent(c, IntervalRecActivity.class);
        intent.putExtra(EXTRA_INTERVAL, interval);
        if (originId != -1) intent.putExtra(EXTRA_ORIGIN_ID, originId);
        c.startActivity(intent);
    }

    // on

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_renameInterval:
                if (interval != null) {
                    TextDialog.newInstance(getString(R.string.dialog_title_rename_interval), "", interval, "", R.string.dialog_btn_rename, "renameInterval")
                            .show(getSupportFragmentManager());;
                }
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    // get

    public String getInterval() {
        return interval;
    }

    // extend

    @Override
    protected void getExtras(Intent intent) {
        if (!intent.hasExtra(EXTRA_INTERVAL)) return;

        interval = intent.getStringExtra(EXTRA_INTERVAL);
        setToolbar(interval);
        originId = intent.hasExtra(EXTRA_ORIGIN_ID) ? intent.getIntExtra(EXTRA_ORIGIN_ID, -1) : -1;
        selectFragment(InExRecyclerFragment.newInstance(interval, originId));
    }

    @Override
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_rec_interval;
    }

    // implement

    @Override public void onTextDialogPositiveClick(String input, String tag) {
        if (input.equals("")) return;

        Helper.getWriter(this).updateInterval(interval, input);

        finish();
        startActivity(this, input, originId);
    }

}
