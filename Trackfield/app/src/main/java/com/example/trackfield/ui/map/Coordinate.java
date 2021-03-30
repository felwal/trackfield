package com.example.trackfield.ui.map;

import android.location.Location;

import com.example.trackfield.service.toolbox.M;

@Deprecated public class Coordinate extends Location {

    private int _id;

    private static final String PROVIDER = "provider";

    ////

    public Coordinate(int _id, Location location) {
        super(location);
        this._id = _id;
    }
    public Coordinate(int _id, double latitude, double longitude, double altitude) {
        super(PROVIDER);
        this._id = _id;
        setLatitude(latitude);
        setLongitude(longitude);
        setAltitude(altitude);
    }

    // get
    public int get_id() {
        return _id;
    }

    // jommetry
    public float distanceTo3D(Coordinate Q) {
        return (float) Math.sqrt(M.sqr(distanceTo(Q)) + M.sqr(elevationTo(Q)));
    }
    public float elevationTo(Coordinate Q) {
        return (float) (Q.getAltitude() - getAltitude());
    }

}