package me.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

import java.util.ArrayList;
import java.util.HashMap;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.db.model.Place;

public class PlaceMapActivity extends MapActivity {

    private Place place;

    //

    public static void startActivity(int placeId, Context c) {
        Intent intent = new Intent(c, PlaceMapActivity.class);
        intent.putExtra(EXTRA_ID, placeId);
        c.startActivity(intent);
    }

    // on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        place = DbReader.get(this).getPlace(id);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        super.onMapReady(mapboxMap);
        selectedPolylines = setReadyMap(mapboxMap);
        mapboxMap.setOnPolylineClickListener(this);
    }

    @Override
    protected void recentre() {
        //moveCamera(map, place.getBounds(), MAP_PADDING, true);
    }

    // set

    private ArrayList<Polyline> setReadyMap(final MapboxMap mapboxMap) {
        // radius circle
        CircleOptions circle = new CircleOptions();
        //circle.center(place.getLocation());
        circle.withCircleRadius((float) place.getRadius());
        //circle.withCircleColor(getColorDeselected());
        circle.withCircleStrokeWidth(0f);
        //mapboxMap.addCircle(circle);

        // marker
        MarkerOptions placeMarker = new MarkerOptions();
        Drawable drawable = ResourcesKt.getDrawableCompatWithTint(this, R.drawable.ic_place, R.attr.colorSecondary);
        Bitmap bitmap = ResourcesKt.toBitmap(drawable);
        //BitmapDescriptor bitmapDesc = BitmapDescriptorFactory.fromBitmap(bitmap);
        //placeMarker.icon(bitmapDesc);
        //placeMarker.position(place.getLocation());
        mapboxMap.addMarker(placeMarker);

        //moveCamera(mapboxMap, place.getBounds(), MAP_PADDING, false);

        return new ArrayList<>();
    }

    // extends MapActivity

    @Override
    protected HashMap<Integer, String> getRestOfPolylines(int exceptId) {
        return DbReader.get(this).getPolylines(Exercise.ID_NONE);
    }

}
