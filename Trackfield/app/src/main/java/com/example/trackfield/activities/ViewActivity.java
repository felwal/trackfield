package com.example.trackfield.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.fragments.dialogs.Dialogs;
import com.example.trackfield.toolbox.Toolbox.C;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.L;
import com.example.trackfield.toolbox.Toolbox.M;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ViewActivity extends AppCompatActivity implements Dialogs.BinaryDialog.DialogListener, OnMapReadyCallback {

    private Exercise exercise;
    private int _id;
    private int from = 0;

    private GoogleMap gMap;
    private SupportMapFragment mapFragment;

    // extras
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_ID = "_id";

    public static final int FROM_NONE = 0;
    public static final int FROM_DISTANCE = 1;
    public static final int FROM_ROUTE = 2;
    public static final int FROM_INTERVAL = 3;

    private static final int MAP_MAX_ZOOM = 17;
    protected static final int MAP_PADDING = 50;

    ////

    public static void startActivity(Context c, int _id) {
        Intent intent = new Intent(c, ViewActivity.class);
        intent.putExtra(EXTRA_ID, _id);
        c.startActivity(intent);
    }
    public static void startActivity(Context c, int _id, int from) {
        Intent intent = new Intent(c, ViewActivity.class);
        intent.putExtra(EXTRA_ID, _id);
        intent.putExtra(EXTRA_FROM, from);
        c.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        setToolbar();

        // extras
        Intent intent = getIntent();
        _id = intent.getIntExtra(EXTRA_ID, -1);
        from = intent.getIntExtra(EXTRA_FROM, FROM_NONE);

        // db
        //Helper.Reader reader = new Helper.Reader(this);
        exercise = Helper.getReader(this).getExercise(_id);
        //reader.close();
        if (exercise == null) { finish(); return; }

        setMap();
        setTexts();
    }

    private void setTexts() {

        // subs
        if (exercise.subCount() > 0) {
            final LinearLayout ll = findViewById(R.id.linearLayout_view);

            for (int i = 0; i < exercise.subCount(); i++) {

                Sub sub = exercise.getSub(i);
                final View subView = getLayoutInflater().inflate(R.layout.layout_sub_view, null);
                ll.addView(subView, ll.getChildCount()-1);

                final TextView sDistanceTv  = subView.findViewById(R.id.textView_distance);
                final TextView sTimeTv      = subView.findViewById(R.id.textView_time);
                final TextView sPaceTv      = subView.findViewById(R.id.textView_velocity);

                setTvHideIfEmpty(sub.printDistance(), sDistanceTv, subView.findViewById(R.id.textView_s));
                setTvHideIfEmpty(sub.printTime(true), sTimeTv, subView.findViewById(R.id.textView_t));
                setTvHideIfEmpty(sub.printPace(true), sPaceTv, subView.findViewById(R.id.textView_v));

                if (i % 2 == 0) { subView.setBackgroundColor(getResources().getColor(L.getBackgroundResourceFromAttr(R.attr.panelBackground, this))); }
            }
            //findViewById(R.id.divider11).setVisibility(View.VISIBLE);
            findViewById(R.id.divider9).setVisibility(View.INVISIBLE);
        }

        // get
        final TextView idTv                 = findViewById(R.id.textView_id);
        final TextView routeTv              = findViewById(R.id.textView_primary);
        final TextView routeVarTv           = findViewById(R.id.textView_routeVar);
        final TextView intervalTv           = findViewById(R.id.textView_interval);
        final TextView dateTv               = findViewById(R.id.textView_caption);
        final TextView distanceTv           = findViewById(R.id.textView_distance);
        final TextView timeTv               = findViewById(R.id.textView_time);
        final TextView paceTv               = findViewById(R.id.textView_velocity);
        final TextView energyTv             = findViewById(R.id.textView_energy);
        final TextView powerTv              = findViewById(R.id.textView_power);
        final TextView elevationTv          = findViewById(R.id.textView_elevation);
        final TextView noteTv               = findViewById(R.id.textView_note);
        final TextView dataSourceTv         = findViewById(R.id.textView_dataSource);
        final TextView recordingMethodTv    = findViewById(R.id.textView_recordingMethod);
        final TextView typeTv               = findViewById(R.id.textView_type);

        // set

        routeTv.setText(exercise.getRoute());
        routeVarTv.setText(exercise.getRouteVar());
        dateTv.setText(exercise.getDateTime().format(C.FORMATTER_VIEW));
        setTvHideIfEmpty(exercise.getNote(), noteTv);

        idTv.setText(exercise.printId());
        typeTv.setText(exercise.printType());
        recordingMethodTv.setText(exercise.getRecordingMethod());
        setTvHideIfEmpty(exercise.getDataSource(), dataSourceTv);

        TextView sTv = findViewById(R.id.textView_s);
        if (exercise.isDistanceDriven()) { sTv.setText("s."); }

        setTvHideIfEmpty(exercise.getInterval(), intervalTv, findViewById(R.id.textView_sigma));
        setTvHideIfEmpty(exercise.printDistance(false), distanceTv, sTv);
        setTvHideIfEmpty(exercise.printTime(true), timeTv, findViewById(R.id.textView_t));
        setTvHideIfEmpty(exercise.printPace(true), paceTv, findViewById(R.id.textView_v));
        setTvHideIfEmpty(exercise.printEnergy(), energyTv, findViewById(R.id.textView_E));
        setTvHideIfEmpty(exercise.printPower(), powerTv, findViewById(R.id.textView_P));
        setTvHideIfEmpty(exercise.printElevation(), elevationTv, findViewById(R.id.textView_h));

        routeTvListener(routeTv);
        intervalTvListener(intervalTv);
        distanceTvListener(distanceTv);
        paceTvListener(paceTv);
        energyTvListener(energyTv);
    }
    private void setMap() {

        FrameLayout frame = findViewById(R.id.frameLayout_mapFragment);
        if (!exercise.hasTrail()) { frame.setVisibility(View.GONE); return; }
        findViewById(R.id.divider9).setVisibility(View.INVISIBLE);
        frame.setClipToOutline(true);

        // fragment
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_mapFragment, mapFragment).commit();
            mapFragment.getMapAsync(this);
        }

    }

    // listeners
    private void routeTvListener(final TextView tv) {

        if (from != FROM_ROUTE) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    RecActivity.RouteActivity.startActivity(ViewActivity.this, exercise.getRouteId(), exercise.get_id());
                }
            });
        }
    }
    private void intervalTvListener(final TextView tv) {

        if (from != FROM_INTERVAL) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    RecActivity.IntervalActivity.startActivity(ViewActivity.this, exercise.getInterval(), exercise.get_id());
                }
            });
        }
    }
    private void distanceTvListener(final TextView tv) {

        if (from != FROM_DISTANCE) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ArrayList<Distance> distances = Helper.getReader(ViewActivity.this).getDistances(Distance.SortMode.DISTANCE, false);
                    for (Distance d : distances) {
                        if (M.insideLimits(exercise.distance(), d.getDistance())) {
                            RecActivity.DistanceActivity.startActivity(ViewActivity.this, d.getDistance(), exercise.get_id());
                            break;
                        }
                    }
                }
            });
        }

    }
    private void paceTvListener(final TextView tv) {

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = tv.getText().toString();
                String perKm = exercise.printPace(true);
                String mPerS = exercise.printVelocity(C.UnitVelocity.METERS_PER_SECOND, true); //M.round(exercise.getVelocity(C.UNIT_METERS_PER_SECOND), 1) + " m/s";
                String kmPerH = exercise.printVelocity(C.UnitVelocity.KILOMETERS_PER_HOUR, true); //M.round(exercise.getVelocity(C.UNIT_KILOMETERS_PER_HOUR), 1) + " km/h";

                if (text.equals(perKm)) {
                    tv.setText(mPerS);
                }
                else if (text.equals(mPerS)) {
                    tv.setText(kmPerH);
                }
                else {
                    tv.setText(perKm);
                }
            }
        });
    }
    private void energyTvListener(final TextView tv) {

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = tv.getText().toString();
                String joules = M.prefix(exercise.energy(C.UnitEnergy.JOULES), 2, "J");
                String calories = M.prefix(exercise.energy(C.UnitEnergy.CALORIES), 2, "cal");
                String watthours = M.prefix(exercise.energy(C.UnitEnergy.WATTHOURS), 2, "Wh");
                String electronvolts = M.bigPrefix(exercise.energy(C.UnitEnergy.ELECTRONVOLTS), 19, "eV");

                if (text.equals(joules)) {
                    tv.setText(calories);
                }
                else if (text.equals(calories)) {
                    tv.setText(watthours);
                }
                else if (text.equals(watthours)) {
                    tv.setText(electronvolts);
                }
                else {
                    tv.setText(joules);
                }
            }
        });
    }

    // toolbar
    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_view);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.activity_view));
        ab.setDisplayHomeAsUpEnabled(true);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_view, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: finish(); return true;

            case R.id.action_edit:
                EditActivity.startActivity(this, _id);
                return true;

            case R.id.action_delete:
                Dialogs.DeleteExercise.newInstance(getSupportFragmentManager());
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBinaryDialogPositiveClick(String tag) {

        //Helper.Writer writer = new Helper.Writer(this);
        try { L.toast(Helper.getWriter(this).deleteExercise(exercise, this), this); }
        catch (Exception e) { L.handleError(e, this); }
        //writer.close();

        try { D.exercises.remove(exercise.getId()); }
        catch (Exception e) { L.handleError(e, this); }
        D.edited();

        finish();
    }
    @Override public void onMapReady(GoogleMap googleMap) {

        if (gMap == null) {
            gMap = googleMap;
            MapActivity.ExerciseMap.setReadyMap(gMap, exercise.getTrail(), MAP_PADDING, this);
            gMap.getUiSettings().setAllGesturesEnabled(false);
            gMap.setMaxZoomPreference(MAP_MAX_ZOOM);

            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override public void onMapClick(LatLng latLng) {
                    MapActivity.ExerciseMap.startActivity(_id, ViewActivity.this);
                }
            });
        }
    }

    @Override protected void onRestart() {
        super.onRestart();
        recreate();
    }
    @Override protected void onDestroy() {
        //Helper.closeReader();
        //Helper.closeWriter();
        super.onDestroy();
    }

    // tools
    private void setTvHideIfEmpty(String value, TextView tv, View alsoHide) {
        if (value.equals(C.NO_VALUE) || value.equals(C.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
            alsoHide.setVisibility(View.GONE);
        }
        else tv.setText(value);
    }
    private void setTvHideIfEmpty(String value, TextView tv) {
        if (value.equals(C.NO_VALUE) || value.equals(C.NO_VALUE_TIME) || value.equals("")) {
            tv.setVisibility(View.GONE);
        }
        else tv.setText(value);
    }

}
