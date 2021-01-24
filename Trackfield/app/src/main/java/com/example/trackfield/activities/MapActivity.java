package com.example.trackfield.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.fragments.dialogs.PeekSheet;
import com.example.trackfield.items.Exerlite;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity {

    public static abstract class Base extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener{

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
            setToolbar();

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
        }

        // tools
        private void togglePolylines() {

            if (!tempShown) {
                if (tempOptions.size() != 0) {
                    tempOptions.clear(); // temp
                    for (PolylineOptions options : tempOptions) {
                        tempPolylines.add(googleMap.addPolyline(options));
                    }
                }
                if (tempOptions.size() == 0) {
                    for (HashMap.Entry<Integer, String> entry : getRestOfPolylines().entrySet()) {

                        // options
                        PolylineOptions options = new PolylineOptions();
                        options.color(getResources().getColor(R.color.colorGreenLightTrans));
                        options.width(L.px(3));
                        options.addAll(PolyUtil.decode(entry.getValue()));
                        tempOptions.add(options);

                        // poly
                        Polyline polyline = googleMap.addPolyline(options);
                        polyline.setTag(entry.getKey());
                        polyline.setClickable(true);
                        tempPolylines.add(polyline);
                    }
                }
            }
            else {
                for (Polyline p : tempPolylines) p.remove();
                tempPolylines.clear();
            }

            tempShown = !tempShown;
        }
        protected abstract HashMap<Integer, String> getRestOfPolylines();

        private void setToolbar() {
            final Toolbar tb = findViewById(R.id.toolbar_map);
            setSupportActionBar(tb);
            ActionBar ab = getSupportActionBar();
            ab.setTitle("");//getResources().getString(R.string.activity_maps));
            ab.setDisplayHomeAsUpEnabled(true);
        }
        @Override public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_toolbar_map, menu);
            return true;
        }
        @Override public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    return true;

                case R.id.action_maptype:
                    googleMap.setMapType(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
                    return true;

                case R.id.action_heatmap:
                    togglePolylines();
                    return true;

                case R.id.action_filter:
                    return true;

                default: return super.onOptionsItemSelected(item);
            }
        }

        @Override public void onPolylineClick(Polyline polyline) {
            int id = (int) polyline.getTag();
            PeekSheet.newInstance(id, getSupportFragmentManager());
        }

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
            Polyline polyline = setReadyMap(googleMap, trail, routeTrails, MAP_PADDING, this);
            googleMap.setOnPolylineClickListener(this);

            // markers
            MarkerOptions startMarker = new MarkerOptions();
            MarkerOptions endMarker = new MarkerOptions();//.icon(BitmapDescriptorFactory.fromResource((R.drawable.ic_map_marker_24dp)));
            startMarker.position(trail.getStart());
            endMarker.position(trail.getEnd());
            //googleMap.addMarker(startMarker);
            //googleMap.addMarker(endMarker);

            //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        public static Polyline setReadyMap(final GoogleMap googleMap, final Trail trail, Trails trails, int padding, Context c) {

            if (!Prefs.isThemeLight()) L.toast(googleMap.setMapStyle(Prefs.getMapStyle(c)), c);

            // polyline
            PolylineOptions options = new PolylineOptions();
            options.color(c.getResources().getColor(R.color.colorGreenLight));
            options.addAll(trail.getLatLngs());
            Polyline polyline = googleMap.addPolyline(options);

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

            return polyline;
        }
        @Override protected HashMap<Integer, String> getRestOfPolylines() {
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
            googleMap.setOnPolylineClickListener(this);
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
        @Override protected HashMap<Integer, String> getRestOfPolylines() {
            return Helper.getReader(this).getPolylinesByRouteExcept(_id);
        }

    }

}
