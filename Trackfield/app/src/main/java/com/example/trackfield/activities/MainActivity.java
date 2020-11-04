package com.example.trackfield.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.database.ApiManager;
import com.example.trackfield.database.Helper;
import com.example.trackfield.fragments.ActivitiesFragment;
import com.example.trackfield.fragments.dialogs.Dialogs;
import com.example.trackfield.fragments.RecsFragment;
import com.example.trackfield.fragments.RecyclerFragments;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends ApiManager implements Dialogs.DecimalDialog.DialogListener, Dialogs.FilterDialog.DialogListener {

    private Helper.Reader reader;
    private Helper.Writer writer;
    private MainFragment fragment;
    private ActionBar ab;

    // fabs
    private FloatingActionButton fab, addFab, trackFab;
    private ConstraintLayout addCl, trackCl;
    private View overlayView;
    private boolean isFabMenuOpen = true;

    public static boolean recreate = false;

    private static final float OVERLAY_ΑLPHA = 0.96f;

    ////

    @Override protected void onCreate(Bundle savedInstanceState) {

        load();
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L.setScale(this);

        //if (reader == null) reader = new Helper.Reader(this);
        writer = Helper.getWriter(this);
        reader = Helper.getReader(this);

        if (Helper.Writer.useUpdateTool) {
            //Helper.Writer w = new Helper.Writer(this);
            writer.updateTool(this);
            //w.close();
        }

        setNavbar();
        setToolbar();
        setFabs();

        connectApi();
    }

    private void load() {
        if (!D.gameOn) {
            if (F.shouldAskPermissions(this)) { F.askPermissions(this); }
            F.loadPrefs(this);
            F.loadExternal(this);
            D.gameOn = true;
        }
    }


    // fab
    private void setFabs() {

        fab = findViewById(R.id.fab_menu);
        addFab = findViewById(R.id.fab_add);
        trackFab = findViewById(R.id.fab_track);

        addCl = findViewById(R.id.constraintLayout_addFab);
        trackCl = findViewById(R.id.constraintLayout_trackFab);
        overlayView = findViewById(R.id.view_overlay);

        closeFabMenu();

        // click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isFabMenuOpen) closeFabMenu();
                else openFabMenu();
            }
        });

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                EditActivity.startActivity(MainActivity.this);
            }
        });

        trackFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                TrackActivity.startActivity(MainActivity.this);
            }
        });

        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                closeFabMenu();
            }
        });

    }
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

    public void recyclerScrollListener(final RecyclerView rv, final RecyclerFragments.Base recyclerFragment) {

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // fab
                if (recyclerFragment instanceof RecyclerFragments.ExerciseRF) {
                    if (fab.isOrWillBeShown() && dy > 0) fab.hide();
                    else if (fab.isOrWillBeHidden() && dy < 0 && fragment instanceof ActivitiesFragment) fab.show();
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
    public Helper.Reader getReader() {
        return reader;
    }

    // tools
    private void selectFragment(MainFragment fragment) {
        this.fragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_fragmentContainer, this.fragment).commit();
    }

    // toolbars
    private void setNavbar() {

        selectFragment(new ActivitiesFragment());
        final BottomNavigationView.OnNavigationItemSelectedListener navItemListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_exercises:
                        if ((fragment instanceof ActivitiesFragment)) fragment.scrollToTop();
                        else selectFragment(new ActivitiesFragment());
                        if (fab.isOrWillBeHidden()) fab.show();
                        return true;

                    case R.id.navigation_recs:
                        if ((fragment instanceof RecsFragment)) fragment.scrollToTop();
                        else selectFragment(new RecsFragment());
                        if (fab.isOrWillBeShown()) fab.hide();
                        return true;
                }
                return false;
            }
        };

        final BottomNavigationView navView = findViewById(R.id.navbar);
        navView.setOnNavigationItemSelectedListener(navItemListener);
    }
    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_main);
        setSupportActionBar(tb);
        ab = getSupportActionBar();
    }
    public void setToolbarTitle(String title) {
        ab.setTitle(title);
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.startActivity(this);
                return true;

            case R.id.action_filter:
                Dialogs.FilterExercises.newInstance(D.exerciseVisibleTypes, getSupportFragmentManager());
                return true;

            case R.id.action_addDistance:
                Dialogs.AddDistance.newInstance(Dialogs.Base.NO_TEXT, getSupportFragmentManager());
                return true;

            case R.id.action_import: // from toolbox
                //Helper.Writer writer = new Helper.Writer(this);
                writer.importFromToolbox(this);
                //writer.close();
                return true;

            case R.id.action_export: // to toolbox
                D.exercises.clear();
                D.exercises.addAll(reader.getExercises());
                return true;

            case R.id.action_recreate:
                //Helper.Writer writer2 = new Helper.Writer(this);
                writer.recreate();
                //writer2.close();

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onDecimalDialogPositiveClick(float input, String tag) {

        int distance = (int) (input * 1000);
        D.addDistance(distance);

        //Helper.Writer writer = new Helper.Writer(this);
        writer.addDistance(new Distance(-1, distance));
        //writer.close();
        fragment.updateFragment();
    }
    @Override public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
        D.exerciseVisibleTypes = checkedTypes;
        fragment.updateFragment();
    }

    @Override protected void onRestart() {
        super.onRestart();
        if (recreate) { recreate(); recreate = false; }
        else { fragment.updateFragment(); if (isFabMenuOpen) closeFabMenu(); }
    }
    @Override protected void onDestroy() {
        //reader.close();
        Helper.closeReader();
        Helper.closeWriter();
        super.onDestroy();
    }
    @Override public void onBackPressed() {
        if (isFabMenuOpen) closeFabMenu();
        else super.onBackPressed();
    }

    public static abstract class MainFragment extends Fragment {
        protected abstract void setToolbarTitle();
        protected abstract void scrollToTop();
        protected abstract void updateFragment();
    }

}

























