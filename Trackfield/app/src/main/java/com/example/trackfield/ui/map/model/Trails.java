package com.example.trackfield.ui.map.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Trails {

    private final List<Trail> trails = new ArrayList<>();

    //

    public Trails(ArrayList<String> polylines) {
        for (String s : polylines) {
            trails.add(new Trail(s));
        }

    }

    public Trails(List<List<LatLng>> latLngs) {
        for (List<LatLng> ll : latLngs) {
            trails.add(new Trail(ll));
        }
    }

    // get

    public List<List<LatLng>> getLatLngs() {

        List<List<LatLng>> latLngs = new ArrayList<>();
        for (Trail trail : trails) latLngs.add(trail.getLatLngs());

        return latLngs;
    }

    public LatLngBounds getBounds() {
        double north = trails.get(0).getBounds().northeast.latitude;
        double south = trails.get(0).getBounds().southwest.latitude;
        double east = trails.get(0).getBounds().northeast.longitude;
        double west = trails.get(0).getBounds().southwest.longitude;

        for (int i = 1; i < trails.size(); i++) {
            double n = trails.get(i).getBounds().northeast.latitude;
            double s = trails.get(i).getBounds().southwest.latitude;
            double e = trails.get(i).getBounds().northeast.longitude;
            double w = trails.get(i).getBounds().southwest.longitude;

            north = Math.max(north, n);
            south = Math.min(south, s);
            east = Math.max(east, e);
            west = Math.min(west, w);
        }

        LatLng northEast = new LatLng(north, east);
        LatLng southWest = new LatLng(south, west);

        return new LatLngBounds(southWest, northEast);
    }

    // get driven

    public Trail toAvgTrail() {
        List<LatLng> latLngs = new ArrayList<>();

        float pointCount = trails.get(0).getDistance() / 15f;
        float step = 1 / pointCount;

        for (float percent = 0; percent < 1 + step; percent += step) {
            List<LatLng> coordsForAvg = new ArrayList<>();
            for (Trail trail : trails) {
                coordsForAvg.add(trail.at(percent));
            }
            latLngs.add(Trail.avg(coordsForAvg));
        }

        return new Trail(latLngs);
    }

    // get driven

    public int trailCount() {
        return trails == null ? 0 : trails.size();
    }

}
