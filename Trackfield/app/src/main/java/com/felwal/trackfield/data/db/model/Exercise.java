package com.felwal.trackfield.data.db.model;

import android.content.Context;

import androidx.annotation.Nullable;

import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.prefs.Prefs;
import com.felwal.trackfield.ui.map.model.Trail;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.DateUtils;
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.TypeUtils;
import com.felwal.trackfield.utils.annotation.Unfinished;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Exercise implements JSONObjectable {

    public static final int DISTANCE_DRIVEN = -1;
    public static final int NO_ID = -1;
    public static final int UNRELEVANT_ID = -2;

    // json keys
    private static final String JSON_ID = "_id";
    private static final String JSON_STRAVA_ID = "strava_id";
    private static final String JSON_GARMIN_ID = "garmin_id";
    private static final String JSON_TYPE = "type";
    private static final String JSON_LABEL = "label";
    private static final String JSON_EPOCH = "epoch";
    private static final String JSON_ROUTE_ID = "route_id";
    private static final String JSON_ROUTEVAR = "route_var";
    private static final String JSON_INTERVAL = "interval";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_EFFECTIVE_DISTANCE = "effective_distance";
    private static final String JSON_TIME = "time";
    private static final String JSON_DEVICE = "device";
    private static final String JSON_RECORDING_METHOD = "recording_method";
    private static final String JSON_NOTE = "note";
    private static final String JSON_START_LATLNG = "start_latlng";
    private static final String JSON_END_LATLNG = "end_latlng";
    private static final String JSON_POLYLINE = "polyline";
    private static final String JSON_HIDE_TRAIL = "trail_hidden";

    private static final int DISTANCE_DECIMALS = 2;

    private final int id;
    private long stravaId;
    private long garminId;
    private String type;
    private String label;
    private LocalDateTime dateTime;
    private int routeId;
    private String route;
    private String routeVar;
    private String interval;
    private String note;
    private String device;
    private String recordingMethod;
    private int distance;
    private float time;
    private Trail trail;
    private boolean trailHidden;

    //

    public Exercise(int id, long stravaId, long garminId, String type, String label, LocalDateTime dateTime,
        int routeId, String route, String routeVar, String interval, String note, String device, String recordingMethod,
        int distance, float time, @Nullable Trail trail, boolean trailHidden) {

        this.id = id;
        this.stravaId = stravaId;
        this.garminId = garminId == 0 ? NO_ID : garminId;
        this.type = type;
        this.label = label;
        this.dateTime = dateTime;
        this.routeId = routeId;
        this.route = route;
        this.routeVar = routeVar;
        this.interval = interval;
        this.note = note;
        this.device = device;
        this.recordingMethod = recordingMethod;
        this.distance = distance;
        this.time = time;
        this.trail = trail;
        this.trailHidden = trailHidden;
    }

    public Exercise(JSONObject obj, Context c) throws JSONException {
        id = obj.getInt(JSON_ID);
        stravaId = obj.getLong(JSON_STRAVA_ID);
        garminId = obj.getLong(JSON_GARMIN_ID);
        type = obj.getString(JSON_TYPE);
        label = obj.getString(JSON_LABEL);
        dateTime = DateUtils.ofEpochSecond(obj.getInt(JSON_EPOCH));
        routeId = obj.getInt(JSON_ROUTE_ID);
        route = DbReader.get(c).getRouteName(routeId);
        routeVar = obj.getString(JSON_ROUTEVAR);
        interval = obj.getString(JSON_INTERVAL);
        distance = obj.getInt(JSON_DISTANCE);
        time = (float) obj.getDouble(JSON_TIME);
        device = obj.getString(JSON_DEVICE);
        recordingMethod = obj.getString(JSON_RECORDING_METHOD);
        note = obj.getString(JSON_NOTE);
        trailHidden = obj.getBoolean(JSON_HIDE_TRAIL);

        // trail
        trail = null;
        if (obj.has(JSON_POLYLINE)) {
            String polyline = obj.getString(JSON_POLYLINE);

            // start & end
            if (obj.has(JSON_START_LATLNG) && obj.has(JSON_END_LATLNG)) {
                JSONArray startLatlng = obj.getJSONArray(JSON_START_LATLNG);
                JSONArray endLatlng = obj.getJSONArray(JSON_END_LATLNG);

                LatLng start = new LatLng(startLatlng.getDouble(0), startLatlng.getDouble(1));
                LatLng end = new LatLng(endLatlng.getDouble(0), endLatlng.getDouble(1));

                trail = new Trail(polyline, start, end);
            }
            else {
                trail = new Trail(polyline);
            }
        }
    }

    // set

    public void setStravaId(long stravaId) {
        this.stravaId = stravaId;
    }

    public void setGarminId(long garminId) {
        this.garminId = garminId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
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

    public void setDevice(String device) {
        this.device = device;
    }

    public void setRecordingMethod(String recordingMethod) {
        this.recordingMethod = recordingMethod;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void setTrail(Trail trail) {
        this.trail = trail;
    }

    public void setTrailHidden(boolean hidden) {
        trailHidden = hidden;
    }

    public void invertTrailHidden() {
        trailHidden = !trailHidden;
    }

    // get

    public int getId() {
        return id;
    }

    public long getStravaId() {
        return stravaId;
    }

    public long getGarminId() {
        return garminId;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
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

    public String getDevice() {
        return device;
    }

    public String getRecordingMethod() {
        return recordingMethod;
    }

    public int getDistance() {
        return distance;
    }

    public float getTime() {
        return time;
    }

    @Unfinished
    public int getElevationGain() {
        // TODO
        return 0;
    }

    @Unfinished
    public int getElevationLoss() {
        // TODO
        return 0;
    }

    public Trail getTrail() {
        return trail;
    }

    // get driven

    public boolean hasStravaId() {
        return stravaId != NO_ID && stravaId != UNRELEVANT_ID;
    }

    public boolean hasGarminId() {
        return garminId != NO_ID && garminId != UNRELEVANT_ID;
    }

    public boolean isType(String type) {
        return this.type.equals(type);
    }

    public long getEpoch() {
        return DateUtils.toEpochSecond(dateTime);
    }

    public int getWeek() {
        return dateTime.get(AppConsts.WEEK_OF_YEAR);
    }

    public int getEffectiveDistance(Context c) {
        if (isDistanceDriven()) return DbReader.get(c).getAvgDistance(routeId, routeVar);
        return distance;
    }

    public boolean isDistanceDriven() {
        return distance == DISTANCE_DRIVEN;
    }

    public float getPace(Context c) {
        int distance = getEffectiveDistance(c);

        if (distance == 0) return 0;
        return getTime() / ((float) distance / 1000f);
    }

    public float getVelocity(AppConsts.UnitVelocity unit, Context c) {
        int distance = getEffectiveDistance(c);
        float time = getTime();

        if (time == 0) return 0;
        if (unit == AppConsts.UnitVelocity.METERS_PER_SECOND) {
            return ((float) distance / time);
        }
        else if (unit == AppConsts.UnitVelocity.KILOMETERS_PER_HOUR) {
            return ((float) distance) / 1000 / (time / 3600);
        }
        return -1;
    }

    public int getEnergy(AppConsts.UnitEnergy unit, Context c) {
        int calories = (int) (Prefs.getMass() * getEffectiveDistance(c));
        if (unit == AppConsts.UnitEnergy.CALORIES) return calories;

        int joules = (int) (calories * 4.184);
        if (unit == AppConsts.UnitEnergy.JOULES) return joules;
        if (unit == AppConsts.UnitEnergy.WATTHOURS) return joules / 3600;
        if (unit == AppConsts.UnitEnergy.ELECTRONVOLTS) return (int) (joules / 1.602177);

        return -1;
    }

    public int getPower(Context c) {
        float time = getTime();

        if (time == 0) return 0;
        return (int) (getEnergy(AppConsts.UnitEnergy.JOULES, c) / time);
    }

    public boolean hasTrail() {
        return trail != null && trail.getPolyline() != null && trail.getLatLngs().size() != 0;
    }

    public boolean isTrailHidden() {
        return trailHidden;
    }

    // print

    public String printId() {
        return "#" + id;
    }

    public String printExternalId() {
        return stravaId == -1 ? "" : "@" + stravaId;
    }

    public String printDistance(boolean unitlessKm, Context c) {
        int distance = getEffectiveDistance(c);

        String print = distance == 0 ? AppConsts.NO_VALUE : unitlessKm
            ? MathUtils.round(distance / 1000f, DISTANCE_DECIMALS) + ""
            : MathUtils.prefix(distance, DISTANCE_DECIMALS, "m");

        return isDistanceDriven() ? TypeUtils.notateDriven(print) : print;
    }

    public String printTime(boolean showUnit) {
        String timePrint = MathUtils.stringTime(getTime(), false);

        if (!showUnit || timePrint.equals(AppConsts.NO_VALUE_TIME)) return timePrint;
        return timePrint + " s";
    }

    public String printPace(boolean showUnit, Context c) {
        String pacePrint = MathUtils.stringTime(getPace(c), true);

        if (!showUnit || pacePrint.equals(AppConsts.NO_VALUE_TIME)) return pacePrint;
        return pacePrint + " s/km";
    }

    public String printVelocity(AppConsts.UnitVelocity unit, boolean showUnit, Context c) {
        String velocityPrint = MathUtils.round(getVelocity(unit, c), 1) + "";

        if (showUnit) {
            if (unit == AppConsts.UnitVelocity.METERS_PER_SECOND) return velocityPrint + " m/s";
            if (unit == AppConsts.UnitVelocity.KILOMETERS_PER_HOUR) return velocityPrint + " km/h";
        }
        return velocityPrint;
    }

    public String printEnergy(Context c) {
        int energy = getEnergy(AppConsts.UnitEnergy.JOULES, c);

        if (energy == 0) return AppConsts.NO_VALUE;
        return MathUtils.prefix(energy, 2, "J");
    }

    public String printPower(Context c) {
        int power = getPower(c);

        if (power == 0) return AppConsts.NO_VALUE;
        return MathUtils.prefix(power, 2, "W");
    }

    @Unfinished
    public String printElevation() {
        int gain = getElevationGain();
        int loss = getElevationLoss();

        return gain == 0 && loss == 0 ? AppConsts.NO_VALUE : "+" + gain + " m, " + loss + " m";
    }

    // implements JSONObjectable

    @Override
    public JSONObject toJSONObject(Context c) {
        JSONObject obj = new JSONObject();

        try {
            obj.put(JSON_ID, id);
            obj.put(JSON_STRAVA_ID, stravaId);
            obj.put(JSON_GARMIN_ID, garminId);
            obj.put(JSON_TYPE, getType());
            obj.put(JSON_LABEL, getLabel());
            obj.put(JSON_EPOCH, getEpoch());
            obj.put(JSON_ROUTE_ID, routeId);
            obj.put(JSON_ROUTEVAR, routeVar);
            obj.put(JSON_INTERVAL, interval);
            obj.put(JSON_DISTANCE, distance);
            obj.put(JSON_EFFECTIVE_DISTANCE, getEffectiveDistance(c));
            obj.put(JSON_TIME, time);
            obj.put(JSON_DEVICE, device);
            obj.put(JSON_RECORDING_METHOD, recordingMethod);
            obj.put(JSON_NOTE, note);
            obj.put(JSON_HIDE_TRAIL, trailHidden);

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

}





















