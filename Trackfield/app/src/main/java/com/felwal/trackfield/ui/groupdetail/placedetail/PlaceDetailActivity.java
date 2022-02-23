package com.felwal.trackfield.ui.groupdetail.placedetail;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import com.felwal.android.util.CollectionUtilsKt;
import com.felwal.android.widget.dialog.AlertDialog;
import com.felwal.android.widget.dialog.BaseDialogKt;
import com.felwal.android.widget.dialog.CheckDialog;
import com.felwal.android.widget.dialog.MultiChoiceDialog;
import com.felwal.android.widget.dialog.NumberDialog;
import com.felwal.android.widget.dialog.TextDialog;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.db.model.Place;
import com.felwal.trackfield.data.db.model.Route;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.groupdetail.GroupDetailActivity;
import com.felwal.trackfield.ui.main.MainActivity;
import com.felwal.trackfield.ui.map.PlaceMapActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PlaceDetailActivity extends GroupDetailActivity implements TextDialog.DialogListener,
    AlertDialog.DialogListener, MultiChoiceDialog.DialogListener, NumberDialog.DialogListener {

    // extras names
    private static final String EXTRA_PLACE_ID = "placeId";

    // dialog tags
    private static final String DIALOG_RENAME_PLACE = "renamePlaceDialog";
    private static final String DIALOG_NAME_ALREADY_EXISTS = "placeNameExistsDialog";
    private static final String DIALOG_DELETE_PLACE = "deletePlaceDialog";
    private static final String DIALOG_FILTER = "filterDialog";
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
            ArrayList<String> types = DbReader.get(this).getTypes(null);
            String[] items = new String[types.size()];
            types.toArray(items);

            int[] checkedItems = CollectionUtilsKt.indicesOf(items, Prefs.getDistanceVisibleTypes().toArray());

            CheckDialog.newInstance(getString(R.string.dialog_title_title_filter), items, checkedItems, null,
                R.string.dialog_btn_filter, R.string.fw_dialog_btn_cancel, DIALOG_FILTER, null)
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_rename_place) {
            if (place != null) {
                TextDialog.newInstance(getString(R.string.dialog_title_rename_place), "", place.getName(),
                    "", R.string.dialog_btn_rename, R.string.fw_dialog_btn_cancel, DIALOG_RENAME_PLACE, null)
                    .show(getSupportFragmentManager());
            }
            return true;
        }
        else if (itemId == R.id.action_exerciseedit_radius) {
            NumberDialog.newInstance(getString(R.string.dialog_title_exerciseedit_radius), "", place.getRadius(), "500",
                R.string.dialog_btn_set, R.string.fw_dialog_btn_cancel, DIALOG_EDIT_RADIUS, null)
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
            AlertDialog.newInstance(getString(R.string.dialog_title_delete_place),
                getString(R.string.dialog_msg_delete_place), R.string.dialog_btn_delete,
                R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES, DIALOG_DELETE_PLACE, null)
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
    public void onTextDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_RENAME_PLACE)) {
            if (input.equals("") || input.equals(place.getName())) return;

            int existingIdForNewName = DbReader.get(this).getPlaceId(input);

            // update place
            if (existingIdForNewName != Route.ID_NON_EXISTANT) {
                AlertDialog.newInstance(getString(R.string.dialog_title_place_name_exists),
                    getString(R.string.dialog_msg_place_name_exists), R.string.fw_dialog_btn_ok,
                    BaseDialogKt.NO_RES, BaseDialogKt.NO_RES,
                    input, DIALOG_NAME_ALREADY_EXISTS)
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

    @Override
    public void onMultiChoiceDialogItemsSelected(@NonNull boolean[] checkedItems, @NonNull String tag,
        @Nullable String passValue) {

        if (tag.equals(DIALOG_FILTER)) {
            ArrayList<String> visibleTypes = (ArrayList<String>)
                CollectionUtilsKt.filter(DbReader.get(this).getTypes(null), checkedItems);

            Prefs.setDistanceVisibleTypes(visibleTypes);
            recyclerFragment.updateRecycler();
        }
    }

    @Override
    public void onNumberDialogPositiveClick(long input, @NonNull String tag, @Nullable String passValue) {
        if (tag.equals(DIALOG_EDIT_RADIUS)) {
            place.setRadius((int) input);
            DbWriter.get(this).updatePlace(place);
            recyclerFragment.updateRecycler();
        }
    }

}
