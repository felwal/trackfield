package me.felwal.trackfield.ui.map.model;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

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
        double north = trails.get(0).getBounds().getLatNorth();
        double south = trails.get(0).getBounds().getLatSouth();
        double east = trails.get(0).getBounds().getLonEast();
        double west = trails.get(0).getBounds().getLonWest();

        for (int i = 1; i < trails.size(); i++) {
            double n = trails.get(i).getBounds().getLatNorth();
            double s = trails.get(i).getBounds().getLatSouth();
            double e = trails.get(i).getBounds().getLonEast();
            double w = trails.get(i).getBounds().getLonWest();

            north = Math.max(north, n);
            south = Math.min(south, s);
            east = Math.max(east, e);
            west = Math.min(west, w);
        }

        return LatLngBounds.from(north, east, south, west);
    }

    // get driven

    public int trailCount() {
        return trails == null ? 0 : trails.size();
    }

}
