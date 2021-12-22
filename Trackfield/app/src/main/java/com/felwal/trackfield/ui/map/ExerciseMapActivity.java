package com.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.ui.map.model.Trail;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class ExerciseMapActivity extends MapActivity {

    private Trail trail;

    //

    public static void startActivity(int exerciseId, Context c) {
        Intent intent = new Intent(c, ExerciseMapActivity.class);
        intent.putExtra(EXTRA_ID, exerciseId);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trail = DbReader.get(this).getTrail(id);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        seletedPolylines = setReadyMap(googleMap, trail, MAP_PADDING, this);
        googleMap.setOnPolylineClickListener(this);

        // start marker
        /*Bitmap startBitmap = MiscUtilsKt.getBitmap(this, R.drawable.ic_play);
        if (startBitmap != null) {
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(trail.getStart());
            startMarker.icon(BitmapDescriptorFactory.fromBitmap(startBitmap));
            googleMap.addMarker(startMarker);
        }

        // end marker
        Bitmap endBitmap = MiscUtilsKt.getBitmap(this, R.drawable.ic_goal);
        if (endBitmap != null) {
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(trail.getEnd());
            endMarker.icon(BitmapDescriptorFactory.fromBitmap(endBitmap));
            googleMap.addMarker(endMarker);
        }*/

        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    protected void recentre() {
        moveCamera(map, trail.getBounds(), MAP_PADDING, true);
    }

    // set

    public static ArrayList<Polyline> setReadyMap(final GoogleMap googleMap, final Trail trail, int padding,
        Context c) {

        // style
        setMapStyle(googleMap, c);

        // draw polylines
        PolylineOptions options = new PolylineOptions();
        options.color(getColorSelected(c));
        options.addAll(trail.getLatLngs());
        Polyline polyline = googleMap.addPolyline(options);
        ArrayList<Polyline> polylines = new ArrayList<>();
        polylines.add(polyline);

        moveCamera(googleMap, trail.getBounds(), padding, false);

        return polylines;
    }

    // extends MapActivity

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylines(exceptId);
    }

}
