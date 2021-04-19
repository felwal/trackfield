package com.example.trackfield.ui.map.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Trails {

    private List<Trail> trails = new ArrayList<>();
    private List<String> polylines;
    private List<List<LatLng>> latLngs;
    private LatLngBounds bounds;

    //

    public Trails(ArrayList<String> polylines) {
        //this.polylines = polylines;
        //latLngs = new ArrayList<>();
        for (String s : polylines) {
            //latLngs.add(PolyUtil.decode(s));
            trails.add(new Trail(s));
        }
        //setBounds();
    }

    public Trails(List<List<LatLng>> latLngs) {
        //polylines = new ArrayList<>();
        for (List<LatLng> ll : latLngs) {
            //polylines.add(PolyUtil.encode(ll));
            trails.add(new Trail(ll));
        }
        //this.latLngs = latLngs;
        //setBounds();
    }

    // set

    private void setBounds() {

        if (latLngs.size() == 0 || latLngs.get(0).size() == 0) return;
        LatLng ll0 = latLngs.get(0).get(0);

        double north = ll0.latitude;
        double south = ll0.latitude;
        double east = ll0.longitude;
        double west = ll0.longitude;

        for (int i = 0; i < latLngs.size(); i++) {
            for (int j = 1; j < latLngs.get(i).size(); j++) {
                LatLng llj = latLngs.get(i).get(j);

                if (llj.latitude > north) north = llj.latitude;
                else if (llj.latitude < south) south = llj.latitude;

                if (llj.longitude > east) east = llj.longitude;
                else if (llj.longitude < west) west = llj.longitude;
            }
        }

        LatLng northEast = new LatLng(north, east);
        LatLng southWest = new LatLng(south, west);
        bounds = new LatLngBounds(southWest, northEast);
    }

    // get

    public List<String> getPolylines() {
        return polylines;
    }

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
        LatLngBounds bounds = new LatLngBounds(southWest, northEast);

        return bounds;
    }

    // get driven

    public Trail toAvgTrail() {

        List<LatLng> latLngs = new ArrayList<>();

        float pointCount = trails.get(0).getDistance() / 15;
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
