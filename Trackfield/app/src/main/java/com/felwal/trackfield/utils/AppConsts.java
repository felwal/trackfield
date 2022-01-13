package com.felwal.trackfield.utils;

import com.felwal.trackfield.R;

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
        PLACES,
        INTERVALS,
        DISTANCE,
        ROUTE,
        PLACE,
        INTERVAL
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

    public static final String TAB = "       "; // 7 tabs
    public static final String NO_VALUE = "—";
    public static final String NO_VALUE_TIME = "– : –";

    // formatters
    public static final DateTimeFormatter FORMATTER_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_SQL_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter FORMATTER_EDIT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATTER_EDIT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_VIEW = DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' HH:mm");
    public static final DateTimeFormatter FORMATTER_CAPTION = DateTimeFormatter.ofPattern("d MMM yyyy");
    public static final DateTimeFormatter FORMATTER_CAPTION_NOYEAR = DateTimeFormatter.ofPattern("d MMM");
    public static final DateTimeFormatter FORMATTER_REC = DateTimeFormatter.ofPattern("d MMM ’yy");
    public static final DateTimeFormatter FORMATTER_REC_NOYEAR = DateTimeFormatter.ofPattern("d MMMM");

    public static final TemporalField WEEK_OF_YEAR = WeekFields.ISO.weekOfWeekBasedYear();

    // theme
    public static final ArrayList<String> themeNames = new ArrayList<>(
        Arrays.asList("Follow system", "Light", "Dark", "Set by Battery Saver"));
    public static final ArrayList<String> colorNames = new ArrayList<>(Arrays.asList("Mono", "Green"));
    public static final int[] COLORS = { R.style.Theme_Trackfield_Mono, R.style.Theme_Trackfield_Green };
    //

    private AppConsts() {
        // this utility class is not publicly instantiable
    }

}
