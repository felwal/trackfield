package com.example.trackfield.utils;

import com.example.trackfield.R;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;

// Consts
public final class AppConsts {

    // enums

    public enum Layout {
        EXERCISES,
        DISTANCES,
        ROUTES,
        INTERVALS,
        DISTANCE,
        ROUTE,
        INTERVAL;

        public static Layout fromInt(int i) {
            switch (i) {
                case 0: return EXERCISES;
                case 1: return DISTANCES;
                case 2: return ROUTES;
                case 3: return INTERVALS;
                case 4: return DISTANCE;
                case 5: return ROUTE;
                case 6: return INTERVAL;
            }
            return EXERCISES;
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
    public static final String[] MONTH_M = { "J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D" };

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
        DateTimeFormatter.ofPattern("dd MMMM yyyy"), DateTimeFormatter.ofPattern("yyyy MMMM dd"),
        DateTimeFormatter.ofPattern("yyyy MMMM d"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"), DateTimeFormatter.ofPattern("yyyy MMM dd"),
        DateTimeFormatter.ofPattern("yyyy MMM d"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("yyyy/MM/dd") };

    public static final TemporalField WEEK_OF_YEAR = WeekFields.ISO.weekOfWeekBasedYear();
    public static final TemporalField DAY_OF_WEEK = WeekFields.ISO.dayOfWeek();

    // theme
    public static final ArrayList<String> themeNames = new ArrayList<>(Arrays.asList("Dark", "Light"));
    public static final ArrayList<String> colorNames = new ArrayList<>(Arrays.asList("Mono", "Green"));
    public static final int[][] LOOKS = {
        { R.style.AppTheme_Dark_Mono, R.style.AppTheme_Dark_Green, R.style.AppTheme_Splash },
        { R.style.AppTheme_Light_Mono, R.style.AppTheme_Light_Green } };

    //

    private AppConsts() {
        // this utility class is not publicly instantiable
    }

}
