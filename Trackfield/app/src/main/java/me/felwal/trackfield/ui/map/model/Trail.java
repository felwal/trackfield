package me.felwal.trackfield.ui.map.model;

import androidx.annotation.Nullable;

import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.List;

import me.felwal.trackfield.utils.LocationUtilsKt;

public class Trail {

    private final String polyline;
    private final List<LatLng> latLngs;

    private LatLng start;
    private LatLng end;
    private LatLngBounds bounds;

    //

    public Trail(String polyline, LatLng start, LatLng end) {
        this.polyline = polyline;
        latLngs = LocationUtilsKt.toLatLngs(PolylineUtils.decode(polyline, 5));
        this.start = start;
        this.end = end;
        setBounds();
    }

    public Trail(String polyline) {
        this.polyline = polyline;
        latLngs = LocationUtilsKt.toLatLngs(PolylineUtils.decode(polyline, 5));
        setBounds();
    }

    public Trail(List<LatLng> latLngs, LatLng start, LatLng end) {
        polyline = PolylineUtils.encode(LocationUtilsKt.toPoints(latLngs), 5);
        this.latLngs = latLngs;
        this.start = start;
        this.end = end;
        setBounds();
    }

    public Trail(List<LatLng> latLngs) {
        polyline = PolylineUtils.encode(LocationUtilsKt.toPoints(latLngs), 5);
        this.latLngs = latLngs;
        setBounds();
    }

    // set

    private void setBounds() {
        bounds = LocationUtilsKt.getBounds(latLngs);
    }

    public void calibrateEndPoints() {
        start = latLngs.get(0);
        end = latLngs.get(latLngs.size() - 1);
    }

    // get

    public String getPolyline() {
        return polyline;
    }

    public List<LatLng> getLatLngs() {
        return latLngs;
    }

    public LatLng getStart() {
        return start;
    }

    public LatLng getEnd() {
        return end;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    // get driven

    public double getStartLat() {
        return start.getLatitude();
    }

    public double getStartLng() {
        return start.getLongitude();
    }

    public double getEndLat() {
        return end.getLatitude();
    }

    public double getEndLng() {
        return end.getLongitude();
    }

    public boolean hasStartEnd() {
        return start != null && end != null;
    }

    public int getDistance() {
        return (int) LocationUtilsKt.distance(latLngs);
    }

    public int getLatLngCount() {
        return latLngs == null ? 0 : latLngs.size();
    }

    //

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
        if (!(obj instanceof Trail)) return false;
        if (this == obj) return true;

        Trail other = (Trail) obj;
        return latLngs.equals(other.latLngs) && start.equals(other.start) && end.equals(other.end)
            && bounds.equals(other.bounds);
    }

}
