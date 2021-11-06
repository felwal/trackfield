package com.felwal.trackfield.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.android.util.CollectionUtilsKt;
import com.felwal.android.util.ResUtilsKt;
import com.felwal.android.widget.dialog.ChipDialog;
import com.felwal.android.widget.dialog.DecimalDialog;
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
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.felwal.android.widget.dialog.DecimalDialogKt.NO_FLOAT_TEXT;

public class MainActivity extends AppCompatActivity implements DecimalDialog.DialogListener,
    ChipDialog.DialogListener, SortSheet.SheetListener {

    public static boolean recreateOnRestart = false;

    // dialog tags
    private static final String DIALOG_FILTER_EXERCISES = "filterExercisesDialog";
    private static final String DIALOG_ADD_DISTANCE = "addDistanceDialog";

    private static final float OVERLAY_ALPHA = 0.96f;
    private static boolean appInitialized = false;

    private MainFragment mainFragment;
    private ActionBar ab;
    private StravaApi strava;

    // fabs
    private FloatingActionButton fab, addFab, stravaFab;
    private ConstraintLayout addCl, stravaCl;
    private View overlayView;
    private boolean isFabMenuOpen = false;

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
        setFabs();
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
            if (isFabMenuOpen) closeFabMenu();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFabMenuOpen) closeFabMenu();
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

            ChipDialog.newInstance(getString(R.string.dialog_title_title_filter),
                getString(R.string.tv_text_dialog_filter_msg), items, checkedItems,
                R.string.dialog_btn_filter, R.string.dialog_btn_cancel, DIALOG_FILTER_EXERCISES)
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_add_distance) {
            DecimalDialog.newInstance(getString(R.string.dialog_title_add_distance), "", NO_FLOAT_TEXT,
                "", R.string.dialog_btn_add, R.string.dialog_btn_cancel, DIALOG_ADD_DISTANCE)
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

    private void setFabs() {
        fab = findViewById(R.id.fab_main_menu);
        addFab = findViewById(R.id.fab_main_add);
        stravaFab = findViewById(R.id.fab_main_strava);

        addCl = findViewById(R.id.cl_main_fabs_add);
        stravaCl = findViewById(R.id.cl_main_fabs_strava);
        overlayView = findViewById(R.id.v_main_overlay);

        // clicks

        fab.setOnClickListener(v -> {
            if (isFabMenuOpen) closeFabMenu();
            else openFabMenu();
        });

        addFab.setOnClickListener(view -> ExerciseAddActivity.startActivity(MainActivity.this));

        stravaFab.setOnClickListener(view -> {
            strava.requestNewActivities((successCount, errorCount) ->
                StravaApi.toastResponse(successCount, errorCount, this));
            closeFabMenu();
        });

        overlayView.setOnClickListener(view -> closeFabMenu());
    }

    public void setRecyclerScrollListener(final RecyclerView rv, final RecyclerFragment recyclerFragment) {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // show/hide fab on scroll
                if (recyclerFragment instanceof ExerciseListRecyclerFragment) {
                    if (fab.isOrWillBeShown() && dy > 0) fab.hide();
                    else if (fab.isOrWillBeHidden() && dy < 0 && mainFragment instanceof ExerciseListFragment)
                        fab.show();
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

    private void openFabMenu() {
        animateFab();
        addFab.show();
        stravaFab.show();

        LayoutUtils.crossfadeIn(addCl, 1);
        LayoutUtils.crossfadeIn(stravaCl, 1);
        LayoutUtils.crossfadeIn(overlayView, OVERLAY_ALPHA);

        isFabMenuOpen = true;
    }

    private void closeFabMenu() {
        animateFab();
        stravaFab.hide();
        addFab.hide();

        LayoutUtils.crossfadeOut(stravaCl);
        LayoutUtils.crossfadeOut(addCl);
        LayoutUtils.crossfadeOut(overlayView);

        isFabMenuOpen = false;
    }

    @SuppressLint("ResourceType")
    private void animateFab() {
        Context fabContext = fab.getContext();
        @ColorInt int primaryVariant = ResUtilsKt.getColorAttr(fabContext, R.attr.colorPrimaryVariant);
        @ColorInt int surface = ResUtilsKt.getColorAttr(fabContext, R.attr.colorSurface);

        @ColorInt int fromColor, toColor;
        Drawable toIcon;

        // animate to closed menu
        if (isFabMenuOpen) {
            fromColor = surface;
            toColor = primaryVariant;
            toIcon = ResUtilsKt.getDrawableCompatFilter(this,
                R.drawable.ic_add, android.R.attr.textColorPrimaryInverse);
        }
        // animate to open menu
        else {
            fromColor = primaryVariant;
            toColor = surface;
            toIcon = ResUtilsKt.getDrawableCompatFilter(this,
                R.drawable.ic_cancel, android.R.attr.textColorSecondary);
        }

        LayoutUtils.animateFab(fab, fromColor, toColor, toIcon);
    }

    public void updateFragment() {
        mainFragment.updateFragment();
    }

    // implements dialogs

    @Override
    public void onDecimalDialogPositiveClick(float input, String tag) {
        if (tag.equals(DIALOG_ADD_DISTANCE)) {
            int distance = (int) (input * 1000);
            DbWriter.get(this).addDistance(new Distance(-1, distance));
            mainFragment.updateFragment();
        }
    }

    @Override
    public void onChipDialogPositiveClick(@NonNull boolean[] checkedItems, @NonNull String tag) {
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

























