package com.example.trackfield.ui.exercise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.DbWriter;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.data.db.model.Sub;
import com.example.trackfield.data.network.StravaApi;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.ui.map.ExerciseMapActivity;
import com.example.trackfield.ui.record.distance.DistanceActivity;
import com.example.trackfield.ui.record.interval.IntervalActivity;
import com.example.trackfield.ui.record.route.RouteActivity;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.ScreenUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class ViewActivity extends AppCompatActivity implements BinaryDialog.DialogListener, OnMapReadyCallback {

    public static final int FROM_NONE = 0;
    public static final int FROM_DISTANCE = 1;
    public static final int FROM_ROUTE = 2;
    public static final int FROM_INTERVAL = 3;

    // extras names
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_FROM = "from";

    // dialog tags
    private static final String DIALOG_DELETE_EXERCISE = "deleteExerciseDialog";

    private static final int MAP_MAX_ZOOM = 17;
    private static final int MAP_PADDING = 50;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    // arguments
    private int exerciseId;
    private Exercise exercise;
    private int fromRecycler = FROM_NONE;

    //

    public static void startActivity(Context c, int exerciseId) {
        Intent intent = new Intent(c, ViewActivity.class);
        intent.putExtra(EXTRA_ID, exerciseId);
        c.startActivity(intent);
    }

    public static void startActivity(Context c, int exerciseId, int from) {
        Intent intent = new Intent(c, ViewActivity.class);
        intent.putExtra(EXTRA_ID, exerciseId);
        intent.putExtra(EXTRA_FROM, from);
        c.startActivity(intent);
    }

    // extends AppCompatActivitys

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        setToolbar();

        // extras
        Intent intent = getIntent();
        exerciseId = intent.getIntExtra(EXTRA_ID, -1);
        fromRecycler = intent.getIntExtra(EXTRA_FROM, FROM_NONE);

        // get exercise
        exercise = DbReader.get(this).getExercise(exerciseId);
        if (exercise == null) {
            finish();
            return;
        }

        setMap();
        setTexts();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // reload edits
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_view, menu);

        // remove pull action if no externalId
        if (!exercise.hasStravaId()) {
            menu.findItem(R.id.action_pull_exercise).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.action_edit_exercise) {
            EditActivity.startActivity(this, exerciseId);
            return true;
        }
        else if (itemId == R.id.action_delete_exercise) {
            BinaryDialog.newInstance(R.string.dialog_title_delete_exercise, R.string.dialog_msg_delete_exercise,
                R.string.dialog_btn_delete, DIALOG_DELETE_EXERCISE)
                .show(getSupportFragmentManager());
            return true;
        }
        else if (itemId == R.id.action_pull_exercise) {
            if (exercise.hasStravaId()) {
                StravaApi strava = new StravaApi(this);
                strava.pullActivity(exercise.getStravaId(), success -> {
                    if (success) {
                        LayoutUtils.toast(R.string.toast_strava_pull_activity_successful, this);
                        recreate();
                    }
                    else {
                        LayoutUtils.toast(R.string.toast_strava_pull_activity_err, this);
                    }
                });
            }
            else {
                LayoutUtils.toast(R.string.toast_strava_pull_activity_gone, this);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // set

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_view);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.activity_title_view));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setTexts() {
        // subs
        if (exercise.subCount() > 0) {
            final LinearLayout ll = findViewById(R.id.ll_view);

            for (int i = 0; i < exercise.subCount(); i++) {

                Sub sub = exercise.getSub(i);
                final View subView = getLayoutInflater().inflate(R.layout.item_view_sub, null);
                ll.addView(subView, ll.getChildCount() - 1);

                final TextView sDistanceTv = subView.findViewById(R.id.tv_view_item_sub_distance);
                final TextView sTimeTv = subView.findViewById(R.id.tv_view_item_sub_time);
                final TextView sPaceTv = subView.findViewById(R.id.tv_view_item_sub_velocity);

                setTvHideIfEmpty(sub.printDistance(), sDistanceTv, subView.findViewById(R.id.tv_view_item_sub_s));
                setTvHideIfEmpty(sub.printTime(true), sTimeTv, subView.findViewById(R.id.tv_view_item_sub_t));
                setTvHideIfEmpty(sub.printPace(true), sPaceTv, subView.findViewById(R.id.tv_view_item_sub_v));

                if (i % 2 == 0) {
                    subView.setBackgroundColor(
                        getResources().getColor(LayoutUtils.getAttr(R.attr.panelBackground, this)));
                }
            }
            //findViewById(R.id.divider11).setVisibility(View.VISIBLE);
            findViewById(R.id.v_view_divider1).setVisibility(View.INVISIBLE);
        }

        // get
        TextView routeTv = findViewById(R.id.tv_view_route);
        TextView routeVarTv = findViewById(R.id.tv_view_routevar);
        TextView intervalTv = findViewById(R.id.tv_view_interval);
        TextView dateTv = findViewById(R.id.tv_view_date);
        TextView distanceTv = findViewById(R.id.tv_view_distance);
        TextView timeTv = findViewById(R.id.tv_view_time);
        TextView paceTv = findViewById(R.id.tv_view_velocity);
        TextView energyTv = findViewById(R.id.tv_view_energy);
        TextView powerTv = findViewById(R.id.tv_view_power);
        TextView elevationTv = findViewById(R.id.tv_view_elevation);
        TextView noteTv = findViewById(R.id.tv_view_note);
        TextView idTv = findViewById(R.id.tv_view_id);
        TextView typeTv = findViewById(R.id.tv_view_type);
        TextView dataSourceTv = findViewById(R.id.tv_view_data_source);
        TextView recordingMethodTv = findViewById(R.id.tv_view_recording_method);

        // set

        routeTv.setText(exercise.getRoute());
        routeVarTv.setText(exercise.getRouteVar());
        dateTv.setText(exercise.getDateTime().format(AppConsts.FORMATTER_VIEW));
        setTvHideIfEmpty(exercise.getNote(), noteTv);

        idTv.setText(exercise.printId());
        typeTv.setText(exercise.printType());
        setTvHideIfEmpty(exercise.getDataSource(), dataSourceTv);
        setTvHideIfEmpty(exercise.getRecordingMethod(), recordingMethodTv);

        setTvHideIfEmpty(exercise.getInterval(), intervalTv, findViewById(R.id.tv_view_i));
        setTvHideIfEmpty(exercise.printDistance(false, this), distanceTv, findViewById(R.id.tv_view_s));
        setTvHideIfEmpty(exercise.printTime(true), timeTv, findViewById(R.id.tv_view_t));
        setTvHideIfEmpty(exercise.printPace(true, this), paceTv, findViewById(R.id.tv_view_v));
        setTvHideIfEmpty(exercise.printEnergy(this), energyTv, findViewById(R.id.tv_view_e));
        setTvHideIfEmpty(exercise.printPower(this), powerTv, findViewById(R.id.tv_view_p));
        setTvHideIfEmpty(exercise.printElevation(), elevationTv, findViewById(R.id.tv_view_h));

        // set listeners

        // open activity
        if (fromRecycler != FROM_ROUTE) {
            routeTv.setOnClickListener(v ->
                RouteActivity.startActivity(ViewActivity.this, exercise.getRouteId(), exercise.getId()));
        }
        if (fromRecycler != FROM_INTERVAL) {
            intervalTv.setOnClickListener(v ->
                IntervalActivity.startActivity(ViewActivity.this, exercise.getInterval(), exercise.getId()));
        }
        if (fromRecycler != FROM_DISTANCE) {
            distanceTv.setOnClickListener(v -> {
                int longestDistance = DbReader.get(ViewActivity.this).longestDistanceWithinLimits(
                    exercise.getEffectiveDistance(this));
                DistanceActivity.startActivity(ViewActivity.this, longestDistance, exercise.getId());
            });
        }

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
            String joules = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.JOULES, this), 2, "J");
            String calories = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.CALORIES, this), 2, "cal");
            String watthours = MathUtils.prefix(exercise.getEnergy(AppConsts.UnitEnergy.WATTHOURS, this), 2, "Wh");
            String electronvolts = MathUtils.bigPrefix(exercise.getEnergy(AppConsts.UnitEnergy.ELECTRONVOLTS, this), 19,
                "eV");

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
        ImageView stravaIv = findViewById(R.id.iv_view_strava_link);
        if (exercise.hasStravaId()) {
            stravaIv.setOnClickListener(v -> StravaApi.launchStravaActivity(exercise.getStravaId(), this));
        }
        else {
            stravaIv.setVisibility(View.GONE);
        }

        // open garmin
        ImageView garminIv = findViewById(R.id.iv_view_garmin_link);
        if (exercise.hasGarminId()) {
            garminIv.setOnClickListener(v -> StravaApi.launchGarminActivity(exercise.getGarminId(), this));
        }
        else {
            garminIv.setVisibility(View.GONE);
        }
    }

    private void setMap() {
        FrameLayout frame = findViewById(R.id.fl_view_map);
        if (!exercise.hasTrail()) {
            frame.setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.v_view_divider1).setVisibility(View.INVISIBLE);
        frame.setClipToOutline(true);

        // fragment
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_view_map, mapFragment).commit();
            mapFragment.getMapAsync(this);
        }
    }

    private void setTvHideIfEmpty(String value, TextView tv, View alsoHide) {
        if (value.equals(AppConsts.NO_VALUE) || value.equals(AppConsts.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
            alsoHide.setVisibility(View.GONE);
        }
        else tv.setText(value);
    }

    private void setTvHideIfEmpty(String value, TextView tv) {
        if (value.equals(AppConsts.NO_VALUE) || value.equals(AppConsts.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
        }
        else tv.setText(value);
    }

    // implements BinaryDialog, OnMapReadyCallback

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (map == null) {
            map = googleMap;
            ExerciseMapActivity.setReadyMap(map, exercise.getTrail(), MAP_PADDING, this);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.setMaxZoomPreference(MAP_MAX_ZOOM);

            map.setOnMapClickListener(latLng -> ExerciseMapActivity.startActivity(exerciseId, ViewActivity.this));
        }

        LayoutUtils.crossfadeInLong(mapFragment.getView(), 1);
    }

}
