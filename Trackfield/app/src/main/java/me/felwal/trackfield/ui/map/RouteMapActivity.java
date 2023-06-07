package me.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.ui.map.model.Trails;
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
    public void onMapReady(MapboxMap mapboxMap) {
        super.onMapReady(mapboxMap);
        selectedPolylines = setReadyMap(mapboxMap, trails, MAP_PADDING);
        mapboxMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void recentre() {
        moveCamera(mapboxMap, trails.getBounds(), MAP_PADDING, true);
    }

    // set

    private ArrayList<Polyline> setReadyMap(final MapboxMap mapboxMap, final Trails trails, int padding) {
        if (trails.trailCount() == 0) return new ArrayList<>();

        // draw polylines
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (List<LatLng> latLngs : trails.getLatLngs()) {
            PolylineOptions options = new PolylineOptions();
            options.color(getColorSelected());
            options.addAll(latLngs);
            polylines.add(mapboxMap.addPolyline(options));
        }

        // draw routevar average polyline
        if (routeVar != null) {
            PolylineOptions options = new PolylineOptions();
            options.color(ResourcesKt.getColorByAttr(this, android.R.attr.textColorPrimary));
            options.addAll(LocationUtilsKt.averageSegment(trails.getLatLngs()));
            polylines.add(mapboxMap.addPolyline(options));
        }

        // focus
        moveCamera(mapboxMap, trails.getBounds(), padding, false);

        return polylines;
    }

    // extends MapActivity

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylinesByRouteExcept(exceptId);
    }

}
