package com.example.trackfield.objects;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Trail {

    private String polyline;
    private List<LatLng> latLngs;
    private LatLng start;
    private LatLng end;
    private LatLngBounds bounds;

    ////

    public Trail(String polyline, LatLng start, LatLng end) {
        this.polyline = polyline;
        latLngs = PolyUtil.decode(polyline);
        this.start = start;
        this.end = end;
        setBounds();
    }
    public Trail(List<LatLng> latLngs, LatLng start, LatLng end) {
        polyline = PolyUtil.encode(latLngs);
        this.latLngs = latLngs;
        this.start = start;
        this.end = end;
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

}
