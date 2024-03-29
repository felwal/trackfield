package me.felwal.trackfield.ui.exercisedetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.jetbrains.annotations.Nullable;

import me.felwal.android.fragment.dialog.AlertDialog;
import me.felwal.android.fragment.dialog.BaseDialogKt;
import me.felwal.android.fragment.dialog.CheckDialog;
import me.felwal.android.fragment.dialog.MultiChoiceDialog;
import me.felwal.android.util.MenuKt;
import me.felwal.android.util.ResourcesKt;
import me.felwal.android.widget.control.CheckListOption;
import me.felwal.android.widget.control.DialogOption;
import me.felwal.trackfield.BuildConfig;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.DbWriter;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.db.model.Place;
import me.felwal.trackfield.data.network.StravaService;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.groupdetail.distancedetail.DistanceDetailActivity;
import me.felwal.trackfield.ui.groupdetail.intervaldetail.IntervalDetailActivity;
import me.felwal.trackfield.ui.groupdetail.placedetail.PlaceDetailActivity;
import me.felwal.trackfield.ui.groupdetail.routedetail.RouteDetailActivity;
import me.felwal.trackfield.ui.main.MainActivity;
import me.felwal.trackfield.ui.map.ExerciseMapActivity;
import me.felwal.trackfield.ui.map.RouteMapActivity;
import me.felwal.trackfield.utils.AppConsts;
import me.felwal.trackfield.utils.LayoutUtils;
import me.felwal.trackfield.utils.MathUtils;
import me.felwal.trackfield.utils.ScreenUtils;

public class ExerciseDetailActivity extends AppCompatActivity implements AlertDialog.DialogListener,
    MultiChoiceDialog.DialogListener, OnMapReadyCallback, Style.OnStyleLoaded {

    public static final int FROM_NONE = 0;
    public static final int FROM_DISTANCE = 1;
    public static final int FROM_ROUTE = 2;
    public static final int FROM_INTERVAL = 3;
    public static final int FROM_PLACE = 4;

    // extras names
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_FROM = "from";

    // dialog tags
    private static final String DIALOG_DELETE_EXERCISE = "deleteExerciseDialog";
    private static final String DIALOG_PULL = "pullDialog";

    private static final int MAP_MAX_ZOOM = 17;
    private static final int MAP_PADDING = 50;

    // arguments
    private int exerciseId;
    private Exercise exercise;
    private int from = FROM_NONE;

    private MapView mapView;
    private MapboxMap mapboxMap;

    //

    public static void startActivity(Context c, int exerciseId) {
        Intent intent = new Intent(c, ExerciseDetailActivity.class);
        intent.putExtra(EXTRA_ID, exerciseId);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int exerciseId, int from) {
        Intent intent = new Intent(c, ExerciseDetailActivity.class);
        intent.putExtra(EXTRA_ID, exerciseId);
        intent.putExtra(EXTRA_FROM, from);
        c.startActivity(intent);
    }

    // extends AppCompatActivitys

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_DOWNLOADS_TOKEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercisedetail);
        setToolbar();

        // extras
        Intent intent = getIntent();
        exerciseId = intent.getIntExtra(EXTRA_ID, Exercise.ID_NONE);
        from = intent.getIntExtra(EXTRA_FROM, FROM_NONE);

        // get exercise
        exercise = DbReader.get(this).getExercise(exerciseId);
        if (exercise == null) {
            finish();
            return;
        }

        // map
        mapView = findViewById(R.id.mv_exercisedetail_map);
        mapView.onCreate(savedInstanceState);

        setMap();
        setTexts();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // reload in case anything has been updated
        exercise = DbReader.get(this).getExercise(exerciseId);
        setMap();
        setTexts();

        // reload if exercise has been updated
        // doesnt work
        /*Exercise updated = DbReader.get(this).getExercise(exerciseId);
        if (!exercise.equals(updated)) {
            exercise = updated;
            setMap();
            setTexts();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_exercisedetail, menu);
        MenuKt.createOptionalIcons(menu);

        // remove actions dependent on externalId
        if (!exercise.hasStravaId()) {
            menu.findItem(R.id.action_pull_exercise).setVisible(false);
        }

        // remove actions dependent on trail
        if (!exercise.hasTrail()) {
            menu.findItem(R.id.action_hide_trail).setVisible(false);
            menu.findItem(R.id.action_recalibrate_endpoints).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuKt.prepareOptionalIcons(menu, this);

        // hide trail
        MenuItem hideItem = menu.findItem(R.id.action_hide_trail);
        hideItem.setChecked(exercise.isTrailHidden());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.action_edit_exercise) {
            ExerciseEditActivity.startActivity(this, exerciseId);
            return true;
        }
        else if (itemId == R.id.action_delete_exercise) {
            AlertDialog.newInstance(
                new DialogOption(getString(R.string.dialog_title_delete_exercise),
                    getString(R.string.dialog_msg_delete_exercise),
                    R.string.dialog_btn_delete, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                    DIALOG_DELETE_EXERCISE, null))
                .show(getSupportFragmentManager());

            MainActivity.updateFragmentOnRestart = true;
            return true;
        }
        else if (itemId == R.id.action_pull_exercise) {
            if (exercise.hasStravaId()) {
                CheckDialog.newInstance(
                    new DialogOption(getString(R.string.dialog_title_pull), "",
                        R.string.dialog_btn_pull, R.string.fw_dialog_btn_cancel, BaseDialogKt.NO_RES,
                        DIALOG_PULL, null),
                    new CheckListOption(Prefs.getPullOptions().getTexts(), Prefs.getPullOptions().getChecked(), null))
                    .show(getSupportFragmentManager());

                MainActivity.updateFragmentOnRestart = true;
            }
            else {
                // TODO: snackbar with 'remove id' action?
                LayoutUtils.toast(R.string.toast_strava_pull_activity_gone, this);
            }
            return true;
        }
        else if (itemId == R.id.action_recalibrate_endpoints) {
            if (exercise.hasTrail()) {
                exercise.getTrail().calibrateEndPoints();
                DbWriter.get(this).updateExercise(exercise, this);
            }
            return true;
        }
        else if (itemId == R.id.action_hide_trail) {
            if (exercise.hasTrail()) {
                exercise.invertTrailHidden();
                DbWriter.get(this).updateExercise(exercise, this);
                // to get immediate check feedback (before the menu closes), update it here,
                // instead of calling invalidateOptionsMenu(), since that also resets the optional icon colors.
                item.setChecked(exercise.isTrailHidden());
            }
            return true;
        }
        else if (itemId == R.id.action_map_routevar) {
            RouteMapActivity.startActivity(exercise.getRouteId(), exercise.getRouteVar(), this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    // set

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_exercisedetail);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.activity_title_exercisedetail));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setTexts() {
        // get
        TextView routeTv = findViewById(R.id.tv_exercisedetail_route);
        TextView routeVarTv = findViewById(R.id.tv_exercisedetail_routevar);
        TextView intervalTv = findViewById(R.id.tv_exercisedetail_interval);
        TextView dateTv = findViewById(R.id.tv_exercisedetail_date);
        TextView distanceTv = findViewById(R.id.tv_exercisedetail_distance);
        TextView timeTv = findViewById(R.id.tv_exercisedetail_time);
        TextView paceTv = findViewById(R.id.tv_exercisedetail_velocity);
        TextView energyTv = findViewById(R.id.tv_exercisedetail_energy);
        TextView powerTv = findViewById(R.id.tv_exercisedetail_power);
        TextView startTv = findViewById(R.id.tv_exercisedetail_start);
        TextView endTv = findViewById(R.id.tv_exercisedetail_end);
        TextView elevationTv = findViewById(R.id.tv_exercisedetail_elevation);
        TextView heartrateTv = findViewById(R.id.tv_exercisedetail_heartrate);
        TextView noteTv = findViewById(R.id.tv_exercisedetail_note);
        TextView typeTv = findViewById(R.id.tv_exercisedetail_type);
        TextView labelTv = findViewById(R.id.tv_exercisedetail_label);
        TextView deviceTv = findViewById(R.id.tv_exercisedetail_device);
        TextView recordingMethodTv = findViewById(R.id.tv_exercisedetail_recording_method);

        // set

        routeTv.setText(exercise.getRoute());
        routeVarTv.setText(exercise.getRouteVar());
        dateTv.setText(exercise.getDateTime().format(AppConsts.FORMATTER_VIEW));
        setTvHideIfEmpty(exercise.getNote(), noteTv);

        setTvHideIfEmpty(exercise.getType(), typeTv);
        setTvHideIfEmpty(exercise.getLabel(), labelTv);
        setTvHideIfEmpty(exercise.getDevice(), deviceTv);
        setTvHideIfEmpty(exercise.getRecordingMethod(), recordingMethodTv);

        setTvHideIfEmpty(exercise.getInterval(), intervalTv, findViewById(R.id.iv_exercisedetail_interval));
        setTvHideIfEmpty(exercise.printDistance(false, this), distanceTv, findViewById(R.id.iv_exercisedetail_distance));
        setTvHideIfEmpty(exercise.printTime(true), timeTv, findViewById(R.id.iv_exercisedetail_time));
        setTvHideIfEmpty(exercise.printPace(true, this), paceTv, findViewById(R.id.iv_exercisedetail_pace));
        setTvHideIfEmpty(exercise.printEnergy(this), energyTv, findViewById(R.id.iv_exercisedetail_energy));
        setTvHideIfEmpty(exercise.printPower(this), powerTv, findViewById(R.id.iv_exercisedetail_power));
        setTvHideIfEmpty(exercise.printElevation(), elevationTv, findViewById(R.id.iv_exercisedetail_elevation));
        setTvHideIfEmpty(exercise.printAvgHeartrate(), heartrateTv, findViewById(R.id.iv_exercisedetail_heartrate));

        // set start and end tvs's and listeners
        if (exercise.hasTrail()) {
            Place start = exercise.getStartPlace(this);
            Place end = exercise.getEndPlace(this);

            startTv.setText(start.getName());

            // set end only if not the same as start
            if (!start.equals(end)) endTv.setText(end.getName());
            else {
                endTv.setVisibility(View.GONE);
                findViewById(R.id.iv_exercisedetail_end).setVisibility(View.GONE);
            }

            // shortcuts to groups
            startTv.setOnClickListener(v -> {
                if (from == FROM_PLACE) finish();
                else PlaceDetailActivity.startActivity(ExerciseDetailActivity.this, start.getId(), exercise.getId());
            });
            endTv.setOnClickListener(v -> {
                if (from == FROM_PLACE) finish();
                else PlaceDetailActivity.startActivity(ExerciseDetailActivity.this, end.getId(), exercise.getId());
            });
        }
        else {
            startTv.setVisibility(View.GONE);
            endTv.setVisibility(View.GONE);
            findViewById(R.id.iv_exercisedetail_start).setVisibility(View.GONE);
            findViewById(R.id.iv_exercisedetail_end).setVisibility(View.GONE);
        }

        // set listeners

        // shortcuts to groups
        routeTv.setOnClickListener(v -> {
            if (from == FROM_ROUTE) finish();
            else RouteDetailActivity.startActivity(ExerciseDetailActivity.this, exercise.getRouteId(),
                exercise.getId());
        });
        intervalTv.setOnClickListener(v -> {
            if (from == FROM_INTERVAL) finish();
            else IntervalDetailActivity.startActivity(ExerciseDetailActivity.this, exercise.getInterval(),
                exercise.getId());
        });
        distanceTv.setOnClickListener(v -> {
            if (from == FROM_DISTANCE) finish();
            else {
                int longestDistance = DbReader.get(ExerciseDetailActivity.this).getLongestDistanceWithinLimits(
                    exercise.getEffectiveDistance(this));
                DistanceDetailActivity.startActivity(ExerciseDetailActivity.this, longestDistance, exercise.getId());
            }
        });

        // toggle units
        paceTv.setOnClickListener(v -> {
            String text = paceTv.getText().toString();
            String perKm = exercise.printPace(true, this);
            String mPerS = exercise.printVelocity(AppConsts.UnitVelocity.METERS_PER_SECOND, true, this);
            String kmPerH = exercise.printVelocity(AppConsts.UnitVelocity.KILOMETERS_PER_HOUR, true, this);

            if (text.equals(perKm)) {
                paceTv.setText(mPerS);
            }
            else if (text.equals(mPerS)) {
                paceTv.setText(kmPerH);
            }
            else {
                paceTv.setText(perKm);
            }
        });
        energyTv.setOnClickListener(v -> {
            String text = energyTv.getText().toString();

            String joules = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.JOULES, this),
                2, false, "J");
            String calories = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.CALORIES, this),
                2, false, "cal");
            String watthours = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.WATTHOURS, this),
                2, false, "Wh");
            String electronvolts = MathUtils.bigPrefix(exercise.getEnergy(AppConsts.UnitEnergy.ELECTRONVOLTS, this),
                19, "eV");

            if (text.equals(joules)) {
                energyTv.setText(calories);
            }
            else if (text.equals(calories)) {
                energyTv.setText(watthours);
            }
            else if (text.equals(watthours)) {
                energyTv.setText(electronvolts);
            }
            else {
                energyTv.setText(joules);
            }
        });

        // open strava
        ImageView stravaIv = findViewById(R.id.iv_exercisedetail_strava);
        if (exercise.hasStravaId()) {
            stravaIv.setOnClickListener(v -> StravaService.launchStravaActivity(exercise.getStravaId(), this));
        }
        else {
            stravaIv.setVisibility(View.GONE);
        }

        // open garmin
        ImageView garminIv = findViewById(R.id.iv_exercisedetail_garmin);
        if (exercise.hasGarminId()) {
            garminIv.setOnClickListener(v -> StravaService.launchGarminActivity(exercise.getGarminId(), this));
        }
        else {
            garminIv.setVisibility(View.GONE);
        }
    }

    private void setMap() {
        if (!exercise.hasTrail()) {
            mapView.setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.v_exercisedetail_divider1).setVisibility(View.INVISIBLE);
        mapView.setClipToOutline(true);
        mapView.getMapAsync(this);

        LayoutUtils.crossfadeInLong(mapView, 1);
    }

    private void setTvHideIfEmpty(String value, TextView tv, View alsoHide) {
        if (value.equals(AppConsts.NO_VALUE) || value.equals(AppConsts.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
            alsoHide.setVisibility(View.GONE);
        }
        else {
            tv.setVisibility(View.VISIBLE);
            alsoHide.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
    }

    private void setTvHideIfEmpty(String value, TextView tv) {
        if (value.equals(AppConsts.NO_VALUE) || value.equals(AppConsts.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
        }
        else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
    }

    // implements BinaryDialog, OnMapReadyCallback

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(ScreenUtils.isThemeLight(this) ? Style.LIGHT : Style.DARK, this);
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        LatLngBounds bounds = exercise.getTrail().getBounds();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);
        mapboxMap.moveCamera(cu);

        mapboxMap.addOnMapClickListener(latLng -> {
            ExerciseMapActivity.startActivity(exerciseId, ExerciseDetailActivity.this);
            return true;
        });

        LineManager lineManager = new LineManager(mapView, mapboxMap, style);

        LineOptions lineOptions = new LineOptions()
            .withLatLngs(exercise.getTrail().getLatLngs())
            .withLineColor(ColorUtils.colorToRgbaString(ResourcesKt.getColorByAttr(this, R.attr.colorSecondary)))
            .withLineWidth((float) ScreenUtils.px(1));

        lineManager.create(lineOptions);

        /*
        ExerciseMapActivity.setReadyMap(map, exercise.getTrail(), MAP_PADDING, this);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.setMaxZoomPreference(MAP_MAX_ZOOM);

        map.setOnMapClickListener(latLng -> ExerciseMapActivity.startActivity(exerciseId, ExerciseDetailActivity.this));
        */
    }

    @Override
    public void onAlertDialogPositiveClick(String tag, String passValue) {
        if (tag.equals(DIALOG_DELETE_EXERCISE)) {
            try {
                LayoutUtils.toast(DbWriter.get(this).deleteExercise(exercise, this), this);
            }
            catch (Exception e) {
                LayoutUtils.handleError(e, this);
            }

            finish();
        }
    }

    @Override
    public void onAlertDialogNeutralClick(@NonNull String tag, @Nullable String passValue) {
    }

    @Override
    public void onMultiChoiceDialogItemsSelected(@NonNull boolean[] itemStates, @NonNull String tag,
        @Nullable String passValue) {
        if (tag.equals(DIALOG_PULL)) {
            Prefs.setPullOptions(itemStates);

            StravaService strava = new StravaService(this);
            LayoutUtils.toast(R.string.toast_strava_pull_activity, this);

            strava.pullActivity(exercise.getStravaId(), Prefs.getPullOptions(), success -> {
                if (success) {
                    LayoutUtils.toast(R.string.toast_strava_pull_activity_successful, this);
                    recreate();
                }
                else {
                    LayoutUtils.toast(R.string.toast_strava_pull_activity_err, this);
                }
            });
        }
    }

}
