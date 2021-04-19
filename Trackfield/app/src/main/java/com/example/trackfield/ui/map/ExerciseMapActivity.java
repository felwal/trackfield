package com.example.trackfield.ui.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.ui.map.model.Trail;
import com.example.trackfield.ui.map.model.Trails;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.data.prefs.Prefs;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class ExerciseMapActivity extends MapActivity {

    private Trail trail;
    private Trails routeTrails;

    //

    public static void startActivity(int _id, Context c) {
        Intent intent = new Intent(c, ExerciseMapActivity.class);
        intent.putExtra(EXTRA_ID, _id);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trail = DbReader.get(this).getTrail(_id); //getExercise(_id).getTrail();
        Exercise e = DbReader.get(this).getExercise(_id);
        //routeTrails = new Trails(Reader.get().getPolylinesByRoute(e.getRouteId(), e.getRouteVar()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        seletedPolylines = setReadyMap(googleMap, trail, routeTrails, MAP_PADDING, this);
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

    @Override
    protected void recentre() {
        moveCamera(googleMap, trail.getBounds(), MAP_PADDING, true);
    }

    // set

    public static ArrayList<Polyline> setReadyMap(final GoogleMap googleMap, final Trail trail, Trails trails, int padding, Activity a) {

        // style
        if (!Prefs.isThemeLight()) LayoutUtils.toast(googleMap.setMapStyle(Prefs.getMapStyle(a)), a);

        // polyline
        PolylineOptions options = new PolylineOptions();
        options.color(getColorSelected(a));
        options.addAll(trail.getLatLngs());
        Polyline polyline = googleMap.addPolyline(options);

        ArrayList<Polyline> polylines = new ArrayList<>();
        polylines.add(polyline);

        // avg poly
        /*if (trails != null) {
            PolylineOptions routePoly = new PolylineOptions();
            routePoly.color(c.getResources().getColor(R.color.colorWhite));
            routePoly.addAll(trails.toAvgTrail().getLatLngs());
            googleMap.addPolyline(routePoly);
        }*/

        moveCamera(googleMap, trail.getBounds(), padding, false);

        return polylines;
    }

    // extends

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylines(exceptId);
    }

}
