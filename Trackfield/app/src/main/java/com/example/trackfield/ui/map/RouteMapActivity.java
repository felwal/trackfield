package com.example.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.data.prefs.Prefs;
import com.example.trackfield.ui.map.model.Trails;
import com.example.trackfield.utils.LayoutUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteMapActivity extends MapActivity {

    private Trails trails;

    //

    public static void startActivity(int routeId, Context c) {
        Intent intent = new Intent(c, RouteMapActivity.class);
        intent.putExtra(EXTRA_ID, routeId);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trails = new Trails(DbReader.get(this).getPolylinesByRoute(id));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        seletedPolylines = setReadyMap(googleMap, trails, MAP_PADDING, this);
        googleMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void recentre() {
        moveCamera(map, trails.getBounds(), MAP_PADDING, true);
    }

    // set

    public static ArrayList<Polyline> setReadyMap(final GoogleMap googleMap, final Trails trails, int padding,
        Context c) {

        //style
        if (!Prefs.isThemeLight()) LayoutUtils.toast(googleMap.setMapStyle(Prefs.getMapStyle(c)), c);

        if (trails.trailCount() == 0) return new ArrayList<>();

        // polyline
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (List<LatLng> latLngs : trails.getLatLngs()) {
            PolylineOptions options = new PolylineOptions();
            options.color(c.getResources().getColor(R.color.colorGreenLight));
            options.addAll(latLngs);
            polylines.add(googleMap.addPolyline(options));
        }

        // focus
        moveCamera(googleMap, trails.getBounds(), padding, false);

        return polylines;
    }

    // extends MapActivity

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylinesByRouteExcept(exceptId);
    }

}
