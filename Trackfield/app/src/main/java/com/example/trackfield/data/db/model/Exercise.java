package com.example.trackfield.data.db.model;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.ui.map.model.Trail;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.DateUtils;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.data.prefs.Prefs;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Exercise implements JSONObjectable {

    private final int _id;
    private long externalId;
    private int type;
    private LocalDateTime dateTime;
    private int routeId;
    private String route;
    private String routeVar;
    private String interval;
    private String note;
    private String dataSource;
    private String recordingMethod;

    private int distance;
    private float time;
    private ArrayList<Sub> subs;
    private Trail trail;

    public enum SortMode { DATE, DISTANCE, TIME, PACE, NAME }

    public static final String[] TYPES = { "Run", "Intervals", "Walk", "Track and field", "Ride", "Other", "Strength",
        "Dance", "Yoga" };
    public static final String[] TYPES_PLURAL = { "Runs", "Intervals", "Walks", "Track and field", "Rides", "Other",
        "Strength", "Dances", "Yoga" };
    public static final int TYPE_RUN = 0;
    public static final int TYPE_INTERVALS = 1;
    public static final int TYPE_WALK = 2;
    public static final int TYPE_TRACK = 3;
    public static final int TYPE_RIDE = 4;
    public static final int TYPE_OTHER = 5;
    public static final int TYPE_STRENGTH = 6;
    public static final int TYPE_DANCE = 7;
    public static final int TYPE_YOGA = 8;

    private static final int DISTANCE_DECIMALS = 2;
    public static final int DISTANCE_DRIVEN = -1;
    public static final int NO_ID = -1;

    // json keys
    private static final String JSON_ID = "id";
    private static final String JSON_EXTERNAL_ID = "external_id";
    private static final String JSON_TYPE = "type";
    private static final String JSON_EPOCH = "epoch";
    private static final String JSON_ROUTE_ID = "route_id";
    private static final String JSON_ROUTE = "route";
    private static final String JSON_ROUTEVAR = "route_var";
    private static final String JSON_INTERVAL = "interval";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_EFFECTIVE_DISTANCE = "effective_distance";
    private static final String JSON_TIME = "time";
    private static final String JSON_DATA_SOURCE = "data_source";
    private static final String JSON_RECORDING_METHOD = "recording_method";
    private static final String JSON_NOTE = "note";
    private static final String JSON_START_LATLNG = "start_latlng";
    private static final String JSON_END_LATLNG = "end_latlng";
    private static final String JSON_POLYLINE = "polyline";

    //

    public Exercise(int _id, long externalId, int type, LocalDateTime dateTime, int routeId, String route,
        String routeVar, String interval, String note, String dataSource, String recordingMethod, int distance,
        float time, @Nullable ArrayList<Sub> subs, @Nullable Trail trail) {
        this._id = _id;
        this.externalId = externalId;
        this.type = type;
        this.dateTime = dateTime;
        this.routeId = routeId;
        this.route = route;
        this.routeVar = routeVar;
        this.interval = interval;
        this.note = note;
        this.dataSource = dataSource;
        this.recordingMethod = recordingMethod;
        this.distance = distance;
        this.time = time;
        this.subs = subs == null ? new ArrayList<>() : subs;
        this.trail = trail;
    }

    public Exercise(JSONObject obj, Context c) throws JSONException {
        _id = obj.getInt(JSON_ID);
        externalId = obj.getLong(JSON_EXTERNAL_ID);
        type = obj.getInt(JSON_TYPE);
        dateTime = DateUtils.ofEpochSecond(obj.getInt(JSON_EPOCH));
        routeId = obj.getInt(JSON_ROUTE_ID);//Reader.get(c).getRouteId(route);
        route = DbReader.get(c).getRouteName(routeId);//obj.getString(JSON_ROUTE);
        routeVar = obj.getString(JSON_ROUTEVAR);
        interval = obj.getString(JSON_INTERVAL);
        distance = obj.getInt(JSON_DISTANCE);
        time = (float) obj.getDouble(JSON_TIME);
        dataSource = obj.getString(JSON_DATA_SOURCE);
        recordingMethod = obj.getString(JSON_RECORDING_METHOD);
        note = obj.getString(JSON_NOTE);

        // trail
        trail = null;
        if (obj.has(JSON_POLYLINE)) {
            String polyline = obj.getString(JSON_POLYLINE);

            // start & end
            if (obj.has(JSON_START_LATLNG) && obj.has(JSON_END_LATLNG)) {
                JSONArray start_latlng = obj.getJSONArray(JSON_START_LATLNG);
                JSONArray end_latlng = obj.getJSONArray(JSON_END_LATLNG);

                LatLng start = new LatLng(start_latlng.getDouble(0), start_latlng.getDouble(1));
                LatLng end = new LatLng(end_latlng.getDouble(0), end_latlng.getDouble(1));

                trail = new Trail(polyline, start, end);
            }
            else {
                trail = new Trail(polyline);
            }
        }

        // TODO
        subs = new ArrayList<>(); //
    }

    // set

    public void setExternalId(long externalId) {
        this.externalId = externalId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        //Data.calcWeekStats();
    }

    public void setRouteId(int _id) {
        this.routeId = _id;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setRouteVar(String routeVar) {
        this.routeVar = routeVar;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setRecordingMethod(String recordingMethod) {
        this.recordingMethod = recordingMethod;
    }

    public void setSubs_superId(int _id) {
        for (Sub sub : subs) {
            sub.set_superId(_id);
        }
    }

    public boolean mergeStravaPull(Exercise strava, Context c) {
        if (externalId != strava.externalId) return false;

        // update policy
        route = strava.route;
        routeId = DbReader.get(c).getRouteIdOrCreate(route, c);
        type = strava.type;
        dateTime = strava.dateTime;
        dataSource = strava.dataSource;
        //recordingMethod = strava.recordingMethod;
        distance = strava.distance;
        time = strava.time;
        if (note.equals("")) note = strava.note;
        trail = strava.trail;

        return true;
    }

    // get

    public int get_id() {
        return _id;
    }

    public long getExternalId() {
        return externalId;
    }

    public int getType() {
        return type;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getRouteId() {
        return routeId;
    }

    public String getRoute() {
        return route;
    }

    public String getRouteVar() {
        return routeVar;
    }

    public String getInterval() {
        return interval;
    }

    public String getNote() {
        return note;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getRecordingMethod() {
        return recordingMethod;
    }

    public int getDistance() {
        return distance;
    }

    public float getTimePrimary() {
        return time;
    }

    // TODO
    public int getElevationGain() {
        return 0;
    }

    // TODO
    public int getElevationLoss() {
        return 0;
    }

    public ArrayList<Sub> getSubs() {
        return subs;
    }

    public Sub getSub(int index) {
        if (index >= subCount()) {
            return null;
        }
        return subs.get(index);
    }

    public Trail getTrail() {
        return trail;
    }

    // get driven

    public boolean hasExternalId() {
        return externalId != -1;
    }

    public int getEffectiveDistance() {
        if (isDistanceDriven()) {
            return DbReader.get().avgDistance(routeId, routeVar);//D.averageDistance(D.filterByRoute(route, routeVar));
        }
        //if (distance == 0 && time == 0) return getSubsDistance();
        return distance;
    }

    public float getTime() {
        if (time == 0 && distance == 0) return getSubsTime();
        return time;
    }

    public float getPace() {
        int distance = getEffectiveDistance();
        if (distance == 0) {
            return 0;
        }
        return getTime() / ((float) distance / 1000f);
    }

    public float getVelocity(AppConsts.UnitVelocity unit) {
        int distance = getEffectiveDistance();
        float time = getTime();
        if (time == 0) {
            return 0;
        }
        if (unit == AppConsts.UnitVelocity.METERS_PER_SECOND) {
            return ((float) distance / time);
        }
        else if (unit == AppConsts.UnitVelocity.KILOMETERS_PER_HOUR) {
            return ((float) distance) / 1000 / (time / 3600);
        }
        return -1;
    }

    public int getEnergy(AppConsts.UnitEnergy unit) {
        // 0J, 1cal, 2Wh, 3eV

        int calories = (int) (Prefs.getMass() * getEffectiveDistance());
        if (unit == AppConsts.UnitEnergy.CALORIES) {
            return calories;
        }

        int joules = (int) (calories * 4.184);
        if (unit == AppConsts.UnitEnergy.JOULES) {
            return joules;
        }

        // watthours
        if (unit == AppConsts.UnitEnergy.WATTHOURS) {
            return joules / 3600;
        }

        // electronvolts
        if (unit == AppConsts.UnitEnergy.ELECTRONVOLTS) {
            return (int) (joules / 1.602177);
        } // / Math.pow(10, -19)); }

        return -1;
    }

    public int getPower() {
        float time = getTime();
        if (time != 0) {
            return (int) (getEnergy(AppConsts.UnitEnergy.JOULES) / time);
        }
        return 0;
    }

    public int getTimeByDistance(int d) {
        if (getTime() == 0) {
            return 0;
        }
        return (int) (d / getVelocity(AppConsts.UnitVelocity.METERS_PER_SECOND));
    }

    public boolean isDistanceDriven() {
        return distance == DISTANCE_DRIVEN;
    }

    public int getSubsDistance() {
        int distance = 0;
        for (Sub s : subs) {
            distance += s.getDistance();
        }
        return distance;
    }

    public float getSubsTime() {
        float time = 0;
        for (Sub s : subs) {
            time += s.getTime();
        }
        return time;
    }

    public int subCount() {
        return subs != null ? subs.size() : 0;
    }

    public boolean hasTrail() {
        return trail != null && trail.getPolyline() != null && trail.getLatLngs().size() != 0;
    }

    public boolean isType(int type) {
        return this.type == type;
    }

    public long getEpoch() {
        return DateUtils.toEpochSecond(dateTime);
    }

    public int getWeek() {
        return dateTime.get(AppConsts.WEEK_OF_YEAR);
    }

    // print

    public String printId() {
        return "#" + _id;
    }

    public String printExternalId() {
        return externalId == -1 ? "" : "@" + externalId;
    }

    public String printType() {
        return TYPES[type];
    }

    public String printDistance(boolean unitlessKm) {
        int distance = getEffectiveDistance();
        String print = distance == 0 ? AppConsts.NO_VALUE :
            unitlessKm ? MathUtils.round(distance / 1000f, DISTANCE_DECIMALS) + "" : MathUtils.prefix(distance, DISTANCE_DECIMALS, "m");
        //if (hasTrail()) print += " [map: " + M.round(trail.getDistance() / 1000f, DISTANCE_DECIMALS) + " km]";
        return isDistanceDriven() ? MathUtils.notateDriven(print) : print;
    }

    public String printElevation() {
        int gain = getElevationGain();
        int loss = getElevationLoss();
        return gain == 0 && loss == 0 ? AppConsts.NO_VALUE : "+" + gain + " m, " + loss + " m";
    }

    public String printTime(boolean unit) {
        String timePrint = MathUtils.stringTime(getTime(), false);
        if (!unit || timePrint.equals(AppConsts.NO_VALUE_TIME)) {
            return timePrint;
        }
        return timePrint + " s";
    }

    public String printPace(boolean unit) {
        String pacePrint = MathUtils.stringTime(getPace(), true);
        if (!unit || pacePrint.equals(AppConsts.NO_VALUE_TIME)) {
            return pacePrint;
        }
        return pacePrint + " s/km";
    }

    public String printVelocity(AppConsts.UnitVelocity unit, boolean showUnit) {

        String v = MathUtils.round(getVelocity(unit), 1) + "";
        if (showUnit) {
            if (unit == AppConsts.UnitVelocity.METERS_PER_SECOND) {
                return v + " m/s";
            }
            if (unit == AppConsts.UnitVelocity.KILOMETERS_PER_HOUR) {
                return v + " km/h";
            }
        }
        return v;
    }

    public String printTimeByDistance(int d, boolean unit) {
        String time = MathUtils.stringTime(getTimeByDistance(d), true);
        if (unit) {
            return time + " s";
        }
        return time;
    }

    public String printEnergy() {
        int energy;
        if ((energy = getEnergy(AppConsts.UnitEnergy.JOULES)) == 0) {
            return AppConsts.NO_VALUE;
        }
        return MathUtils.prefix(energy, 2, "J");
    }

    public String printPower() {
        int power;
        if ((power = getPower()) == 0) {
            return AppConsts.NO_VALUE;
        }
        return MathUtils.prefix(power, 2, "W");
    }

    public String extractToFile(char div) {
        return _id + "" + div + "" + type + "" + div + getEpoch() + div + route + div + routeVar + div + interval +
            div + distance + div + time + div + dataSource + div + recordingMethod + div + note + div + (trail != null ?
            (trail.getStartLat() + "" + div + "" + trail.getStartLng() + "" + div + "" + trail.getEndLat() + "" + div +
                "" + trail.getEndLng() + "" + div + trail.getPolyline()) : (div + "" + div + "" + div + "" + div));
    }

    // extends

    @Override
    public JSONObject toJSONObject(Context c) {

        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, _id);
            obj.put(JSON_EXTERNAL_ID, externalId);
            obj.put(JSON_TYPE, type);
            obj.put(JSON_EPOCH, getEpoch());
            obj.put(JSON_ROUTE_ID, routeId);
            //obj.put(JSON_ROUTE, route);
            obj.put(JSON_ROUTEVAR, routeVar);
            obj.put(JSON_INTERVAL, interval);
            obj.put(JSON_DISTANCE, distance);
            obj.put(JSON_EFFECTIVE_DISTANCE, getEffectiveDistance());
            obj.put(JSON_TIME, time);
            obj.put(JSON_DATA_SOURCE, dataSource);
            obj.put(JSON_RECORDING_METHOD, recordingMethod);
            obj.put(JSON_NOTE, note);

            if (hasTrail()) {
                JSONArray start_latlng = new JSONArray();
                JSONArray end_latlng = new JSONArray();

                start_latlng.put(trail.getStartLat());
                start_latlng.put(trail.getStartLng());
                end_latlng.put(trail.getEndLat());
                end_latlng.put(trail.getEndLng());

                obj.put(JSON_START_LATLNG, start_latlng);
                obj.put(JSON_END_LATLNG, end_latlng);
                obj.put(JSON_POLYLINE, trail.getPolyline());
            }
        }
        catch (JSONException e) {
            LayoutUtils.handleError(e, c);
        }

        return obj;
    }

    // static tools

    public static float calcPace(int distance, float time) {
        if (distance == 0) {
            return 0;
        }
        return time / ((float) distance / 1000f);
    }

    public static String printDistance(int distance, boolean unitlessKm, boolean distanceDriven) {
        String print;
        if (distance == 0) {
            print = AppConsts.NO_VALUE;
        }
        else {
            if (unitlessKm) {
                print = MathUtils.round(distance / 1000f, 1) + "";
            }
            else {
                print = MathUtils.prefix(distance, 2, "m");
            }
        }
        if (distanceDriven) {
            print = "( " + print + " )";
        }
        return print;
    }

    public static String printTime(float time, String unit) {
        String timePrint = MathUtils.stringTime(time, false);
        if (unit.equals("") || timePrint.equals(AppConsts.NO_VALUE_TIME)) {
            return timePrint;
        }
        return timePrint + " " + unit;
    }

    public static int typeFromStravaType(String stravaType) {
        switch (stravaType) {
            case "Run": return TYPE_RUN;
            case "Walk":
            case "Hike": return TYPE_WALK;
            case "Ride": return TYPE_RIDE;
            case "Swim":
            case "Apline Ski":
            case "Backcountry Ski":
            case "Canoe":
            case "Crossfit": return TYPE_STRENGTH;
            case "E-Bike Ride":
            case "Elliptical":
            case "Handcycle":
            case "Ice Skate":
            case "Inline Skate":
            case "Kayak":
            case "Kitesurf Session":
            case "Nordic Ski":
            case "Row":
            case "Snowboard":
            case "Snowshoe":
            case "Stair Stepper":
            case "Stand Up Paddle":
            case "Surf":
            case "Virtual Ride":
            case "Virtual Run":
            case "Weight Training":
            case "Windsurf Session":
            case "Wheelchair":
            case "Workout":
            case "Yoga": return TYPE_YOGA;
            default: return TYPE_OTHER;
        }
    }

}




















