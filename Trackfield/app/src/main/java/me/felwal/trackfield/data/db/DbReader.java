package me.felwal.trackfield.data.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.felwal.trackfield.data.db.DbContract.DistanceEntry;
import me.felwal.trackfield.data.db.DbContract.ExerciseEntry;
import me.felwal.trackfield.data.db.DbContract.PlaceEntry;
import me.felwal.trackfield.data.db.DbContract.RouteEntry;
import me.felwal.trackfield.data.db.model.Distance;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.data.db.model.Place;
import me.felwal.trackfield.data.db.model.Route;
import me.felwal.trackfield.data.prefs.Prefs;
import me.felwal.trackfield.ui.common.model.Exerlite;
import me.felwal.trackfield.ui.common.model.SorterItem;
import me.felwal.trackfield.ui.main.groupingpager.distancelist.model.DistanceItem;
import me.felwal.trackfield.ui.main.groupingpager.intervallist.model.IntervalItem;
import me.felwal.trackfield.ui.main.groupingpager.placelist.model.PlaceItem;
import me.felwal.trackfield.ui.main.groupingpager.routelist.model.RouteItem;
import me.felwal.trackfield.ui.map.model.Trail;
import me.felwal.trackfield.utils.DateUtils;
import me.felwal.trackfield.utils.MathUtils;
import me.felwal.trackfield.utils.annotation.Unfinished;
import com.google.android.gms.maps.model.LatLng;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class DbReader extends DbHelper {

    private static final String LOG_TAG = "Reader";

    private static DbReader instance;

    //

    private DbReader(Context c) {
        super(c.getApplicationContext());
        db = getReadableDatabase();
    }

    @NonNull
    public static DbReader get(Context c) {
        if (instance == null || !instance.db.isOpen()) instance = new DbReader(c);
        return instance;
    }

    // get version

    public int getVersion() {
        return db.getVersion();
    }

    // get exercises

    @Nullable
    public Exercise getExercise(int id) {
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(id) };

        Cursor cursor = db.query(from, null, where, whereArgs, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return getFirst(exercises);
    }

    @Nullable
    public Exercise getExercise(long stravaId) {
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_STRAVA_ID + " = ?";
        String[] whereArgs = { Long.toString(stravaId) };

        Cursor cursor = db.query(true, from, null, where, whereArgs, null, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        if (exercises.size() > 1) {
            Log.w(LOG_TAG + " getExercise", "more than one exercise with stravaId " + stravaId);
        }

        return getFirst(exercises);
    }

    @NonNull
    public ArrayList<Exercise> getExercises() {
        String query = "SELECT * FROM " + ExerciseEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    @NonNull
    public ArrayList<Exercise> getExercises(LocalDateTime dateTime) {
        String colDate = ExerciseEntry.COLUMN_DATE;

        String from = ExerciseEntry.TABLE_NAME;
        String where = colDate + " = ? OR " + colDate + " = ? OR " + colDate + " = ?";
        String[] whereArgs = { Long.toString(DateUtils.toEpochSecond(dateTime)),
            Long.toString(DateUtils.toEpochSecond(dateTime.truncatedTo(ChronoUnit.MINUTES))),
            Long.toString(DateUtils.toEpochSecond(DateUtils.dateTime(dateTime.toLocalDate()))) };

        Cursor cursor = db.query(true, from, null, where, whereArgs, null, null, null, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    // get exerlites

    @Nullable
    public Exerlite getExerlite(int id) {
        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(id) };

        Cursor cursor = db.query(from, select, where, whereArgs, null, null, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return getFirst(exerlites);
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types, int startIndex, int endIndex) {

        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = typeFilter("", types);
        String orderBy = orderBy(sortMode, ascending);
        String limit = Integer.toString(endIndex);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy, limit);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlites(SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types) {

        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = typeFilter("", types);
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    /**
     * Note: when updating this, also update {@link me.felwal.trackfield.R.string#tv_text_empty_search_msg}
     */
    @NonNull
    public ArrayList<Exerlite> getExerlitesBySearch(String search, SorterItem.Mode sortMode, boolean ascending) {
        if (search.equals("")) {
            return getExerlites(sortMode, ascending, Prefs.getExerciseVisibleTypes());
        }

        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where =
            "('#' || " + ExerciseEntry._ID + " = '" + search + "' OR " +
                ExerciseEntry.COLUMN_DATE + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_ROUTE + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_ROUTE_VAR + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_DEVICE + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_RECORDING_METHOD + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_NOTE + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_TYPE + " LIKE " + "'%" + search + "%' OR " +
                ExerciseEntry.COLUMN_LABEL + " LIKE " + "'%" + search + "%')" +
                typeFilter(" AND", Prefs.getExerciseVisibleTypes());
        String orderBy = orderBy(sortMode, ascending);

        Log.i(LOG_TAG, "search where: " + where);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByRoute(int routeId, SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types) {

        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + typeFilter(" AND", types);
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    @SuppressLint("Range")
    public int getExerliteCountByRoute(int routeId, @Nullable String routeVar, SorterItem.Mode sortMode,
        boolean ascending, @NonNull ArrayList<String> types) {

        String colAmount = "amount";

        String[] select = { "count() AS " + colAmount };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
            (routeVar != null ? " AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'" : "") +
            " AND " + ExerciseEntry.COLUMN_DISTANCE + " != " + Exercise.DISTANCE_DRIVEN +
            typeFilter(" AND", types);
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy);
        int amount = 0;

        while (cursor.moveToNext()) {
            amount = cursor.getInt(cursor.getColumnIndex(colAmount));
        }
        cursor.close();

        return amount;
    }

    /**
     * Gets all exerlites of specified types in range of distance. Also included are any longer exerlites which make it
     * into top 3 by pace. Marks top 3.
     *
     * @param distance The length to consider exerlites in regards to
     * @param sortMode Mode to sort by
     * @param ascending Ordering by value
     * @param types Types to filter in
     * @return List of filtered exerlites
     *
     * @see MathUtils#minDistance(int)
     * @see MathUtils#maxDistance(int)
     * @see Exerlite#setTop(int)
     */
    @NonNull
    public ArrayList<Exerlite> getExerlitesByDistance(int distance, SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types) {

        int minDist = MathUtils.minDistance(distance);
        int maxDist = MathUtils.maxDistance(distance);

        String exerliteColumns = ExerciseEntry.toString(ExerciseEntry.COLUMNS_EXERLITE);
        String orderByPace = orderBy(SorterItem.Mode.PACE, true);
        String table = ExerciseEntry.TABLE_NAME;
        String id = ExerciseEntry._ID;
        String dist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String andTypeFilter = typeFilter(" AND", types);

        // sqlite 3.25, säkert effektivare
        /*String query3p25 = "SELECT row_number over (ORDER BY " + orderByPace + ") AS rownum, " + exerliteColumns +
                " FROM " + table +
                " WHERE " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " AND " + dist + " <= " + maxDist + ")" +
                " OR " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist + " ORDER BY " + orderByPace + " LIMIT 3)" +
                " ORDER BY " + orderBy(sortMode, ascending);*/

        String query =
            "SELECT " + exerliteColumns +
                " FROM " + table +
                " WHERE (" + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist +
                " AND " + dist + " <= " + maxDist + ") " + andTypeFilter +
                " OR " + id + " IN (SELECT " + id + " FROM " + table + " WHERE " + dist + " >= " + minDist +
                andTypeFilter + " ORDER BY " + orderByPace + " LIMIT 3))" +
                " ORDER BY " + orderBy(sortMode, ascending);

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByPlace(Place place, SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types) {

        // since we are merely comparing distances in a binary sense of shorter or longer,
        // we can use not the magnitude, but the magnitude squared.
        // we cant access the SQLite sqrt() function from Android.

        // since the angle corresponding to a certain distance varies for longitude and not for latitude,
        // a circle in meters results in a ellipse in degrees.

        double radius = place.getRadius();
        double lat = place.getLat();
        double lng = place.getLng();

        // calculate degrees to metres conversion. this differs for latitude and longitude.
        double deltaDeg = 0.001;
        float[] deltaLatDistArr = new float[1];
        float[] deltaLngDistArr = new float[1];
        Location.distanceBetween(lat, lng, lat + deltaDeg, lng, deltaLatDistArr);
        Location.distanceBetween(lat, lng, lat, lng + deltaDeg, deltaLngDistArr);
        float deltaLatDist = deltaLatDistArr[0];
        float deltaLngDist = deltaLngDistArr[0];
        double latDistToDegMultiplier = deltaDeg / deltaLatDist;
        double lngDistToDegMultiplier = deltaDeg / deltaLngDist;
        double radiusLatDegSqr = MathUtils.sqr(radius * latDistToDegMultiplier);
        double radiusLngDegSqr = MathUtils.sqr(radius * lngDistToDegMultiplier);

        String exerliteColumns = ExerciseEntry.toString(ExerciseEntry.COLUMNS_EXERLITE);
        String table = ExerciseEntry.TABLE_NAME;
        String startLat = ExerciseEntry.COLUMN_START_LAT;
        String startLng = ExerciseEntry.COLUMN_START_LNG;
        String endLat = ExerciseEntry.COLUMN_END_LAT;
        String endLng = ExerciseEntry.COLUMN_END_LNG;
        String andTypeFilter = typeFilter(" AND", types);

        String query =
            "SELECT " + exerliteColumns +
                " FROM " + table +
                " WHERE ((" + sqr(lat + " - " + startLat) + " / " + radiusLatDegSqr +
                " + " + sqr(lng + " - " + startLng) + " / " + radiusLngDegSqr + ") <= 1" +
                " OR (" + sqr(lat + " - " + endLat) + " / " + radiusLatDegSqr +
                " + " + sqr(lng + " - " + endLng) + " / " + radiusLngDegSqr + ") <= 1)" +
                andTypeFilter +
                " ORDER BY " + orderBy(sortMode, ascending);

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exercise> getExercisesWithTrail(SorterItem.Mode sortMode, boolean ascending) {
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_START_LAT + " IS NOT NULL AND " +
            ExerciseEntry.COLUMN_START_LNG + " IS NOT NULL";
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(true, from, null, where, null, null, null, orderBy, null);
        ArrayList<Exercise> exercises = unpackCursor(cursor);
        cursor.close();

        return exercises;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByInterval(String interval, SorterItem.Mode sortMode, boolean ascending) {
        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] whereArgs = { interval };
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(from, select, where, whereArgs, null, null, orderBy);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, true);
        cursor.close();

        return exerlites;
    }

    @NonNull
    public ArrayList<Exerlite> getExerlitesByDate(LocalDateTime min, LocalDateTime max, SorterItem.Mode sortMode,
        boolean ascending, @NonNull ArrayList<String> types) {

        String[] select = ExerciseEntry.COLUMNS_EXERLITE;
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_DATE + " >= " + DateUtils.toEpochSecond(DateUtils.first(min, max)) +
            " AND " + ExerciseEntry.COLUMN_DATE + " <= " + DateUtils.toEpochSecond(DateUtils.last(min, max)) +
            typeFilter(" AND", types);
        String orderBy = orderBy(sortMode, ascending);

        Cursor cursor = db.query(true, from, select, where, null, null, null, orderBy, null);
        ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor, false);
        cursor.close();

        return exerlites;
    }

    // get routes

    @NonNull
    public ArrayList<Route> getRoutes(boolean includeHidden) {
        String from = RouteEntry.TABLE_NAME;
        String where = includeHidden ? null : RouteEntry.COLUMN_HIDDEN + " = 0";

        Cursor cursor = db.query(from, null, where, null, null, null, null);
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
        String from = RouteEntry.TABLE_NAME;
        String where = RouteEntry.COLUMN_NAME + " = ?";
        String[] shereArgs = { name };

        Cursor cursor = db.query(from, null, where, shereArgs, null, null, null);
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
    @NonNull
    public Route getRoute(int routeId) {
        // TODO: nullable?
        String from = RouteEntry.TABLE_NAME;
        String where = RouteEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(from, null, where, whereArgs, null, null, null);
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
        String[] select = { RouteEntry.COLUMN_NAME };
        String from = RouteEntry.TABLE_NAME;
        String where = RouteEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(routeId) };

        Cursor cursor = db.query(from, select, where, whereArgs, null, null, null);
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
        String[] select = { RouteEntry._ID };
        String from = RouteEntry.TABLE_NAME;
        String where = RouteEntry.COLUMN_NAME + " = ?";
        String[] whereArgs = { name };

        Cursor cursor = db.query(from, select, where, whereArgs, null, null, null);
        int id = Route.ID_NON_EXISTANT;
        
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry._ID));
        }
        cursor.close();

        return id;
    }

    /**
     * Gets the routeId by corresponding routeName. Internally calls {@link #getRouteId(String)} to query routeId, and
     * {@link DbWriter#addRoute(Route, Context)} if not existing.
     *
     * @param name Name of route
     * @param c Context, used to get {@link DbWriter} instance
     * @return The routeId of the existing or created route
     */
    public int getRouteIdOrCreate(String name, Context c) {
        int routeId = getRouteId(name);
        if (routeId == Route.ID_NON_EXISTANT) {
            routeId = (int) DbWriter.get(c).addRoute(new Route(name), c);
        }
        
        return routeId;
    }

    // get distances

    @NonNull
    public ArrayList<Distance> getDistances() {
        String from = DistanceEntry.TABLE_NAME;
        String orderBy = DistanceEntry.COLUMN_DISTANCE + sortOrder(true);

        Cursor cursor = db.query(from, null, null, null, null, null, orderBy);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return distances;
    }

    @Nullable
    public Distance getDistance(int length) {
        String from = DistanceEntry.TABLE_NAME;
        String where = DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] whereArgs = { Integer.toString(length) };

        Cursor cursor = db.query(from, null, where, whereArgs, null, null, null);
        ArrayList<Distance> distances = unpackDistanceCursor(cursor);
        cursor.close();

        return getFirst(distances);
    }

    @Deprecated
    public float getDistanceGoal(int distance) {
        String[] select = { DistanceEntry.COLUMN_GOAL_PACE };
        String from = DistanceEntry.TABLE_NAME;
        String where = DistanceEntry.COLUMN_DISTANCE + " = " + distance;

        Cursor cursor = db.query(from, select, where, null, null, null, null);
        float goalPace = Distance.NO_GOAL_PACE;
        
        while (cursor.moveToNext()) {
            goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_GOAL_PACE));
        }
        cursor.close();

        return goalPace;
    }

    // get places

    @NonNull
    public ArrayList<Place> getPlaces() {
        String query = "SELECT * FROM " + PlaceEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Place> places = unpackPlaceCursor(cursor);
        cursor.close();

        return places;
    }

    @NonNull
    public Place getPlace(int placeId) {
        // TODO: nullable?
        String from = PlaceEntry.TABLE_NAME;
        String where = PlaceEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(placeId) };

        Cursor cursor = db.query(from, null, where, whereArgs, null, null, null);
        ArrayList<Place> places = unpackPlaceCursor(cursor);
        cursor.close();

        return places.size() > 0 ? places.get(0) : new Place();
    }

    public int getPlaceId(String name) {
        String[] select = { PlaceEntry._ID };
        String from = PlaceEntry.TABLE_NAME;
        String where = PlaceEntry.COLUMN_NAME + " = ?";
        String[] whereArgs = { name };

        Cursor cursor = db.query(from, select, where, whereArgs, null, null, null);
        int id = Place.ID_NON_EXISTANT;
        
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(PlaceEntry._ID));
        }
        cursor.close();

        return id;
    }

    public ArrayList<Place> generatePlaces() {
        ArrayList<Place> places = getPlaces();
        ArrayList<Place> newPlaces = new ArrayList<>();

        ArrayList<Exercise> exercises = getExercisesWithTrail(SorterItem.Mode.DATE, false);

        for (Exercise e : exercises) {
            boolean startHasPlace = false;
            boolean endHasPlace = false;

            LatLng start = e.getTrail().getStart();
            LatLng end = e.getTrail().getEnd();

            for (Place p : places) {
                if (p.contains(start)) startHasPlace = true;
                if (p.contains(end)) endHasPlace = true;

                if (startHasPlace && endHasPlace) break;
            }

            Place startPlace = null;
            if (!startHasPlace) {
                startPlace = new Place(start);
                places.add(startPlace);
                newPlaces.add(startPlace);
            }
            if (!endHasPlace && (startPlace == null || !startPlace.contains(end))) {
                places.add(new Place(end));
                newPlaces.add(new Place(end));
            }
        }

        return newPlaces;
    }

    // get trail

    @NonNull
    public HashMap<Integer, String> getPolylines(int exceptId) {
        String[] select = { ExerciseEntry._ID, ExerciseEntry.COLUMN_POLYLINE };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry._ID + " != " + exceptId +
            " AND " + ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL" +
            " AND " + ExerciseEntry.COLUMN_TRAIL_HIDDEN + " = 0";

        Cursor cursor = db.query(from, select, where, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();
        
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @NonNull
    public ArrayList<String> getPolylinesByRoute(int routeId) {
        String[] select = { ExerciseEntry.COLUMN_POLYLINE };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
            " AND " + ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL" +
            " AND " + ExerciseEntry.COLUMN_TRAIL_HIDDEN + " = 0";

        Cursor cursor = db.query(from, select, where, null, null, null, null);
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
        String[] select = { ExerciseEntry.COLUMN_POLYLINE };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
            " AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'" +
            " AND " + ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL" +
            " AND " + ExerciseEntry.COLUMN_TRAIL_HIDDEN + " = 0";

        Cursor cursor = db.query(from, select, where, null, null, null, null);
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
        String[] select = { ExerciseEntry._ID, ExerciseEntry.COLUMN_POLYLINE };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " != " + exceptRouteId +
            " AND " + ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL" +
            " AND " + ExerciseEntry.COLUMN_TRAIL_HIDDEN + " = 0";

        Cursor cursor = db.query(from, select, where, null, null, null, null);
        HashMap<Integer, String> polylines = new HashMap<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            polylines.put(id, polyline);
        }
        cursor.close();

        return polylines;
    }

    @Nullable
    public Trail getTrail(int id) {
        String[] select = ExerciseEntry.COLUMNS_TRAIL;
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry._ID + " = " + id;

        Cursor cursor = db.query(from, select, where, null, null, null, null);
        Trail trail = null;

        if (cursor.moveToNext()) {
            double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LAT));
            double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LNG));
            double endLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LAT));
            double endLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_END_LNG));
            String polyline = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_POLYLINE));
            trail = new Trail(polyline, new LatLng(startLat, startLng), new LatLng(endLat, endLng));
        }
        cursor.close();

        return trail;
    }

    // get items

    @NonNull
    public ArrayList<RouteItem> getRouteItems(SorterItem.Mode sortMode, boolean ascending, boolean includeHidden,
        @NonNull ArrayList<String> types) {

        final String tabExercises = ExerciseEntry.TABLE_NAME;
        final String tabRoutes = RouteEntry.TABLE_NAME;
        final String colExDist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        final String colExTime = ExerciseEntry.COLUMN_TIME;
        final String colRoName = RouteEntry.COLUMN_NAME;
        final String colExRouteid = ExerciseEntry.COLUMN_ROUTE_ID;
        final String colExDate = ExerciseEntry.COLUMN_DATE;
        final String colRoHidden = RouteEntry.COLUMN_HIDDEN;
        final String colRoId = RouteEntry._ID;

        final String aliEx = "e";
        final String aliEx2 = "e2";
        final String aliA = "a";
        final String aliAmount = "amount";
        final String aliAvgDist = "avg_distance";
        final String aliBestPace = "best_pace";

        // TODO: dont count driven distances into avg
        String select = "SELECT " + col(aliEx, colExRouteid) + ", " + col(tabRoutes, colRoName) + ", " +
            "count() AS " + aliAmount + ", " + "avg(" + col(aliEx, colExDist) + ") AS " + aliAvgDist + ", " +
            col(aliA, aliBestPace);
        String from = " FROM " + tabExercises + " AS " + aliEx;

        String innerJoin1 = " INNER JOIN " + tabRoutes + " ON " +
            col(aliEx, colExRouteid) + " = " + col(tabRoutes, colRoId);
        String innerJoin2 = " INNER JOIN (" +
            "SELECT " + col(aliEx2, colExRouteid) + ", " +
            "min(" + col(aliEx2, colExTime) + "/" + col(aliEx2, colExDist) + ")*1000" + " AS " + aliBestPace +
            " FROM " + tabExercises + " AS " + aliEx2 +
            " WHERE " + col(aliEx2, colExTime) + " > 0 AND " + col(aliEx2, colExDist) + " > 0" + typeFilter(
            " AND", types) +
            " GROUP BY " + col(aliEx2, colExRouteid) +
            ") AS " + aliA + " ON " + col(aliA, colExRouteid) + " = " + col(aliEx, colExRouteid);

        String whereHidden = includeHidden ? "" : " AND " + col(tabRoutes, colRoHidden) + " != 1";
        String where = " WHERE 1 = 1" + whereHidden + typeFilter(" AND", types);
        String groupBy = " GROUP BY " + col(aliEx, colExRouteid);
        String having = includeHidden || !Prefs.areSingletonGroupsHidden() ? "" : " HAVING count() > 1";

        String orderBy = " ORDER BY ";
        switch (sortMode) {
            case NAME:
                orderBy += "max(" + colRoName + ")";
                break;
            case AMOUNT:
                orderBy += aliAmount;
                break;
            case DISTANCE:
                orderBy += aliAvgDist;
                break;
            case PACE:
                orderBy += col(aliA, aliBestPace);
                break;
            case DATE_ALT:
                orderBy += "min(" + colExDate + ")";
                break;
            case DATE:
            default:
                orderBy += "max(" + colExDate + ")";
                break;
        }
        orderBy += sortOrder(ascending);

        String query = select + from + innerJoin1 + innerJoin2 + where + groupBy + having + orderBy;

        Log.i(LOG_TAG + " getRouteItems", query);

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<RouteItem> routeItems = new ArrayList<>();

        while (cursor.moveToNext()) {
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(colExRouteid));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(colRoName));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow(aliAmount));
            int avgDistance = cursor.getInt(cursor.getColumnIndexOrThrow(aliAvgDist));
            int bestPace = cursor.getInt(cursor.getColumnIndexOrThrow(aliBestPace));
            routeItems.add(new RouteItem(routeId, name, count, avgDistance, bestPace));
        }
        cursor.close();

        return routeItems;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<DistanceItem> getDistanceItems(SorterItem.Mode sortMode, boolean ascending,
        @NonNull ArrayList<String> types) {

        String tabExercises = ExerciseEntry.TABLE_NAME;
        String tabDistances = DistanceEntry.TABLE_NAME;
        String colDiDist = DistanceEntry.COLUMN_DISTANCE;
        String colExEffDist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String colExTime = ExerciseEntry.COLUMN_TIME;
        String tabColExPace = "(1000 * " + col(tabExercises, colExTime) + " / " + col(tabExercises, colExEffDist) + ")";
        String colBestPace = "best_pace";

        String orderBy;
        switch (sortMode) {
            case AMOUNT:
                //orderBy = "count(" + "" + ")";
                //break;
            case DISTANCE:
            default:
                orderBy = colDiDist;
                break;
        }
        orderBy += sortOrder(ascending);

        // ver 5

        String selectBestPace = "(" +
        " SELECT min(" + tabColExPace + ")" +
            " FROM " + tabExercises +
            " WHERE " + colExEffDist + " >= " + col(tabDistances, colDiDist) + " - " + Prefs.getDistanceLowerLimit() +
            " AND " + colExTime + " != 0 " + typeFilter("AND", types) + ")" +
            " AS " + colBestPace +
            " FROM " + tabDistances;

        String query = "SELECT " + colDiDist + ", " + selectBestPace + " ORDER BY " + orderBy;

        Log.i(LOG_TAG + " getDistanceItems", query);

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DistanceItem> distanceItems = new ArrayList<>();

        while (cursor.moveToNext()) {
            int distance = cursor.getInt(cursor.getColumnIndex(colDiDist));
            float bestPace = cursor.getFloat(cursor.getColumnIndex(colBestPace));

            DistanceItem item = new DistanceItem(distance, bestPace);
            distanceItems.add(item);
        }
        cursor.close();

        return distanceItems;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<IntervalItem> getIntervalItems(SorterItem.Mode sortMode, boolean ascending, boolean includeHidden) {
        String colAmount = "amount";

        String[] select = { ExerciseEntry.COLUMN_INTERVAL, "count() AS " + colAmount };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_INTERVAL + " != ''";
        String groupBy = ExerciseEntry.COLUMN_INTERVAL;
        String having = includeHidden || !Prefs.areSingletonGroupsHidden() ? "" : colAmount + " > 1";

        String orderBy;
        switch (sortMode) {
            case NAME:
                orderBy = ExerciseEntry.COLUMN_INTERVAL;
                break;
            case AMOUNT:
                orderBy = colAmount;
                break;
            case DATE:
            default:
                orderBy = "max(" + ExerciseEntry.COLUMN_DATE + ")";
        }
        orderBy += sortOrder(ascending);

        Cursor cursor = db.query(from, select, where, null, groupBy, having, orderBy);
        ArrayList<IntervalItem> intervalItems = new ArrayList<>();

        while (cursor.moveToNext()) {
            String interval = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_INTERVAL));
            int amount = cursor.getInt(cursor.getColumnIndex(colAmount));
            intervalItems.add(new IntervalItem(interval, amount));
        }
        cursor.close();

        return intervalItems;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<PlaceItem> getPlaceItems(SorterItem.Mode sortMode, boolean ascending, boolean includeHidden) {
        String from = PlaceEntry.TABLE_NAME;
        String where = includeHidden ? null : PlaceEntry.COLUMN_HIDDEN + " != 1";

        String orderBy;
        switch (sortMode) {
            case START_LAT: orderBy = PlaceEntry.COLUMN_LAT; break;
            case START_LNG: orderBy = PlaceEntry.COLUMN_LNG; break;
            case NAME:
            default: orderBy = PlaceEntry.COLUMN_NAME; break;
        }
        orderBy += sortOrder(ascending); // TODO

        Cursor cursor = db.query(from, null, where, null, null, null, orderBy);
        ArrayList<PlaceItem> placeItems = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(PlaceEntry._ID));
            String name = cursor.getString(cursor.getColumnIndex(PlaceEntry.COLUMN_NAME));
            placeItems.add(new PlaceItem(id, name));
        }
        cursor.close();

        return placeItems;
    }

    // projections

    @SuppressLint("Range")
    public int getDrivenDistance(int routeId, String routeVar, String type) {
        ArrayList<String> types = new ArrayList<>(Collections.singleton(type));
        String colAvgDistance = "avg_distance";

        // the filter will be applied with priority:
        // 1. route and routevar and type
        // 2. route and routevar
        // 3. route and type
        // 4. route

        boolean filterByRoutevar = !Prefs.fallbackToRouteWhenDriving()
            || getExerliteCountByRoute(routeId, routeVar, SorterItem.Mode.DATE, true, new ArrayList<>()) > 1;

        boolean filterByType = Prefs.preferSameTypeWhenDriving()
            && ((filterByRoutevar && getExerliteCountByRoute(routeId, routeVar, SorterItem.Mode.DATE, true, types) > 1)
            || getExerliteCountByRoute(routeId, null, SorterItem.Mode.DATE, true, types) > 1);

        String andTypeFilter = filterByType
            ? " AND " + ExerciseEntry.COLUMN_TYPE + " = '" + type  + "'"
            : "";
        String andRoutevarFilter = filterByRoutevar
            ? " AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " = '" + routeVar + "'"
            : "";

        String query =
            "SELECT avg(" + ExerciseEntry.COLUMN_DISTANCE + ") AS " + colAvgDistance +
                " FROM " + ExerciseEntry.TABLE_NAME +
                " WHERE " + ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId +
                andTypeFilter +
                andRoutevarFilter +
                " AND " + ExerciseEntry.COLUMN_DISTANCE + " != " + Exercise.DISTANCE_DRIVEN +
                " AND " + ExerciseEntry.COLUMN_DISTANCE + " != " + 0;

        Cursor cursor = db.rawQuery(query, null);
        int avgDistance = 0;

        while (cursor.moveToNext()) {
            avgDistance = cursor.getInt(cursor.getColumnIndex(colAvgDistance));
        }
        cursor.close();

        return avgDistance;
    }

    @SuppressLint("Range")
    public int getLongestDistanceWithinLimits(int ofDistance) {
        int minDist = MathUtils.minDistance(ofDistance);
        int maxDist = MathUtils.maxDistance(ofDistance);

        String[] select = { DistanceEntry.COLUMN_DISTANCE };
        String from = DistanceEntry.TABLE_NAME;
        String where = DistanceEntry.COLUMN_DISTANCE + " >= " + minDist +
            " AND " + DistanceEntry.COLUMN_DISTANCE + " <= " + maxDist;
        String orderBy = DistanceEntry.COLUMN_DISTANCE + sortOrder(false);
        String limit = "1";

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy, limit);
        int longestDistance = Distance.NO_DISTANCE;

        if (cursor.moveToNext()) {
            longestDistance = cursor.getInt(cursor.getColumnIndex(DistanceEntry.COLUMN_DISTANCE));
        }
        cursor.close();

        return longestDistance;
    }

    public boolean doesStravaIdExist(long stravaId) {
        String[] select = {};
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_STRAVA_ID + " = ?";
        String[] whereArgs = { Long.toString(stravaId) };

        Cursor cursor = db.query(true, from, select, where, whereArgs, null, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    // get single

    @NonNull @SuppressLint("Range")
    public ArrayList<Long> getStravaIds() {
        String[] select = { ExerciseEntry.COLUMN_STRAVA_ID };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_STRAVA_ID + " != ''" +
            " AND " + ExerciseEntry.COLUMN_STRAVA_ID + " != " + Exercise.NO_ID;

        Cursor cursor = db.query(from, select, where, null, null, null, null);
        ArrayList<Long> externalIds = new ArrayList<>();

        while (cursor.moveToNext()) {
            long externalId = cursor.getLong(cursor.getColumnIndex(ExerciseEntry.COLUMN_STRAVA_ID));
            externalIds.add(externalId);
        }
        cursor.close();

        return externalIds;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getTypes(@Nullable String limit) {
        String[] select = { ExerciseEntry.COLUMN_TYPE };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_TYPE + " != ''";
        String groupBy = ExerciseEntry.COLUMN_TYPE;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, limit);
        ArrayList<String> types = new ArrayList<>();

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_TYPE));
            types.add(type);
        }
        cursor.close();

        return types;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getLabels() {
        String[] select = { ExerciseEntry.COLUMN_LABEL };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_LABEL + " != ''";
        String groupBy = ExerciseEntry.COLUMN_LABEL;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, null);
        ArrayList<String> labels = new ArrayList<>();

        while (cursor.moveToNext()) {
            String label = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_LABEL));
            labels.add(label);
        }
        cursor.close();

        return labels;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getRouteNames() {
        String[] select = { ExerciseEntry.COLUMN_ROUTE };
        String from = ExerciseEntry.TABLE_NAME;
        String groupBy = ExerciseEntry.COLUMN_ROUTE;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, null, null, groupBy, null, orderBy, null);
        ArrayList<String> routes = new ArrayList<>();

        while (cursor.moveToNext()) {
            String route = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_ROUTE));
            routes.add(route);
        }
        cursor.close();

        return routes;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getRouteVariations(int routeId) {
        String[] select = { ExerciseEntry.COLUMN_ROUTE_VAR };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId
            + " AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " != ''";
        String groupBy = ExerciseEntry.COLUMN_ROUTE_VAR;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, null);
        ArrayList<String> routeVars = new ArrayList<>();

        while (cursor.moveToNext()) {
            String routeVar = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_ROUTE_VAR));
            routeVars.add(routeVar);
        }
        cursor.close();

        return routeVars;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getIntervals() {
        String[] select = { ExerciseEntry.COLUMN_INTERVAL };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_INTERVAL + " != ''";
        String groupBy = ExerciseEntry.COLUMN_INTERVAL;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, null);
        ArrayList<String> intervals = new ArrayList<>();

        while (cursor.moveToNext()) {
            String interval = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_INTERVAL));
            intervals.add(interval);
        }
        cursor.close();

        return intervals;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getDevices() {
        String[] select = { ExerciseEntry.COLUMN_DEVICE };
        String from = ExerciseEntry.TABLE_NAME;
        String groupBy = ExerciseEntry.COLUMN_DEVICE;
        String where = ExerciseEntry.COLUMN_DEVICE + " != ''";
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, null);
        ArrayList<String> devices = new ArrayList<>();

        while (cursor.moveToNext()) {
            String device = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_DEVICE));
            devices.add(device);
        }
        cursor.close();

        return devices;
    }

    @NonNull @SuppressLint("Range")
    public ArrayList<String> getMethods() {
        String[] select = { ExerciseEntry.COLUMN_RECORDING_METHOD };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_RECORDING_METHOD + " != ''";
        String groupBy = ExerciseEntry.COLUMN_RECORDING_METHOD;
        String orderBy = "count() DESC";

        Cursor cursor = db.query(true, from, select, where, null, groupBy, null, orderBy, null);
        ArrayList<String> methods = new ArrayList<>();

        while (cursor.moveToNext()) {
            String method = cursor.getString(cursor.getColumnIndex(ExerciseEntry.COLUMN_RECORDING_METHOD));
            methods.add(method);
        }
        cursor.close();

        return methods;
    }

    // streamlined graph data

    @Unfinished @SuppressLint("Range")
    public TreeMap<Float, Float> aggregateDistance(@NonNull ArrayList<String> types, LocalDate startDate,
        int nodeCount, ChronoUnit groupUnit) {
        // TODO

        LocalDate endDate = startDate.plus(nodeCount, groupUnit);
        long startEpoch = DateUtils.toEpochSecond(DateUtils.dateTime(startDate));
        long endEpoch = DateUtils.toEpochSecond(DateUtils.dateTime(endDate));

        String colDdate = ExerciseEntry.COLUMN_DATE;
        String colDist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String colDateGroup = "date_group";
        String colTotDist = "total_distance";

        String query =
            "SELECT " + strftime(groupUnit, colDdate) + " AS " + colDateGroup + ", " +
                sum(colDist) + " AS " + colTotDist +
                " FROM " + ExerciseEntry.TABLE_NAME +
                " WHERE " + colDdate + " >= " + startEpoch +
                " AND " + colDdate + " < " + endEpoch + typeFilter(" AND", types) +
                " GROUP BY " + colDateGroup +
                " ORDER BY " + orderBy(SorterItem.Mode.DATE, true);

        Cursor cursor = db.rawQuery(query, null);
        TreeMap<Float, Float> nodes = new TreeMap<>();

        while (cursor.moveToNext()) {
            // use getString and Float.valueOf to avoid cursor converting e.g. 08 to 0
            String dateGroup = cursor.getString(cursor.getColumnIndex(colDateGroup));
            float totalDistance = cursor.getInt(cursor.getColumnIndex(colTotDist));
            nodes.put(Float.valueOf(dateGroup), totalDistance);
        }
        cursor.close();

        // create empty nodes
        int startGroup = startDate.get(DateUtils.toChronoField(groupUnit));
        int endGroup = endDate.get(DateUtils.toChronoField(groupUnit));
        for (int group = startGroup; group < endGroup; group++) {
            if (!nodes.containsKey((float) group)) {
                nodes.put((float) group, 0f);
            }
        }

        return nodes;
    }

    @Unfinished
    public TreeMap<Float, Float> accummulateDistance() {
        // TODO
        TreeMap<Float, Float> nodes = new TreeMap<>();

        return nodes;
    }

    @SuppressLint("Range")
    public TreeMap<Float, Float> getPaceNodesByDistance(int distance, @NonNull ArrayList<String> types) {
        int minDist = MathUtils.minDistance(distance);
        int maxDist = MathUtils.maxDistance(distance);

        String table = ExerciseEntry.TABLE_NAME;
        String colId = ExerciseEntry._ID;
        String colDist = ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE;
        String colTime = ExerciseEntry.COLUMN_TIME;
        String selPace = "pace";

        String select = ExerciseEntry.SELECTION_PACE + " AS " + selPace;
        String andTypeFilter = typeFilter(" AND", types);
        String orderByPace = orderBy(SorterItem.Mode.PACE, true);
        String orderByDate = orderBy(SorterItem.Mode.DATE, true);

        String query =
            "SELECT " + select +
                " FROM " + table +
                " WHERE (" + colId + " IN (SELECT " + colId + " FROM " + table + " WHERE " + selPace + " > 0 AND " +
                colDist + " >= " + minDist + " AND " + colDist + " <= " + maxDist + ")" + andTypeFilter +
                " OR " + colId + " IN (SELECT " + colId + " FROM " + table + " WHERE " + colDist + " >= " + minDist +
                " AND " + selPace + " > 0" + andTypeFilter + " ORDER BY " + orderByPace + " LIMIT 3))" +
                " ORDER BY " + orderByDate;

        Log.i(LOG_TAG + " getPaceNodesByDistance", query);

        Cursor cursor = db.rawQuery(query, null);
        TreeMap<Float, Float> nodes = new TreeMap<>();
        int rowNum = 0;

        while (cursor.moveToNext()) {
            float pace = cursor.getFloat(cursor.getColumnIndex(selPace));
            nodes.put((float) rowNum++, pace);
        }
        cursor.close();

        return nodes;
    }

    @SuppressLint("Range")
    public TreeMap<Float, Float> getPaceNodesByRoute(int routeId, @NonNull ArrayList<String> types) {
        String colPace = "pace";

        String[] select = { ExerciseEntry.SELECTION_PACE + " AS " + colPace };
        String from = ExerciseEntry.TABLE_NAME;
        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = " + routeId + " AND " + colPace + " > 0" +
            typeFilter(" AND", types);
        String orderBy = orderBy(SorterItem.Mode.DATE, true);

        Cursor cursor = db.query(from, select, where, null, null, null, orderBy);
        TreeMap<Float, Float> nodes = new TreeMap<>();
        int rowNum = 0;

        while (cursor.moveToNext()) {
            float pace = cursor.getFloat(cursor.getColumnIndex(colPace));
            nodes.put((float) rowNum++, pace);
        }
        cursor.close();

        return nodes;
    }

    // graph data

    public TreeMap<Float, Float> getWeekDailyDistance(@NonNull ArrayList<String> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(DateUtils.atStartOfWeek(includingDate),
            DateUtils.atEndOfWeek(includingDate), SorterItem.Mode.DATE, false, types);

        for (Exerlite e : exerlites) {
            int key = e.getDate().getDayOfWeek().getValue();
            int value = dayAndDistance.containsKey(key) ? dayAndDistance.get(key) + e.getDistance() : e.getDistance();
            dayAndDistance.put(key, value);
        }

        for (int d = 1; d <= 7; d++) {
            points.put((float) d, dayAndDistance.containsKey(d) ? (float) dayAndDistance.get(d) : 0);
        }

        return points;
    }

    public TreeMap<Float, Float> getYearMonthlyDistance(@NonNull ArrayList<String> types, LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> monthAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(DateUtils.atStartOfYear(includingDate),
            DateUtils.atEndOfYear(includingDate),
            SorterItem.Mode.DATE, false, types);

        for (Exerlite e : exerlites) {
            int key = e.getDate()
                .getMonthValue();
            int value =
                monthAndDistance.containsKey(key) ? monthAndDistance.get(key) + e.getDistance() : e.getDistance();
            monthAndDistance.put(key, value);
        }

        for (int m = 1; m <= 12; m++) {
            points.put((float) m, monthAndDistance.containsKey(m) ? (float) monthAndDistance.get(m) : 0);
        }

        return points;
    }

    public TreeMap<Float, Float> getYearMonthlyDistanceGoal() {
        TreeMap<Float, Float> points = new TreeMap<>();

        for (int m = 1; m <= 12; m++) {
            points.put((float) m, 100_000f);
        }

        return points;
    }

    public TreeMap<Float, Float> getMonthDailyIntegralDistance(@NonNull ArrayList<String> types,
        LocalDate includingDate) {

        TreeMap<Float, Float> points = new TreeMap<>();
        TreeMap<Integer, Integer> dayAndDistance = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(DateUtils.atStartOfMonth(includingDate),
            DateUtils.atEndOfMonth(includingDate), SorterItem.Mode.DATE, false, types);

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

    public TreeMap<Float, Float> getYearWeeklyIntegralDistance(@NonNull ArrayList<String> types,
        LocalDate includingDate) {

        TreeMap<Float, Float> points = new TreeMap<>();
        ArrayList<Exerlite> exerlites = getExerlitesByDate(DateUtils.atStartOfYear(includingDate),
            DateUtils.atEndOfYear(includingDate), SorterItem.Mode.DATE, true, types);

        float totalDistance = 0;
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

    @Deprecated
    public TreeMap<Float, Float> getMonthIntegralDistanceGoal(LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();

        points.put(0f, 0f);
        points.put((float) includingDate.getMonth().length(includingDate.isLeapYear()), 100_000f);

        return points;
    }

    @Deprecated
    public TreeMap<Float, Float> getYearIntegralDistanceGoal(LocalDate includingDate) {
        TreeMap<Float, Float> points = new TreeMap<>();

        points.put(0f, 0f);
        points.put(53f/*(float) includingDate.lengthOfYear()*/, 1_200_000f);

        return points;
    }

    // unpack cursors

    private ArrayList<Exercise> unpackCursor(Cursor cursor) {
        ArrayList<Exercise> exercises = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            long stravaId = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_STRAVA_ID));
            long garminId = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_GARMIN_ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TYPE));
            String label = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_LABEL));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_ID));
            String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_VAR));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_INTERVAL));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_NOTE));
            String dataSource = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DEVICE));
            String recordingMethod = cursor.getString(cursor.getColumnIndexOrThrow(
                ExerciseEntry.COLUMN_RECORDING_METHOD));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TIME));
            boolean hideTrail = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TRAIL_HIDDEN)) != 0;

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
            LocalDateTime dateTime = DateUtils.ofEpochSecond(epoch);
            if (interval == null) interval = "";
            String routeName = getRouteName(routeId);

            Exercise exercise = new Exercise(id, stravaId, garminId, type, label, dateTime, routeId, routeName,
                routeVar, interval, note, dataSource, recordingMethod, distance, time, trail, hideTrail);
            exercises.add(exercise);
        }

        return exercises;
    }

    private ArrayList<Exerlite> unpackLiteCursor(Cursor cursor, boolean markTop3) {
        ArrayList<Exerlite> exerlites = new ArrayList<>();
        int[] indexTop = { -1, -1, -1 };

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry._ID));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TYPE));
            long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DATE));
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_ID));
            String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_ROUTE_VAR));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_INTERVAL));
            int distance = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_DISTANCE));
            //int effectiveDistance =
            //    cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE));
            float time = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_TIME));
            double startLat = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LAT));
            double startLng = cursor.getDouble(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_START_LNG));

            // convert
            LocalDate date = DateUtils.ofEpochSecond(epoch).toLocalDate();
            if (interval == null) interval = "";
            String routeName = getRouteName(routeId);
            LatLng start = new LatLng(startLat, startLng);
            boolean distanceDriven = distance == Exercise.DISTANCE_DRIVEN;
            int effectiveDistance = distanceDriven ? getDrivenDistance(routeId, routeVar, type) : distance;

            Exerlite exerlite = new Exerlite(id, type, date, routeName, interval, effectiveDistance, time,
                start, distanceDriven);
            exerlites.add(exerlite);

            // check pace ranking against previous
            if (markTop3) {
                int index = exerlites.size() - 1;
                float pace = exerlite.getPace();
                if (pace == 0) continue;

                if (indexTop[0] == -1 || pace < exerlites.get(indexTop[0]).getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = indexTop[0];
                    indexTop[0] = index;
                }
                else if (indexTop[1] == -1 || pace < exerlites.get(indexTop[1]).getPace()) {
                    indexTop[2] = indexTop[1];
                    indexTop[1] = index;
                }
                else if (indexTop[2] == -1 || pace < exerlites.get(indexTop[2]).getPace()) {
                    indexTop[2] = index;
                }
            }
        }

        // mark top 3
        if (markTop3) {
            if (indexTop[2] != -1) exerlites.get(indexTop[2]).setTop(3);
            if (indexTop[1] != -1) exerlites.get(indexTop[1]).setTop(2);
            if (indexTop[0] != -1) exerlites.get(indexTop[0]).setTop(1);
        }

        return exerlites;
    }

    private ArrayList<Distance> unpackDistanceCursor(Cursor cursor) {
        ArrayList<Distance> distances = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DistanceEntry._ID));
            int length = cursor.getInt(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_DISTANCE));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(DistanceEntry.COLUMN_GOAL_PACE));

            Distance distance = new Distance(id, length, goalPace);
            distances.add(distance);
        }

        return distances;
    }

    private ArrayList<Place> unpackPlaceCursor(Cursor cursor) {
        ArrayList<Place> places = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(PlaceEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(PlaceEntry.COLUMN_NAME));
            float lat = cursor.getFloat(cursor.getColumnIndexOrThrow(PlaceEntry.COLUMN_LAT));
            float lng = cursor.getFloat(cursor.getColumnIndexOrThrow(PlaceEntry.COLUMN_LNG));
            int radius = cursor.getInt(cursor.getColumnIndexOrThrow(PlaceEntry.COLUMN_RADIUS));
            boolean hidden = cursor.getInt(cursor.getColumnIndexOrThrow(PlaceEntry.COLUMN_HIDDEN)) != 0;

            Place place = new Place(id, name, lat, lng, radius, hidden);
            places.add(place);
        }

        return places;
    }

    private ArrayList<Route> unpackRouteCursor(Cursor cursor) {
        ArrayList<Route> routes = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME));
            float goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_GOAL_PACE));
            boolean hidden = cursor.getInt(cursor.getColumnIndexOrThrow(RouteEntry.COLUMN_HIDDEN)) != 0;

            Route route = new Route(id, name, goalPace, hidden);
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
        StringBuilder expression = new StringBuilder(fun + "(");
        for (int i = 0; i < params.length; i++) {
            expression.append(params[i]);
            if (i != params.length - 1) expression.append(", ");
        }
        expression.append(")");
        return expression.toString();
    }

    private String col(String table, String column) {
        return table + "." + column;
    }

    // sql clauses

    /**
     * Converts a {@link SorterItem.Mode} and a boolean to a ORDER BY SQL clause string
     *
     * @param sortMode Mode to sort by
     * @param ascending Ordering by value
     * @return The column and order combined, e.g. "_ID ASC"
     *
     * @see me.felwal.trackfield.data.db.ExerciseEntry#sortColumn(SorterItem.Mode)
     * @see #sortOrder(boolean)
     */
    @NonNull
    private String orderBy(SorterItem.Mode sortMode, boolean ascending) {
        return ExerciseEntry.sortColumn(sortMode) + sortOrder(ascending);
    }

    /**
     * Converts a boolean to the second parameter of a SQL ORDER BY clause string.
     *
     * @param ascending Ordering by value
     * @return " ASC" if ascending is true, " DESC" if false
     *
     * @see me.felwal.trackfield.data.db.ExerciseEntry#sortColumn(SorterItem.Mode)
     * @see #orderBy(SorterItem.Mode, boolean)
     */
    @NonNull
    private String sortOrder(boolean ascending) {
        return ascending ? " ASC" : " DESC";
    }

    // sql functions

    private String strftime(ChronoUnit unit, String column) {
        String format;
        switch (unit) {
            case WEEKS:
                format = "'%W'";
                break;
            case MONTHS:
                format = "'%m'";
                break;
            case YEARS:
                format = "'%Y'";
                break;
            case DAYS:
            default:
                format = "'%d'";
                break;
        }

        return fun("strftime", format, column, "'unixepoch'");
    }

    private String sum(String column) {
        return fun("sum", column);
    }

    private String pow(String base, int exponent) {
        if (exponent < 0) throw new InvalidParameterException("Only nonnegative exponents are supported.");

        String baseParenthesised = "(" + base + ")";

        StringBuilder expression = new StringBuilder(baseParenthesised);
        for (int i = 1; i < exponent; i++) {
            expression.append(" * ").append(baseParenthesised);
        }
        return expression.toString();
    }

    private String sqr(String base) {
        return pow(base, 2);
    }

    // sql sub-clauses

    /**
     * Add to any SQL where string to also filter by type Includes spacing after keyword, but not before it; use the
     * form " AND".
     *
     * <p>Note: do not substitue passing a keyword for adding one before this string;
     * this takes care of empty lists by not filtering at all, while substituting does not.</p>
     *
     * @param precedingKeyword To precede the statement with if list isn't empty
     * @param visibleTypes Types to filter in
     * @return The SQL query where string
     */
    @NonNull
    private String typeFilter(@NonNull String precedingKeyword, @NonNull ArrayList<String> visibleTypes) {
        StringBuilder filter = new StringBuilder();

        for (int i = 0; i < visibleTypes.size(); i++) {
            if (i == 0) filter.append(precedingKeyword).append(" (");

            filter.append(ExerciseEntry.COLUMN_TYPE + " = '").append(visibleTypes.get(i)).append("'");

            if (i == visibleTypes.size() - 1) filter.append(")");
            else filter.append(" OR ");
        }
        return filter.toString();
    }

}
