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

    @Nullable
    public Exercise getExercise(int _id) {
        String selection = Contract.ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return getFirst(exercises);
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

        return getFirst(exercises);
    }

    @NonNull
    public ArrayList<Exercise> getExercises() {
        String queryString = "SELECT * FROM " + Contract.ExerciseEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    @NonNull
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

    // get exerlites

    @Nullable
    public Exerlite getExerlite(int _id) {
        String[] columns = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = Contract.ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return getFirst(exerlites);
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(C.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types, int startIndex, int endIndex) {
        String[] columns = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = selectionTypeFilter("", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy, Integer.toString(endIndex));
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(C.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types) {
        String[] columns = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = selectionTypeFilter("", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesBySearch(String search, C.SortMode sortMode, boolean smallestFirst) {
        if (search.equals("")) {
            return getExerlites(sortMode, smallestFirst, Prefs.getExerciseVisibleTypes());
        }

        String[] columns = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = "(" +
                Contract.ExerciseEntry._ID + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_DATE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_DATA_SOURCE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_RECORDING_METHOD + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_NOTE + " LIKE" + "'%" + search + "%' OR " +
                Contract.ExerciseEntry.COLUMN_TYPE + " LIKE" + "'%" + search + "%')" +
                selectionTypeFilter(" AND", Prefs.getExerciseVisibleTypes());

        //String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE ?";
        //String[] selectionArgs = { "%" + filter + "%" };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByRoute(int routeId, C.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types) {
        String[] colums = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + selectionTypeFilter(" AND", types);
        //String[] selectionArgs = { Integer.toString(routeId) };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    /**
     * Gets all exerlites of specified types in range of distance.
     * Also included are any longer exerlites which make it into top 3 by pace.
     * Marks top 3.
     *
     * @param distance The length to consider exerlites in regards to
     * @param sortMode Mode to sort by
     * @param smallestFirst Ordering by value
     * @param types Types to filter in
     * @return List of filtered exerlites
     *
     * @see M#minDistance(int)
     * @see M#maxDistance(int)
     * @see Exerlite#setTop(int)
     */
    @NonNull
    public ArrayList<Exerlite> getExerlitesByDistance(int distance, C.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String exerliteColumns = Contract.ExerciseEntry.toString(Contract.ExerciseEntry.COLUMNS_EXERLITE);
        String orderByPace = orderBy(C.SortMode.PACE, true);
        String table = Contract.ExerciseEntry.TABLE_NAME;
        String id = Contract.ExerciseEntry._ID;
        String dist = Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String andTypeFilter = selectionTypeFilter(" AND", types);

        // sqlite 3.25, säkert effektivare
        /*String queryString3p25 = "SELECT row_number over (ORDER BY " + orderByPace + ") AS rownum, " + exerliteColumns +
                " FROM " + table +
                " WHERE " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " AND " + dist + " <= " + maxDist + ")" +
                " OR " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " ORDER BY " + orderByPace + " LIMIT 3)" +
                " ORDER BY " + orderBy(sortMode, smallestFirst);*/

        String queryString = "SELECT " + exerliteColumns +
                " FROM " + table +
                " WHERE (" + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist +
                    " AND " + dist + " <= " + maxDist + ") " + andTypeFilter +
                " OR " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist +
                    andTypeFilter + " ORDER BY " + orderByPace + " LIMIT 3))" +
                " ORDER BY " + orderBy(sortMode, smallestFirst);

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;

        /*String selection = Contract.ExerciseEntry.COLUMN_DISTANCE + (Prefs.includeLonger() ? " >= " + minDist : " BETWEEN " + minDist + " AND " + maxDist) + filter;
        // temp, behöver sortera igen
        if (!Prefs.includeLonger()) D.removeLonger(top3, maxDist);
        D.sortExerlites(top3, sortMode, smallestFirst);*/
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByInterval(String interval, C.SortMode sortMode, boolean smallestFirst) {
        String[] colums = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { interval };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        D.sortExerlites(exerlites, sortMode, smallestFirst);

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByDate(LocalDateTime min, LocalDateTime max, C.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types) {
        String[] colums = Contract.ExerciseEntry.COLUMNS_EXERLITE;
        String selection = Contract.ExerciseEntry.COLUMN_DATE + " >= " + M.epoch(M.first(min, max)) +
                " AND " + Contract.ExerciseEntry.COLUMN_DATE + " <= " + M.epoch(M.last(min, max)) +
                selectionTypeFilter(" AND", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    // get subs

    @NonNull
    private ArrayList<Sub> getSubs(int _superId) {
        String selection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
        String[] selectionArgs = { Integer.toString(_superId) };

        Cursor cursor = db.query(Contract.SubEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Sub> subs = unpackSubCursor(cursor);
        cursor.close();

        return subs;
    }

    // get routes

    @NonNull
    public ArrayList<Route> getRoutes(boolean includeHidden) {
        String selection = includeHidden ? null : Contract.RouteEntry.COLUMN_HIDDEN + " = ?"; //+ 0 + " AND " + Contract.RouteEntry.COLUMN_AMOUNT + " >= " + 2;
        String[] selectionArgs = includeHidden ? null : new String[] { Integer.toString(0) };
        String orderBy = null;//orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return routes;
    }

    /**
     * Gets the route by corresponding name.
     *
     * @param name Name of route
     * @return The route of the existing routeName, or null if not existing
     */
    @Nullable
    public Route getRoute(String name) {
        String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return getFirst(routes);
    }

    /**
     * Gets the route by corresponding routeId.
     *
     * @param routeId Id of route
     * @return The route of the existing routeId, or {@link Route#Route()} if not existing
     */
    // TODO: nullable?
    @NonNull
    public Route getRoute(int routeId) {
        String selection = Contract.RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Route> routes = unpackRouteCursor(cursor);
        cursor.close();

        return routes.size() > 0 ? routes.get(0) : new Route();
    }

    /**
     * Gets the routeName by corresponding routeId.
     *
     * @param routeId Id of route
     * @return The routeName of the existing route, or {@link Route#NO_NAME} if not existing
     */
    @NonNull
    public String getRouteName(int routeId) {
        String[] columns = { Contract.RouteEntry.COLUMN_NAME };
        String selection = Contract.RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String name = Route.NO_NAME;
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.RouteEntry.COLUMN_NAME));
        }
        cursor.close();

        return name;
    }

    /**
     * Gets the routeId by corresponding routeName.
     *
     * @param name Name of route
     * @return The routeId of the existing route, or {@link Route#ID_NON_EXISTANT} if not existing
     * @see #getRouteIdOrCreate(String, Context)
     */
    public int getRouteId(String name) {
        String[] columns = { Contract.RouteEntry._ID };
        String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
        int _id = Route.ID_NON_EXISTANT;
        while (cursor.moveToNext()) {
            _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
        }
        cursor.close();

        return _id;
    }

    /**
     * Gets the routeId by corresponding routeName.
     * Internally calls {@link #getRouteId(String)} to query routeId,
     * and {@link Writer#addRoute(Route, Context)} if not existing.
     *
     * @param name Name of route
     * @param c Context, used to get {@link Writer} instance
     * @return The routeId of the existing or created route
     */
    public int getRouteIdOrCreate(String name, Context c) {
        int routeId = getRouteId(name);
        if (routeId == Route.ID_NON_EXISTANT) {
            routeId = (int) Writer.get(c).addRoute(new Route(name), c);
        }
        return routeId;
    }

    // get distances

    @NonNull
    public ArrayList<Distance> getDistances() {
        String orderBy = Contract.DistanceEntry.COLUMN_DISTANCE + sortOrder(true);

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, null, null, null,
                null, null, orderBy);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return distances;
    }

    @Nullable
    public Distance getDistance(int length) {
        String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString(length) };

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return getFirst(distances);
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

    @NonNull
    public HashMap<Integer, String> getPolylines(int exceptId) {
        String[] columns = { Contract.ExerciseEntry._ID, Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry._ID + " != " + exceptId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(_id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public ArrayList<String> getPolylinesByRoute(int routeId) {
        String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        ArrayList<String> polylines = new ArrayList<>();
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public ArrayList<String> getPolylinesByRoute(int routeId, String routeVar) {
        String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "' AND " +
                Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        ArrayList<String> polylines = new ArrayList<>();
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public HashMap<Integer, String> getPolylinesByRouteExcept(int exceptRouteId) {
        String[] columns = { Contract.ExerciseEntry._ID, Contract.ExerciseEntry.COLUMN_POLYLINE };
        String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " != " + exceptRouteId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(_id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @Nullable
    public Trail getTrail(int _id) {
        String[] columns = Contract.ExerciseEntry.COLUMNS_TRAIL;
        String selection = Contract.ExerciseEntry._ID + " = " + _id;

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        Trail trail = null;
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

    // get items

    // TODO: förenkla, effectiveDistance
    @NonNull
    public ArrayList<RouteItem> getRouteItems(C.SortMode sortMode, boolean smallestFirst, boolean includeLesser, @NonNull ArrayList<Integer> types) {
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
        String whereTypes = selectionTypeFilter(" AND", types);
        String whereTypesLone = selectionTypeFilter(" WHERE", types);
        String whereTypesAs = rawSelectionTypeFilter(" AND", types, "e2.");

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

    // TODO: raw
    @NonNull
    public ArrayList<DistanceItem> getDistanceItems(Distance.SortMode sortMode, boolean smallestFirst, @NonNull ArrayList<Integer> types) {
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
        ArrayList<Distance> distances = getDistances();
        for (Distance distance : distances) {
            distanceItems.add(getDistanceItem(distance.getDistance(), types));
        }

        /*ArrayList<Integer> dList = D.sortDistances(D.distances, smallestFirst, sortMode);
        for (int d : dList) {
            distanceItems.add(getDistanceItem(d));
        }*/

        return distanceItems;
    }

    @Deprecated
    @NonNull
    public DistanceItem getDistanceItem(int distance, @NonNull ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String[] columns = { Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME };
        String selection = Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE + " >= " + minDist + selectionTypeFilter(" AND", types);
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

    @NonNull
    public ArrayList<IntervalItem> getIntervalItems(C.SortMode sortMode, boolean smallestFirst, boolean includeLesser) {
        String[] columns = { Contract.ExerciseEntry.COLUMN_INTERVAL };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, null, null, null, null, orderBy);
        HashMap<String, Integer> names = new HashMap<>();
        ArrayList<IntervalItem> intervalItems = new ArrayList<>();
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

    @NonNull
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

    // projections

    public int avgDistance(int routeId, String routeVar) {
        String columnAvgDistance = "avg_distance";

        String queryString = "select avg(" + Contract.ExerciseEntry.COLUMN_DISTANCE + ") as " + columnAvgDistance +
                " from " + Contract.ExerciseEntry.TABLE_NAME +
                " where " + Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
                " and " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'" +
                " and " + Contract.ExerciseEntry.COLUMN_DISTANCE + " != " + Exercise.DISTANCE_DRIVEN +
                " and " + Contract.ExerciseEntry.COLUMN_DISTANCE + " != " + 0;

        Cursor cursor = db.rawQuery(queryString, null);
        int avgDistance = 0;
        while (cursor.moveToNext()) {
            avgDistance = cursor.getInt(cursor.getColumnIndex(columnAvgDistance));
        }
        cursor.close();

        return avgDistance;
    }

    public int longestDistanceWithinLimits(int ofDistance) {
        int minDist = M.minDistance(ofDistance);
        int maxDist = M.maxDistance(ofDistance);

        String[] columns = { Contract.DistanceEntry.COLUMN_DISTANCE };
        String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " >= " + minDist + " AND " +
                Contract.DistanceEntry.COLUMN_DISTANCE + " <= " + maxDist;
        String orderBy = Contract.DistanceEntry.COLUMN_DISTANCE + sortOrder(false);
        String limit = "1";

        Cursor cursor = db.query(Contract.DistanceEntry.TABLE_NAME, columns, selection, null, null,
                null, orderBy, limit);
        int longestDistance = Distance.NO_DISTANCE;
        while (cursor.moveToNext()) {
            longestDistance = cursor.getInt(cursor.getColumnIndex(Contract.DistanceEntry.COLUMN_DISTANCE));
            break;
        }
        cursor.close();

        return longestDistance;
    }

    // graph data

    // TODO: streamline
    public TreeMap<Float, Float> aggregateDistance(@NonNull ArrayList<Integer> types, LocalDate startDate,
                                                   int intervalLength, int dataPointCount) {
        TreeMap<Float, Float> list = new TreeMap<>();

        return list;
    }

    public TreeMap<Float, Float> weekDailyDistance(@NonNull ArrayList<Integer> types, LocalDate includingDate) {
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

    public TreeMap<Float, Float> yearMonthlyDistance(@NonNull ArrayList<Integer> types, LocalDate includingDate) {
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

    public TreeMap<Float, Float> monthDailyIntegralDistance(@NonNull ArrayList<Integer> types, LocalDate includingDate) {
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

    public TreeMap<Float, Float> yearWeeklyIntegralDistance(@NonNull ArrayList<Integer> types, LocalDate includingDate) {
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
            String routeName = getRouteName(routeId);

            Exercise exercise = new Exercise(_id, externalId, type, dateTime, routeId, routeName, routeVar, interval, note, dataSource, recordingMethod, distance, time, getSubs(_id), trail);
            exercises.add(exercise);
        }

        return exercises;
    }

    private ArrayList<Exerlite> unpackLiteCursor(Cursor cursor, boolean markTop) {
        ArrayList<Exerlite> exerlites = new ArrayList<>();
        int[] indexTop = { -1, -1, -1 };

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
            String routeName = getRouteName(routeId);
            boolean distanceDriven = distance == Exercise.DISTANCE_DRIVEN;

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

            // mark top 3
            if (markTop) {
                int index = exerlites.size() - 1;
                float pace = exerlite.getPace();
                if (pace == 0) continue;

                if (indexTop[0] == -1 || pace < exerlites.get(indexTop[0]).getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = indexTop[0];
                    indexTop[0] = index;
                } else if (indexTop[1] == -1 || pace < exerlites.get(indexTop[1]).getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = index;
                } else if (indexTop[2] == -1 || pace < exerlites.get(indexTop[2]).getPace()) {
                    indexTop[2] = index;
                }
            }
        }

        // mark top 3
        if (markTop) {
            if (indexTop[2] != -1) exerlites.get(indexTop[2]).setTop(3);
            if (indexTop[1] != -1) exerlites.get(indexTop[1]).setTop(2);
            if (indexTop[0] != -1) exerlites.get(indexTop[0]).setTop(1);
        }

        return exerlites;
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

    // tools

    @Nullable
    private <T> T getFirst(ArrayList<T> list) {
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    // query tools

    /**
     * Add to any SQL selection string to also filter by type
     * Includes spacing after keyword, but not before it; use the form " AND".
     *
     * <p>Note: do not substitue passing a keyword for adding one before this string;
     * this takes care of empty lists by not filtering at all, while substituting does not.</p>
     *
     * @param precedingKeyword To precede the statement with if list isn't empty
     * @param visibleTypes Types to filter in
     * @return The SQL query selection string
     */
    @NonNull
    private String selectionTypeFilter(@NonNull String precedingKeyword, @NonNull ArrayList<Integer> visibleTypes) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += precedingKeyword + " (";
            filter += Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) filter += ")";
            else filter += " OR ";
        }
        return filter;
    }

    private String rawSelectionTypeFilter(@NonNull String precedingKeyword, @NonNull ArrayList<Integer> visibleTypes, String tableAsName) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += precedingKeyword + " (";
            filter += tableAsName + Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) filter += ")";
            else filter += " OR ";
        }
        return filter;
    }

    /**
     * Converts a {@link C.SortMode} and a boolean to a ORDER BY SQL clause string
     *
     * @param sortMode Mode to sort by
     * @param smallestFirst Ordering by value
     * @return The column and order combined, e.g. "_ID ASC"
     *
     * @see com.example.trackfield.database.Contract.ExerciseEntry#sortColumn(C.SortMode)
     * @see #sortOrder(boolean)
     */
    @NonNull
    private String orderBy(C.SortMode sortMode, boolean smallestFirst) {
        return Contract.ExerciseEntry.sortColumn(sortMode) + sortOrder(smallestFirst);
    }

    private String orderBy(Distance.SortMode sortMode, boolean smallestFirst) {
        return Contract.DistanceEntry.COLUMN_DISTANCE + sortOrder(smallestFirst);
    }

    /**
     * Converts a boolean to the second parameter of a SQL ORDER BY clause string.
     *
     * @param smallestFirst Ordering by value
     * @return "ASC" if smallestFirst is true, "DESC" if false
     *
     * @see com.example.trackfield.database.Contract.ExerciseEntry#sortColumn(C.SortMode)
     * @see #orderBy(C.SortMode, boolean)
     */
    @NonNull
    private String sortOrder(boolean smallestFirst) {
        return smallestFirst ? " ASC" : " DESC";
    }

}
