package me.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.ui.map.model.Trails;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.utils.LocationUtilsKt;

public class RouteMapActivity extends MapActivity {

    // extras names
    protected static final String EXTRA_ROUTEVAR = "routeVar";

    private String routeVar;
    private Trails trails;

    //

    public static void startActivity(int routeId, Context c) {
        Intent intent = new Intent(c, RouteMapActivity.class);
        intent.putExtra(EXTRA_ID, routeId);
        c.startActivity(intent);
    }

    public static void startActivity(int routeId, String routeVar, Context c) {
        Intent intent = new Intent(c, RouteMapActivity.class);
        intent.putExtra(EXTRA_ID, routeId);
        intent.putExtra(EXTRA_ROUTEVAR, routeVar);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get route or route + routevar trails
        if (getIntent().hasExtra(EXTRA_ROUTEVAR)) {
            routeVar = getIntent().getStringExtra(EXTRA_ROUTEVAR);
            trails = new Trails(DbReader.get(this).getPolylinesByRoute(id, routeVar));
        }
        else {
            trails = new Trails(DbReader.get(this).getPolylinesByRoute(id));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        selectedPolylines = setReadyMap(googleMap, trails, MAP_PADDING);
        googleMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void recentre() {
        moveCamera(map, trails.getBounds(), MAP_PADDING, true);
    }

    // set

    private ArrayList<Polyline> setReadyMap(final GoogleMap googleMap, final Trails trails, int padding) {

        //style
        setMapStyle(googleMap, this);

        if (trails.trailCount() == 0) return new ArrayList<>();

        // draw polylines
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (List<LatLng> latLngs : trails.getLatLngs()) {
            PolylineOptions options = new PolylineOptions().zIndex(0);
            options.color(getColorSelected());
            options.addAll(latLngs);
            polylines.add(googleMap.addPolyline(options));
        }

        // draw routevar average polyline
        if (routeVar != null) {
            PolylineOptions options = new PolylineOptions().zIndex(1);
            options.color(ResourcesKt.getColorByAttr(this, android.R.attr.textColorPrimary));
            options.addAll(LocationUtilsKt.averageSegment(trails.getLatLngs()));
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
