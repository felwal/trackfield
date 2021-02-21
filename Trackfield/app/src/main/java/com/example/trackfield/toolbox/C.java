package com.example.trackfield.toolbox;

import com.example.trackfield.R;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;

// Consts
public class C {

    public enum Layout {
        EXERCISE,
        DISTANCE,
        ROUTE,
        EXERCISE_DISTANCE,
        EXERCISE_ROUTE;

        public static Layout fromInt(int i) {
            switch (i) {
                case 0: return EXERCISE;
                case 1: return DISTANCE;
                case 2: return ROUTE;
                case 3: return EXERCISE_DISTANCE;
                case 4: return EXERCISE_ROUTE;
            }
            return EXERCISE;
        }
    }
    public enum SortMode {
        DATE,
        DISTANCE,
        TIME,
        PACE,
        NAME,
        AMOUNT;

        public static SortMode fromInt(int i) {
            switch (i) {
                case 0: return DATE;
                case 1: return DISTANCE;
                case 2: return TIME;
                case 3: return PACE;
                case 4: return NAME;
                case 5: return AMOUNT;
            }
            return DATE;
        }
        public static int toInt(SortMode sortMode) {
            switch (sortMode) {
                case DATE: return 0;
                case DISTANCE: return 1;
                case TIME: return 2;
                case PACE: return 3;
                case NAME: return 4;
                case AMOUNT: return 5;
            }
            return 0;
        }

        public static int[] toInts(SortMode[] sortModes) {
            int[] ints = new int[sortModes.length];
            for (int i = 0; i < sortModes.length; i++) {
                ints[i] = toInt(sortModes[i]);
            }
            return ints;
        }
        public static SortMode[] fromInts(int[] ints) {
            SortMode[] sortModes = new SortMode[ints.length];
            for (int i = 0; i < ints.length; i++) {
                sortModes[i] = fromInt(ints[i]);
            }
            return sortModes;
        }
    }

    public enum UnitVelocity {
        METERS_PER_SECOND,
        KILOMETERS_PER_HOUR
    }
    public enum UnitEnergy {
        JOULES,
        CALORIES,
        WATTHOURS,
        ELECTRONVOLTS
    }

    // text

    public static final String TAB = "       "; // 7
    public static final char[] ARROWS = { '↓', '↑' };
    public static final String NO_VALUE = "—";
    public static final String NO_VALUE_TIME = "– : –";
    public static final String[] M = { "J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D" };

    // formatter
    public static final DateTimeFormatter FORMATTER_FILE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final DateTimeFormatter FORMATTER_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_SQL_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter FORMATTER_EDIT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATTER_EDIT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_VIEW = DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' HH:mm");
    public static final DateTimeFormatter FORMATTER_CAPTION = DateTimeFormatter.ofPattern("d MMM yyyy");
    public static final DateTimeFormatter FORMATTER_CAPTION_NOYEAR = DateTimeFormatter.ofPattern("d MMM");
    public static final DateTimeFormatter FORMATTER_REC = DateTimeFormatter.ofPattern("d MMM ’yy");
    public static final DateTimeFormatter FORMATTER_REC_NOYEAR = DateTimeFormatter.ofPattern("d MMMM");
    public static final DateTimeFormatter[] FORMATTERS_SEARCH = {
            DateTimeFormatter.ofPattern("dd MMMM yyyy"), DateTimeFormatter.ofPattern("yyyy MMMM dd"), DateTimeFormatter.ofPattern("yyyy MMMM d"),
            DateTimeFormatter.ofPattern("dd MMM yyyy"), DateTimeFormatter.ofPattern("yyyy MMM dd"), DateTimeFormatter.ofPattern("yyyy MMM d"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("yyyy/MM/dd") };

    public static final TemporalField WEEK_OF_YEAR = WeekFields.ISO.weekOfWeekBasedYear();
    public static final TemporalField DAY_OF_WEEK = WeekFields.ISO.dayOfWeek();

    // theme
    public static final ArrayList<String> themeNames = new ArrayList<>(Arrays.asList("Dark", "Light", "Set by Battery Saver"));
    public static final ArrayList<String> colorNames = new ArrayList<>(Arrays.asList("Mono", "Green"));
    public static final int[][] LOOKS = {
            { R.style.AppTheme_Dark_Mono, R.style.AppTheme_Dark_Green, R.style.AppTheme_Splash},
            { R.style.AppTheme_Light_Mono, R.style.AppTheme_Light_Green} };

}
