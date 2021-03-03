package com.example.trackfield.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Route;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.M;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Reader extends Helper {

    private static Reader instance;

    ////

    private Reader(Context context) {
        super(context);
        db = getReadableDatabase();
    }

    @NonNull
    public static Reader get(Context c) {
        if (instance == null || !instance.db.isOpen()) instance = new Reader(c);
        return instance;
    }

    @Deprecated
    @Nullable
    public static Reader get() {
        return instance;
    }

    // get exercises

    public Exercise getExercise(int _id) {
        String selection = Contract.ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);

        cursor.close();
        return exercises.size() > 0 ? exercises.get(0) : null;
    }

    public ArrayList<Exercise> getExercises() {
        String queryString = "SELECT * FROM " + Contract.ExerciseEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    public ArrayList<Exercise> getExercisesForMerge(LocalDateTime dateTime, int type) {
        String selection = "(" + Contract.ExerciseEntry.COLUMN_DATE + " = " + M.epoch(dateTime) + " OR " +
                Contract.ExerciseEntry.COLUMN_DATE + " = " + M.epoch(dateTime.truncatedTo(ChronoUnit.MINUTES)) + " OR " +
                Contract.ExerciseEntry.COLUMN_DATE + " = " + M.epoch(M.dateTime(dateTime.toLocalDate())) + ")" +
                " AND (" + Contract.ExerciseEntry.COLUMN_TYPE + " = " + type + (type == Exercise.TYPE_RUN ? " OR " + Contract.ExerciseEntry.COLUMN_TYPE + " = " + Exercise.TYPE_INTERVALS : "") + ")";

        Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, null, selection, null, null, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    @Nullable
    public Exercise getExercise(long externalId) {
        String selection = Contract.ExerciseEntry.COLUMN_EXTERNAL_ID + " = ?";
        String[] selectionArgs = { Long.toString(externalId) };

        Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        if (exercises.size() > 1) {
            Log.w("reader", "more than one exercise with externalId " + externalId);
        }

        return exercises.size() > 0 ? exercises.get(0) : null;
    }

    // get exerlites

    public Exerlite getExerlite(int _id) {
        String[] columns = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = Contract.ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        return exerlites.size() > 0 ? exerlites.get(0) : null;
    }

    public ArrayList<Exerlite> getExerlites(C.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {
        String selection = "";
        for (int i = 0; i < types.size(); i++) {
            if (i != 0) selection += " OR ";
            selection += Contract.ExerciseEntry.COLUMN_TYPE + " = " + types.get(i);
        }

        String[] columns = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        return exerlites;
    }

    public ArrayList<Exerlite> getExerlitesBySearch(String search, C.SortMode sortMode, boolean smallestFirst) {
        if (search.equals("")) {
            return getExerlites(sortMode, smallestFirst, Prefs.getExerciseVisibleTypes());
        }

        String[] columns = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = "(" +
                Contract.ExerciseEntry._ID + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_DATE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_DATA_SOURCE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_RECORDING_METHOD + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_NOTE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_TYPE + " LIKE" + "'%" + search + "%')" + selectionFilter(Prefs.getExerciseVisibleTypes());

        //String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE ?";
        //String[] selectionArgs = { "%" + filter + "%" };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        return exerlites;
    }

    @Deprecated
    public ArrayList<Exerlite> getExerlitesByRoute(String route, C.SortMode sortMode, boolean smallestFirst) {
        String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { route };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        D.sortExerlites(exerlites, sortMode, smallestFirst);

        cursor.close();
        return exerlites;
    }

    public ArrayList<Exerlite> getExerlitesByRoute(int routeId, C.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {
        String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + selectionFilter(types);
        //String[] selectionArgs = { Integer.toString(routeId) };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        D.sortExerlites(exerlites, sortMode, smallestFirst);

        cursor.close();
        return exerlites;
    }

    // TODO: raw
    public ArrayList<Exerlite> getExerlitesByDistance(int distance, C.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String filter = selectionFilter(types);
        String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        //String selection = Contract.ExerciseEntry.COLUMN_DISTANCE + (Prefs.includeLonger() ? " >= " + minDist : " BETWEEN " + minDist + " AND " + maxDist) + filter;
        String selection = Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE + " >= " + minDist + filter;
        //String drivenSelection = Contract.ExerciseEntry.COLUMN_DISTANCE + " = " + Exercise.DISTANCE_DRIVEN + filter;
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        // temp, behöver sortera igen
        if (!Prefs.includeLonger()) D.removeLonger(exerlites, maxDist);
        D.sortExerlites(exerlites, sortMode, smallestFirst);

        return exerlites;
    }

    public ArrayList<Exerlite> getExerlitesByInterval(String interval, C.SortMode sortMode, boolean smallestFirst) {
        String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { interval };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        D.sortExerlites(exerlites, sortMode, smallestFirst);

        return exerlites;
    }

    public ArrayList<Exerlite> getExerlitesByDate(LocalDateTime min, LocalDateTime max, C.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {
        String filter = selectionFilter(types);
        String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
        String selection = Contract.ExerciseEntry.COLUMN_DATE + " >= " + M.epoch(M.first(min, max)) + " AND " + Contract.ExerciseEntry.COLUMN_DATE + " <= " + M.epoch(M.last(min, max)) + filter;
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        cursor.close();

        return exerlites;
    }

    // get subs

    private ArrayList<Sub> getSubs(int _superId) {
        String selection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
        String[] selectionArgs = { Integer.toString(_superId) };

        Cursor cursor = db.query(Contract.SubEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Sub> subs = unpackSubCursor(cursor);
        cursor.close();

        return subs;
    }

    // get routes

    public ArrayList<Route> getRoutes(C.SortMode sortMode, boolean smallestFirst, boolean includeHidden) {
        String selection = includeHidden ? null : Contract.RouteEntry.COLUMN_HIDDEN + " = ?"; //+ 0 + " AND " + Contract.RouteEntry.COLUMN_AMOUNT + " >= " + 2;
        String[] selectionArgs = includeHidden ? null : new String[]{ Integer.toString(0) };
        String orderBy = null;//orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return routes;
    }

    public Route getRoute(String name) {
        String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return routes.size() > 0 ? routes.get(0) : null;
    }

    public Route getRoute(int _id) {
        String selection = Contract.RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return routes.size() > 0 ? routes.get(0) : new Route();
    }

    public String getRouteName(int _id) {
        String[] columns = { Contract.RouteEntry.COLUMN_NAME };
        String selection = Contract.RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String name = "";
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.RouteEntry.COLUMN_NAME));
        }
        cursor.close();

        return name;
    }

    public int getRouteId(String name) {
        String[] columns = { Contract.RouteEntry._ID };
        String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int _id = -1;
        while (cursor.moveToNext()) {
            _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
        }
        cursor.close();

        return _id;
    }

    public int getRouteIdOrCreate(String name, Context c) {
        /*String[] columns = { Contract.RouteEntry._ID };
        String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int _id = -1;
        while (cursor.moveToNext()) {
            _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
        }*/
        int _id = getRouteId(name);
        if (_id == -1) {
            _id = (int) Writer.get(c).addRouteIfNotAdded(new Route(-1, name), c);
        }

        return _id;
    }

    // get distances

    public ArrayList<Distance> getDistances(Distance.SortMode sortMode, boolean smallestFirst) {
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, null, null, null, null, null, orderBy);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return distances;
    }

    public Distance getDistance(int length) {
        String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString(length) };

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return distances.size() > 0 ? distances.get(0) : null;
    }

    public float getDistanceGoal(int distance) {
        String[] columns = { Contract.DistanceEntry.COLUMN_GOAL_PACE };
        String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = " + distance;

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, columns, selection, null, null, null, null);
        float goalPace = Distance.NO_GOAL_PACE;
        while (cursor.moveToNext()) {
            goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.DistanceEntry.COLUMN_GOAL_PACE));
        }
        cursor.close();

        return goalPace;
    }

    // get trail

    public HashMap<Integer, String> getPolylines(int exceptId) {
        HashMap<Integer, String> polys = new HashMap<>();
        ArrayList<String> polylines = new ArrayList<>();

        String[] columns = { Contract.ExerciseEntry._ID, Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry._ID + " != " + exceptId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
            polys.put(_id, polyline);
        }
        cursor.close();

        return polys;
    }

    public ArrayList<String> getPolylinesByRoute(int routeId) {
        ArrayList<String> polylines = new ArrayList<>();

        String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    public ArrayList<String> getPolylinesByRoute(int routeId, String routeVar) {
        ArrayList<String> polylines = new ArrayList<>();

        String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "' AND " +
                Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    public HashMap<Integer, String> getPolylinesByRouteExcept(int exceptRouteId) {
        HashMap<Integer, String> polylines = new HashMap<>();

        String[] columns = { Contract.ExerciseEntry._ID, Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " != " + exceptRouteId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(_id, polyline);
        }
        cursor.close();

        return polylines;
    }

    public Trail getTrail(int _id) {
        Trail trail = null;

        String[] columns = Contract.ExerciseEntry.TRAIL_COLUMNS;
        String selection = Contract.ExerciseEntry._ID + " = " + _id;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        while (cursor.moveToNext()) {
            double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_START_LAT));
            double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_START_LNG));
            double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_END_LAT));
            double endLng = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_END_LNG));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            trail = new Trail(polyline, new LatLng(startLat, startLng), new LatLng(endLat, endLng));
            break;
        }
        cursor.close();

        return trail;
    }

    // get route items

    @Deprecated
    public ArrayList<RouteItem> getRouteItems(ArrayList<String> rList) {
        ArrayList<RouteItem> routeItems = new ArrayList<>();
        for (String r : rList) {
            routeItems.add(getRouteItem(r));
        }

        return routeItems;
    }

    @Deprecated
    public RouteItem getRouteItem(String route) {
        String[] columns = { Contract.ExerciseEntry.COLUMN_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME, Contract.ExerciseEntry.COLUMN_ROUTE_VAR };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { route };
        String orderBy = orderBy(C.SortMode.PACE, true);

        int amount = 0;
        int totalDistanceCount = 0;
        int totalDistance = 0;
        float bestPace = -1;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
        while (cursor.moveToNext()) {
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            // driven distance
            if (distance == Exercise.DISTANCE_DRIVEN) {
                String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_VAR));
                distance = avgDistance(route, routeVar);
            }
            // bidra bara till avg om inte driven
            else {
                totalDistanceCount++;
                totalDistance += distance;
            }

            amount++;
            float pace = time / distance * 1000f;
            if (distance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) {
                bestPace = pace;
            }
        }
        cursor.close();

        int avgDistance = totalDistanceCount != 0 ? totalDistance / totalDistanceCount : 0;

        return new RouteItem(getRouteId(route), route, amount, avgDistance, bestPace);
    }

    // TODO: förenkla, effectiveDistance
    public ArrayList<RouteItem> getRouteItems(C.SortMode sortMode, boolean smallestFirst, boolean includeLesser, ArrayList<Integer> types) {
        final String e_t = Contract.ExerciseEntry.TABLE_NAME;
        final String r_t = Contract.RouteEntry.TABLE_NAME;
        final String e_dist = Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        final String e_time = Contract.ExerciseEntry.COLUMN_TIME;
        final String r_name = Contract.RouteEntry.COLUMN_NAME;
        final String e_rid = Contract.ExerciseEntry.COLUMN_ROUTE_ID;
        final String e_rvar = Contract.ExerciseEntry.COLUMN_ROUTE_VAR;
        final String e_date = Contract.ExerciseEntry.COLUMN_DATE;
        final String r_hidden = Contract.RouteEntry.COLUMN_HIDDEN;
        final String r_id = Contract.RouteEntry._ID;

        final String c_count = "antal";
        final String c_avg_dist = "avgDistance";
        final String c_min_pace = "minPace";

        String havingCount = includeLesser ? "" : " HAVING antal > 1";
        String whereHidden = includeLesser ? "" : " AND r." + r_hidden + " != 1";
        String whereTypes = selectionFilter(types);
        String whereTypesLone = selectionFilterFirst(types, "");
        String whereTypesAs = selectionFilterAs(types, "e2.");

        String orderBy;
        switch (sortMode) {
            case DATE:
                orderBy = "max(" + e_date + ")";
                break;
            case NAME:
                orderBy = "max(" + r_name + ")";
                break;
            case AMOUNT:
                orderBy = c_count;
                break;
            case DISTANCE:
                orderBy = c_avg_dist;
                break;
            case PACE:
                orderBy = c_min_pace;
                break;
            default:
                orderBy = "max(" + e_date + ")";
                break;
        }
        orderBy += sortOrder(smallestFirst);

        String queryString = "select e." + e_rid + ", r." + r_name + ", " + c_count + ", avg(e." + e_dist + ") as " + c_avg_dist + ", case when varPaceDrv is null or pace < varPaceDrv then pace else varPaceDrv end " + c_min_pace + " " +
                "from " + e_t + " as e inner join " + r_t + " as r on e." + e_rid + " = r." + r_id + " inner join " +
                "(select " + e_rid + ", count(1) as " + c_count + " from " + e_t + whereTypesLone + " group by " + e_rid + havingCount + ") as a on e." + e_rid + " = a." + e_rid + " inner join " +
                "(select " + e_rid + ", min(" + e_time + "/" + e_dist + ")*1000 as pace from " + e_t + " where " + e_dist + " > 0 and " + e_time + " != 0" + whereTypes + " group by " + e_rid + ") as v on e." + e_rid + " = v." + e_rid + " left outer join " +
                "(select e2." + e_rid + ", min(" + e_time + "/varDistAvg)*1000 as varPaceDrv from " + e_t + " as e2 inner join " +
                "(select " + e_rid + ", " + e_rvar + ", avg(" + e_dist + ") as varDistAvg from " + e_t + " where " + e_dist + " > 0 and " + e_time + " != 0 group by " + e_rvar + ", " + e_rid + ") as vAvg on e2." + e_rid + " = vAvg." + e_rid + " and e2." + e_rvar + " = vAvg." + e_rvar + " " +
                "where e2." + e_dist + " = -1 and e2." + e_time + " != 0 " + whereTypesAs + " group by e2." + e_rvar + ", e2." + e_rid + ") as vDrv on e." + e_rid + " = vDrv." + e_rid + " " +
                "where e." + e_dist + " != -1" + whereHidden + whereTypes + " group by e." + e_rid + " order by " + orderBy;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<RouteItem> routeItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(e_rid));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(r_name));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow(c_count));
            int avgDistance = cursor.getInt(cursor.getColumnIndexOrThrow(c_avg_dist));
            int minPace = cursor.getInt(cursor.getColumnIndexOrThrow(c_min_pace));

            routeItems.add(new RouteItem(routeId, name, count, avgDistance, minPace));
        }
        cursor.close();

        return routeItems;
    }

    // get distance items

    // TODO: raw
    public ArrayList<DistanceItem> getDistanceItems(Distance.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {
        /*String queryString = "SELECT d.distance, AVG(e.distance), pace FROM exercises AS e, distances as d," +
                " (SELECT routeId, COUNT(1) AS antal FROM exercises GROUP BY routeId HAVING COUNT(1) > 1) AS a," +
                " (SELECT routeId, MIN(time/distance)*1000 as pace FROM exercises WHERE distance > 0 AND time != 0 GROUP BY routeId) AS v" +
                " WHERE e.routeId = r._id AND e.routeId = a.routeId AND e.routeId = v.routeId AND e.distance != -1 GROUP BY e.routeId" +
                " ORDER BY " + orderBy;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<DistanceItem> distanceItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int distance = cursor.getInt(0);
            int minTime = cursor.getInt(1);
            int minPace = cursor.getInt(2);

            distanceItems.add(new DistanceItem(distance, minTime, minPace));
        }
        cursor.close();*/

        ArrayList<DistanceItem> distanceItems = new ArrayList<>();
        ArrayList<Distance> distances = getDistances(sortMode, smallestFirst);
        for (Distance distance : distances) {
            distanceItems.add(getDistanceItem(distance.getDistance(), types));
        }

        /*ArrayList<Integer> dList = D.sortDistances(D.distances, smallestFirst, sortMode);
        for (int d : dList) {
            distanceItems.add(getDistanceItem(d));
        }*/

        return distanceItems;
    }

    public DistanceItem getDistanceItem(int distance, ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String[] columns = { Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME, Contract.ExerciseEntry.COLUMN_ROUTE, Contract.ExerciseEntry.COLUMN_ROUTE_VAR };
        String selection = Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE + " >= " + minDist + selectionFilter(types);
        String orderBy = orderBy(C.SortMode.PACE, true);

        float bestPace = -1;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
        while (cursor.moveToNext()) {
            int fullDistance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            /*if (fullDistance == Exercise.DISTANCE_DRIVEN) {
                String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
                String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                fullDistance = avgDistance(route, routeVar);
                if (fullDistance < minDist) continue;
            }*/
            float pace = time / fullDistance * 1000f;
            if (fullDistance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) {
                bestPace = pace;
            }
        }
        cursor.close();

        float bestTimePerDistance = bestPace * distance / 1000;

        return new DistanceItem(distance, bestTimePerDistance, bestPace);
    }

    // get interval items

    public IntervalItem getIntervalItem(String interval) {
        String[] columns = {};
        String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { interval };
        String orderBy = orderBy(C.SortMode.PACE, true);

        int amount = 0;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
        while (cursor.moveToNext()) {
            amount++;
        }

        cursor.close();
        return new IntervalItem(interval, amount);
    }

    public ArrayList<IntervalItem> getIntervalItems(C.SortMode sortMode, boolean smallestFirst, boolean includeLesser) {
        String[] columns = { Contract.ExerciseEntry.COLUMN_INTERVAL };
        String orderBy = orderBy(sortMode, smallestFirst);

        HashMap<String, Integer> names = new HashMap<>();
        ArrayList<IntervalItem> intervalItems = new ArrayList<>();

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, null, null, null, null, orderBy);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_INTERVAL));
            if (!name.equals("")) {
                if (names.containsKey(name)) {
                    int count = names.get(name);
                    if (!includeLesser && count == 1) intervalItems.add(getIntervalItem(name));
                    names.put(name, count + 1);
                }
                else {
                    if (includeLesser) intervalItems.add(getIntervalItem(name));
                    names.put(name, 1);
                }
            }
        }
        cursor.close();

        return intervalItems;
    }

    // projections

    public int avgDistance(int routeId, String routeVar) {
        String queryString = "select avg(" + Contract.ExerciseEntry.COLUMN_DISTANCE + ")" +
                " from " + Contract.ExerciseEntry.TABLE_NAME +
                " where " + Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
                " and " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'" +
                " and " + Contract.ExerciseEntry.COLUMN_DISTANCE + " != " + Exercise.DISTANCE_DRIVEN +
                " and " + Contract.ExerciseEntry.COLUMN_DISTANCE + " != " + 0;

        Cursor cursor = db.rawQuery(queryString, null);
        int avgDistance = 0;
        while (cursor.moveToNext()) {
            avgDistance = cursor.getInt(0);
        }
        cursor.close();

        return avgDistance;
    }

    @Deprecated
    public int avgDistance(String route, String routeVar) {
        String[] colums = { Contract.ExerciseEntry.COLUMN_DISTANCE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ? AND " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = ?";
        String[] selectionArgs = { route, routeVar };

        Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs, null, null, null, null);
        int totalDistance = 0;
        int count = 0;
        while (cursor.moveToNext()) {
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            if (distance != Exercise.DISTANCE_DRIVEN && distance != 0) {
                totalDistance += distance;
                count++;
            }
        }
        cursor.close();

        return count != 0 ? totalDistance / count : 0;
    }

    @Deprecated
    private String routeValues(String route) {
        // amount, avg distance, fastest pace

        String[] columns = { Contract.ExerciseEntry.COLUMN_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { route };
        String orderBy = orderBy(C.SortMode.PACE, true);

        int amount = 0;
        int totalDistance = 0;
        float bestPace = -1;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
        while (cursor.moveToNext()) {
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            amount++;
            if (distance == Exercise.DISTANCE_DRIVEN) continue;
            totalDistance += distance;
            float pace = time / distance * 1000f;
            if (distance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) {
                bestPace = pace;
            }
        }
        cursor.close();

        int avgDistance = amount != 0 ? totalDistance / amount : 0;

        return amount + C.TAB + M.prefix(avgDistance, 1, "m") + C.TAB + M.stringTime(bestPace, true);
    }

    @Deprecated
    private String distanceValues(int distance) {
        // best time per distance, pace

        String[] columns = { Contract.ExerciseEntry.COLUMN_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME };
        String selection = Contract.ExerciseEntry.COLUMN_DISTANCE + " BETWEEN " + M.minDistance(distance) + " AND " + M.maxDistance(distance);
        String orderBy = orderBy(C.SortMode.PACE, true);

        float bestPace = -1;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
        while (cursor.moveToNext()) {
            int fullDistance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            float pace = time / fullDistance * 1000f;
            if (fullDistance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) {
                bestPace = pace;
            }
        }
        cursor.close();

        float bestTimePerDistance = bestPace * distance / 1000;

        return M.stringTime(bestTimePerDistance, true) + C.TAB + M.stringTime(bestPace, true);
    }

    // graph data

    public TreeMap<Float, Float> weekDailyDistance(ArrayList<Integer> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfWeek(includingDate), M.atEndOfWeek(includingDate), C.SortMode.DATE, false, types);

        for (Exerlite e : exerlites) {
            int key = e.getDate().getDayOfWeek().getValue();
            int value = dayAndDistance.containsKey(key) ? dayAndDistance.get(key) + e.getDistance() : e.getDistance();
            dayAndDistance.put(key, value);
        }

        for (int d = 1; d <= 7; d++) {
            points.put((float) d, dayAndDistance.containsKey(d) ? (float) dayAndDistance.get(d).intValue() : 0);
        }

        return points;
    }

    public TreeMap<Float, Float> yearMonthlyDistance(ArrayList<Integer> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> monthAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfYear(includingDate), M.atEndOfYear(includingDate), C.SortMode.DATE, false, types);

        for (Exerlite e : exerlites) {
            int key = e.getDate().getMonthValue();
            int value = monthAndDistance.containsKey(key) ? monthAndDistance.get(key) + e.getDistance() : e.getDistance();
            monthAndDistance.put(key, value);
        }

        for (int m = 1; m <= 12; m++) {
            points.put((float) m, monthAndDistance.containsKey(m) ? (float) monthAndDistance.get(m).intValue() : 0);
        }

        return points;
    }

    public TreeMap<Float, Float> yearMonthlyDistanceGoal() {
        TreeMap<Float, Float> points = new TreeMap<>();

        for (int m = 1; m <= 12; m++) {
            points.put((float) m, 100_000f);
        }

        return points;
    }

    public TreeMap<Float, Float> monthDailyIntegralDistance(ArrayList<Integer> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfMonth(includingDate), M.atEndOfMonth(includingDate), C.SortMode.DATE, false, types);

        float totalDistance = 0;

        for (Exerlite e : exerlites) {
            int key = e.getDate().getDayOfMonth();
            int value = dayAndDistance.containsKey(key) ? dayAndDistance.get(key) + e.getDistance() : e.getDistance();
            dayAndDistance.put(key, value);
        }
        if (dayAndDistance.size() == 0) return points;

        for (int d = 0; d <= includingDate.lengthOfMonth(); d++) {
            if (dayAndDistance.containsKey(d)) totalDistance += dayAndDistance.get(d);
            points.put((float) d, totalDistance);
            if (includingDate.isEqual(LocalDate.now()) && LocalDate.now().getDayOfMonth() == d)
                break;
        }

        return points;
    }

    public TreeMap<Float, Float> yearWeeklyIntegralDistance(ArrayList<Integer> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfYear(includingDate), M.atEndOfYear(includingDate), C.SortMode.DATE, true, types);

        float totalDistance = 0;

        /*for (Exerlite e : exerlites) {
            map.put(e.getDate().getDayOfYear(), e);
        }
        if (map.size() == 0) return points;

        for (int d = 0; d <= includingDate.lengthOfYear(); d++) {
            if (map.containsKey(d)) totalDistance += map.get(d).getDistance();
            points.put((float) d, totalDistance);
            if (d == map.lastKey() && includingDate.isEqual(LocalDate.now())) break;
        }*/

        float lastWeek = 0;
        for (Exerlite e : exerlites) {
            float week = (float) Math.ceil(e.getDate().getDayOfYear() / 7f);//e.getWeek();
            if (week != lastWeek) {
                points.put(lastWeek, totalDistance);
                lastWeek = week;
            }
            totalDistance += e.getDistance();
        }

        return points;
    }

    public TreeMap<Float, Float> monthIntegralDistanceGoal(LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();

        points.put(0f, 0f);
        points.put((float) includingDate.getMonth().length(includingDate.isLeapYear()), 100_000f);

        return points;
    }

    public TreeMap<Float, Float> yearIntegralDistanceGoal(LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();

        points.put(0f, 0f);
        points.put(53f/*(float) includingDate.lengthOfYear()*/, 1_200_000f);

        return points;
    }

    // unpack cursors

    private ArrayList<Exercise> unpackCursor(Cursor cursor) {
        ArrayList<Exercise> exercises = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            long externalId = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_EXTERNAL_ID));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TYPE));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_ID));
            //String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
            String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_VAR));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_INTERVAL));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_NOTE));
            String dataSource = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATA_SOURCE));
            String recordingMethod = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_RECORDING_METHOD));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            // convert trail
            Trail trail = null;
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            if (polyline != null) {
                double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_START_LAT));
                double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_START_LNG));
                double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_END_LAT));
                double endLng = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_END_LNG));
                trail = new Trail(polyline, new LatLng(startLat, startLng), new LatLng(endLat, endLng));
            }

            // convert
            LocalDateTime dateTime = M.ofEpoch(epoch);//LocalDateTime.parse(epoch, Toolbox.C.FORMATTER_SQL);
            if (interval == null) interval = "";
            //int routeId = getRouteId(route);
            Route route = getRoute(routeId);
            String routeName = route == null ? Route.NO_NAME : getRoute(routeId).getName();

            Exercise exercise = new Exercise(_id, externalId, type, dateTime, routeId, routeName, routeVar, interval, note, dataSource, recordingMethod, distance, time, getSubs(_id), trail);
            exercises.add(exercise);
        }

        return exercises;
    }

    private ArrayList<Exerlite> unpackLiteCursor(Cursor cursor) {
        ArrayList<Exerlite> exerlites = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_ID));
            //String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_INTERVAL));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
            int effectiveDistance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

            // convert
            LocalDate date = M.ofEpoch(epoch).toLocalDate();
            if (interval == null) interval = "";

            Route route = getRoute(routeId);
            String routeName = route == null ? "error" : getRoute(routeId).getName();

            // distance driven
            boolean distanceDriven = distance == Exercise.DISTANCE_DRIVEN;
            /*if (distanceDriven) {
                String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                distance = avgDistance(routeName, routeVar);
            }*/

            // TODO: subs
            /*if (effectiveDistance == 0 && time == 0) {
                String selection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
                String[] selectionArgs = { Integer.toString(_id) };
                String[] columns = { Contract.SubEntry.COLUMN_DISTANCE, Contract.SubEntry.COLUMN_TIME };

                Cursor subCursor = db.query(Contract.SubEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                while (subCursor.moveToNext()) {
                    int subDistance = subCursor.getInt(subCursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_DISTANCE));
                    float subTime = subCursor.getInt(subCursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_TIME));
                    effectiveDistance += subDistance;
                    time += subTime;
                }
                subCursor.close();
            }*/

            Exerlite exerlite = new Exerlite(_id, date, routeName, interval, effectiveDistance, time, distanceDriven);
            exerlites.add(exerlite);
        }

        return exerlites;
    }

    @Deprecated
    private ArrayList<Exerlite> unpackLiteDrivenCursor(Cursor cursor, int distance) {
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
        ArrayList<Exerlite> filtered = new ArrayList<>();
        for (Exerlite e : exerlites) {
            if (M.insideLimits(e.getDistance(), distance, true)) filtered.add(e);
        }

        return filtered;
    }

    private ArrayList<Sub> unpackSubCursor(Cursor cursor) {
        ArrayList<Sub> subs = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.SubEntry._ID));
            int superId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_SUPERID));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_DISTANCE));
            float time = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_TIME));

            Sub sub = new Sub(_id, superId, distance, time);
            subs.add(sub);
        }

        return subs;
    }

    private ArrayList<Distance> unpackDistanceCursor(Cursor cursor) {
        ArrayList<Distance> distances = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.DistanceEntry._ID));
            int length = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.DistanceEntry.COLUMN_DISTANCE));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.DistanceEntry.COLUMN_GOAL_PACE));

            Distance distance = new Distance(_id, length, goalPace);
            distances.add(distance);
        }

        return distances;
    }

    private ArrayList<Route> unpackRouteCursor(Cursor cursor) {
        ArrayList<Route> routes = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.RouteEntry.COLUMN_NAME));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.RouteEntry.COLUMN_GOAL_PACE));
            boolean hidden = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry.COLUMN_HIDDEN)) != 0;

            Route route = new Route(_id, name, goalPace, hidden);
            routes.add(route);
        }

        return routes;
    }

    // query tools

    private String selectionFilter(ArrayList<Integer> visibleTypes) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += " AND (";
            filter += Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) filter += ")";
            else filter += " OR ";
        }
        return filter;
    }

    private String selectionFilterAs(ArrayList<Integer> visibleTypes, String tableAsName) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += " AND (";
            filter += tableAsName + Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) filter += ")";
            else filter += " OR ";
        }
        return filter;
    }

    private String selectionFilterFirst(ArrayList<Integer> visibleTypes, String tableAsName) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += " WHERE (";
            filter += tableAsName + Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) filter += ")";
            else filter += " OR ";
        }
        return filter;
    }

    private String sortOrder(boolean smallestFirst) {
        return smallestFirst ? " ASC" : " DESC";
    }

    private String orderBy(C.SortMode sortMode, boolean smallestFirst) {
        return Contract.ExerciseEntry.getColumn(sortMode) + sortOrder(smallestFirst);
    }

    private String orderBy(Distance.SortMode sortMode, boolean smallestFirst) {
        return Contract.DistanceEntry.COLUMN_DISTANCE + sortOrder(smallestFirst);
    }

}
