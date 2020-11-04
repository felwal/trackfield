package com.example.trackfield.database;

import android.provider.BaseColumns;

import com.example.trackfield.objects.Route;
import com.example.trackfield.toolbox.Toolbox.C;

public final class Contract {

    private Contract() {}

    public static String sortOrder(boolean smallestFirst) {
        return smallestFirst ? " ASC" : " DESC";
    }

    public static class ExerciseEntry implements BaseColumns {
        public static final String TABLE_NAME = "exercises";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ROUTE_ID = "routeId";
        public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_ROUTEVAR = "routeVar";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_DATASOURCE = "dataSource";
        public static final String COLUMN_RECORDINGMETHOD = "recordingMethod";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_TIME = "time";
        public static final String[] EXERLITE_COLUMNS = { _ID, COLUMN_DATE, COLUMN_ROUTE_ID, COLUMN_ROUTE, COLUMN_ROUTEVAR, COLUMN_INTERVAL, COLUMN_DISTANCE, COLUMN_TIME };

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_ID + " INTEGER," +
                COLUMN_TYPE + " INTEGER," +
                COLUMN_DATE + " TEXT," +
                COLUMN_ROUTE_ID + " INTEGER," +
                COLUMN_ROUTE + " TEXT," +
                COLUMN_ROUTEVAR + " TEXT," +
                COLUMN_INTERVAL + " TEXT," +
                COLUMN_NOTE + " TEXT," +
                COLUMN_DATASOURCE + " TEXT," +
                COLUMN_RECORDINGMETHOD + " TEXT," +
                COLUMN_DISTANCE + " INTEGER," +
                COLUMN_TIME + " REAL)";

        public static String getColumn(C.SortMode sortMode)  {

            switch (sortMode) {
                case DATE: return COLUMN_DATE;
                case DISTANCE: return COLUMN_DISTANCE;
                case TIME: return COLUMN_TIME;
                case PACE: return COLUMN_TIME;
                case NAME: return COLUMN_ROUTE;
            }
            return "";
        }
        public static String orderBy(C.SortMode sortMode, boolean smallestFirst) {
            return getColumn(sortMode) + sortOrder(smallestFirst);
        }

    }
    public static class SubEntry implements BaseColumns {
        public static final String TABLE_NAME = "subs";
        public static final String COLUMN_SUPERID = "superId";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_TIME = "time";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_SUPERID + " INTEGER," +
                COLUMN_DISTANCE + " INTEGER," +
                COLUMN_TIME + " REAL)";
    }
    public static class MapEntry implements BaseColumns {
        public static final String TABLE_NAME = "maps";
        public static final String COLUMN_SUPERID = "superId";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String[] ALL_BUT_SUPERID_COLUMNS = { _ID, COLUMN_TIME, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ALTITUDE};

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_SUPERID + " INTEGER," +
                COLUMN_TIME + " REAL," +
                COLUMN_LATITUDE + " REAL," +
                COLUMN_LONGITUDE + " REAL," +
                COLUMN_ALTITUDE + " REAL)";
    }

    public static class DistanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "distances";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_BEST_TIME = "bestTime";
        public static final String COLUMN_BEST_PACE = "bestPace";
        public static final String COLUMN_GOAL_PACE = "goalPace";

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
        public static final String COLUMN_AVG_DISTANCE = "avgDistance";
        public static final String COLUMN_BEST_PACE = "bestPace";
        public static final String COLUMN_GOAL_PACE = "goalPace";
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

        public static String getColumn(Route.SortMode sortMode)  {

            switch (sortMode) {
                case RECENT: return "";
                case NAME: return COLUMN_NAME;
                case AMOUNT: return COLUMN_AMOUNT;
                case AVG_DISTANCE: return COLUMN_AVG_DISTANCE;
                case BEST_PACE: return COLUMN_BEST_PACE;
            }
            return "";
        }
        public static String orderBy(Route.SortMode sortMode, boolean smallestFirst) {
            return getColumn(sortMode) + sortOrder(smallestFirst);
        }

    }

}
