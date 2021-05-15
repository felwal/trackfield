package com.example.trackfield.data.db;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.trackfield.data.db.DbContract.*;
import com.example.trackfield.data.db.model.Distance;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.data.db.model.Route;
import com.example.trackfield.data.db.model.Sub;
import com.example.trackfield.ui.map.model.Trail;
import com.example.trackfield.utils.model.SortMode;

import java.util.ArrayList;

public class DbWriter extends DbHelper {

    private static DbWriter instance;
    private static boolean useUpdateTool = false;

    //

    private DbWriter(Context context) {
        super(context);
        db = getWritableDatabase();
    }

    /**
     * Gets the current writer instance, or creates if null or closed.
     *
     * @return {@link DbWriter} instance
     */
    @NonNull
    public static DbWriter get(Context c) {
        if (instance == null || !instance.db.isOpen()) instance = new DbWriter(c);
        return instance;
    }

    // recreate

    public void recreate() {
        onUpgrade(db, 0, DATABASE_TARGET_VERSION);
    }

    public void recreate(int toVersion) {
        onUpgrade(db, 0, Math.min(toVersion, DATABASE_TARGET_VERSION));
    }

    public void upgradeToTargetVersion(int oldVersion) {
        onUpgrade(db, oldVersion, DATABASE_TARGET_VERSION);
    }

    // database tools

    public void useUpdateToolIfEnabled(Context c) {
        if (!useUpdateTool) return;
        recreate();
        useUpdateTool = false;
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
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)}</p>
     *
     * @param e The exercise to add
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean addExercise(@NonNull Exercise e, Context c) {
        int routeId = DbReader.get(c).getRouteId(e.getRoute());
        e.setRouteId((int) addRoute(new Route(routeId, e.getRoute()), c));

        final ContentValues cv = fillExerciseContentValues(e);

        final long _id = db.insert(ExerciseEntry.TABLE_NAME, null, cv);
        e.setSubs_superId((int) _id);
        final boolean subSuccess = addSubs(e.getSubs());

        // must be called to keep effective distance current
        updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);

        return success(_id) && subSuccess;
    }

    /**
     * Updates an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)} when changed routeId, routeVar or
     * distance</p>
     *
     * @param e The exercise to update
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean updateExercise(@NonNull Exercise e, Context c) {
        Exercise old = DbReader.get(c).getExercise(e.get_id());
        ContentValues newCv = fillExerciseContentValues(e);

        String where = ExerciseEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(e.get_id()) };

        final int count = db.update(ExerciseEntry.TABLE_NAME, newCv, where, whereArgs);
        final boolean subSuccess = updateSubs(e.getSubs());

        // delete route if changed and empty
        if (old.getRouteId() != e.getRouteId()) {
            deleteRouteIfEmpty(old.getRouteId(), c);
        }

        // update effective distance if routeId, routeVar or distance updated
        if (old.getRouteId() != e.getRouteId() || !old.getRouteVar().equals(e.getRouteVar())) {
            updateEffectiveDistance(old.getRouteId(), old.getRouteVar(), c);
            updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);
        }
        else if (old.getDistance() != e.getDistance()) {
            updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);
        }

        return count > 0 && subSuccess;
    }

    /**
     * Deletes an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)}</p>
     *
     * @param e The exercise to delete
     * @param c Context
     * @return True if the exercise was added successfully
     */
    public boolean deleteExercise(@NonNull Exercise e, Context c) {
        final String selection = ExerciseEntry._ID + " = ?";
        final String subSelection = SubEntry.COLUMN_SUPERID + " = ?";
        final String[] selectionArgs = { Integer.toString(e.get_id()) };

        final long result = db.delete(ExerciseEntry.TABLE_NAME, selection, selectionArgs);
        final long subResult = db.delete(SubEntry.TABLE_NAME, subSelection, selectionArgs);

        // route
        deleteRouteIfEmpty(e.getRouteId(), c);

        // effective distance
        updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);

        return success(result) && success(subResult);
    }

    // single columns

    /**
     * Updates effective distance for all exercises having routeId and routeVar
     * <p>Must be called in:
     * <ul>
     *     <li>{@link #addExercise(Exercise, Context)} when an exercise is created</li>
     *     <li>{@link #deleteExercise(Exercise, Context)} when an exercise is deleted</li>
     *     <li>{@link #updateExercise(Exercise, Context)} when distance of an exercise is edited
     *     and(twice) when rotue or routeVar of is edited</li>
     * </ul></p>
     *
     * @param routeId The routeId to edit effective distance of
     * @param routeVar The routeVar to edit effective distance of
     * @param c Context
     * @return True if operaton successful
     */
    private boolean updateEffectiveDistance(int routeId, String routeVar, Context c) {
        int effectiveDistance = DbReader.get(c)
            .avgDistance(routeId, routeVar);

        ContentValues cv = new ContentValues();
        cv.put(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, effectiveDistance);

        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = ? AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " = ? AND " +
            ExerciseEntry.COLUMN_DISTANCE + " = ?";
        String[] whereArgs = { Integer.toString(routeId), routeVar, Integer.toString(Exercise.DISTANCE_DRIVEN) };

        int count = db.update(ExerciseEntry.TABLE_NAME, cv, where, whereArgs);

        return success(count);
    }

    /**
     * Checks whether a route has no referencing exercíses, deletes the route if true
     * <p>Must be called when:
     * <ul>
     *     <li>the route of an exercise is edited in {@link #updateExercise(Exercise, Context)}</li>
     *     <li>an exercise is deleted in {@link #deleteExercise(Exercise, Context)}</li>
     * </ul></p>
     *
     * @param routeId RouteId of the route to check
     * @param c Context
     * @return True if the route was empty and successfully deleted
     */
    private boolean deleteRouteIfEmpty(int routeId, Context c) {
        int remainingOfRoute = DbReader.get(c)
            .getExerlitesByRoute(routeId, SortMode.Mode.DATE, false, new ArrayList<>())
            .size();
        if (remainingOfRoute == 0) return deleteRoute(routeId);
        return false;
    }

    /**
     * Cleans database routes table from unused routes. **Should not be called**, unless as a one-time operation.
     *
     * @param c Context
     * @return True if operation successful
     */
    public boolean deleteEmptyRoutes(Context c) {
        boolean success = true;
        for (Route route : DbReader.get(c)
            .getRoutes(true)) {
            success &= deleteRouteIfEmpty(route.get_id(), c);
        }
        return success;
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
        final long result = db.insert(SubEntry.TABLE_NAME, null, cvSub);
        return success(result);
    }

    private boolean updateSubs(@NonNull ArrayList<Sub> subs) {
        boolean success = true;
        for (Sub sub : subs) {

            if (sub.get_id() != -1) {
                ContentValues newCv = fillSubContentValues(sub);
                String selection = SubEntry._ID + " = ?";
                String[] selectionArgs = { Integer.toString(sub.get_id()) };

                int count = db.update(SubEntry.TABLE_NAME, newCv, selection, selectionArgs);
                success &= count > 0;
            }
            else {
                success &= addSub(sub);
            }
        }

        return success;
    }

    public boolean deleteSub(@NonNull Sub sub) {
        final String selection = SubEntry._ID + " = ?";
        final String[] selectionArgs = { Integer.toString(sub.get_id()) };

        final long result = db.delete(SubEntry.TABLE_NAME, selection, selectionArgs);

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
        final long result = db.insert(DistanceEntry.TABLE_NAME, null, cv);
        return success(result);
    }

    public boolean updateDistance(Distance distance) {
        ContentValues newCv = fillDistanceContentValues(distance);

        String selection = DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString((distance.getDistance())) };

        final int count = db.update(DistanceEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    public boolean deleteDistance(@NonNull Distance distance) {
        final String selection = DistanceEntry.COLUMN_DISTANCE + " = ?";
        final String[] selectionArgs = { Integer.toString(distance.getDistance()) };

        final long result = db.delete(DistanceEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    // routes

    public void addRoutes(@NonNull ArrayList<Route> routes, Context c) {
        for (Route r : routes) addRoute(r, c);
    }

    /**
     * Adds a rotue if not already added; if the route's routeName doesn't already exist. This does NOT update any
     * existing rows.
     *
     * @param route The route to add.
     * @param c Context
     * @return The routeId of the added or already existing route
     */
    public long addRoute(@NonNull Route route, Context c) {
        Route existingRoute = DbReader.get(c).getRoute(route.getName());
        if (existingRoute != null) return existingRoute.get_id();

        final ContentValues cv = fillRouteContentValues(route);
        final long _id = db.insert(RouteEntry.TABLE_NAME, null, cv);
        return _id;
    }

    /**
     * Updates a route. Merges with existing route if new name already exists.
     *
     * @param route Route to update
     * @return The routeId of the updated route; of mergee if merged, same as parameter otherwise
     */
    public int updateRoute(Route route) {
        Route oldRoute = DbReader.get().getRoute(route.get_id());
        int existingIdForNewName = DbReader.get().getRouteId(route.getName());

        boolean nameNotChanged = route.getName().equals(oldRoute.getName());
        boolean newNameFree = existingIdForNewName == Route.ID_NON_EXISTANT;
        boolean dontMerge = nameNotChanged || newNameFree;

        // update route
        if (dontMerge) {
            ContentValues newCv = fillRouteContentValues(route);

            String selection = RouteEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(route.get_id()) };

            final int count = db.update(RouteEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return route.get_id();
            //return count > 0;
        }

        // merge routes, update routeId and delete merger
        else {
            // update routeId to mergeree routeId
            ContentValues newCv = new ContentValues();
            newCv.put(ExerciseEntry.COLUMN_ROUTE_ID, existingIdForNewName);

            String where = ExerciseEntry.COLUMN_ROUTE_ID + " = ?";
            String[] whereArgs = { Integer.toString(route.get_id()) };

            final int count = db.update(ExerciseEntry.TABLE_NAME, newCv, where, whereArgs);

            // delete merger route
            boolean deleteSuccess = deleteRoute(route.get_id());

            return existingIdForNewName;
            //return count > 0 && deleteSuccess;
        }
    }

    public boolean deleteRoute(int routeId) {
        final String selection = RouteEntry._ID + " = ?";
        final String[] selectionArgs = { Integer.toString(routeId) };

        final long result = db.delete(RouteEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    /**
     * Update route name in exercises table. Call whenever updating route name via {@link #updateRoute(Route)}. TODO:
     * remove when routeName column is removed
     */
    @Deprecated
    public boolean updateRouteName(String oldName, String newName) {
        //if (newName.equals(oldName)) return true;

        ContentValues newCv = new ContentValues();
        newCv.put(ExerciseEntry.COLUMN_ROUTE, newName);

        String selection = ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { oldName };

        int count = db.update(ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    // intervals

    public boolean updateInterval(String oldInterval, String newInterval) {
        ContentValues newCv = new ContentValues();
        newCv.put(ExerciseEntry.COLUMN_INTERVAL, newInterval);

        String selection = ExerciseEntry.COLUMN_INTERVAL + " = ?";
        String[] selectionArgs = { oldInterval };

        int count = db.update(ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    // fill ContentValues

    @NonNull
    private ContentValues fillExerciseContentValues(@NonNull Exercise e) {
        ContentValues cv = new ContentValues();

        cv.put(ExerciseEntry.COLUMN_EXTERNAL_ID, e.getExternalId());
        cv.put(ExerciseEntry.COLUMN_TYPE, e.getType());
        cv.put(ExerciseEntry.COLUMN_DATE, e.getEpoch());
        cv.put(ExerciseEntry.COLUMN_ROUTE_ID, e.getRouteId());
        cv.put(ExerciseEntry.COLUMN_ROUTE, e.getRoute());
        cv.put(ExerciseEntry.COLUMN_ROUTE_VAR, e.getRouteVar());
        cv.put(ExerciseEntry.COLUMN_INTERVAL, e.getInterval());
        cv.put(ExerciseEntry.COLUMN_NOTE, e.getNote());
        cv.put(ExerciseEntry.COLUMN_DATA_SOURCE, e.getDataSource());
        cv.put(ExerciseEntry.COLUMN_RECORDING_METHOD, e.getRecordingMethod());
        cv.put(ExerciseEntry.COLUMN_DISTANCE, e.getDistance());
        cv.put(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, e.getEffectiveDistance());
        cv.put(ExerciseEntry.COLUMN_TIME, e.getTimePrimary());

        Trail trail = e.getTrail();
        if (trail != null) {
            cv.put(ExerciseEntry.COLUMN_POLYLINE, trail.getPolyline());
            if (trail.hasStartEnd()) {
                cv.put(ExerciseEntry.COLUMN_START_LAT, trail.getStartLat());
                cv.put(ExerciseEntry.COLUMN_START_LNG, trail.getStartLng());
                cv.put(ExerciseEntry.COLUMN_END_LAT, trail.getEndLat());
                cv.put(ExerciseEntry.COLUMN_END_LNG, trail.getEndLng());
            }
            else {
                cv.put(ExerciseEntry.COLUMN_START_LAT, (Double) null);
                cv.put(ExerciseEntry.COLUMN_START_LNG, (Double) null);
                cv.put(ExerciseEntry.COLUMN_END_LAT, (Double) null);
                cv.put(ExerciseEntry.COLUMN_END_LNG, (Double) null);
            }
        }
        else {
            cv.put(ExerciseEntry.COLUMN_START_LAT, (Double) null);
            cv.put(ExerciseEntry.COLUMN_START_LNG, (Double) null);
            cv.put(ExerciseEntry.COLUMN_END_LAT, (Double) null);
            cv.put(ExerciseEntry.COLUMN_END_LNG, (Double) null);
            cv.put(ExerciseEntry.COLUMN_POLYLINE, (String) null);
        }

        return cv;
    }

    @NonNull
    private ContentValues fillSubContentValues(@NonNull Sub sub) {
        ContentValues cv = new ContentValues();

        cv.put(SubEntry.COLUMN_SUPERID, sub.get_superId());
        cv.put(SubEntry.COLUMN_DISTANCE, sub.getDistance());
        cv.put(SubEntry.COLUMN_TIME, sub.getTime());

        return cv;
    }

    @NonNull
    private ContentValues fillDistanceContentValues(@NonNull Distance distance) {
        ContentValues cv = new ContentValues();

        cv.put(DistanceEntry.COLUMN_DISTANCE, distance.getDistance());
        cv.put(DistanceEntry.COLUMN_GOAL_PACE, distance.getGoalPace());

        return cv;
    }

    @NonNull
    private ContentValues fillRouteContentValues(@NonNull Route route) {
        ContentValues cv = new ContentValues();

        if (route.get_id() != Route.ID_NON_EXISTANT) {
            cv.put(RouteEntry._ID, route.get_id());
        }
        cv.put(RouteEntry.COLUMN_NAME, route.getName());
        cv.put(RouteEntry.COLUMN_HIDDEN, route.isHidden() ? 1 : 0);
        cv.put(RouteEntry.COLUMN_GOAL_PACE, route.getGoalPace());

        return cv;
    }

    // query tools

    private boolean success(long dbResult) {
        return dbResult != -1;
    }

}
