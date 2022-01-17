package com.felwal.trackfield.ui.map.model;

import com.felwal.trackfield.utils.LocationUtilsKt;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class Trail {

    private final String polyline;
    private final List<LatLng> latLngs;

    private LatLng start;
    private LatLng end;
    private LatLngBounds bounds;

    //

    public Trail(String polyline, LatLng start, LatLng end) {
        this.polyline = polyline;
        latLngs = PolyUtil.decode(polyline);
        this.start = start;
        this.end = end;
        setBounds();
    }

    public Trail(String polyline) {
        this.polyline = polyline;
        latLngs = PolyUtil.decode(polyline);
        setBounds();
    }

    public Trail(List<LatLng> latLngs, LatLng start, LatLng end) {
        polyline = PolyUtil.encode(latLngs);
        this.latLngs = latLngs;
        this.start = start;
        this.end = end;
        setBounds();
    }

    public Trail(List<LatLng> latLngs) {
        polyline = PolyUtil.encode(latLngs);
        this.latLngs = latLngs;
        setBounds();
    }

    // set

    private void setBounds() {
        if (latLngs.size() == 0) return;
        LatLng ll0 = latLngs.get(0);

        double north = ll0.latitude;
        double south = ll0.latitude;
        double east = ll0.longitude;
        double west = ll0.longitude;

        for (int i = 1; i < latLngs.size(); i++) {
            LatLng lli = latLngs.get(i);

            if (lli.latitude > north) north = lli.latitude;
            else if (lli.latitude < south) south = lli.latitude;

            if (lli.longitude > east) east = lli.longitude;
            else if (lli.longitude < west) west = lli.longitude;
        }

        LatLng northEast = new LatLng(north, east);
        LatLng southWest = new LatLng(south, west);
        bounds = new LatLngBounds(southWest, northEast);
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
        return start.latitude;
    }

    public double getStartLng() {
        return start.longitude;
    }

    public double getEndLat() {
        return end.latitude;
    }

    public double getEndLng() {
        return end.longitude;
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

    // static tools

    public static LatLngBounds bounds(List<LatLng> latLngs) {
        if (latLngs.size() == 0) return null;
        LatLng ll0 = latLngs.get(0);

        double north = ll0.latitude;
        double south = ll0.latitude;
        double east = ll0.longitude;
        double west = ll0.longitude;

        for (int i = 1; i < latLngs.size(); i++) {
            LatLng lli = latLngs.get(i);

            if (lli.latitude > north) north = lli.latitude;
            else if (lli.latitude < south) south = lli.latitude;

            if (lli.longitude > east) east = lli.longitude;
            else if (lli.longitude < west) west = lli.longitude;
        }

        LatLng northEast = new LatLng(north, east);
        LatLng southWest = new LatLng(south, west);
        return new LatLngBounds(southWest, northEast);
    }

}































