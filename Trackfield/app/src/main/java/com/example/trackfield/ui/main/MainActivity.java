package com.example.trackfield.ui.main;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.data.network.StravaApi;
import com.example.trackfield.data.db.Writer;
import com.example.trackfield.utils.ScreenUtils;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.DecimalDialog;
import com.example.trackfield.ui.custom.dialog.FilterDialog;
import com.example.trackfield.ui.custom.sheet.SortSheet;
import com.example.trackfield.ui.main.exercises.ExercisesRecyclerFragment;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.FileUtils;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.main.exercises.ExercisesFragment;
import com.example.trackfield.ui.main.recs.RecsFragment;
import com.example.trackfield.ui.main.stats.StatsFragment;
import com.example.trackfield.ui.onboarding.OnboardingActivity;
import com.example.trackfield.ui.setting.SettingsActivity;
import com.example.trackfield.ui.map.TrackActivity;
import com.example.trackfield.ui.exercise.EditActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DecimalDialog.DialogListener,
    FilterDialog.DialogListener, SortSheet.DismissListener {

    private MainFragment mainFragment;
    private ActionBar ab;
    StravaApi strava;

    // fabs
    private FloatingActionButton fab, addFab, trackFab, stravaFab;
    private ConstraintLayout addCl, trackCl, stravaCl;
    private View overlayView;
    private boolean isFabMenuOpen = false;

    @Deprecated public static boolean gameOn = false;
    public static boolean recreate = false;

    // dialog tags
    private static final String DIALOG_FILTER_EXERCISES = "filterExercisesDialog";
    private static final String DIALOG_ADD_DISTANCE = "addDistanceDialog";

    private static final float OVERLAY_ΑLPHA = 0.96f;

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
        Writer.get(this).useUpdateToolIfEnabled(this);

        strava = StravaApi.getInstance(this);

        // layout
        setBottomNavbar();
        setToolbar();
        setFabs();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (recreate) {
            recreate();
            recreate = false;
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
        else if (itemId == R.id.action_filter) {
            FilterDialog.newInstance(R.string.dialog_title_filter, Prefs.getExerciseVisibleTypes(),
                R.string.dialog_btn_filter, DIALOG_FILTER_EXERCISES)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_addDistance) {
            DecimalDialog.newInstance(R.string.dialog_title_add_distance, BaseDialog.NO_RES, BaseDialog.NO_FLOAT_TEXT,
                "", R.string.dialog_btn_add, DIALOG_ADD_DISTANCE)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_showHidden) {
            Prefs.showHiddenRoutes(!Prefs.areHiddenRoutesShown());
            mainFragment.updateFragment();
            invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // set

    private void initApp() {
        if (!gameOn) {
            if (FileUtils.shouldAskPermissions(this)) FileUtils.askPermissions(this);
            Prefs.SetUpAndLoad(this);
            gameOn = true;
        }
    }

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_main);
        setSupportActionBar(tb);
        ab = getSupportActionBar();
    }

    public void setToolbarTitle(String title) {
        ab.setTitle(title);
    }

    private void setBottomNavbar() {
        selectFragment(new ExercisesFragment());
        final BottomNavigationView.OnNavigationItemSelectedListener navItemListener = item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_exercises) {
                if ((mainFragment instanceof ExercisesFragment)) mainFragment.scrollToTop();
                else selectFragment(new ExercisesFragment());
                if (fab.isOrWillBeHidden()) fab.show();
                return true;
            }
            else if (itemId == R.id.navigation_recs) {
                if ((mainFragment instanceof RecsFragment)) mainFragment.scrollToTop();
                else selectFragment(new RecsFragment());
                if (fab.isOrWillBeShown()) fab.hide();
                return true;
            }
            else if (itemId == R.id.navigation_dev) {
                if ((mainFragment instanceof StatsFragment)) mainFragment.scrollToTop();
                else selectFragment(new StatsFragment());
                if (fab.isOrWillBeShown()) fab.hide();
                return true;
            }
            return false;
        };

        final BottomNavigationView navView = findViewById(R.id.navbar);
        navView.setOnNavigationItemSelectedListener(navItemListener);
    }

    private void setFabs() {
        fab = findViewById(R.id.fab_menu);
        addFab = findViewById(R.id.fab_add);
        trackFab = findViewById(R.id.fab_track);
        stravaFab = findViewById(R.id.fab_strava);

        addCl = findViewById(R.id.constraintLayout_addFab);
        trackCl = findViewById(R.id.constraintLayout_trackFab);
        stravaCl = findViewById(R.id.constraintLayout_stravaFab);
        overlayView = findViewById(R.id.view_overlay);

        // hide
        //stravaCl.setVisibility(View.GONE);
        //trackCl.setVisibility(View.GONE);
        //addCl.setVisibility(View.GONE);
        //closeFabMenu();

        // click
        fab.setOnClickListener(v -> {
            if (isFabMenuOpen) closeFabMenu();
            else openFabMenu();
        });

        addFab.setOnClickListener(view -> EditActivity.startActivity(MainActivity.this));

        trackFab.setOnClickListener(view -> TrackActivity.startActivity(MainActivity.this));

        stravaFab.setOnClickListener(view -> {
            strava.requestLastActivity();
            closeFabMenu();
        });

        overlayView.setOnClickListener(view -> closeFabMenu());
    }

    public void setRecyclerScrollListener(final RecyclerView rv, final RecyclerFragment recyclerFragment) {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // fab
                if (recyclerFragment instanceof ExercisesRecyclerFragment) {
                    if (fab.isOrWillBeShown() && dy > 0) fab.hide();
                    else if (fab.isOrWillBeHidden() && dy < 0 && mainFragment instanceof ExercisesFragment) fab.show();
                }
            }

            /*@Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // toolbar
                if (!recyclerView.canScrollVertically(-1)) {
                    tb.setElevation(0);
                    ab.setElevation(0);
                }
            }*/
        });
    }

    private void selectFragment(MainFragment fragment) {
        this.mainFragment = fragment;
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.frameLayout_fragmentContainer, this.mainFragment)
            .commit();
    }

    // tools

    private void openFabMenu() {
        animateFab();
        addFab.show();
        //trackFab.show();
        stravaFab.show();

        LayoutUtils.crossfadeIn(addCl, 1);
        //L.crossfadeIn(trackCl, 1);
        LayoutUtils.crossfadeIn(stravaCl, 1);
        LayoutUtils.crossfadeIn(overlayView, OVERLAY_ΑLPHA);

        isFabMenuOpen = true;
    }

    private void closeFabMenu() {
        animateFab();
        stravaFab.hide();
        //trackFab.hide();
        addFab.hide();

        LayoutUtils.crossfadeOut(stravaCl);
        //L.crossfadeOut(trackCl);
        LayoutUtils.crossfadeOut(addCl);
        LayoutUtils.crossfadeOut(overlayView);

        isFabMenuOpen = false;
    }

    private void animateFab() {
        Context fabContext = fab.getContext();

        @ColorInt int primaryVariant = LayoutUtils.getColorInt(R.attr.colorPrimaryVariant, fabContext);
        @ColorInt int surface = LayoutUtils.getColorInt(R.attr.colorSurface, fabContext);

        @ColorInt int fromColor = isFabMenuOpen ? surface : primaryVariant;
        @ColorInt int toColor = !isFabMenuOpen ? surface : primaryVariant;
        Drawable toIcon = isFabMenuOpen ? getDrawable(R.drawable.ic_fab_base_24dp) :
            getDrawable(R.drawable.ic_cancel_fab_24dp);

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
            Writer.get(this).addDistance(new Distance(-1, distance));
            mainFragment.updateFragment();
        }
    }

    @Override
    public void onFilterDialogPositiveClick(@NonNull ArrayList<Integer> checkedTypes, String tag) {
        if (tag.equals(DIALOG_FILTER_EXERCISES)) {
            Prefs.setExerciseVisibleTypes(checkedTypes);
            mainFragment.updateFragment();
        }
    }

    // implements SortSheet

    @Override
    public void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst) {
        mainFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

    // class

    public static abstract class MainFragment extends Fragment {

        protected abstract void setToolbarTitle();

        protected abstract void scrollToTop();

        protected abstract void updateFragment();

        protected abstract void onSortSheetDismiss(Constants.SortMode sortMode, boolean smallestFirst);

    }

}

























