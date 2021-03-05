package com.example.trackfield.activities.mapactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.R;
import com.example.trackfield.database.Reader;
import com.example.trackfield.objects.Trails;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

public class RouteMapActivity extends MapActivity {

    private Trails trails;

    ////

    public static void startActivity(int routeId, Context c) {
        Intent intent = new Intent(c, RouteMapActivity.class);
        intent.putExtra(EXTRA_ID, routeId);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trails = new Trails(Reader.get(this).getPolylinesByRoute(_id));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        setReadyMap(googleMap, trails, MAP_PADDING, this);
        googleMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void recentre() {
        moveCamera(googleMap, trails.getBounds(), MAP_PADDING, true);
    }

    // set

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
        moveCamera(googleMap, trails.getBounds(), padding, false);

    }

    // extends

    @Override protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return Reader.get(this).getPolylinesByRouteExcept(exceptId);
    }

}
