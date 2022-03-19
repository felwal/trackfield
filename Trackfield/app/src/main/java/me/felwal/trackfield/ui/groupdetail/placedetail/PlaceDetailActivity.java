package me.felwal.trackfield.ui.groupdetail.placedetail;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Place;
import me.felwal.trackfield.data.db.model.Route;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.groupdetail.GroupDetailActivity;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.map.PlaceMapActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import me.felwal.android.fragment.dialog.AlertDialog;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.CheckDialog;
import me.felwal.android.fragment.dialog.InputDialog;
import me.felwal.android.fragment.dialog.MultiChoiceDialog;
import me.felwal.android.util.CollectionsKt;
import me.felwal.android.widget.control.CheckListOption;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.android.widget.control.InputOption;

public class PlaceDetailActivity extends GroupDetailActivity implements InputDialog.DialogListener,
    AlertDialog.DialogListener {

    // extras names
    private static final String EXTRA_PLACE_ID = "placeId";

    // dialog tags
    private static final String DIALOG_RENAME_PLACE = "renamePlaceDialog";
    private static final String DIALOG_NAME_ALREADY_EXISTS = "placeNameExistsDialog";
    private static final String DIALOG_DELETE_PLACE = "deletePlaceDialog";
    private static final String DIALOG_EDIT_RADIUS = "editRadiusDialog";

    private Place place;

    //

    public static void startActivity(Context c, int placeId) {
        if (placeId == Place.ID_NON_EXISTANT) return;

        Intent intent = new Intent(c, PlaceDetailActivity.class);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // hide
        MenuItem hideItem = menu.findItem(R.id.action_hide_place);
        hideItem.setChecked(place.isHidden());
        //hideItem.setIcon(place.isHidden() ? R.drawable.ic_show_filled :  R.drawable.ic_show);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_filter_exercises) {
            showFilterSheet();

            return true;
        }
        else if (itemId == R.id.action_rename_place) {
            if (place != null) {
                InputDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_rename_place), "",
                        R.string.dialog_btn_rename, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                        DIALOG_RENAME_PLACE, null),
                    new InputOption(place.getName(), "", EditorInfo.TYPE_CLASS_TEXT))
                    .show(getSupportFragmentManager());
            }

            return true;
        }
        else if (itemId == R.id.action_exerciseedit_radius) {
            InputDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_exerciseedit_radius), "",
                    R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_EDIT_RADIUS, null),
                new InputOption(place.getRadius(), "500"))
                .show(getSupportFragmentManager());
        }
        else if (itemId == R.id.action_hide_place) {
            place.invertHidden();
            DbWriter.get(this).updatePlace(place);
            // to get immediate check feedback (before the menu closes), update it here,
            // instead of calling invalidateOptionsMenu(), since that also resets the optional icon colors.
            item.setChecked(place.isHidden());

            return true;
        }
        else if (itemId == R.id.action_place_map) {
            PlaceMapActivity.startActivity(place.getId(), this);
        }
        else if (itemId == R.id.action_delete_place) {
            AlertDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_delete_place),
                    getString(R.string.dialog_msg_delete_place),
                    R.string.dialog_btn_delete, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_DELETE_PLACE, null))
                .show(getSupportFragmentManager());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // extends RecActivity

    @Override
    protected void getExtras(Intent intent) {
        if (!intent.hasExtra(EXTRA_PLACE_ID)) return;
        int placeId = intent.getIntExtra(EXTRA_PLACE_ID, 0);
        place = DbReader.get(this).getPlace(placeId);

        setToolbar(place.getName());
        selectFragment(PlaceDetailFragment.newInstance(placeId));
    }

    @Override
    @MenuRes
    protected int getToolbarMenuRes() {
        return R.menu.menu_toolbar_group_placedetail;
    }

    // implements dialogs

    @Override
    public void onInputDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_RENAME_PLACE)) {
            if (input.equals("") || input.equals(place.getName())) return;

            int existingIdForNewName = DbReader.get(this).getPlaceId(input);

            // update place
            if (existingIdForNewName != Route.ID_NON_EXISTANT) {
                AlertDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_place_name_exists),
                        getString(R.string.dialog_msg_place_name_exists),
                        R.string.fw_dialog_btn_ok, BaseDialogKt.NO_RES, BaseDialogKt.NO_RES,
                        input, DIALOG_NAME_ALREADY_EXISTS))
                    .show(getSupportFragmentManager());
            }
            else {
                place.setName(input);
                DbWriter.get(this).updatePlace(place);

                MainActivity.updateFragmentOnRestart = true;
                finish();
                startActivity(this, place.getId());
            }
        }
        else if (tag.equals(DIALOG_EDIT_RADIUS)) {
            place.setRadius(Integer.parseInt(input));
            DbWriter.get(this).updatePlace(place);
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onAlertDialogPositiveClick(String tag, String passValue) {
        if (tag.equals(DIALOG_DELETE_PLACE)) {
            DbWriter.get(this).deletePlace(place);

            MainActivity.updateFragmentOnRestart = true;
            finish();
        }
    }

    @Override
    public void onAlertDialogNeutralClick(@NonNull String tag, String passValue) {
    }

}
