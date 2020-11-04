package com.example.trackfield.activities;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Map;
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private Map map;

    private static String EXTRA_ID = "_id";

    private static final int MAP_PADDING = 50;
    private static final int MAP_ZOOM = 18;

    ////

    public static void startActivity(int _id, Context c) {
        Intent intent = new Intent(c, MapActivity.class);
        intent.putExtra(EXTRA_ID, _id);
        c.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        if (intent == null) finish();
        int _id = intent.getIntExtra(EXTRA_ID, -1);

        Helper.Reader reader = new Helper.Reader(this);
        map = reader.getMap(_id);
        reader.close();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        googleMap.setMaxZoomPreference(MAP_ZOOM);
        setReadyMap(googleMap, map, this);
    }

    // tools
    public static void setReadyMap(final GoogleMap googleMap, final Map map, Context c) {

        if (!D.theme) L.toast(googleMap.setMapStyle(D.getMapStyle(c)), c);

        // polyline
        PolylineOptions polyline = new PolylineOptions();
        polyline.color(c.getResources().getColor(R.color.colorGreenLight));
        polyline.addAll(map.getLatLngs());
        googleMap.addPolyline(polyline);

        // focus
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(map.getBounds(), MAP_PADDING);
        try { googleMap.moveCamera(cu); }
        catch (Exception e) {
            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override public void onMapLoaded() {
                    googleMap.moveCamera(cu);
                }
            });
        }

    }

}
