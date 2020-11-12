package com.example.trackfield.objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class Trails {

    private List<String> polylines;
    private List<List<LatLng>> latLngs;
    private LatLngBounds bounds;

    ////

    public Trails(ArrayList<String> polylines) {
        this.polylines = polylines;
        latLngs = new ArrayList<>();
        for (String s : polylines) {
            latLngs.add(PolyUtil.decode(s));
        }
        setBounds();
    }
    public Trails(List<List<LatLng>> latLngs) {
        polylines = new ArrayList<>();
        for (List<LatLng> ll : latLngs) {
            polylines.add(PolyUtil.encode(ll));
        }
        this.latLngs = latLngs;
        setBounds();
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
        return latLngs;
    }
    public LatLngBounds getBounds() {
        return bounds;
    }

    // get driven
    public int trailCount() {
        return latLngs == null ? 0 : latLngs.size();
    }

}
