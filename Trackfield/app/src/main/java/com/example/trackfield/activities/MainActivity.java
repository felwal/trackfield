package com.example.trackfield.activities;

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
import com.example.trackfield.database.Writer;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.DecimalDialog;
import com.example.trackfield.dialogs.FilterDialog;
import com.example.trackfield.dialogs.sheets.SortSheet;
import com.example.trackfield.fragments.ExercisesFragment;
import com.example.trackfield.fragments.RecsFragment;
import com.example.trackfield.fragments.StatsFragment;
import com.example.trackfield.fragments.recyclerfragments.ExercisesRecyclerFragment;
import com.example.trackfield.fragments.recyclerfragments.RecyclerFragment;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.F;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DecimalDialog.DialogListener, FilterDialog.DialogListener, SortSheet.DismissListener {

    private MainFragment mainFragment;
    private ActionBar ab;

    // fabs
    private FloatingActionButton fab, addFab, trackFab;
    private ConstraintLayout addCl, trackCl;
    private View overlayView;
    private boolean isFabMenuOpen = true;

    public static boolean recreate = false;

    private static final float OVERLAY_ΑLPHA = 0.96f;

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initApp();
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L.setScale(this);

        // TODO: prefs
        Prefs.setDeveloper(true);
        //Prefs.setFirstLogin(true);
        if (Prefs.isFirstLogin()) BoardingActivity.startActivity(this);

        // TODO: dev tool
        Writer.get(this).useUpdateToolIfEnabled(this);

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
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.startActivity(this);
                return true;

            case R.id.action_filter:
                FilterDialog.newInstance(getString(R.string.dialog_title_filter), Prefs.getExerciseVisibleTypes(), R.string.dialog_btn_filter, "filterExercises")
                        .show(getSupportFragmentManager());
                return true;

            case R.id.action_addDistance:
                DecimalDialog.newInstance(getString(R.string.dialog_title_add_distance), "", BaseDialog.NO_TEXT, "", R.string.dialog_btn_add, "addDistance")
                        .show(getSupportFragmentManager());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // set

    private void initApp() {
        if (!D.gameOn) {
            if (F.shouldAskPermissions(this)) F.askPermissions(this);
            Prefs.SetUpAndLoad(this);
            //F.loadPrefs(this);
            //F.loadExternal(this);
            D.gameOn = true;
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

            switch (item.getItemId()) {
                case R.id.navigation_exercises:
                    if ((mainFragment instanceof ExercisesFragment)) mainFragment.scrollToTop();
                    else selectFragment(new ExercisesFragment());
                    if (fab.isOrWillBeHidden()) fab.show();
                    return true;

                case R.id.navigation_recs:
                    if ((mainFragment instanceof RecsFragment)) mainFragment.scrollToTop();
                    else selectFragment(new RecsFragment());
                    if (fab.isOrWillBeShown()) fab.hide();
                    return true;

                case R.id.navigation_dev:
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

        addCl = findViewById(R.id.constraintLayout_addFab);
        trackCl = findViewById(R.id.constraintLayout_trackFab);
        overlayView = findViewById(R.id.view_overlay);

        // hide
        trackCl.setVisibility(View.GONE);
        addCl.setVisibility(View.GONE);
        closeFabMenu();

        // click
        fab.setOnClickListener(v -> {
            if (isFabMenuOpen) closeFabMenu();
            else openFabMenu();
        });

        addFab.setOnClickListener(view -> EditActivity.startActivity(MainActivity.this));

        trackFab.setOnClickListener(view -> TrackActivity.startActivity(MainActivity.this));

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
                    else if (fab.isOrWillBeHidden() && dy < 0 && mainFragment instanceof ExercisesFragment)
                        fab.show();
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
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_fragmentContainer, this.mainFragment).commit();
    }

    // tools

    private void openFabMenu() {
        animateFab();
        addFab.show();
        trackFab.show();

        L.crossfadeIn(addCl, 1);
        L.crossfadeIn(trackCl, 1);
        L.crossfadeIn(overlayView, OVERLAY_ΑLPHA);

        isFabMenuOpen = true;
    }

    private void closeFabMenu() {
        animateFab();
        trackFab.hide();
        addFab.hide();

        L.crossfadeOut(trackCl);
        L.crossfadeOut(addCl);
        L.crossfadeOut(overlayView);

        isFabMenuOpen = false;
    }

    private void animateFab() {
        Context fabContext = fab.getContext();

        @ColorInt int primaryVariant = L.getColorInt(R.attr.colorPrimaryVariant, fabContext);
        @ColorInt int surface = L.getColorInt(R.attr.colorSurface, fabContext);

        @ColorInt int fromColor = isFabMenuOpen ? surface : primaryVariant;
        @ColorInt int toColor = !isFabMenuOpen ? surface : primaryVariant;
        Drawable toIcon = isFabMenuOpen ? getDrawable(R.drawable.ic_fab_base_24dp) : getDrawable(R.drawable.ic_cancel_fab_24dp);

        L.animateFab(fab, fromColor, toColor, toIcon);
    }

    // implements dialogs

    @Override
    public void onDecimalDialogPositiveClick(float input, String tag) {
        int distance = (int) (input * 1000);
        D.addDistance(distance);

        Writer.get(this).addDistance(new Distance(-1, distance));
        mainFragment.updateFragment();
    }

    @Override
    public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
        Prefs.setExerciseVisibleTypes(checkedTypes);
        mainFragment.updateFragment();
    }

    // implements SortSheet

    @Override
    public void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst) {
        mainFragment.onSortSheetDismiss(sortMode, smallestFirst);
    }

    // class

    public static abstract class MainFragment extends Fragment {

        protected abstract void setToolbarTitle();

        protected abstract void scrollToTop();

        protected abstract void updateFragment();

        protected abstract void onSortSheetDismiss(C.SortMode sortMode, boolean smallestFirst);

    }

}

























