package me.felwal.trackfield.data.db;

import android.provider.BaseColumns;

import me.felwal.trackfield.ui.common.model.SorterItem;

public final class DbContract {

    private DbContract() {
        // this utility class is not publicly instantiable
    }

    // entries

    public static class ExerciseEntry implements BaseColumns {

        public static final String TABLE_NAME = "exercises";
        public static final String COLUMN_STRAVA_ID = "strava_id";
        public static final String COLUMN_GARMIN_ID = "garmin_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ROUTE_ID = "route_id";
        @Deprecated public static final String COLUMN_ROUTE = "route";
        public static final String COLUMN_ROUTE_VAR = "route_var";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_DEVICE = "device";
        public static final String COLUMN_RECORDING_METHOD = "recording_method";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_EFFECTIVE_DISTANCE = "effective_distance";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_START_LAT = "start_lat";
        public static final String COLUMN_START_LNG = "start_lng";
        public static final String COLUMN_END_LAT = "end_lat";
        public static final String COLUMN_END_LNG = "end_lng";
        public static final String COLUMN_POLYLINE = "polyline";
        public static final String COLUMN_TRAIL_HIDDEN = "trail_hidden";

        public static final String SELECTION_PACE = "1000*(" + COLUMN_TIME + "/" + COLUMN_EFFECTIVE_DISTANCE + ")";

        public static final String[] COLUMNS_EXERLITE = {
            _ID, COLUMN_TYPE, COLUMN_DATE, COLUMN_ROUTE_ID, COLUMN_ROUTE_VAR, COLUMN_INTERVAL, COLUMN_DISTANCE,
            COLUMN_EFFECTIVE_DISTANCE, COLUMN_TIME, COLUMN_START_LAT, COLUMN_START_LNG
        };
        public static final String[] COLUMNS_TRAIL = {
            COLUMN_POLYLINE, COLUMN_START_LAT, COLUMN_START_LNG, COLUMN_END_LAT, COLUMN_END_LNG
        };

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_STRAVA_ID + " INTEGER," +
            COLUMN_GARMIN_ID + " INTEGER," +
            COLUMN_TYPE + " TEXT," +
            COLUMN_LABEL + " TEXT," +
            COLUMN_DATE + " INTEGER," +
            COLUMN_ROUTE_ID + " INTEGER," +
            COLUMN_ROUTE + " TEXT," +
            COLUMN_ROUTE_VAR + " TEXT," +
            COLUMN_INTERVAL + " TEXT," +
            COLUMN_NOTE + " TEXT," +
            COLUMN_DEVICE + " TEXT," +
            COLUMN_RECORDING_METHOD + " TEXT," +
            COLUMN_DISTANCE + " INTEGER," +
            COLUMN_EFFECTIVE_DISTANCE + " INTEGER," +
            COLUMN_TIME + " REAL," +
            COLUMN_START_LAT + " REAL," +
            COLUMN_START_LNG + " REAL," +
            COLUMN_END_LAT + " REAL," +
            COLUMN_END_LNG + " REAL," +
            COLUMN_POLYLINE + " TEXT," +
            COLUMN_TRAIL_HIDDEN + " INTEGER)";

        public static final String ALTER_TO_VER_2 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN (" +
            COLUMN_STRAVA_ID + " INTEGER," +
            COLUMN_EFFECTIVE_DISTANCE + " INTEGER);";

        public static String toString(String[] strings) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < strings.length; i++) {
                builder.append(strings[i]);
                if (i != strings.length - 1) builder.append(", ");
            }
            return builder.toString();
        }

        /**
         * Converts a {@link SorterItem.Mode}  to the first parameter of a SQL ORDER BY clause string.
         *
         * @param sortMode Mode to sort by
         * @return The column/columns with neccessary function calls, e.g. "time" or "(time / effective_distance)"
         *
         * @see DbReader#sortOrder(boolean)
         * @see DbReader#orderBy(SorterItem.Mode, boolean)
         */
        public static String sortColumn(SorterItem.Mode sortMode) {
            // column=0, column: sorts all with column = 0 last
            switch (sortMode) {
                case DISTANCE:
                    return COLUMN_EFFECTIVE_DISTANCE + "=0, " + COLUMN_EFFECTIVE_DISTANCE;
                case TIME:
                    return COLUMN_TIME + "=0, " + COLUMN_TIME;
                case PACE:
                    return COLUMN_TIME + "=0 OR " + COLUMN_EFFECTIVE_DISTANCE + "=0, " + SELECTION_PACE;
                case NAME:
                    return COLUMN_ROUTE;
                case START_LAT:
                    return COLUMN_START_LAT;
                case START_LNG:
                    return COLUMN_START_LNG;
                case DATE:
                default:
                    return COLUMN_DATE;
            }
        }

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

    }

    public static class PlaceEntry implements BaseColumns {

        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_HIDDEN = "hidden";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME + " TEXT," +
            COLUMN_LAT + " REAL," +
            COLUMN_LNG + " REAL," +
            COLUMN_HIDDEN + " INTEGER," +
            COLUMN_RADIUS + " INTEGER)";

    }

}
