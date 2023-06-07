package me.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.HashMap;

import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.ui.map.model.Trail;
import me.felwal.trackfield.utils.ScreenUtils;

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
    public void onMapReady(MapboxMap mapboxMap) {
        super.onMapReady(mapboxMap);
        selectedPolylines = setReadyMap(mapboxMap, trail, MAP_PADDING, this);
        mapboxMap.setOnPolylineClickListener(this);

        // start marker
        /*Bitmap startBitmap = MiscUtilsKt.getBitmap(this, R.drawable.ic_play);
        if (startBitmap != null) {
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(trail.getStart());
            startMarker.icon(BitmapDescriptorFactory.fromBitmap(startBitmap));
            mapboxMap.addMarker(startMarker);
        }

        // end marker
        Bitmap endBitmap = MiscUtilsKt.getBitmap(this, R.drawable.ic_goal);
        if (endBitmap != null) {
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(trail.getEnd());
            endMarker.icon(BitmapDescriptorFactory.fromBitmap(endBitmap));
            mapboxMap.addMarker(endMarker);
        }*/

        //mapboxMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    protected void recentre() {
        moveCamera(mapboxMap, trail.getBounds(), MAP_PADDING, true);
    }

    // set

    public static ArrayList<Polyline> setReadyMap(final MapboxMap mapboxMap, final Trail trail, int padding,
        Context c) {

        // draw polylines
        PolylineOptions options = new PolylineOptions();
        options.color(getColorSelected(false, c));
        options.addAll(trail.getLatLngs());
        Polyline polyline = mapboxMap.addPolyline(options);
        polyline.setWidth(ScreenUtils.px(3));
        ArrayList<Polyline> polylines = new ArrayList<>();
        polylines.add(polyline);

        moveCamera(mapboxMap, trail.getBounds(), padding, false);

        return polylines;
    }

    // extends MapActivity

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylines(exceptId);
    }

}
