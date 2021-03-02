package com.example.trackfield.database;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.objects.Route;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.L;

import java.util.ArrayList;

public class Writer extends Helper {

    private static Writer instance;
    private static boolean useUpdateTool = false;

    ////

    private Writer(Context context) {
        super(context);
        db = getWritableDatabase();
    }

    /**
     * Gets the current writer instance, or creates if null or closed
     * @return Writer Instance
     */
    @NonNull
    public static Writer get(Context c) {
        if (instance == null || !instance.db.isOpen()) instance = new Writer(c);
        return instance;
    }

    // database tools

    public void recreate() {
        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
    }

    public void useUpdateToolIfEnabled(Context c) {
        if (!useUpdateTool) return;

        recreate();

        useUpdateTool = false;
    }

    @Deprecated
    public void importFromToolbox(Context c) {
        L.toast(deleteAllExercises() && addExercises(D.exercises, c), c);
    }

    // exercises

    public boolean addExercises(ArrayList<Exercise> exercises, Context c) {
        boolean success = true;
        for (Exercise e : exercises) {
            success &= addExercise(e, c);
        }
        return success;
    }

    public boolean addExercise(Exercise e, Context c) {
        e.setRouteId((int) addRouteIfNotAdded(new Route(e.getRouteId(), e.getRoute()), c));

        final ContentValues cv = fillExerciseContentValues(e);

        final long _id = db.insert(Contract.ExerciseEntry.TABLE_NAME, null, cv);
        e.setSubs_superId((int) _id);
        final boolean subSuccess = addSubs(e.getSubs());
        //final boolean mapSuccess = addMap(e.getMap());

        return success(_id) && subSuccess; //&& mapSuccess;
    }

    public boolean updateExercise(Exercise e) {
        ContentValues newCv = fillExerciseContentValues(e);

        String selection = Contract.ExerciseEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(e.get_id()) };

        final int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);
        final boolean subSuccess = updateSubs(e.getSubs());
        //final boolean mapSuccess = updateMap(e.getMap());

        return count > 0 && subSuccess;// && mapSuccess;
    }

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

    // subs

    private boolean addSubs(ArrayList<Sub> subs) {
        boolean success = true;
        for (Sub sub : subs) {
            success &= addSub(sub);
        }
        return success;
    }

    private boolean addSub(Sub sub) {
        final ContentValues cvSub = fillSubContentValues(sub);
        final long result = db.insert(Contract.SubEntry.TABLE_NAME, null, cvSub);
        return success(result);
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
            else {
                success &= addSub(sub);
            }
        }

        return success;
    }

    public boolean deleteSub(Sub sub) {
        final String selection = Contract.SubEntry._ID + " = ?";
        final String[] selectionArgs = { Integer.toString(sub.get_id()) };

        final long result = db.delete(Contract.SubEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    // distances

    public boolean addDistances(ArrayList<Distance> distances) {
        boolean success = true;
        for (Distance d : distances) {
            success &= addDistance(d);
        }
        return success;
    }

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

    // routes

    public void addRoutes(ArrayList<Route> routes, Context c) {
        for (Route r : routes) addRouteIfNotAdded(r, c);
    }

    public long addRouteIfNotAdded(Route route, Context c) {
        Route existingRoute = Reader.get(c).getRoute(route.getName());
        if (existingRoute != null) return existingRoute.get_id();

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

    public boolean updateRouteName(String oldRoute, String newRoute) {
        ContentValues newCv = new ContentValues();
        newCv.put(Contract.ExerciseEntry.COLUMN_ROUTE, newRoute);

        String selection = Contract.ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { oldRoute };

        int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    // intervals

    public boolean updateInterval(String oldInterval, String newInterval) {
        ContentValues newCv = new ContentValues();
        newCv.put(Contract.ExerciseEntry.COLUMN_INTERVAL, newInterval);

        String selection = Contract.ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { oldInterval };

        int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    // fill ContentValues

    private ContentValues fillExerciseContentValues(Exercise e) {
        ContentValues cv = new ContentValues();

        cv.put(Contract.ExerciseEntry.COLUMN_EXTERNAL_ID, e.getExternalId());
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
        cv.put(Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, e.distance()); // TODO: eller effectiveDistance??
        cv.put(Contract.ExerciseEntry.COLUMN_TIME, e.getTimePrimary());

        Trail trail = e.getTrail();
        if (trail != null) {
            cv.put(Contract.ExerciseEntry.COLUMN_POLYLINE, trail.getPolyline());
            if (trail.hasStartEnd()) {
                cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, trail.getStartLat());
                cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, trail.getStartLng());
                cv.put(Contract.ExerciseEntry.COLUMN_END_LAT, trail.getEndLat());
                cv.put(Contract.ExerciseEntry.COLUMN_END_LNG, trail.getEndLng());
            }
            else {
                cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_END_LAT, (Double) null);
                cv.put(Contract.ExerciseEntry.COLUMN_END_LNG, (Double) null);
            }
        }
        else {
            cv.put(Contract.ExerciseEntry.COLUMN_START_LAT, (Double) null);
            cv.put(Contract.ExerciseEntry.COLUMN_START_LNG, (Double) null);
            cv.put(Contract.ExerciseEntry.COLUMN_END_LAT, (Double) null);
            cv.put(Contract.ExerciseEntry.COLUMN_END_LNG, (Double) null);
            cv.put(Contract.ExerciseEntry.COLUMN_POLYLINE, (String) null);
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
