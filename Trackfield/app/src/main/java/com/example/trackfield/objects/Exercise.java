package com.example.trackfield.objects;

import com.example.trackfield.database.Helper;
import com.example.trackfield.toolbox.Prefs;
import com.example.trackfield.toolbox.Toolbox.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class Exercise {

    private final int _id;
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

    public enum SortMode {
        DATE,
        DISTANCE,
        TIME,
        PACE,
        NAME
    }

    public static final String[] TYPES = { "Run", "Intervals", "Walk", "Track and field", "Ride", "Other" };
    public static final String[] TYPES_PLURAL = { "Runs", "Intervals", "Walks", "Track and field", "Rides", "Others" };
    public static final int TYPE_RUN = 0;
    public static final int TYPE_INTERVALS = 1;
    public static final int TYPE_WALK = 2;
    public static final int TYPE_TRACK = 3;
    public static final int TYPE_RIDE = 4;
    public static final int TYPE_OTHER = 5;

    private static final int DISTANCE_DECIMALS = 2;
    public static final int DISTANCE_DRIVEN = -1;

    ////

    public Exercise(int _id, int type, LocalDateTime dateTime, int routeId, String route, String routeVar, String interval, String note, String dataSource, String recordingMethod, int distance, float time, ArrayList<Sub> subs, Trail trail) {
        this._id = _id;
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
        this.subs = subs == null ? new ArrayList<Sub>() : subs;
        this.trail = trail;
    }

    // set
    public void setSubs_superId(int _id) {
        for (Sub sub : subs) {
            sub.set_superId(_id);
        }
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

    // get
    public int get_id() {
        return _id;
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
    public int getDistancePrimary() {
        return distance;
    }
    public float getTimePrimary() {
        return time;
    }
    public int getElevationGain() {
        return 0;
    }
    public int getElevationLoss() {
        return 0;
    }

    public ArrayList<Sub> getSubs() {
        return subs;
    }
    public Sub getSub(int index) {
        if (index >= subCount()) { return null; }
        return subs.get(index);
    }
    public Trail getTrail() {
        return trail;
    }

    // get driven
    public int distance() {
        if (isDistanceDriven()) return Helper.getReader().avgDistance(route, routeVar);//D.averageDistance(D.filterByRoute(route, routeVar));
        if (distance == 0 && time == 0) return getSubsDistance();
        return distance;
    }
    public float time() {
        if (time == 0 && distance == 0) getSubsTime();
        return time;
    }
    public float pace() {
        int distance = distance();
        if (distance == 0) { return 0; }
        return time() / ((float) distance / 1000f);
    }
    public float velocity(C.UnitVelocity unit) {
        int distance = distance();
        float time = time();
        if (time == 0) { return 0; }
        if (unit == C.UnitVelocity.METERS_PER_SECOND) {
            return ((float) distance / time);
        }
        else if (unit == C.UnitVelocity.KILOMETERS_PER_HOUR) {
            return ((float) distance)/1000 / (time/3600);
        }
        return -1;
    }
    public int energy(C.UnitEnergy unit) {
        // 0J, 1cal, 2Wh, 3eV

        int calories = (int) (Prefs.getMass() * distance());
        if (unit == C.UnitEnergy.CALORIES) { return calories; }

        int joules = (int) (calories * 4.184);
        if (unit == C.UnitEnergy.JOULES) { return joules; }

        // watthours
        if (unit == C.UnitEnergy.WATTHOURS) { return joules / 3600; }

        // electronvolts
        if (unit == C.UnitEnergy.ELECTRONVOLTS) { return (int) (joules / 1.602177); } // / Math.pow(10, -19)); }

        return -1;
    }
    public int power() {
        float time = time();
        if (time != 0) {
            return (int) (energy(C.UnitEnergy.JOULES) / time);
        }
        return 0;
    }
    public int timeByDistance(int d) {
        if (time() == 0) { return 0; }
        return (int) (d / velocity(C.UnitVelocity.METERS_PER_SECOND));
    }
    public boolean isDistanceDriven() {
        return distance == DISTANCE_DRIVEN;
    }
    public int getSubsDistance() {
        int distance = 0;
        for (Sub s : subs) { distance += s.getDistance(); }
        return distance;
    }
    public float getSubsTime() {
        float time = 0;
        for (Sub s : subs) { time += s.getTime(); }
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
        return M.epoch(dateTime);
    }

    public int getWeek() {
        return dateTime.get(C.WEEK_OF_YEAR);
    }

    // print
    public String printId() {
        return "#" + _id;
    }
    public String printType() {
        return TYPES[type];
    }
    public String printDistance(boolean unitlessKm) {
        int distance = distance();
        String print = distance == 0 ? C.NO_VALUE : unitlessKm ? M.round(distance / 1000f, DISTANCE_DECIMALS) + "" : M.prefix(distance, DISTANCE_DECIMALS, "m");
        //if (hasTrail()) print += " [map: " + M.round(trail.getDistance() / 1000f, DISTANCE_DECIMALS) + " km]";
        return isDistanceDriven() ? M.drive(print) : print;
    }
    public String printElevation() {
        int gain = getElevationGain();
        int loss = getElevationLoss();
        return gain == 0 && loss == 0 ? C.NO_VALUE : "+" + gain + " m, " + loss + " m";
    }
    public String printTime(boolean unit) {
        String timePrint = M.stringTime(time(), false);
        if (!unit || timePrint.equals(C.NO_VALUE_TIME)) { return timePrint; }
        return timePrint + " s";
    }
    public String printPace(boolean unit) {
        String pacePrint = M.stringTime(pace(), true);
        if (!unit || pacePrint.equals(C.NO_VALUE_TIME)) { return pacePrint; }
        return pacePrint + " s/km";
    }
    public String printVelocity(C.UnitVelocity unit, boolean showUnit) {

        String v = M.round(velocity(unit), 1) + "";
        if (showUnit) {
            if (unit == C.UnitVelocity.METERS_PER_SECOND) {
                return v + " m/s";
            }
            if (unit == C.UnitVelocity.KILOMETERS_PER_HOUR) {
                return v + " km/h";
            }
        }
        return v;
    }
    public String printTimeByDistance(int d, boolean unit) {
        String time = M.stringTime(timeByDistance(d), true);
        if (unit) { return time + " s"; }
        return time;
    }
    public String printEnergy() {
        int energy;
        if ((energy = energy(C.UnitEnergy.JOULES)) == 0) { return C.NO_VALUE; }
        return M.prefix(energy, 2, "J");
    }
    public String printPower() {
        int power;
        if ((power = power()) == 0) { return C.NO_VALUE; }
        return M.prefix(power, 2, "W");
    }
    public String extractToFile(char div) {
        return _id + "" + div + "" + type + "" + div + getEpoch() + div + route + div + routeVar + div +
                interval + div + distance + div + time + div + dataSource + div + recordingMethod + div + note + div +
                (trail != null ? (trail.getStartLat() + "" + div + "" + trail.getStartLng() + "" + div + "" + trail.getEndLat() + "" + div + "" + trail.getEndLng() + "" + div + trail.getPolyline())
                        : (div + "" + div + "" + div + "" + div));
    }

    // statics
    public static float calcPace(int distance, float time) {
        if (distance == 0) { return 0; }
        return time / ((float) distance / 1000f);
    }
    public static String printDistance(int distance, boolean unitlessKm, boolean distanceDriven) {
        String print;
        if (distance == 0) { print = C.NO_VALUE; }
        else {
            if (unitlessKm) { print = M.round(distance / 1000f, 1) + ""; }
            else { print = M.prefix(distance, 2, "m"); }
        }
        if (distanceDriven) { print = "( " + print + " )"; }
        return print;
    }
    public static String printTime(float time, String unit) {
        String timePrint = M.stringTime(time, false);
        if (unit.equals("") || timePrint.equals(C.NO_VALUE_TIME)) { return timePrint; }
        return timePrint + " " + unit;
    }
    public static int typeFromStravaType(String stravaType) {

        /*
        Ride
        Run
        Swim
        Walk
        Hike
        Alpine Ski
        Backcountry Ski
        Canoe
        Crossfit
        E-Bike Ride
        Elliptical
        Handcycle
        Ice Skate
        Inline Skate
        Kayak
        Kitesurf Session
        Nordic Ski
        Rock Climb
        Roller Ski
        Row
        Snowboard
        Snowshoe
        Stair Stepper
        Stand Up Paddle
        Surf
        Virtual Ride
        Virtual Run
        Weight Training
        Windsurf Session
        Wheelchair
        Workout
        Yoga
        */
        switch (stravaType) {
            case "Run": return TYPE_RUN;
            case "Walk":
            case "Hike": return TYPE_WALK;
            case "Ride": return TYPE_RIDE;
            case "Swim":
            case "Apline Ski":
            case "Backcountry Ski":
            case "Canoe":
            case "Crossfit":
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
            case "Yoga":
            default: return TYPE_OTHER;
        }
    }

}





















