package com.example.trackfield.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trackfield.database.Contract.*;
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
    private static final String LOG_TAG = "Reader";

    //

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

    // version

    public int getVersion() {
        return db.getVersion();
    }

    // get exercises

    @Nullable
    public Exercise getExercise(int _id) {
        String selection = ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return getFirst(exercises);
    }

    @Nullable
    public Exercise getExercise(long externalId) {
        String selection = ExerciseEntry.COLUMN_EXTERNAL_ID + " = ?";
        String[] selectionArgs = { Long.toString(externalId) };

        Cursor cursor = db.query(true, ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null,
            null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        if (exercises.size() > 1) {
            Log.w(LOG_TAG + " getExercise", "more than one exercise with externalId " + externalId);
        }

        return getFirst(exercises);
    }

    @NonNull
    public ArrayList<Exercise> getExercises() {
        String queryString = "SELECT * FROM " + ExerciseEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    @NonNull
    public ArrayList<Exercise> getExercises(LocalDateTime dateTime, int type) {
        String selection = "(" + ExerciseEntry.COLUMN_DATE + " = " + M.toEpochSecond(dateTime) + " OR " +
            ExerciseEntry.COLUMN_DATE + " = " + M.toEpochSecond(dateTime.truncatedTo(ChronoUnit.MINUTES)) +
            " OR " +
            ExerciseEntry.COLUMN_DATE + " = " + M.toEpochSecond(M.dateTime(dateTime.toLocalDate())) + ")" +
            " AND (" +
            ExerciseEntry.COLUMN_TYPE + " = " + type + (type == Exercise.TYPE_RUN ?
            " OR " + ExerciseEntry.COLUMN_TYPE + " = " + Exercise.TYPE_INTERVALS : "") +
            ")";

        Cursor cursor = db.query(true, ExerciseEntry.TABLE_NAME, null, selection,
            null, null, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    // get exerlites

    @Nullable
    public Exerlite getExerlite(int _id) {
        String[] columns = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(_id) };

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null,
            null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return getFirst(exerlites);
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(C.SortMode sortMode, boolean smallestFirst,
        @NonNull ArrayList<Integer> types, int startIndex, int endIndex) {
        String[] columns = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = typeFilter("", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy,
            Integer.toString(endIndex));
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(C.SortMode sortMode, boolean smallestFirst,
        @NonNull ArrayList<Integer> types) {
        String[] columns = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = typeFilter("", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesBySearch(String search, C.SortMode sortMode, boolean smallestFirst) {
        if (search.equals("")) {
            return getExerlites(sortMode, smallestFirst, Prefs.getExerciseVisibleTypes());
        }

        String[] columns = ExerciseEntry.COLUMNS_EXERLITE;
        String selection =
            "(" + ExerciseEntry._ID + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_DATE + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_ROUTE + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_ROUTE_VAR + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_DATA_SOURCE + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_RECORDING_METHOD + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_NOTE + " LIKE" + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_TYPE + " LIKE" + "'%" + search + "%')" +
                typeFilter(" AND", Prefs.getExerciseVisibleTypes());

        //String selection = ExerciseEntry.COLUMN_ROUTE + " LIKE ?";
        //String[] selectionArgs = { "%" + filter + "%" };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy,
            null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByRoute(int routeId, C.SortMode sortMode, boolean smallestFirst,
        @NonNull ArrayList<Integer> types) {
        String[] colums = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + typeFilter(" AND", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    /**
     * Gets all exerlites of specified types in range of distance. Also included are any longer exerlites which make it
     * into top 3 by pace. Marks top 3.
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
    public ArrayList<Exerlite> getExerlitesByDistance(int distance, C.SortMode sortMode, boolean smallestFirst,
        @NonNull ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String exerliteColumns = ExerciseEntry.toString(ExerciseEntry.COLUMNS_EXERLITE);
        String orderByPace = orderBy(C.SortMode.PACE, true);
        String table = ExerciseEntry.TABLE_NAME;
        String id = ExerciseEntry._ID;
        String dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String andTypeFilter = typeFilter(" AND", types);

        // sqlite 3.25, säkert effektivare
        /*String queryString3p25 = "SELECT row_number over (ORDER BY " + orderByPace + ") AS rownum, " + exerliteColumns +
                " FROM " + table +
                " WHERE " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " AND " + dist + " <= " + maxDist + ")" +
                " OR " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " ORDER BY " + orderByPace + " LIMIT 3)" +
                " ORDER BY " + orderBy(sortMode, smallestFirst);*/

        String queryString =
            "SELECT " + exerliteColumns +
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

        /*String selection = ExerciseEntry.COLUMN_DISTANCE + (Prefs.includeLonger() ? " >= " + minDist : " BETWEEN " + minDist + " AND " + maxDist) + filter;
        // temp, behöver sortera igen
        if (!Prefs.includeLonger()) D.removeLonger(top3, maxDist);
        D.sortExerlites(top3, sortMode, smallestFirst);*/
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByInterval(String interval, C.SortMode sortMode, boolean smallestFirst) {
        String[] colums = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { interval };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs,
            null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByDate(LocalDateTime min, LocalDateTime max, C.SortMode sortMode,
        boolean smallestFirst, @NonNull ArrayList<Integer> types) {
        String[] colums = ExerciseEntry.COLUMNS_EXERLITE;
        String selection = ExerciseEntry.COLUMN_DATE + " >= " + M.toEpochSecond(M.first(min, max)) + " AND " +
            ExerciseEntry.COLUMN_DATE + " <= " + M.toEpochSecond(M.last(min, max)) +
            typeFilter(" AND", types);
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(true, ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy,
            null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    // get subs

    @NonNull
    private ArrayList<Sub> getSubs(int _superId) {
        String selection = SubEntry.COLUMN_SUPERID + " = ?";
        String[] selectionArgs = { Integer.toString(_superId) };

        Cursor cursor = db.query(SubEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Sub> subs = unpackSubCursor(cursor);
        cursor.close();

        return subs;
    }

    // get routes

    @NonNull
    public ArrayList<Route> getRoutes(boolean includeHidden) {
        String selection = includeHidden ? null : RouteEntry.COLUMN_HIDDEN +
            " = ?"; //+ 0 + " AND " + RouteEntry.COLUMN_AMOUNT + " >= " + 2;
        String[] selectionArgs = includeHidden ? null : new String[] { Integer.toString(0) };
        String orderBy = null;//orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
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
        String selection = RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
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
        String selection = RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(RouteEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
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
        String[] columns = { RouteEntry.COLUMN_NAME };
        String selection = RouteEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String name = Route.NO_NAME;
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME));
        }
        cursor.close();

        return name;
    }

    /**
     * Gets the routeId by corresponding routeName.
     *
     * @param name Name of route
     * @return The routeId of the existing route, or {@link Route#ID_NON_EXISTANT} if not existing
     *
     * @see #getRouteIdOrCreate(String, Context)
     */
    public int getRouteId(String name) {
        String[] columns = { RouteEntry._ID };
        String selection = RouteEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = db.query(RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int _id = Route.ID_NON_EXISTANT;
        while (cursor.moveToNext()) {
            _id = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry._ID));
        }
        cursor.close();

        return _id;
    }

    /**
     * Gets the routeId by corresponding routeName. Internally calls {@link #getRouteId(String)} to query routeId, and
     * {@link Writer#addRoute(Route, Context)} if not existing.
     *
     * @param name Name of route
     * @param c Context, used to get {@link Writer} instance
     * @return The routeId of the existing or created route
     */
    public int getRouteIdOrCreate(String name, Context c) {
        int routeId = getRouteId(name);
        if (routeId == Route.ID_NON_EXISTANT) {
            routeId = (int) Writer.get(c)
                .addRoute(new Route(name), c);
        }
        return routeId;
    }

    // get distances

    @NonNull
    public ArrayList<Distance> getDistances() {
        String orderBy = DistanceEntry.COLUMN_DISTANCE + sortOrder(true);

        Cursor cursor = db.query(DistanceEntry.TABLE_NAME, null, null, null, null, null, orderBy);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return distances;
    }

    @Nullable
    public Distance getDistance(int length) {
        String selection = DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString(length) };

        Cursor cursor = db.query(DistanceEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return getFirst(distances);
    }

    @Deprecated
    public float getDistanceGoal(int distance) {
        String[] columns = { DistanceEntry.COLUMN_GOAL_PACE };
        String selection = DistanceEntry.COLUMN_DISTANCE + " = " + distance;

        Cursor cursor = db.query(DistanceEntry.TABLE_NAME, columns, selection, null, null, null, null);
        float goalPace = Distance.NO_GOAL_PACE;
        while (cursor.moveToNext()) {
            goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_GOAL_PACE));
        }
        cursor.close();

        return goalPace;
    }

    // get trail

    @NonNull
    public HashMap<Integer, String> getPolylines(int exceptId) {
        String[] columns = { ExerciseEntry._ID, ExerciseEntry.COLUMN_POLYLINE };
        String selection =
            ExerciseEntry._ID + " != " + exceptId + " AND " + ExerciseEntry.COLUMN_POLYLINE +
                " IS NOT NULL";

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(_id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public ArrayList<String> getPolylinesByRoute(int routeId) {
        String[] columns = { ExerciseEntry.COLUMN_POLYLINE };
        String selection = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " +
            ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        ArrayList<String> polylines = new ArrayList<>();
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public ArrayList<String> getPolylinesByRoute(int routeId, String routeVar) {
        String[] columns = { ExerciseEntry.COLUMN_POLYLINE };
        String selection = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " +
            ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "' AND " +
            ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        ArrayList<String> polylines = new ArrayList<>();
        while (cursor.moveToNext()) {
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.add(polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public HashMap<Integer, String> getPolylinesByRouteExcept(int exceptRouteId) {
        String[] columns = { ExerciseEntry._ID, ExerciseEntry.COLUMN_POLYLINE };
        String selection = ExerciseEntry.COLUMN_ROUTE_ID + " != " + exceptRouteId + " AND " +
            ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(_id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @Nullable
    public Trail getTrail(int _id) {
        String[] columns = ExerciseEntry.COLUMNS_TRAIL;
        String selection = ExerciseEntry._ID + " = " + _id;

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
        Trail trail = null;
        while (cursor.moveToNext()) {
            double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LAT));
            double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LNG));
            double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LAT));
            double endLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LNG));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            trail = new Trail(polyline, new LatLng(startLat, startLng), new LatLng(endLat, endLng));
            break;
        }
        cursor.close();

        return trail;
    }

    // get items

    @NonNull
    public ArrayList<RouteItem> getRouteItems(C.SortMode sortMode, boolean smallestFirst, boolean includeHidden,
        @NonNull ArrayList<Integer> types) {
        final String tab_e = ExerciseEntry.TABLE_NAME;
        final String tab_r = RouteEntry.TABLE_NAME;
        final String col_e_dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        final String col_e_time = ExerciseEntry.COLUMN_TIME;
        final String col_r_name = RouteEntry.COLUMN_NAME;
        final String col_e_rid = ExerciseEntry.COLUMN_ROUTE_ID;
        final String col_e_date = ExerciseEntry.COLUMN_DATE;
        final String col_r_hidden = RouteEntry.COLUMN_HIDDEN;
        final String col_r_id = RouteEntry._ID;

        final String ali_e = "e";
        final String ali_e2 = "e2";
        final String ali_a = "a";
        final String ali_amount = "amount";
        final String ali_avg_dist = "avg_distance";
        final String ali_best_pace = "best_pace";

        String havingAmount = includeHidden || !Prefs.areSingletonRoutesHidden() ? "" : " HAVING count(1) > 1";
        String whereHidden = includeHidden ? "" : " AND " + col(tab_r, col_r_hidden) + " != 1";

        String orderBy;
        switch (sortMode) {
            case NAME:
                orderBy = "max(" + col_r_name + ")";
                break;
            case AMOUNT:
                orderBy = ali_amount;
                break;
            case DISTANCE:
                orderBy = ali_avg_dist;
                break;
            case PACE:
                orderBy = ali_best_pace;
                break;
            case DATE:
            default:
                orderBy = "max(" + col_e_date + ")";
                break;
        }
        orderBy += sortOrder(smallestFirst);

        String query =
            "SELECT " + com(col(ali_e, col_e_rid), col(tab_r, col_r_name), "count(1) AS " + ali_amount,
                fun("avg", col(ali_e, col_e_dist)) + " AS " + ali_avg_dist, col(ali_a, ali_best_pace)) +
                " FROM " + tab_e + " AS " + ali_e +
                " INNER JOIN " + tab_r + " ON " + col(ali_e, col_e_rid) + " = " + col(tab_r, col_r_id) +
                " INNER JOIN (" +
                "SELECT " + col(ali_e2, col_e_rid) + ", " +
                "min(" + col(ali_e2, col_e_time) + "/" + col(ali_e2, col_e_dist) + ")*1000" + " AS " + ali_best_pace +
                " FROM " + tab_e + " AS " + ali_e2 +
                " WHERE " + col(ali_e2, col_e_time) + " > 0 AND " + col(ali_e2, col_e_dist) + " > 0" + typeFilter(
                    " AND", types) +
                " GROUP BY "  + col(ali_e2, col_e_rid) +
                ") AS " + ali_a + " ON " + col(ali_a, col_e_rid) + " = " + col(ali_e, col_e_rid) +
                " WHERE 1=1" + whereHidden + typeFilter(" AND", types) +
                " GROUP BY " + col(ali_e, col_e_rid) +
                havingAmount +
                " ORDER BY " + orderBy;

        Log.i(LOG_TAG + " getRouteItems", query);

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<RouteItem> routeItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(col_e_rid));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(col_r_name));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow(ali_amount));
            int avgDistance = cursor.getInt(cursor.getColumnIndexOrThrow(ali_avg_dist));
            int bestPace = cursor.getInt(cursor.getColumnIndexOrThrow(ali_best_pace));
            routeItems.add(new RouteItem(routeId, name, count, avgDistance, bestPace));
        }
        cursor.close();

        return routeItems;
    }

    @NonNull
    public ArrayList<DistanceItem> getDistanceItems(Distance.SortMode sortMode, boolean smallestFirst,
        @NonNull ArrayList<Integer> types) {
        // ver 1
        /*ArrayList<Integer> dList = D.sortDistances(D.distances, smallestFirst, sortMode);
        for (int d : dList) {
            distanceItems.add(getDistanceItem(d));
        }*/

        // ver 2
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

        // ver 3
        /*ArrayList<Distance> distances = getDistances();
        for (Distance distance : distances) {
            distanceItems.add(getDistanceItem(distance.getDistance(), types));
        }*/

        String tab_e = ExerciseEntry.TABLE_NAME;
        String tab_d = DistanceEntry.TABLE_NAME;
        String tab_s = "sub";
        String col_d_dist = DistanceEntry.COLUMN_DISTANCE;
        String col_e_eff_dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String col_e_time = ExerciseEntry.COLUMN_TIME;
        String tab_col_e_pace = "(1000 * " + col(tab_e, col_e_time) + " / " + col(tab_e, col_e_eff_dist) + ")";
        String col_s_best_pace = "best_pace";
        String col_s_dist = "distance";
        String col_s_best_time = "best_time";

        String orderBy;
        switch (sortMode) {
            case AMOUNT: // TODO: sub med inner join nödvändig?
                //orderBy = "count(" + "" + ")";
                //break;
            case BEST_TIME: // TODO: ta bort? samma sak som pace?
            case BEST_PACE:
                orderBy = col_s_best_pace;
                break;
            case DISTANCE:
            default:
                orderBy = col_d_dist;
                break;
        }
        orderBy += sortOrder(smallestFirst);

        // ver 4
        /*String queryString =
            "SELECT " + col(tab_d, col_d_dist) + ", " + col(tab_s, col_s_best_pace) + ", " + col(tab_s, col_s_best_time) +
                " FROM " + tab_d +
                " LEFT OUTER JOIN (" +
                "SELECT " + col(tab_d, col_d_dist) + " AS " + col_s_dist + "," +
                " min(" + tab_col_e_pace + ") AS " + col_s_best_pace + ", " +
                col(tab_d, col_d_dist) + " * min(" + tab_col_e_pace + " / 1000) AS " + col_s_best_time +
                " FROM " + tab_e + ", " + tab_d +
                " WHERE " + col(tab_e, col_e_eff_dist) + " >= " + col(tab_d, col_d_dist) +
                " AND " + col(tab_e, col_e_time) + " != 0 " +
                rawSelectionTypeFilter("AND", types, tab_e) +
                " GROUP BY " + col(tab_d, col_d_dist) + ")" +
                " AS " + tab_s + " ON " + col(tab_d, col_d_dist) + " = " + col(tab_s, col_s_dist);*/

        // ver 5
        String queryString = "SELECT " + col_d_dist + ", (" +
            " SELECT min(" + tab_col_e_pace + ")" +
            " FROM " + tab_e +
            " WHERE " + col_e_eff_dist + " >= " + col(tab_d, col_d_dist) + " - " + Prefs.getDistanceLowerLimit() +
            " AND " + col_e_time + " != 0 " + typeFilter("AND", types) + ")" +
            " AS " + col_s_best_pace +
            " FROM " + tab_d +
            " ORDER BY " + orderBy;

        Cursor cursor = db.rawQuery(queryString, null);
        ArrayList<DistanceItem> distanceItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            int distance = cursor.getInt(cursor.getColumnIndex(col_d_dist));
            float bestPace = cursor.getFloat(cursor.getColumnIndex(col_s_best_pace));
            //int bestTime = cursor.getInt(cursor.getColumnIndex(col_s_best_time));

            DistanceItem item = new DistanceItem(distance, bestPace);
            distanceItems.add(item);
        }
        cursor.close();

        return distanceItems;
    }

    @NonNull
    public ArrayList<IntervalItem> getIntervalItems(C.SortMode sortMode, boolean smallestFirst, boolean includeHidden) {
        /*String[] columns = { ExerciseEntry.COLUMN_INTERVAL };
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, null, null, null, null, orderBy);
        HashMap<String, Integer> names = new HashMap<>();
        ArrayList<IntervalItem> intervalItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_INTERVAL));
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
        cursor.close();*/

        String col_amount = "amount";

        String table = ExerciseEntry.TABLE_NAME;
        String[] columns = { ExerciseEntry.COLUMN_INTERVAL, "count(1) AS " + col_amount };
        String selection = ExerciseEntry.COLUMN_INTERVAL + " != ''";
        String groupBy = ExerciseEntry.COLUMN_INTERVAL;
        String having = includeHidden || !Prefs.areSingletonRoutesHidden() ? "" : "count(1) > 1";
        String orderBy = orderBy(sortMode, smallestFirst);

        Cursor cursor = db.query(table, columns, selection, null, groupBy, having, orderBy);
        ArrayList<IntervalItem> intervalItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            String interval = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_INTERVAL));
            int amount = cursor.getInt(cursor.getColumnIndex(col_amount));
            intervalItems.add(new IntervalItem(interval, amount));
        }
        cursor.close();

        return intervalItems;
    }

    // projections

    public int avgDistance(int routeId, String routeVar) {
        String columnAvgDistance = "avg_distance";

        String queryString =
            "select avg(" + ExerciseEntry.COLUMN_DISTANCE + ") as " + columnAvgDistance + " from " +
                ExerciseEntry.TABLE_NAME + " where " + ExerciseEntry.COLUMN_ROUTE_ID + " = " +
                routeId + " and " + ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'" + " and " +
                ExerciseEntry.COLUMN_DISTANCE + " != " + Exercise.DISTANCE_DRIVEN + " and " +
                ExerciseEntry.COLUMN_DISTANCE + " != " + 0;

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

        String[] columns = { DistanceEntry.COLUMN_DISTANCE };
        String selection = DistanceEntry.COLUMN_DISTANCE + " >= " + minDist + " AND " +
            DistanceEntry.COLUMN_DISTANCE + " <= " + maxDist;
        String orderBy = DistanceEntry.COLUMN_DISTANCE + sortOrder(false);
        String limit = "1";

        Cursor cursor = db.query(DistanceEntry.TABLE_NAME, columns, selection, null, null, null, orderBy,
            limit);
        int longestDistance = Distance.NO_DISTANCE;
        while (cursor.moveToNext()) {
            longestDistance = cursor.getInt(cursor.getColumnIndex(DistanceEntry.COLUMN_DISTANCE));
            break;
        }
        cursor.close();

        return longestDistance;
    }

    @NonNull
    public ArrayList<Long> getExternalIds() {
        String[] columns = { ExerciseEntry.COLUMN_EXTERNAL_ID };
        String selection = ExerciseEntry.COLUMN_EXTERNAL_ID + " != ''";

        Cursor cursor = db.query(ExerciseEntry.TABLE_NAME, columns, selection,null, null, null, null);
        ArrayList<Long> externalIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            long externalId = cursor.getLong(cursor.getColumnIndex(ExerciseEntry.COLUMN_EXTERNAL_ID));
            externalIds.add(externalId);
        }
        cursor.close();

        return externalIds;
    }

    // streamlined graph data

    public TreeMap<Float, Float> aggregateDistance(@NonNull ArrayList<Integer> types, LocalDate startDate,
        int nodeCount, ChronoUnit groupUnit) {

        LocalDate endDate = startDate.plus(nodeCount, groupUnit);
        long startEpoch = M.toEpochSecond(M.dateTime(startDate));
        long endEpoch = M.toEpochSecond(M.dateTime(endDate));

        String col_date = ExerciseEntry.COLUMN_DATE;
        String col_dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String col_date_group = "date_group";
        String col_tot_dist = "total_distance";
        String sel_date_group = strftime(groupUnit, col_date); //"strftime('%d', " + col_date + ", 'unixepoch')";

        String queryString =
            "SELECT " + sel_date_group + " AS " + col_date_group + ", " + sum(col_dist) + " AS " + col_tot_dist +
                " FROM " + ExerciseEntry.TABLE_NAME +
                " WHERE " + col_date + " >= " + startEpoch +
                " AND " + col_date + " < " + endEpoch + typeFilter(" AND", types) +
                " GROUP BY " + col_date_group +
                " ORDER BY " + orderBy(C.SortMode.DATE, true);

        Cursor cursor = db.rawQuery(queryString, null);
        TreeMap<Float, Float> nodes = new TreeMap<>();
        while (cursor.moveToNext()) {
            // use getString and Float.valueOf to avoid cursor converting e.g. 08 to 0
            String dateGroup = cursor.getString(cursor.getColumnIndex(col_date_group));
            float totalDistance = cursor.getInt(cursor.getColumnIndex(col_tot_dist));
            nodes.put(Float.valueOf(dateGroup), totalDistance);
        }
        cursor.close();

        // create empty nodes
        int startGroup = startDate.get(M.toChronoField(groupUnit));
        int endGroup = endDate.get(M.toChronoField(groupUnit));
        for (int group = startGroup; group < endGroup; group++) {
            if (!nodes.containsKey((float) group)) {
                nodes.put((float) group, 0f);
            }
        }

        return nodes;
    }

    public TreeMap<Float, Float> accummulateDistance() {
        TreeMap<Float, Float> nodes = new TreeMap<>();

        return nodes;
    }

    public TreeMap<Float, Float> getPaceNodesByDistance(int distance, @NonNull ArrayList<Integer> types) {
        int minDist = M.minDistance(distance);
        int maxDist = M.maxDistance(distance);

        String table = ExerciseEntry.TABLE_NAME;
        String col_id = ExerciseEntry._ID;
        String col_dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String col_time = ExerciseEntry.COLUMN_TIME;
        String sel_pace = "pace";

        String columns = ExerciseEntry.SELECTION_PACE + " AS " + sel_pace;
        String andTypeFilter = typeFilter(" AND", types);
        String orderByPace = orderBy(C.SortMode.PACE, true);
        String orderByDate = orderBy(C.SortMode.DATE, true);

        String query =
            "SELECT " + columns +
                " FROM " + table +
                " WHERE (" + col_id + " IN (SELECT " + col_id + " FROM " + table + " WHERE " + sel_pace + " > 0 AND " +
                col_dist + " >= " + minDist + " AND " + col_dist + " <= " + maxDist + ")" + andTypeFilter +
                " OR " + col_id + " IN (SELECT " + col_id + " FROM " + table + " WHERE " + col_dist + " >= " + minDist +
                " AND " + sel_pace + " > 0" + andTypeFilter + " ORDER BY " + orderByPace + " LIMIT 3))" +
                " ORDER BY " + orderByDate;

        Log.i(LOG_TAG + " getPaceNodesByDistance", query);

        Cursor cursor = db.rawQuery(query, null);
        TreeMap<Float, Float> nodes = new TreeMap<>();
        int rowNum = 0;
        while (cursor.moveToNext()) {
            float pace = cursor.getFloat(cursor.getColumnIndex(sel_pace));
            nodes.put((float) rowNum++, pace);
        }
        cursor.close();

        return nodes;
    }

    public TreeMap<Float, Float> getPaceNodesByRoute(int routeId, @NonNull ArrayList<Integer> types) {
        String sel_pace = "pace";

        String[] select = { ExerciseEntry.SELECTION_PACE + " AS " + sel_pace };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + sel_pace + " > 0" +
            typeFilter(" AND", types);
        String orderBy = orderBy(C.SortMode.DATE, true);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy);
        TreeMap<Float, Float> nodes = new TreeMap<>();
        int rowNum = 0;
        while (cursor.moveToNext()) {
            float pace = cursor.getFloat(cursor.getColumnIndex(sel_pace));
            nodes.put((float) rowNum++, pace);
        }
        cursor.close();

        return nodes;
    }

    // graph data

    public TreeMap<Float, Float> weekDailyDistance(@NonNull ArrayList<Integer> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfWeek(includingDate), M.atEndOfWeek(includingDate),
            C.SortMode.DATE, false, types);

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
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfYear(includingDate), M.atEndOfYear(includingDate),
            C.SortMode.DATE, false, types);

        for (Exerlite e : exerlites) {
            int key = e.getDate()
                .getMonthValue();
            int value =
                monthAndDistance.containsKey(key) ? monthAndDistance.get(key) + e.getDistance() : e.getDistance();
            monthAndDistance.put(key, value);
        }

        for (int m = 1; m <= 12; m++) {
            points.put((float) m, monthAndDistance.containsKey(m) ? (float) monthAndDistance.get(m)
                .intValue() : 0);
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

    public TreeMap<Float, Float> monthDailyIntegralDistance(@NonNull ArrayList<Integer> types,
        LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfMonth(includingDate),
            M.atEndOfMonth(includingDate), C.SortMode.DATE, false, types);

        float totalDistance = 0;

        for (Exerlite e : exerlites) {
            int key = e.getDate()
                .getDayOfMonth();
            int value = dayAndDistance.containsKey(key) ? dayAndDistance.get(key) + e.getDistance() : e.getDistance();
            dayAndDistance.put(key, value);
        }
        if (dayAndDistance.size() == 0) return points;

        for (int d = 0; d <= includingDate.lengthOfMonth(); d++) {
            if (dayAndDistance.containsKey(d)) totalDistance += dayAndDistance.get(d);
            points.put((float) d, totalDistance);
            if (includingDate.isEqual(LocalDate.now()) && LocalDate.now()
                .getDayOfMonth() == d) {
                break;
            }
        }

        return points;
    }

    public TreeMap<Float, Float> yearWeeklyIntegralDistance(@NonNull ArrayList<Integer> types,
        LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfYear(includingDate), M.atEndOfYear(includingDate),
            C.SortMode.DATE, true, types);

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
            float week = (float) Math.ceil(e.getDate()
                .getDayOfYear() / 7f);//e.getWeek();
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
        points.put((float) includingDate.getMonth()
            .length(includingDate.isLeapYear()), 100_000f);

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
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            long externalId = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_EXTERNAL_ID));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TYPE));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_ID));
            //String route = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE));
            String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_VAR));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_INTERVAL));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_NOTE));
            String dataSource = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DATA_SOURCE));
            String recordingMethod = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_RECORDING_METHOD));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TIME));

            // convert trail
            Trail trail = null;
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            if (polyline != null) {
                double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LAT));
                double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LNG));
                double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LAT));
                double endLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LNG));
                trail = new Trail(polyline, new LatLng(startLat, startLng), new LatLng(endLat, endLng));
            }

            // convert
            LocalDateTime dateTime = M.ofEpochSecond(epoch);//LocalDateTime.parse(epoch, Toolbox.C.FORMATTER_SQL);
            if (interval == null) interval = "";
            String routeName = getRouteName(routeId);

            Exercise exercise = new Exercise(_id, externalId, type, dateTime, routeId, routeName, routeVar, interval,
                note, dataSource, recordingMethod, distance, time, getSubs(_id), trail);
            exercises.add(exercise);
        }

        return exercises;
    }

    private ArrayList<Exerlite> unpackLiteCursor(Cursor cursor, boolean markTop) {
        ArrayList<Exerlite> exerlites = new ArrayList<>();
        int[] indexTop = { -1, -1, -1 };

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_ID));
            //String route = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_INTERVAL));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DISTANCE));
            int effectiveDistance = cursor.getInt(
                cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TIME));

            // convert
            LocalDate date = M.ofEpochSecond(epoch)
                .toLocalDate();
            if (interval == null) interval = "";
            String routeName = getRouteName(routeId);
            boolean distanceDriven = distance == Exercise.DISTANCE_DRIVEN;

            // TODO: subs
            /*if (effectiveDistance == 0 && time == 0) {
                String selection = SubEntry.COLUMN_SUPERID + " = ?";
                String[] selectionArgs = { Integer.toString(_id) };
                String[] columns = { SubEntry.COLUMN_DISTANCE, SubEntry.COLUMN_TIME };

                Cursor subCursor = db.query(SubEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                while (subCursor.moveToNext()) {
                    int subDistance = subCursor.getInt(subCursor.getColumnIndexOrThrow(SubEntry.COLUMN_DISTANCE));
                    float subTime = subCursor.getInt(subCursor.getColumnIndexOrThrow(SubEntry.COLUMN_TIME));
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

                if (indexTop[0] == -1 || pace < exerlites.get(indexTop[0])
                    .getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = indexTop[0];
                    indexTop[0] = index;
                }
                else if (indexTop[1] == -1 || pace < exerlites.get(indexTop[1])
                    .getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = index;
                }
                else if (indexTop[2] == -1 || pace < exerlites.get(indexTop[2])
                    .getPace()) {
                    indexTop[2] = index;
                }
            }
        }

        // mark top 3
        if (markTop) {
            if (indexTop[2] != -1) {
                exerlites.get(indexTop[2])
                    .setTop(3);
            }
            if (indexTop[1] != -1) {
                exerlites.get(indexTop[1])
                    .setTop(2);
            }
            if (indexTop[0] != -1) {
                exerlites.get(indexTop[0])
                    .setTop(1);
            }
        }

        return exerlites;
    }

    private ArrayList<Sub> unpackSubCursor(Cursor cursor) {
        ArrayList<Sub> subs = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(SubEntry._ID));
            int superId = cursor.getInt(cursor.getColumnIndexOrThrow(SubEntry.COLUMN_SUPERID));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(SubEntry.COLUMN_DISTANCE));
            float time = cursor.getInt(cursor.getColumnIndexOrThrow(SubEntry.COLUMN_TIME));

            Sub sub = new Sub(_id, superId, distance, time);
            subs.add(sub);
        }

        return subs;
    }

    private ArrayList<Distance> unpackDistanceCursor(Cursor cursor) {
        ArrayList<Distance> distances = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(DistanceEntry._ID));
            int length = cursor.getInt(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_DISTANCE));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_GOAL_PACE));

            Distance distance = new Distance(_id, length, goalPace);
            distances.add(distance);
        }

        return distances;
    }

    private ArrayList<Route> unpackRouteCursor(Cursor cursor) {
        ArrayList<Route> routes = new ArrayList<>();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_GOAL_PACE));
            boolean hidden = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_HIDDEN)) != 0;

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

    // sql tools

    @NonNull
    private String fun(@NonNull String fun, @NonNull String... params) {
        String expression = fun + "(";
        for (int i = 0; i < params.length; i++) {
            expression += params[i];
            if (i != params.length - 1) expression += ", ";
        }
        expression += ")";
        return expression;
    }

    private String col(String table, String column) {
        return table + "." + column;
    }

    private String com(String... s) {
        String expression = s[0];
        for (int i = 1; i < s.length; i++) {
            expression += ", " + s[i];
        }
        return expression;
    }

    // sql clauses

    /**
     * Converts a {@link C.SortMode} and a boolean to a ORDER BY SQL clause string
     *
     * @param sortMode Mode to sort by
     * @param smallestFirst Ordering by value
     * @return The column and order combined, e.g. "_ID ASC"
     *
     * @see com.example.trackfield.database.ExerciseEntry#sortColumn(C.SortMode)
     * @see #sortOrder(boolean)
     */
    @NonNull
    private String orderBy(C.SortMode sortMode, boolean smallestFirst) {
        return ExerciseEntry.sortColumn(sortMode) + sortOrder(smallestFirst);
    }

    /**
     * Converts a boolean to the second parameter of a SQL ORDER BY clause string.
     *
     * @param smallestFirst Ordering by value
     * @return " ASC" if smallestFirst is true, " DESC" if false
     *
     * @see com.example.trackfield.database.ExerciseEntry#sortColumn(C.SortMode)
     * @see #orderBy(C.SortMode, boolean)
     */
    @NonNull
    private String sortOrder(boolean smallestFirst) {
        return smallestFirst ? " ASC" : " DESC";
    }

    // sql functions

    private String strftime(ChronoUnit unit, String column) {
        String format;
        switch (unit) {
            case DAYS:
                format = "'%d'";
                break;
            case WEEKS:
                format = "'%W'";
                break;
            case MONTHS:
                format = "'%m'";
                break;
            case YEARS:
                format = "'%Y'";
                break;
            default:
                format = "'%d'";
                break;
        }

        return fun("strftime", format, column, "'unixepoch'");
        //return "strftime(" + format + ", " + column + ", 'unixepoch')";
    }

    private String sum(String column) {
        return fun("sum", column);
    }

    // sql sub-clauses

    /**
     * Add to any SQL selection string to also filter by type Includes spacing after keyword, but not before it; use the
     * form " AND".
     *
     * <p>Note: do not substitue passing a keyword for adding one before this string;
     * this takes care of empty lists by not filtering at all, while substituting does not.</p>
     *
     * @param precedingKeyword To precede the statement with if list isn't empty
     * @param visibleTypes Types to filter in
     * @return The SQL query selection string
     */
    @NonNull
    private String typeFilter(@NonNull String precedingKeyword, @NonNull ArrayList<Integer> visibleTypes) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += precedingKeyword + " (";
            filter += ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) {
                filter += ")";
            }
            else {
                filter += " OR ";
            }
        }
        return filter;
    }

    private String typeFilter(@NonNull String precedingKeyword, @NonNull ArrayList<Integer> visibleTypes,
        String tableAsName) {
        String filter = "";
        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter += precedingKeyword + " (";
            filter += col(tableAsName, ExerciseEntry.COLUMN_TYPE) + " = " + visibleTypes.get(i);
            if (i == visibleTypes.size() - 1) {
                filter += ")";
            }
            else {
                filter += " OR ";
            }
        }
        return filter;
    }

}
