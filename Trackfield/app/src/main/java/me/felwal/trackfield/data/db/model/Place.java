package me.felwal.trackfield.data.db.model;

import android.content.Context;
import android.location.Location;

import me.felwal.trackfield.utils.LayoutUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class Place implements JSONObjectable {

    private static final String JSON_ID = "_id";
    private static final String JSON_NAME = "name";
    private static final String JSON_LAT = "lat";
    private static final String JSON_LNG = "lng";
    private static final String JSON_RADIUS = "radius";
    private static final String JSON_HIDDEN = "hidden";

    public static final String NAME_NONE = "Unnamed place";
    public static final int ID_NON_EXISTANT = -1;

    private final int id;
    private String name;
    private LatLng location;
    private int radius = 500;
    private boolean hidden = false;

    //

    public Place(int id, String name, double lat, double lng, int radius, boolean hidden) {
        this.id = id;
        this.name = name;
        this.radius = radius;
        this.hidden = hidden;
        location = new LatLng(lat, lng);
    }

    public Place(double lat, double lng) {
        this(new LatLng(lat, lng));
    }

    public Place(LatLng latLng) {
        id = ID_NON_EXISTANT;
        name = NAME_NONE;
        location = latLng;
    }

    public Place() {
        id = ID_NON_EXISTANT;
        name = NAME_NONE;
        location = new LatLng(0, 0);
    }

    public Place(JSONObject obj) throws JSONException {
        id = obj.getInt(JSON_ID);
        name = obj.getString(JSON_NAME);
        location = new LatLng(obj.getDouble(JSON_LAT), obj.getDouble(JSON_LNG));
        radius = obj.getInt(JSON_RADIUS);
        hidden = obj.getBoolean(JSON_HIDDEN);
    }

    // set

    public void setName(String name) {
        this.name = name;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void invertHidden() {
        hidden = !hidden;
    }

    // get

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public double getLat() {
        return location.latitude;
    }

    public double getLng() {
        return location.longitude;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isHidden() {
        return hidden;
    }

    //

    public boolean contains(LatLng latLng) {
        float[] distBetweenArr = new float[1];
        Location.distanceBetween(getLat(), getLng(), latLng.latitude, latLng.longitude, distBetweenArr);
        float distBetween = distBetweenArr[0];

        return distBetween <= radius;
    }

    public LatLngBounds getBounds() {
        LatLng south = SphericalUtil.computeOffset(location, radius,180);
        LatLng southWest = SphericalUtil.computeOffset(south, radius,270);

        LatLng north = SphericalUtil.computeOffset(location, radius,0);
        LatLng northEast = SphericalUtil.computeOffset(north, radius,90);

        return new LatLngBounds(southWest, northEast);
    }

    // extends

    @Override
    public JSONObject toJSONObject(Context c) {
        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, id);
            obj.put(JSON_NAME, name);
            obj.put(JSON_LAT, getLat());
            obj.put(JSON_LNG, getLng());
            obj.put(JSON_RADIUS, radius);
            obj.put(JSON_HIDDEN, hidden);
        }
        catch (JSONException e) {
            LayoutUtils.handleError(e, c);
        }

        return obj;
    }

}
