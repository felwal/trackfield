package com.felwal.trackfield.ui.map.model;

import android.location.Location;

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
        int distance = 0;

        for (int i = 0; i < latLngs.size() - 1; i++) {
            LatLng P = latLngs.get(i);
            LatLng Q = latLngs.get(i+1);
            if (P == null || Q == null) continue;

            float[] distBetween = new float[1];
            Location.distanceBetween(P.latitude, P.longitude, Q.latitude, Q.longitude, distBetween);
            distance += distBetween[0];
        }

        return distance;
    }

    public int getLatLngCount() {
        return latLngs == null ? 0 : latLngs.size();
    }

    public LatLng at(float percent) {
        if (percent < 0) percent = 0;
        if (percent > 1) percent = 1;

        int trailLength = getDistance();
        int distTo = (int) (trailLength * percent);

        float dist = 0;
        LatLng at = null;

        for (int i = 0; i < latLngs.size() - 1; i++) {
            LatLng P = latLngs.get(i);
            LatLng Q = latLngs.get(i+1);
            if (P == null || Q == null) continue;

            float[] distBetweenArr = new float[1];
            Location.distanceBetween(P.latitude, P.longitude, Q.latitude, Q.longitude, distBetweenArr);
            float distBetween = distBetweenArr[0];

            if (dist + distBetween < distTo) {
                dist += distBetween;
            }
            else {
                at = between(P, Q, (distTo - dist) / distBetween);
                break;
            }
        }

        return at;
    }

    // static tools

    public static LatLng between(LatLng P, LatLng Q, float percent) {
        if (P == null || Q == null) return null;

        double lat = P.latitude + (Q.latitude - P.latitude) * percent;
        double lng = P.longitude + (Q.longitude - P.longitude) * percent;

        return new LatLng(lat, lng);
    }

    public static LatLng avg(List<LatLng> latLngs) {
        if (latLngs == null || latLngs.size() == 0) return null;

        double latTot = 0;
        double lngTot = 0;

        for (LatLng pos : latLngs) {
            if (pos == null) continue;
            latTot += pos.latitude;
            lngTot += pos.longitude;
        }

        return new LatLng(latTot / latLngs.size(), lngTot / latLngs.size());
    }

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































