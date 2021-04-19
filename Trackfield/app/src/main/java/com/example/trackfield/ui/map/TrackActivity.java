package com.example.trackfield.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.trackfield.ui.map.model.Coordinate;
import com.example.trackfield.utils.ScreenUtils;
import com.example.trackfield.ui.custom.dialog.BaseDialog;
import com.example.trackfield.ui.custom.dialog.BinaryDialog;
import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.FileUtils;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.trackfield.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.TreeMap;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, BinaryDialog.DialogListener {

    private LocationManager manager;
    private LocationListener listener;

    // map
    private GoogleMap gMap;
    private Marker marker;
    private PolylineOptions polyline;

    // layout
    private FloatingActionButton playPauseFab;
    private TextView timeTv, distanceTv, paceTv, avgPaceTv, coordsTv, coordsDiffTv;
    private FrameLayout mapFrame;

    // fields
    private int distance = 0;
    private TreeMap<Float, Coordinate> coordinates = new TreeMap<>();
    private double[] lastFourAlts = new double[] { -1, -1, -1, -1 };
    private boolean recording = false;
    private boolean loaded = false;
    private boolean mapExpanded = false;

    // consts
    private static final int MAP_ZOOM = 16; // 15
    private static final int DISTANCE_DECIMALS = 2;
    private static final String DIALOG_FINISH_TRACKING = "finishTrackingDialog";

    ////

    public static void startActivity(Context c) {
        Intent intent = new Intent(c, TrackActivity.class);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @SuppressLint("MissingPermission") @Override
    protected void onCreate(Bundle savedInstanceState) {

        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ScreenUtils.makeStatusBarTransparent(getWindow(), false, null);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!FileUtils.permissionToLocation(this)) return;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);

        timeTv = findViewById(R.id.textView_time);
        distanceTv = findViewById(R.id.textView_distance);
        paceTv = findViewById(R.id.textView_pace);
        avgPaceTv = findViewById(R.id.textView_avgPace);
        coordsTv = findViewById(R.id.textView_coords);
        coordsDiffTv = findViewById(R.id.textView_coordsDiff);

        setFabs();
        setMap();
    }

    @Override
    public void onBackPressed() {
        if (coordinates.size() == 0) {
            super.onBackPressed();
            return;
        }
        finishDialog();
    }

    // implements OnMapReadyCallback

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (!Prefs.isThemeLight()) LayoutUtils.toast(gMap.setMapStyle(Prefs.getMapStyle(this)), this);
        gMap.getUiSettings().setAllGesturesEnabled(false);

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override public void onMapClick(LatLng latLng) {
                if (mapExpanded) {
                    LayoutUtils.crossfadeIn(paceTv, 1);
                    LayoutUtils.crossfadeIn(avgPaceTv, 1);
                    LayoutUtils.animateHeight(mapFrame, MathUtils.goldenRatioSmall(ScreenUtils.getScreenHeight(TrackActivity.this)));
                    gMap.getUiSettings().setAllGesturesEnabled(false);
                    mapExpanded = false;
                }
                else {
                    LayoutUtils.crossfadeOut(paceTv);
                    LayoutUtils.crossfadeOut(avgPaceTv);
                    LayoutUtils.animateHeight(mapFrame, MathUtils.goldenRatioLarge(ScreenUtils.getScreenHeight(TrackActivity.this)));
                    gMap.getUiSettings().setAllGesturesEnabled(true);
                    mapExpanded = true;
                }
            }
        });
    }

    // set

    private void setFabs() {

        playPauseFab = findViewById(R.id.fab_pause);
        final FloatingActionButton finishFab = findViewById(R.id.fab_finish);

        finishFab.hide();
        playPauseFab.setVisibility(View.INVISIBLE);

        // play / pause
        playPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (recording) {
                    recording = false;
                    finishFab.show();

                    LayoutUtils.animateFab(playPauseFab,
                            LayoutUtils.getColorInt(R.attr.colorSurface, playPauseFab.getContext()),
                            getResources().getColor(R.color.colorPrimaryAccent),
                            getDrawable(R.drawable.ic_fab_play_24dp));
                    //playPauseFab.setImageDrawable(getDrawable(R.drawable.ic_fab_play_24dp));
                }
                else {
                    recording = true;
                    finishFab.hide();

                    LayoutUtils.animateFab(playPauseFab,
                            getResources().getColor(R.color.colorPrimaryAccent),
                            LayoutUtils.getColorInt(R.attr.colorSurface, playPauseFab.getContext()),
                            getDrawable(R.drawable.ic_fab_pause_24dp));
                    //playPauseFab.setImageDrawable(getDrawable(R.drawable.ic_fab_pause_24dp));
                }
            }
        });

        // finish
        finishFab.hide();
        finishFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finishDialog();
            }
        });

    }

    private void setMap() {

        mapFrame = findViewById(R.id.frameLayout_mapFragment);
        mapFrame.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MathUtils.goldenRatioSmall(ScreenUtils.getScreenHeight(this))));
        mapFrame.setClipToOutline(true);

        polyline = new PolylineOptions();
        polyline.color(getResources().getColor(R.color.colorGreenLight));

        // fragment
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_mapFragment, mapFragment).commit();
        mapFragment.getMapAsync(this);

    }

    // tools

    private void updateMap(Location location) {

        final LatLng latLng = MathUtils.toLatLng(location);
        if (marker == null) marker = gMap.addMarker(new MarkerOptions().position(latLng));
        else marker.setPosition(latLng);

        // focus
        if (!mapExpanded) {
            try { gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM)); }
            catch (Exception e) {
                gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override public void onMapLoaded() {
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
                    }
                });
            }
        }

        if (!recording) return;
        polyline.add(latLng);
        gMap.addPolyline(polyline);

    }

    private void finishDialog() {
        BinaryDialog.newInstance(R.string.dialog_title_finish_recording, BaseDialog.NO_RES, R.string.dialog_btn_finish, DIALOG_FINISH_TRACKING)
                .show(getSupportFragmentManager());
    }

    private void saveExercise() {

        /*Map map = new Map(-1, coordinates);
        Exercise exercise = new Exercise(-1, 0, LocalDateTime.now(), "Trackfield", "GPS", map);

        Helper.Writer writer = new Helper.Writer(TrackActivity.this);
        writer.addExercise(exercise, this);
        writer.close();*/
    }

    // implements dialogs

    @Override
    public void onBinaryDialogPositiveClick(String passValue, String tag) {
        if (tag.equals(DIALOG_FINISH_TRACKING)) {
            if (coordinates.lastKey() < 30) { finish(); return; }
            saveExercise();
            finish();
        }
    }

    // implements LocationListener

    @SuppressLint("SetTextI18n") @Override
    public void onLocationChanged(@NonNull Location location) {

        if (gMap != null) updateMap(location);
        if (!loaded) { playPauseFab.setVisibility(View.VISIBLE); loaded = true; }
        if (!recording) return;

        float time = coordinates.size() == 0 ? 0 : coordinates.lastKey() + 1;
        distance += coordinates.size() == 0 ? 0 : location.distanceTo(coordinates.lastEntry().getValue());

        // textViews
        timeTv.setText(MathUtils.stringTime(time, true) + " s");
        distanceTv.setText(MathUtils.prefix(distance, DISTANCE_DECIMALS, "m"));
        avgPaceTv.setText((distance == 0 ? Constants.NO_VALUE_TIME : MathUtils.stringTime(time / ((float) distance / 1000), true)) + " s/km");

        // altitude correction
        lastFourAlts[3] = lastFourAlts[2];
        lastFourAlts[2] = lastFourAlts[1];
        lastFourAlts[1] = lastFourAlts[0];
        lastFourAlts[0] = location.getAltitude();
        location.setAltitude(MathUtils.arrayAvg(lastFourAlts, -1));

        coordsTv.setText(
                MathUtils.round(location.getLatitude(), 6) + " °N, " +
                MathUtils.round(location.getLongitude(), 6) + " °E, " +
                MathUtils.round(location.getAltitude(), 2) + " m");
        if (coordinates.size() > 0) {
            coordsDiffTv.setText(
                    MathUtils.round(location.getLatitude() - coordinates.lastEntry().getValue().getLatitude(), 6) + " °N, " +
                    MathUtils.round(location.getLongitude() - coordinates.lastEntry().getValue().getLongitude(), 6) + " °E, " +
                    MathUtils.round(location.getAltitude() - coordinates.lastEntry().getValue().getAltitude(), 2) + " m");
        }

        coordinates.put(time, new Coordinate(-1, location));

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LayoutUtils.crossfade(mapFrame, 1);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LayoutUtils.crossfade(mapFrame, 0.5f);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

}
