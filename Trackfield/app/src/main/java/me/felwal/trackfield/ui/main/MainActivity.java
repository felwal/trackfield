package me.felwal.trackfield.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import kotlin.Unit;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.InputDialog;
import me.felwal.android.fragment.sheet.SortSheet;
import me.felwal.android.util.MenuKt;
import me.felwal.android.widget.FloatingActionMenu;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.android.widget.control.InputOption;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Distance;
import me.felwal.trackfield.data.network.StravaService;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.base.ExerciseFilter;
import me.felwal.trackfield.ui.base.ExerciseFilterActivity;
import me.felwal.trackfield.ui.base.RecyclerFragment;
import me.felwal.trackfield.ui.exercisedetail.ExerciseAddActivity;
import me.felwal.trackfield.ui.main.exerciselist.ExerciseListFragment;
import me.felwal.trackfield.ui.main.exerciselist.ExerciseListRecyclerFragment;
import me.felwal.trackfield.ui.main.groupingpager.GroupingPagerFragment;
import me.felwal.trackfield.ui.main.statistics.StatisticsFragment;
import me.felwal.trackfield.ui.onboarding.OnboardingActivity;
import me.felwal.trackfield.ui.setting.SettingsActivity;
import me.felwal.trackfield.utils.FileUtils;
import me.felwal.trackfield.utils.MathUtils;
import me.felwal.trackfield.utils.ScreenUtils;

public class MainActivity extends ExerciseFilterActivity implements InputDialog.DialogListener,
    SortSheet.SheetListener {

    public static boolean recreateOnRestart = false;
    public static boolean updateFragmentOnRestart = false;

    // dialog tags
    private static final String DIALOG_ADD_DISTANCE = "addDistanceDialog";

    private static boolean appInitialized = false;

    private MainFragment mainFragment;
    private ActionBar ab;
    private StravaService strava;

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

        strava = new StravaService(this);

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
            if (updateFragmentOnRestart) {
                mainFragment.updateFragment();
                updateFragmentOnRestart = false;
            }
            if (fam.isMenuOpen()) fam.closeMenu();
        }
    }

    @Override
    public void onBackPressed() {
        if (fam.isMenuOpen()) fam.closeMenu();
        else super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // set "filter" check state
        MenuItem filterItem = menu.findItem(R.id.action_filter_exercises);
        MenuKt.fixIconCheckState(filterItem);
        filterItem.setChecked(Prefs.getMainFilter().isActive());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            SettingsActivity.startActivity(this);
            return true;
        }
        else if (itemId == R.id.action_filter_exercises) {
            showFilterSheet();
            return true;
        }
        else if (itemId == R.id.action_add_distance) {
            InputDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_add_distance), "",
                    R.string.dialog_btn_add, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_ADD_DISTANCE, null),
                new InputOption("", "", EditorInfo.TYPE_NUMBER_FLAG_DECIMAL))
                .show(getSupportFragmentManager());

            return true;
        }
        else if (itemId == R.id.action_show_hidden_groups) {
            Prefs.showHiddenGroups(!Prefs.areHiddenGroupsShown());
            mainFragment.updateFragment();
            invalidateOptionsMenu();

            return true;
        }
        else if (itemId == R.id.action_add_place) {
            // TODO
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
            else if (itemId == R.id.navigation_main_groupingpager) {
                if ((mainFragment instanceof GroupingPagerFragment)) mainFragment.scrollToTop();
                else selectFragment(new GroupingPagerFragment());
                if (fab.isOrWillBeShown()) fab.hide();
                return true;
            }
            else if (itemId == R.id.navigation_main_statistics) {
                if ((mainFragment instanceof StatisticsFragment)) mainFragment.scrollToTop();
                else selectFragment(new StatisticsFragment());
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
                    StravaService.toastResponse(successCount, errorCount, this));
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

    @NonNull @Override
    public ExerciseFilter getFilter() {
        return Prefs.getMainFilter();
    }

    @Override
    public void applyTypeFilter(@NonNull ArrayList<String> visibleTypes) {
        Prefs.setMainVisibleTypes(visibleTypes);
    }

    @Override
    public void applyLabelFilter(@NonNull ArrayList<String> visibleLabels) {
        Prefs.setMainVisibleLabels(visibleLabels);
    }

    // implements dialogs

    @Override
    public void onInputDialogPositiveClick(@NonNull String input, String tag, String passValue) {
        if (tag.equals(DIALOG_ADD_DISTANCE)) {
            int distance = (int) MathUtils.round(Float.parseFloat(input) * 1000, 0);
            DbWriter.get(this).addDistance(new Distance(-1, distance));
            mainFragment.updateFragment();
        }
    }

    @Override
    public void onExerciseFilter() {
        // the recyclers' content may have been updated
        mainFragment.updateFragment();
        // the filter MenuItem's state has been updated
        invalidateOptionsMenu();
    }

    @Override
    public void onSortSheetItemClick(int selectedIndex) {
        mainFragment.onSortSheetClick(selectedIndex);
    }

}
