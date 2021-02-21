package com.example.trackfield.activities.map_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.objects.Trails;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;

public class ExerciseMapActivity extends MapActivity {

    private Trail trail;
    private Trails routeTrails;

    ////

    public static void startActivity(int _id, Context c) {
        Intent intent = new Intent(c, ExerciseMapActivity.class);
        intent.putExtra(EXTRA_ID, _id);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trail = Helper.getReader(this).getTrail(_id); //getExercise(_id).getTrail();
        Exercise e = Helper.getReader().getExercise(_id);
        //routeTrails = new Trails(Helper.getReader().getPolylinesByRoute(e.getRouteId(), e.getRouteVar()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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

    // set

    public static Polyline setReadyMap(final GoogleMap googleMap, final Trail trail, Trails trails, int padding, Context c) {

        if (!Prefs.isThemeLight()) L.toast(googleMap.setMapStyle(Prefs.getMapStyle(c)), c);

        // polyline
        PolylineOptions options = new PolylineOptions();
        options.color(polyColorSelected(c));
        options.addAll(trail.getLatLngs());
        Polyline polyline = googleMap.addPolyline(options);

        // avg poly
        /*if (trails != null) {
            PolylineOptions routePoly = new PolylineOptions();
            routePoly.color(c.getResources().getColor(R.color.colorWhite));
            routePoly.addAll(trails.toAvgTrail().getLatLngs());
            googleMap.addPolyline(routePoly);
        }*/

        moveCamera(googleMap, trail.getBounds(), padding, false);

        return polyline;
    }

    // extends

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return Helper.getReader(this).getPolylines(exceptId);
    }

}
