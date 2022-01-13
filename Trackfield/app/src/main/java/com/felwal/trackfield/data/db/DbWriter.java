package com.felwal.trackfield.data.db;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import com.felwal.trackfield.data.db.DbContract.*;
import com.felwal.trackfield.data.db.model.Distance;
import com.felwal.trackfield.data.db.model.Exercise;
import com.felwal.trackfield.data.db.model.Place;
import com.felwal.trackfield.data.db.model.Route;
import com.felwal.trackfield.data.db.model.Sub;
import com.felwal.trackfield.ui.common.model.SorterItem;
import com.felwal.trackfield.ui.map.model.Trail;
import com.felwal.trackfield.utils.annotation.Debug;
import com.felwal.trackfield.utils.annotation.Unimplemented;

import java.util.ArrayList;

public class DbWriter extends DbHelper {

    private static DbWriter instance;
    @Debug private static boolean useUpdateTool = false;

    //

    private DbWriter(Context c) {
        super(c.getApplicationContext());
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

    @Debug
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
     * @return True if the exercise was added successfully
     */
    public boolean addExercise(@NonNull Exercise e, Context c) {
        int routeId = DbReader.get(c).getRouteId(e.getRoute());
        e.setRouteId((int) addRoute(new Route(routeId, e.getRoute()), c));

        ContentValues cv = fillExerciseContentValues(e, c);

        long id = db.insert(ExerciseEntry.TABLE_NAME, null, cv);
        e.setSubsSuperId((int) id);
        final boolean subSuccess = addSubs(e.getSubs());

        // must be called to keep effective distance current
        updateEffectiveDistance(e.getRouteId(), e.getRouteVar(), c);

        return success(id) && subSuccess;
    }

    /**
     * Updates an exercise
     * <p>Internally calls {@link #updateEffectiveDistance(int, String, Context)} when changed routeId, routeVar or
     * distance</p>
     *
     * @param e The exercise to update
     * @return True if the exercise was added successfully
     */
    public boolean updateExercise(@NonNull Exercise e, Context c) {
        Exercise old = DbReader.get(c).getExercise(e.getId());
        ContentValues newCv = fillExerciseContentValues(e, c);

        String where = ExerciseEntry._ID + " = ?";
        String[] whereArgs = { Integer.toString(e.getId()) };

        int count = db.update(ExerciseEntry.TABLE_NAME, newCv, where, whereArgs);
        boolean subSuccess = updateSubs(e.getSubs());

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
     * @return True if the exercise was added successfully
     */
    public boolean deleteExercise(@NonNull Exercise e, Context c) {
        String selection = ExerciseEntry._ID + " = ?";
        String subSelection = SubEntry.COLUMN_SUPERID + " = ?";
        String[] selectionArgs = { Integer.toString(e.getId()) };

        long result = db.delete(ExerciseEntry.TABLE_NAME, selection, selectionArgs);
        long subResult = db.delete(SubEntry.TABLE_NAME, subSelection, selectionArgs);

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
     * @return True if operaton successful
     */
    private boolean updateEffectiveDistance(int routeId, String routeVar, Context c) {
        int effectiveDistance = DbReader.get(c).avgDistance(routeId, routeVar);

        ContentValues cv = new ContentValues();
        cv.put(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, effectiveDistance);

        String where = ExerciseEntry.COLUMN_ROUTE_ID + " = ? AND " + ExerciseEntry.COLUMN_ROUTE_VAR + " = ?" +
            " AND " + ExerciseEntry.COLUMN_DISTANCE + " = ?";
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
     * @return True if the route was empty and successfully deleted
     */
    private boolean deleteRouteIfEmpty(int routeId, Context c) {
        int remainingOfRoute = DbReader.get(c)
            .getExerlitesByRoute(routeId, SorterItem.Mode.DATE, false, new ArrayList<>()).size();
        if (remainingOfRoute == 0) return deleteRoute(routeId);
        return false;
    }

    /**
     * Cleans database routes table from unused routes. **Should not be called**, unless as a one-time operation.
     *
     * @return True if operation successful
     */
    @Debug
    public boolean deleteEmptyRoutes(Context c) {
        boolean success = true;
        for (Route route : DbReader.get(c).getRoutes(true)) {
            success &= deleteRouteIfEmpty(route.getId(), c);
        }
        return success;
    }

    // subs

    @Unimplemented
    private boolean addSubs(@NonNull ArrayList<Sub> subs) {
        boolean success = true;
        for (Sub sub : subs) {
            success &= addSub(sub);
        }
        return success;
    }

    @Unimplemented
    private boolean addSub(Sub sub) {
        ContentValues cvSub = fillSubContentValues(sub);
        long result = db.insert(SubEntry.TABLE_NAME, null, cvSub);
        return success(result);
    }

    @Unimplemented
    private boolean updateSubs(@NonNull ArrayList<Sub> subs) {
        boolean success = true;
        for (Sub sub : subs) {

            if (sub.getId() != -1) {
                ContentValues newCv = fillSubContentValues(sub);
                String selection = SubEntry._ID + " = ?";
                String[] selectionArgs = { Integer.toString(sub.getId()) };

                int count = db.update(SubEntry.TABLE_NAME, newCv, selection, selectionArgs);
                success &= count > 0;
            }
            else {
                success &= addSub(sub);
            }
        }

        return success;
    }

    @Unimplemented
    public boolean deleteSub(@NonNull Sub sub) {
        String selection = SubEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(sub.getId()) };

        long result = db.delete(SubEntry.TABLE_NAME, selection, selectionArgs);

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
        ContentValues cv = fillDistanceContentValues(distance);
        long result = db.insert(DistanceEntry.TABLE_NAME, null, cv);
        return success(result);
    }

    public boolean updateDistance(Distance distance) {
        ContentValues newCv = fillDistanceContentValues(distance);

        String selection = DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString((distance.getDistance())) };

        int count = db.update(DistanceEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    public boolean deleteDistance(@NonNull Distance distance) {
        String selection = DistanceEntry.COLUMN_DISTANCE + " = ?";
        String[] selectionArgs = { Integer.toString(distance.getDistance()) };

        long result = db.delete(DistanceEntry.TABLE_NAME, selection, selectionArgs);

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
     * @return The routeId of the added or already existing route
     */
    public long addRoute(@NonNull Route route, Context c) {
        Route existingRoute = DbReader.get(c).getRoute(route.getName());
        if (existingRoute != null) return existingRoute.getId();

        ContentValues cv = fillRouteContentValues(route);
        long id = db.insert(RouteEntry.TABLE_NAME, null, cv);
        return id;
    }

    /**
     * Updates a route. Merges with existing route if new name already exists.
     *
     * @param route Route to update
     * @return The routeId of the updated route; of mergee if merged, same as parameter otherwise
     */
    public int updateRoute(Route route, Context c) {
        Route oldRoute = DbReader.get(c).getRoute(route.getId());
        int existingIdForNewName = DbReader.get(c).getRouteId(route.getName());

        boolean nameNotChanged = route.getName().equals(oldRoute.getName());
        boolean newNameFree = existingIdForNewName == Route.ID_NON_EXISTANT;
        boolean dontMerge = nameNotChanged || newNameFree;

        // update route
        if (dontMerge) {
            ContentValues newCv = fillRouteContentValues(route);

            String selection = RouteEntry._ID + " = ?";
            String[] selectionArgs = { Integer.toString(route.getId()) };

            int count = db.update(RouteEntry.TABLE_NAME, newCv, selection, selectionArgs);

            return route.getId();
        }

        // merge routes, update routeId and delete merger
        else {
            // update routeId to mergeree routeId
            ContentValues newCv = new ContentValues();
            newCv.put(ExerciseEntry.COLUMN_ROUTE_ID, existingIdForNewName);

            String where = ExerciseEntry.COLUMN_ROUTE_ID + " = ?";
            String[] whereArgs = { Integer.toString(route.getId()) };

            int count = db.update(ExerciseEntry.TABLE_NAME, newCv, where, whereArgs);

            // delete merger route
            boolean deleteSuccess = deleteRoute(route.getId());

            return existingIdForNewName;
        }
    }

    public boolean deleteRoute(int routeId) {
        final String selection = RouteEntry._ID + " = ?";
        final String[] selectionArgs = { Integer.toString(routeId) };

        final long result = db.delete(RouteEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    /**
     * Update route name in exercises table. Call whenever updating route name via {@link #updateRoute(Route, Context)}.
     */
    @Deprecated
    public boolean updateRouteName(String oldName, String newName) {
        // TODO: remove this when routeName column is removed

        ContentValues newCv = new ContentValues();
        newCv.put(ExerciseEntry.COLUMN_ROUTE, newName);

        String selection = ExerciseEntry.COLUMN_ROUTE + " = ?";
        String[] selectionArgs = { oldName };

        int count = db.update(ExerciseEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    // places

    public boolean addPlaces(ArrayList<Place> places) {
        boolean success = true;
        for (Place p : places) {
            success &= addPlace(p);
        }
        return success;
    }

    public boolean addPlace(Place place) {
        ContentValues cv = fillPlaceContentValues(place);
        long result = db.insert(PlaceEntry.TABLE_NAME, null, cv);
        return success(result);
    }

    public boolean updatePlace(Place place) {
        ContentValues newCv = fillPlaceContentValues(place);

        String selection = DistanceEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString((place.getId())) };

        int count = db.update(PlaceEntry.TABLE_NAME, newCv, selection, selectionArgs);

        return count > 0;
    }

    public boolean deletePlace(@NonNull Place place) {
        String selection = PlaceEntry._ID + " = ?";
        String[] selectionArgs = { Integer.toString(place.getId()) };

        long result = db.delete(PlaceEntry.TABLE_NAME, selection, selectionArgs);

        return success(result);
    }

    public boolean regeneratePlaces(Context c) {
        return addPlaces(DbReader.get(c).generatePlaces());
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
    private ContentValues fillExerciseContentValues(@NonNull Exercise e, Context c) {
        ContentValues cv = new ContentValues();

        cv.put(ExerciseEntry.COLUMN_STRAVA_ID, e.getStravaId());
        cv.put(ExerciseEntry.COLUMN_GARMIN_ID, e.getGarminId());
        cv.put(ExerciseEntry.COLUMN_TYPE, e.getType());
        cv.put(ExerciseEntry.COLUMN_DATE, e.getEpoch());
        cv.put(ExerciseEntry.COLUMN_ROUTE_ID, e.getRouteId());
        cv.put(ExerciseEntry.COLUMN_ROUTE, e.getRoute());
        cv.put(ExerciseEntry.COLUMN_ROUTE_VAR, e.getRouteVar());
        cv.put(ExerciseEntry.COLUMN_INTERVAL, e.getInterval());
        cv.put(ExerciseEntry.COLUMN_NOTE, e.getNote());
        cv.put(ExerciseEntry.COLUMN_DEVICE, e.getDevice());
        cv.put(ExerciseEntry.COLUMN_RECORDING_METHOD, e.getRecordingMethod());
        cv.put(ExerciseEntry.COLUMN_DISTANCE, e.getDistance());
        cv.put(ExerciseEntry.COLUMN_EFFECTIVE_DISTANCE, e.getEffectiveDistance(c));
        cv.put(ExerciseEntry.COLUMN_TIME, e.getTime());
        cv.put(ExerciseEntry.COLUMN_TRAIL_HIDDEN, e.isTrailHidden());

        // put trail
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

        cv.put(SubEntry.COLUMN_SUPERID, sub.getSuperId());
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
    private ContentValues fillPlaceContentValues(@NonNull Place place) {
        ContentValues cv = new ContentValues();

        cv.put(PlaceEntry.COLUMN_NAME, place.getName());
        cv.put(PlaceEntry.COLUMN_LAT, place.getLat());
        cv.put(PlaceEntry.COLUMN_LNG, place.getLng());
        cv.put(PlaceEntry.COLUMN_RADIUS, place.getRadius());

        return cv;
    }

    @NonNull
    private ContentValues fillRouteContentValues(@NonNull Route route) {
        ContentValues cv = new ContentValues();

        if (route.getId() != Route.ID_NON_EXISTANT) {
            cv.put(RouteEntry._ID, route.getId());
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
