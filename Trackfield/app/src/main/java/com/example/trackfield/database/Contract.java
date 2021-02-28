package com.example.trackfield.database;

import android.provider.BaseColumns;

import com.example.trackfield.objects.Route;
import com.example.trackfield.toolbox.C;

public final class Contract {

    private Contract() {
    }

    // tools

    public static String sortOrder(boolean smallestFirst) {
        return smallestFirst ? " ASC" : " DESC";
    }

    // entries

    public static class ExerciseEntry implements BaseColumns {

        public static final String TABLE_NAME = "exercises";
        public static final String COLUMN_EXTERNAL_ID = "external_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ROUTE_ID = "route_id";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_ROUTEVAR = "routeVar";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_DATASOURCE = "data_source";
        public static final String COLUMN_RECORDINGMETHOD = "recording_method";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_EFFECTIVE_DISTANCE = "effective_distance";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_START_LAT = "start_lat";
        public static final String COLUMN_START_LNG = "start_lng";
        public static final String COLUMN_END_LAT = "end_lat";
        public static final String COLUMN_END_LNG = "end_lng";
        public static final String COLUMN_POLYLINE = "polyline";

        public static final String[] EXERLITE_COLUMNS = {
                _ID, COLUMN_DATE, COLUMN_ROUTE_ID, COLUMN_ROUTE, COLUMN_ROUTEVAR,
                COLUMN_INTERVAL, COLUMN_DISTANCE, COLUMN_EFFECTIVE_DISTANCE, COLUMN_TIME
        };
        public static final String[] TRAIL_COLUMNS = {
                COLUMN_POLYLINE, COLUMN_START_LAT, COLUMN_START_LNG,
                COLUMN_END_LAT, COLUMN_END_LNG
        };

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_EXTERNAL_ID + " INTEGER," +
                COLUMN_TYPE + " INTEGER," +
                COLUMN_DATE + " INTEGER," +
                COLUMN_ROUTE_ID + " INTEGER," +
                COLUMN_ROUTE + " TEXT," +
                COLUMN_ROUTEVAR + " TEXT," +
                COLUMN_INTERVAL + " TEXT," +
                COLUMN_NOTE + " TEXT," +
                COLUMN_DATASOURCE + " TEXT," +
                COLUMN_RECORDINGMETHOD + " TEXT," +
                COLUMN_DISTANCE + " INTEGER," +
                COLUMN_EFFECTIVE_DISTANCE + " INTEGER," +
                COLUMN_TIME + " REAL," +
                COLUMN_START_LAT + " REAL," +
                COLUMN_START_LNG + " REAL," +
                COLUMN_END_LAT + " REAL," +
                COLUMN_END_LNG + " REAL," +
                COLUMN_POLYLINE + " TEXT)";

        private static final String ALTER_TABLE_1 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN (" +
                COLUMN_EXTERNAL_ID + " INTEGER," +
                COLUMN_EFFECTIVE_DISTANCE + " INTEGER);";

        public static String getColumn(C.SortMode sortMode) {

            switch (sortMode) {
                case DATE:
                    return COLUMN_DATE;
                case DISTANCE:
                    return COLUMN_DISTANCE;
                case TIME:
                    return COLUMN_TIME;
                case PACE:
                    return COLUMN_TIME;
                case NAME:
                    return COLUMN_ROUTE;
                default:
                    return COLUMN_DATE;
            }
        }

        public static String orderBy(C.SortMode sortMode, boolean smallestFirst) {
            return getColumn(sortMode) + sortOrder(smallestFirst);
        }

    }

    public static class SubEntry implements BaseColumns {

        public static final String TABLE_NAME = "subs";
        public static final String COLUMN_SUPERID = "super_id";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_TIME = "time";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_SUPERID + " INTEGER," +
                COLUMN_DISTANCE + " INTEGER," +
                COLUMN_TIME + " REAL)";

    }

    public static class DistanceEntry implements BaseColumns {

        public static final String TABLE_NAME = "distances";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_BEST_TIME = "best_time";
        public static final String COLUMN_BEST_PACE = "best_pace";
        public static final String COLUMN_GOAL_PACE = "goal_pace";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_DISTANCE + " INTEGER," +
                COLUMN_BEST_TIME + " REAL," +
                COLUMN_BEST_PACE + " REAL," +
                COLUMN_GOAL_PACE + " REAL)";

    }

    public static class RouteEntry implements BaseColumns {

        public static final String TABLE_NAME = "routes";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_AVG_DISTANCE = "avg_distance";
        public static final String COLUMN_BEST_PACE = "best_pace";
        public static final String COLUMN_GOAL_PACE = "goal_pace";
        public static final String COLUMN_HIDDEN = "hidden";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_AMOUNT + " INTEGER," +
                COLUMN_AVG_DISTANCE + " INTEGER," +
                COLUMN_BEST_PACE + " REAL," +
                COLUMN_GOAL_PACE + " REAL," +
                COLUMN_HIDDEN + " INTEGER)";

        public static String getColumn(Route.SortMode sortMode) {

            switch (sortMode) {
                case RECENT:
                    return "";
                case NAME:
                    return COLUMN_NAME;
                case AMOUNT:
                    return COLUMN_AMOUNT;
                case AVG_DISTANCE:
                    return COLUMN_AVG_DISTANCE;
                case BEST_PACE:
                    return COLUMN_BEST_PACE;
            }
            return "";
        }

        public static String orderBy(Route.SortMode sortMode, boolean smallestFirst) {
            return getColumn(sortMode) + sortOrder(smallestFirst);
        }

    }

}
