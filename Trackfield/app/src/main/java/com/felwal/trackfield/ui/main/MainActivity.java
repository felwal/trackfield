package com.felwal.trackfield.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.android.util.CollectionUtilsKt;
import com.felwal.android.util.ResUtilsKt;
import com.felwal.android.widget.FloatingActionMenu;
import com.felwal.android.widget.dialog.ChipDialog;
import com.felwal.android.widget.dialog.DecimalDialog;
import com.felwal.android.widget.dialog.MultiChoiceDialog;
import com.felwal.android.widget.sheet.SortSheet;
import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.DbWriter;
import com.felwal.trackfield.data.db.model.Distance;
import com.felwal.trackfield.data.network.StravaApi;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.base.RecyclerFragment;
import com.felwal.trackfield.ui.exercisedetail.ExerciseAddActivity;
import com.felwal.trackfield.ui.main.exerciselist.ExerciseListFragment;
import com.felwal.trackfield.ui.main.exerciselist.ExerciseListRecyclerFragment;
import com.felwal.trackfield.ui.main.recordlist.RecordListFragment;
import com.felwal.trackfield.ui.main.stats.StatsFragment;
import com.felwal.trackfield.ui.onboarding.OnboardingActivity;
import com.felwal.trackfield.ui.setting.SettingsActivity;
import com.felwal.trackfield.utils.FileUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import kotlin.Unit;

import static com.felwal.android.widget.dialog.DecimalDialogKt.NO_FLOAT_TEXT;

public class MainActivity extends AppCompatActivity implements DecimalDialog.DialogListener,
    MultiChoiceDialog.DialogListener, SortSheet.SheetListener {

    public static boolean recreateOnRestart = false;

    // dialog tags
    private static final String DIALOG_FILTER_EXERCISES = "filterExercisesDialog";
    private static final String DIALOG_ADD_DISTANCE = "addDistanceDialog";

    private static boolean appInitialized = false;

    private MainFragment mainFragment;
    private ActionBar ab;
    private StravaApi strava;

    private FloatingActionMenu fam;

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initApp();
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScreenUtils.setScale(this);

        if (Prefs.isFirstLogin()) OnboardingActivity.startActivity(this);

        // TODO: dev
        Prefs.setDeveloper(true);
        DbWriter.get(this).useUpdateToolIfEnabled(this);

        strava = new StravaApi(this);

        // layout
        setBottomNavbar();
        setToolbar();
        setFam();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (recreateOnRestart) {
            recreate();
            recreateOnRestart = false;
        }
        else {
            mainFragment.updateFragment();
            if (fam.isMenuOpen()) fam.closeMenu();
        }
    }

    @Override
    public void onBackPressed() {
        if (fam.isMenuOpen()) fam.closeMenu();
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            SettingsActivity.startActivity(this);
            return true;
        }
        else if (itemId == R.id.action_filter_exercises) {
            ArrayList<String> types = DbReader.get(this).getTypes();
            String[] items = new String[types.size()];
            types.toArray(items);

            int[] checkedItems = CollectionUtilsKt.indicesOf(items, Prefs.getExerciseVisibleTypes().toArray());

            ChipDialog.newInstance(getString(R.string.dialog_title_title_filter), items, checkedItems,
                R.string.dialog_btn_filter, R.string.fw_dialog_btn_cancel, DIALOG_FILTER_EXERCISES, null)
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_add_distance) {
            DecimalDialog.newInstance(getString(R.string.dialog_title_add_distance), "", NO_FLOAT_TEXT,
                "", R.string.dialog_btn_add, R.string.fw_dialog_btn_cancel, DIALOG_ADD_DISTANCE, null)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_show_hidden_routes) {
            Prefs.showHiddenRoutes(!Prefs.areHiddenRoutesShown());
            mainFragment.updateFragment();
            invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // set

    private void initApp() {
        if (!appInitialized) {
            if (FileUtils.shouldAskPermissions(this)) FileUtils.askPermissions(this);
            Prefs.setUpAndLoad(this);
            appInitialized = true;
        }
    }

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_main);
        setSupportActionBar(tb);
        ab = getSupportActionBar();
    }

    public void setToolbarTitle(String title) {
        ab.setTitle(title);
    }

    private void setBottomNavbar() {
        selectFragment(new ExerciseListFragment());

        final BottomNavigationView.OnNavigationItemSelectedListener navItemListener = item -> {
            int itemId = item.getItemId();
            FloatingActionButton fab = fam.getFab();

            if (itemId == R.id.navigation_main_exercises) {
                if ((mainFragment instanceof ExerciseListFragment)) mainFragment.scrollToTop();
                else selectFragment(new ExerciseListFragment());
                if (fab.isOrWillBeHidden()) fab.show();
                return true;
            }
            else if (itemId == R.id.navigation_main_records) {
                if ((mainFragment instanceof RecordListFragment)) mainFragment.scrollToTop();
                else selectFragment(new RecordListFragment());
                if (fab.isOrWillBeShown()) fab.hide();
                return true;
            }
            else if (itemId == R.id.navigation_main_stats) {
                if ((mainFragment instanceof StatsFragment)) mainFragment.scrollToTop();
                else selectFragment(new StatsFragment());
                if (fab.isOrWillBeShown()) fab.hide();
                return true;
            }
            return false;
        };

        final BottomNavigationView navView = findViewById(R.id.nb_main);
        navView.setOnNavigationItemSelectedListener(navItemListener);
    }

    private void setFam() {
        fam = findViewById(R.id.fam);
        fam.onSetContentView();

        // add exercise manually
        fam.addItem(getString(R.string.tv_text_fab_add),
            R.drawable.ic_edit,
            View -> {
                ExerciseAddActivity.startActivity(MainActivity.this);
                return Unit.INSTANCE;
            }
        );

        // requst new from strava
        fam.addItem(getString(R.string.tv_text_fab_request_strava),
            R.drawable.ic_logo_strava,
            View -> {
                strava.requestNewActivities((successCount, errorCount) ->
                    StravaApi.toastResponse(successCount, errorCount, this));
                fam.closeMenu();
                return Unit.INSTANCE;
            }
        );
    }

    public void setRecyclerScrollListener(final RecyclerView rv, final RecyclerFragment recyclerFragment) {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // show/hide fab on scroll
                if (recyclerFragment instanceof ExerciseListRecyclerFragment) {
                    fam.updateVisibilityOnScroll(dy);
                }
            }
        });
    }

    private void selectFragment(MainFragment fragment) {
        this.mainFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_main, this.mainFragment)
            .commit();
    }

    // tools

    public void updateFragment() {
        mainFragment.updateFragment();
    }

    // implements dialogs

    @Override
    public void onDecimalDialogPositiveClick(float input, String tag, String passValue) {
        if (tag.equals(DIALOG_ADD_DISTANCE)) {
            int distance = (int) (input * 1000);
            DbWriter.get(this).addDistance(new Distance(-1, distance));
            mainFragment.updateFragment();
        }
    }

    @Override
    public void onMultiChoiceDialogItemsSelected(@NonNull boolean[] checkedItems, @NonNull String tag, String passValue) {
        if (tag.equals(DIALOG_FILTER_EXERCISES)) {
            ArrayList<String> visibleTypes = (ArrayList<String>)
                CollectionUtilsKt.filter(DbReader.get(this).getTypes(), checkedItems);

            Prefs.setExerciseVisibleTypes(visibleTypes);
            mainFragment.updateFragment();
        }
    }

    // implements SortSheet

    @Override
    public void onSortSheetItemClick(int selectedIndex) {
        mainFragment.onSortSheetClick(selectedIndex);
    }

}

























