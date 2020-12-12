package com.example.trackfield.toolbox;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trackfield.R;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Distance;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.objects.Route;
import com.example.trackfield.objects.Sub;
import com.example.trackfield.objects.Trail;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.TreeMap;

public class Toolbox {

    // Consts
    public static class C {

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
        public static final int COLOR_MONO = 0;
        public static final int COLOR_GREEN = 1;
        public static final int[][] LOOKS = {
                { R.style.AppTheme_Dark_Mono, R.style.AppTheme_Dark_Green, R.style.AppTheme_Splash},
                { R.style.AppTheme_Light_Mono, R.style.AppTheme_Light_Green} };
        public static final int[] MAP_STYLES = { R.string.mapstyle_night_json, R.string.mapstyle_retro_json };

    }

    // Data
    public static class D {

        @Deprecated public static ArrayList<Exercise> exercises = new ArrayList<>();
        @Deprecated public static ArrayList<Integer> distances = new ArrayList<>();
        @Deprecated public static ArrayList<String> routes = new ArrayList<>();

        // stats & more
        @Deprecated public static int totalDistance = 0;
        @Deprecated public static float totalTime = 0;
        @Deprecated public static boolean gameOn = false;

        // bort
        @Deprecated public static int[] weeks;
        @Deprecated public static int weekAmount = 12;
        @Deprecated public static int distanceTopX = 3;

        // theme
        public static boolean updateTheme(Activity a) {
            int newTheme = C.LOOKS[M.heaviside(Prefs.isThemeLight())][Prefs.getColor()];
            try {
                int currentTheme = a.getPackageManager().getActivityInfo(a.getComponentName(), 0).getThemeResource();
                if (currentTheme != newTheme) {
                    a.setTheme(newTheme);
                    return true;
                }
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }
        public static MapStyleOptions getMapStyle(Context c) {
            return new MapStyleOptions(c.getResources().getString(C.MAP_STYLES[M.heaviside(Prefs.isThemeLight())]));
        }

        // sort
        public static ArrayList<Exercise> sortExercises(ArrayList<Exercise> listToSort, boolean smallestFirst, C.SortMode sortBy) {

            ArrayList<Exercise> sorted = new ArrayList<>(listToSort);
            for (int i = sorted.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // get values
                    float compareValue1 = 0;
                    float compareValue2 = 0;
                    switch (sortBy) {
                        case DATE: // date
                            compareValue1 = M.heaviside(sorted.get(j).getDate().isAfter(sorted.get(j+1).getDate()));
                            compareValue2 = M.heaviside(sorted.get(j+1).getDate().isAfter(sorted.get(j).getDate()));
                            break;
                        case DISTANCE:// distance
                            compareValue1 = sorted.get(j).distance();
                            compareValue2 = sorted.get(j+1).distance();
                            break;
                        case TIME: // time
                            compareValue1 = sorted.get(j).time();
                            compareValue2 = sorted.get(j+1).time();
                            break;
                        case PACE: // pace
                            compareValue1 = sorted.get(j).pace();
                            compareValue2 = sorted.get(j+1).pace();
                            break;
                        default:
                            break;
                    }

                    // compare values
                    boolean moveBack;
                    if (smallestFirst)  { moveBack = compareValue1 > compareValue2; }
                    else                { moveBack = compareValue1 < compareValue2; }

                    // move back
                    if (moveBack) {
                        Exercise temp = sorted.get(j+1);
                        sorted.set(j+1, sorted.get(j));
                        sorted.set(j, temp);
                    }
                }
            }

            return sorted;
        }
        public static ArrayList<Exercise> sortExercisesByDistance(ArrayList<Exercise> listToSort, boolean smallestFirst, C.SortMode sortBy) {

            ArrayList<Exercise> sorted = new ArrayList<>(listToSort);
            for (int i = sorted.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // get values
                    float compareValue1 = 0;
                    float compareValue2 = 0;
                    switch (sortBy) {
                        case DATE: // date
                            compareValue1 = M.heaviside(sorted.get(j).getDate().isAfter(sorted.get(j+1).getDate()));
                            compareValue2 = M.heaviside(sorted.get(j+1).getDate().isAfter(sorted.get(j).getDate()));
                            break;
                        case DISTANCE: // full distance
                            compareValue1 = sorted.get(j).distance();
                            compareValue2 = sorted.get(j+1).distance();
                            break;
                        case PACE: // time & pace (by distance)
                            //compareValue1 = sorted.get(j).getTimeByDistance(distance);
                            //compareValue2 = sorted.get(j+1).getTimeByDistance(distance);
                            compareValue1 = sorted.get(j).pace();
                            compareValue2 = sorted.get(j+1).pace();
                            break;
                        default:
                            break;
                    }

                    // compare values
                    boolean moveBack;
                    if (smallestFirst)  { moveBack = compareValue1 > compareValue2; }
                    else                { moveBack = compareValue1 < compareValue2; }

                    // move back
                    if (moveBack) {
                        Exercise temp = sorted.get(j+1);
                        sorted.set(j+1, sorted.get(j));
                        sorted.set(j, temp);
                    }
                }
            }

            return sorted;
        }
        public static ArrayList<String> sortRoutes(ArrayList<String> listToSort, boolean smallestFirst, C.SortMode sortBy, boolean alwaysIncludeLesser) {

            ArrayList<String> sorted = new ArrayList<>(listToSort);
            for (int i = sorted.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    if (!alwaysIncludeLesser && !Prefs.areHiddenRoutesShown() && filterByRoute(sorted.get(j)).size() <= 1) {
                        sorted.remove(j);
                        j--; i--; // räcker inte
                        continue;
                    }

                    // get values
                    float compareValue1 = 0;
                    float compareValue2 = 0;
                    switch (sortBy) {
                        case NAME: // name
                            compareValue1 = (float) M.sortValue(sorted.get(j));
                            compareValue2 = (float) M.sortValue(sorted.get(j+1));
                            break;
                        case AMOUNT: // amount
                            compareValue1 = filterByRoute(sorted.get(j)).size();
                            compareValue2 = filterByRoute(sorted.get(j+1)).size();
                            break;
                        case PACE: // best pace
                            compareValue1 = fastestPace(filterByRoute(sorted.get(j))).pace();
                            compareValue2 = fastestPace(filterByRoute(sorted.get(j+1))).pace();
                            break;
                        case DISTANCE: // average distance
                            compareValue1 = averageDistance(filterByRoute(sorted.get(j)));
                            compareValue2 = averageDistance(filterByRoute(sorted.get(j+1)));
                            break;
                        case DATE: // recent
                            boolean jBigger = sortExercises(filterByRoute(sorted.get(j)), false, C.SortMode.DATE).get(0).getDate().
                                    isAfter(sortExercises(filterByRoute(sorted.get(j+1)), false, C.SortMode.DATE).get(0).getDate());
                            compareValue1 = M.heaviside(jBigger);
                            compareValue2 = M.heaviside(!jBigger);
                            break;
                        default:
                            break;
                    }

                    // compare values
                    boolean moveBack;
                    if (smallestFirst)  { moveBack = compareValue1 > compareValue2; }
                    else                { moveBack = compareValue1 < compareValue2; }

                    // move back
                    if (moveBack) {
                        String temp = sorted.get(j+1);
                        sorted.set(j+1, sorted.get(j));
                        sorted.set(j, temp);
                    }
                }
            }

            return sorted;
        }
        public static ArrayList<Integer> sortDistances(ArrayList<Integer> listToSort, boolean smallestFirst, C.SortMode sortBy) {

            ArrayList<Integer> sorted = new ArrayList<>(listToSort);
            for (int i = sorted.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // get values
                    float compareValue1 = 0;
                    float compareValue2 = 0;
                    switch (sortBy) {
                        case DISTANCE: // distance
                            compareValue1 = sorted.get(j);
                            compareValue2 = sorted.get(j+1);
                            break;
                        case AMOUNT: // amount
                            compareValue1 = filterByDistance(sorted.get(j)).size();
                            compareValue2 = filterByDistance(sorted.get(j+1)).size();
                            break;
                        case TIME: // best time
                            compareValue1 = shortestTime(filterByDistance(sorted.get(j)), sorted.get(j)).timeByDistance(sorted.get(j));
                            compareValue2 = shortestTime(filterByDistance(sorted.get(j+1)), sorted.get(j+1)).timeByDistance(sorted.get(j+1));
                            break;
                        case PACE: // best pace
                            compareValue1 = fastestPace(filterByDistance(sorted.get(j))).pace();
                            compareValue2 = fastestPace(filterByDistance(sorted.get(j+1))).pace();
                            break;
                        default:
                            break;
                    }

                    // compare values
                    boolean moveBack;
                    if (smallestFirst)  { moveBack = compareValue1 > compareValue2; }
                    else                { moveBack = compareValue1 < compareValue2; }

                    // move back
                    if (moveBack) {
                        int temp = sorted.get(j+1);
                        sorted.set(j+1, sorted.get(j));
                        sorted.set(j, temp);
                    }
                }
            }

            return sorted;
        }
        public static void sortExerlites(ArrayList<Exerlite> listToSort, C.SortMode sortMode, boolean smallestFirst) {

            for (int i = listToSort.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // get values
                    float compareValue1 = 0;
                    float compareValue2 = 0;
                    switch (sortMode) {
                        case DATE: // date
                            compareValue1 = M.heaviside(listToSort.get(j).getDate().isAfter(listToSort.get(j+1).getDate()));
                            compareValue2 = M.heaviside(listToSort.get(j+1).getDate().isAfter(listToSort.get(j).getDate()));
                            break;
                        case DISTANCE:// distance
                            compareValue1 = listToSort.get(j).getDistance();
                            compareValue2 = listToSort.get(j+1).getDistance();
                            break;
                        case TIME: // time
                            compareValue1 = listToSort.get(j).getTime();
                            compareValue2 = listToSort.get(j+1).getTime();
                            break;
                        case PACE: // pace
                            compareValue1 = listToSort.get(j).getPace();
                            compareValue2 = listToSort.get(j+1).getPace();
                            break;
                        default:
                            break;
                    }

                    // move back
                    boolean moveBack = smallestFirst ? compareValue1 > compareValue2 : compareValue1 < compareValue2;;
                    if ((sortMode == C.SortMode.PACE || sortMode == C.SortMode.TIME)) {
                        if (compareValue2 == 0) moveBack = false;
                        else if (compareValue1 == 0) moveBack = true;
                    }
                    if (moveBack) {
                        Exerlite temp = listToSort.get(j+1);
                        listToSort.set(j+1, listToSort.get(j));
                        listToSort.set(j, temp);
                    }
                }
            }

        }

        // filter
        @Deprecated public static ArrayList<Exercise> filterByRoute(String route) {

            ArrayList<Exercise> filtered = new ArrayList<>();
            for (Exercise e : exercises) {
                if (e.getRoute().equalsIgnoreCase(route)) {
                    filtered.add(e);
                }
            }
            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByRoute(String route, String routeVar) {

            ArrayList<Exercise> filtered = new ArrayList<>();
            for (Exercise e : exercises) {
                if (e.getRoute().equalsIgnoreCase(route) && e.getRouteVar().equalsIgnoreCase(routeVar)) {
                    filtered.add(e);
                }
            }
            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByInterval(String interval) {

            ArrayList<Exercise> filtered = new ArrayList<>();
            for (Exercise e : exercises) {
                if (e.getInterval().equalsIgnoreCase(interval)) {
                    filtered.add(e);
                }
            }
            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByDistance(int distance) {

            ArrayList<Exercise> filteredByMin = filterByMinDistance(distance);
            ArrayList<Exercise> top10 = new ArrayList<>(sortExercises(filteredByMin, true, C.SortMode.PACE).subList(0, Math.min(distanceTopX, filteredByMin.size())));
            ArrayList<Exercise> filtered = new ArrayList<>(top10);
            for (Exercise e : exercises) {
                int eDistance = e.distance();
                if (M.insideLimits(eDistance, distance) && !filtered.contains(e)) filtered.add(e);
            }

            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByDistance(int distance, int type) {

            ArrayList<Exercise> filteredByMin = filterByMinDistance(distance);
            ArrayList<Exercise> top10 = filterByType(new ArrayList<>(sortExercises(filteredByMin, true, C.SortMode.PACE).subList(0, Math.min(distanceTopX, filteredByMin.size()))), type);
            ArrayList<Exercise> filtered = new ArrayList<>(top10);
            for (Exercise e : exercises) {
                int eDistance = e.distance();
                if (M.insideLimits(eDistance, distance) && !filtered.contains(e) && e.isType(type)) filtered.add(e);
            }

            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByMinDistance(int distance) {

            ArrayList<Exercise> filtered = new ArrayList<>();
            for (Exercise e : exercises) {
                if (e.distance() >= distance) {
                    filtered.add(e);
                }
            }
            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterByType(ArrayList<Exercise> list, int type) {

            ArrayList<Exercise> filtered = new ArrayList<>();
            for (Exercise e : list) {
                if (e.getType() == type) { filtered.add(e); }
            }

            return filtered;
        }
        @Deprecated public static ArrayList<Exercise> filterBySearch(String search) {

            search = search.toLowerCase();
            ArrayList<Exercise> filtered = new ArrayList<>();

            for (Exercise e : exercises) {
                if (e.getRoute().toLowerCase().contains(search) || e.getRouteVar().toLowerCase().contains(search) || e.getNote().toLowerCase().contains(search) ||
                        Exercise.TYPES[e.getType()].toLowerCase().contains(search) || e.getDataSource().toLowerCase().contains(search) || e.getRecordingMethod().toLowerCase().contains(search) ||
                        e.getDate().format(C.FORMATTERS_SEARCH[0]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[1]).toLowerCase().contains(search) ||
                        e.getDate().format(C.FORMATTERS_SEARCH[2]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[3]).toLowerCase().contains(search) ||
                        e.getDate().format(C.FORMATTERS_SEARCH[4]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[5]).toLowerCase().contains(search) ||
                        e.getDate().format(C.FORMATTERS_SEARCH[6]).toLowerCase().contains(search) || e.getDate().format(C.FORMATTERS_SEARCH[7]).toLowerCase().contains(search)) {
                    filtered.add(e);
                }
            }

            return filtered;
        }

        // compare
        @Deprecated public static Exercise shortestTime(ArrayList<Exercise> list, int distance) {

            Exercise shortest = list.get(0);
            for (Exercise e: list) {
                if (e.timeByDistance(distance) < shortest.timeByDistance(distance) && e.timeByDistance(distance) != 0) { shortest = e; }
            }
            return shortest;

        }
        @Deprecated public static Exercise fastestPace(ArrayList<Exercise> list) {

            Exercise fastest = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i).pace() != 0) {
                    fastest = list.get(i);
                }
            }

            for (Exercise e: list) {
                if (e.pace() < fastest.pace() && e.pace() != 0) { fastest = e; }
            }
            return fastest;

        }
        @Deprecated public static int averageDistance(ArrayList<Exercise> list) {

            int totalDistance = 0;
            int count = 0;
            for (Exercise e : list) {
                if (e.isDistanceDriven()) continue;
                totalDistance += e.distance();
                count++;
            }
            if (count == 0) { return 0; }
            return totalDistance / count;

        }
        @Deprecated public static int longestDistance(ArrayList<Exercise> list) {

            int longestDistance = 0;
            for (Exercise e: list) {
                int distance = e.distance();
                if (distance > longestDistance) {
                    longestDistance = distance;
                }
            }

            return longestDistance;
        }

        public static void markTop(ArrayList<Exerlite> list) {
            if (list.size() == 0) return;

            // get top
            int[] top = {-1,-1,-1};
            for (int i = 0; i < list.size(); i++) {
                Exerlite e = list.get(i);
                float pace = e.getPace();
                if (pace == 0) continue;

                if      (top[0] == -1 || pace < list.get(top[0]).getPace()) { top[2] = top[1]; top[1] = top[0]; top[0] = i; }
                else if (top[1] == -1 || pace < list.get(top[1]).getPace()) { top[2] = top[1]; top[1] = i; }
                else if (top[2] == -1 || pace < list.get(top[2]).getPace()) { top[2] = i; }
            }

            // mark
            if (top[2] != -1) list.get(top[2]).setTop(3);
            if (top[1] != -1) list.get(top[1]).setTop(2);
            if (top[0] != -1) list.get(top[0]).setTop(1);
        }
        public static void removeLonger(ArrayList<Exerlite> list, int maxDist) {

            // separate
            ArrayList<Exerlite> longer = new ArrayList<>();
            for (Exerlite e : list) if (e.getDistance() > maxDist) longer.add(e);
            list.removeAll(longer);

            // sort
            sortExerlites(list, C.SortMode.PACE, true);
            sortExerlites(longer, C.SortMode.PACE, true);

            // get top
            Exerlite[] top = {null,null,null};
            for (int i = 0; i < 3 && i < list.size(); i++) {
                Exerlite e = list.get(i);
                if (e.getPace() == 0) continue;
                top[i] = e;
            }
            for (int i = 0; i < 3 && i < longer.size(); i++) {
                Exerlite e = longer.get(i);
                if (e.getPace() == 0) continue;

                if      (top[0] == null || e.getPace() < top[0].getPace()) { top[2] = top[1]; top[1] = top[0]; top[0] = e; }
                else if (top[1] == null || e.getPace() < top[1].getPace()) { top[2] = top[1]; top[1] = e; }
                else if (top[2] == null || e.getPace() < top[2].getPace()) { top[2] = e; }
            }

            // readd
            if (top[0] != null && !list.contains(top[0])) list.add(top[0]);
            if (top[1] != null && !list.contains(top[1])) list.add(top[1]);
            if (top[2] != null && !list.contains(top[2])) list.add(top[2]);
        }

        // update data
        @Deprecated public static void addDistance(int d) {
            if (!distances.contains(d)) {
                distances.add(d);
                sortDistancesData();
            }
        }
        @Deprecated public static void sortRoutesData() {

            //trimRoutes();

            // bubblesort
            for (int i = routes.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // consider length
                    int charIndex = 0;
                    int shortestLength = routes.get(j).length();
                    if (routes.get(j+1).length() < routes.get(j).length()) {
                        shortestLength = routes.get(j+1).length();
                    }
                    while (charIndex < shortestLength-1 && routes.get(j).charAt(charIndex) == routes.get(j+1).charAt(charIndex)) {
                        charIndex++;
                    }

                    // move back j
                    if (routes.get(j).charAt(charIndex) > routes.get(j+1).charAt(charIndex) || (charIndex == shortestLength-1 && routes.get(j).length() > routes.get(j+1).length())) {
                        String temp = routes.get(j+1);
                        routes.set(j+1, routes.get(j));
                        routes.set(j, temp);
                    }
                }
            }

        }
        @Deprecated public static void sortDistancesData() {

            for (int i = distances.size()-1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {

                    // move back
                    if (distances.get(j) > distances.get(j+1)) {
                        int temp = distances.get(j+1);
                        distances.set(j+1, distances.get(j));
                        distances.set(j, temp);
                    }

                }
            }

        }
        @Deprecated public static void importRoutes() {

            routes.clear();
            for (Exercise e : exercises) {
                if (e.getType() == 0 && !routes.contains(e.getRoute())) {
                    routes.add(e.getRoute());
                }
            }

        }

        // charts
        @Deprecated public static float[] weekDailyDistance() {

            float[] distances = {0, 0, 0, 0, 0, 0, 0};
            LocalDate now = LocalDate.now();
            int week = now.get(C.WEEK_OF_YEAR);

            for (Exercise e : sortExercises(exercises, false, C.SortMode.DATE)) {
                if (e.getWeek() != week) { break; }
                distances[e.getDate().get(C.DAY_OF_WEEK)-1] += e.distance();
            }
            return distances;
        }
        @Deprecated public static float[] yearMonthlyDistance(int year) {

            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year+1, 1, 1).minusDays(1);

            float[] distances = new float[12];

            for (Exercise e : sortExercises(exercises, true, C.SortMode.DATE)) {
                LocalDate date = e.getDate();
                if (date.isBefore(startDate)) { continue; }
                if (date.isAfter(endDate)) { break; }
                distances[date.getMonthValue()-1] += e.distance();
            }
            return distances;
        }

    }

    // File
    public static class F {

        // file keys
        private static final String FILENAME_E = "exercises.txt";
        private static final String FILENAME_S = "subs.txt";
        private static final String FILENAME_R = "routes.txt";
        private static final String FILENAME_D = "distances.txt";
        private static final String FOLDER = "Trackfield";
        private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER + "/";
        private static final char DIV_READ = '•';
        private static final char DIV_WRITE = '•';

        // prefs keys
        public static final String SP_SHARED_PREFERENCES = "shared preferences";
        private static final String SP_PREFS = "prefs";

        public static void exportToExternal(Context c) {

            //D.moveExercise();
            //D.trimRoutes();

            try {
                // exercise
                java.io.File eFile = new java.io.File(PATH + FILENAME_E);
                java.io.File sFile = new java.io.File(PATH + FILENAME_S);
                FileOutputStream eFos = new FileOutputStream(eFile);
                FileOutputStream sFos = new FileOutputStream(sFile);
                OutputStreamWriter eWriter = new OutputStreamWriter(eFos);
                OutputStreamWriter sWriter = new OutputStreamWriter(sFos);
                for (Exercise e : Helper.getReader(c).getExercises()) {
                    eWriter.append(e.extractToFile(DIV_WRITE) + "\n");
                    for (int index = 0; index < e.getSubs().size(); index++) {
                        sWriter.append(e.getSub(index).extractToFile(DIV_WRITE, e.get_id(), index) + "\n");
                    }
                }
                eWriter.close(); eFos.flush(); eFos.close();
                sWriter.close(); sFos.flush(); sFos.close();

                // routes
                java.io.File rFile = new java.io.File(PATH + FILENAME_R);
                FileOutputStream rFos = new FileOutputStream(rFile);
                OutputStreamWriter rWriter = new OutputStreamWriter(rFos);
                for (Route r : Helper.getReader(c).getRoutes(C.SortMode.DATE, true, true)) {
                    rWriter.append(r.getName() + "\n");
                }
                rWriter.close(); rFos.flush(); rFos.close();

                // distance
                java.io.File dFile = new java.io.File(PATH + FILENAME_D);
                FileOutputStream dFos = new FileOutputStream(dFile);
                OutputStreamWriter dWriter = new OutputStreamWriter(dFos);
                for (Distance d : Helper.getReader(c).getDistances(Distance.SortMode.DISTANCE, true)) {
                    dWriter.append(d.getDistance() + "\n");
                }
                dWriter.close(); dFos.flush(); dFos.close();

                //Toast.makeText(c,"Done writing to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(c, e.getMessage(),Toast.LENGTH_LONG).show();
            }

        }
        public static void importFromExternal(Context c) {

            // are you sure?

            //D.exercises.clear();
            //D.routes.clear();
            //D.distances.clear();
            //Helper.getWriter(c).deleteAllExercises();
            Helper.getWriter(c).recreate();

            try {
                // sub
                ArrayList<ArrayList<Sub>> subSets = new ArrayList<>();
                ArrayList<Sub> subs = new ArrayList<>();

                java.io.File sFile = new java.io.File(PATH + FILENAME_S);
                if (!sFile.exists()) { return; }
                FileInputStream sFis = new FileInputStream(sFile);
                BufferedReader sReader = new BufferedReader(new InputStreamReader(sFis));
                String previousLine = null;
                String line = sReader.readLine();
                String nextLine = sReader.readLine();
                while (line != null) {

                    // values
                    int superId = -1;
                    int index = 0;
                    int distance = 0;
                    float time = 0;

                    // get values
                    int section = 0;
                    String temp = "";
                    for (int ch = 0; ch < line.length(); ch++) {
                        if (line.charAt(ch) != DIV_READ) {
                            temp += line.charAt(ch);
                        }
                        if (line.charAt(ch) == DIV_READ || ch == line.length()-1) {
                            switch (section) {
                                case 0: // superId
                                    superId = Integer.valueOf(temp);
                                    break;
                                case 1: // index
                                    index = Integer.valueOf(temp);
                                    break;
                                case 2: // distance
                                    distance = Integer.valueOf(temp);
                                    break;
                                case 3: // time
                                    time = M.round(Float.valueOf(temp), 2);
                                    break;
                                default: break;
                            }
                            temp = "";
                            section++;
                        }
                    }

                    // add exercise
                    if (superId != -1) {
                        if (index == 0 && previousLine != null) {
                            subSets.add(subs);
                            subs = new ArrayList<>();
                        }
                        subs.add(new Sub(-1, superId, distance, time));
                        if (nextLine == null) {
                            subSets.add(subs);
                        }
                    }

                    previousLine = line;
                    line = nextLine;
                    nextLine = sReader.readLine();

                }
                sReader.close(); sFis.close();

                // exercise
                java.io.File eFile = new java.io.File(PATH + FILENAME_E);
                if (!eFile.exists()) { return; }
                FileInputStream eFis = new FileInputStream(eFile);
                BufferedReader eReader = new BufferedReader(new InputStreamReader(eFis));
                while ((line = eReader.readLine()) != null) {

                    // values
                    int _id = -1;
                    int type = 0;
                    LocalDateTime date = LocalDateTime.now();//.parse("0001/01/01", C.FORMATTER_FILE);
                    String route = "";
                    String routeVar = "";
                    String interval = "";
                    int distance = 0;
                    float time = 0;
                    String dataSource = "";
                    String recordingMethod = "";
                    String note = "";
                    String startLat = "";
                    String startLng = "";
                    String endLat = "";
                    String endLng = "";
                    String polyline = "";

                    // get values
                    int section = 0;
                    String temp = "";
                    for (int ch = 0; ch < line.length(); ch++) {
                        if (line.charAt(ch) != DIV_READ) {
                            temp += line.charAt(ch);
                        }
                        if (line.charAt(ch) == DIV_READ || ch == line.length() - 1) {
                            switch (section) {
                                case 0: _id = Integer.parseInt(temp); break;
                                case 1: type = Integer.parseInt(temp); break;
                                case 2: date = M.ofEpoch(Long.parseLong(temp));//.parse(temp, C.FORMATTER_FILE); break;
                                case 3: route = temp; break;
                                case 4: routeVar = temp; break;
                                case 5: interval = temp; break;
                                case 6: distance = Integer.parseInt(temp); break;
                                case 7: time = Float.parseFloat(temp); break;
                                case 8: dataSource = temp; break;
                                case 9: recordingMethod = temp; break;
                                case 10: note = temp; break;
                                case 11: startLat = temp; break;
                                case 12: startLng = temp; break;
                                case 13: endLat = temp; break;
                                case 14: endLng = temp; break;
                                case 15: polyline = temp; break;
                                default: break;
                            }
                            temp = "";
                            section++;
                        }
                    }

                    // add exercise
                    if (_id != -1) {
                        int routeId = Helper.getReader(c).getRouteId(route);
                        Trail trail = null;
                        if (!polyline.equals("")) {
                            if (!startLat.equals("") && !startLng.equals("") && !endLat.equals("") && !endLng.equals("")) {
                                LatLng start = new LatLng(Double.parseDouble(startLat), Double.parseDouble(startLng));
                                LatLng end = new LatLng(Double.parseDouble(endLat), Double.parseDouble(endLng));
                                trail = new Trail(polyline, start, end);
                            }
                            else trail = new Trail(polyline);
                        }

                        Exercise e = new Exercise(_id, type, date, routeId, route, routeVar, interval, note, dataSource, recordingMethod, distance, time, getSubsBySuperId(subSets, _id), trail);
                        Helper.getWriter(c).addExercise(e, c);
                        //D.exercises.add(e);
                    }

                }
                eReader.close(); eFis.close();

                // route
                java.io.File rFile = new java.io.File(PATH + FILENAME_R);
                FileInputStream rFis = new FileInputStream(rFile);
                BufferedReader rReader = new BufferedReader(new InputStreamReader(rFis));
                while ((line = rReader.readLine()) != null) {
                    //D.routes.add(line);
                    Helper.getWriter(c).addRoute(new Route(-1, line), c);
                }
                rReader.close(); rFis.close();

                // distance
                java.io.File dFile = new java.io.File(PATH + FILENAME_D);
                FileInputStream dFis = new FileInputStream(dFile);
                BufferedReader dReader = new BufferedReader(new InputStreamReader(dFis));
                while ((line = dReader.readLine()) != null) {
                    //D.distances.add(Integer.valueOf(line));
                    Helper.getWriter(c).addDistance(new Distance(-1, Integer.parseInt(line)));
                }
                dReader.close(); dFis.close();

                //Toast.makeText(c,"Done reading to '" + PATH + "'", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(c, e.getMessage(),Toast.LENGTH_LONG).show();
            }

            //D.edited();

        }

        private static ArrayList<Sub> getSubsBySuperId(ArrayList<ArrayList<Sub>> subSets, int superId) {

            for (ArrayList<Sub> subSet : subSets) {
                if (subSet.get(0).get_superId() == superId) { return subSet; }
            }

            return new ArrayList<>();
        }

        // permissions
        public static boolean shouldAskPermissions(Context c) {
            return !permissionToStorage(c) || !permissionToLocation(c);
        }
        @TargetApi(23) public static void askPermissions(Activity a) {
            String[] permissions = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION" };
            int requestCode = 200;
            ActivityCompat.requestPermissions(a, permissions, requestCode);
        }
        public static boolean permissionToStorage(Context c) {
            return ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        }
        public static boolean permissionToLocation(Context c) {
            return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

    }

    // Math
    public static class M {

        public static final double GOLDEN_RATIO = 1.618033988749894;

        // real maths
        public static double sqr(double d) {
            return d * d;
        }
        public static int heaviside(float x) {
            return x < 0 ? 0 : 1;
        }
        public static int heaviside(boolean one) {
            return one ? 1 : 0;
        }
        public static float nonNegative(float f) {
            return f * heaviside(f);
        }
        public static int signum(float x) {
            return x > 0 ? 1 : (x < 0 ? -1 : 0);
        }
        public static int signum(boolean positive) {
            return positive ? 1 : -1;
        }

        public static float round(float f, int decimals) {
            BigDecimal bd = new BigDecimal(Float.toString(f));
            bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
            return bd.floatValue();
        }
        public static double round(double d, int decimals) {
            BigDecimal bd = new BigDecimal(Double.toString(d));
            bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
        }

        // ratios
        public static int goldenRatioSmall(int of) {
            return (int) (of / (1 + GOLDEN_RATIO));
        }
        public static int goldenRatioLarge(int of) {
            return (int) (of * GOLDEN_RATIO / (1 + GOLDEN_RATIO));
        }

        // strings
        public static String prefix(float value, int decimals, String unit) {

            String prefix = "";
            boolean integer = false;

            if (value >= Math.pow(10, 12)) {
                value /= Math.pow(10, 12);
                prefix = "T";
            }
            else if (value >= Math.pow(10, 9)) {
                value /= Math.pow(10, 9);
                prefix = "G";
            }
            else if (value >= Math.pow(10, 6)) {
                value /= Math.pow(10, 6);
                prefix = "M";
            }
            else if (value >= Math.pow(10, 3)) {
                value /= Math.pow(10, 3);
                prefix = "k";
            }
            else integer = true;

            value = round(value, decimals);
            return (integer ? Integer.toString((int) value) : value) + " " + prefix + unit;
        }
        public static String bigPrefix(float value, int E, String unit) {

            String str = Float.toString(value);
            int decimalIndex = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '.') {
                    decimalIndex = i;
                    break;
                }
            }
            int size = str.substring(0, decimalIndex).length() + E;

            String prefix;
            if (size <= 3) { return value + " "; }
            else if (size <= 6)  { prefix = "k"; }
            else if (size <= 9)  { prefix = "M"; }
            else if (size <= 12) { prefix = "G"; }
            else if (size <= 15) { prefix = "T"; }
            else if (size <= 18) { prefix = "P"; }
            else if (size <= 21) { prefix = "E"; }
            else if (size <= 24) { prefix = "Z"; }
            else if (size <= 27) { prefix = "Y"; }
            else if (size <= 30) { prefix = "!"; }
            else { prefix = "?"; }

            String strTrunc = str.substring(0, decimalIndex) + str.substring(decimalIndex+1);
            String strLong = strTrunc + "000";
            int preCommaLength = size % 3;
            if (preCommaLength == 0) { preCommaLength = 3; }
            String preComma = strLong.substring(0, preCommaLength);
            String postComma = strLong.substring(preCommaLength, preCommaLength+2);

            return preComma + "." + postComma + " " + prefix + unit;
        }
        public static String kiloPrefix(float value, int decimals, String unit) {
            float rounded = round(value / 1000f, decimals);
            return decimals == 0 ? (int) rounded + " k" + unit : rounded + " k" + unit;
        }
        public static String timePrefix(int seconds) {
            return seconds < 60 ? seconds + " s" : seconds < 3600 ? seconds / 60 + " min" : hours(seconds);
        }
        public static String stringTime(float timeToString, boolean round) {

            if (timeToString == 0) { return C.NO_VALUE_TIME; }

            float[] parts = getTimeParts(timeToString);
            float seconds = parts[0];
            int minutes = (int) parts[1];
            int hours = (int) parts[2];

            String hh = Integer.toString(hours);
            String mm = Integer.toString(minutes);
            String ssInt = Integer.toString((int) seconds);
            String ss;

            if (round || seconds == (int) seconds) {
                ss = Integer.toString((int) seconds); }
            else { ss = Float.toString(round(seconds,2)); }

            if (hours == 0) {
                hh = "";
                if (minutes == 0) {
                    mm = "";
                    if (seconds == 0) { ss = "0"; }
                    //else { ss = ss + " s"; }
                }
                else {
                    if (ssInt.length() < 2) { ss = "0" + ss; }
                    mm += ":"; } }
            else {
                while (mm.length() < 2) { mm = "0" + mm; }
                while (ss.length() < 2) { ss = "0" + ss; }
                hh += ":"; mm += ":"; }

            return hh + mm + ss;
        }
        public static String hours(int seconds) {
            return round((float) seconds / 3600, 1) + " h";
        }
        public static float seconds(int hours, int minutes, float seconds) {
            return hours * 3600 + minutes * 60 + seconds;
        }
        public static String drive(String s) {
            return "( " + s + " )";
        }
        public static String truncateString(String s, int maxLength) {
            return s.length() < maxLength ? s : s.substring(0,maxLength);
        }

        // convert / compute
        public static boolean intToBool(int i) {
            return i != 0;
        }
        public static int shortenInt(int i, int length) {
            if (Integer.toString(i).length() > length) {
                i = Integer.valueOf(Integer.toString(i).substring(0, length-1));
            }
            return i;
        }
        public static double arrayAvg(double[] arr, double ignoreIfEqualTo) {

            int length = arr.length;

            double total = 0;
            for (double d : arr) {
                if (d == ignoreIfEqualTo) length--;
                else total += d;
            }
            return length == 0 ? 0 : total / length;
        }
        public static double sortValue(String s) {

            double value = 0;
            for (int c = 0; c < s.length(); c++) {
                value += (int)s.toLowerCase().charAt(c) / Math.pow(1000, c);
            }

            return value;
        }
        public static float[] getTimeParts(float timeToSplit) {

            float seconds;
            int minutes;
            int hours;

            minutes = (int)(timeToSplit / 60);
            seconds = timeToSplit % 60;
            hours = minutes / 60;
            minutes = minutes % 60;

            return new float[] {seconds, minutes, hours};
        }

        // lists
        public static boolean treeMapsEquals(TreeMap<Float, Float> map1, TreeMap<Float, Float> map2) {

            for (TreeMap.Entry<Float, Float> entry : map1.entrySet()) {
                float key = entry.getKey();
                if (!map2.containsKey(key) || map2.get(key).floatValue() != entry.getValue().floatValue()) return false;
            }
            return true;
        }
        public static ArrayList<Integer> createList(int valueToAdd) {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(valueToAdd);
            return list;
        }

        // distance
        public static int minDistance(int distance) {
            return (int) nonNegative(distance - Prefs.distanceLowerLimit);
        }
        public static int maxDistance(int distance) {
            return distance + Prefs.distanceUpperLimit;
        }
        public static boolean insideLimits(int distance, int fitsInside) {
            return distance > minDistance(fitsInside) && distance < maxDistance(fitsInside);
        }
        public static boolean insideLimits(int distance, int fitsInside, boolean includeLonger) {
            return distance > M.minDistance(fitsInside) && (includeLonger || distance < M.maxDistance(fitsInside));
        }

        // dates
        public static LocalDateTime dateTime(LocalDate date) {
            return LocalDateTime.of(date, LocalTime.of(12,0));
        }
        public static LocalDateTime ofEpoch(long seconds) {
            return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
        }
        public static long epoch(LocalDateTime dateTime) {
            return dateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
        }
        public static LocalDateTime first(LocalDateTime a, LocalDateTime b) {
            return a.isBefore(b) ? a : b;
        }
        public static LocalDateTime last(LocalDateTime a, LocalDateTime b) {
            return a.isAfter(b) ? a : b;
        }

        public static LocalDateTime atStartOfWeek(LocalDate date) {
            return date.minusDays(date.getDayOfWeek().getValue() - 1).atStartOfDay();
        }
        public static LocalDateTime atEndOfWeek(LocalDate date) {
            return date.plusDays(7 - date.getDayOfWeek().getValue()).atTime(23, 59, 59);
        }
        public static LocalDateTime atStartOfMonth(LocalDate date) {
            return date.minusDays(date.getDayOfMonth() - 1).atStartOfDay();
        }
        public static LocalDateTime atEndOfMonth(LocalDate date) {
            return date.plusDays(date.lengthOfMonth() - date.getDayOfMonth()).atTime(23, 59, 59);
        }
        public static LocalDateTime atStartOfYear(LocalDate date) {
            return date.minusDays(date.getDayOfYear() - 1).atStartOfDay();
        }
        public static LocalDateTime atEndOfYear(LocalDate date) {
            return date.plusDays(date.lengthOfYear() - date.getDayOfYear()).atTime(23, 59, 59);
        }

        // map
        public static LatLng toLatLng(Location location) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }

    }

    // Layout
    public static class L {

        public static float scale;

        // screen
        public static void setScale(Context c) {
            try {
                scale = c.getResources().getDisplayMetrics().density;
            } catch (Exception e) {
                toast("Couldn't fetch display density: " + e.getMessage(), c);
            }
        }
        public static int getScreenWidth(Activity a) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
        public static int getScreenHeight(Activity a) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            a.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
        public static int px(float dp) {
            return (int) (dp * scale + 0.5f);
        }
        public static void transStatusBar(Window window) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // resources
        public static void ripple(View v, Context c) {
            int[] attrs = new int[] {R.attr.selectableItemBackground};
            TypedArray typedArray = c.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            v.setBackgroundResource(backgroundResource);
            typedArray.recycle();
        }
        public static int getBackgroundResourceFromAttr(int attr, Context c) {
            int[] attrs = new int[] { attr };
            TypedArray typedArray = c.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            return backgroundResource;
        }
        @ColorInt public static int getColorInt(int resId, Context context) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(resId, typedValue, true);
            return typedValue.data;
        }
        public static void setColor(TextView view, String fulltext, String subtext, int color) {
            view.setText(fulltext, TextView.BufferType.SPANNABLE);
            Spannable str = (Spannable) view.getText();
            int i = fulltext.indexOf(subtext);
            str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // toast
        public static void toast(String s, Context c) {
            Toast.makeText(c, s, Toast.LENGTH_LONG).show();
        }
        public static void toast(boolean b, Context c) {
            if (b) return;
            Toast.makeText(c, "success: " + b, Toast.LENGTH_SHORT).show();
        }
        public static void handleError(Exception e, Context c) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        public static void handleError(String desc, Exception e, Context c) {
            Toast.makeText(c, desc + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // check
        public static void checkReader(Helper.Reader reader, Context c) {
            if (reader == null) { reader = new Helper.Reader(c); }
        }
        public static void setVisibleOrGone(View v, boolean visible) {
            v.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        public static void setVisibleOrInvisible(View v, boolean visible) {
            v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        public static boolean isEmpty(EditText et) {
            if (et.getText().toString().trim().length() > 0) return false;
            return true;
        }

        // animate
        private static final int ANIM_DURATION = 100;
        private static final int ANIM_DURATION_LONG = 250;
        private static final int ANIM_DURATION_RECYCLER = 175;

        public static void crossfade(View view, float toAlpha) {
            view.animate().alpha(toAlpha).setDuration(ANIM_DURATION).setListener(null);
        }
        public static void crossfadeIn(View view, float toAlpha) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(toAlpha).setDuration(ANIM_DURATION).setListener(null);
        }
        public static void crossfadeOut(final View view) {
            view.animate().alpha(0f).setDuration(ANIM_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
        }

        public static void crossfadeRecycler(View recycler) {
            recycler.setAlpha(0f);
            recycler.animate().alpha(1f).setDuration(ANIM_DURATION_RECYCLER).setListener(null);
        }

        public static void animateHeight(final View view, int toHeight) {

            ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), toHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = value;
                    view.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(ANIM_DURATION_LONG);
            anim.start();

        }
        public static void animateHeight(final View view, int expandedHeight, int collapsedHeight, boolean expand) {
            animateHeight(view, expand ? expandedHeight : collapsedHeight);
        }
        public static void animateHeight(final View view, int expandedHeight, boolean expand) {
            animateHeight(view, expandedHeight, 0, expand);
        }

        public static void animateColor(final View view, @ColorInt int fromColor, @ColorInt int toColor) {

            @SuppressLint("ObjectAnimatorBinding")
            final ObjectAnimator colorFade = ObjectAnimator.ofInt(view, "background", fromColor, toColor);
            colorFade.setDuration(ANIM_DURATION);
            colorFade.setEvaluator(new ArgbEvaluator());
            colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    view.setBackgroundColor(animatedValue);
                }
            });
            colorFade.start();
        }
        public static void animateColor(final View view, @ColorInt int disabledColor, @ColorInt int enabledColor, boolean enabled) {
            animateColor(view, enabled ? disabledColor : enabledColor, enabled ? enabledColor : disabledColor);
        }
        public static void animateFab(final FloatingActionButton target, @ColorInt int fromColor, @ColorInt int toColor, Drawable toIcon) {

            @SuppressLint("ObjectAnimatorBinding")
            final ObjectAnimator colorFade = ObjectAnimator.ofInt(target, "backgroundTint", fromColor, toColor);
            colorFade.setDuration(ANIM_DURATION);
            colorFade.setEvaluator(new ArgbEvaluator());
            colorFade.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    target.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                }
            });
            colorFade.start();
            target.setImageDrawable(toIcon);
        }

    }

}
