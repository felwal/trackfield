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

    public boolean addExercises(@NonNull ArrayList<Exercise> exercises, Context c) {
        boolean success = true;
        for (Exercise e : exercises) {
            success &= addExercise(e, c);
        }
        return success;
    }

    /**
     * Adds an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)}
     *
     * @param e The exercise to add
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean addExercise(@NonNull Exercise e, Context c) {
        e.setRouteId((int) addRouteIfNotAdded(new Route(e.getRouteId(), e.getRoute()), c));

        final ContentValues cv = fillExerciseContentValues(e);

        final long _id = db.insert(Contract.ExerciseEntry.TABLE_NAME, null, cv);
        e.setSubs_superId((int) _id);
        final boolean subSuccess = addSubs(e.getSubs());

        // must be called to keep effective distance current
        updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);

        return success(_id) && subSuccess;
    }

    /**
     * Updates an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)} when changed routeId, routeVar or distance
     *
     * @param e The exercise to update
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean updateExercise(@NonNull Exercise e, Context c) {
        Exercise old = Reader.get(c).getExercise(e.get_id());
        ContentValues newCv = fillExerciseContentValues(e);

        String where = Contract.ExerciseEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(e.get_id()) };

        final int count = db.update(Contract.ExerciseEntry.TABLE_NAME, newCv, where, whereArgs);
        final boolean subSuccess = updateSubs(e.getSubs());

        // update effective distance if routeId, routeVar or distance updated
        if (old.getRouteId() != e.getRouteId() || !old.getRouteVar().equals(e.getRouteVar())) {
            updateEffectiveDistance(old.getRouteId(), old.getRouteVar(), c);
            updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);
        }
        else if (old.getDistancePrimary() != e.getDistancePrimary()) {
            updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);
        }

        return count > 0 && subSuccess;
    }

    /**
     * Deletes an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)}
     *
     * @param e The exercise to delete
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean deleteExercise(@NonNull Exercise e, Context c) {
        final String selection = Contract.ExerciseEntry._ID + " = ?";
        final String subSelection = Contract.SubEntry.COLUMN_SUPERID + " = ?";
        final String[] selectionArgs = { Integer.toString(e.get_id()) };

        final long result = db.delete(Contract.ExerciseEntry.TABLE_NAME, selection, selectionArgs);
        final long subResult = db.delete(Contract.SubEntry.TABLE_NAME, subSelection, selectionArgs);

        // route
        /*Reader reader = new Reader(c);
        if (reader.getExerlitesByRoute(e.getRouteId(), C.SortMode.DATE, false).size() == 0) deleteRoute(e.getRouteId());
        reader.close();*/

        // must be called to keep effective distance current
        updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);

        return success(result) && success(subResult);
    }

    public boolean deleteAllExercises() {
        final long result = db.delete(Contract.ExerciseEntry.TABLE_NAME, null, null);
        final long subResult = db.delete(Contract.SubEntry.TABLE_NAME, null, null);

        return success(result) && success(subResult);
    }

    // single columns

    /**
     * Updates effective distance for all exercises having routeId and routeVar
     * <p>Is called in:
     * <p>1) {@link #addExercise(Exercise, Context)} when an exercise is created
     * <p>2) {@link #deleteExercise(Exercise, Context)} when an exercise is deleted
     * <p>3) {@link #updateExercise(Exercise, Context)} when distance of an exercise is edited and (twice) when rotue or routeVar of is edited
     *
     * @param routeId The routeId to edit effective distance of
     * @param routeVar The routeVar to edit effective distance of
     * @param c Context
     */
    private boolean updateEffectiveDistance(int routeId, String routeVar, Context c) {
        int effectiveDistance = Reader.get(c).avgDistance(routeId, routeVar);

        ContentValues cv = new ContentValues();
        cv.put(Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, effectiveDistance);

        String where = Contract.ExerciseEntry.COLUMN_ROUTE_ID + " = ? AND " + Contract.ExerciseEntry.COLUMN_ROUTE_VAR + " = ? AND " + Contract.ExerciseEntry.COLUMN_DISTANCE + " = ?";
        String[] whereArgs = { Integer.toString(routeId), routeVar, Integer.toString(Exercise.DISTANCE_DRIVEN) };

        int count = db.update(Contract.ExerciseEntry.TABLE_NAME, cv, where, whereArgs);

        return success(count);
    }

    // subs

    private boolean addSubs(@NonNull ArrayList<Sub> subs) {
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

    private boolean updateSubs(@NonNull ArrayList<Sub> subs) {
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

    public boolean deleteSub(@NonNull Sub sub) {
        final String selection = Contract.SubEntry._ID + " = ?";
        final String[] selectionArgs = { Integer.toString(sub.get_id()) };

        final long result = db.delete(Contract.SubEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    // distances

    public boolean addDistances(@NonNull ArrayList<Distance> distances) {
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

    public boolean deleteDistance(@NonNull Distance distance) {
        final String selection = Contract.DistanceEntry.COLUMN_DISTANCE + " = ?";
        final String[] selectionArgs = { Integer.toString(distance.getDistance()) };

        final long result = db.delete(Contract.DistanceEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    // routes

    public void addRoutes(@NonNull ArrayList<Route> routes, Context c) {
        for (Route r : routes) addRouteIfNotAdded(r, c);
    }

    public long addRouteIfNotAdded(@NonNull Route route, Context c) {
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

    @Deprecated
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

    @NonNull
    private ContentValues fillExerciseContentValues(@NonNull Exercise e) {
        ContentValues cv = new ContentValues();

        cv.put(Contract.ExerciseEntry.COLUMN_EXTERNAL_ID, e.getExternalId());
        cv.put(Contract.ExerciseEntry.COLUMN_TYPE, e.getType());
        cv.put(Contract.ExerciseEntry.COLUMN_DATE, e.getEpoch());
        cv.put(Contract.ExerciseEntry.COLUMN_ROUTE_ID, e.getRouteId());
        cv.put(Contract.ExerciseEntry.COLUMN_ROUTE, e.getRoute());
        cv.put(Contract.ExerciseEntry.COLUMN_ROUTE_VAR, e.getRouteVar());
        cv.put(Contract.ExerciseEntry.COLUMN_INTERVAL, e.getInterval());
        cv.put(Contract.ExerciseEntry.COLUMN_NOTE, e.getNote());
        cv.put(Contract.ExerciseEntry.COLUMN_DATA_SOURCE, e.getDataSource());
        cv.put(Contract.ExerciseEntry.COLUMN_RECORDING_METHOD, e.getRecordingMethod());
        cv.put(Contract.ExerciseEntry.COLUMN_DISTANCE, e.getDistancePrimary());
        cv.put(Contract.ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, e.distance()); // TODO: eller effectiveDistance?
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

    @NonNull
    private ContentValues fillSubContentValues(@NonNull Sub sub) {
        ContentValues cv = new ContentValues();

        cv.put(Contract.SubEntry.COLUMN_SUPERID, sub.get_superId());
        cv.put(Contract.SubEntry.COLUMN_DISTANCE, sub.getDistance());
        cv.put(Contract.SubEntry.COLUMN_TIME, sub.getTime());

        return cv;
    }

    @NonNull
    private ContentValues fillDistanceContentValues(@NonNull Distance distance) {
        ContentValues cv = new ContentValues();

        cv.put(Contract.DistanceEntry.COLUMN_DISTANCE, distance.getDistance());
        cv.put(Contract.DistanceEntry.COLUMN_GOAL_PACE, distance.getGoalPace());

        return cv;
    }

    @NonNull
    private ContentValues fillRouteContentValues(@NonNull Route route) {
        ContentValues cv = new ContentValues();

        cv.put(Contract.RouteEntry.COLUMN_NAME, route.getName());
        cv.put(Contract.RouteEntry.COLUMN_HIDDEN, route.isHidden() ? 1 : 0);
        cv.put(Contract.RouteEntry.COLUMN_GOAL_PACE, route.getGoalPace());

        return cv;
    }

    // query tools

    private boolean success(long dbResult) {
        return dbResult != -1;
    }

}
