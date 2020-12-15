package com.example.trackfield.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.trackfield.objects.Distance;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.objects.Route;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.Prefs;
import com.example.trackfield.toolbox.Toolbox.*;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Helper {

    private static Writer writer;
    private static Reader reader;

    public static Writer getWriter(Context c) {
        return writer == null || !writer.isOpen() ? writer = new Writer(c) : writer;
    }
    public static Reader getReader(Context c) {
        return reader == null || !reader.isOpen() ? reader = new Reader(c) : reader;
    }
    public static Reader getReader() {
        return reader;
    }

    public static void openWriter(Context c) {
        if (writer == null || !writer.isOpen()) writer = new Writer(c);
    }
    public static void openReader(Context c) {
        if (reader == null || !reader.isOpen()) reader = new Reader(c);
    }

    public static void closeWriter() {
        if (writer != null && writer.isOpen()) writer.close();
    }
    public static void closeReader() {
        if (reader != null && reader.isOpen()) reader.close();
    }

    ////

    public static abstract class Base extends SQLiteOpenHelper {

        protected SQLiteDatabase db;

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Trackfield.db";

        ////

        public Base(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override public void onCreate(SQLiteDatabase db) {
            Contract.ExerciseEntry.create(db);
            Contract.SubEntry.create(db);
            Contract.RouteEntry.create(db);
            Contract.DistanceEntry.create(db);
        }
        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            Contract.ExerciseEntry.delete(db);
            Contract.SubEntry.delete(db);
            Contract.RouteEntry.delete(db);
            Contract.DistanceEntry.delete(db);
            onCreate(db);
        }
        @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public boolean isOpen() {
            return db.isOpen();
        }
        @Override public synchronized void close() {
            db.close();
            super.close();
        }

    }

    public static class Writer extends Base {

        public static boolean useUpdateTool = false;

        ////

        public Writer(Context context) {
            super(context);
            db = getWritableDatabase();
        }

        public void recreate() {
            onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
        }

        public void importFromToolbox(Context c) {
            L.toast(deleteAllExercises() && addExercises(D.exercises, c), c);
        }
        public void updateTool(Context c) {

            useUpdateTool = false;
        }

        // add
        public boolean addExercises(ArrayList<Exercise> exercises, Context c) {

            boolean success = true;
            for (Exercise e : exercises) { success &= addExercise(e, c); }
            return success;
        }
        public boolean addExercise(Exercise e, Context c) {

            e.setRouteId((int) addRoute(new Route(e.getRouteId(), e.getRoute()), c));

            final ContentValues cv = fillExerciseContentValues(e);

            final long _id = db.insert(Contract.ExerciseEntry.TABLE_NAME, null, cv);
            e.setSubs_superId((int) _id);
            final boolean subSuccess = addSubs(e.getSubs());
            //final boolean mapSuccess = addMap(e.getMap());

            return success(_id) && subSuccess; //&& mapSuccess;
        }

        private boolean addSubs(ArrayList<Sub> subs) {

            boolean success = true;
            for (Sub sub : subs) { success &= addSub(sub); }
            return success;
        }
        private boolean addSub(Sub sub) {

            final ContentValues cvSub = fillSubContentValues(sub);
            final long result = db.insert(Contract.SubEntry.TABLE_NAME, null, cvSub);
            return success(result);
        }

        // new rec
        public boolean addDistance(Distance distance) {

            final ContentValues cv = fillDistanceContentValues(distance);
            final long result = db.insert(Contract.DistanceEntry.TABLE_NAME, null, cv);
            return success(result);
        }
        public boolean updateDistance(Distance distance) {

            ContentValues newCv = fillDistanceContentValues(distance);

            String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = ?";
            String[] selectionArgs = { Integer.toString((distance.getDistance())) };

            final int count = db.update(Contract.DistanceEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return count > 0;
        }
        public boolean deleteDistance(Distance distance) {

            final String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = ?";
            final String[] selectionArgs = { Integer.toString(distance.getDistance()) };

            final long result = db.delete(Contract.DistanceEntry.TABLE_NAME, selection, selectionArgs);

            return success(result);
        }

        public long addRoute(Route route, Context c) {

            Reader reader = new Reader(c);
            Route existingRoute = reader.getRoute(route.getName());
            if (existingRoute != null) return existingRoute.get_id();
            reader.close();

            final ContentValues cv = fillRouteContentValues(route);
            final long _id = db.insert(Contract.RouteEntry.TABLE_NAME, null, cv);
            return _id;
        }
        public boolean updateRoute(Route route) {

            ContentValues newCv = fillRouteContentValues(route);

            String selection = Contract.RouteEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(route.get_id()) };

            final int count = db.update(Contract.RouteEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return count > 0;
        }
        public boolean deleteRoute(int routeId) {

            final String selection = Contract.RouteEntry._ID + " = ?";
            final String[] selectionArgs = { Integer.toString(routeId) };

            final long result = db.delete(Contract.RouteEntry.TABLE_NAME, selection, selectionArgs);

            return success(result);
        }
        public boolean deleteRoute(String name) {

            final String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
            final String[] selectionArgs = { name };

            final long result = db.delete(Contract.RouteEntry.TABLE_NAME, selection, selectionArgs);

            return success(result);
        }

        // update
        public boolean updateExercise(Exercise e) {

            ContentValues newCv = fillExerciseContentValues(e);

            String selection = Contract.ExerciseEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(e.get_id()) };

            final int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);
            final boolean subSuccess = updateSubs(e.getSubs());
            //final boolean mapSuccess = updateMap(e.getMap());

            return count > 0 && subSuccess;// && mapSuccess;
        }
        public boolean updateRouteName(String oldRoute, String newRoute) {

            ContentValues newCv = new ContentValues();
            newCv.put(Contract.ExerciseEntry.COLUMN_ROUTE, newRoute);

            String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?";
            String[] selectionArgs = { oldRoute };

            int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return count > 0;
        }
        public boolean updateInterval(String oldInterval, String newInterval) {

            ContentValues newCv = new ContentValues();
            newCv.put(Contract.ExerciseEntry.COLUMN_INTERVAL, newInterval);

            String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
            String[] selectionArgs = { oldInterval };

            int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return count > 0;
        }

        private boolean updateSubs(ArrayList<Sub> subs) {

            boolean success = true;
            for (Sub sub : subs) {

                if (sub.get_id() != -1) {
                    ContentValues newCv = fillSubContentValues(sub);
                    String selection = Contract.SubEntry._ID + " = ?";
                    String[] selectionArgs = { Integer.toString(sub.get_id()) };

                    int count = db.update(Contract.SubEntry.TABLE_NAME, newCv, selection, selectionArgs);
                    success &= count > 0;
                }
                else { success &= addSub(sub); }
            }

            return success;
        }

        // delete
        public boolean deleteExercise(Exercise e, Context c) {

            final String selection = Contract.ExerciseEntry._ID + " = ?";
            final String subSelection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
            final String[] selectionArgs = { Integer.toString(e.get_id()) };

            final long result = db.delete(Contract.ExerciseEntry.TABLE_NAME, selection, selectionArgs);
            final long subResult = db.delete(Contract.SubEntry.TABLE_NAME, subSelection, selectionArgs);

            // route
            /*Reader reader = new Reader(c);
            if (reader.getExerlitesByRoute(e.getRouteId(), C.SortMode.DATE, false).size() == 0) deleteRoute(e.getRouteId());
            reader.close();*/

            return success(result) && success(subResult);
        }
        public boolean deleteAllExercises() {

            final long result = db.delete(Contract.ExerciseEntry.TABLE_NAME, null, null);
            final long subResult = db.delete(Contract.SubEntry.TABLE_NAME, null, null);

            return success(result) && success(subResult);
        }
        public boolean deleteSub(Sub sub) {

            final String selection = Contract.SubEntry._ID + " = ?";
            final String[] selectionArgs = { Integer.toString(sub.get_id()) };

            final long result = db.delete(Contract.SubEntry.TABLE_NAME, selection, selectionArgs);

            return success(result);
        }

        // ContentValues
        private ContentValues fillExerciseContentValues(Exercise e) {

            ContentValues cv = new ContentValues();

            cv.put(Contract.ExerciseEntry.COLUMN_ID, e.get_id());
            cv.put(Contract.ExerciseEntry.COLUMN_TYPE, e.getType());
            cv.put(Contract.ExerciseEntry.COLUMN_DATE, e.getEpoch());
            cv.put(Contract.ExerciseEntry.COLUMN_ROUTE_ID, e.getRouteId());
            cv.put(Contract.ExerciseEntry.COLUMN_ROUTE, e.getRoute());
            cv.put(Contract.ExerciseEntry.COLUMN_ROUTEVAR, e.getRouteVar());
            cv.put(Contract.ExerciseEntry.COLUMN_INTERVAL, e.getInterval());
            cv.put(Contract.ExerciseEntry.COLUMN_NOTE, e.getNote());
            cv.put(Contract.ExerciseEntry.COLUMN_DATASOURCE, e.getDataSource());
            cv.put(Contract.ExerciseEntry.COLUMN_RECORDINGMETHOD, e.getRecordingMethod());
            cv.put(Contract.ExerciseEntry.COLUMN_DISTANCE, e.getDistancePrimary());
            cv.put(Contract.ExerciseEntry.COLUMN_TIME, e.getTimePrimary());

            Trail trail = e.getTrail();
            if (trail != null) {
                cv.put(Contract.ExerciseEntry.COLUMN_POLYLINE, trail.getPolyline());
                if (trail.hasStartEnd()) {
                    cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, trail.getStartLat());
                    cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, trail.getStartLng());
                    cv.put(Contract.ExerciseEntry.COLUMN_END_LAT,   trail.getEndLat());
                    cv.put(Contract.ExerciseEntry.COLUMN_END_LNG,   trail.getEndLng());
                }
                else {
                    cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, (Double) null);
                    cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, (Double) null);
                    cv.put(Contract.ExerciseEntry.COLUMN_END_LAT,   (Double) null);
                    cv.put(Contract.ExerciseEntry.COLUMN_END_LNG,   (Double) null);
                }
            }
            else {
                cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_END_LAT,   (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_END_LNG,   (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_POLYLINE,  (String) null);
            }

            return cv;
        }
        private ContentValues fillSubContentValues(Sub sub) {

            ContentValues cv = new ContentValues();

            cv.put(Contract.SubEntry.COLUMN_SUPERID, sub.get_superId());
            cv.put(Contract.SubEntry.COLUMN_DISTANCE, sub.getDistance());
            cv.put(Contract.SubEntry.COLUMN_TIME, sub.getTime());

            return cv;
        }
        private ContentValues fillDistanceContentValues(Distance distance) {

            ContentValues cv = new ContentValues();

            cv.put(Contract.DistanceEntry.COLUMN_DISTANCE, distance.getDistance());
            cv.put(Contract.DistanceEntry.COLUMN_GOAL_PACE, distance.getGoalPace());

            return cv;
        }
        private ContentValues fillRouteContentValues(Route route) {

            ContentValues cv = new ContentValues();

            cv.put(Contract.RouteEntry.COLUMN_NAME, route.getName());
            cv.put(Contract.RouteEntry.COLUMN_HIDDEN, route.isHidden() ? 1 : 0);
            cv.put(Contract.RouteEntry.COLUMN_GOAL_PACE, route.getGoalPace());

            return cv;
        }

        // tools
        private boolean success(long dbResult) {
            return dbResult != -1;
        }

    }

    public static class Reader extends Base {

        public Reader(Context context) {
            super(context);
            db = getReadableDatabase();
        }

        // get single
        public Exercise getExerciseById(int id) {

            String selection = Contract.ExerciseEntry.COLUMN_ID + " = ?";
            String[] selectionArgs = { Integer.toString(id) };

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            ArrayList<Exercise> exercises = unpackCursor(cursor);

            cursor.close();
            return exercises.size() > 0 ? exercises.get(0) : null;
        }
        public Exercise getExercise(int _id) {

            String selection = Contract.ExerciseEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(_id) };

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            ArrayList<Exercise> exercises = unpackCursor(cursor);

            cursor.close();
            return exercises.size() > 0 ? exercises.get(0) : null;
        }
        public Exerlite getExerlite(int _id) {

            String[] columns = Contract.ExerciseEntry.EXERLITE_COLUMNS;
            String selection = Contract.ExerciseEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(_id) };

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);

            cursor.close();
            return exerlites.size() > 0 ? exerlites.get(0) : null;
        }

        private ArrayList<Sub> getSubs(int _superId) {

            String selection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
            String[] selectionArgs = { Integer.toString(_superId) };

            Cursor cursor = db.query(Contract.SubEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            ArrayList<Sub> subs = unpackSubCursor(cursor);

            cursor.close();
            return subs;
        }

        // get multiple
        public ArrayList<Exercise> getExercises() {

            String queryString = "SELECT * FROM " + Contract.ExerciseEntry.TABLE_NAME;

            Cursor cursor = db.rawQuery(queryString, null);
            ArrayList<Exercise> exercises = unpackCursor(cursor);
            cursor.close();

            return exercises;
        }
        public ArrayList<Exercise> getExercisesForMerge(LocalDateTime dateTime, int type) {

            String selection = "(" + Contract.ExerciseEntry.COLUMN_DATE + " = " + M.epoch(dateTime) + " OR " +
                    Contract.ExerciseEntry.COLUMN_DATE + " = " + M.epoch(M.dateTime(dateTime.toLocalDate())) + ") AND (" +
                    Contract.ExerciseEntry.COLUMN_TYPE + " = " + type + (type == Exercise.TYPE_RUN ? " OR " + Contract.ExerciseEntry.COLUMN_TYPE + " = " + Exercise.TYPE_INTERVALS : "") + ")" ;

            Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, null, selection, null, null, null, null, null);
            ArrayList<Exercise> exercises = unpackCursor(cursor);
            cursor.close();

            return exercises;
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
            if (search.equals("")) return getExerlites(sortMode, smallestFirst, Prefs.getExerciseVisibleTypes());

            String[] columns = Contract.ExerciseEntry.EXERLITE_COLUMNS;
            String selection = "(" +
                    Contract.ExerciseEntry._ID + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_DATE + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_ROUTEVAR + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_DATASOURCE + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_RECORDINGMETHOD + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_NOTE + " LIKE" + "'%" + search + "%' OR " +
                    Contract.ExerciseEntry.COLUMN_TYPE + " LIKE" + "'%" + search + "%')" + selectionFilter(Prefs.getExerciseVisibleTypes());

            //String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " LIKE ?";
            //String[] selectionArgs = { "%" + filter + "%" };
            String orderBy = orderBy(sortMode, smallestFirst);

            Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy, null);
            ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);

            cursor.close();
            return exerlites;
        }
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
        public ArrayList<Exerlite> getExerlitesByDistance(int distance, C.SortMode sortMode, boolean smallestFirst, ArrayList<Integer> types) {

            int minDist = M.minDistance(distance);
            int maxDist = M.maxDistance(distance);

            String filter = selectionFilter(types);
            String[] colums = Contract.ExerciseEntry.EXERLITE_COLUMNS;
            //String selection = Contract.ExerciseEntry.COLUMN_DISTANCE + (D.includeLonger ? " >= " + minDist : " BETWEEN " + minDist + " AND " + maxDist);
            String selection = Contract.ExerciseEntry.COLUMN_DISTANCE + " >= " + minDist + filter;
            String drivenSelection = Contract.ExerciseEntry.COLUMN_DISTANCE + " = " + Exercise.DISTANCE_DRIVEN + filter;
            String orderBy = orderBy(sortMode, smallestFirst);

            Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, selection, null, null, null, orderBy, null);
            ArrayList<Exerlite> exerlites = unpackLiteCursor(cursor);
            cursor.close();

            Cursor drivenCursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, drivenSelection, null, null, null, orderBy, null);
            ArrayList<Exerlite> drivenExerlites = unpackLiteDrivenCursor(drivenCursor, distance);
            drivenCursor.close();

            exerlites.addAll(drivenExerlites);
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
            D.sortExerlites(exerlites, sortMode, smallestFirst);

            cursor.close();
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

        // get recs
        public ArrayList<Route> getRoutes(C.SortMode sortMode, boolean smallestFirst, boolean includeHidden) {

            String selection = includeHidden ? null : Contract.RouteEntry.COLUMN_HIDDEN + " = ?"; //+ 0 + " AND " + Contract.RouteEntry.COLUMN_AMOUNT + " >= " + 2;
            String[] selectionArgs = includeHidden ? null : new String[] { Integer.toString(0) };
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
        public int getRouteId(String name) {

            String[] columns = { Contract.RouteEntry._ID };
            String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
            String[] selectionArgs = { name };

            Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int _id = -1;
            while(cursor.moveToNext()) {
                _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
            }

            cursor.close();
            return _id;
        }
        public int getRouteIdOrCreate(String name, Context c) {

            String[] columns = { Contract.RouteEntry._ID };
            String selection = Contract.RouteEntry.COLUMN_NAME + " = ?";
            String[] selectionArgs = { name };

            Cursor cursor = db.query(Contract.RouteEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int _id = -1;
            while(cursor.moveToNext()) {
                _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RouteEntry._ID));
            }
            if (_id == -1) {
                Writer writer = new Writer(c);
                _id = (int) writer.addRoute(new Route(-1, name), c);
                writer.close();
            }

            cursor.close();
            return _id;
        }

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
            while(cursor.moveToNext()) {
                goalPace = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.DistanceEntry.COLUMN_GOAL_PACE));
            }

            cursor.close();
            return goalPace;
        }

        public ArrayList<String> getPolylines(int exceptId) {

            ArrayList<String> polylines = new ArrayList<>();

            String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
            String selection = Contract.ExerciseEntry._ID + " != " + exceptId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
            while (cursor.moveToNext()) {
                String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
                polylines.add(polyline);
            }
            cursor.close();

            return polylines;
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
        public ArrayList<String> getPolylinesByRouteExcept(int exceptRouteId) {

            ArrayList<String> polylines = new ArrayList<>();

            String[] columns = { Contract.ExerciseEntry.COLUMN_POLYLINE };
            String selection = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " != " + exceptRouteId + " AND " + Contract.ExerciseEntry.COLUMN_POLYLINE + " IS NOT NULL";

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, null);
            while (cursor.moveToNext()) {
                String polyline = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_POLYLINE));
                polylines.add(polyline);
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

        // get items
        public ArrayList<RouteItem> getRouteItems(ArrayList<String> rList) {

            ArrayList<RouteItem> routeItems = new ArrayList<>();
            for (String r : rList) {
                routeItems.add(getRouteItem(r));
            }

            return routeItems;
        }
        public ArrayList<RouteItem> getRouteItems(C.SortMode sortMode, boolean smallestFirst, boolean includeLesser, ArrayList<Integer> types) {

            /*String[] columns = { Contract.ExerciseEntry.COLUMN_ROUTE };
            String orderBy = orderBy(sortMode, smallestFirst);

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, null, null, null, null, orderBy);

            HashMap<String, Integer> names = new HashMap<>();
            ArrayList<RouteItem> routeItems = new ArrayList<>();

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
                if (names.containsKey(name)) {
                    int count = names.get(name);
                    if (!includeLesser && count == 1) routeItems.add(getRouteItem(name));
                    names.put(name, count + 1);
                }
                else {
                    if (includeLesser) routeItems.add(getRouteItem(name));
                    names.put(name, 1);
                }
            }*/

            ////

            /*ArrayList<Route> routes = getRoutes(sortMode, smallestFirst, includeLesser);
            ArrayList<RouteItem> routeItems = new ArrayList<>();

            for (Route route : routes) {
                RouteItem item = getRouteItem(route.getName());
                if (includeLesser || item.getCount() > 1) routeItems.add(item);
            }*/

            ////

            /*String[] columns = null;
            String selection = null;
            String [] selectionArgs = null;
            String groupBy = Contract.ExerciseEntry.COLUMN_ROUTE_ID;
            String orderBy = null;

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, groupBy, null, orderBy);
            ArrayList<RouteItem> routeItems = new ArrayList<>();
            cursor.close();*/

            final String e_t = Contract.ExerciseEntry.TABLE_NAME;
            final String r_t = Contract.RouteEntry.TABLE_NAME;
            final String e_dist = Contract.ExerciseEntry.COLUMN_DISTANCE;
            final String e_time = Contract.ExerciseEntry.COLUMN_TIME;
            final String r_name = Contract.RouteEntry.COLUMN_NAME;
            final String e_rid = Contract.ExerciseEntry.COLUMN_ROUTE_ID;
            final String e_rvar = Contract.ExerciseEntry.COLUMN_ROUTEVAR;
            final String e_date = Contract.ExerciseEntry.COLUMN_DATE;
            final String r_hidden = Contract.RouteEntry.COLUMN_HIDDEN;
            final String r_id = Contract.RouteEntry._ID;

            final String c_count = "antal";
            final String c_avg_dist = "avgDistance";
            final String c_min_pace = "minPace";

            String havingCount = includeLesser ? "" : " HAVING antal > 1";
            String whereHidden = includeLesser ? "" : " AND r."+r_hidden+" != 1";
            String whereTypes = selectionFilter(types);
            String whereTypesLone = selectionFilterFirst(types, "");
            String whereTypesAs = selectionFilterAs(types, "e2.");

            String orderBy;
            switch (sortMode) {
                case DATE: orderBy = "max("+e_date+")"; break;
                case NAME: orderBy = "max("+r_name+")"; break;
                case AMOUNT: orderBy = c_count; break;
                case DISTANCE: orderBy = c_avg_dist; break;
                case PACE: orderBy = c_min_pace; break;
                default: orderBy = "max("+e_date+")"; break;
            }
            orderBy += sortOrder(smallestFirst);

            String queryString = "select e."+e_rid+", r."+r_name+", "+c_count+", avg(e."+e_dist+") as "+c_avg_dist+", case when varPaceDrv is null or pace < varPaceDrv then pace else varPaceDrv end "+c_min_pace+" " +
                    "from "+e_t+" as e inner join "+r_t+" as r on e."+e_rid+" = r."+r_id+" inner join " +
                        "(select "+e_rid+", count(1) as "+c_count+" from "+e_t + whereTypesLone + " group by "+e_rid + havingCount +") as a on e."+e_rid+" = a."+e_rid+" inner join " +
                        "(select "+e_rid+", min("+e_time+"/"+e_dist+")*1000 as pace from "+e_t+" where "+e_dist+" > 0 and "+e_time+" != 0"+whereTypes+" group by "+e_rid+") as v on e."+e_rid+" = v."+e_rid+" left outer join " +
                        "(select e2."+e_rid+", min("+e_time+"/varDistAvg)*1000 as varPaceDrv from "+e_t+" as e2 inner join " +
                            "(select "+e_rid+", "+e_rvar+", avg("+e_dist+") as varDistAvg from "+e_t+" where "+e_dist+" > 0 and "+e_time+" != 0 group by "+e_rvar+", "+e_rid+") as vAvg on e2."+e_rid+" = vAvg."+e_rid+" and e2."+e_rvar+" = vAvg."+e_rvar+" " +
                        "where e2."+e_dist+" = -1 and e2."+e_time+" != 0 "+whereTypesAs+" group by e2."+e_rvar+", e2."+e_rid+") as vDrv on e."+e_rid+" = vDrv."+e_rid+" " +
                    "where e."+e_dist+" != -1" + whereHidden + whereTypes + " group by e."+e_rid+" order by " + orderBy;

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
        public RouteItem getRouteItem(String route) {

            String[] columns = { Contract.ExerciseEntry.COLUMN_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME, Contract.ExerciseEntry.COLUMN_ROUTEVAR };
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
                    String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                    distance = avgDistance(route, routeVar);
                }
                // bidra bara till avg om inte driven
                else {
                    totalDistanceCount++;
                    totalDistance += distance;
                }

                amount++;
                float pace = time / distance * 1000f;
                if (distance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) { bestPace = pace; }
            }
            int avgDistance = totalDistanceCount != 0 ? totalDistance / totalDistanceCount : 0;

            cursor.close();
            return new RouteItem(getRouteId(route), route, amount, avgDistance, bestPace);
        }
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
            for (Distance distance : distances) distanceItems.add(getDistanceItem(distance.getDistance(), types));

            /*ArrayList<Integer> dList = D.sortDistances(D.distances, smallestFirst, sortMode);
            for (int d : dList) {
                distanceItems.add(getDistanceItem(d));
            }*/

            return distanceItems;
        }
        public DistanceItem getDistanceItem(int distance, ArrayList<Integer> types) {

            int minDist = M.minDistance(distance);
            int maxDist = M.maxDistance(distance);

            String[] columns = { Contract.ExerciseEntry.COLUMN_DISTANCE, Contract.ExerciseEntry.COLUMN_TIME, Contract.ExerciseEntry.COLUMN_ROUTE, Contract.ExerciseEntry.COLUMN_ROUTEVAR };
            String selection = "(" + Contract.ExerciseEntry.COLUMN_DISTANCE + " >= " + minDist + " OR " + Contract.ExerciseEntry.COLUMN_DISTANCE + " = " + Exercise.DISTANCE_DRIVEN + ")" + selectionFilter(types);
            String orderBy = orderBy(C.SortMode.PACE, true);

            float bestPace = -1;

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, null, null, null, orderBy);
            while (cursor.moveToNext()) {
                int fullDistance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
                float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

                if (fullDistance == Exercise.DISTANCE_DRIVEN) {
                    String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
                    String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                    fullDistance = avgDistance(route, routeVar);
                    if (fullDistance < minDist /*|| fullDistance > maxDist*/) continue;
                }
                float pace = time / fullDistance * 1000f;
                if (fullDistance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) { bestPace = pace; }
            }
            float bestTimePerDistance = bestPace * distance / 1000;

            cursor.close();
            return new DistanceItem(distance, bestTimePerDistance, bestPace);

        }
        public IntervalItem getIntervalItem(String interval) {

            String[] columns = {};
            String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
            String[] selectionArgs = { interval };
            String orderBy = orderBy(C.SortMode.PACE, true);

            int amount = 0;

            Cursor cursor = db.query(Contract.ExerciseEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
            while (cursor.moveToNext()) { amount++; }

            cursor.close();
            return new IntervalItem(interval, amount);
        }
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

        // projections
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
                if (distance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) { bestPace = pace; }
            }
            int avgDistance = amount != 0 ? totalDistance / amount : 0;

            cursor.close();
            return amount + C.TAB + M.prefix(avgDistance, 1, "m") + C.TAB + M.stringTime(bestPace, true);
        }
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
                if (fullDistance != 0 && time != 0 && (pace < bestPace || bestPace == -1)) { bestPace = pace; }
            }
            float bestTimePerDistance = bestPace * distance / 1000;

            cursor.close();
            return M.stringTime(bestTimePerDistance, true) + C.TAB + M.stringTime(bestPace, true);
        }
        public int avgDistance(String route, String routeVar) {

            String[] colums = { Contract.ExerciseEntry.COLUMN_DISTANCE };
            String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?" + " AND " + Contract.ExerciseEntry.COLUMN_ROUTEVAR + " = ?";
            String[] selectionArgs = { route, routeVar };

            Cursor cursor = db.query(true, Contract.ExerciseEntry.TABLE_NAME, colums, selection, selectionArgs, null, null, null, null);
            int totalDistance = 0;
            int count = 0;
            while(cursor.moveToNext()) {
                int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
                if (distance != -1) { totalDistance += distance; count++; }
            }

            cursor.close();
            return count != 0 ? totalDistance / count : 0;
        }

        public TreeMap<Float, Float> weekDistance(ArrayList<Integer> types, LocalDate includingDate) {

            TreeMap<Float, Float> points = new TreeMap<>();
            TreeMap<Integer, Exerlite> map = new TreeMap<>();
            ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfWeek(includingDate), M.atEndOfWeek(includingDate), C.SortMode.DATE, false, types);

            for (Exerlite e : exerlites) {
                map.put(e.getDate().getDayOfWeek().getValue(), e);
            }

            for (int d = 1; d <= 7; d++) {
                points.put((float) d, map.containsKey(d) ? (float) map.get(d).getDistance() : 0);
            }

            return points;
        }
        public TreeMap<Float, Float> monthDistance(ArrayList<Integer> types, LocalDate includingDate) {

            TreeMap<Float, Float> points = new TreeMap<>();
            TreeMap<Integer, Exerlite> map = new TreeMap<>();
            ArrayList<Exerlite> exerlites = getExerlitesByDate(M.atStartOfMonth(includingDate), M.atEndOfMonth(includingDate), C.SortMode.DATE, false, types);

            float totalDistance = 0;

            for (Exerlite e : exerlites) {
                map.put(e.getDate().getDayOfMonth(), e);
            }
            if (map.size() == 0) return points;

            for (int d = 0; d <= includingDate.getMonth().length(includingDate.isLeapYear()); d++) {
                if (map.containsKey(d)) totalDistance += map.get(d).getDistance();
                points.put((float) d, totalDistance);
                if (d == map.lastKey() && includingDate.isEqual(LocalDate.now())) break;
            }

            return points;
        }
        public TreeMap<Float, Float> yearDistance(ArrayList<Integer> types, LocalDate includingDate) {

            TreeMap<Float, Float> points = new TreeMap<>();
            TreeMap<Integer, Exerlite> map = new TreeMap<>();
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
                float week = e.getWeek();
                if (week != lastWeek) {
                    points.put(lastWeek, totalDistance);
                    lastWeek = week;
                }
                totalDistance += e.getDistance();
            }

            return points;
        }

        public TreeMap<Float, Float> monthDistanceGoal(LocalDate includingDate) {

            TreeMap<Float, Float> points = new TreeMap<>();

            points.put(0f, 0f);
            points.put((float) includingDate.getMonth().length(includingDate.isLeapYear()), 126_000f);

            return points;
        }
        public TreeMap<Float, Float> yearDistanceGoal(LocalDate includingDate) {

            TreeMap<Float, Float> points = new TreeMap<>();

            points.put(0f, 0f);
            points.put(53f/*(float) includingDate.lengthOfYear()*/, 1_000_000f);

            return points;
        }

        // cursors
        private ArrayList<Exercise> unpackCursor(Cursor cursor) {

            ArrayList<Exercise> exercises = new ArrayList<>();

            while(cursor.moveToNext()) {
                int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ID));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TYPE));
                long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATE));
                int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_ID));
                //String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
                String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                String interval = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_INTERVAL));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_NOTE));
                String dataSource = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATASOURCE));
                String recordingMethod = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_RECORDINGMETHOD));
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
                String routeName = route == null ? "error" : getRoute(routeId).getName();

                Exercise exercise = new Exercise(_id, type, dateTime, routeId, routeName, routeVar, interval, note, dataSource, recordingMethod, distance, time, getSubs(_id), trail);
                exercises.add(exercise);
            }

            return exercises;
        }
        private ArrayList<Exerlite> unpackLiteCursor(Cursor cursor) {

            ArrayList<Exerlite> exerlites = new ArrayList<>();

            while(cursor.moveToNext()) {
                int _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry._ID));
                long epoch = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DATE));
                int routeId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE_ID));
                //String route = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTE));
                String interval = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_INTERVAL));
                int distance = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_DISTANCE));
                float time = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_TIME));

                // convert
                LocalDate date = M.ofEpoch(epoch).toLocalDate();
                if (interval == null) interval = "";

                Route route = getRoute(routeId);
                String routeName = route == null ? "error" : getRoute(routeId).getName();

                // distance driven
                boolean distanceDriven = distance == Exercise.DISTANCE_DRIVEN;
                if (distanceDriven) {
                    String routeVar = cursor.getString(cursor.getColumnIndexOrThrow(Contract.ExerciseEntry.COLUMN_ROUTEVAR));
                    distance = avgDistance(routeName, routeVar);
                }

                // subs
                if (distance == 0 && time == 0) {
                    String selection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
                    String[] selectionArgs = { Integer.toString(_id) };
                    String[] columns = { Contract.SubEntry.COLUMN_DISTANCE, Contract.SubEntry.COLUMN_TIME };

                    Cursor subCursor = db.query(Contract.SubEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                    while(subCursor.moveToNext()) {
                        int subDistance = subCursor.getInt(subCursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_DISTANCE));
                        float subTime = subCursor.getInt(subCursor.getColumnIndexOrThrow(Contract.SubEntry.COLUMN_TIME));
                        distance += subDistance;
                        time += subTime;
                    }
                    subCursor.close();
                }

                Exerlite exerlite = new Exerlite(_id, date, routeName, interval, distance, time, distanceDriven);
                exerlites.add(exerlite);
            }

            return exerlites;
        }
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

            while(cursor.moveToNext()) {
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

            while(cursor.moveToNext()) {
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

            while(cursor.moveToNext()) {
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
        private String selectionFilter(ArrayList<Integer> visibleTypes) {

            String filter = "";
            for (int i = 0; i < visibleTypes.size(); i++) {
                if (i == 0) filter += " AND (";
                filter += Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
                if (i == visibleTypes.size()-1) filter += ")";
                else filter += " OR ";
            }
            return filter;
        }
        private String selectionFilterAs(ArrayList<Integer> visibleTypes, String tableAsName) {

            String filter = "";
            for (int i = 0; i < visibleTypes.size(); i++) {
                if (i == 0) filter += " AND (";
                filter += tableAsName + Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
                if (i == visibleTypes.size()-1) filter += ")";
                else filter += " OR ";
            }
            return filter;
        }
        private String selectionFilterFirst(ArrayList<Integer> visibleTypes, String tableAsName) {

            String filter = "";
            for (int i = 0; i < visibleTypes.size(); i++) {
                if (i == 0) filter += " WHERE (";
                filter += tableAsName + Contract.ExerciseEntry.COLUMN_TYPE + " = " + visibleTypes.get(i);
                if (i == visibleTypes.size()-1) filter += ")";
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

}
