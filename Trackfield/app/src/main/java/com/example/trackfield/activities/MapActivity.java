package com.example.trackfield.activities;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.objects.Trails;
import com.example.trackfield.toolbox.Prefs;
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MapActivity {

    public static abstract class Base extends FragmentActivity implements OnMapReadyCallback {

        protected int _id;

        protected GoogleMap googleMap;
        protected ArrayList<Polyline> tempPolylines = new ArrayList<>();
        protected ArrayList<PolylineOptions> tempOptions = new ArrayList<>();
        protected boolean tempShown = false;

        protected static String EXTRA_ID = "_id";
        protected static final int MAP_PADDING = 100;
        protected static final int MAP_MAX_ZOOM = 18;

        ////

        @Override protected void onCreate(Bundle savedInstanceState) {

            D.updateTheme(this);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            L.transStatusBar(getWindow());

            Intent intent = getIntent();
            if (intent == null) finish();
            _id = intent.getIntExtra(EXTRA_ID, -1);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        @Override public void onMapReady(GoogleMap googleMap) {
            this.googleMap = googleMap;
            googleMap.setMaxZoomPreference(MAP_MAX_ZOOM);
            mapOptionsFab();
        }

        // tools
        private void mapOptionsFab() {

            FloatingActionButton fab = findViewById(R.id.fab_mapOptions);
            fab.setOnClickListener(view -> togglePolylines());

        }
        private void togglePolylines() {

            if (!tempShown) {
                if (tempOptions.size() == 0) {
                    for (String path : getRestOfPolylines()) {
                        PolylineOptions options = new PolylineOptions();
                        options.color(getResources().getColor(R.color.colorGreenLightTrans));
                        options.width(L.px(3));
                        options.addAll(PolyUtil.decode(path));
                        tempOptions.add(options);
                        tempPolylines.add(googleMap.addPolyline(options));
                    }
                }
                else for (PolylineOptions options : tempOptions) {
                    tempPolylines.add(googleMap.addPolyline(options));
                }
            }
            else {
                for (Polyline p : tempPolylines) p.remove();
                tempPolylines.clear();
            }

            tempShown = !tempShown;
        }
        protected abstract ArrayList<String> getRestOfPolylines();

    }

    public static class ExerciseMap extends Base {

        private Trail trail;
        private Trails routeTrails;

        ////

        public static void startActivity(int _id, Context c) {
            Intent intent = new Intent(c, ExerciseMap.class);
            intent.putExtra(EXTRA_ID, _id);
            c.startActivity(intent);
        }

        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            trail = Helper.getReader(this).getTrail(_id); //getExercise(_id).getTrail();
            Exercise e = Helper.getReader().getExercise(_id);
            //routeTrails = new Trails(Helper.getReader().getPolylinesByRoute(e.getRouteId(), e.getRouteVar()));
        }
        @Override public void onMapReady(GoogleMap googleMap) {
            super.onMapReady(googleMap);
            setReadyMap(googleMap, trail, routeTrails, MAP_PADDING, this);

            // markers
            MarkerOptions startMarker = new MarkerOptions();
            MarkerOptions endMarker = new MarkerOptions();
            startMarker.position(trail.getStart());
            endMarker.position(trail.getEnd());
            googleMap.addMarker(startMarker);
            googleMap.addMarker(endMarker);
        }

        public static void setReadyMap(final GoogleMap googleMap, final Trail trail, Trails trails, int padding, Context c) {

            if (!Prefs.isThemeLight()) L.toast(googleMap.setMapStyle(Prefs.getMapStyle(c)), c);

            // polyline
            PolylineOptions polyline = new PolylineOptions();
            polyline.color(c.getResources().getColor(R.color.colorGreenLight));
            polyline.addAll(trail.getLatLngs());
            googleMap.addPolyline(polyline);

            // avg poly
            /*if (trails != null) {
                PolylineOptions routePoly = new PolylineOptions();
                routePoly.color(c.getResources().getColor(R.color.colorWhite));
                routePoly.addAll(trails.toAvgTrail().getLatLngs());
                googleMap.addPolyline(routePoly);
            }*/

            // focus
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(trail.getBounds(), padding);
            try { googleMap.moveCamera(cu); }
            catch (Exception e) {
                googleMap.setOnMapLoadedCallback(() -> googleMap.moveCamera(cu));
            }

        }
        @Override protected ArrayList<String> getRestOfPolylines() {
            return Helper.getReader(this).getPolylines(_id);
        }

    }
    public static class RouteMap extends Base {

        private Trails trails;

        ////

        public static void startActivity(int routeId, Context c) {
            Intent intent = new Intent(c, RouteMap.class);
            intent.putExtra(EXTRA_ID, routeId);
            c.startActivity(intent);
        }

        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            trails = new Trails(Helper.getReader(this).getPolylinesByRoute(_id));
        }
        @Override public void onMapReady(GoogleMap googleMap) {
            super.onMapReady(googleMap);
            setReadyMap(googleMap, trails, MAP_PADDING, this);
        }

        public static void setReadyMap(final GoogleMap googleMap, final Trails trails, int padding, Context c) {

            if (!Prefs.isThemeLight()) L.toast(googleMap.setMapStyle(Prefs.getMapStyle(c)), c);
            if (trails.trailCount() == 0) return;

            // polyline
            for (List<LatLng> latLngs : trails.getLatLngs()) {
                PolylineOptions polyline = new PolylineOptions();
                polyline.color(c.getResources().getColor(R.color.colorGreenLight));
                polyline.addAll(latLngs);
                googleMap.addPolyline(polyline);
            }

            // avg poly
            //PolylineOptions polyline = new PolylineOptions();
            //polyline.color(c.getResources().getColor(R.color.colorWhite));
            //polyline.addAll(trails.toAvgTrail().getLatLngs());
            //googleMap.addPolyline(polyline);

            // focus
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(trails.getBounds(), padding);
            try { googleMap.moveCamera(cu); }
            catch (Exception e) {
                googleMap.setOnMapLoadedCallback(() -> googleMap.moveCamera(cu));
            }

        }
        @Override protected ArrayList<String> getRestOfPolylines() {
            return Helper.getReader(this).getPolylinesByRouteExcept(_id);
        }

    }

}
