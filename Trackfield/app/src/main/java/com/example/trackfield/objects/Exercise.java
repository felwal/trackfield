package com.example.trackfield.objects;

import com.example.trackfield.toolbox.Toolbox.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class Exercise {

    private final int _id;
    private int id;
    private int type;
    private LocalDate date;
    private int routeId = -1;
    private String route;
    private String routeVar;
    private String interval;
    private String note;
    private String dataSource;
    private String recordingMethod;

    private int distance;
    private float time;
    private ArrayList<Sub> subs;
    private Map map;

    public enum SortMode {
        DATE,
        DISTANCE,
        TIME,
        PACE,
        NAME
    }

    public static final String[] TYPES = { "Run", "Intervals", "Walk", "Track and field", "Ride" };
    public static final String[] TYPES_PLURAL = { "Runs", "Intervals", "Walks", "Track and field", "Rides" };
    public static final int TYPE_RUN = 0;
    public static final int TYPE_INTERVALS = 1;
    public static final int TYPE_WALK = 2;

    private static final int DISTANCE_DECIMALS = 2;
    public static final int DISTANCE_DRIVEN = -1;

    ////

    public Exercise(int _id, int id, int type, LocalDate date, int routeId, String route, String routeVar, String interval, String note, String dataSource, String recordingMethod, int distance, float time, ArrayList<Sub> subs, Map map) {
        this._id = _id;
        this.id = id;
        this.type = type;
        this.date = date;
        this.routeId = routeId;
        this.route = route;
        this.routeVar = routeVar;
        this.interval = interval;
        this.note = note;
        this.dataSource = dataSource;
        this.recordingMethod = recordingMethod;
        this.distance = distance;
        this.time = time;
        this.subs = subs;
        this.map = map == null ? new Map(_id) : map;
    }
    public Exercise(int _id, int type, LocalDate date, String dataSource, String recordingMethod, Map map) {
        this._id = _id;
        this.type = type;
        this.date = date;
        this.route = "Route";
        this.routeVar = "";
        this.interval = "";
        this.note = "";
        this.dataSource = dataSource;
        this.recordingMethod = recordingMethod;
        this.subs = new ArrayList<>();
        this.map = map;
    }

    // set
    public void setSubs_superId(int _id) {
        for (Sub sub : subs) {
            sub.set_superId(_id);
        }
    }
    public void setMap_superId(int _id) {
        if (map != null) map.set_superId(_id);
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setType(int type) {
        this.type = type;
    }
    public void setDate(LocalDate date) {
        this.date = date;
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
    public void setMap(Map map) {
        this.map = map;
    }
    public void removeMap() {
        map = new Map(_id);
    }

    // get
    public int get_id() {
        return _id;
    }
    public int getId() {
        return id;
    }
    public int getType() {
        return type;
    }
    public LocalDate getDate() {
        return date;
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
        return map.elevationGain();
    }
    public int getElevationLoss() {
        return map.elevationLoss();
    }

    public ArrayList<Sub> getSubs() {
        return subs;
    }
    public Sub getSub(int index) {
        if (index >= subCount()) { return null; }
        return subs.get(index);
    }
    public Map getMap() {
        return map;
    }

    // get driven
    public int distance() {
        if (isDistanceDriven()) return D.averageDistance(D.filterByRoute(route, routeVar));
        if (distance == 0 && time == 0) { return map.distance() == 0 ? getSubsDistance() : map.distance(); }
        return distance;
    }
    public float time() {
        if (time == 0 && distance == 0) { return map.time() == 0 ? getSubsTime() : map.time(); }
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

        int calories = (int) (D.mass * distance());
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
    public boolean hasMap() {
        return map.getCoordinates().size() > 0;
    }
    public boolean isType(int type) {
        return this.type == type;
    }

    public int getWeek() {
        return date.get(C.WEEK_OF_YEAR);
    }
    public int getYearAndWeek() {
        return M.mergeYearAndWeek(date.getYear(), getWeek());
    }

    // print
    public String printId() {
        return "#" + id + " _" + _id;
    }
    public String printType() {
        return TYPES[type];
    }
    public String printDistance(boolean unitlessKm) {
        int distance = distance();
        String print = distance == 0 ? C.NO_VALUE : unitlessKm ? M.round(distance / 1000f, DISTANCE_DECIMALS) + "" : M.prefix(distance, DISTANCE_DECIMALS, "m");
        return isDistanceDriven() ? M.drive(print) : print + (hasMap() ? "; " + map.distance3D() + " m" : "");
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
        return id + "" + div + "" + type + "" + div + date.format(C.FORMATTER_FILE) + div + route + div + routeVar + div +
                interval + div + distance + div + time + div + dataSource + div + recordingMethod + div + note;
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

}





















