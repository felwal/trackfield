package me.felwal.trackfield.ui.groupdetail.intervaldetail;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.ui.groupdetail.GroupDetailActivity;
import me.felwal.trackfield.ui.main.MainActivity;

import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.InputDialog;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.android.widget.control.InputOption;

public class IntervalDetailActivity extends GroupDetailActivity implements InputDialog.DialogListener {

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
                InputDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_rename_interval),
                        getString(R.string.dialog_msg_rename_interval),
                        R.string.dialog_btn_rename, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                        DIALOG_RENAME_INTERVAL, null),
                    new InputOption(interval, "", EditorInfo.TYPE_CLASS_TEXT))
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
        selectFragment(IntervalDetailFragment.newInstance(interval, originId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_group_intervaldetail;
    }

    // implements TextDialog

    @Override
    public void onInputDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_RENAME_INTERVAL)) {
            if (input.equals("")) return;

            DbWriter.get(this).updateInterval(interval, input);

            MainActivity.updateFragmentOnRestart = true;
            finish();
            startActivity(this, input, originId);
        }
    }

}
