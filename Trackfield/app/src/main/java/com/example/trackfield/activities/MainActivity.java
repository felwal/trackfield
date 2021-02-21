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
import com.example.trackfield.api.StravaAPI;
import com.example.trackfield.database.Helper;
import com.example.trackfield.fragments.recycler_fragments.ExRecyclerFragment;
import com.example.trackfield.fragments.recycler_fragments.RecyclerFragment;
import com.example.trackfield.fragments.StatsFragment;
import com.example.trackfield.fragments.recycler_fragments.ExercisesFragment;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.dialogs.DecimalDialog;
import com.example.trackfield.fragments.RecsFragment;
import com.example.trackfield.dialogs.FilterDialog;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.F;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends StravaAPI implements DecimalDialog.DialogListener, FilterDialog.DialogListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        init();
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L.setScale(this);

        //Prefs.setFirstLogin(true);
        if (Prefs.isFirstLogin()) BoardingActivity.startActivity(this);

        //if (reader == null) reader = new Helper.Reader(this);
        writer = Helper.getWriter(this);
        Helper.openReader(this);

        if (Helper.Writer.useUpdateTool) {
            //Helper.Writer w = new Helper.Writer(this);
            writer.updateTool(this);
            //w.close();
        }

        connectStrava();
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
            fragment.updateFragment();
            if (isFabMenuOpen) closeFabMenu();
        }
    }

    @Override
    protected void onDestroy() {
        //reader.close();
        Helper.closeReader();
        Helper.closeWriter();
        super.onDestroy();
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
                        .show(getSupportFragmentManager());;
                return true;

            case R.id.action_addDistance:
                DecimalDialog.newInstance(getString(R.string.dialog_title_add_distance), "", BaseDialog.NO_TEXT, "", R.string.dialog_btn_add, "addDistance")
                        .show(getSupportFragmentManager());
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    // set

    private void init() {
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

    private void setBottomNavbar() {

        selectFragment(new ExercisesFragment());
        final BottomNavigationView.OnNavigationItemSelectedListener navItemListener = item -> {

            switch (item.getItemId()) {
                case R.id.navigation_exercises:
                    if ((fragment instanceof ExercisesFragment)) fragment.scrollToTop();
                    else selectFragment(new ExercisesFragment());
                    if (fab.isOrWillBeHidden()) fab.show();
                    return true;

                case R.id.navigation_recs:
                    if ((fragment instanceof RecsFragment)) fragment.scrollToTop();
                    else selectFragment(new RecsFragment());
                    if (fab.isOrWillBeShown()) fab.hide();
                    return true;

                case R.id.navigation_dev:
                    if ((fragment instanceof StatsFragment)) fragment.scrollToTop();
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

        //trackFab.setOnClickListener(view -> TrackActivity.startActivity(MainActivity.this));
        trackFab.setOnClickListener(view -> requestLastActivity());

        overlayView.setOnClickListener(view -> closeFabMenu());

    }

    public void setRecyclerScrollListener(final RecyclerView rv, final RecyclerFragment recyclerFragment) {

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // fab
                if (recyclerFragment instanceof ExRecyclerFragment) {
                    if (fab.isOrWillBeShown() && dy > 0) fab.hide();
                    else if (fab.isOrWillBeHidden() && dy < 0 && fragment instanceof ExercisesFragment) fab.show();
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

    private void selectFragment(MainFragment fragment) {
        this.fragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_fragmentContainer, this.fragment).commit();
    }

    public void setToolbarTitle(String title) {
        ab.setTitle(title);
    }

    // implements

    @Override
    public void onDecimalDialogPositiveClick(float input, String tag) {

        int distance = (int) (input * 1000);
        D.addDistance(distance);

        //Helper.Writer writer = new Helper.Writer(this);
        writer.addDistance(new Distance(-1, distance));
        //writer.close();
        fragment.updateFragment();
    }

    @Override
    public void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag) {
        Prefs.setExerciseVisibleTypes(checkedTypes);
        fragment.updateFragment();
    }

    // class

    public static abstract class MainFragment extends Fragment {
        protected abstract void setToolbarTitle();
        protected abstract void scrollToTop();
        protected abstract void updateFragment();
    }

}

























