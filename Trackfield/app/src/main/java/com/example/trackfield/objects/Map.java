package com.example.trackfield.objects;

import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.TreeMap;

public class Map {

    private int _superId;
    private TreeMap<Float, Coordinate> coordinates;
    private LatLngBounds bounds;

    ////

    public Map(int _superId, TreeMap<Float, Coordinate> coordinates) {
        this._superId = _superId;
        this.coordinates = coordinates;
        setBounds();
    }
    public Map(int _superId) {
        this._superId = _superId;
        coordinates = new TreeMap<>();
    }

    // set
    public void set_superId(int _superId) {
        this._superId = _superId;
    }
    public void setCoordinates(TreeMap<Float, Coordinate> coordinates) {
        this.coordinates = coordinates;
    }
    public void addCoordinate(float t, Coordinate P) {
        coordinates.put(t, P);
    }
    private void setBounds() {

        ArrayList<LatLng> latLngs = getLatLngs();
        if (coordinates.size() < 1) return;
        LatLng latLng = latLngs.get(0);

        double north = latLng.latitude;
        double south = latLng.latitude;
        double east = latLng.longitude;
        double west = latLng.longitude;

        for (int i = 1; i < latLngs.size(); i++) {
            LatLng latLng1 = latLngs.get(i);

            if (latLng1.latitude > north) north = latLng1.latitude;
            else if (latLng1.latitude < south) south = latLng1.latitude;

            if (latLng1.longitude > east) east = latLng1.longitude;
            else if (latLng1.longitude < west) west = latLng1.longitude;
        }

        LatLng northEast = new LatLng(north, east);
        LatLng southWest = new LatLng(south, west);
        bounds = new LatLngBounds(southWest, northEast);
    }

    // get
    public int get_superId() {
        return _superId;
    }
    public TreeMap<Float, Coordinate> getCoordinates() {
        return coordinates;
    }
    public ArrayList<LatLng> getLatLngs() {

        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (Coordinate P : coordinates.values()) { latLngs.add(D.toLatLng(P)); }
        return latLngs;
    }
    public LatLngBounds getBounds() {
        return bounds;
    }

    // driven
    public int distance() {

        int distance = 0;
        for (TreeMap.Entry<Float, Coordinate> entry : coordinates.entrySet()) {
            if (entry == coordinates.lastEntry()) break;
            if (coordinates.higherEntry(entry.getKey()) == null) break;

           Coordinate P = entry.getValue();
           Coordinate Q = coordinates.higherEntry(entry.getKey()).getValue();
           distance += P.distanceTo(Q);
        }
        return distance;
    }
    public int distance3D() {

        int distance = 0;
        for (TreeMap.Entry<Float, Coordinate> entry : coordinates.entrySet()) {
            if (entry == coordinates.lastEntry()) break;
            if (coordinates.higherEntry(entry.getKey()) == null) break;

            Coordinate P = entry.getValue();
            Coordinate Q = coordinates.higherEntry(entry.getKey()).getValue();
            distance += P.distanceTo3D(Q);
        }
        return distance;
    }

    public int elevationGain() {

        float elevGain = 0;
        for (TreeMap.Entry<Float, Coordinate> entry : coordinates.entrySet()) {
            if (entry == coordinates.lastEntry()) break;
            if (coordinates.higherEntry(entry.getKey()) == null) break;

            Coordinate P = entry.getValue();
            Coordinate Q = coordinates.higherEntry(entry.getKey()).getValue();
            float elevDelta = P.elevationTo(Q);
            if (elevDelta > 0) elevGain += elevDelta;
        }

        return (int) elevGain;
    }
    public int elevationLoss() {

        float elevLoss = 0;
        for (TreeMap.Entry<Float, Coordinate> entry : coordinates.entrySet()) {
            if (entry == coordinates.lastEntry()) break;
            if (coordinates.higherEntry(entry.getKey()) == null) break;

            Coordinate P = entry.getValue();
            Coordinate Q = coordinates.higherEntry(entry.getKey()).getValue();
            float elevDelta = P.elevationTo(Q);
            if (elevDelta < 0) elevLoss += elevDelta;
        }

        return (int) elevLoss;
    }

    public float time() {
        return coordinates.size() == 0 ? 0 : coordinates.lastKey();
    }

}
