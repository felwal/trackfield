package com.felwal.trackfield.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.custom.dialog.BaseDialog;
import com.felwal.trackfield.ui.custom.dialog.BinaryDialog;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.FileUtils;
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.felwal.trackfield.utils.TypeUtils;
import com.felwal.trackfield.utils.annotations.Unfinished;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.TreeMap;

@Unfinished
public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
    BinaryDialog.DialogListener {

    private static final int MAP_ZOOM = 16;
    private static final int DISTANCE_DECIMALS = 2;

    // dialog tags
    private static final String DIALOG_FINISH_TRACKING = "finishTrackingDialog";

    // map
    private GoogleMap map;
    private Marker marker;
    private PolylineOptions polyline;

    private FrameLayout mapFrame;
    private FloatingActionButton playPauseFab;

    // textviews
    private TextView timeTv;
    private TextView distanceTv;
    private TextView paceTv;
    private TextView avgPaceTv;
    private TextView coordsTv;
    private TextView coordsDiffTv;

    // recorded data
    private int distance = 0;
    private final TreeMap<Float, Location> locations = new TreeMap<>();
    private final double[] lastFourAlts = new double[] { 0, 0, 0, 0 };

    // states
    private boolean recording = false;
    private boolean loaded = false;
    private boolean mapExpanded = false;

    //

    public static void startActivity(Context c) {
        Intent intent = new Intent(c, TrackActivity.class);
        c.startActivity(intent);
    }

    // extends AppCompatActivity

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ScreenUtils.makeStatusBarTransparent(getWindow(), false, null);

        if (FileUtils.hasNotPermissionToLocation(this)) {
            LayoutUtils.toast(R.string.toast_err_location, this);
            finish();
            return;
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);

        timeTv = findViewById(R.id.tv_track_time);
        distanceTv = findViewById(R.id.tv_track_distance);
        paceTv = findViewById(R.id.tv_track_pace);
        avgPaceTv = findViewById(R.id.tv_track_pace_avg);
        coordsTv = findViewById(R.id.tv_track_coords);
        coordsDiffTv = findViewById(R.id.tv_track_coords_diff);

        setFabs();
        setMap();
    }

    @Override
    public void onBackPressed() {
        if (locations.size() == 0) super.onBackPressed();
        else finishDialog();
    }

    // implements OnMapReadyCallback

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (!Prefs.isThemeLight()) LayoutUtils.toast(map.setMapStyle(Prefs.getMapStyle(this)), this);
        map.getUiSettings().setAllGesturesEnabled(false);

        map.setOnMapClickListener(latLng -> {
            if (mapExpanded) {
                LayoutUtils.crossfadeIn(paceTv, 1);
                LayoutUtils.crossfadeIn(avgPaceTv, 1);
                LayoutUtils.animateHeight(mapFrame,
                    MathUtils.goldenRatioSmall(ScreenUtils.getScreenHeight(TrackActivity.this)));
                map.getUiSettings().setAllGesturesEnabled(false);
                mapExpanded = false;
            }
            else {
                LayoutUtils.crossfadeOut(paceTv);
                LayoutUtils.crossfadeOut(avgPaceTv);
                LayoutUtils.animateHeight(mapFrame,
                    MathUtils.goldenRatioLarge(ScreenUtils.getScreenHeight(TrackActivity.this)));
                map.getUiSettings().setAllGesturesEnabled(true);
                mapExpanded = true;
            }
        });
    }

    // set

    private void setFabs() {
        // finish
        FloatingActionButton finishFab = findViewById(R.id.fab_track_finish);
        finishFab.hide();
        finishFab.setOnClickListener(v -> finishDialog());

        // play / pause
        playPauseFab = findViewById(R.id.fab_track_pause);
        playPauseFab.setVisibility(View.INVISIBLE);
        playPauseFab.setOnClickListener(v -> {
            if (recording) {
                recording = false;
                finishFab.show();

                Drawable toIcon = ContextCompat.getDrawable(this, R.drawable.ic_play).mutate();
                toIcon.setColorFilter(LayoutUtils.getColorInt(android.R.attr.textColorPrimaryInverse, this),
                    PorterDuff.Mode.SRC_IN);

                LayoutUtils.animateFab(playPauseFab,
                    LayoutUtils.getColorInt(R.attr.colorSurface, this),
                    LayoutUtils.getColorInt(R.attr.colorPrimaryVariant, this),
                    toIcon);
            }
            else {
                recording = true;
                finishFab.hide();

                Drawable toIcon = ContextCompat.getDrawable(this, R.drawable.ic_pause).mutate();
                toIcon.setColorFilter(LayoutUtils.getColorInt(android.R.attr.textColorHighlight, this),
                    PorterDuff.Mode.SRC_IN);

                LayoutUtils.animateFab(playPauseFab,
                    LayoutUtils.getColorInt(R.attr.colorPrimaryVariant, this),
                    LayoutUtils.getColorInt(R.attr.colorSurface, this),
                    toIcon);
            }
        });
    }

    private void setMap() {
        mapFrame = findViewById(R.id.fl_track_map);
        mapFrame.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            MathUtils.goldenRatioSmall(ScreenUtils.getScreenHeight(this))));
        mapFrame.setClipToOutline(true);

        polyline = new PolylineOptions();
        polyline.color(getResources().getColor(R.color.colorGreenLight));

        // fragment
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_track_map, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    // tools

    private void updateMap(Location location) {
        final LatLng latLng = TypeUtils.toLatLng(location);
        if (marker == null) marker = map.addMarker(new MarkerOptions().position(latLng));
        else marker.setPosition(latLng);

        // focus
        if (!mapExpanded) {
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
            }
            catch (Exception e) {
                map.setOnMapLoadedCallback(() ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM)));
            }
        }

        if (!recording) return;
        polyline.add(latLng);
        map.addPolyline(polyline);
    }

    private void finishDialog() {
        BinaryDialog.newInstance(R.string.dialog_title_finish_recording, BaseDialog.NO_RES, R.string.dialog_btn_finish,
            DIALOG_FINISH_TRACKING)
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
            if (locations.lastKey() < 30) {
                finish();
                return;
            }
            saveExercise();
            finish();
        }
    }

    // implements LocationListener

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (map != null) updateMap(location);
        if (!loaded) {
            playPauseFab.setVisibility(View.VISIBLE);
            loaded = true;
        }
        if (!recording) return;

        float time = locations.size() == 0 ? 0 : locations.lastKey() + 1;
        distance += locations.size() == 0 ? 0 : location.distanceTo(locations.lastEntry().getValue());

        // textViews
        timeTv.setText(MathUtils.stringTime(time, true) + " s");
        distanceTv.setText(MathUtils.prefix(distance, DISTANCE_DECIMALS, "m"));
        avgPaceTv.setText((distance == 0
            ? AppConsts.NO_VALUE_TIME
            : MathUtils.stringTime(time / ((float) distance / 1000), true))
            + " s/km");

        // altitude correction
        lastFourAlts[3] = lastFourAlts[2];
        lastFourAlts[2] = lastFourAlts[1];
        lastFourAlts[1] = lastFourAlts[0];
        lastFourAlts[0] = location.getAltitude();
        location.setAltitude(MathUtils.arrayAvg(lastFourAlts, -1));

        coordsTv.setText(MathUtils.round(location.getLatitude(), 6) + " °N, " +
                MathUtils.round(location.getLongitude(), 6) + " °E, " +
                MathUtils.round(location.getAltitude(), 2) + " m");
        if (locations.size() > 0) {
            coordsDiffTv.setText(
                MathUtils.round(location.getLatitude() - locations.lastEntry().getValue().getLatitude(), 6)
                    + " °N, "
                    + MathUtils.round(location.getLongitude() - locations.lastEntry().getValue().getLongitude(), 6)
                    + " °E, "
                    + MathUtils.round(location.getAltitude() - locations.lastEntry().getValue().getAltitude(), 2)
                    + " m");
        }

        locations.put(time, location);
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
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // no need to do anything on status change
    }

}
